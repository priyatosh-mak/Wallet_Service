package com.mightyfin.WalletService.mapper;

import com.mightyfin.WalletService.Dto.response.CurrencyResponse;
import com.mightyfin.WalletService.Dto.response.WalletTypeResponse;
import com.mightyfin.WalletService.entity.Currency;
import com.mightyfin.WalletService.entity.WalletType;

public class ReferenceDataMapper {

    public CurrencyResponse toResponse(Currency c) {
        return new CurrencyResponse(c.getId(), c.getCode(), c.getName(), c.getMinorUnits(), c.isActive());
    }

    public WalletTypeResponse toResponse(WalletType t) {
        return new WalletTypeResponse(
                t.getId(),
                t.getCode(),
                t.getName(),
                t.getDescription(),
                t.getDefaultCreditLimit(),
                t.isCanExtendCredit(),
                t.getDefaultDisbursementQuotaLimit(),
                t.getDefaultDisbursementQuotaPeriod(),
                t.getDefaultGeneralTransactionLimit(),
                t.getDefaultGeneralTransactionPeriod(),
                t.getValueChainCategory(),
                t.isActive());
    }
    
}
