package com.mightyfin.WalletService.event;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.mightyfin.WalletService.OutBox.OutBoxService;
import com.mightyfin.WalletService.entity.Wallet;
import com.mightyfin.WalletService.entity.WalletCreditReservation;
import com.mightyfin.WalletService.entity.WalletLimitIncreaseRequest;
import com.mightyfin.WalletService.entity.WalletQuotaIncreaseRequest;
import com.mightyfin.WalletService.entity.Enum.QuotaKind;
import com.mightyfinance.auth.starter.security.AuthPrincipal;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
@Data 
public class WalletEventRecorder {


    static final String AGGREGATE_TYPE = "wallet";
    static final String CREATED = "wallet.created";
    static final String ACTIVATED = "wallet.activated";
    static final String SUSPENDED = "wallet.suspended";
    static final String TYPE_CHANGED = "wallet.type_changed";
    static final String QUOTA_CHANGED = "wallet.quota_changed";
    static final String QUOTA_INCREASE_SUBMITTED = "wallet.quota_increase.submitted";
    static final String QUOTA_INCREASE_APPROVED = "wallet.quota_increase.approved";
    static final String QUOTA_INCREASE_REJECTED = "wallet.quota_increase.rejected";
    static final String QUOTA_INCREASE_CANCELLED = "wallet.quota_increase.cancelled";
    static final String QUOTA_INCREASE_EXPIRED = "wallet.quota_increase.expired";
    static final String QUOTA_RESTORED = "wallet.quota_restored";
    static final String LIMIT_CHANGED = "wallet.limit_changed";
    static final String LIMIT_INCREASE_SUBMITTED = "wallet.limit_increase.submitted";
    static final String LIMIT_INCREASE_APPROVED = "wallet.limit_increase.approved";
    static final String LIMIT_INCREASE_REJECTED = "wallet.limit_increase.rejected";
    static final String LIMIT_INCREASE_CANCELLED = "wallet.limit_increase.cancelled";
    static final String LIMIT_INCREASE_EXPIRED = "wallet.limit_increase.expired";
    static final String LIMIT_INCREASE_SUPERSEDED = "wallet.limit_increase.superseded"; 

    private final OutBoxService outboxService;

    public void walletCreated(Wallet wallet) {
        Map<String, Object> payload = basePayload(wallet);
        payload.put("userId", wallet.getUserId());
        payload.put("businessPublicUuid", wallet.getBusinessPublicUuid() == null
                ? null : wallet.getBusinessPublicUuid().toString());
        payload.put("currencyCode", wallet.getCurrency().getCode());
        record(CREATED, wallet, payload);
    }

    public void walletActivated(Wallet wallet, boolean firstActivation, String approvalReference, AuthPrincipal actor) {
        Map<String, Object> payload = basePayload(wallet);
        payload.put("firstActivation", firstActivation);
        payload.put("approvalReference", approvalReference);
        payload.put("performedBy", actor.userId());
        record(ACTIVATED, wallet, payload);
    }

    public void walletSuspended(Wallet wallet, AuthPrincipal actor) {
        Map<String, Object> payload = basePayload(wallet);
        payload.put("performedBy", actor.userId());
        record(SUSPENDED, wallet, payload);
    }

    public void walletTypeChanged(Wallet wallet, String previousTypeCode, AuthPrincipal actor) {
        Map<String, Object> payload = basePayload(wallet);
        payload.put("previousWalletTypeCode", previousTypeCode);
        payload.put("performedBy", actor.userId());
        record(TYPE_CHANGED, wallet, payload);
    }

    public void walletLimitChanged(Wallet wallet, java.math.BigDecimal previousLimit, String previousPeriod,
                                   AuthPrincipal actor) {
        Map<String, Object> payload = basePayload(wallet);
        payload.put("previousGeneralTransactionLimit", previousLimit);
        payload.put("previousGeneralTransactionPeriod", previousPeriod);
        payload.put("generalTransactionLimit", wallet.getGeneralTransactionLimit());
        payload.put("generalTransactionPeriod", wallet.getGeneralTransactionPeriod().name());
        payload.put("performedBy", actor.userId());
        record(LIMIT_CHANGED, wallet, payload);
    }

     public void limitIncreaseSubmitted(WalletLimitIncreaseRequest r) {
        limitIncrease(LIMIT_INCREASE_SUBMITTED, r);
    }

    public void limitIncreaseApproved(WalletLimitIncreaseRequest r) {
        limitIncrease(LIMIT_INCREASE_APPROVED, r);
    }

    public void limitIncreaseRejected(WalletLimitIncreaseRequest r) {
        limitIncrease(LIMIT_INCREASE_REJECTED, r);
    }

