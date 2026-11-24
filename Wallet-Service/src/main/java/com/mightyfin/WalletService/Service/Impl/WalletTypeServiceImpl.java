package com.mightyfin.WalletService.Service.Impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.apache.kafka.common.errors.DuplicateResourceException;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import com.mightyfin.WalletService.Dto.request.CreateWalletTypeRequest;
import com.mightyfin.WalletService.Dto.request.UpdateWalletTypeRequest;
import com.mightyfin.WalletService.Dto.response.WalletTypeResponse;
import com.mightyfin.WalletService.Exception.BusinessRuleException;
import com.mightyfin.WalletService.Repo.WalletTypeRepo;
import com.mightyfin.WalletService.Security.WalletAccessGuard;
import com.mightyfin.WalletService.Service.WalletTypeService;
import com.mightyfin.WalletService.entity.WalletType;
import com.mightyfin.WalletService.entity.Enum.DisbursementQuotaPeriod;
import com.mightyfin.WalletService.entity.Enum.WalletLimitPeriod;
import com.mightyfin.WalletService.entity.Enum.WalletTypeCode;
import com.mightyfin.WalletService.event.ReferenceDataEventRecorder;
import com.mightyfin.WalletService.mapper.ReferenceDataMapper;
import com.mightyfinance.auth.starter.security.AuthPrincipal;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@Service 
public class WalletTypeServiceImpl implements WalletTypeService{
    private final WalletTypeRepo walletTypeRepository;
    private final ReferenceDataMapper mapper;
    private final WalletAccessGuard guard;
    private final ReferenceDataEventRecorder events;

    @Override
    public List<WalletTypeResponse> listActive() {
        return walletTypeRepository.findAllByActiveTrueOrderByCodeAsc().stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<WalletTypeResponse> listAll(AuthPrincipal caller) {
        guard.requireSuperAdmin(caller);
        return walletTypeRepository.findAllByOrderByCodeAsc().stream().map(mapper::toResponse).toList();
    }

    @Override
    public WalletTypeResponse getByCode(String code) {
        return mapper.toResponse(requireType(code));
    }

    @Override
    @Transactional 
    public WalletTypeResponse create(CreateWalletTypeRequest request, AuthPrincipal caller) {
        guard.requireSuperAdmin(caller);
        String code = request.getCode().trim().toUpperCase(Locale.ROOT);
        if (walletTypeRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Wallet type already exists: " + code);
        }
        boolean canExtendCredit = Boolean.TRUE.equals(request.getCanExtendCredit());
        requireConsistentQuota(canExtendCredit, request.getDefaultDisbursementQuotaLimit());

        WalletType type = walletTypeRepository.save(WalletType.builder()
                .code(code)
                .name(request.getName().trim())
                .description(blankToNull(request.getDescription()))
                .defaultCreditLimit(request.getDefaultCreditLimit())
                .canExtendCredit(canExtendCredit)
                .defaultDisbursementQuotaLimit(request.getDefaultDisbursementQuotaLimit())
                .defaultDisbursementQuotaPeriod(request.getDefaultDisbursementQuotaPeriod() != null
                        ? request.getDefaultDisbursementQuotaPeriod() : DisbursementQuotaPeriod.MONTHLY)
                .defaultGeneralTransactionLimit(request.getDefaultGeneralTransactionLimit())
                .defaultGeneralTransactionPeriod(request.getDefaultGeneralTransactionPeriod() != null
                        ? request.getDefaultGeneralTransactionPeriod() : WalletLimitPeriod.DAILY)
                .valueChainCategory(blankToNull(request.getValueChainCategory()))
                .build());

        events.walletTypeCreated(type, caller);
        return mapper.toResponse(type);
    }

    // @Override
    // @Transactional
    // public WalletTypeResponse update(String code, UpdateWalletTypeRequest request, AuthPrincipal caller) {
    //     guard.requireSuperAdmin(caller);
    //     WalletType type = requireType(code);
    //     boolean canExtendCredit = Boolean.TRUE.equals(request.canExtendCredit());
    //     requireConsistentQuota(canExtendCredit, request.defaultDisbursementQuotaLimit());

    //     type.update(request.name().trim(),
    //             blankToNull(request.description()),
    //             request.defaultCreditLimit(),
    //             canExtendCredit,
    //             request.defaultDisbursementQuotaLimit(),
    //             request.defaultDisbursementQuotaPeriod(),
    //             request.defaultGeneralTransactionLimit(),
    //             request.defaultGeneralTransactionPeriod(),
    //             blankToNull(request.valueChainCategory()));

    //     events.walletTypeUpdated(type, caller);
    //     return mapper.toResponse(type);
    // }

    @Override
@Transactional
public WalletTypeResponse update(String code, UpdateWalletTypeRequest request, AuthPrincipal caller) {
    guard.requireSuperAdmin(caller);

    WalletType type = requireType(code);

    boolean canExtendCredit = Boolean.TRUE.equals(request.getCanExtendCredit());
    requireConsistentQuota(canExtendCredit, request.getDefaultDisbursementQuotaLimit());

    type.setName(request.getName().trim());
    type.setDescription(blankToNull(request.getDescription()));
    type.setDefaultCreditLimit(request.getDefaultCreditLimit());
    type.setCanExtendCredit(canExtendCredit);
    type.setDefaultDisbursementQuotaLimit(request.getDefaultDisbursementQuotaLimit());
    type.setDefaultDisbursementQuotaPeriod(request.getDefaultDisbursementQuotaPeriod());
    type.setDefaultGeneralTransactionLimit(request.getDefaultGeneralTransactionLimit());
    type.setDefaultGeneralTransactionPeriod(request.getDefaultGeneralTransactionPeriod());
    type.setValueChainCategory(blankToNull(request.getValueChainCategory()));

    events.walletTypeUpdated(type, caller);

    return mapper.toResponse(type);
}

    @Override
    @Transactional
    public WalletTypeResponse activate(String code, AuthPrincipal caller) {
        guard.requireSuperAdmin(caller);
        WalletType type = requireType(code);
        if (!type.isActive()) {
            type.activate();
            events.walletTypeActivated(type, caller);
        }
        return mapper.toResponse(type);
    }

    @Override
    @Transactional
    public WalletTypeResponse deactivate(String code, AuthPrincipal caller) {
        guard.requireSuperAdmin(caller);
        WalletType type = requireType(code);
        if (WalletTypeCode.isReserved(type.getCode())) {
            throw new BusinessRuleException("WALLET_TYPE_PROTECTED",
                    "Wallet type '" + type.getCode() + "' is required by the service and cannot be deactivated");
        }
        if (type.isActive()) {
            type.deactivate();
            events.walletTypeDeactivated(type, caller);
        }
        return mapper.toResponse(type);
    }

    private WalletType requireType(String code) {
        String trimmed = code.trim();
        return walletTypeRepository.findByCodeIgnoreCase(trimmed)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet type not found: " + trimmed));
    }

    /** A disbursement quota only makes sense for types that may grant credit to others. */
    private static void requireConsistentQuota(boolean canExtendCredit, BigDecimal disbursementQuotaLimit) {
        if (!canExtendCredit && disbursementQuotaLimit != null) {
            throw new BusinessRuleException("INVALID_WALLET_TYPE_CONFIG",
                    "A disbursement quota limit can only be set on a type that can extend credit");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    

    

}
