package io.github.jaeykweon.jpaautoenumstring.integration;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
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
                                   SharedSessionContractImplementor session,
                                   Object owner) throws SQLException {
        int ordinal = rs.getInt(position);
        return rs.wasNull() ? null : OrderStatus.values()[ordinal];
    }

    @Override
    public void nullSafeSet(PreparedStatement st, OrderStatus value, int index,
                            SharedSessionContractImplementor session) throws SQLException {
        if (value == null) st.setNull(index, Types.INTEGER);
        else st.setInt(index, value.ordinal());
    }

    @Override
    public boolean equals(OrderStatus x, OrderStatus y) { return x == y; }

    @Override
    public int hashCode(OrderStatus x) { return x == null ? 0 : x.hashCode(); }

    @Override
    public OrderStatus deepCopy(OrderStatus value) { return value; }

    @Override
    public boolean isMutable() { return false; }

    @Override
    public Serializable disassemble(OrderStatus value) { return value; }

    @Override
    public OrderStatus assemble(Serializable cached, Object owner) { return (OrderStatus) cached; }
}
