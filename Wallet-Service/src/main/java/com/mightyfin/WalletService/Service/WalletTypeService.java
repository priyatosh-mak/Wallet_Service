package com.mightyfin.WalletService.Service;

import java.util.List;

import com.mightyfin.WalletService.Dto.request.CreateWalletTypeRequest;
import com.mightyfin.WalletService.Dto.response.WalletTypeResponse;
import com.mightyfinance.auth.starter.security.AuthPrincipal;

public interface WalletTypeService {

     List<WalletTypeResponse> listActive();

    /** Back office: every wallet type including deactivated ones. Requires super_admin. */
    List<WalletTypeResponse> listAll(AuthPrincipal caller);

    WalletTypeResponse getByCode(String code);

    /** Requires super_admin. Rejects a code that already exists. */
    WalletTypeResponse create(CreateWalletTypeRequest request, AuthPrincipal caller);

    /** Requires super_admin. Replaces the editable attributes; the code is immutable. */
    // WalletTypeResponse update(String code, UpdateWalletTypeRequest request, AuthPrincipal caller);

    /** Requires super_admin. Idempotent. */
    WalletTypeResponse activate(String code, AuthPrincipal caller);

    /** Requires super_admin. Idempotent. The seeded types (CUSTOMER, ORGANIZATION, PARTNER, SYSTEM) cannot be deactivated. */
    WalletTypeResponse deactivate(String code, AuthPrincipal caller);
}
