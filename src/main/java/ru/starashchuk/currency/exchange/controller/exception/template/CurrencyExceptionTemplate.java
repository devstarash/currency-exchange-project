package ru.starashchuk.currency.exchange.controller.exception.template;

public abstract class CurrencyExceptionTemplate {
    public static final String CURRENCY_NOT_FOUND = "Currency with code %s not found";
    public static final String CURRENCY_ALREADY_EXIST = "Currency with code %s already exist";
    public static final String CURRENCY_CODE_NOT_VALID = "Currency code %s is not valid under ISO 4217";
    public static final String INVALID_NAME = "The name %s does not correspond to the currency with the code %s";
    public static final String CURRENCY_FIELD_EMPTY = "The currency code is missing from the address";
}
