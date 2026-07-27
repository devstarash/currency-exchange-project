package ru.starashchuk.currency.exchange.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.starashchuk.currency.exchange.controller.exception.AlreadyExistException;
import ru.starashchuk.currency.exchange.controller.exception.NotFoundException;
import ru.starashchuk.currency.exchange.controller.exception.template.CurrencyExceptionTemplate;
import ru.starashchuk.currency.exchange.dao.CurrencyDao;
import ru.starashchuk.currency.exchange.controller.exception.BadRequestException;
import ru.starashchuk.currency.exchange.model.Currency;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CurrencyService {
    private final CurrencyDao currencyDao;
    private final Locale currencyNameLanguage = Locale.ENGLISH;

    public List<Currency> findAllCurrencies() {
        List<Currency> currencies = currencyDao.findAllCurrencies();
        return currencies;
    }

    public Currency findCurrencyByCode(String code) throws NotFoundException {
        boolean isCodeFieldNotEmpty = isFieldNotEmpty(code);
        if (!isCodeFieldNotEmpty) {
            throw new BadRequestException(CurrencyExceptionTemplate.CURRENCY_FIELD_EMPTY);
        }
        String formattedCode = code.toUpperCase();
        try {
            Currency foundCurrency = findByCode(formattedCode);
            return foundCurrency;
        } catch (NotFoundException ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    private Currency findByCode(String code) throws NotFoundException {
        Currency foundCurrency = currencyDao.findCurrencyByCode(code)
                .orElseThrow(() -> {
                    String exceptionMessage = String.format(CurrencyExceptionTemplate.CURRENCY_NOT_FOUND, code);
                    return new NotFoundException(exceptionMessage);
                });
        return foundCurrency;
    }

    public Currency save(Currency currencyToSave) {
        Currency currency;
        try {
            currency = validateCurrencyBeforeSaving(currencyToSave);
        } catch (BadRequestException ex) {
            throw new BadRequestException(ex.getMessage());
        }
        try {
            Currency savedCurrency = saveCurrency(currency);
            return savedCurrency;
        } catch (AlreadyExistException ex) {
            throw new AlreadyExistException(ex.getMessage());
        }
    }

    private Currency saveCurrency(Currency currency) throws AlreadyExistException {
        String code = currency.getCode();
        boolean isCurrencyAlreadyExist = currencyDao.isCurrencyAlreadyExist(code);
        if (isCurrencyAlreadyExist) {
            String exceptionMessage = String.format(CurrencyExceptionTemplate.CURRENCY_ALREADY_EXIST, code);
            throw new AlreadyExistException(exceptionMessage);
        }
        Currency savedCurrency = currencyDao.save(currency);
        return savedCurrency;
    }

    private Currency validateCurrencyBeforeSaving(Currency currencyToValidate) throws BadRequestException {
        String sign = currencyToValidate.getSign();
        String name = currencyToValidate.getFullName();
        String formattedCode = currencyToValidate.getCode().toUpperCase();
        String formattedSign = removeSpaces(sign).toUpperCase();
        try {
            checkNameAndCodeCompliantIso(name, formattedCode);
        } catch (BadRequestException ex) {
            throw new BadRequestException(ex.getMessage());
        }
        String isoCurrencyName = getIsoNameByCode(formattedCode);
        Currency currency = new Currency(null, formattedCode, isoCurrencyName, formattedSign);
        return currency;
    }

    private void checkNameAndCodeCompliantIso(String name, String code) throws BadRequestException {
        try {
            checkInputCodeCompliantIso(code);
            checkInputNameCompliesCode(name, code);
        } catch (BadRequestException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    private void checkInputCodeCompliantIso(String code) throws BadRequestException {
        boolean isCodeCompliantIso = isCurrencyCodeCompliantIso(code);
        if (!isCodeCompliantIso) {
            String exceptionMessage = String.format(CurrencyExceptionTemplate.CURRENCY_CODE_NOT_VALID, code);
            throw new BadRequestException(exceptionMessage);
        }
    }

    private void checkInputNameCompliesCode(String name, String code) throws BadRequestException {
        boolean isNameCompliesCode = isNameCompliesCurrencyCode(name, code);
        if (!isNameCompliesCode) {
            String exceptionMessage = String.format(CurrencyExceptionTemplate.INVALID_NAME, name, code);
            throw new BadRequestException(exceptionMessage);
        }
    }

    public String getIsoNameByCode(String code) {
        java.util.Currency currency = java.util.Currency.getInstance(code);
        String isoCorrectName = currency.getDisplayName(currencyNameLanguage);
        return isoCorrectName;
    }

    private boolean isCurrencyCodeCompliantIso(String code) {
        List<String> availableCurrencyCodes = java.util.Currency.getAvailableCurrencies().stream()
                .map(java.util.Currency::getCurrencyCode).toList();
        boolean isCodeCompliantIso = availableCurrencyCodes.contains(code);
        return isCodeCompliantIso;
    }

    private boolean isNameCompliesCurrencyCode(String name, String code) {
        String realCurrencyName = getIsoNameByCode(code);
        boolean isNameCompliesCode = isFieldEquals(realCurrencyName, name);
        return isNameCompliesCode;
    }

    private boolean isFieldNotEmpty(String fieldToCheck) {
        boolean isNotEmpty = (fieldToCheck != null) && (!fieldToCheck.isBlank());
        return isNotEmpty;
    }

    private String removeSpaces(String field) {
        String formattedField = field.replace(" ", "");
        return formattedField;
    }

    private boolean isFieldEquals(String first, String second) {
        String formattedFirst = first.toLowerCase();
        formattedFirst = removeSpaces(formattedFirst);
        String formattedSecond = second.toLowerCase();
        formattedSecond = removeSpaces(formattedSecond);
        boolean isFieldEquals = formattedFirst.equals(formattedSecond);
        return isFieldEquals;
    }
}
