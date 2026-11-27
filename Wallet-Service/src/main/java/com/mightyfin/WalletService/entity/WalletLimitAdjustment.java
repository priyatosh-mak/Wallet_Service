package com.mightyfin.WalletService.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@Entity 
@AllArgsConstructor 
public class WalletLimitAdjustment {

    public static final String KIND_SUPPLY = "SUPPLY_QUOTA";
    public static final String KIND_ORIGINATION = "ORIGINATION_QUOTA";
    public static final String KIND_CREDIT = "CREDIT";

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", nullable = false, updatable = false)
    private Integer walletId;

    @Column(name = "limit_kind", nullable = false, length = 32, updatable = false)
    private String limitKind;

    @Column(name = "old_limit", nullable = false, precision = 18, scale = 2, updatable = false)
    private BigDecimal oldLimit;

    @Column(name = "new_limit", nullable = false, precision = 18, scale = 2, updatable = false)
    private BigDecimal newLimit;

    /** Whether the quota gate was on before / after the edit. Null for a credit limit. */
    @Column(name = "old_enabled", updatable = false)
    private Boolean oldEnabled;

    @Column(name = "new_enabled", updatable = false)
    private Boolean newEnabled;

    @Column(nullable = false, length = 500, updatable = false)
    private String reason;

    @Column(name = "adjusted_by", nullable = false, updatable = false)
    private Integer adjustedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

}
