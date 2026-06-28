package ru.starashchuk.currency.exchange.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.starashchuk.currency.exchange.controller.exception.DatabaseException;
import ru.starashchuk.currency.exchange.dao.mapper.CurrencyMapper;
import ru.starashchuk.currency.exchange.model.Currency;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CurrencyDao {
    private final JdbcTemplate jdbcTemplate;

    private String findAllRequest = "SELECT c.id, c.code, c.full_name, c.sign FROM Currencies c";
    private String findByCodeRequest = "SELECT c.id, c.code, c.full_name, c.sign FROM Currencies c WHERE c.code = ?";
    private String saveCurrencyRequest = "INSERT INTO Currencies(code, full_name, sign) VALUES (?, ?, ?) RETURNING id;";

    public List<Currency> findAllCurrencies() {
        List<Currency> currencies = jdbcTemplate.query(findAllRequest, new CurrencyMapper());
        return currencies;
    }

    public Optional<Currency> findCurrencyByCode(String code) {
        Optional<Currency> currency = jdbcTemplate.query(findByCodeRequest, new Object[]{code}, new CurrencyMapper())
                .stream().findFirst();
        return currency;
    }

    public boolean isCurrencyAlreadyExist(String code) {
        boolean isCurrencyExist = findCurrencyByCode(code).isPresent();
        return isCurrencyExist;
    }

    public Currency save(Currency currency) {
        try {
            String fullName = currency.getFullName();
            String code = currency.getCode();
            String sign = currency.getSign();
            long id = jdbcTemplate.queryForObject(saveCurrencyRequest, Long.class, fullName, code, sign);
            currency.setId(id);
            return currency;
        } catch (Exception e) {
            throw new DatabaseException("Server error");
        }
    }
}