    public void limitIncreaseCancelled(WalletLimitIncreaseRequest r) {
        limitIncrease(LIMIT_INCREASE_CANCELLED, r);
    }

    public void limitIncreaseExpired(WalletLimitIncreaseRequest r) {
        limitIncrease(LIMIT_INCREASE_EXPIRED, r);
    }

    private void limitIncrease(String eventType, WalletLimitIncreaseRequest r) {
        Wallet wallet = r.getWallet();
        Map<String, Object> payload = basePayload(wallet);
        payload.put("requestId", r.getId());
        payload.put("status", r.getStatus().name());
        payload.put("requestedByUserId", r.getRequestedByUserId());
        payload.put("period", r.getPeriod().name());
        payload.put("currentLimit", r.getCurrentLimit());
        payload.put("requestedLimit", r.getRequestedLimit());
        payload.put("approvedLimit", r.getApprovedLimit());
        payload.put("decidedByUserId", r.getDecidedByUserId());
        record(eventType, wallet, payload);
    }

    private Map<String, Object> basePayload(Wallet wallet) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("walletId", wallet.getId());
        payload.put("walletTypeCode", wallet.getWalletType().getCode());
        return payload;
    }

    private void record(String eventType, Wallet wallet, Map<String, Object> payload) {
        String walletId = String.valueOf(wallet.getId());
        outboxService.record(eventType, AGGREGATE_TYPE, walletId, walletId, payload);
    }

    public void quotaChanged(Wallet wallet, QuotaKind kind, java.math.BigDecimal previousRemaining,
                             boolean previousEnabled, AuthPrincipal actor) {
        Map<String, Object> payload = basePayload(wallet);
        payload.put("quota", kind.name());
        payload.put("previousEnabled", previousEnabled);
        payload.put("enabled", wallet.isQuotaEnabled(kind));
        payload.put("previousRemaining", previousRemaining);
        payload.put("remaining", wallet.getQuotaRemaining(kind));
        payload.put("performedBy", actor.userId());
        record(QUOTA_CHANGED, wallet, payload);
    }

    public void quotaIncreaseSubmitted(WalletQuotaIncreaseRequest r) {
        quotaIncrease(QUOTA_INCREASE_SUBMITTED, r);
    }

    public void quotaIncreaseApproved(WalletQuotaIncreaseRequest r) {
        quotaIncrease(QUOTA_INCREASE_APPROVED, r);
    }

    public void quotaIncreaseRejected(WalletQuotaIncreaseRequest r) {
        quotaIncrease(QUOTA_INCREASE_REJECTED, r);
    }

    public void quotaIncreaseCancelled(WalletQuotaIncreaseRequest r) {
        quotaIncrease(QUOTA_INCREASE_CANCELLED, r);
    }

    public void quotaIncreaseExpired(WalletQuotaIncreaseRequest r) {
        quotaIncrease(QUOTA_INCREASE_EXPIRED, r);
    }

    /** Principal was repaid and quota went back to the wallet(s) that supplied / originated the loan. */
    public void quotaRestored(Wallet borrower, WalletCreditReservation r, java.math.BigDecimal restored) {
        Map<String, Object> payload = reservationPayload(borrower, r);
        payload.put("restored", restored);
        payload.put("restoredTotal", r.getRestoredAmount());
        record(QUOTA_RESTORED, borrower, payload);
    }

    private Map<String, Object> reservationPayload(Wallet borrower, WalletCreditReservation r) {
        Map<String, Object> payload = basePayload(borrower);
        payload.put("reservationId", r.getId());
        payload.put("agreementId", r.getAgreementId());
        payload.put("status", r.getStatus().name());
        payload.put("amount", r.getAmount());
        payload.put("supplierWalletId", r.getSupplierWalletId());
        payload.put("originationWalletId", r.getOriginationWalletId());
        return payload;
    }

    private void quotaIncrease(String eventType, WalletQuotaIncreaseRequest r) {
        Wallet wallet = r.getWallet();
        Map<String, Object> payload = basePayload(wallet);
        payload.put("requestId", r.getId());
        payload.put("quota", r.getQuotaKind().name());
        payload.put("status", r.getStatus().name());
        payload.put("requestedByUserId", r.getRequestedByUserId());
        payload.put("remainingWhenAsked", r.getRemainingWhenAsked());
        payload.put("requestedIncrease", r.getRequestedIncrease());
        payload.put("approvedIncrease", r.getApprovedIncrease());
        payload.put("decidedByUserId", r.getDecidedByUserId());
        record(eventType, wallet, payload);
    }
}
