package com.ns.vertx.pg.converters;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Objects;

import org.jooq.Binding;
import org.jooq.BindingGetResultSetContext;
import org.jooq.BindingGetSQLInputContext;
import org.jooq.BindingGetStatementContext;
import org.jooq.BindingRegisterContext;
import org.jooq.BindingSQLContext;
import org.jooq.BindingSetSQLOutputContext;
import org.jooq.BindingSetStatementContext;
import org.jooq.Converter;
import org.jooq.JSON;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;

import io.vertx.core.json.JsonArray;

@SuppressWarnings("serial")
public class PostgresJSONVertxJsonArrayBinding implements Binding<Object, JsonArray> {

	@Override
	public Converter<Object, JsonArray> converter() {
		return new Converter<Object, JsonArray>() {

			@Override
			public JsonArray from(Object dbObject) { // databaseObject is of type JSON in PL/pgSQL
				return dbObject == null ? null : new JsonArray((String) dbObject);
			}

			@Override
			public Object to(JsonArray jsonObject) {
				String strVal = (jsonObject == null ? null : jsonObject.toString());
				return jsonObject == null ? null : JSON.valueOf(strVal);
			}

			@Override
			public Class<Object> fromType() {
				return Object.class;
			}

			@Override
			public Class<JsonArray> toType() {
				return JsonArray.class;
			}
		};
	}

	// Rending a bind variable for the binding context's value and casting it to the
	// json type
	@Override
	public void sql(BindingSQLContext<JsonArray> ctx) throws SQLException {
		// Depending on how you generate your SQL, you may need to explicitly
		// distinguish
		// between jOOQ generating bind variables or inlined literals.
		if (ctx.render().paramType() == ParamType.INLINED)
			ctx.render().visit(DSL.inline(ctx.convert(converter()).value())).sql("::json");
		else
			ctx.render().sql("?::json");
	}

	// Registering VARCHAR types for JDBC CallableStatement OUT parameters
	@Override
	public void register(BindingRegisterContext<JsonArray> ctx) throws SQLException {
		ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
	}

	// Converting the JsonArray to a String value and setting that on a JDBC
	// PreparedStatement
	@Override
	public void set(BindingSetStatementContext<JsonArray> ctx) throws SQLException {
		ctx.statement().setString(ctx.index(), Objects.toString(ctx.convert(converter()).value(), null));
	}

	// Setting a value on a JDBC SQLOutput (useful for Oracle OBJECT types)
	@Override
	public void set(BindingSetSQLOutputContext<JsonArray> ctx) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	// Getting a String value from a JDBC ResultSet and converting that to a
	// JsonArray
	@Override
	public void get(BindingGetResultSetContext<JsonArray> ctx) throws SQLException {
		ctx.convert(converter()).value(ctx.resultSet().getString(ctx.index()));
	}

	// Getting a String value from a JDBC CallableStatement and converting that to a
	// JsonElement
	@Override
	public void get(BindingGetStatementContext<JsonArray> ctx) throws SQLException {
		ctx.convert(converter()).value(ctx.statement().getString(ctx.index()));
	}

	// Getting a value from a JDBC SQLInput (useful for Oracle OBJECT types)
	@Override
	public void get(BindingGetSQLInputContext<JsonArray> ctx) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

}
