package com.mightyfin.WalletService.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class CurrencyResponse {

    private Integer id;
    private String code;
    private String name;
    private Short minorUnits;
    private boolean active;
}
