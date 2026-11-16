package com.mightyfin.WalletService.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mightyfin.WalletService.entity.WalletTypeHistory;

public interface WalletTypeHistoryRepo extends JpaRepository<WalletTypeHistory, Integer> {

    Optional<WalletTypeHistory> findByWalletIdAndEffectiveToIsNull(Integer walletId);

    List<WalletTypeHistory> findByWalletIdOrderByEffectiveFromDescIdDesc(Integer walletId);

}
