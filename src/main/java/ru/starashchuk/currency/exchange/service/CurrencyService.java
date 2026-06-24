package ru.starashchuk.currency.exchange.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.starashchuk.currency.exchange.controller.exception.AlreadyExistException;
import ru.starashchuk.currency.exchange.controller.exception.NotFoundException;
import ru.starashchuk.currency.exchange.dao.CurrencyDAO;
import ru.starashchuk.currency.exchange.controller.exception.BadRequestException;
import ru.starashchuk.currency.exchange.model.Currency;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CurrencyService {
    private final CurrencyDAO currencyDAO;
    private static final String EMPTY_FIELD_EXCEPTION_MESSAGE = "The currency code is missing from the address";
    private final Locale currencyNameLanguage = Locale.ENGLISH;
    private static final String CURRENCY_NOT_FOUND_TEMPLATE = "Currency with code %s not found";
    private static final String CURRENCY_ALREADY_EXIST_TEMPLATE = "Currency with code %s already exist";
    private static final String CURRENCY_CODE_NOT_VALID_TEMPLATE = "Currency code %s is not valid under ISO 4217";
    private static final String NAME_NOT_EQUALS_TEMPLATE = """
            The name %s does not correspond to the currency with the code %s""";

    public List<Currency> findAllCurrencies() {
        List<Currency> currencies = currencyDAO.findAllCurrencies();
        return currencies;
    }

    public Currency findCurrencyByCode(String code) {
        if (!isCorrectFieldInput(code)) {
            throw new BadRequestException(EMPTY_FIELD_EXCEPTION_MESSAGE);
        }
        String formatedCode = code.toUpperCase();
        Currency foundCurrency = currencyDAO.findCurrencyByCode(formatedCode)
                .orElseThrow(() -> {
                    String exceptionMessage = String.format(CURRENCY_NOT_FOUND_TEMPLATE, formatedCode);
                    return new NotFoundException(exceptionMessage);
                });
        return foundCurrency;
    }

    public Currency save(String name, String code, String sign) {
        String formattedCode = code.toUpperCase();
        boolean isRealCode = isCodeCompliantISO(formattedCode);
        if (!isRealCode) {
            String exceptionMessage = String.format(CURRENCY_CODE_NOT_VALID_TEMPLATE, formattedCode);
            throw new BadRequestException(exceptionMessage);
        }
        String realCurrencyName = getISONameByCode(formattedCode);
        boolean isCorrectInputName = isFieldEquals(realCurrencyName, name);
        if (!isCorrectInputName) {
            String message = String.format(NAME_NOT_EQUALS_TEMPLATE, name, formattedCode);
            throw new BadRequestException(message);
        }
        boolean isCurrencyAlreadyExist = currencyDAO.isCurrencyAlreadyExist(formattedCode);
        if (isCurrencyAlreadyExist) {
            String exceptionMessage = String.format(CURRENCY_ALREADY_EXIST_TEMPLATE, formattedCode);
            throw new AlreadyExistException(exceptionMessage);
        }
        String formattedSign = sign.toUpperCase().replace(" ", "");
        Currency currency = new Currency(0, formattedCode, realCurrencyName, formattedSign);
        Currency savedCurrency = currencyDAO.save(currency);
        return savedCurrency;
    }

    private boolean isFieldEquals(String first, String second) {
        String formattedFirst = first.toLowerCase();
        formattedFirst = formattedFirst.replace(" ", "");
        String formattedSecond = second.toLowerCase();
        formattedSecond = formattedSecond.replace(" ", "");
        return formattedFirst.equals(formattedSecond);
    }

    public String getISONameByCode(String code) {
        java.util.Currency currency = java.util.Currency.getInstance(code);
        return currency.getDisplayName(currencyNameLanguage);
    }

    private boolean isCorrectFieldInput(String fieldToCheck) {
        boolean isCorrect = (fieldToCheck != null) && (!fieldToCheck.isBlank());
        return isCorrect;
    }

    private boolean isCodeCompliantISO(String code) {
        boolean isRealCurrency = false;
        try {
            java.util.Currency currency = java.util.Currency.getInstance(code);
            isRealCurrency = true;
        } catch (IllegalArgumentException ex) {
        }
        return isRealCurrency;
    }
}
