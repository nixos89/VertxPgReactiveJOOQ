package com.ns.vertx.pg.examples;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

/* IMPORTANT: this class uses queryExecutor and DAO objects for querying and upsert operations, but NOT transactions!
 * This class holds STATIC methods which are used as helper methods for routing in order to reduce 
 * boiler-plate code from MainVerticle class */
public class AuthorJooqQueries {	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorJooqQueries.class);
	
	private static JsonObject fillAuthor(Row row) {
		return new JsonObject()
				.put("author_id", row.getLong(0))
				.put("name", row.getString(1))
				.put("is_deleted", row.getBoolean(2));
	}
	
	static JsonObject convertListOfRowsToJO(List<Row> rowList) {
		JsonArray authorArr = new JsonArray();
		Iterator<Row> ir = rowList.iterator();		
		while(ir.hasNext()) {			
			Row row = ir.next();
			JsonObject author = fillAuthor(row);
			LOGGER.info("category:\n" + author.encodePrettily());
			authorArr.add(author);
		}
		JsonObject authorFinal = new JsonObject();
		authorFinal.put("authors", authorArr);
		return authorFinal;
	}
	
}
