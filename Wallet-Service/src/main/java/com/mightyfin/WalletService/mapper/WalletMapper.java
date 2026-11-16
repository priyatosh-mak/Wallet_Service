package com.mightyfin.WalletService.mapper;

import com.mightyfin.WalletService.Dto.response.CurrencySummary;
import com.mightyfin.WalletService.Dto.response.WalletActivation;
import com.mightyfin.WalletService.Dto.response.WalletLimits;
import com.mightyfin.WalletService.Dto.response.WalletResponse;
import com.mightyfin.WalletService.Dto.response.WalletTypeSummary;
import com.mightyfin.WalletService.entity.Currency;
import com.mightyfin.WalletService.entity.Wallet;
import com.mightyfin.WalletService.entity.WalletType;

public class WalletMapper {

    public WalletResponse toResponse(Wallet wallet) {

        WalletType type = wallet.getWalletType();
        Currency currency = wallet.getCurrency();

        WalletTypeSummary walletType = new WalletTypeSummary();
        walletType.setId(type.getId());
        walletType.setCode(type.getCode());
        walletType.setName(type.getName());
        walletType.setCanExtendCredit(type.isCanExtendCredit());

        CurrencySummary currencySummary = new CurrencySummary();
        currencySummary.setId(currency.getId());
        currencySummary.setCode(currency.getCode());
        currencySummary.setName(currency.getName());
        currencySummary.setMinorUnits(currency.getMinorUnits());

        WalletLimits limits = new WalletLimits();
        limits.setGeneralTransactionLimit(wallet.getGeneralTransactionLimit());
        limits.setGeneralTransactionPeriod(wallet.getGeneralTransactionPeriod());
        limits.setDisbursementQuotaLimit(wallet.getDisbursementQuotaLimit());
        limits.setDisbursementQuotaUsed(wallet.getDisbursementQuotaUsed());
        limits.setDisbursementQuotaPeriod(wallet.getDisbursementQuotaPeriod());

        WalletActivation activation = new WalletActivation();
        activation.setStatus(wallet.getStatus());
        activation.setActive(wallet.isActive());
        activation.setVerifiedAt(wallet.getVerifiedAt());

        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setUserId(wallet.getUserId());
        response.setBusinessPublicUuid(wallet.getBusinessPublicUuid());
        response.setWalletType(walletType);
        response.setCurrency(currencySummary);
        response.setBalance(wallet.getBalance());
        response.setCreditLimit(wallet.getCreditLimit());
        response.setPinDashboard(wallet.isPinDashboard());
        response.setLimits(limits);
        response.setActivation(activation);
        response.setCreatedAt(wallet.getCreatedAt());
        response.setUpdatedAt(wallet.getUpdatedAt());

        return response;
    }

    // public WalletTypeHistoryResponse toResponse(WalletTypeHistory history) {
    //     WalletTypeHistoryResponse response = new WalletTypeHistoryResponse();

    //     response.setId(history.getId());
    //     response.setWalletId(history.getWallet().getId());
    //     response.setWalletTypeCode(history.getWalletType().getCode());
    //     response.setWalletTypeName(history.getWalletType().getName());
    //     response.setEffectiveFrom(history.getEffectiveFrom());
    //     response.setEffectiveTo(history.getEffectiveTo());
    //     response.setSupersededById(history.getSupersededById());
    //     response.setCurrent(history.isCurrent());

    //     return response;
    // }
}
