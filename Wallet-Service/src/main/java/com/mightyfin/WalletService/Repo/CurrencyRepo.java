package com.mightyfin.WalletService.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mightyfin.WalletService.entity.Currency;

public interface CurrencyRepo extends JpaRepository<Currency, Integer> {

    Optional<Currency> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<Currency> findAllByActiveTrueOrderByCodeAsc();

    List<Currency> findAllByOrderByCodeAsc();

}
