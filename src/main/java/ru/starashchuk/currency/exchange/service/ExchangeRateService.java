package ru.starashchuk.currency.exchange.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.starashchuk.currency.exchange.controller.exception.*;
import ru.starashchuk.currency.exchange.dao.CurrencyDAO;
import ru.starashchuk.currency.exchange.dao.ExchangeRateDAO;
import ru.starashchuk.currency.exchange.model.Currency;
import ru.starashchuk.currency.exchange.model.ExchangeRate;
import ru.starashchuk.currency.exchange.model.ExchangeRateCreation;
import ru.starashchuk.currency.exchange.model.ExchangeRateResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {
    private final ExchangeRateDAO exchangeRateDAO;
    private final CurrencyDAO currencyDAO;
    private static final int CURRENCY_CODE_LENGTH = 3;
    private static final String CURRENCY_NOT_FOUND_TEMPLATE = "Currency with code %s not found";
    private static final String RATE_NOT_FOUND_TEMPLATE = "The exchange rate for the %s-%s pair not found";
    private static final String RATE_AlREADY_EXIST_TEMPLATE = "The exchange rate for the %s-%s pair already exist";
    private static final String CURRENCY_PAIR_FILED_EMPTY_EXCEPTION_MESSAGE = """
            The currency pair is missing from the address""";

    public List<ExchangeRateResponse> findAllExchangeRates() {
        List<ExchangeRateResponse> exchangeRates = exchangeRateDAO.findAllExchangeRates();
        return exchangeRates;
    }

    public ExchangeRateResponse findExchangeRateByCurrencyPair(String pair) {
        if (!isCurrencyPair(pair)) {
            throw new BadRequestException(CURRENCY_PAIR_FILED_EMPTY_EXCEPTION_MESSAGE);
        }
        String baseCurrencyCode = pair.substring(0, CURRENCY_CODE_LENGTH).toUpperCase();
        String targetCurrencyCode = pair.substring(CURRENCY_CODE_LENGTH).toUpperCase();
        ExchangeRateResponse foundExchangeRate =
                exchangeRateDAO.findExchangeRateByCurrencyPair(baseCurrencyCode, targetCurrencyCode)
                        .orElseThrow(() -> {
                            String message = String.format(RATE_NOT_FOUND_TEMPLATE, baseCurrencyCode, targetCurrencyCode);
                            return new NotFoundException(message);
                        });
        return foundExchangeRate;
    }

    public ExchangeRateResponse save(String baseCode, String targetCode, String rate) {
        String baseFormatedCode = baseCode.toUpperCase();
        String targetFormatedCode = targetCode.toUpperCase();
        BigDecimal rateToExchange = convertRateToBigDecimal(rate)
                .orElseThrow(() -> new BadRequestException("Rate must be a number"));
        if (BigDecimal.ZERO.compareTo(rateToExchange) != -1) {
            throw new BadRequestException("Rate must be greater than zero");
        }
        Currency baseCurrency = currencyDAO.findCurrencyByCode(baseFormatedCode)
                .orElseThrow(() -> {
                    String exceptionMessage = String.format(CURRENCY_NOT_FOUND_TEMPLATE, baseFormatedCode);
                    return new NotFoundException(exceptionMessage);
                });
        Currency targetCurrency = currencyDAO.findCurrencyByCode(targetFormatedCode)
                .orElseThrow(() -> {
                    String exceptionMessage = String.format(CURRENCY_NOT_FOUND_TEMPLATE, targetFormatedCode);
                    return new NotFoundException(exceptionMessage);
                });
        boolean isExchangeRateExist = exchangeRateDAO.isExchangeRateAlreadyExist(baseFormatedCode, targetFormatedCode);
        if (isExchangeRateExist) {
            String exceptionMessage = String.format(RATE_AlREADY_EXIST_TEMPLATE, baseFormatedCode, targetFormatedCode);
            throw new AlreadyExistException(exceptionMessage);
        }
        ExchangeRate exchangeRate = new ExchangeRate(0, baseCurrency.getId(), targetCurrency.getId(), rateToExchange);
        ExchangeRate savedExchangeRate = exchangeRateDAO.save(exchangeRate);
        long savedId = savedExchangeRate.getId();
        BigDecimal savedRate = savedExchangeRate.getRate();
        ExchangeRateResponse response = new ExchangeRateResponse(savedId, baseCurrency, targetCurrency, savedRate);
        return response;
    }

    public ExchangeRateResponse update(String currencyPair, String rate) {
        if (!isCurrencyPair(currencyPair)) {
            throw new BadRequestException(CURRENCY_PAIR_FILED_EMPTY_EXCEPTION_MESSAGE);
        }
        if (!isCorrectInputField(rate)) {
            throw new BadRequestException("The field rate must not be empty");
        }
        BigDecimal exchangeRate = convertRateToBigDecimal(rate)
                .orElseThrow(() -> new BadRequestException("Rate must be a number"));
        if (BigDecimal.ZERO.compareTo(exchangeRate) != -1) {
            throw new BadRequestException("Rate must be greater than zero");
        }
        String baseCode = currencyPair.substring(0, CURRENCY_CODE_LENGTH).toUpperCase();
        String targetCode = currencyPair.substring(CURRENCY_CODE_LENGTH).toUpperCase();
        boolean isExchangeRateExist = exchangeRateDAO.isExchangeRateAlreadyExist(baseCode, targetCode);
        if (!isExchangeRateExist) {
            String exceptionMessage = String.format(RATE_NOT_FOUND_TEMPLATE, baseCode, targetCode);
            throw new NotFoundException(exceptionMessage);
        }
        ExchangeRateCreation exchangeRateToUpdate = new ExchangeRateCreation(baseCode, targetCode, exchangeRate);
        exchangeRateDAO.updateExchangeRate(exchangeRateToUpdate);
        ExchangeRateResponse updatedRate = exchangeRateDAO.findExchangeRateByCurrencyPair(baseCode, targetCode).get();
        return updatedRate;
    }

    private boolean isCurrencyPair(String pairToCheck) {
        boolean isCurrencyPair = (pairToCheck != null) && (pairToCheck.length() == CURRENCY_CODE_LENGTH * 2);
        return isCurrencyPair;
    }

    private boolean isCorrectInputField(String fieldToCheck) {
        boolean iCorrect = (fieldToCheck != null) && (!fieldToCheck.isBlank());
        return iCorrect;
    }

    private Optional<BigDecimal> convertRateToBigDecimal(String rateToCovert) {
        BigDecimal amount = null;
        try {
            amount = new BigDecimal(rateToCovert);
        } catch (NumberFormatException e) {
        }
        return Optional.ofNullable(amount);
    }
}

