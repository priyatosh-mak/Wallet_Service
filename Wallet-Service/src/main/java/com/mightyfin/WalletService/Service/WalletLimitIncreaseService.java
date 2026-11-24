package com.mightyfin.WalletService.Service;

import com.mightyfin.WalletService.Dto.request.ApproveLimitIncreaseRequest;
import com.mightyfin.WalletService.Dto.request.RejectLimitIncreaseRequest;
import com.mightyfin.WalletService.Dto.request.SubmitLimitIncreaseRequest;
import com.mightyfin.WalletService.Dto.response.LimitIncreaseResponse;
import com.mightyfinance.auth.starter.security.AuthPrincipal;

public interface WalletLimitIncreaseService {

    /** Owner or business admin of the wallet. One open request per wallet; retrying the same request is harmless. */
    LimitIncreaseResponse submit(Integer walletId, SubmitLimitIncreaseRequest request, AuthPrincipal caller);

    /** Withdraws an open request. Same people as {@link #submit}. */
    LimitIncreaseResponse cancel(Integer requestId, AuthPrincipal caller);

    /** super_admin only. Raises the wallet's limit and records the decision in one transaction. */
    LimitIncreaseResponse approve(Integer requestId, ApproveLimitIncreaseRequest request, AuthPrincipal caller);

    /** super_admin only. A rejection always carries a note for the requester. */
    LimitIncreaseResponse reject(Integer requestId, RejectLimitIncreaseRequest request, AuthPrincipal caller);

    /** Anyone who may see the wallet. */
    LimitIncreaseResponse get(Integer requestId, AuthPrincipal caller);

    /** Newest first. Anyone who may see the wallet. */
    // PageResponse<LimitIncreaseResponse> listForWallet(Integer walletId, int page, int size, AuthPrincipal caller);

    /** super_admin only. With {@code PENDING}: the work queue, oldest first. Without a status: everything, newest first. */
    // PageResponse<LimitIncreaseResponse> listAll(LimitIncreaseStatus status, int page, int size, AuthPrincipal caller);

    /**
     * For the expiry sweep, no caller. Expires the request if it is still open and past its time.
     *
     * @return whether it was expired
     */
    boolean expire(Integer requestId);
}
