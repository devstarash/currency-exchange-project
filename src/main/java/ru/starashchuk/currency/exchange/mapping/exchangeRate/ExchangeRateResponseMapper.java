package ru.starashchuk.currency.exchange.mapping.exchangeRate;

import org.mapstruct.Mapper;
import ru.starashchuk.currency.exchange.mapping.currency.CurrencyResponseMapper;
import ru.starashchuk.currency.exchange.model.ExchangeRateResponse;
import ru.starashchuk.currency.exchange.dto.exchangeRate.ExchangeRateResponseDTO;

@Mapper(componentModel = "spring", uses = {CurrencyResponseMapper.class})
public interface ExchangeRateResponseMapper {
    ExchangeRateResponseDTO toDTO(ExchangeRateResponse exchangeRateResponse);
}
