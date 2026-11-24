package com.mightyfin.WalletService.Dto.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class WalletTypeHistoryResponse {


    private Integer id;
    private Integer walletId;
    private String walletTypeCode;
    private String walletTypeName;
    private Instant effectiveFrom;
    private Instant effectiveTo;
    private Integer supersededById;
    private boolean current;
}
