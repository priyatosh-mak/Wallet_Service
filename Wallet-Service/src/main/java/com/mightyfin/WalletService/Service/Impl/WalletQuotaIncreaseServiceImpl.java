package com.mightyfin.WalletService.Service.Impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mightyfin.WalletService.Config.QuotaIncreaseProperties;
import com.mightyfin.WalletService.Dto.request.ApproveQuotaIncreaseRequest;
import com.mightyfin.WalletService.Dto.request.RejectLimitIncreaseRequest;
import com.mightyfin.WalletService.Dto.request.SubmitQuuotaIncreaseRequest;
import com.mightyfin.WalletService.Dto.response.PageResponse;
import com.mightyfin.WalletService.Dto.response.QuotaIncreaseResponse;
import com.mightyfin.WalletService.Exception.BusinessRuleException;
import com.mightyfin.WalletService.Repo.WalletLimitAdjustmentRepo;
import com.mightyfin.WalletService.Repo.WalletOwnerView;
import com.mightyfin.WalletService.Repo.WalletQuotaIncreaseRequestRepo;
import com.mightyfin.WalletService.Repo.WalletRepo;
import com.mightyfin.WalletService.Security.WalletAccessGuard;
import com.mightyfin.WalletService.Security.WalletRole;
import com.mightyfin.WalletService.Security.WalletScope;
import com.mightyfin.WalletService.Security.exception.WalletSecurityAudit;
import com.mightyfin.WalletService.Security.exception.WalletSecurityException;
import com.mightyfin.WalletService.Service.WalletQuotaIncreaseService;
import com.mightyfin.WalletService.Service.rule.MoneyAmounts;
import com.mightyfin.WalletService.entity.Wallet;
import com.mightyfin.WalletService.entity.WalletQuotaIncreaseRequest;
import com.mightyfin.WalletService.entity.Enum.LimitIncreaseStatus;
import com.mightyfin.WalletService.entity.Enum.QuotaKind;
import com.mightyfin.WalletService.event.WalletEventRecorder;
import com.mightyfinance.auth.starter.security.AuthPrincipal;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class WalletQuotaIncreaseServiceImpl implements WalletQuotaIncreaseService {

    private static final Set<String> REQUESTER_ROLES =
            Set.of(WalletRole.INDIVIDUAL, WalletRole.BUSINESS_ADMIN, WalletRole.EMBEDDED_ADMIN);

    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 50;

    private final WalletRepo walletRepository;
    private final WalletQuotaIncreaseRequestRepo requestRepository;
    private final WalletLimitAdjustmentRepo adjustmentRepository;
    private final WalletAccessGuard guard;
    private final WalletEventRecorder events;
    private final WalletSecurityAudit audit;
    private final QuotaIncreaseProperties properties;
    private final Clock clock;
    @Override
    @Transactional 
    public QuotaIncreaseResponse submit(Integer walletId, QuotaKind kind, SubmitQuuotaIncreaseRequest request,
                                        AuthPrincipal caller) {
        requireRequester(caller);
        Wallet wallet = authorizeThenLock(walletId, caller);
        Instant now = Instant.now(clock);

        if (!wallet.isActive()) {
            throw new BusinessRuleException("WALLET_NOT_ACTIVE", "Wallet is not active.");
        }
        requireQuotaUsable(wallet, kind);

        BigDecimal requested = MoneyAmounts.requireValid(request.getRequestedIncrease());
        MoneyAmounts.requireFitsCurrency(requested, wallet.getCurrency());
        requireWithinCeiling(wallet.getQuotaRemaining(kind).add(requested));
        String reason = cleanReason(request.getReason());

        Optional<WalletQuotaIncreaseRequest> open = requestRepository.findPending(walletId, kind);
        if (open.isPresent()) {
            WalletQuotaIncreaseRequest existing = open.get();
            if (existing.isExpired(now)) {
                existing.expire(now);
                requestRepository.saveAndFlush(existing);   // flushed so the new row never meets the old one in the unique index
                events.quotaIncreaseExpired(existing);
            } else if (Objects.equals(existing.getRequestedByUserId(), caller.userId())
                    && existing.getRequestedIncrease().compareTo(requested) == 0) {
                return QuotaIncreaseResponse.from(existing);
            } else {
                throw new BusinessRuleException("QUOTA_REQUEST_ALREADY_PENDING",
                        "A request for this quota is already waiting for a decision. "
                                + "Cancel it first if you want to ask for a different amount.");
            }
        }
        requireCooldownOver(walletId, kind, now);

        WalletQuotaIncreaseRequest saved = requestRepository.save(WalletQuotaIncreaseRequest.submit(
                wallet, kind, caller.userId(), caller.role(), wallet.getQuotaRemaining(kind), requested, reason,
                now.plus(Duration.ofDays(properties.getExpiryDays()))));
        events.quotaIncreaseSubmitted(saved);
        audit.adminOperation(caller, "QUOTA_INCREASE_SUBMIT", String.valueOf(saved.getId()));
        return QuotaIncreaseResponse.from(saved);
    }

    @Override
    public QuotaIncreaseResponse cancel(Integer requestId, AuthPrincipal caller) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cancel'");
    }

    @Override
    public QuotaIncreaseResponse approve(Integer requestId, ApproveQuotaIncreaseRequest request, AuthPrincipal caller) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'approve'");
    }

    @Override
    public QuotaIncreaseResponse reject(Integer requestId, RejectLimitIncreaseRequest request, AuthPrincipal caller) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reject'");
    }

    @Override
    public QuotaIncreaseResponse get(Integer requestId, AuthPrincipal caller) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public PageResponse<QuotaIncreaseResponse> listForWallet(Integer walletId, int page, int size,
            AuthPrincipal caller) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listForWallet'");
    }

    @Override
    public PageResponse<QuotaIncreaseResponse> listAll(LimitIncreaseStatus status, int page, int size,
            AuthPrincipal caller) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listAll'");
    }

    @Override
    public boolean expire(Integer requestId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'expire'");
    }

    private void requireRequester(AuthPrincipal caller) {
        if (!REQUESTER_ROLES.contains(caller.role()) || caller.userId() == null) {
            guard.requireNotPlatformAdmin(caller);
            audit.accessDenied(caller, "quota_request_role_not_allowed");
            throw new WalletSecurityException.Forbidden();
        }
    }

    private Wallet authorizeThenLock(Integer walletId, AuthPrincipal caller) {
        WalletOwnerView owner = walletRepository.findOwnerById(walletId).orElseThrow(WalletSecurityException.WalletNotFound::new);
        guard.assertCanAccess(caller, scopeOf(owner));
        return walletRepository.findByIdForUpdate(walletId).orElseThrow(WalletSecurityException.WalletNotFound::new);
    }

    private Wallet lockWalletOf(Integer requestId) {
        Integer walletId = requestRepository.findWalletIdById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Quota request not found"));
        return walletRepository.findByIdForUpdate(walletId).orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    }

    private WalletQuotaIncreaseRequest lockRequest(Integer requestId) {
        Supplier<RuntimeException> notFound = () -> new ResourceNotFoundException("Quota request not found");
        return requestRepository.findByIdForUpdate(requestId).orElseThrow(notFound);
    }

    /** The gate is on and the wallet type still allows it. Asking to grow a quota that cannot be used is pointless. */
    private static void requireQuotaUsable(Wallet wallet, QuotaKind kind) {
        if (!wallet.canUseQuota(kind)) {
            throw new BusinessRuleException("QUOTA_NOT_ENABLED",
                    kind.name().toLowerCase() + " quota is not enabled for this wallet. Contact support.");
        }
    }

    private static WalletScope scopeOf(WalletOwnerView owner) {
        return new WalletScope(owner.getUserId(), owner.getBusinessPublicUuid(), null);
    }

    private static void requirePending(WalletQuotaIncreaseRequest request, String action) {
        if (!request.isPending()) {
            throw new BusinessRuleException("INVALID_REQUEST_STATE",
                    "Cannot " + action + " a request that is " + request.getStatus() + ".");
        }
    }

    private void requireWithinCeiling(BigDecimal resultingRemaining) {
        BigDecimal ceiling = properties.getMaxQuotaRemaining();
        if (ceiling != null && resultingRemaining.compareTo(ceiling) > 0) {
            throw new BusinessRuleException("QUOTA_ABOVE_MAXIMUM", "That would put the quota above the maximum this service allows.");
        }
    }

    private void requireCooldownOver(Integer walletId, QuotaKind kind, Instant now) {
        List<WalletQuotaIncreaseRequest> latest = requestRepository.findDecided(walletId, kind,
            List.of(LimitIncreaseStatus.APPROVED, LimitIncreaseStatus.REJECTED), null);
        if (latest.isEmpty()) {
            return;
        }
        WalletQuotaIncreaseRequest last = latest.get(0);
        int hours = last.getStatus() == LimitIncreaseStatus.REJECTED
                ? properties.getCooldownAfterRejectionHours() : properties.getCooldownAfterApprovalHours();
        Instant retryAt = last.getDecidedAt().plus(Duration.ofHours(hours));
        if (retryAt.isAfter(now)) {
            throw new BusinessRuleException("QUOTA_REQUEST_COOLDOWN", "You can ask again after " + retryAt + ".");
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

    private static Pageable pageOf(int page, int size) {
        int safeSize = size < 1 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        return PageRequest.of(Math.max(page, 0), safeSize);
    }


}
