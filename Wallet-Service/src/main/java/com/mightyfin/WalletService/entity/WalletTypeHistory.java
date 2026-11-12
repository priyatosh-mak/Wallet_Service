// package com.mightyfin.WalletService.entity;

// import java.time.Instant;

// import org.hibernate.annotations.CreationTimestamp;
// import org.hibernate.annotations.UpdateTimestamp;

// import jakarta.persistence.Column;
// import jakarta.persistence.FetchType;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;

// public class WalletTypeHistory {

//     @Id 
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Integer id;

//     @ManyToOne(fetch = FetchType.LAZY, optional = false)
//     @JoinColumn(name = "wallet_id", nullable = false, updatable = false)
//     private Wallet wallet;

//     @ManyToOne(fetch = FetchType.LAZY, optional = false)
//     @JoinColumn(name = "wallet_type_id", nullable = false, updatable = false)
//     private WalletType walletType;

//     @Column(name = "effective_from", nullable = false, updatable = false)
//     private Instant effectiveFrom;

//     /** NULL = current assignment. */
//     @Column(name = "effective_to")
//     private Instant effectiveTo;

//     @Column(name = "superseded_by_id")
//     private Integer supersededById;

//     @CreationTimestamp
//     @Column(name = "created_at", nullable = false, updatable = false)
//     private Instant createdAt;

//     @UpdateTimestamp
//     @Column(name = "updated_at", nullable = false)
//     private Instant updatedAt;

//     /** A new, open assignment. */
//     public static WalletTypeHistory open(Wallet wallet, WalletType type, Instant effectiveFrom) {
//         return WalletTypeHistory.builder().wallet(wallet).walletType(type).effectiveFrom(effectiveFrom).build();
//     }

//     public boolean isCurrent() {
//         return effectiveTo == null;
//     }

//     public void close(Instant effectiveTo) {
//         this.effectiveTo = effectiveTo;
//     }

//     public void supersededBy(Integer nextHistoryId) {
//         this.supersededById = nextHistoryId;
//     }
// }
