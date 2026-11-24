package com.mightyfin.WalletService.Service.Impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.mightyfin.WalletService.Config.LimitIncreaseProperties;
import com.mightyfin.WalletService.Dto.request.ApproveLimitIncreaseRequest;
import com.mightyfin.WalletService.Dto.request.RejectLimitIncreaseRequest;
import com.mightyfin.WalletService.Dto.request.SubmitLimitIncreaseRequest;
import com.mightyfin.WalletService.Dto.response.LimitIncreaseResponse;
import com.mightyfin.WalletService.Exception.BusinessRuleException;
import com.mightyfin.WalletService.Repo.WalletLimitIncreaseRequestRepo;
import com.mightyfin.WalletService.Repo.WalletOwnerView;
import com.mightyfin.WalletService.Repo.WalletRepo;
import com.mightyfin.WalletService.Security.WalletAccessGuard;
import com.mightyfin.WalletService.Security.WalletRole;
import com.mightyfin.WalletService.Security.WalletScope;
import com.mightyfin.WalletService.Security.exception.WalletSecurityAudit;
import com.mightyfin.WalletService.Security.exception.WalletSecurityException;
import com.mightyfin.WalletService.Service.WalletLimitIncreaseService;
import com.mightyfin.WalletService.Service.rule.MoneyAmounts;
import com.mightyfin.WalletService.entity.Wallet;
import com.mightyfin.WalletService.entity.WalletLimitIncreaseRequest;
import com.mightyfin.WalletService.entity.Enum.LimitIncreaseStatus;
import com.mightyfin.WalletService.entity.Enum.WalletLimitPeriod;
import com.mightyfin.WalletService.event.WalletEventRecorder;
import com.mightyfinance.auth.starter.security.AuthPrincipal;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@Service 
public class WalletLimitImcreaseServiceImpl implements WalletLimitIncreaseService{

    private static final Set<String> REQUESTER_ROLES =Set.of(WalletRole.INDIVIDUAL,WalletRole.BUSINESS_ADMIN,WalletRole.EMBEDDED_ADMIN);

    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 90;

    private final WalletRepo walletRepo;
    private final WalletLimitIncreaseRequestRepo  walletLimitIncreaseRequestRepo;
    private final WalletAccessGuard walletAccessGuard;
    private final WalletEventRecorder walletEventRecorder;
    private final WalletSecurityAudit walletSecurityAudit;
    private final LimitIncreaseProperties limitIncreaseProperties;
    private final Clock clock;


    
    @Override
    @Transactional 
    public LimitIncreaseResponse submit(Integer walletId, SubmitLimitIncreaseRequest request, AuthPrincipal caller) {
        requireRequester(caller);
        Wallet wallet = authorizeThenLock(walletId, caller);
        Instant now = Instant.now(clock);

        if (!wallet.isActive()) {
            throw new BusinessRuleException("WALLET_NOT_ACTIVE", "Wallet is not active.");
        }
        BigDecimal current = wallet.getGeneralTransactionLimit();
        WalletLimitPeriod period = wallet.getGeneralTransactionPeriod();
        if (current == null || period == null) {
            throw new BusinessRuleException("LIMIT_NOT_CONFIGURED",
                    "This wallet has no transaction limit to raise. Contact support.");
        }

        BigDecimal requested = MoneyAmounts.requireValid(request.getRequestedLimit());
        MoneyAmounts.requireFitsCurrency(requested, wallet.getCurrency());
        requireHigher(requested, current, "requestedLimit");
        requireWithinCeiling(requested);
        String reason = cleanReason(request.getReason());

        // One open request per wallet. A stale one is closed first; a repeat of the same request is not an error.
        Optional<WalletLimitIncreaseRequest> open = walletLimitIncreaseRequestRepo.findPendingByWalletId(walletId);
        if (open.isPresent()) {
            WalletLimitIncreaseRequest existing = open.get();
            if (existing.isExpired(now)) {
                existing.expire(now);
                // flushed now: Hibernate writes inserts before updates, and the new row must not meet the old one in the unique index
                walletLimitIncreaseRequestRepo.saveAndFlush(existing);
                walletEventRecorder.limitIncreaseExpired(existing);
            } else if (Objects.equals(existing.getRequestedByUserId(), caller.userId())
                    && existing.getRequestedLimit().compareTo(requested) == 0) {
                return LimitIncreaseResponse.from(existing);
            } else {
                throw new BusinessRuleException("LIMIT_REQUEST_ALREADY_PENDING",
                        "A limit increase request for this wallet is already waiting for a decision. "
                                + "Cancel it first if you want to ask for a different amount.");
            }
        }

        requireCooldownOver(walletId, now);

        WalletLimitIncreaseRequest saved = walletLimitIncreaseRequestRepo.save(WalletLimitIncreaseRequest.submit(
                wallet, caller.userId(), caller.role(), period, current, requested, reason,
                now.plus(Duration.ofDays(limitIncreaseProperties.getExpiryDays()))));
        walletEventRecorder.limitIncreaseSubmitted(saved);
        walletSecurityAudit.adminOperation(caller, "LIMIT_INCREASE_SUBMIT", String.valueOf(saved.getId()));
        return LimitIncreaseResponse.from(saved);
    }



