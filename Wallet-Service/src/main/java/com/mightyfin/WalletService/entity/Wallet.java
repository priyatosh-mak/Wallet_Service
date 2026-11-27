package com.mightyfin.WalletService.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.mightyfin.WalletService.entity.Enum.DisbursementQuotaPeriod;
import com.mightyfin.WalletService.entity.Enum.QuotaKind;
import com.mightyfin.WalletService.entity.Enum.WalletLimitPeriod;
import com.mightyfin.WalletService.entity.Enum.WalletStatus;

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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data 
@AllArgsConstructor 
@NoArgsConstructor
@Table (name = "wallets")
@Entity 
public class Wallet {

        public static final BigDecimal MAX_BALANCE = new BigDecimal("9999999999999999.99");
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Logical FK to the auth service user. Exactly one of userId / businessPublicUuid is set. */
    @Column(name = "user_id", updatable = false)
    private Integer userId;

    @Column(name = "business_public_uuid", updatable = false)
    private UUID businessPublicUuid;

    /** Current type (cache). The full history lives in wallet_type_history. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_type_id", nullable = false)
    private WalletType walletType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "currency_id", nullable = false, updatable = false)
    private Currency currency;

    /** Spendable cache. No setter on purpose: only the posting engine (a later phase) may change it. */
    @Builder.Default
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /** Part of {@code balance} reserved by PENDING cash-outs. Spendable = balance - heldBalance. */
    @Builder.Default
    @Column(name = "held_balance", nullable = false, precision = 18, scale = 2)
    private BigDecimal heldBalance = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "credit_limit", nullable = false, precision = 18, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "pin_dashboard", nullable = false)
    private boolean pinDashboard = true;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = false;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "general_transaction_limit", precision = 18, scale = 2)
    private BigDecimal generalTransactionLimit;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "general_transaction_period", nullable = false, length = 16)
    private WalletLimitPeriod generalTransactionPeriod = WalletLimitPeriod.DAILY;

    /** Live Limit 1 gate. Default false. Fail-closed: also needs {@code walletType.mayEnableSupply}. */
    @Builder.Default
    @Column(name = "can_supply_on_credit", nullable = false)
    private boolean canSupplyOnCredit = false;

    /** Live Limit 2 gate. Default false. Fail-closed: also needs {@code walletType.mayEnableOrigination}. */
    @Builder.Default
    @Column(name = "can_originate_loans", nullable = false)
    private boolean canOriginateLoans = false;

    /** Limit 1 REMAINING (goods still supplyable on credit). Enabled and 0 means exhausted, not unlimited. */
    @Builder.Default
    @Column(name = "supply_quota_limit", nullable = false, precision = 18, scale = 2)
    private BigDecimal supplyQuotaLimit = BigDecimal.ZERO;

    /** Limit 2 REMAINING (loans still originable). Enabled and 0 means exhausted, not unlimited. */
    @Builder.Default
    @Column(name = "origination_quota_limit", nullable = false, precision = 18, scale = 2)
    private BigDecimal originationQuotaLimit = BigDecimal.ZERO;

    /** Pooled credit promised to requests that are not disbursed yet (customer side). */
    @Builder.Default
    @Column(name = "credit_reserved", nullable = false, precision = 18, scale = 2)
    private BigDecimal creditReserved = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public boolean isCustomerWallet() {
        return userId != null;
    }

    public WalletStatus getStatus() {
        if (active) {
            return WalletStatus.ACTIVE;
        }
        return verifiedAt == null ? WalletStatus.PENDING_ACTIVATION : WalletStatus.SUSPENDED;
    }

    /** First activation stamps verified_at; re-activation after a suspension keeps the original stamp. */
    public void activate(Instant now) {
        if (verifiedAt == null) {
            verifiedAt = now;
        }
        active = true;
    }

    /** verified_at is deliberately left untouched. */
    public void suspend() {
        active = false;
    }

    /** What the wallet may spend right now: the balance minus money reserved by pending cash-outs. */
    public BigDecimal getAvailableBalance() {
        return balance.subtract(heldBalance);
    }

    /** Posting-engine only: callers must hold the row lock. Refuses to spend money that is on hold. */
    public void debit(BigDecimal amount) {
        requirePositive(amount);
        if (getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("insufficient available balance");
        }
        balance = balance.subtract(amount);
    }

    /** Posting-engine only: callers must hold the row lock. */
    public void credit(BigDecimal amount) {
        requirePositive(amount);
        balance = balance.add(amount);
    }

    public void changeGeneralTransactionLimit(BigDecimal limit, WalletLimitPeriod period) {
        if (limit == null || limit.signum() < 0) {
            throw new IllegalArgumentException("limit must be zero or positive");
        }
        if (period == null) {
            throw new IllegalArgumentException("period is required");
        }
        this.generalTransactionLimit = limit;
        this.generalTransactionPeriod = period;
    }

    /** Reserves {@code amount} for a pending cash-out. The balance itself does not change. Row lock required. */
    public void hold(BigDecimal amount) {
        requirePositive(amount);
        if (getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("insufficient available balance");
        }
        heldBalance = heldBalance.add(amount);
    }

    /** Gives a hold back (cash-out failed). The balance does not change. Row lock required. */
    public void releaseHold(BigDecimal amount) {
        requirePositive(amount);
        if (heldBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("release exceeds held balance");
        }
        heldBalance = heldBalance.subtract(amount);
    }

    /** Cash-out completed: the held money leaves the wallet. Balance and hold drop together. Row lock required. */
    public void settleHold(BigDecimal amount) {
        requirePositive(amount);
        if (heldBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("settle exceeds held balance");
        }
        heldBalance = heldBalance.subtract(amount);
        balance = balance.subtract(amount);
    }

    private static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    public void changeType(WalletType newType) {
        this.walletType = newType;
    }

    // ---------------------------------------------------------------- supply / origination quotas

