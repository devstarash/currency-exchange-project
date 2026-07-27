package ru.starashchuk.currency.exchange.controller.exception.template;

public abstract class ExchangeRateExceptionTemplate {
    public static final String RATE_NOT_FOUND = "The exchange rate for the %s-%s pair not found";
    public static final String RATE_AlREADY_EXIST = "The exchange rate for the %s-%s pair already exist";
    public static final String CURRENCY_PAIR_FIELD_EMPTY = "The currency pair is missing from the address";
}
