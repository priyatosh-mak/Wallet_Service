package com.mightyfin.WalletService.Controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mightyfin.WalletService.Dto.request.CreateCurrencyRequest;
import com.mightyfin.WalletService.Dto.request.UpdateCurrencyRequest;
import com.mightyfin.WalletService.Dto.response.ApiResponse;
import com.mightyfin.WalletService.Dto.response.CurrencyResponse;
import com.mightyfin.WalletService.Security.WalletRole;
import com.mightyfin.WalletService.Service.CurrencyService;
import com.mightyfinance.auth.starter.security.AuthPrincipal;
import com.mightyfinance.auth.starter.security.CurrentUser;
import com.mightyfinance.auth.starter.security.RequiresPermission;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@RestController 
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor 
public class CurrencyController {

        private final CurrencyService currencyService;

        @GetMapping
    public ResponseEntity<ApiResponse<List<CurrencyResponse>>> list(
            @RequestParam(defaultValue = "false") boolean includeInactive, @CurrentUser AuthPrincipal caller) {
        List<CurrencyResponse> currencies = includeInactive
                ? currencyService.listAll(caller)
                : currencyService.listActive();
        return ResponseEntity.ok(ApiResponse.success("Supported currencies retrieved", currencies));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<CurrencyResponse>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success("Currency retrieved", currencyService.getByCode(code)));
    }

    @RequiresPermission(roles = WalletRole.SUPER_ADMIN)
    @PostMapping
    public ResponseEntity<ApiResponse<CurrencyResponse>> create(@Valid @RequestBody CreateCurrencyRequest request,
                                                                @CurrentUser AuthPrincipal caller) {
        CurrencyResponse created = currencyService.create(request, caller);
        return ResponseEntity.created(URI.create("/api/v1/currencies/" + created.getCode()))
                .body(ApiResponse.success("Currency created", created));
    }

    @RequiresPermission(roles = WalletRole.SUPER_ADMIN)
    @PutMapping("/{code}")
    public ResponseEntity<ApiResponse<CurrencyResponse>> update(@PathVariable String code,
                                                                @Valid @RequestBody UpdateCurrencyRequest request,
                                                                @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Currency updated", currencyService.update(code, request, caller)));
    }

    @RequiresPermission(roles = WalletRole.SUPER_ADMIN)
    @PostMapping("/{code}/activate")
    public ResponseEntity<ApiResponse<CurrencyResponse>> activate(@PathVariable String code, @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Currency activated", currencyService.activate(code, caller)));
    }

    @RequiresPermission(roles = WalletRole.SUPER_ADMIN)
    @PostMapping("/{code}/deactivate")
    public ResponseEntity<ApiResponse<CurrencyResponse>> deactivate(@PathVariable String code, @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Currency deactivated", currencyService.deactivate(code, caller)));
    }
}
