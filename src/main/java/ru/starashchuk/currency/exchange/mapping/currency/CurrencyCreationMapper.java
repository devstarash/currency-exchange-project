package ru.starashchuk.currency.exchange.mapping.currency;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.starashchuk.currency.exchange.dto.currency.CurrencyCreationDto;
import ru.starashchuk.currency.exchange.model.Currency;

@Mapper(componentModel = "spring")
public interface CurrencyCreationMapper {
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(source = "name", target = "fullName")
    })
    Currency toEntity(CurrencyCreationDto dto);
}
