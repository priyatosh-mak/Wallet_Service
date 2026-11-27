package com.mightyfin.WalletService.Controller;

import java.net.URI;
import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mightyfin.WalletService.Dto.request.ApproveQuotaIncreaseRequest;
import com.mightyfin.WalletService.Dto.request.RejectLimitIncreaseRequest;
import com.mightyfin.WalletService.Dto.request.SubmitQuuotaIncreaseRequest;
import com.mightyfin.WalletService.Dto.response.ApiResponse;
import com.mightyfin.WalletService.Dto.response.PageResponse;
import com.mightyfin.WalletService.Dto.response.QuotaIncreaseResponse;
import com.mightyfin.WalletService.Security.WalletPermissions;
import com.mightyfin.WalletService.Security.WalletRole;
import com.mightyfin.WalletService.Service.WalletQuotaIncreaseService;
import com.mightyfin.WalletService.entity.Enum.LimitIncreaseStatus;
import com.mightyfin.WalletService.entity.Enum.QuotaKind;
import com.mightyfinance.auth.starter.security.AuthPrincipal;
import com.mightyfinance.auth.starter.security.CurrentUser;
import com.mightyfinance.auth.starter.security.RequiresPermission;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@RestController 
@RequestMapping("/api/v1")

public class QuotaIncreaseController {

        private final WalletQuotaIncreaseService service;

    @RequiresPermission(WalletPermissions.VIEW)
    @PostMapping("/wallets/{walletId}/quotas/{kind}/increase-requests")
    public ResponseEntity<ApiResponse<QuotaIncreaseResponse>> submit(@PathVariable Integer walletId,
                                                                     @PathVariable String kind,
                                                                     @Valid @RequestBody SubmitQuuotaIncreaseRequest request,
                                                                     @CurrentUser AuthPrincipal caller) {
        QuotaIncreaseResponse created = service.submit(walletId, QuotaKind.valueOf(kind.toUpperCase(Locale.ROOT)), request, caller);
        return ResponseEntity.created(URI.create("/api/v1/quota-requests/" + created.getId()))
                .body(ApiResponse.success("Quota increase request submitted", created));
    }

    @RequiresPermission(WalletPermissions.VIEW)
    @PostMapping("/quota-requests/{requestId}/cancel")
    public ResponseEntity<ApiResponse<QuotaIncreaseResponse>> cancel(@PathVariable Integer requestId,
                                                                    @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Quota increase request cancelled", service.cancel(requestId, caller)));
    }

    @RequiresPermission(WalletPermissions.VIEW)
    @GetMapping("/wallets/{walletId}/quota-requests")
    public ResponseEntity<ApiResponse<PageResponse<QuotaIncreaseResponse>>> listForWallet(
            @PathVariable Integer walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Quota increase requests retrieved",
                service.listForWallet(walletId, page, size, caller)));
    }

    @RequiresPermission(WalletPermissions.VIEW)
    @GetMapping("/quota-requests/{requestId}")
    public ResponseEntity<ApiResponse<QuotaIncreaseResponse>> get(@PathVariable Integer requestId,
                                                                 @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Quota increase request retrieved", service.get(requestId, caller)));
    }

    // ---- administrator ----

    @RequiresPermission(roles = WalletRole.SUPER_ADMIN)
    @GetMapping("/quota-requests")
    public ResponseEntity<ApiResponse<PageResponse<QuotaIncreaseResponse>>> listAll(
            @RequestParam(required = false) LimitIncreaseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Quota increase requests retrieved",
                service.listAll(status, page, size, caller)));
    }

    @RequiresPermission(value = WalletPermissions.LIMIT_SET, roles = WalletRole.SUPER_ADMIN)
    @PostMapping("/quota-requests/{requestId}/approve")
    public ResponseEntity<ApiResponse<QuotaIncreaseResponse>> approve(
            @PathVariable Integer requestId,
            @Valid @RequestBody(required = false) ApproveQuotaIncreaseRequest request,
            @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Quota increase request approved",
                service.approve(requestId, request, caller)));
    }

    @RequiresPermission(value = WalletPermissions.LIMIT_SET, roles = WalletRole.SUPER_ADMIN)
    @PostMapping("/quota-requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<QuotaIncreaseResponse>> reject(@PathVariable Integer requestId,
                                                                    @Valid @RequestBody RejectLimitIncreaseRequest request,
                                                                    @CurrentUser AuthPrincipal caller) {
        return ResponseEntity.ok(ApiResponse.success("Quota increase request rejected",
                service.reject(requestId, request, caller)));
    }
}
