package com.mightyfin.WalletService.Security;

import java.util.UUID;

public class WalletScope {

    private Integer ownerUserId;
    private UUID businessPublicUuid;
    private UUID tenantPublicUuid;

    public WalletScope(Integer ownerUserId, UUID businessPublicUuid, UUID tenantPublicUuid) {
        this.ownerUserId = ownerUserId;
        this.businessPublicUuid = businessPublicUuid;
        this.tenantPublicUuid = tenantPublicUuid;
    }

    public Integer getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Integer ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public UUID getBusinessPublicUuid() {
        return businessPublicUuid;
    }

    public void setBusinessPublicUuid(UUID businessPublicUuid) {
        this.businessPublicUuid = businessPublicUuid;
    }

    public UUID getTenantPublicUuid() {
        return tenantPublicUuid;
    }

    public void setTenantPublicUuid(UUID tenantPublicUuid) {
        this.tenantPublicUuid = tenantPublicUuid;
    }

    public static WalletScope customer(Integer ownerUserId, UUID tenantPublicUuid) {
        return new WalletScope(ownerUserId, null, tenantPublicUuid);
    }

    public static WalletScope business(UUID businessPublicUuid, UUID tenantPublicUuid) {
        return new WalletScope(null, businessPublicUuid, tenantPublicUuid);
    }

    // public static WalletScope of(Wallet wallet) {
    //     return new WalletScope(
    //             wallet.getUserId(),
    //             wallet.getBusinessPublicUuid(),
    //             null
    //     );
    }

