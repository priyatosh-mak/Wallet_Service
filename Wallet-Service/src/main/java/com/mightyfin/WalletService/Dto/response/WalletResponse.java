package com.mightyfin.WalletService.Dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import io.grpc.netty.shaded.io.netty.channel.unix.Limits;
import lombok.Data;

@Data 
public class WalletResponse {

    
    private Integer id;
    private Integer userId;
    private UUID businessPublicUuid;
    private WalletTypeSummary walletType;
    private CurrencySummary currency;
    private BigDecimal balance;
    private BigDecimal heldBalance;
    private BigDecimal availableBalance;
    private BigDecimal creditLimit;
    private boolean pinDashboard;
    private WalletLimits limits;
    private WalletActivation activation;
    private Instant createdAt;
    private Instant updatedAt;

    public WalletResponse() {
    }

}
