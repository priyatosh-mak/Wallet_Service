package com.mightyfin.WalletService.event;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.mightyfin.WalletService.OutBox.OutBoxService;
import com.mightyfin.WalletService.entity.Currency;
import com.mightyfin.WalletService.entity.WalletType;
import com.mightyfinance.auth.starter.security.AuthPrincipal;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor 
@Component 
public class ReferenceDataEventRecorder {
    static final String CURRENCY_AGGREGATE = "currency";
    static final String CURRENCY_CREATED = "currency.created";
    static final String CURRENCY_UPDATED = "currency.updated";
    static final String CURRENCY_ACTIVATED = "currency.activated";
    static final String CURRENCY_DEACTIVATED = "currency.deactivated";

    static final String WALLET_TYPE_AGGREGATE = "wallet_type";
    static final String WALLET_TYPE_CREATED = "wallet_type.created";
    static final String WALLET_TYPE_UPDATED = "wallet_type.updated";
    static final String WALLET_TYPE_ACTIVATED = "wallet_type.activated";
    static final String WALLET_TYPE_DEACTIVATED = "wallet_type.deactivated";

        private final OutBoxService outboxService;


         public void currencyCreated(Currency currency, AuthPrincipal actor) {
        Map<String, Object> payload = currencyPayload(currency, actor);
        payload.put("minorUnits", currency.getMinorUnits());
        record(CURRENCY_CREATED, CURRENCY_AGGREGATE, currency.getCode(), payload);
    }

    public void currencyUpdated(Currency currency, AuthPrincipal actor) {
        record(CURRENCY_UPDATED, CURRENCY_AGGREGATE, currency.getCode(), currencyPayload(currency, actor));
    }

    public void currencyActivated(Currency currency, AuthPrincipal actor) {
        record(CURRENCY_ACTIVATED, CURRENCY_AGGREGATE, currency.getCode(), currencyPayload(currency, actor));
    }

    public void currencyDeactivated(Currency currency, AuthPrincipal actor) {
        record(CURRENCY_DEACTIVATED, CURRENCY_AGGREGATE, currency.getCode(), currencyPayload(currency, actor));
    }

    public void walletTypeCreated(WalletType type, AuthPrincipal actor) {
        record(WALLET_TYPE_CREATED, WALLET_TYPE_AGGREGATE, type.getCode(), walletTypePayload(type, actor));
    }

    public void walletTypeUpdated(WalletType type, AuthPrincipal actor) {
        record(WALLET_TYPE_UPDATED, WALLET_TYPE_AGGREGATE, type.getCode(), walletTypePayload(type, actor));
    }

    public void walletTypeActivated(WalletType type, AuthPrincipal actor) {
        record(WALLET_TYPE_ACTIVATED, WALLET_TYPE_AGGREGATE, type.getCode(), walletTypePayload(type, actor));
    }

    public void walletTypeDeactivated(WalletType type, AuthPrincipal actor) {
        record(WALLET_TYPE_DEACTIVATED, WALLET_TYPE_AGGREGATE, type.getCode(), walletTypePayload(type, actor));
    }

    private Map<String, Object> currencyPayload(Currency currency, AuthPrincipal actor) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("currencyId", currency.getId());
        payload.put("code", currency.getCode());
        payload.put("active", currency.isActive());
        payload.put("performedBy", actor.userId());
        return payload;
    }

    private Map<String, Object> walletTypePayload(WalletType type, AuthPrincipal actor) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("walletTypeId", type.getId());
        payload.put("code", type.getCode());
        payload.put("canExtendCredit", type.isCanExtendCredit());
        payload.put("active", type.isActive());
        payload.put("performedBy", actor.userId());
        return payload;
    }

    private void record(String eventType, String aggregateType, String code, Map<String, Object> payload) {
        outboxService.record(eventType, aggregateType, code, code, payload);
    }
}
