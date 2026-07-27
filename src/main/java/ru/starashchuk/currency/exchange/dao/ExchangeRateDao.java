package ru.starashchuk.currency.exchange.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.starashchuk.currency.exchange.controller.exception.DatabaseException;
import ru.starashchuk.currency.exchange.dao.mapper.ExchangeRateResponseMapper;
import ru.starashchuk.currency.exchange.model.ExchangeRate;
import ru.starashchuk.currency.exchange.model.ExchangeRateResponse;
import ru.starashchuk.currency.exchange.model.ExchangeRateUpdate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ExchangeRateDao {
    private final JdbcTemplate jdbcTemplate;

    private String findAllExchangeRatesRequest = """
            SELECT er.id as rate_id, cb.id as base_currency_id, cb.code as base_currency_code, 
            cb.full_name as base_currency_name, cb.sign as base_currency_sign, ct.id as target_currency_id,
            ct.code as target_currency_code, ct.full_name as target_currency_name, 
            ct.sign as target_currency_sign, er.rate as exchange_rate FROM exchange_rates er
            JOIN currencies cb ON cb.id = er.base_currency_id
            JOIN currencies ct ON ct.id = er.target_currency_id;
            """;
    private String findExchangeRateByCurrencyPairRequest = """
            SELECT er.id as rate_id, cb.id as base_currency_id, cb.code as base_currency_code, 
            cb.full_name as base_currency_name, cb.sign as base_currency_sign, ct.id as target_currency_id,
            ct.code as target_currency_code, ct.full_name as target_currency_name, 
            ct.sign as target_currency_sign, er.rate as exchange_rate FROM exchange_rates er
            JOIN currencies cb ON cb.id = er.base_currency_id
            JOIN currencies ct ON ct.id = er.target_currency_id
            WHERE cb.code = ? AND ct.code = ?;
            """;
    private String saveExchangeRateRequest = """
            INSERT INTO exchange_rates(base_currency_id, target_currency_id, rate) VALUES (?, ?, ?) RETURNING id;
            """;
    private String updateExchangeRateRequest = """
            UPDATE exchange_rates er SET rate = ?
            WHERE er.base_currency_id = (SELECT id FROM currencies WHERE code = ?)
            AND er.target_currency_id = (SELECT id FROM currencies WHERE code = ?);
            """;

    public List<ExchangeRateResponse> findAllExchangeRates() {
        List<ExchangeRateResponse> exchangeRates =
                jdbcTemplate.query(findAllExchangeRatesRequest, new ExchangeRateResponseMapper());
        return exchangeRates;
    }

    public Optional<ExchangeRateResponse> findExchangeRateByCurrencyPair(String baseCode, String targetCode) {
        Optional<ExchangeRateResponse> exchangeRate = jdbcTemplate.query(findExchangeRateByCurrencyPairRequest,
                new Object[]{baseCode, targetCode}, new ExchangeRateResponseMapper()).stream().findFirst();
        return exchangeRate;
    }

    public boolean isExchangeRateAlreadyExist(String baseCode, String targetCode) {
        boolean isExistExchangeRateExist = findExchangeRateByCurrencyPair(baseCode, targetCode).isPresent();
        return isExistExchangeRateExist;
    }

    public ExchangeRate save(ExchangeRate exchangeRate) {
        try {
            long baseCurrencyId = exchangeRate.getBaseCurrencyId();
            long targetCurrencyId = exchangeRate.getTargetCurrencyId();
            BigDecimal rate = exchangeRate.getRate();
            long id = jdbcTemplate.queryForObject(saveExchangeRateRequest, Long.class, baseCurrencyId, targetCurrencyId, rate);
            exchangeRate.setId(id);
            return exchangeRate;
        } catch (Exception e) {
            throw new DatabaseException("Server error");
        }
    }

    public void updateExchangeRate(ExchangeRateUpdate rateToUpdate) {
        try {
            BigDecimal rate = rateToUpdate.getRate();
            String baseCurrencyCode = rateToUpdate.getBaseCurrencyCode();
            String targetCurrencyCode = rateToUpdate.getTargetCurrencyCode();
            jdbcTemplate.update(updateExchangeRateRequest, rate, baseCurrencyCode, targetCurrencyCode);
        } catch (Exception e) {
            throw new DatabaseException("Server error");
        }
    }
}
