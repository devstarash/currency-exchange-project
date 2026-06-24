package ru.starashchuk.currency.exchange.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.starashchuk.currency.exchange.controller.exception.DatabaseException;
import ru.starashchuk.currency.exchange.util.DatabaseSource;
import ru.starashchuk.currency.exchange.model.Currency;
import ru.starashchuk.currency.exchange.model.ExchangeRate;
import ru.starashchuk.currency.exchange.model.ExchangeRateCreation;
import ru.starashchuk.currency.exchange.model.ExchangeRateResponse;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ExchangeRateDAO {
    private final DatabaseSource databaseSource;

    private static final String FIND_ALL_EXCHANGE_RATES_REQUEST = """
            SELECT er.id as rate_id, cb.id as base_currency_id, cb.code as base_currency_code, 
            cb.full_name as base_currency_name, cb.sign as base_currency_sign, ct.id as target_currency_id,
            ct.code as target_currency_code, ct.full_name as target_currency_name, 
            ct.sign as target_currency_sign, er.rate as exchange_rate FROM exchange_rates er
            JOIN currencies cb ON cb.id = er.base_currency_id
            JOIN currencies ct ON ct.id = er.target_currency_id;
            """;
    private static final String FIND_EXCHANGE_RATE_BY_CURRENCY_PAIR_REQUEST = """
            SELECT er.id as rate_id, cb.id as base_currency_id, cb.code as base_currency_code, 
            cb.full_name as base_currency_name, cb.sign as base_currency_sign, ct.id as target_currency_id,
            ct.code as target_currency_code, ct.full_name as target_currency_name, 
            ct.sign as target_currency_sign, er.rate as exchange_rate FROM exchange_rates er
            JOIN currencies cb ON cb.id = er.base_currency_id
            JOIN currencies ct ON ct.id = er.target_currency_id
            WHERE cb.code = ? AND ct.code = ?;
            """;
    private static final String SAVE_EXCHANGE_RATE_REQUEST = """
            INSERT INTO exchange_rates(base_currency_id, target_currency_id, rate) VALUES (?, ?, ?);
            """;
    private static final String UPDATE_EXCHANGE_RATE_REQUEST = """
            UPDATE exchange_rates er SET rate = ?
            WHERE er.base_currency_id = (SELECT id FROM currencies WHERE code = ?)
            AND er.target_currency_id = (SELECT id FROM currencies WHERE code = ?);
            """;
    private static final String DATABASE_EXCEPTION_MESSAGE = "Error on the database side";

    public List<ExchangeRateResponse> findAllExchangeRates() {
        try (Connection connection = databaseSource.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_ALL_EXCHANGE_RATES_REQUEST)) {
            ResultSet result = ps.executeQuery();
            List<ExchangeRateResponse> exchangeRates = new ArrayList<>();
            while (result.next()) {
                ExchangeRateResponse exchangeRate = enrichBaseFieldsExchangeRate(result);
                Currency baseCurrency = convertToBaseCurrency(result);
                Currency targetCurrency = convertToTargetCurrency(result);
                exchangeRate.setBaseCurrency(baseCurrency);
                exchangeRate.setTargetCurrency(targetCurrency);
                exchangeRates.add(exchangeRate);
            }
            return exchangeRates;
        } catch (SQLException e) {
            throw new DatabaseException(DATABASE_EXCEPTION_MESSAGE);
        }
    }

    public Optional<ExchangeRateResponse> findExchangeRateByCurrencyPair(String baseCode, String targetCode) {
        try (Connection connection = databaseSource.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_EXCHANGE_RATE_BY_CURRENCY_PAIR_REQUEST)) {
            ps.setString(1, baseCode);
            ps.setString(2, targetCode);
            ResultSet result = ps.executeQuery();
            ExchangeRateResponse exchangeRate = null;
            if (result.next()) {
                exchangeRate = enrichBaseFieldsExchangeRate(result);
                Currency baseCurrency = convertToBaseCurrency(result);
                Currency targetCurrency = convertToTargetCurrency(result);
                exchangeRate.setBaseCurrency(baseCurrency);
                exchangeRate.setTargetCurrency(targetCurrency);
            }
            return Optional.ofNullable(exchangeRate);
        } catch (SQLException e) {
            throw new DatabaseException(DATABASE_EXCEPTION_MESSAGE);
        }
    }

    public boolean isExchangeRateAlreadyExist(String baseCode, String targetCode) {
        boolean isExistExchangeRateExist = findExchangeRateByCurrencyPair(baseCode, targetCode).isPresent();
        return isExistExchangeRateExist;
    }

    public ExchangeRate save(ExchangeRate exchangeRate) {
        try (Connection connection = databaseSource.getDbConnection();
             PreparedStatement ps =
                     connection.prepareStatement(SAVE_EXCHANGE_RATE_REQUEST, Statement.RETURN_GENERATED_KEYS)) {
            long baseId = exchangeRate.getBaseCurrencyId();
            long targetId = exchangeRate.getTargetCurrencyId();
            BigDecimal rate = exchangeRate.getRate();
            ps.setLong(1, baseId);
            ps.setLong(2, targetId);
            ps.setBigDecimal(3, rate);
            ps.executeUpdate();
            ResultSet result = ps.getGeneratedKeys();
            result.next();
            long generatedRateId = result.getLong(1);
            exchangeRate.setId(generatedRateId);
            return exchangeRate;
        } catch (SQLException e) {
            throw new DatabaseException(DATABASE_EXCEPTION_MESSAGE);
        }
    }

    public void updateExchangeRate(ExchangeRateCreation rateToUpdate) {
        try (Connection connection = databaseSource.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_EXCHANGE_RATE_REQUEST)) {
            BigDecimal rate = rateToUpdate.getRate();
            String baseCode = rateToUpdate.getBaseCurrencyCode();
            String targetCode = rateToUpdate.getTargetCurrencyCode();
            ps.setBigDecimal(1, rate);
            ps.setString(2, baseCode);
            ps.setString(3, targetCode);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    private ExchangeRateResponse enrichBaseFieldsExchangeRate(ResultSet result) throws SQLException {
        ExchangeRateResponse exchangeRate = new ExchangeRateResponse();
        exchangeRate.setId(result.getLong("rate_id"));
        exchangeRate.setRate(result.getBigDecimal("exchange_rate"));
        return exchangeRate;
    }
}
