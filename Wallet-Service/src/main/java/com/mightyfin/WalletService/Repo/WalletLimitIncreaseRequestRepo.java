package com.mightyfin.WalletService.Repo;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mightyfin.WalletService.entity.WalletLimitIncreaseRequest;
import com.mightyfin.WalletService.entity.Enum.LimitIncreaseStatus;

import jakarta.persistence.LockModeType;

public interface WalletLimitIncreaseRequestRepo extends JpaRepository<WalletLimitIncreaseRequest, Integer> {


    /**
     * Row lock for deciding a request. Always take the wallet lock first, then this one, in every code path, so two
     * paths can never wait on each other.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from WalletLimitIncreaseRequest r where r.id = :id")
    Optional<WalletLimitIncreaseRequest> findByIdForUpdate(@Param("id") Integer id);

    /** The wallet of a request, without loading the request (so the later locked read is fresh). */
    @Query("select r.wallet.id from WalletLimitIncreaseRequest r where r.id = :id")
    Optional<Integer> findWalletIdById(@Param("id") Integer id);

    @Query("select r from WalletLimitIncreaseRequest r where r.wallet.id = :walletId and r.status = com.mightyfin.WalletService.entity.Enum.LimitIncreaseStatus.PENDING")
    Optional<WalletLimitIncreaseRequest> findPendingByWalletId(@Param("walletId") Integer walletId);

    /** Most recent decision first. Used for the cooldown after an approval or a rejection. */
    @Query("select r from WalletLimitIncreaseRequest r where r.wallet.id = :walletId and r.status in :statuses order by r.decidedAt desc")
    List<WalletLimitIncreaseRequest> findDecided(@Param("walletId") Integer walletId,
                                                 @Param("statuses") Collection<LimitIncreaseStatus> statuses,
                                                 Pageable page);

    @Query(value = "select r from WalletLimitIncreaseRequest r where r.wallet.id = :walletId order by r.createdAt desc, r.id desc",
            countQuery = "select count(r) from WalletLimitIncreaseRequest r where r.wallet.id = :walletId")
    Page<WalletLimitIncreaseRequest> findForWallet(@Param("walletId") Integer walletId, Pageable page);

    /** The admin queue: oldest first, so the longest-waiting request is decided first. */
    @Query(value = "select r from WalletLimitIncreaseRequest r where r.status = :status order by r.createdAt asc, r.id asc",
            countQuery = "select count(r) from WalletLimitIncreaseRequest r where r.status = :status")
    Page<WalletLimitIncreaseRequest> findByStatusOldestFirst(@Param("status") LimitIncreaseStatus status, Pageable page);

    @Query(value = "select r from WalletLimitIncreaseRequest r where r.status = :status order by r.createdAt desc, r.id desc",
            countQuery = "select count(r) from WalletLimitIncreaseRequest r where r.status = :status")
    Page<WalletLimitIncreaseRequest> findAllNewestFirstByStatus(@Param("status") LimitIncreaseStatus status, Pageable page);

    @Query(value = "select r from WalletLimitIncreaseRequest r order by r.createdAt desc, r.id desc",
            countQuery = "select count(r) from WalletLimitIncreaseRequest r")
    Page<WalletLimitIncreaseRequest> findAllNewestFirst(Pageable page);

    /** Open requests whose time is up. */
    @Query("select r.id from WalletLimitIncreaseRequest r where r.status = com.mightyfin.WalletService.entity.Enum.LimitIncreaseStatus.PENDING and r.expiresAt <= :now order by r.expiresAt asc")
    List<Integer> findExpiredPendingIds(@Param("now") Instant now, Pageable page);
}