package com.mightyfin.WalletService.Dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class CreateCurrencyRequest {

    @NotBlank(message = "code is required")
    @Pattern (regexp = "^[A-Za-z]{3}$", message = "code must be a 3-letter ISO 4217 code")
    private String code;

    @NotBlank(message = "name is required")
    @Size(max = 100)
    private String name;

    @Min(0)
    @Max(6)
    private Short minorUnits;
}
