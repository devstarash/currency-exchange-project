package ru.starashchuk.currency.exchange.dto.exchangeRate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExchangeRateCreationDto(
        @NotBlank(message = "The base code field must not be empty")
        @Size(min = 3, max = 3, message = "The base code code must be 3 characters long")
        String baseCurrencyCode,
        @NotBlank(message = "The target code field must not be empty")
        @Size(min = 3, max = 3, message = "The target code code must be 3 characters long")
        String targetCurrencyCode,
        @NotBlank(message = "The rate field must not be empty")
        String rate) {
}

