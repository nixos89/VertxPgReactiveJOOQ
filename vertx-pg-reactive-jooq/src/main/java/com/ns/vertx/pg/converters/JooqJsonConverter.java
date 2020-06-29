package com.ns.vertx.pg.converters;

import org.jooq.Converter;
import org.jooq.JSON;

public class JooqJsonConverter implements Converter<String, JSON>{

	private static final long serialVersionUID = -4773701755042752633L;

	@Override
	public JSON from(String jooqJson) { return jooqJson == null ? null : JSON.valueOf(jooqJson); }

	@Override
	public String to(JSON jooqJson) { return jooqJson == null ? null : jooqJson.toString();	}

	@Override
	public Class<String> fromType() { return String.class; }

	@Override
	public Class<JSON> toType() { return JSON.class; }

}
