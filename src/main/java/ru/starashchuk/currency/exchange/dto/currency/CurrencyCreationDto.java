package ru.starashchuk.currency.exchange.dto.currency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CurrencyCreationDto(
        @NotBlank(message = "The name field must not be empty")
        String name,
        @NotBlank(message = "The code field must not be empty")
        @Size(min = 3, max = 3, message = "The currency code must be 3 characters long")
        String code,
        @NotBlank(message = "The sign field must not be empty")
        @Size(max = 3, message = "The maximum length of the sign field is 3 characters")
        String sign) {
}