    @Override
    @Transactional
    public LimitIncreaseResponse cancel(Integer requestId, AuthPrincipal caller) {
        requireRequester(caller);
        Integer walletId = walletLimitIncreaseRequestRepo.findWalletIdById(requestId).orElseThrow(WalletSecurityException.WalletNotFound::new);
        authorizeThenLock(walletId, caller);
        WalletLimitIncreaseRequest request = lockRequest(requestId, WalletSecurityException.WalletNotFound::new);

        if (request.getStatus() == LimitIncreaseStatus.CANCELLED) {
            return LimitIncreaseResponse.from(request);
        }
        requirePending(request, "cancel");

        request.cancel(caller.userId(), Instant.now(clock));
        walletLimitIncreaseRequestRepo.save(request);
        walletEventRecorder.limitIncreaseCancelled(request);
        walletSecurityAudit.adminOperation(caller, "LIMIT_INCREASE_CANCEL", String.valueOf(requestId));
        return LimitIncreaseResponse.from(request);
    }

   @Override
    @Transactional
    public LimitIncreaseResponse approve(Integer requestId, ApproveLimitIncreaseRequest body, AuthPrincipal caller) {
        walletAccessGuard.requireSuperAdmin(caller);
        Wallet wallet = lockWalletOf(requestId);
        WalletLimitIncreaseRequest request = lockRequest(requestId, () -> new ResourceNotFoundException("Limit request not found"));
        Instant now = Instant.now(clock);

        if (request.getStatus() == LimitIncreaseStatus.APPROVED) {
            return LimitIncreaseResponse.from(request);
        }
        requirePending(request, "approve");

        if (Objects.equals(caller.userId(), request.getRequestedByUserId())) {
            throw new BusinessRuleException("SELF_APPROVAL_NOT_ALLOWED", "You cannot approve your own request.");
        }
        if (request.isExpired(now)) {
            throw new BusinessRuleException("REQUEST_EXPIRED", "This request has expired. Ask the requester to submit a new one.");
        }
        if (!wallet.isActive()) {
            throw new BusinessRuleException("WALLET_NOT_ACTIVE",
                    "Wallet is not active. Reactivate it before approving, or reject the request.");
        }
        BigDecimal current = wallet.getGeneralTransactionLimit();
        if (current == null || current.compareTo(request.getCurrentLimit()) != 0
                || wallet.getGeneralTransactionPeriod() != request.getPeriod()) {
            throw new BusinessRuleException("REQUEST_OUT_OF_DATE",
                    "The wallet's limit has changed since this request was made. Reject it and ask for a new one.");
        }

        BigDecimal approved = body == null || body.getApprovedLimit() == null
                ? request.getRequestedLimit()
                : MoneyAmounts.requireValid(body.getApprovedLimit());
        MoneyAmounts.requireFitsCurrency(approved, wallet.getCurrency());
        if (approved.compareTo(request.getRequestedLimit()) > 0) {
            throw new BusinessRuleException("APPROVED_ABOVE_REQUESTED", "You cannot approve more than was requested.");
        }
        requireHigher(approved, current, "approvedLimit");
        requireWithinCeiling(approved);

        String note = body == null ? null : blankToNull(body.getNote());
        WalletLimitPeriod period = wallet.getGeneralTransactionPeriod();
        wallet.changeGeneralTransactionLimit(approved, period);
        walletRepo.save(wallet);

        request.approve(approved, caller.userId(), note, now);
        walletLimitIncreaseRequestRepo.save(request);

        walletEventRecorder.walletLimitChanged(wallet, current, period.name(), caller);
        walletEventRecorder.limitIncreaseApproved(request);
        walletSecurityAudit.adminOperation(caller, "LIMIT_INCREASE_APPROVE", String.valueOf(requestId));
        return LimitIncreaseResponse.from(request);
    }

