package com.mightyfin.WalletService.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.mightyfin.WalletService.entity.Enum.CreditReservationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data 
@AllArgsConstructor 
@Builder 
@Entity 
public class WalletCreditReservation {


    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agreement_id", nullable = false, length = 64, updatable = false)
    private String agreementId;

    /** Whoever the loan is registered against: a customer wallet (B2C) or a business wallet (B2B). */
    @Column(name = "borrower_wallet_id", nullable = false, updatable = false)
    private Integer borrowerWalletId;

    /** The wallet that supplies the goods on credit (Limit 1). Null when no goods are financed. */
    @Column(name = "supplier_wallet_id", updatable = false)
    private Integer supplierWalletId;

    /** The partner that originates an embedded request (Limit 2). Null for a direct request. */
    @Column(name = "origination_wallet_id", updatable = false)
    private Integer originationWalletId;

    @Column(nullable = false, precision = 18, scale = 2, updatable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CreditReservationStatus status;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    /** Principal repaid so far that already went back into the quotas. Never above the amount. */
    @Builder.Default
    @Column(name = "restored_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal restoredAmount = BigDecimal.ZERO;

    @Column(name = "decision_note", length = 200)
    private String decisionNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    public boolean isOpen() {
        return status == CreditReservationStatus.RESERVED;
    }

    public void markConsumed(Instant now) {
        this.status = CreditReservationStatus.CONSUMED;
        this.decidedAt = now;
    }

    /** What a repayment may still put back: the amount that was taken, minus what already went back. */
    public BigDecimal getRestorable() {
        return amount.subtract(restoredAmount);
    }

    public void addRestored(BigDecimal restored) {
        if (status != CreditReservationStatus.CONSUMED || restored.signum() <= 0 || restored.compareTo(getRestorable()) > 0) {
            throw new IllegalStateException("invalid quota restoration");
        }
        this.restoredAmount = restoredAmount.add(restored);
    }

    public void markReleased(Instant now, String note) {
        this.status = CreditReservationStatus.RELEASED;
        this.decisionNote = note;
        this.decidedAt = now;
    }

    public void markExpired(Instant now) {
        this.status = CreditReservationStatus.EXPIRED;
        this.decisionNote = "AUTO: not decided in time";
        this.decidedAt = now;
    }
}
