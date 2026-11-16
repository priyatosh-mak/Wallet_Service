package com.mightyfin.WalletService.Service;

import java.util.UUID;

import com.mightyfin.WalletService.Dto.request.CreateBusinessWalletRequest;
import com.mightyfin.WalletService.Dto.request.CreateCustomerWalletRequest;
import com.mightyfin.WalletService.Dto.response.WalletResponse;
import com.mightyfinance.auth.starter.security.AuthPrincipal;

public interface WalletService {


    WalletResponse createCustomerWallet(CreateCustomerWalletRequest request, AuthPrincipal caller);

    /**
     * Creates an inactive wallet for a business, with its first type-history row.
     * Requires super_admin.
     */
    WalletResponse createBusinessWallet(CreateBusinessWalletRequest request, AuthPrincipal caller);

    /** A wallet the caller may not see is reported as not found, exactly like one that does not exist. */
    WalletResponse getById(Integer walletId, AuthPrincipal caller);

    /** The wallet of the authenticated customer. */
    WalletResponse getCurrentCustomerWallet(AuthPrincipal caller);

    WalletResponse getBusinessWallet(UUID businessPublicUuid, AuthPrincipal caller);
}
