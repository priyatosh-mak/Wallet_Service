package com.mightyfin.WalletService.mapper;

import org.springframework.stereotype.Component;

import com.mightyfin.WalletService.Dto.response.CurrencyResponse;
import com.mightyfin.WalletService.Dto.response.WalletTypeResponse;
import com.mightyfin.WalletService.entity.Currency;
import com.mightyfin.WalletService.entity.WalletType;

@Component 
public class ReferenceDataMapper {

    public CurrencyResponse toResponse(Currency c) {
        CurrencyResponse response = new CurrencyResponse();

        response.setId(c.getId());
        response.setCode(c.getCode());
        response.setName(c.getName());
        response.setMinorUnits(c.getMinorUnits());
        response.setActive(c.isActive());

        return response;
    }

    public WalletTypeResponse toResponse(WalletType t) {
        WalletTypeResponse response = new WalletTypeResponse();

        response.setId(t.getId());
        response.setCode(t.getCode());
        response.setName(t.getName());
        response.setDescription(t.getDescription());
        response.setDefaultCreditLimit(t.getDefaultCreditLimit());
        response.setMayEnableSupply(t.isMayEnableSupply());
        response.setMayEnableOrigination(t.isMayEnableOrigination());
        response.setDefaultSupplyQuotaLimit(t.getDefaultSupplyQuotaLimit());
        response.setDefaultOriginationQuotaLimit(t.getDefaultOriginationQuotaLimit());
        response.setDefaultGeneralTransactionLimit(t.getDefaultGeneralTransactionLimit());
        response.setDefaultGeneralTransactionPeriod(t.getDefaultGeneralTransactionPeriod());
        response.setValueChainCategory(t.getValueChainCategory());
        response.setActive(t.isActive());

        return response;
    }
    
}
