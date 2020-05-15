package com.ns.vertx.pg.service;

import static com.ns.vertx.pg.jooq.tables.Author.AUTHOR;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.QueryResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

public class AuthorServiceImpl {

private static final Logger LOGGER = LoggerFactory.getLogger(AuthorServiceImpl.class);
	
	
	private static JsonObject fillAuthor(QueryResult booksQR) {
		return new JsonObject()
			.put("book_id", booksQR.get("author_id", Long.class))
			.put("first_name", booksQR.get("first_name", String.class))
			.put("last_name", booksQR.get("last_name", Double.class));							
	}
	
//	private static JsonObject extractAuthorsFromQR(QueryResult queryResult){
//		JsonArray authorJA = new JsonArray();
//		for(QueryResult qr: queryResult.asList()) {
//			JsonObject author = fillAuthor(qr);
//			authorJA.add(author);
//		}
//		return new JsonObject().put("authors", authorJA);
//	}	
	
	
	// FIXME: adjust this method to EXTRACT fields for Author
	private static JsonObject extractAuthorsFromLR(List<Row> authorLR){		
		JsonObject authorJO = new JsonObject(); 
		JsonArray authorsJA = new JsonArray();
		for(Row row: authorLR) {
			authorJO.put("author_id", row.getLong("author_id"));
			authorJO.put("first_name", row.getString("first_name"));
			authorJO.put("last_name", row.getString("last_name"));
			authorsJA.add(authorJO);
			authorJO.clear();
		}
		JsonObject joAuthors= new JsonObject().put("authors", authorsJA);		
		return joAuthors;
	}
	
	
	public static Future<JsonObject> getAllAuthorsJooq(ReactiveClassicGenericQueryExecutor queryExecutor) {
		Promise<JsonObject> finalRes = Promise.promise();					
		Future<List<Row>> bookFuture = queryExecutor.transaction(qe -> {			
			return qe.findManyRow(dsl -> dsl.selectFrom(AUTHOR));
		});				
		
	    bookFuture.onComplete(handler -> {
			if (handler.succeeded()) {								
				List<Row> authorsLR = handler.result();				
				JsonObject authorsJsonObject = extractAuthorsFromLR(authorsLR);
				//LOGGER.info("bookJsonObject.encodePrettily(): " + booksJsonObject.encodePrettily());
				finalRes.complete(authorsJsonObject);
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening ALL authors! Cause: " 
	    				+ handler.cause().getMessage());
	    		queryExecutor.rollback();
	    		finalRes.fail(handler.cause());
	    	}
	    }); 
		
		return finalRes.future();
	}
	
	
	
	
	
	
}
