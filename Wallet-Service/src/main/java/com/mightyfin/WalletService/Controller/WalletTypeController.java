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

import com.mightyfin.WalletService.Dto.request.CreateWalletTypeRequest;
import com.mightyfin.WalletService.Dto.response.ApiResponse;
import com.mightyfin.WalletService.Dto.response.WalletTypeResponse;
import com.mightyfin.WalletService.Security.WalletRole;
import com.mightyfin.WalletService.Service.WalletTypeService;
import com.mightyfinance.auth.starter.security.AuthPrincipal;
import com.mightyfinance.auth.starter.security.CurrentUser;
import com.mightyfinance.auth.starter.security.RequiresPermission;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@RestController 
@RequestMapping ("/api/v1/wallet-types")
public class WalletTypeController {
    
    private final WalletTypeService walletTypeService;

    /** Anyone sees the active types; {@code includeInactive=true} is back-office only (super_admin). */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WalletTypeResponse>>> list(
            @RequestParam(defaultValue = "false") boolean includeInactive, @CurrentUser AuthPrincipal caller) {
        List<WalletTypeResponse> types = includeInactive
                ? walletTypeService.listAll(caller)
                : walletTypeService.listActive();
        return ResponseEntity.ok(ApiResponse.success("Wallet types retrieved", types));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<WalletTypeResponse>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success("Wallet type retrieved", walletTypeService.getByCode(code)));
    }

    @RequiresPermission(roles = WalletRole.SUPER_ADMIN)
    @PostMapping
    public ResponseEntity<ApiResponse<WalletTypeResponse>> create(@Valid @RequestBody CreateWalletTypeRequest request,
                                                                  @CurrentUser AuthPrincipal caller) {
        WalletTypeResponse created = walletTypeService.create(request, caller);
        return ResponseEntity.created(URI.create("/api/v1/wallet-types/" + created.getCode()))
                .body(ApiResponse.success("Wallet type created", created));
    }

    // @RequiresPermission(roles = WalletRole.SUPER_ADMIN)
    // @PutMapping("/{code}")
    // public ResponseEntity<ApiResponse<WalletTypeResponse>> update(@PathVariable String code,
    //                                                               @Valid @RequestBody UpdateWalletTypeRequest request,
    //                                                               @CurrentUser AuthPrincipal caller) {
    //     return ResponseEntity.ok(ApiResponse.success("Wallet type updated",
    //             walletTypeService.update(code, request, caller)));
    // }

    @RequiresPermission(roles = WalletRole.SUPER_ADMIN)
    @PostMapping("/{code}/activate")
    public ResponseEntity<ApiResponse<WalletTypeResponse>> activate(@PathVariable String code, @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Wallet type activated", walletTypeService.activate(code, caller)));
    }

    @RequiresPermission(roles = WalletRole.SUPER_ADMIN)
    @PostMapping("/{code}/deactivate")
    public ResponseEntity<ApiResponse<WalletTypeResponse>> deactivate(@PathVariable String code, @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Wallet type deactivated",
                walletTypeService.deactivate(code, caller)));
    }
}