    @Override
    public LimitIncreaseResponse reject(Integer requestId, RejectLimitIncreaseRequest request, AuthPrincipal caller) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reject'");
    }

    @Override
    public LimitIncreaseResponse get(Integer requestId, AuthPrincipal caller) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public boolean expire(Integer requestId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'expire'");
    }



    private void requireRequester(AuthPrincipal caller){
        if(!REQUESTER_ROLES.contains(caller.role()) || caller.userId() == null  ){
            walletAccessGuard.requireNotPlatformAdmin(caller);
            walletSecurityAudit.accessDenied(caller, "limit_request_role_not_allowed");
            throw new WalletSecurityException.Forbidden();
        }
    }

    private Wallet authorizeThenLock(Integer walletId,AuthPrincipal caller){
        WalletOwnerView wallet = walletRepo.findOwnerById(walletId).orElseThrow(WalletSecurityException.WalletNotFound::new);
        walletAccessGuard.assertCanAccess(caller,scopeOf(wallet));
        return walletRepo.findByIdForUpdate(walletId).orElseThrow(WalletSecurityException.WalletNotFound::new);

    }

    private Wallet lockWalletOf(Integer requestId){
        Integer walletId = walletLimitIncreaseRequestRepo.findWalletIdById(requestId).orElseThrow(() -> new ResourceNotFoundException("Limit request not found"));
        return walletRepo.findByIdForUpdate(walletId).orElseThrow(()-> new ResourceNotFoundException("Wallet not found"));
    }

    private WalletLimitIncreaseRequest lockRequest(Integer requestId, java.util.function.Supplier<? extends RuntimeException> notFound) {
        return walletLimitIncreaseRequestRepo.findByIdForUpdate(requestId).orElseThrow(notFound);
    }



    private static WalletScope scopeOf(WalletOwnerView owner) {
        return new WalletScope(owner.getUserId(), owner.getBusinessPublicUuid(), null);
    }

    private static void requirePending(WalletLimitIncreaseRequest request, String action) {
        if (!request.isPending()) {
            throw new BusinessRuleException("INVALID_REQUEST_STATE",
                    "Cannot " + action + " a request that is " + request.getStatus() + ".");
        }
    }

    private static void requireHigher(BigDecimal value, BigDecimal current, String field) {
        if (value.compareTo(current) <= 0) {
            throw new BusinessRuleException("LIMIT_NOT_HIGHER", field + " must be higher than the current limit.");
        }
    }

     private void requireWithinCeiling(BigDecimal value) {
        BigDecimal ceiling = limitIncreaseProperties.getMaxGeneralLimit();
        if (ceiling != null && value.compareTo(ceiling) > 0) {
            throw new BusinessRuleException("LIMIT_ABOVE_MAXIMUM", "That limit is above the maximum this service allows.");
        }
    }
    
    private void requireCooldownOver(Integer walletId, Instant now) {
        List<WalletLimitIncreaseRequest> latest = walletLimitIncreaseRequestRepo.findDecided(
            walletId, List.of(LimitIncreaseStatus.APPROVED, LimitIncreaseStatus.REJECTED), null);
        if (latest.isEmpty()) {
            return;
        }
        WalletLimitIncreaseRequest last = latest.get(0);
        int hours = last.getStatus() == LimitIncreaseStatus.REJECTED
                ? limitIncreaseProperties.getCooldownAfterRejectionHours()
                : limitIncreaseProperties.getCooldownAfterApprovalHours();
        Instant retryAt = last.getDecidedAt().plus(Duration.ofHours(hours));
        if (retryAt.isAfter(now)) {
            throw new BusinessRuleException("LIMIT_REQUEST_COOLDOWN",
                    "You can ask again after " + retryAt + ".");
        }
    }

    private static String cleanReason(String reason) {
        String cleaned = reason == null ? "" : reason.trim();
        if (cleaned.length() < 10 || cleaned.length() > 500) {
            throw new BusinessRuleException("INVALID_REASON", "Reason must be between 10 and 500 characters.");
        }
        return cleaned;
    }

    private static String blankToNull(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }



}
