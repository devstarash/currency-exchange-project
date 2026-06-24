package ru.starashchuk.currency.exchange.mapping.currency;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.starashchuk.currency.exchange.dto.currency.CurrencyResponseDTO;
import ru.starashchuk.currency.exchange.model.Currency;

@Mapper(componentModel = "spring")
public interface CurrencyResponseMapper {
    @Mapping(target = "name", source = "fullName")
    CurrencyResponseDTO toDTO(Currency currency);
}
