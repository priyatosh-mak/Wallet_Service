package com.mightyfin.WalletService.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.mightyfin.WalletService.entity.Enum.LimitIncreaseStatus;
import com.mightyfin.WalletService.entity.Enum.WalletLimitPeriod;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Data 
@AllArgsConstructor 
@Table (name = "wallet_limit_increase_requests")
@NoArgsConstructor 
public class WalletLimitIncreaseRequest {


    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false, updatable = false)
    private Wallet wallet;

    @Column(name = "requested_by_user_id", nullable = false, updatable = false)
    private Integer requestedByUserId;

    @Column(name = "requested_by_role", nullable = false, updatable = false, length = 32)
    private String requestedByRole;

    /** The wallet's period when the request was made. A request never changes the period. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 16)
    private WalletLimitPeriod period;

    /** The wallet's limit when the request was made. */
    @Column(name = "current_limit", nullable = false, updatable = false, precision = 18, scale = 2)
    private BigDecimal currentLimit;

    @Column(name = "requested_limit", nullable = false, updatable = false, precision = 18, scale = 2)
    private BigDecimal requestedLimit;

    @Column(nullable = false, updatable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LimitIncreaseStatus status;

    @Column(name = "approved_limit", precision = 18, scale = 2)
    private BigDecimal approvedLimit;

    @Column(name = "decided_by_user_id")
    private Integer decidedByUserId;

    @Column(name = "decided_at")
    private Instant decidedAt;

    /** Written for the requester to read on a rejection. Not a place for internal remarks. */
    @Column(name = "decision_note", length = 500)
    private String decisionNote;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;



    public static WalletLimitIncreaseRequest submit(Wallet wallet, Integer requestedByUserId, String requestedByRole,
                                                WalletLimitPeriod period, BigDecimal currentLimit,
                                                BigDecimal requestedLimit, String reason, Instant expiresAt) {
    if (requestedLimit.compareTo(currentLimit) <= 0) {
        throw new IllegalArgumentException("a limit increase must ask for more than the current limit");
    }

    WalletLimitIncreaseRequest request = new WalletLimitIncreaseRequest();
    request.setWallet(wallet);
    request.setRequestedByUserId(requestedByUserId);
    request.setRequestedByRole(requestedByRole);
    request.setPeriod(period);
    request.setCurrentLimit(currentLimit);
    request.setRequestedLimit(requestedLimit);
    request.setReason(reason);
    request.setStatus(LimitIncreaseStatus.PENDING);
    request.setExpiresAt(expiresAt);

    return request;
}



    public boolean isPending() {
        return status == LimitIncreaseStatus.PENDING;
    }

    /** Only a PENDING request can be past its expiry; a decided one is final whatever the date. */
    public boolean isExpired(Instant now) {
        return isPending() && !expiresAt.isAfter(now);
    }

    /** {@code approved} may be less than requested, but must still be above the limit that applied when asked. */
    public void approve(BigDecimal approved, Integer decidedBy, String note, Instant now) {
        if (approved.compareTo(currentLimit) <= 0 || approved.compareTo(requestedLimit) > 0) {
            throw new IllegalArgumentException("approved limit must be above the current limit and at most the requested one");
        }
        decide(LimitIncreaseStatus.APPROVED, decidedBy, note, now);
        this.approvedLimit = approved;
    }

    public void reject(Integer decidedBy, String note, Instant now) {
        if (note == null || note.isBlank()) {
            throw new IllegalArgumentException("a rejection needs a note");
        }
        decide(LimitIncreaseStatus.REJECTED, decidedBy, note, now);
    }

    public void cancel(Integer decidedBy, Instant now) {
        decide(LimitIncreaseStatus.CANCELLED, decidedBy, null, now);
    }

    /** Decided by the clock, not a person. */
    public void expire(Instant now) {
        decide(LimitIncreaseStatus.EXPIRED, null, null, now);
    }

    public void supersede(Integer decidedBy, String note, Instant now) {
        decide(LimitIncreaseStatus.SUPERSEDED, decidedBy, note, now);
    }

    private void decide(LimitIncreaseStatus newStatus, Integer decidedBy, String note, Instant now) {
        if (!isPending()) {
            throw new IllegalStateException("limit request is already " + status);
        }
        this.status = newStatus;
        this.decidedByUserId = decidedBy;
        this.decisionNote = note;
        this.decidedAt = now;
    }
}
