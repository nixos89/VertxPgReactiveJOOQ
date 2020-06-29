package com.ns.vertx.pg.converters;

import org.jooq.Converter;
import org.jooq.JSON;

import io.vertx.core.json.JsonArray;

@SuppressWarnings("serial")
public class JooqJsonToVertxJsonArrayConverter implements Converter<JSON, JsonArray> {

	@Override
	public JsonArray from(JSON databaseObject) {
		String str = databaseObject == null ? null : databaseObject.toString();
		return str == null ? null : new JsonArray(str);
	}

	@Override
	public JSON to(JsonArray userObject) {
		String str = userObject == null ? null : userObject.toString();
		return str == null ? null : JSON.valueOf(str);
	}

	@Override
	public Class<JSON> fromType() {
		return JSON.class;
	}

	@Override
	public Class<JsonArray> toType() {
		return JsonArray.class;
	}

}
