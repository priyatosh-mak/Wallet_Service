package com.mightyfin.WalletService.Dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import com.mightyfin.WalletService.entity.WalletLimitIncreaseRequest;
import com.mightyfin.WalletService.entity.Enum.LimitIncreaseStatus;
import com.mightyfin.WalletService.entity.Enum.WalletLimitPeriod;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class LimitIncreaseResponse {


    private Integer id;
    private Integer walletId;
    private LimitIncreaseStatus status;
    private WalletLimitPeriod period;
    private BigDecimal currentLimit;
    private BigDecimal requestedLimit;
    private BigDecimal approvedLimit;
    private String reason;
    private String decisionNote;
    private Integer requestedByUserId;
    private Instant decidedAt;
    private Instant expiresAt;
    private Instant createdAt;

    public LimitIncreaseResponse() {
    }
    public static LimitIncreaseResponse from(WalletLimitIncreaseRequest r) {
    LimitIncreaseResponse response = new LimitIncreaseResponse();

    response.setId(r.getId());
    response.setWalletId(r.getWallet().getId());
    response.setStatus(r.getStatus());
    response.setPeriod(r.getPeriod());
    response.setCurrentLimit(r.getCurrentLimit());
    response.setRequestedLimit(r.getRequestedLimit());
    response.setApprovedLimit(r.getApprovedLimit());
    response.setReason(r.getReason());
    response.setDecisionNote(r.getDecisionNote());
    response.setRequestedByUserId(r.getRequestedByUserId());
    response.setDecidedAt(r.getDecidedAt());
    response.setExpiresAt(r.getExpiresAt());
    response.setCreatedAt(r.getCreatedAt());

    return response;
}
}
