package ru.starashchuk.currency.exchange.mapping.exchange;

import org.mapstruct.Mapper;
import ru.starashchuk.currency.exchange.dto.exchange.ExchangeResponseDto;
import ru.starashchuk.currency.exchange.mapping.currency.CurrencyResponseMapper;
import ru.starashchuk.currency.exchange.model.Exchange;

@Mapper(componentModel = "spring", uses = {CurrencyResponseMapper.class})
public interface ExchangeResponseMapper {
    ExchangeResponseDto toDto(Exchange exchange);
}
