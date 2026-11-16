package com.mightyfin.WalletService.Repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mightyfin.WalletService.entity.Wallet;

import jakarta.persistence.LockModeType;

public interface WalletRepo extends JpaRepository<Wallet, Integer> {

    Optional<Wallet> findByUserId(Integer userId);

    Optional<Wallet> findByBusinessPublicUuid(UUID businessPublicUuid);

    boolean existsByUserId(Integer userId);

    boolean existsByBusinessPublicUuid(UUID businessPublicUuid);

    /** Row lock for state changes (activate, suspend, type change) so they cannot race. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") Integer id);
}
