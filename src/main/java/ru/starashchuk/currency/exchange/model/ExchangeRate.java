package ru.starashchuk.currency.exchange.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {
    private Long id;
    private long baseCurrencyId;
    private long targetCurrencyId;
    private BigDecimal rate;
}
