package ru.starashchuk.currency.exchange.mapping.exchangeRate;

import org.mapstruct.Mapper;
import ru.starashchuk.currency.exchange.dto.exchangeRate.ExchangeRateResponseDto;
import ru.starashchuk.currency.exchange.mapping.currency.CurrencyResponseMapper;
import ru.starashchuk.currency.exchange.model.ExchangeRateResponse;

@Mapper(componentModel = "spring", uses = {CurrencyResponseMapper.class})
public interface ExchangeRateResponseMapper {
    ExchangeRateResponseDto toDto(ExchangeRateResponse exchangeRateResponse);
}
