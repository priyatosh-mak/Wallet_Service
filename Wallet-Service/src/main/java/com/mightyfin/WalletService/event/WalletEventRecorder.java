package com.mightyfin.WalletService.event;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.mightyfin.WalletService.OutBox.OutBoxService;
import com.mightyfin.WalletService.entity.Wallet;
import com.mightyfinance.auth.starter.security.AuthPrincipal;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class WalletEventRecorder {


    static final String AGGREGATE_TYPE = "wallet";
    static final String CREATED = "wallet.created";
    static final String ACTIVATED = "wallet.activated";
    static final String SUSPENDED = "wallet.suspended";
    static final String TYPE_CHANGED = "wallet.type_changed";

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
}
