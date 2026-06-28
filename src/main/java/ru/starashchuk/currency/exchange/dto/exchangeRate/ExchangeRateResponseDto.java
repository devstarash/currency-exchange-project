package ru.starashchuk.currency.exchange.dto.exchangeRate;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ru.starashchuk.currency.exchange.dto.currency.CurrencyResponseDto;

import java.math.BigDecimal;

@JsonPropertyOrder({"id", "baseCurrency", "targetCurrency", "rate"})
public record ExchangeRateResponseDto(long id, CurrencyResponseDto baseCurrency,
                                      CurrencyResponseDto targetCurrency, BigDecimal rate) {
}
