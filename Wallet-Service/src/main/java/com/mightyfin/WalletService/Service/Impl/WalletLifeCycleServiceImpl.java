package com.mightyfin.WalletService.Service.Impl;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mightyfin.WalletService.Dto.request.ActivateWalletRequest;
import com.mightyfin.WalletService.Dto.request.ChangeWalletTypeRequest;
import com.mightyfin.WalletService.Dto.request.UpdateWalletLimitRequest;
import com.mightyfin.WalletService.Dto.response.WalletResponse;
import com.mightyfin.WalletService.Dto.response.WalletTypeHistoryResponse;
import com.mightyfin.WalletService.Exception.BusinessRuleException;
import com.mightyfin.WalletService.Repo.WalletLimitIncreaseRequestRepo;
import com.mightyfin.WalletService.Repo.WalletRepo;
import com.mightyfin.WalletService.Repo.WalletTypeHistoryRepo;
import com.mightyfin.WalletService.Security.WalletAccessGuard;
import com.mightyfin.WalletService.Security.WalletScope;
import com.mightyfin.WalletService.Security.exception.WalletSecurityException;
import com.mightyfin.WalletService.Service.ReferenceDataResolver;
import com.mightyfin.WalletService.Service.WalletLifeCycleService;
import com.mightyfin.WalletService.Service.rule.WalletActivationValidator;
import com.mightyfin.WalletService.Service.rule.WalletTypeRules;
import com.mightyfin.WalletService.entity.Wallet;
import com.mightyfin.WalletService.entity.WalletType;
import com.mightyfin.WalletService.entity.WalletTypeHistory;
import com.mightyfin.WalletService.event.WalletEventRecorder;
import com.mightyfin.WalletService.mapper.WalletMapper;
import com.mightyfinance.auth.starter.security.AuthPrincipal;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@Service 
public class WalletLifeCycleServiceImpl implements  WalletLifeCycleService{


    private final WalletRepo walletRepository;
    private final WalletTypeHistoryRepo historyRepository;
    private final ReferenceDataResolver referenceData;
    private final WalletActivationValidator activationValidator;
    private final WalletAccessGuard guard;
    private final WalletEventRecorder events;
    private final WalletMapper mapper;
    private final WalletLimitIncreaseRequestRepo limitRequestRepository;
    private final Clock clock;

    @Override
    @Transactional 
    public WalletResponse activate(Integer walletId, ActivateWalletRequest request, AuthPrincipal caller) {
        guard.requireSuperAdmin(caller);
        Wallet wallet = requireWalletForUpdate(walletId);
        activationValidator.validate(wallet);

        boolean firstActivation = wallet.getVerifiedAt() == null;
        wallet.activate(Instant.now(clock));

        events.walletActivated(wallet, firstActivation, request.getApprovalReference(), caller);
        return mapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse suspend(Integer walletId, AuthPrincipal caller) {
        guard.requireSuperAdmin(caller);
        Wallet wallet = requireWalletForUpdate(walletId);
        if (!wallet.isActive()) {
            throw new BusinessRuleException("WALLET_NOT_ACTIVE", "Wallet " + walletId + " is not active");
        }
        wallet.suspend();

        events.walletSuspended(wallet, caller);
        return mapper.toResponse(wallet);
    }

    /**
     * One DB transaction: lock the wallet, close the current history row, insert the new one, point the wallet
     * at the new type. Any failure rolls all of it back, including the outbox event.
     */
    @Override
    @Transactional
    public WalletResponse changeType(Integer walletId, ChangeWalletTypeRequest request, AuthPrincipal caller) {
        guard.requireSuperAdmin(caller);
        Wallet wallet = requireWalletForUpdate(walletId);
        WalletType newType = referenceData.requireActiveWalletType(request.getWalletTypeCode());
        WalletType oldType = wallet.getWalletType();

        if (oldType.getId().equals(newType.getId())) {
            throw new BusinessRuleException("WALLET_TYPE_UNCHANGED",
                    "Wallet " + walletId + " already has type '" + newType.getCode() + "'");
        }
        WalletTypeRules.requireCompatible(newType, wallet.isCustomerWallet());

        WalletTypeHistory current = historyRepository.findByWalletIdAndEffectiveToIsNull(walletId)
                .orElseThrow(() -> new IllegalStateException("Wallet " + walletId + " has no current type history row"));

        // effective_to must be strictly after effective_from (DB check), so never close a row at its own start.
        Instant now = Instant.now(clock);
        Instant changeAt = now.isAfter(current.getEffectiveFrom())
                ? now : current.getEffectiveFrom().plus(1, ChronoUnit.MICROS);

        // Close first and flush: the partial unique index allows only one open row per wallet.
        current.close(changeAt);
        historyRepository.saveAndFlush(current);
        WalletTypeHistory next = historyRepository.save(WalletTypeHistory.open(wallet, newType, changeAt));
        current.supersededBy(next.getId());

        wallet.changeType(newType);

        events.walletTypeChanged(wallet, oldType.getCode(), caller);
        return mapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse updateGeneralTransactionLimit(Integer walletId, UpdateWalletLimitRequest request,
                                                        AuthPrincipal caller) {
        guard.requireSuperAdmin(caller);
        Wallet wallet = requireWalletForUpdate(walletId);

        java.math.BigDecimal previousLimit = wallet.getGeneralTransactionLimit();
        String previousPeriod = wallet.getGeneralTransactionPeriod() == null
                ? null : wallet.getGeneralTransactionPeriod().name();
        wallet.setGeneralTransactionLimit(request.getGeneralTransactionLimit());
        wallet.setGeneralTransactionPeriod(request.getGeneralTransactionPeriod());

        events.walletLimitChanged(wallet, previousLimit, previousPeriod, caller);

        // An open limit increase request was made against the old limit. Close it, so it can never be approved later
        // over the number the administrator just set. (Wallet lock first, then request: the same order as everywhere.)
        limitRequestRepository.findPendingByWalletId(walletId).ifPresent(pending -> {
            pending.supersede(caller.userId(), "The wallet's limit was changed directly by an administrator.", Instant.now(clock));
            limitRequestRepository.save(pending);
            // events.limitIncreaseSuperseded(pending);
        });
        return mapper.toResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTypeHistoryResponse> getTypeHistory(Integer walletId, AuthPrincipal caller) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(WalletSecurityException.WalletNotFound::new);
        guard.assertCanAccess(caller, WalletScope.of(wallet));
        return historyRepository.findByWalletIdOrderByEffectiveFromDescIdDesc(walletId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    private Wallet requireWalletForUpdate(Integer id) {
        return walletRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + id));
    }

    
}
