package com.mightyfin.WalletService.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class WalletTypeSummary {


    private Integer id;
    private String code;
    private String name;
    private boolean canExtendCredit;

    public WalletTypeSummary() {
    }
}
