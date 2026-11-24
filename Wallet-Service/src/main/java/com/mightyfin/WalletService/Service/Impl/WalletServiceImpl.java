package com.mightyfin.WalletService.Service.Impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mightyfin.WalletService.Config.WalletCreationProperties;
import com.mightyfin.WalletService.Dto.request.CreateBusinessWalletRequest;
import com.mightyfin.WalletService.Dto.request.CreateCustomerWalletRequest;
import com.mightyfin.WalletService.Dto.response.WalletResponse;
import com.mightyfin.WalletService.Exception.DuplicateResourceException;
import com.mightyfin.WalletService.Repo.WalletRepo;
import com.mightyfin.WalletService.Repo.WalletTypeHistoryRepo;
import com.mightyfin.WalletService.Security.WalletAccessGuard;
import com.mightyfin.WalletService.Security.WalletRole;
import com.mightyfin.WalletService.Security.WalletScope;
import com.mightyfin.WalletService.Security.exception.WalletSecurityException;
import com.mightyfin.WalletService.Service.ReferenceDataResolver;
import com.mightyfin.WalletService.Service.WalletService;
import com.mightyfin.WalletService.Service.rule.WalletActivationValidator;
import com.mightyfin.WalletService.Service.rule.WalletTypeRules;
import com.mightyfin.WalletService.entity.Currency;
import com.mightyfin.WalletService.entity.Wallet;
import com.mightyfin.WalletService.entity.WalletType;
import com.mightyfin.WalletService.entity.WalletTypeHistory;
import com.mightyfin.WalletService.entity.Enum.WalletTypeCode;
import com.mightyfin.WalletService.event.WalletEventRecorder;
import com.mightyfin.WalletService.mapper.WalletMapper;
import com.mightyfinance.auth.starter.security.AuthPrincipal;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@Service 
public class WalletServiceImpl implements WalletService {


    /** Unique indexes enforcing one wallet per customer / per business. */
    private static final String USER_OWNERSHIP_INDEX = "uq_wallets_user_id";
    private static final String BUSINESS_OWNERSHIP_INDEX ="uq_wallets_business_public_uuid";

    /** Approval reference recorded when a wallet is automatically activated. */
    static final String AUTO_ACTIVATION_REFERENCE = "AUTO-ON-CREATE";

    private final WalletRepo walletRepository;
    private final WalletTypeHistoryRepo historyRepository;
    private final ReferenceDataResolver referenceData;
    private final WalletAccessGuard guard;
    private final WalletEventRecorder events;
    private final WalletMapper mapper;
    private final WalletActivationValidator activationValidator;
    private final WalletCreationProperties creationProperties;
    private final Clock clock;

    // ------------------------------------------------------------
    // Create
    // ------------------------------------------------------------

    @Override
    @Transactional
    public WalletResponse createCustomerWallet(CreateCustomerWalletRequest request,AuthPrincipal caller) {

        guard.requireSuperAdmin(caller);

        if (walletRepository.existsByUserId(request.getUserId())) {
            throw duplicateCustomerWallet(request.getUserId());
        }

        WalletType type = referenceData.requireActiveWalletType(WalletTypeCode.CUSTOMER.name());

        Currency currency = referenceData.requireActiveCurrency(request.getCurrencyCode());

        return open(newWallet(request.getUserId(), null, type, currency),
                type,
                creationProperties.isAutoActivateCustomers(),
                caller
        );
    }

    @Override
    @Transactional
    public WalletResponse createBusinessWallet(CreateBusinessWalletRequest request,AuthPrincipal caller) {

        guard.requireSuperAdmin(caller);

        if (walletRepository.existsByBusinessPublicUuid(request.getBusinessPublicUuid())) {
            throw duplicateBusinessWallet(request.getBusinessPublicUuid());
        }

        WalletType type = referenceData.requireActiveWalletType(request.getWalletTypeCode());

        WalletTypeRules.requireCompatible(type, false);

        Currency currency = referenceData.requireActiveCurrency(request.getCurrencyCode());

        return open(
                newWallet(null, request.getBusinessPublicUuid(), type, currency),
                type,
                false,
                caller
        );
    }

    // ------------------------------------------------------------
    // Read
    // ------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getById(Integer walletId, AuthPrincipal caller) {

