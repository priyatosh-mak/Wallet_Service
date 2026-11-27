package com.mightyfin.WalletService.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import lombok.Data;

@ConfigurationProperties (prefix = "wallet.creation")
@Data 
public class WalletCreationProperties {

    private boolean autoActivateCustomers = true;
}
