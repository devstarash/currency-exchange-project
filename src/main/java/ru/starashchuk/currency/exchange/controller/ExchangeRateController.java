package ru.starashchuk.currency.exchange.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.starashchuk.currency.exchange.controller.exception.BadRequestException;
import ru.starashchuk.currency.exchange.dto.exchangeRate.ExchangeRateCreationDTO;
import ru.starashchuk.currency.exchange.mapping.exchangeRate.ExchangeRateResponseMapper;
import ru.starashchuk.currency.exchange.dto.exchangeRate.ExchangeRateResponseDTO;
import ru.starashchuk.currency.exchange.model.ExchangeRateResponse;
import ru.starashchuk.currency.exchange.service.ExchangeRateService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExchangeRateController {
    private final ExchangeRateService exchangeRateService;
    private final ExchangeRateResponseMapper responseMapper;

    @GetMapping(path = {"/exchangeRates"})
    public ResponseEntity<List<ExchangeRateResponseDTO>> findAllExchangeRates() {
        List<ExchangeRateResponseDTO> exchangeRates = exchangeRateService.findAllExchangeRates().stream()
                .map(exchangeRate -> responseMapper.toDTO(exchangeRate)).toList();
        return ResponseEntity.ok(exchangeRates);
    }

    @GetMapping(path = {"/exchangeRate/", "/exchangeRate/{currencyPair}"})
    public ResponseEntity<ExchangeRateResponseDTO> findExchangeRateByCurrencyPair(
            @PathVariable(value = "currencyPair", required = false) String currencyPair) {
        ExchangeRateResponse foundExchangeRate = exchangeRateService.findExchangeRateByCurrencyPair(currencyPair);
        ExchangeRateResponseDTO response = responseMapper.toDTO(foundExchangeRate);
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = {"/exchangeRates"}, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ExchangeRateResponseDTO> create(@Valid ExchangeRateCreationDTO exchangeRate,
                                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String exceptionMessage = constructExceptionMessage(bindingResult);
            throw new BadRequestException(exceptionMessage);
        }
        String baseCode = exchangeRate.getBaseCurrencyCode();
        String targetCode = exchangeRate.getTargetCurrencyCode();
        String rate = exchangeRate.getRate();
        ExchangeRateResponse createdExchangeRate = exchangeRateService.save(baseCode, targetCode, rate);
        ExchangeRateResponseDTO response = responseMapper.toDTO(createdExchangeRate);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(path = {"/exchangeRate/{currencyPair}"}, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ExchangeRateResponseDTO> update(@PathVariable(value = "currencyPair") String pair,
                                                          @RequestBody(required = false)
                                                          MultiValueMap<String, String> requestParams) {
        if (requestParams == null) {
            throw new BadRequestException("The request body must not be empty");
        }
        String rate = requestParams.getFirst("rate");
        ExchangeRateResponse updatedExchangeRate = exchangeRateService.update(pair, rate);
        ExchangeRateResponseDTO response = responseMapper.toDTO(updatedExchangeRate);
        return ResponseEntity.ok(response);
    }

    private String constructExceptionMessage(BindingResult result) {
        List<FieldError> errors = result.getFieldErrors();
        StringBuilder exceptionMessage = new StringBuilder();
        for (FieldError error : errors) {
            String message = error.getDefaultMessage() + "; ";
            exceptionMessage.append(message);
        }
        return exceptionMessage.toString();
    }
}

