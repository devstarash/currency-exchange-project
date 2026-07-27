package ru.starashchuk.currency.exchange.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.starashchuk.currency.exchange.controller.exception.*;
import ru.starashchuk.currency.exchange.controller.exception.template.ExchangeRateExceptionTemplate;
import ru.starashchuk.currency.exchange.dao.ExchangeRateDao;
import ru.starashchuk.currency.exchange.model.*;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {
    private final ExchangeRateDao exchangeRateDao;
    private final CurrencyService currencyService;
    private static final int CURRENCY_CODE_LENGTH = 3;

    public List<ExchangeRateResponse> findAllExchangeRates() {
        List<ExchangeRateResponse> exchangeRates = exchangeRateDao.findAllExchangeRates();
        return exchangeRates;
    }

    public ExchangeRateResponse findExchangeRateByCurrencyPair(String pair) {
        if (!isLengthCorrespondsCurrencyPair(pair)) {
            throw new BadRequestException(ExchangeRateExceptionTemplate.CURRENCY_PAIR_FIELD_EMPTY);
        }
        String baseCurrencyCode = pair.substring(0, CURRENCY_CODE_LENGTH).toUpperCase();
        String targetCurrencyCode = pair.substring(CURRENCY_CODE_LENGTH).toUpperCase();
        try {
            ExchangeRateResponse foundRate = findExchangeRateByCurrencyPair(baseCurrencyCode, targetCurrencyCode);
            return foundRate;
        } catch (NotFoundException ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    private ExchangeRateResponse findExchangeRateByCurrencyPair(String baseCurrencyCode, String targetCurrencyCode)
            throws NotFoundException {
        ExchangeRateResponse foundExchangeRate =
                exchangeRateDao.findExchangeRateByCurrencyPair(baseCurrencyCode, targetCurrencyCode).orElseThrow(() -> {
                    String exceptionMessage = String.format(ExchangeRateExceptionTemplate.RATE_NOT_FOUND,
                            baseCurrencyCode, targetCurrencyCode);
                    return new NotFoundException(exceptionMessage);
                });
        return foundExchangeRate;
    }

    public ExchangeRateResponse save(String baseCode, String targetCode, String rate) {
        BigDecimal formattedRate;
        try {
            formattedRate = convertAndCheckRate(rate);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        Currency baseCurrency;
        Currency targetCurrency;
        try {
            baseCurrency = currencyService.findCurrencyByCode(baseCode);
            targetCurrency = currencyService.findCurrencyByCode(targetCode);
        } catch (NotFoundException ex) {
            throw new NotFoundException(ex.getMessage());
        }
        try {
            ExchangeRate savedExchangeRate = save(baseCurrency, targetCurrency, formattedRate);
            ExchangeRateResponse response = createExchangeRateResponse(savedExchangeRate, baseCurrency, targetCurrency);
            return response;
        } catch (AlreadyExistException ex) {
            throw new AlreadyExistException(ex.getMessage());
        }
    }

    private ExchangeRate save(Currency baseCurrency, Currency targetCurrency, BigDecimal rate)
            throws AlreadyExistException {
        String baseCurrencyCode = baseCurrency.getCode();
        String targetCurrencyCode = targetCurrency.getCode();
        boolean isExchangeRateExist = exchangeRateDao.isExchangeRateAlreadyExist(baseCurrencyCode, targetCurrencyCode);
        if (isExchangeRateExist) {
            String exceptionMessage = String.format(ExchangeRateExceptionTemplate.RATE_AlREADY_EXIST,
                    baseCurrencyCode, targetCurrencyCode);
            throw new AlreadyExistException(exceptionMessage);
        }
        long baseId = baseCurrency.getId();
        long targetId = targetCurrency.getId();
        ExchangeRate exchangeRate = new ExchangeRate(null, baseId, targetId, rate);
        ExchangeRate savedExchangeRate = exchangeRateDao.save(exchangeRate);
        return savedExchangeRate;
    }

    private ExchangeRateResponse createExchangeRateResponse(ExchangeRate exchangeRate, Currency base, Currency target) {
        long rateId = exchangeRate.getId();
        BigDecimal rate = exchangeRate.getRate();
        ExchangeRateResponse response = new ExchangeRateResponse(rateId, base, target, rate);
        return response;
    }

    public ExchangeRateResponse update(String currencyPair, String rate) {
        if (!isLengthCorrespondsCurrencyPair(currencyPair)) {
            throw new BadRequestException(ExchangeRateExceptionTemplate.CURRENCY_PAIR_FIELD_EMPTY);
        }
        BigDecimal exchangeRate;
        try {
            exchangeRate = convertAndCheckRate(rate);
        } catch (BadRequestException ex) {
            throw new BadRequestException(ex.getMessage());
        }
        String baseCode = currencyPair.substring(0, CURRENCY_CODE_LENGTH).toUpperCase();
        String targetCode = currencyPair.substring(CURRENCY_CODE_LENGTH).toUpperCase();
        try {
            ExchangeRateResponse updatedRate = update(baseCode, targetCode, exchangeRate);
            return updatedRate;
        } catch (NotFoundException ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    private ExchangeRateResponse update(String baseCode, String targetCode, BigDecimal rate) throws NotFoundException {
        boolean isExchangeRateExist = exchangeRateDao.isExchangeRateAlreadyExist(baseCode, targetCode);
        if (!isExchangeRateExist) {
            String exceptionMessage = String.format(ExchangeRateExceptionTemplate.RATE_NOT_FOUND, baseCode, targetCode);
            throw new NotFoundException(exceptionMessage);
        }
        ExchangeRateUpdate exchangeRateToUpdate = new ExchangeRateUpdate(baseCode, targetCode, rate);
        exchangeRateDao.updateExchangeRate(exchangeRateToUpdate);
        ExchangeRateResponse updatedRate = exchangeRateDao.findExchangeRateByCurrencyPair(baseCode, targetCode).get();
        return updatedRate;
    }

    private boolean isLengthCorrespondsCurrencyPair(String pair) {
        boolean isNotNull = pair != null;
        boolean isCorrectPairLength = pair.length() == CURRENCY_CODE_LENGTH * 2;
        boolean isCurrencyPair = isNotNull && isCorrectPairLength;
        return isCurrencyPair;
    }

    private boolean isInputFieldNotEmpty(String field) {
        boolean isNotEmpty = (field != null) && (!field.isBlank());
        return isNotEmpty;
    }

    private BigDecimal convertAndCheckRate(String rateToCovert) throws BadRequestException {
        if (!isInputFieldNotEmpty(rateToCovert)) {
            throw new BadRequestException("The field exchange rate must not be empty");
        }
        BigDecimal rate;
        try {
            rate = new BigDecimal(rateToCovert);
        } catch (NumberFormatException e) {
            throw new BadRequestException("The exchange rate must be a number");
        }
        if (BigDecimal.ZERO.compareTo(rate) != -1) {
            throw new BadRequestException("The exchange rate must be greater than zero");
        }
        return rate;
    }
}

