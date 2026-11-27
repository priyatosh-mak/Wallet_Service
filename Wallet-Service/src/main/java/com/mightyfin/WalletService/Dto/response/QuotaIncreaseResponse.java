package com.mightyfin.WalletService.Dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.mightyfin.WalletService.entity.WalletQuotaIncreaseRequest;
import com.mightyfin.WalletService.entity.Enum.LimitIncreaseStatus;
import com.mightyfin.WalletService.entity.Enum.QuotaKind;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class QuotaIncreaseResponse {


    private Integer id;
    private Integer walletId;
    private QuotaKind quota;
    private LimitIncreaseStatus status;
    private BigDecimal remainingWhenAsked;
    private BigDecimal requestedIncrease;
    private BigDecimal approvedIncrease;
    private String reason;
    private String decisionNote;
    private Integer requestedByUserId;
    private Instant decidedAt;
    private Instant expiresAt;
    private Instant createdAt;

    public static QuotaIncreaseResponse from(WalletQuotaIncreaseRequest r) {

    QuotaIncreaseResponse response = new QuotaIncreaseResponse();

    response.setId(r.getId());
    response.setWalletId(r.getWallet().getId());
    response.setQuota(r.getQuotaKind());
    response.setStatus(r.getStatus());
    response.setRemainingWhenAsked(r.getRemainingWhenAsked());
    response.setRequestedIncrease(r.getRequestedIncrease());
    response.setApprovedIncrease(r.getApprovedIncrease());
    response.setReason(r.getReason());
    response.setDecisionNote(r.getDecisionNote());
    response.setRequestedByUserId(r.getRequestedByUserId());
    response.setDecidedAt(r.getDecidedAt());
    response.setExpiresAt(r.getExpiresAt());
    response.setCreatedAt(r.getCreatedAt());

    return response;
}
}
