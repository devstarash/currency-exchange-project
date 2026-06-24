package ru.starashchuk.currency.exchange.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.starashchuk.currency.exchange.controller.exception.BadRequestException;
import ru.starashchuk.currency.exchange.dto.currency.CurrencyCreationDTO;
import ru.starashchuk.currency.exchange.mapping.currency.CurrencyResponseMapper;
import ru.starashchuk.currency.exchange.dto.currency.CurrencyResponseDTO;
import ru.starashchuk.currency.exchange.model.Currency;
import ru.starashchuk.currency.exchange.service.CurrencyService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CurrencyController {
    private final CurrencyService currencyService;
    private final CurrencyResponseMapper responseMapper;

    @GetMapping(path = {"/currencies"})
    public ResponseEntity<List<CurrencyResponseDTO>> findAllCurrencies() {
        List<CurrencyResponseDTO> currencies = currencyService.findAllCurrencies().stream()
                .map(currency -> responseMapper.toDTO(currency)).toList();
        return ResponseEntity.ok(currencies);
    }

    @GetMapping(path = {"currency/", "currency/{code}"})
    public ResponseEntity<CurrencyResponseDTO> findCurrencyByCode(
            @PathVariable(value = "code", required = false) String code) {
        Currency foundCurrency = currencyService.findCurrencyByCode(code);
        CurrencyResponseDTO response = responseMapper.toDTO(foundCurrency);
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = {"/currencies"}, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<CurrencyResponseDTO> save(@Valid CurrencyCreationDTO currency, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String exceptionMessage = constructExceptionMessage(bindingResult);
            throw new BadRequestException(exceptionMessage);
        }
        Currency savedCurrency = currencyService.save(currency.getName(), currency.getCode(), currency.getSign());
        CurrencyResponseDTO response = responseMapper.toDTO(savedCurrency);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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