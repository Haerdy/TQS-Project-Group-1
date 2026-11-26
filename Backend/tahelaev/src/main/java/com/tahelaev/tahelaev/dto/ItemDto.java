package com.tahelaev.tahelaev.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDto {
    private UUID id;
    private String name;
    private float value;
    private float iva;
    private float weight;
    private boolean isTempSensitive;
    private Float temp;
}
