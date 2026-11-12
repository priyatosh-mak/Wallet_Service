package com.mightyfin.WalletService.Service.Impl;

import java.util.List;
import java.util.Locale;

import org.apache.kafka.common.errors.DuplicateResourceException;
import org.apache.kafka.common.errors.ResourceNotFoundException;

import com.mightyfin.WalletService.Dto.request.CreateCurrencyRequest;
import com.mightyfin.WalletService.Dto.request.UpdateCurrencyRequest;
import com.mightyfin.WalletService.Dto.response.CurrencyResponse;
import com.mightyfin.WalletService.Repo.CurrencyRepo;
import com.mightyfin.WalletService.Security.WalletAccessGuard;
import com.mightyfin.WalletService.Service.CurrencyService;
import com.mightyfin.WalletService.entity.Currency;
import com.mightyfin.WalletService.event.ReferenceDataEventRecorder;
import com.mightyfin.WalletService.mapper.ReferenceDataMapper;
import com.mightyfinance.auth.starter.security.AuthPrincipal;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
public class CurrencyServiceImpl implements CurrencyService {
    
        private static final short DEFAULT_MINOR_UNITS = 2;

    private final CurrencyRepo currencyRepository;
    private final ReferenceDataMapper mapper;
        private final WalletAccessGuard guard;
    private final ReferenceDataEventRecorder events;

    @Override
    public List<CurrencyResponse> listActive() {
        return currencyRepository.findAllByActiveTrueOrderByCodeAsc().stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<CurrencyResponse> listAll(AuthPrincipal caller) {
        guard.requireSuperAdmin(caller);
        return currencyRepository.findAllByOrderByCodeAsc().stream().map(mapper::toResponse).toList();
    }

    @Override
    public CurrencyResponse getByCode(String code) {
        return mapper.toResponse(requireCurrency(code));
    }

    @Override
    @Transactional
    public CurrencyResponse create(CreateCurrencyRequest request, AuthPrincipal caller) {
        guard.requireSuperAdmin(caller);
        String code = request.getCode().trim().toUpperCase(Locale.ROOT);
        if (currencyRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Currency already exists: " + code);
        }
       Currency currency = new Currency();
currency.setCode(code);
currency.setName(request.getName().trim());
currency.setMinorUnits(
        request.getMinorUnits() != null
                ? request.getMinorUnits()
                : DEFAULT_MINOR_UNITS
);

currency = currencyRepository.save(currency);

        events.currencyCreated(currency, caller);
        return mapper.toResponse(currency);
    }

    @Override
    @Transactional
    public CurrencyResponse update(String code, UpdateCurrencyRequest request, AuthPrincipal caller) {
        guard.requireSuperAdmin(caller);
        Currency currency = requireCurrency(code);
        String newName = request.name().trim();
        if (!newName.equals(currency.getName())) {
            currency.rename(newName);
            events.currencyUpdated(currency, caller);
        }
        return mapper.toResponse(currency);
    }

    @Override
    @Transactional
    public CurrencyResponse activate(String code, AuthPrincipal caller) {
        guard.requireSuperAdmin(caller);
        Currency currency = requireCurrency(code);
        if (!currency.isActive()) {
            currency.activate();
            events.currencyActivated(currency, caller);
        }
        return mapper.toResponse(currency);
    }

    @Override
    @Transactional
    public CurrencyResponse deactivate(String code, AuthPrincipal caller) {
        guard.requireSuperAdmin(caller);
        Currency currency = requireCurrency(code);
        if (currency.isActive()) {
            currency.deactivate();
            events.currencyDeactivated(currency, caller);
        }
        return mapper.toResponse(currency);
    }

    private Currency requireCurrency(String code) {
        String trimmed = code.trim();
        return currencyRepository.findByCodeIgnoreCase(trimmed)
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + trimmed));
    }

    

}
