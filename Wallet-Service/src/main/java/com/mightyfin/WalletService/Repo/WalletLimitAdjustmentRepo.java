package com.mightyfin.WalletService.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mightyfin.WalletService.entity.WalletLimitAdjustment;

public interface WalletLimitAdjustmentRepo extends JpaRepository<WalletLimitAdjustment, Long> {

    List<WalletLimitAdjustment> findByWalletIdOrderByIdDesc(Integer walletId);

}
