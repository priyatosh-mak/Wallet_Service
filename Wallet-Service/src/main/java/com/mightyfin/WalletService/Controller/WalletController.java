package com.mightyfin.WalletService.Controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mightyfin.WalletService.Dto.request.ActivateWalletRequest;
import com.mightyfin.WalletService.Dto.request.ChangeWalletTypeRequest;
import com.mightyfin.WalletService.Dto.request.CreateBusinessWalletRequest;
import com.mightyfin.WalletService.Dto.request.CreateCustomerWalletRequest;
import com.mightyfin.WalletService.Dto.request.UpdateWalletLimitRequest;
import com.mightyfin.WalletService.Dto.response.ApiResponse;
import com.mightyfin.WalletService.Dto.response.WalletResponse;
import com.mightyfin.WalletService.Security.WalletPermissions;
import com.mightyfin.WalletService.Security.WalletRole;
import com.mightyfin.WalletService.Dto.response.WalletTypeHistoryResponse;
import com.mightyfin.WalletService.Service.WalletLifeCycleService;
import com.mightyfin.WalletService.Service.WalletService;
import com.mightyfinance.auth.starter.security.AuthPrincipal;
import com.mightyfinance.auth.starter.security.CurrentUser;
import com.mightyfinance.auth.starter.security.RequiresPermission;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@RestController 
@RequestMapping ("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;
    private final WalletLifeCycleService lifecycleService;

    @RequiresPermission(roles = WalletRole.SUPER_ADMIN)
    @PostMapping("/customer")
    public ResponseEntity<ApiResponse<WalletResponse>> createCustomerWallet(
            @Valid @RequestBody CreateCustomerWalletRequest request, @CurrentUser AuthPrincipal caller) {
        return created(walletService.createCustomerWallet(request, caller), "Customer wallet created");
    }

    @RequiresPermission(roles = WalletRole.SUPER_ADMIN)
    @PostMapping("/business")
    public ResponseEntity<ApiResponse<WalletResponse>> createBusinessWallet(
            @Valid @RequestBody CreateBusinessWalletRequest request, @CurrentUser AuthPrincipal caller) {
        return created(walletService.createBusinessWallet(request, caller), "Business wallet created");
    }

    // ---- retrieval ----

    @RequiresPermission(WalletPermissions.VIEW)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<WalletResponse>> getCurrentCustomerWallet(@CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Wallet retrieved", walletService.getCurrentCustomerWallet(caller)));
    }

    @RequiresPermission(WalletPermissions.VIEW)
    @GetMapping("/{walletId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getById(@PathVariable Integer walletId, @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Wallet retrieved", walletService.getById(walletId, caller)));
    }

    @RequiresPermission(WalletPermissions.VIEW)
    @GetMapping("/business/{businessPublicUuid}")
    public ResponseEntity<ApiResponse<WalletResponse>> getBusinessWallet(@PathVariable UUID businessPublicUuid,
                                                                         @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Wallet retrieved",
                walletService.getBusinessWallet(businessPublicUuid, caller)));
    }

    // ---- activation and type history ----

    @RequiresPermission(value = WalletPermissions.ACTIVATE, roles = WalletRole.SUPER_ADMIN)
    @PostMapping("/{walletId}/activate")
    public ResponseEntity<ApiResponse<WalletResponse>> activate(@PathVariable Integer walletId,
                                                                @Valid @RequestBody ActivateWalletRequest request,
                                                                @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Wallet activated",
                lifecycleService.activate(walletId, request, caller)));
    }

    @RequiresPermission(roles = WalletRole.SUPER_ADMIN)
    @PostMapping("/{walletId}/suspend")
    public ResponseEntity<ApiResponse<WalletResponse>> suspend(@PathVariable Integer walletId, @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Wallet suspended", lifecycleService.suspend(walletId, caller)));
    }

    @RequiresPermission(roles = WalletRole.SUPER_ADMIN)
    @PutMapping("/{walletId}/type")
    public ResponseEntity<ApiResponse<WalletResponse>> changeType(@PathVariable Integer walletId,
                                                                  @Valid @RequestBody ChangeWalletTypeRequest request,
                                                                  @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Wallet type changed",
                lifecycleService.changeType(walletId, request, caller)));
    }

    /** Privileged and non-delegable: super_admin only, on top of the permission code. */
    @RequiresPermission(value = WalletPermissions.LIMIT_SET, roles = WalletRole.SUPER_ADMIN)
    @PutMapping("/{walletId}/limits")
    public ResponseEntity<ApiResponse<WalletResponse>> updateLimits(@PathVariable Integer walletId,
                                                                    @Valid @RequestBody UpdateWalletLimitRequest request,
                                                                    @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Wallet limits updated",
                lifecycleService.updateGeneralTransactionLimit(walletId, request, caller)));
    }

    @RequiresPermission(WalletPermissions.VIEW)
    @GetMapping("/{walletId}/type-history")
    public ResponseEntity<ApiResponse<List<WalletTypeHistoryResponse>>> typeHistory(@PathVariable Integer walletId,
                                                                                    @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Wallet type history retrieved",
                lifecycleService.getTypeHistory(walletId, caller)));
    }

    private ResponseEntity<ApiResponse<WalletResponse>> created(WalletResponse wallet, String message) {
        return ResponseEntity.created(URI.create("/api/v1/wallets/" + wallet.getId()))
                .body(ApiResponse.success(message, wallet));
    }
}
