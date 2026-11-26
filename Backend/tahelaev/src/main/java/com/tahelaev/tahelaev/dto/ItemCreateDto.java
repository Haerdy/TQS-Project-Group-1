package com.tahelaev.tahelaev.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCreateDto {

    @NotBlank(message = "Item name is required")
    private String name;

    @Positive(message = "Item value must be positive")
    private float value;

    private float iva;

    @Positive(message = "Item weight must be positive")
    private float weight;

    private boolean isTempSensitive;
    private Float temp;
}
