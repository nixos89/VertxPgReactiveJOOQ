package com.ns.vertx.pg.converters;

import java.sql.SQLException;
import java.util.Objects;

import org.jooq.Binding;
import org.jooq.BindingGetResultSetContext;
import org.jooq.BindingSQLContext;
import org.jooq.BindingSetStatementContext;
import org.jooq.Converter;
import org.jooq.JSON;
import org.jooq.impl.DSL;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class ObjectToJsonObjectBinding  /*implements Binding<Object, JsonObject> */{
/*
	private static final Converter<Object, JsonObject> CONVERTER = new Converter<Object, JsonObject>() {
        @Override
        public JsonObject from(Object databaseJsonObject) {
            Json pgJsonWrapper = convertobject(databaseJsonObject, Json.class);
            return convertobject(pgJsonWrapper.value(), toType());
        }

        private <U> U convertobject(Object object, Class<U> type) {
            if (!type.isInstance(object)) {
                throw new IllegalArgumentException("Expected Vert.x JSON type: " + type + ", but got: " + object);
            }
            return (U) object;
        }

        @Override
        public Object to(JsonObject vertxJsonObject) {
            return JsonObject.mapFrom(vertxJsonObject);
        }

        @Override
        public Class<Object> fromType() {
            return Object.class;
        }

        @Override
        public Class<JsonObject> toType() {
            return JsonObject.class;
        }
    };

    // The converter does all the work
    @Override
    public Converter<Object, JsonObject> converter() {
        return CONVERTER;
    }

    // Rending a bind variable for the binding context's value and casting it to the json type
    @Override
    public void sql(BindingSQLContext<JsonObject> ctx) throws SQLException {
        // Depending on how you generate your SQL, you may need to explicitly distinguish
        // between jOOQ generating bind variables or inlined literals. If so, use this check:
        // ctx.render().paramType() == INLINED
        ctx.render().sql("?::json");
    }

    // Registering VARCHAR types for JDBC CallableStatement OUT parameters
    @Override
    public void register(BindingRegisterContext<JsonObject> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }

    // Converting the JsonObject to a String value and setting that on a JDBC PreparedStatement
    @Override
    public void set(BindingSetStatementContext<JsonObject> ctx) throws SQLException {
        ctx.statement().setString(ctx.index(), Objects.toString(ctx.convert(converter()).value(), null));
    }

    // Getting a String value from a JDBC ResultSet and converting that to a JsonObject
    @Override
    public void get(BindingGetResultSetContext<JsonObject> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.resultSet().getString(ctx.index()));
    }

    // Getting a String value from a JDBC CallableStatement and converting that to a JsonObject
    @Override
    public void get(BindingGetStatementContext<JsonObject> ctx) throws SQLException {
        ctx.convert(converter()).value(ctx.statement().getString(ctx.index()));
    }

    // Setting a value on a JDBC SQLOutput (useful for Oracle OBJECT types)
    @Override
    public void set(BindingSetSQLOutputContext<JsonObject> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    // Getting a value from a JDBC SQLInput (useful for Oracle OBJECT types)
    @Override
    public void get(BindingGetSQLInputContext<JsonObject> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
	*/
}
