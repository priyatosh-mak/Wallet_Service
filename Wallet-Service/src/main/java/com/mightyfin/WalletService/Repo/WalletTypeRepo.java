package com.mightyfin.WalletService.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mightyfin.WalletService.entity.WalletType;

public interface WalletTypeRepo extends JpaRepository<WalletType, Integer> {

    Optional<WalletType> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<WalletType> findAllByActiveTrueOrderByCodeAsc();

    List<WalletType> findAllByOrderByCodeAsc();

}
