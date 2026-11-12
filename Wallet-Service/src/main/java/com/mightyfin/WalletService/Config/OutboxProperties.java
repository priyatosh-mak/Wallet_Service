package com.mightyfin.WalletService.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data 
@ConfigurationProperties(prefix = "wallet.outbox")
public class OutboxProperties {

    private boolean publisherEnabled = false;
    private String topic = "wallet.events";
    private int batchSize = 50;
    private int maxAttempts = 10;
    private long pollIntervalMs = 1000;
    private long retryBackoffSeconds = 30;
    private long sendTimeoutSeconds = 10;
}
