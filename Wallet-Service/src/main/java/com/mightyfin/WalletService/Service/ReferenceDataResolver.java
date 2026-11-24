package com.mightyfin.WalletService.Service;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.mightyfin.WalletService.Exception.BusinessRuleException;
import com.mightyfin.WalletService.Repo.CurrencyRepo;
import com.mightyfin.WalletService.Repo.WalletTypeRepo;
import com.mightyfin.WalletService.entity.Currency;
import com.mightyfin.WalletService.entity.WalletType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@Component 
public class ReferenceDataResolver {

    private final CurrencyRepo currencyRepository;
    private final WalletTypeRepo walletTypeRepository;

    public Currency requireActiveCurrency(String code) {
    String normalized = normalize(code);

    Currency currency = currencyRepository.findByCodeIgnoreCase(normalized)
            .orElseThrow(() -> new BusinessRuleException(
                    "INVALID_CURRENCY", "Currency '" + normalized + "' is not supported"));

    if (!currency.isActive()) {
        throw new BusinessRuleException(
                "INVALID_CURRENCY", "Currency '" + normalized + "' is not supported");
    }

    return currency;
}

public WalletType requireActiveWalletType(String code) {
    String normalized = normalize(code);

    WalletType walletType = walletTypeRepository.findByCodeIgnoreCase(normalized)
            .orElseThrow(() -> new BusinessRuleException(
                    "INVALID_WALLET_TYPE", "Wallet type '" + normalized + "' is not available"));

    if (!walletType.isActive()) {
        throw new BusinessRuleException(
                "INVALID_WALLET_TYPE", "Wallet type '" + normalized + "' is not available");
    }

    return walletType;
}

private static String normalize(String code) {
    return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
}
}
