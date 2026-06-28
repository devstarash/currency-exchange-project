package ru.starashchuk.currency.exchange.dto.exchange;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import ru.starashchuk.currency.exchange.dto.currency.CurrencyResponseDto;

import java.math.BigDecimal;

@JsonPropertyOrder({"baseCurrency", "targetCurrency", "rate", "amount", "convertedAmount"})
public record ExchangeResponseDto(CurrencyResponseDto baseCurrency, CurrencyResponseDto targetCurrency, BigDecimal rate,
                                  BigDecimal amount, BigDecimal convertedAmount) {
}
