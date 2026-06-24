package ru.starashchuk.currency.exchange.dto.currency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyCreationDTO {
    @NotBlank(message = "The name field must not be empty")
    private String name;
    @NotBlank(message = "The code field must not be empty")
    @Size(min = 3, max = 3, message = "The currency code must be 3 characters long")
    private String code;
    @NotBlank(message = "The sign field must not be empty")
    @Size(max = 3, message = "The maximum length of the sign field is 3 characters")
    private String sign;
}
