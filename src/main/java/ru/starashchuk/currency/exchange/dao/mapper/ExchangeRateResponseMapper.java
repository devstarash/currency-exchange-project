package ru.starashchuk.currency.exchange.dao.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.starashchuk.currency.exchange.model.Currency;
import ru.starashchuk.currency.exchange.model.ExchangeRateResponse;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExchangeRateResponseMapper implements RowMapper<ExchangeRateResponse> {
    @Override
    public ExchangeRateResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        Currency baseCurrency = convertToBaseCurrency(rs);
        Currency targetCurrency = convertToTargetCurrency(rs);
        long id = rs.getLong("rate_id");
        BigDecimal rate = rs.getBigDecimal("exchange_rate");
        ExchangeRateResponse response = new ExchangeRateResponse(id, baseCurrency, targetCurrency, rate);
        return response;
    }

    private Currency convertToBaseCurrency(ResultSet result) throws SQLException {
        long id = result.getLong("base_currency_id");
        String currencyName = result.getString("base_currency_name");
        String currencyCode = result.getString("base_currency_code");
        String currencySign = result.getString("base_currency_sign");
        Currency currency = new Currency(id, currencyCode, currencyName, currencySign);
        return currency;
    }

    private Currency convertToTargetCurrency(ResultSet result) throws SQLException {
        long id = result.getLong("target_currency_id");
        String currencyName = result.getString("target_currency_name");
        String currencyCode = result.getString("target_currency_code");
        String currencySign = result.getString("target_currency_sign");
        Currency currency = new Currency(id, currencyCode, currencyName, currencySign);
        return currency;
    }
}
