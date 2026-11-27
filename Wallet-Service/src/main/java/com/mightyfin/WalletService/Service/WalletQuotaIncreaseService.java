package com.mightyfin.WalletService.Service;

import com.mightyfin.WalletService.Dto.request.ApproveQuotaIncreaseRequest;
import com.mightyfin.WalletService.Dto.request.RejectLimitIncreaseRequest;
import com.mightyfin.WalletService.Dto.request.SubmitQuuotaIncreaseRequest;
import com.mightyfin.WalletService.Dto.response.PageResponse;
import com.mightyfin.WalletService.Dto.response.QuotaIncreaseResponse;
import com.mightyfin.WalletService.entity.Enum.LimitIncreaseStatus;
import com.mightyfin.WalletService.entity.Enum.QuotaKind;
import com.mightyfinance.auth.starter.security.AuthPrincipal;

public interface WalletQuotaIncreaseService {

    QuotaIncreaseResponse submit(Integer walletId, QuotaKind kind, SubmitQuuotaIncreaseRequest request, AuthPrincipal caller);

    QuotaIncreaseResponse cancel(Integer requestId, AuthPrincipal caller);

    /** super_admin only. Never the requester. Adds the approved amount and records the decision in one transaction. */
    QuotaIncreaseResponse approve(Integer requestId, ApproveQuotaIncreaseRequest request, AuthPrincipal caller);

    QuotaIncreaseResponse reject(Integer requestId, RejectLimitIncreaseRequest request, AuthPrincipal caller);

    QuotaIncreaseResponse get(Integer requestId, AuthPrincipal caller);

    PageResponse<QuotaIncreaseResponse> listForWallet(Integer walletId, int page, int size, AuthPrincipal caller);

    PageResponse<QuotaIncreaseResponse> listAll(LimitIncreaseStatus status, int page, int size, AuthPrincipal caller);

    /** For the expiry sweep, no caller. @return whether it was expired */
    boolean expire(Integer requestId);
}
