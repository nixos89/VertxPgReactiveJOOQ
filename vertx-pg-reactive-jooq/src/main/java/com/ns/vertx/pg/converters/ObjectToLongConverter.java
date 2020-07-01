package com.ns.vertx.pg.converters;

import org.jooq.Converter;

public class ObjectToLongConverter implements Converter<String, Long> {

	private static final long serialVersionUID = -5708890839251044451L;

	@Override
	public Long from(String databaseObject) {
		return databaseObject == null ? null : Long.valueOf(databaseObject);
	}

	@Override
	public String to(Long userObject) {
		long longVal = userObject;
		return userObject == null ? null : String.valueOf(longVal);
	}

	@Override
	public Class<String> fromType() {
		return String.class;
	}

	@Override
	public Class<Long> toType() {
		return Long.class;
	}

}
