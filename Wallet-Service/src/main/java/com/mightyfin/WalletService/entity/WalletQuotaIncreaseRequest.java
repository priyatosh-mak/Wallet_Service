package com.mightyfin.WalletService.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.mightyfin.WalletService.entity.Enum.LimitIncreaseStatus;
import com.mightyfin.WalletService.entity.Enum.QuotaKind;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class WalletQuotaIncreaseRequest {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false, updatable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "quota_kind", nullable = false, updatable = false, length = 16)
    private QuotaKind quotaKind;

    @Column(name = "requested_by_user_id", nullable = false, updatable = false)
    private Integer requestedByUserId;

    @Column(name = "requested_by_role", nullable = false, updatable = false, length = 32)
    private String requestedByRole;

    /** What was left when the request was made. Information for the administrator, never used to compute anything. */
    @Column(name = "remaining_when_asked", nullable = false, updatable = false, precision = 18, scale = 2)
    private BigDecimal remainingWhenAsked;

    @Column(name = "requested_increase", nullable = false, updatable = false, precision = 18, scale = 2)
    private BigDecimal requestedIncrease;

    @Column(nullable = false, updatable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LimitIncreaseStatus status;

    @Column(name = "approved_increase", precision = 18, scale = 2)
    private BigDecimal approvedIncrease;

    @Column(name = "decided_by_user_id")
    private Integer decidedByUserId;

    @Column(name = "decided_at")
    private Instant decidedAt;

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

    public static WalletQuotaIncreaseRequest submit(
        Wallet wallet,
        QuotaKind kind,
        Integer requestedByUserId,
        String requestedByRole,
        BigDecimal remainingWhenAsked,
        BigDecimal requestedIncrease,
        String reason,
        Instant expiresAt) {

    if (requestedIncrease.signum() <= 0) {
        throw new IllegalArgumentException("a quota increase must ask for a positive amount");
    }

    WalletQuotaIncreaseRequest request = new WalletQuotaIncreaseRequest();

    request.setWallet(wallet);
    request.setQuotaKind(kind);
    request.setRequestedByUserId(requestedByUserId);
    request.setRequestedByRole(requestedByRole);
    request.setRemainingWhenAsked(remainingWhenAsked);
    request.setRequestedIncrease(requestedIncrease);
    request.setReason(reason);
    request.setStatus(LimitIncreaseStatus.PENDING);
    request.setExpiresAt(expiresAt);

    return request;
}

public boolean isPending() {
    return status == LimitIncreaseStatus.PENDING;
}

public boolean isExpired(Instant now) {
    return isPending() && !expiresAt.isAfter(now);
}

/** Approved may be less than requested, never more, and must be positive. */
public void approve(BigDecimal approved, Integer decidedBy, String note, Instant now) {
    if (approved.signum() <= 0 || approved.compareTo(requestedIncrease) > 0) {
        throw new IllegalArgumentException(
                "approved increase must be positive and at most the requested one");
    }

    decide(LimitIncreaseStatus.APPROVED, decidedBy, note, now);
    this.approvedIncrease = approved;
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

public void expire(Instant now) {
    decide(LimitIncreaseStatus.EXPIRED, null, null, now);
}

private void decide(LimitIncreaseStatus newStatus, Integer decidedBy, String note, Instant now) {
    if (!isPending()) {
        throw new IllegalStateException("quota request is already " + status);
    }

    this.status = newStatus;
    this.decidedByUserId = decidedBy;
    this.decisionNote = note;
    this.decidedAt = now;
}
}
