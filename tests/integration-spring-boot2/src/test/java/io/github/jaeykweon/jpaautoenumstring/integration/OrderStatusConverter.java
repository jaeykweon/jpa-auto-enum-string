package io.github.jaeykweon.jpaautoenumstring.integration;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

// Stores OrderStatus as a single-character code (P/C/S/X) instead of the enum name or ordinal.
// Used in tests to verify that @Convert on an enum field is not overridden by the library.
@Converter
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {

    @Override
    public String convertToDatabaseColumn(OrderStatus status) {
        if (status == null) return null;
        switch (status) {
            case PENDING:   return "P";
            case CONFIRMED: return "C";
            case SHIPPED:   return "S";
            case CANCELLED: return "X";
            default: throw new IllegalArgumentException("Unknown status: " + status);
        }
    }

    @Override
    public OrderStatus convertToEntityAttribute(String code) {
        if (code == null) return null;
        switch (code) {
            case "P": return OrderStatus.PENDING;
            case "C": return OrderStatus.CONFIRMED;
            case "S": return OrderStatus.SHIPPED;
            case "X": return OrderStatus.CANCELLED;
            default: throw new IllegalArgumentException("Unknown code: " + code);
        }
    }
}
