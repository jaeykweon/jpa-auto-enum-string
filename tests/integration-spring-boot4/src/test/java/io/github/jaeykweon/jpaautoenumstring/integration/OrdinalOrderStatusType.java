package io.github.jaeykweon.jpaautoenumstring.integration;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.UserType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class OrdinalOrderStatusType implements UserType<OrderStatus> {

    @Override
    public int getSqlType() {
        return Types.INTEGER;
    }

    @Override
    public Class<OrderStatus> returnedClass() {
        return OrderStatus.class;
    }

    @Override
    public OrderStatus nullSafeGet(ResultSet rs, int position,
                                   WrapperOptions options) throws SQLException {
        int ordinal = rs.getInt(position);
        return rs.wasNull() ? null : OrderStatus.values()[ordinal];
    }

    @Override
    public void nullSafeSet(PreparedStatement st, OrderStatus value, int index,
                            WrapperOptions options) throws SQLException {
        if (value == null) st.setNull(index, Types.INTEGER);
        else st.setInt(index, value.ordinal());
    }

    @Override
    public OrderStatus deepCopy(OrderStatus value) { return value; }

    @Override
    public boolean isMutable() { return false; }
}
