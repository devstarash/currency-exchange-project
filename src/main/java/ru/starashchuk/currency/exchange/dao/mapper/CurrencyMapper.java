package ru.starashchuk.currency.exchange.dao.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.starashchuk.currency.exchange.model.Currency;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrencyMapper implements RowMapper<Currency> {
    @Override
    public Currency mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("id");
        String code = rs.getString("code");
        String fullName = rs.getString("full_name");
        String sign = rs.getString("sign");
        Currency currency = new Currency(id, code, fullName, sign);
        return currency;
    }
}