        Wallet wallet = walletRepository.findById(walletId).orElseThrow(WalletSecurityException.WalletNotFound::new);

        guard.assertCanAccess(caller, WalletScope.of(wallet));

        return mapper.toResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getCurrentCustomerWallet(AuthPrincipal caller) {

        if (!WalletRole.INDIVIDUAL.equals(caller.role())|| caller.userId() == null) {
            throw new WalletSecurityException.WalletNotFound();
        }

        Wallet wallet = walletRepository.findByUserId(caller.userId()).orElseThrow(WalletSecurityException.WalletNotFound::new);

        guard.assertCanAccess(caller, WalletScope.of(wallet));

        return mapper.toResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getBusinessWallet(UUID businessPublicUuid,AuthPrincipal caller) {

        Wallet wallet = walletRepository.findByBusinessPublicUuid(businessPublicUuid)
                .orElseThrow(WalletSecurityException.WalletNotFound::new);

        guard.assertCanAccess(caller, WalletScope.of(wallet));

        return mapper.toResponse(wallet);
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    /**
     * Creates a wallet with default configuration.
     * New wallets have zero balance and are inactive until activated.
     */
    private Wallet newWallet(Integer userId,UUID businessPublicUuid,WalletType type,Currency currency) {

        Wallet wallet = new Wallet();

        wallet.setUserId(userId);
        wallet.setBusinessPublicUuid(businessPublicUuid);
        wallet.setWalletType(type);
        wallet.setCurrency(currency);
        wallet.setBalance(BigDecimal.ZERO);

        wallet.setCreditLimit(type.getDefaultCreditLimit() != null? type.getDefaultCreditLimit(): BigDecimal.ZERO);

        wallet.setGeneralTransactionLimit(type.getDefaultGeneralTransactionLimit());

        wallet.setGeneralTransactionPeriod(type.getDefaultGeneralTransactionPeriod());

        wallet.setDisbursementQuotaLimit(
                type.isCanExtendCredit()
                        && type.getDefaultDisbursementQuotaLimit() != null
                                ? type.getDefaultDisbursementQuotaLimit()
                                : BigDecimal.ZERO
        );

        wallet.setDisbursementQuotaUsed(BigDecimal.ZERO);

        wallet.setDisbursementQuotaPeriod(type.getDefaultDisbursementQuotaPeriod());

        return wallet;
    }

    /**
     * Saves the wallet, records its initial type history and creation event.
     * If auto-activation is enabled, validates and activates the wallet.
     * All operations execute in the same transaction.
     */
    private WalletResponse open(Wallet wallet,WalletType type,boolean activateNow,AuthPrincipal caller) {

        Wallet saved = saveRejectingDuplicateOwner(wallet);

        // Record initial wallet type history first.
        historyRepository.save(WalletTypeHistory.open(saved, type, Instant.now(clock)));

        // Record wallet creation event.
        events.walletCreated(saved);

        if (activateNow) {

            // Validate before activation.
            activationValidator.validate(saved);

            // Activate wallet.
            saved.activate(Instant.now(clock));

            // Record automatic activation event.
            events.walletActivated(saved,true,AUTO_ACTIVATION_REFERENCE,caller);
        }

        return mapper.toResponse(saved);
    }

    /**
     * Handles concurrent duplicate wallet creation using database
     * unique constraints.
     */
    private Wallet saveRejectingDuplicateOwner(Wallet wallet) {

        try {
            return walletRepository.save(wallet);

        } catch (DataIntegrityViolationException ex) {

            String cause = String.valueOf(ex.getMostSpecificCause().getMessage());

            if (cause.contains(USER_OWNERSHIP_INDEX)) {
                throw duplicateCustomerWallet(wallet.getUserId());
            }

            if (cause.contains(BUSINESS_OWNERSHIP_INDEX)) {
                throw duplicateBusinessWallet(wallet.getBusinessPublicUuid());
            }

            throw ex;
        }
    }

    private static DuplicateResourceException duplicateCustomerWallet(Integer userId) {

        return new DuplicateResourceException("A wallet already exists for user " + userId);
}

    private static DuplicateResourceException duplicateBusinessWallet(UUID businessPublicUuid) {

        return new DuplicateResourceException("A wallet already exists for business " + businessPublicUuid);
    }

}
