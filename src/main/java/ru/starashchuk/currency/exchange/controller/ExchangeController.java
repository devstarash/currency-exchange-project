package ru.starashchuk.currency.exchange.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.starashchuk.currency.exchange.dto.exchange.ExchangeCreationDto;
import ru.starashchuk.currency.exchange.dto.exchange.ExchangeResponseDto;
import ru.starashchuk.currency.exchange.mapping.exchange.ExchangeResponseMapper;
import ru.starashchuk.currency.exchange.model.Exchange;
import ru.starashchuk.currency.exchange.service.ExchangeService;

@RestController
@RequiredArgsConstructor
public class ExchangeController {
    private final ExchangeService exchangeService;
    private final ExchangeResponseMapper responseMapper;

    @GetMapping("/exchange")
    public ExchangeResponseDto findExchangeRate(@Valid ExchangeCreationDto creation) {
        Exchange foundExchange = exchangeService.exchange(creation.from(), creation.to(), creation.amount());
        ExchangeResponseDto response = responseMapper.toDto(foundExchange);
        return response;
    }
}
