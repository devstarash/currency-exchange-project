package ru.starashchuk.currency.exchange.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.starashchuk.currency.exchange.util.DatabaseSource;
import ru.starashchuk.currency.exchange.controller.exception.DatabaseException;
import ru.starashchuk.currency.exchange.model.Currency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CurrencyDAO {
    private final DatabaseSource databaseSource;
    private static final String FIND_ALL_CURRENCIES_REQUEST = "SELECT c.id, c.code, c.full_name, c.sign FROM currencies c;";
    private static final String FIND_CURRENCY_BY_CODE_REQUEST = """
            SELECT c.id, c.code, c.full_name, c.sign FROM currencies c WHERE c.code = ?;
            """;
    private static final String SAVE_CURRENCY_REQUEST = """
            INSERT INTO currencies(code, full_name, sign) VALUES (?, ?, ?);
            """;
    private static final String DATABASE_EXCEPTION_MESSAGE = "Error on the database side";

    public List<Currency> findAllCurrencies() {
        try (Connection connection = databaseSource.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_ALL_CURRENCIES_REQUEST)) {
            ResultSet result = ps.executeQuery();
            List<Currency> currencies = new ArrayList<>();
            while (result.next()) {
                Currency currency = convertToCurrency(result);
                currencies.add(currency);
            }
            return currencies;
        } catch (SQLException e) {
            throw new DatabaseException(DATABASE_EXCEPTION_MESSAGE);
        }
    }

    public Optional<Currency> findCurrencyByCode(String code) {
        try (Connection connection = databaseSource.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_CURRENCY_BY_CODE_REQUEST)) {
            ps.setString(1, code);
            ResultSet result = ps.executeQuery();
            Currency currency = null;
            if (result.next()) {
                currency = convertToCurrency(result);
            }
            return Optional.ofNullable(currency);
        } catch (SQLException e) {
            throw new DatabaseException(DATABASE_EXCEPTION_MESSAGE);
        }
    }

    public boolean isCurrencyAlreadyExist(String code) {
        boolean isCurrencyExist = findCurrencyByCode(code).isPresent();
        return isCurrencyExist;
    }

    public Currency save(Currency currency) {
        try (Connection connection = databaseSource.getDbConnection();
             PreparedStatement ps = connection.prepareStatement(SAVE_CURRENCY_REQUEST, Statement.RETURN_GENERATED_KEYS)) {
            String fullName = currency.getFullName();
            String code = currency.getCode();
            String sign = currency.getSign();
            ps.setString(1, code);
            ps.setString(2, fullName);
            ps.setString(3, sign);
            ps.executeUpdate();
            ResultSet result = ps.getGeneratedKeys();
            result.next();
            long currencyGeneratedId = result.getInt(1);
            currency.setId(currencyGeneratedId);
            return currency;
        } catch (SQLException e) {
            throw new DatabaseException(DATABASE_EXCEPTION_MESSAGE);
        }
    }

    private Currency convertToCurrency(ResultSet result) throws SQLException {
        long id = result.getLong("id");
        String code = result.getString("code");
        String fullName = result.getString("full_name");
        String sign = result.getString("sign");
        Currency currency = new Currency(id, code, fullName, sign);
        return currency;
    }
}
