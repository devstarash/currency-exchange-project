package ru.starashchuk.currency.exchange.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Currency {
    private Long id;
    private String code;
    private String fullName;
    private String sign;
}
