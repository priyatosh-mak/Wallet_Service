package com.mightyfin.WalletService.Service.Impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;

import com.mightyfin.WalletService.Dto.request.CreateBusinessWalletRequest;
import com.mightyfin.WalletService.Dto.request.CreateCustomerWalletRequest;
import com.mightyfin.WalletService.Dto.response.WalletResponse;
import com.mightyfin.WalletService.Exception.DuplicateResourceException;
import com.mightyfin.WalletService.Repo.WalletRepo;
import com.mightyfin.WalletService.Repo.WalletTypeHistoryRepo;
import com.mightyfin.WalletService.Security.WalletAccessGuard;
import com.mightyfin.WalletService.Service.ReferenceDataResolver;
import com.mightyfin.WalletService.Service.WalletService;
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
public class WalletServiceImpl implements WalletService {


    /** Unique indexes from V5 that enforce one wallet per customer / per business. */
    private static final String USER_OWNERSHIP_INDEX = "uq_wallets_user_id";
    private static final String BUSINESS_OWNERSHIP_INDEX = "uq_wallets_business_public_uuid";

    private final WalletRepo walletRepository;
    private final WalletTypeHistoryRepo historyRepository;
    private final ReferenceDataResolver referenceData;
    private final WalletAccessGuard guard;
    private final WalletEventRecorder events;
    private final WalletMapper mapper;
    private final Clock clock;


    @Override
    public WalletResponse createCustomerWallet(CreateCustomerWalletRequest request, AuthPrincipal caller) {
        guard.requireSuperAdmin(caller);
        if (walletRepository.existsByUserId(request.getUserId())) {
            throw new DuplicateResourceException("A wallet for the specified user already exists.");
        }
        WalletType type = referenceData.requireActiveWalletType(WalletTypeCode.CUSTOMER.name());
        Currency currency = referenceData.requireActiveCurrency(request.getCurrencyCode());

        return open(newWallet(request.getUserId(), null, type, currency), type);
    }

    @Override
    public WalletResponse createBusinessWallet(CreateBusinessWalletRequest request, AuthPrincipal caller) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createBusinessWallet'");
    }

    @Override
    public WalletResponse getById(Integer walletId, AuthPrincipal caller) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }

    @Override
    public WalletResponse getCurrentCustomerWallet(AuthPrincipal caller) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCurrentCustomerWallet'");
    }

    @Override
    public WalletResponse getBusinessWallet(UUID businessPublicUuid, AuthPrincipal caller) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBusinessWallet'");
    }

    private WalletResponse open(Wallet wallet, WalletType type) {
        Wallet saved = saveRejectingDuplicateOwner(wallet);
        historyRepository.save(WalletTypeHistory.open(saved, type, Instant.now(clock)));
        events.walletCreated(saved);
        return mapper.toResponse(saved);
    }



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

    


    private Wallet newWallet(Integer userId, UUID businessPublicUuid, WalletType type, Currency currency) {

        Wallet wallet = new Wallet();

    wallet.setUserId(userId);
    wallet.setBusinessPublicUuid(businessPublicUuid);
    wallet.setWalletType(type);
    wallet.setCurrency(currency);
    wallet.setBalance(BigDecimal.ZERO);
    wallet.setCreditLimit(type.getDefaultCreditLimit() != null ? type.getDefaultCreditLimit() : BigDecimal.ZERO);
    wallet.setGeneralTransactionLimit(type.getDefaultGeneralTransactionLimit());
    wallet.setGeneralTransactionPeriod(type.getDefaultGeneralTransactionPeriod());
    wallet.setDisbursementQuotaLimit(
            type.isCanExtendCredit() && type.getDefaultDisbursementQuotaLimit() != null
                    ? type.getDefaultDisbursementQuotaLimit()
                    : BigDecimal.ZERO
    );
    wallet.setDisbursementQuotaUsed(BigDecimal.ZERO);
    wallet.setDisbursementQuotaPeriod(type.getDefaultDisbursementQuotaPeriod());

    return wallet;
}

private static DuplicateResourceException duplicateCustomerWallet(Integer userId) {
        return new DuplicateResourceException("A wallet already exists for user " + userId);
    }

    private static DuplicateResourceException duplicateBusinessWallet(UUID businessPublicUuid) {
        return new DuplicateResourceException("A wallet already exists for business " + businessPublicUuid);
    }

}
