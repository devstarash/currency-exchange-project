package ru.starashchuk.currency.exchange.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import ru.starashchuk.currency.exchange.controller.exception.BadRequestException;
import ru.starashchuk.currency.exchange.dto.exchangeRate.ExchangeRateCreationDto;
import ru.starashchuk.currency.exchange.dto.exchangeRate.ExchangeRateResponseDto;
import ru.starashchuk.currency.exchange.mapping.exchangeRate.ExchangeRateResponseMapper;
import ru.starashchuk.currency.exchange.model.ExchangeRateResponse;
import ru.starashchuk.currency.exchange.service.ExchangeRateService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExchangeRateController {
    private final ExchangeRateService exchangeRateService;
    private final ExchangeRateResponseMapper responseMapper;

    @GetMapping("/exchangeRates")
    public List<ExchangeRateResponseDto> findAllExchangeRates() {
        List<ExchangeRateResponseDto> exchangeRates = exchangeRateService.findAllExchangeRates().stream()
                .map(responseMapper::toDto).toList();
        return exchangeRates;
    }

    @GetMapping({"/exchangeRate/", "/exchangeRate/{currencyPair}"})
    public ExchangeRateResponseDto findExchangeRateByCurrencyPair(
            @PathVariable(value = "currencyPair", required = false) String currencyPair) {
        ExchangeRateResponse foundExchangeRate = exchangeRateService.findExchangeRateByCurrencyPair(currencyPair);
        ExchangeRateResponseDto response = responseMapper.toDto(foundExchangeRate);
        return response;
    }

    @PostMapping(path = {"/exchangeRates"}, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ExchangeRateResponseDto create(@Valid ExchangeRateCreationDto exchangeRate) {
        String baseCode = exchangeRate.baseCurrencyCode();
        String targetCode = exchangeRate.targetCurrencyCode();
        String rate = exchangeRate.rate();
        ExchangeRateResponse createdExchangeRate = exchangeRateService.save(baseCode, targetCode, rate);
        ExchangeRateResponseDto response = responseMapper.toDto(createdExchangeRate);
        return response;
    }

    @PatchMapping(path = {"/exchangeRate/{currencyPair}"}, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ExchangeRateResponseDto update(@PathVariable(value = "currencyPair") String pair,
                                          @RequestBody(required = false) MultiValueMap<String, String> requestParams) {
        if (requestParams == null) {
            throw new BadRequestException("The request body must not be empty");
        }
        String rate = requestParams.getFirst("rate");
        ExchangeRateResponse updatedExchangeRate = exchangeRateService.update(pair, rate);
        ExchangeRateResponseDto response = responseMapper.toDto(updatedExchangeRate);
        return response;
    }
}

