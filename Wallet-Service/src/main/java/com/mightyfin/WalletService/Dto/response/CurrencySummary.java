package com.mightyfin.WalletService.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class CurrencySummary {


    private Integer id;
    private String code;
    private String name;
    private Short minorUnits;

    public CurrencySummary() {
    }

}
