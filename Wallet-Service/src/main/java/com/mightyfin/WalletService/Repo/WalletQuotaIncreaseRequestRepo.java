package com.mightyfin.WalletService.Repo;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mightyfin.WalletService.entity.WalletQuotaIncreaseRequest;
import com.mightyfin.WalletService.entity.Enum.LimitIncreaseStatus;
import com.mightyfin.WalletService.entity.Enum.QuotaKind;

import jakarta.persistence.LockModeType;

public interface WalletQuotaIncreaseRequestRepo extends  JpaRepository<WalletQuotaIncreaseRequest, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from WalletQuotaIncreaseRequest r where r.id = :id")
    Optional<WalletQuotaIncreaseRequest> findByIdForUpdate(@Param("id") Integer id);

    @Query("select r.wallet.id from WalletQuotaIncreaseRequest r where r.id = :id")
    Optional<Integer> findWalletIdById(@Param("id") Integer id);

    @Query("select r from WalletQuotaIncreaseRequest r where r.wallet.id = :walletId and r.quotaKind = :kind "
            + "and r.status = LimitIncreaseStatus.PENDING")
    Optional<WalletQuotaIncreaseRequest> findPending(@Param("walletId") Integer walletId, @Param("kind") QuotaKind kind);

    @Query("select r from WalletQuotaIncreaseRequest r where r.wallet.id = :walletId and r.quotaKind = :kind "
            + "and r.status in :statuses order by r.decidedAt desc")
    List<WalletQuotaIncreaseRequest> findDecided(@Param("walletId") Integer walletId, @Param("kind") QuotaKind kind,
                                                 @Param("statuses") Collection<LimitIncreaseStatus> statuses, Pageable page);

    @Query(value = "select r from WalletQuotaIncreaseRequest r where r.wallet.id = :walletId order by r.createdAt desc, r.id desc",
            countQuery = "select count(r) from WalletQuotaIncreaseRequest r where r.wallet.id = :walletId")
    Page<WalletQuotaIncreaseRequest> findForWallet(@Param("walletId") Integer walletId, Pageable page);

    @Query(value = "select r from WalletQuotaIncreaseRequest r where r.status = :status order by r.createdAt asc, r.id asc",
            countQuery = "select count(r) from WalletQuotaIncreaseRequest r where r.status = :status")
    Page<WalletQuotaIncreaseRequest> findByStatusOldestFirst(@Param("status") LimitIncreaseStatus status, Pageable page);

    @Query(value = "select r from WalletQuotaIncreaseRequest r where r.status = :status order by r.createdAt desc, r.id desc",
            countQuery = "select count(r) from WalletQuotaIncreaseRequest r where r.status = :status")
    Page<WalletQuotaIncreaseRequest> findAllNewestFirstByStatus(@Param("status") LimitIncreaseStatus status, Pageable page);

    @Query(value = "select r from WalletQuotaIncreaseRequest r order by r.createdAt desc, r.id desc",
            countQuery = "select count(r) from WalletQuotaIncreaseRequest r")
    Page<WalletQuotaIncreaseRequest> findAllNewestFirst(Pageable page);

    @Query("select r.id from WalletQuotaIncreaseRequest r where r.status = LimitIncreaseStatus.PENDING "
            + "and r.expiresAt <= :now order by r.expiresAt asc")
    List<Integer> findExpiredPendingIds(@Param("now") Instant now, Pageable page);
}
