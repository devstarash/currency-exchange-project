package ru.starashchuk.currency.exchange.mapping.currency;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.starashchuk.currency.exchange.dto.currency.CurrencyResponseDto;
import ru.starashchuk.currency.exchange.model.Currency;

@Mapper(componentModel = "spring")
public interface CurrencyResponseMapper {
    @Mapping(target = "name", source = "fullName")
    CurrencyResponseDto toDto(Currency currency);
}
