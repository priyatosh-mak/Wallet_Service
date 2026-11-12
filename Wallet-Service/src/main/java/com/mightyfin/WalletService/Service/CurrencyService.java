package com.mightyfin.WalletService.Service;

import java.util.List;

import com.mightyfin.WalletService.Dto.request.CreateCurrencyRequest;
import com.mightyfin.WalletService.Dto.request.UpdateCurrencyRequest;
import com.mightyfin.WalletService.Dto.response.CurrencyResponse;
import com.mightyfinance.auth.starter.security.AuthPrincipal;

public interface CurrencyService {


   List<CurrencyResponse> listActive();

    /** Back office: every currency including deactivated ones. Requires super_admin. */
    List<CurrencyResponse> listAll(AuthPrincipal caller);

    CurrencyResponse getByCode(String code);

    /** Requires super_admin. Rejects a code that already exists. */
    CurrencyResponse create(CreateCurrencyRequest request, AuthPrincipal caller);

    /** Requires super_admin. Only the name can change. */
    CurrencyResponse update(String code, UpdateCurrencyRequest request, AuthPrincipal caller);

    /** Requires super_admin. Idempotent. */
    CurrencyResponse activate(String code, AuthPrincipal caller);

    /** Requires super_admin. Idempotent. Existing wallets keep working; new wallets can no longer use it. */
    CurrencyResponse deactivate(String code, AuthPrincipal caller);


}
