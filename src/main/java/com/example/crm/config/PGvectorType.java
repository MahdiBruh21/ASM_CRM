package com.example.crm.config;

import com.pgvector.PGvector;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

public class PGvectorType implements UserType<PGvector> {

    @Override
    public int getSqlType() {
        return Types.OTHER; // PostgreSQL vector is a custom type
    }

    @Override
    public Class<PGvector> returnedClass() {
        return PGvector.class;
    }

    @Override
    public PGvector nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        Object value = rs.getObject(position);
        return value != null ? new PGvector(value.toString()) : null;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, PGvector value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value.toString(), Types.OTHER);
        }
    }

    @Override
    public boolean equals(PGvector x, PGvector y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(PGvector x) {
        return x != null ? x.hashCode() : 0;
    }

    @Override
    public PGvector deepCopy(PGvector value) {
        try {
            return value != null ? new PGvector(value.toString()) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(PGvector value) {
        return value != null ? value.toString() : null;
    }

    @Override
    public PGvector assemble(Serializable cached, Object owner) {
        try {
            return cached != null ? new PGvector(cached.toString()) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PGvector replace(PGvector original, PGvector target, Object owner) {
        return deepCopy(original);
    }
}