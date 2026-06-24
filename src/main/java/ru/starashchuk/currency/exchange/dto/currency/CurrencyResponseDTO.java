package ru.starashchuk.currency.exchange.dto.currency;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonPropertyOrder({"id", "name", "code", "sign"})
public class CurrencyResponseDTO {
    private long id;
    private String name;
    private String code;
    private String sign;
}
