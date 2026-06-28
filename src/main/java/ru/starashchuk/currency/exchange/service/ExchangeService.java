package ru.starashchuk.currency.exchange.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.starashchuk.currency.exchange.controller.exception.BadRequestException;
import ru.starashchuk.currency.exchange.controller.exception.NotFoundException;
import ru.starashchuk.currency.exchange.controller.exception.template.ExchangeRateExceptionTemplate;
import ru.starashchuk.currency.exchange.dao.ExchangeRateDao;
import ru.starashchuk.currency.exchange.model.Currency;
import ru.starashchuk.currency.exchange.model.Exchange;
import ru.starashchuk.currency.exchange.model.ExchangeRateResponse;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExchangeService {
    private final ExchangeRateDao exchangeRateDao;
    private static final int ROUNDING_TO_THE_NUMBER = 2;
    private static final int ROUNDING_METHOD = BigDecimal.ROUND_HALF_UP;
    private static final int ROUNDING_FOR_DIVIDE = 4;
    private static final String USD_CODE = "USD";

    public Exchange exchange(String from, String to, String amountToExchange) {
        BigDecimal amount;
        try {
            amount = convertAndCheckAmount(amountToExchange);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
        String formattedBaseCode = from.toUpperCase();
        String formattedTargetCode = to.toUpperCase();
        Exchange exchange = exchange(formattedBaseCode, formattedTargetCode, amount).orElseThrow(() -> {
            String exceptionMessage = String.format(ExchangeRateExceptionTemplate.RATE_NOT_FOUND, formattedBaseCode,
                    formattedTargetCode);
            return new NotFoundException(exceptionMessage);
        });
        return exchange;
    }

    private Optional<Exchange> exchange(String baseCode, String targetCode, BigDecimal amount) {
        Optional<Exchange> directExchange = calculateDirectExchangeRate(baseCode, targetCode, amount);
        if (directExchange.isPresent()) {
            return directExchange;
        }
        Optional<Exchange> reverseExchange = calculateReverseExchangeRate(baseCode, targetCode, amount);
        if (reverseExchange.isPresent()) {
            return reverseExchange;
        }
        Optional<Exchange> bypassExchange = calculateBypassExchangeRate(baseCode, targetCode, amount);
        return bypassExchange;
    }

    private Optional<Exchange> calculateDirectExchangeRate(String baseCode, String targetCode, BigDecimal amount) {
        Optional<ExchangeRateResponse> directRateOptional =
                exchangeRateDao.findExchangeRateByCurrencyPair(baseCode, targetCode);
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
                exchangeRateDao.findExchangeRateByCurrencyPair(targetCode, baseCode);
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
                exchangeRateDao.findExchangeRateByCurrencyPair(USD_CODE, baseCode);
        Optional<ExchangeRateResponse> USDToTargetRateOptional =
                exchangeRateDao.findExchangeRateByCurrencyPair(USD_CODE, targetCode);
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

    private BigDecimal convertAndCheckAmount(String amountToConvert) throws BadRequestException {
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountToConvert);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Amount must be a number");
        }
        if (BigDecimal.ZERO.compareTo(amount) != -1) {
            throw new BadRequestException("Amount must be greater than zero");
        }
        return amount;
    }
}