public boolean isQuotaEnabled(QuotaKind kind) {
    return kind == QuotaKind.SUPPLY
            ? canSupplyOnCredit
            : canOriginateLoans;
}

public boolean typeAllowsQuota(QuotaKind kind) {
    return kind == QuotaKind.SUPPLY
            ? walletType.isMayEnableSupply()
            : walletType.isMayEnableOrigination();
}

public boolean canUseQuota(QuotaKind kind) {
    return isQuotaEnabled(kind) && typeAllowsQuota(kind);
}

public BigDecimal getQuotaRemaining(QuotaKind kind) {
    return kind == QuotaKind.SUPPLY
            ? supplyQuotaLimit
            : originationQuotaLimit;
}

public void takeQuota(QuotaKind kind, BigDecimal amount) {
    requirePositive(amount);

    if (!canUseQuota(kind)) {
        throw new IllegalStateException(kind + " quota is not enabled for this wallet");
    }

    if (getQuotaRemaining(kind).compareTo(amount) < 0) {
        throw new IllegalStateException(kind + " quota exhausted");
    }

    setQuota(kind, getQuotaRemaining(kind).subtract(amount));
}

public void giveBackQuota(QuotaKind kind, BigDecimal amount) {
    requirePositive(amount);

    BigDecimal next = getQuotaRemaining(kind).add(amount);

    if (next.compareTo(MAX_BALANCE) > 0) {
        throw new IllegalStateException(kind + " quota would exceed the column capacity");
    }

    setQuota(kind, next);
}

public void increaseQuota(QuotaKind kind, BigDecimal amount) {
    requirePositive(amount);

    if (!canUseQuota(kind)) {
        throw new IllegalStateException(kind + " quota is not enabled for this wallet");
    }

    BigDecimal next = getQuotaRemaining(kind).add(amount);

    if (next.compareTo(MAX_BALANCE) > 0) {
        throw new IllegalStateException(kind + " quota would exceed the column capacity");
    }

    setQuota(kind, next);
}

public void enableQuota(QuotaKind kind, BigDecimal remaining) {
    requireNonNegative(remaining);

    if (!typeAllowsQuota(kind)) {
        throw new IllegalStateException("wallet type does not allow " + kind + " quota");
    }

    if (kind == QuotaKind.SUPPLY) {
        canSupplyOnCredit = true;
    } else {
        canOriginateLoans = true;
    }

    setQuota(kind, remaining);
}

public void disableQuota(QuotaKind kind) {
    if (kind == QuotaKind.SUPPLY) {
        canSupplyOnCredit = false;
    } else {
        canOriginateLoans = false;
    }
}

public void setQuotaRemaining(QuotaKind kind, BigDecimal remaining) {
    requireNonNegative(remaining);

    if (!isQuotaEnabled(kind)) {
        throw new IllegalStateException(kind + " quota is not enabled for this wallet");
    }

    setQuota(kind, remaining);
}

private void setQuota(QuotaKind kind, BigDecimal value) {
    if (kind == QuotaKind.SUPPLY) {
        supplyQuotaLimit = value;
    } else {
        originationQuotaLimit = value;
    }
}

// ---------------------------------------------------------------- pooled credit

public void reserveCredit(BigDecimal amount) {
    requirePositive(amount);
    creditReserved = creditReserved.add(amount);
}

public void releaseCreditReservation(BigDecimal amount) {
    requirePositive(amount);

    if (creditReserved.compareTo(amount) < 0) {
        throw new IllegalStateException("credit release exceeds the reserved amount");
    }

    creditReserved = creditReserved.subtract(amount);
}

public void applyCreditLimit(BigDecimal limit) {
    requireNonNegative(limit);
    this.creditLimit = limit;
}

// ---------------------------------------------------------------- validation

private static void requireNonNegative(BigDecimal amount) {
    if (amount == null || amount.signum() < 0) {
        throw new IllegalArgumentException("limit must be zero or positive");
    }
}
}
