package io.github.jaeykweon.jpaautoenumstring.integration;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {

    @Override
    public String convertToDatabaseColumn(OrderStatus status) {
        if (status == null) return null;
        return switch (status) {
            case PENDING -> "P";
            case CONFIRMED -> "C";
            case SHIPPED -> "S";
            case CANCELLED -> "X";
        };
    }

    @Override
    public OrderStatus convertToEntityAttribute(String code) {
        if (code == null) return null;
        return switch (code) {
            case "P" -> OrderStatus.PENDING;
            case "C" -> OrderStatus.CONFIRMED;
            case "S" -> OrderStatus.SHIPPED;
            case "X" -> OrderStatus.CANCELLED;
            default -> throw new IllegalArgumentException("Unknown code: " + code);
        };
    }
}
