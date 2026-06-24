package ru.starashchuk.currency.exchange.dto.exchange;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeCreationDTO {
    @NotBlank(message = "The from field must not be empty")
    @Size(min = 3, max = 3, message = "The 'from' field must be 3 characters long")
    String from;
    @NotBlank(message = "The to field must not be empty")
    @Size(min = 3, max = 3, message = "The from field must be 3 characters long")
    String to;
    @NotBlank(message = "The amount field must not be empty")
    String amount;
}
