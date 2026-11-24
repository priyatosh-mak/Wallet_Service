package com.mightyfin.WalletService.Service;

import java.util.List;

import com.mightyfin.WalletService.Dto.request.ActivateWalletRequest;
import com.mightyfin.WalletService.Dto.request.ChangeWalletTypeRequest;
import com.mightyfin.WalletService.Dto.request.UpdateWalletLimitRequest;
import com.mightyfin.WalletService.Dto.response.WalletResponse;
import com.mightyfin.WalletService.Dto.response.WalletTypeHistoryResponse;
import com.mightyfinance.auth.starter.security.AuthPrincipal;

public interface WalletLifeCycleService {


    /** Sets verified_at on first activation and is_active = true. */
    WalletResponse activate(Integer walletId, ActivateWalletRequest request, AuthPrincipal caller);

    /** Clears is_active; verified_at stays. */
    WalletResponse suspend(Integer walletId, AuthPrincipal caller);

    /** Closes the current history row, inserts the new one and updates the wallet, all in one transaction. */
    WalletResponse changeType(Integer walletId, ChangeWalletTypeRequest request, AuthPrincipal caller);

    /** Sets the wallet's general transaction limit and period. Required before a wallet can be activated. */
    WalletResponse updateGeneralTransactionLimit(Integer walletId, UpdateWalletLimitRequest request, AuthPrincipal caller);

    /** Newest first. */
    List<WalletTypeHistoryResponse> getTypeHistory(Integer walletId, AuthPrincipal caller);
}
