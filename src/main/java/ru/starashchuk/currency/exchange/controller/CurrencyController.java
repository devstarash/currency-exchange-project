package ru.starashchuk.currency.exchange.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.starashchuk.currency.exchange.dto.currency.CurrencyCreationDto;
import ru.starashchuk.currency.exchange.dto.currency.CurrencyResponseDto;
import ru.starashchuk.currency.exchange.mapping.currency.CurrencyCreationMapper;
import ru.starashchuk.currency.exchange.mapping.currency.CurrencyResponseMapper;
import ru.starashchuk.currency.exchange.model.Currency;
import ru.starashchuk.currency.exchange.service.CurrencyService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CurrencyController {
    private final CurrencyService currencyService;
    private final CurrencyResponseMapper responseMapper;
    private final CurrencyCreationMapper creationMapper;

    @GetMapping("/currencies")
    public List<CurrencyResponseDto> findAllCurrencies() {
        List<CurrencyResponseDto> currencies = currencyService.findAllCurrencies().stream().map(responseMapper::toDto)
                .toList();
        return currencies;
    }

    @GetMapping({"currency/", "currency/{code}"})
    public CurrencyResponseDto findCurrencyByCode(@PathVariable(value = "code", required = false) String code) {
        Currency foundCurrency = currencyService.findCurrencyByCode(code);
        CurrencyResponseDto response = responseMapper.toDto(foundCurrency);
        return response;
    }

    @PostMapping("/currencies")
    @ResponseStatus(HttpStatus.CREATED)
    public CurrencyResponseDto save(@Valid CurrencyCreationDto currencyToSave) {
        Currency currency = creationMapper.toEntity(currencyToSave);
        Currency savedCurrency = currencyService.save(currency);
        CurrencyResponseDto response = responseMapper.toDto(savedCurrency);
        return response;
    }
}