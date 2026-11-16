package com.mightyfin.WalletService.Dto.response;

import java.time.Instant;

import com.mightyfin.WalletService.entity.Enum.WalletStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class WalletActivation {


    private WalletStatus status;
    private boolean active;
    private Instant verifiedAt;

    public WalletActivation() {
    }
}
