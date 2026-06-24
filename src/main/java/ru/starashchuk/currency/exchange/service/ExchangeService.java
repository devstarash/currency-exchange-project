package ru.starashchuk.currency.exchange.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.starashchuk.currency.exchange.controller.exception.BadRequestException;
import ru.starashchuk.currency.exchange.controller.exception.NotFoundException;
import ru.starashchuk.currency.exchange.dao.CurrencyDAO;
import ru.starashchuk.currency.exchange.dao.ExchangeRateDAO;
import ru.starashchuk.currency.exchange.model.Currency;
import ru.starashchuk.currency.exchange.model.Exchange;
import ru.starashchuk.currency.exchange.model.ExchangeRateResponse;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExchangeService {
    private final ExchangeRateDAO exchangeRateDAO;
    private final CurrencyDAO currencyDAO;
    private static final int ROUNDING_TO_THE_NUMBER = 2;
    private static final int ROUNDING_METHOD = BigDecimal.ROUND_HALF_UP;
    private static final int ROUNDING_FOR_DIVIDE = 4;
    private static final String USD_CODE = "USD";
    private static final String CURRENCY_NOT_FOUND_TEMPLATE = "Currency with code %s not found";
    private static final String RATE_NOT_FOUND_TEMPLATE = "There is no exchange rate for the currency pair %s-%s";

    public Exchange exchange(String baseCode, String targetCode, String amountToExchange) {
        BigDecimal amount = convertAmountToBigDecimal(amountToExchange)
                .orElseThrow(() -> new BadRequestException("The amount must be a number"));
        if (BigDecimal.ZERO.compareTo(amount) == 1) {
            throw new BadRequestException("Amount must be greater than zero");
        }
        String formattedBaseCode = baseCode.toUpperCase();
        String formattedTargetCode = targetCode.toUpperCase();
        if (!isCurrencyExist(formattedBaseCode)) {
            String exceptionMessage = String.format(CURRENCY_NOT_FOUND_TEMPLATE, formattedBaseCode);
            throw new NotFoundException(exceptionMessage);
        }
        if (!isCurrencyExist(formattedTargetCode)) {
            String exceptionMessage = String.format(CURRENCY_NOT_FOUND_TEMPLATE, formattedTargetCode);
            throw new NotFoundException(exceptionMessage);
        }
        Optional<Exchange> directExchange = calculateDirectExchangeRate(formattedBaseCode, formattedTargetCode, amount);
        if (directExchange.isPresent()) {
            return directExchange.get();
        }
        Optional<Exchange> reverseExchange = calculateReverseExchangeRate(formattedBaseCode, formattedTargetCode, amount);
        if (reverseExchange.isPresent()) {
            return reverseExchange.get();
        }
        Optional<Exchange> bypassExchange = calculateBypassExchangeRate(formattedBaseCode, formattedTargetCode, amount);
        if (bypassExchange.isPresent()) {
            return bypassExchange.get();
        }
        String exceptionMessage = String.format(RATE_NOT_FOUND_TEMPLATE, formattedBaseCode, formattedTargetCode);
        throw new NotFoundException(exceptionMessage);
    }

    private Optional<Exchange> calculateDirectExchangeRate(String baseCode,
                                                           String targetCode,
                                                           BigDecimal amount) {
        Optional<ExchangeRateResponse> directRateOptional =
                exchangeRateDAO.findExchangeRateByCurrencyPair(baseCode, targetCode);
        Exchange exchange = null;
        if (directRateOptional.isPresent()) {
            ExchangeRateResponse directRate = directRateOptional.get();
            BigDecimal rate = directRate.getRate();
            BigDecimal totalAmount = amount.multiply(rate).setScale(ROUNDING_TO_THE_NUMBER, ROUNDING_METHOD);
            rate = rate.setScale(ROUNDING_TO_THE_NUMBER, ROUNDING_METHOD);
            Currency baseCurrency = directRate.getBaseCurrency();
            Currency targetCurrency = directRate.getTargetCurrency();
            exchange = new Exchange(baseCurrency, targetCurrency, rate, amount, totalAmount);
        }
        return Optional.ofNullable(exchange);
    }

    private Optional<Exchange> calculateReverseExchangeRate(String baseCode, String targetCode, BigDecimal amount) {
        Optional<ExchangeRateResponse> reverseRateOptional =
                exchangeRateDAO.findExchangeRateByCurrencyPair(targetCode, baseCode);
        Exchange exchange = null;
        if (reverseRateOptional.isPresent()) {
            ExchangeRateResponse reverseRate = reverseRateOptional.get();
            BigDecimal rate = BigDecimal.ONE.divide(reverseRate.getRate(), ROUNDING_FOR_DIVIDE, ROUNDING_METHOD);
            BigDecimal totalAmount = amount.multiply(rate).setScale(ROUNDING_TO_THE_NUMBER, ROUNDING_METHOD);
            rate = rate.setScale(ROUNDING_TO_THE_NUMBER, ROUNDING_METHOD);
            Currency baseCurrency = reverseRate.getTargetCurrency();
            Currency targetCurrency = reverseRate.getBaseCurrency();
            exchange = new Exchange(baseCurrency, targetCurrency, rate, amount, totalAmount);
        }
        return Optional.ofNullable(exchange);
    }

    private Optional<Exchange> calculateBypassExchangeRate(String baseCode, String targetCode, BigDecimal amount) {
        Optional<ExchangeRateResponse> USDToBaseRateOptional =
                exchangeRateDAO.findExchangeRateByCurrencyPair(USD_CODE, baseCode);
        Optional<ExchangeRateResponse> USDToTargetRateOptional =
                exchangeRateDAO.findExchangeRateByCurrencyPair(USD_CODE, targetCode);
        boolean isRateExist = (USDToTargetRateOptional.isPresent() && USDToBaseRateOptional.isPresent());
        Exchange exchange = null;
        if (isRateExist) {
            ExchangeRateResponse USDToBaseExchangeRate = USDToBaseRateOptional.get();
            ExchangeRateResponse USDToTargetExchangeRate = USDToTargetRateOptional.get();
            BigDecimal rate = USDToTargetExchangeRate.getRate()
                    .divide(USDToBaseExchangeRate.getRate(), ROUNDING_FOR_DIVIDE, ROUNDING_METHOD);
            BigDecimal totalAmount = amount.multiply(rate).setScale(ROUNDING_TO_THE_NUMBER, ROUNDING_METHOD);
            rate = rate.setScale(ROUNDING_TO_THE_NUMBER, ROUNDING_METHOD);
            Currency baseCurrency = USDToBaseExchangeRate.getTargetCurrency();
            Currency targetCurrency = USDToTargetExchangeRate.getTargetCurrency();
            exchange = new Exchange(baseCurrency, targetCurrency, rate, amount, totalAmount);
        }
        return Optional.ofNullable(exchange);
    }

    private boolean isCurrencyExist(String currencyCode) {
        boolean isCurrencyExist = currencyDAO.findCurrencyByCode(currencyCode).isPresent();
        return isCurrencyExist;
    }

    private Optional<BigDecimal> convertAmountToBigDecimal(String amountToCheck) {
        BigDecimal amount = null;
        try {
            amount = new BigDecimal(amountToCheck);
        } catch (NumberFormatException e) {
        }
        return Optional.ofNullable(amount);
    }
}
