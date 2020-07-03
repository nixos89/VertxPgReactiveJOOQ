package com.ns.vertx.pg.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import io.netty.buffer.ByteBufInputStream;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.jooq.*;
import org.jooq.exception.DataTypeException;
import org.jooq.impl.DSL;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract class PojoListToJsonArrayBinding<O> implements Binding<JSONB, List<O>> {

    private final Class<O> destinationClass;

    private final Converter<JSONB, List<O>> converter = new Converter<JSONB, List<O>>() {
        @Override
        @SuppressWarnings("unchecked")
        public List<O> from(JSONB t) {
            if(t == null || t.data() == null){
                return null;
            }
            CollectionType collectionType = new ObjectMapper().getTypeFactory().constructCollectionType(ArrayList.class, destinationClass);
            try {
                return new ObjectMapper().readValue(new ByteArrayInputStream(t.toString().getBytes()), collectionType);
            } catch (IOException e) {
                throw new DataTypeException(e.getMessage(),e);
            }
        }

        @Override
        public JSONB to(List<O> u) {
            try {
                return u == null ? JSONB.valueOf(null) : JSONB.valueOf(new ObjectMapper().writeValueAsString(u));
            } catch (JsonProcessingException e) {
                throw new DataTypeException(e.getMessage(),e);
            }
        }

        @Override
        public Class<JSONB> fromType() {
            return JSONB.class;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<List<O>> toType() {
            return (Class<List<O>>) destinationClass;
        }
    };

    protected PojoListToJsonArrayBinding(Class<O> destinationClass) {
        this.destinationClass = destinationClass;
    }

    // The converter does all the work
    @Override
    public Converter<JSONB, List<O>> converter() {
        return converter;
    }

    // Rending a bind variable for the binding context's value and casting it to the json type
    @Override
    public void sql(BindingSQLContext<List<O>> ctx) throws SQLException {
        // Depending on how you generate your SQL, you may need to explicitly distinguish
        // between jOOQ generating bind variables or inlined literals. If so, use this check:
        // ctx.render().paramType() == INLINED
        ctx.render().visit(DSL.val(ctx.convert(converter()).value())).sql("::jsonb");
    }

    // Registering VARCHAR types for JDBC CallableStatement OUT parameters
    @Override
    public void register(BindingRegisterContext<List<O>> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }

    // Converting the O to a String value and setting that on a JDBC PreparedStatement
    @Override
    public void set(BindingSetStatementContext<List<O>> ctx) throws SQLException {
        ctx.statement().setString(ctx.index(), Objects.toString(ctx.convert(converter()).value(), null));
    }

    // Getting a String value from a JDBC ResultSet and converting that to a O
    @Override
    public void get(BindingGetResultSetContext<List<O>> ctx) throws SQLException {
        ctx.convert(converter()).value(JSONB.valueOf(ctx.resultSet().getString(ctx.index())));
    }

    // Getting a String value from a JDBC CallableStatement and converting that to a O
    @Override
    public void get(BindingGetStatementContext<List<O>> ctx) throws SQLException {
        ctx.convert(converter()).value(JSONB.valueOf(ctx.statement().getString(ctx.index())));
    }

    // Setting a value on a JDBC SQLOutput (useful for Oracle JSONB types)
    @Override
    public void set(BindingSetSQLOutputContext<List<O>> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    // Getting a value from a JDBC SQLInput (useful for Oracle JSONB types)
    @Override
    public void get(BindingGetSQLInputContext<List<O>> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}