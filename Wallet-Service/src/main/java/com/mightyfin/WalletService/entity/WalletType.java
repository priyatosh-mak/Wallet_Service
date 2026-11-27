package com.mightyfin.WalletService.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.mightyfin.WalletService.entity.Enum.DisbursementQuotaPeriod;
import com.mightyfin.WalletService.entity.Enum.WalletLimitPeriod;
import com.mightyfin.WalletService.entity.Enum.WalletTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder 
@Table (name = "wallet_types")
@AllArgsConstructor 
@NoArgsConstructor 
public class WalletType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 64, unique = true, updatable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "default_credit_limit", precision = 18, scale = 2)
    private BigDecimal defaultCreditLimit;

    /** FALSE: wallets of this type must not turn Limit 1 (supply on credit) on. */
    @Column(name = "may_enable_supply", nullable = false)
    private boolean mayEnableSupply;

    /** FALSE: wallets of this type must not turn Limit 2 (origination) on. */
    @Column(name = "may_enable_origination", nullable = false)
    private boolean mayEnableOrigination;

    /** Catalog hint shown to the admin who enables supply. Never applied automatically. */
    @Column(name = "default_supply_quota_limit", precision = 18, scale = 2)
    private BigDecimal defaultSupplyQuotaLimit;

    /** Catalog hint shown to the admin who enables origination. Never applied automatically. */
    @Column(name = "default_origination_quota_limit", precision = 18, scale = 2)
    private BigDecimal defaultOriginationQuotaLimit;

    @Column(name = "default_general_transaction_limit", precision = 18, scale = 2)
    private BigDecimal defaultGeneralTransactionLimit;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "default_general_transaction_period", nullable = false, length = 16)
    private WalletLimitPeriod defaultGeneralTransactionPeriod = WalletLimitPeriod.DAILY;

    @Column(name = "value_chain_category")
    private String valueChainCategory;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public boolean hasCode(WalletTypeCode expected) {
        return expected.name().equalsIgnoreCase(code);
    }

    /** Replaces the editable attributes. The code never changes. Existing wallets keep the limits they were created with. */
    public void update(String name, String description, BigDecimal defaultCreditLimit,
                       boolean mayEnableSupply, boolean mayEnableOrigination,
                       BigDecimal defaultSupplyQuotaLimit, BigDecimal defaultOriginationQuotaLimit,
                       BigDecimal defaultGeneralTransactionLimit, WalletLimitPeriod defaultGeneralTransactionPeriod,
                       String valueChainCategory) {
        this.name = name;
        this.description = description;
        this.defaultCreditLimit = defaultCreditLimit;
        this.mayEnableSupply = mayEnableSupply;
        this.mayEnableOrigination = mayEnableOrigination;
        this.defaultSupplyQuotaLimit = defaultSupplyQuotaLimit;
        this.defaultOriginationQuotaLimit = defaultOriginationQuotaLimit;
        this.defaultGeneralTransactionLimit = defaultGeneralTransactionLimit;
        this.defaultGeneralTransactionPeriod = defaultGeneralTransactionPeriod;
        this.valueChainCategory = valueChainCategory;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
