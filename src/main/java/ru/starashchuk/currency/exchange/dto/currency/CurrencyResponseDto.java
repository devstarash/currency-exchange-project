package ru.starashchuk.currency.exchange.dto.currency;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "name", "code", "sign"})
public record CurrencyResponseDto(long id, String name, String code, String sign) {
}
