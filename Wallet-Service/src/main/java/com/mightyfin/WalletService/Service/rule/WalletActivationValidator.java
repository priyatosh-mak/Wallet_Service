package com.mightyfin.WalletService.Service.rule;

import org.springframework.stereotype.Component;

import com.mightyfin.WalletService.Exception.BusinessRuleException;
import com.mightyfin.WalletService.Repo.WalletTypeHistoryRepo;
import com.mightyfin.WalletService.entity.Wallet;
import com.mightyfin.WalletService.entity.WalletTypeHistory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@Component 
public class WalletActivationValidator {


    private final WalletTypeHistoryRepo historyRepository;

    public void validate(Wallet wallet) {
        if (wallet.isActive()) {
            throw new BusinessRuleException("WALLET_ALREADY_ACTIVE", "Wallet " + wallet.getId() + " is already active");
        }
        if (!wallet.getWalletType().isActive()) {
            throw new BusinessRuleException("WALLET_TYPE_INACTIVE",
                    "Wallet type '" + wallet.getWalletType().getCode() + "' is no longer active");
        }
        if (!wallet.getCurrency().isActive()) {
            throw new BusinessRuleException("WALLET_CURRENCY_INACTIVE",
                    "Currency '" + wallet.getCurrency().getCode() + "' is no longer active");
        }

        // Fail closed on the wallet's OWN limit. The type default is only a template copied at creation, so a wallet
        // created while the default was empty must get a limit (PUT /wallets/{id}/limits) before it can go live.
        if (wallet.getGeneralTransactionLimit() == null) {
            throw new BusinessRuleException("LIMITS_NULL_BLOCK_ACTIVATION",
                    "Wallet activation blocked: set the wallet's general transaction limit first (fail-closed policy).");
        }

        WalletTypeHistory current = historyRepository.findByWalletIdAndEffectiveToIsNull(wallet.getId())
                .orElseThrow(() -> new IllegalStateException("Wallet " + wallet.getId() + " has no current type history row"));
        if (!current.getWalletType().getId().equals(wallet.getWalletType().getId())) {
            throw new IllegalStateException("Wallet " + wallet.getId() + " type does not match its current history row");
        }
    }
}
