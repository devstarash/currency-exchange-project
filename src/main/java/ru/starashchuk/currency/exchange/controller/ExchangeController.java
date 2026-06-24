package ru.starashchuk.currency.exchange.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.starashchuk.currency.exchange.controller.exception.BadRequestException;
import ru.starashchuk.currency.exchange.dto.exchange.ExchangeCreationDTO;
import ru.starashchuk.currency.exchange.dto.exchange.ExchangeResponseDTO;
import ru.starashchuk.currency.exchange.mapping.exchange.ExchangeResponseMapper;
import ru.starashchuk.currency.exchange.model.Exchange;
import ru.starashchuk.currency.exchange.service.ExchangeService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExchangeController {
    private final ExchangeService exchangeService;
    private final ExchangeResponseMapper responseMapper;

    @GetMapping(path = "/exchange")
    public ResponseEntity<ExchangeResponseDTO> findExchangeRate(@Valid ExchangeCreationDTO creation,
                                                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String exceptionMessage = constructExceptionMessage(bindingResult);
            throw new BadRequestException(exceptionMessage);
        }
        Exchange foundExchange = exchangeService.exchange(creation.getFrom(), creation.getTo(), creation.getAmount());
        ExchangeResponseDTO response = responseMapper.toDTO(foundExchange);
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
