package com.ns.vertx.pg.service;

import static com.ns.vertx.pg.jooq.tables.Author.AUTHOR;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.jooq.tables.pojos.Author;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

public class AuthorServiceImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorServiceImpl.class);
		
	private static JsonObject fillAuthor(Row row) {
		return new JsonObject()
			.put("author_id", row.getLong("author_id"))
			.put("first_name", row.getString("first_name"))
			.put("last_name", row.getString("last_name"));							
	}	
	
	private static JsonObject extractAuthorsFromLR(List<Row> authorLR){		
		JsonObject authorJO = new JsonObject(); 
		JsonArray authorsJA = new JsonArray();
		for(Row row: authorLR) {
			authorJO = fillAuthor(row);
			authorsJA.add(authorJO);
			authorJO = new JsonObject();
		}
		JsonObject joAuthors= new JsonObject().put("authors", authorsJA);		
		return joAuthors;
	}
	
	
	public static Future<JsonObject> getAllAuthorsJooq(ReactiveClassicGenericQueryExecutor queryExecutor) {
		Promise<JsonObject> finalRes = Promise.promise();					
		Future<List<Row>> bookFuture = queryExecutor.transaction(qe -> {			
			return qe.findManyRow(dsl -> dsl.select(AUTHOR.AUTHOR_ID, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME).from(AUTHOR));
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
	
	
	public static Future<JsonObject> getAuthorByIdJooq(ReactiveClassicGenericQueryExecutor queryExecutor, long authorId) {
		Promise<JsonObject> finalRes = Promise.promise();					
		Future<Row> bookFuture = queryExecutor.transaction(qe -> {			
			return qe.findOneRow(dsl -> dsl
				.select(AUTHOR.AUTHOR_ID, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME)
				.from(AUTHOR).where(AUTHOR.AUTHOR_ID.eq(Long.valueOf(authorId))));
		});				
		
	    bookFuture.onComplete(handler -> {
			if (handler.succeeded()) {										
				if (handler.result() == null) {
					finalRes.handle(Future.failedFuture(new NoSuchElementException("No author with id = " + authorId)));				
				} else {
					JsonObject authorJsonObject = fillAuthor(handler.result());
					finalRes.complete(authorJsonObject);
				}				
	    	} else {
				LOGGER.error("Error, something failed in retrivening author by id " + authorId 
						+ " ! Cause: " + handler.cause().getMessage());
	    		queryExecutor.rollback();
	    		finalRes.fail(handler.cause());
	    	}
	    }); 
		
		return finalRes.future();
	}
	
	
	public static Future<Void> createAuthorJooq(ReactiveClassicGenericQueryExecutor queryExecutor,
			String firstName, String lastName) {		
		Promise<Void> promise = Promise.promise();			
		Future<Integer> retVal = queryExecutor.transaction(qe ->
			qe.execute(dsl -> dsl
				.insertInto(AUTHOR, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME)
				.values(firstName, lastName)
				.returningResult(AUTHOR.AUTHOR_ID, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME))
		);
		
		retVal.onComplete(ar -> promise.complete());
		retVal.onFailure(ar -> promise.fail(ar));			
		return promise.future();
	}	
	

	public static Future<Void> updateAuthorJooq(ReactiveClassicGenericQueryExecutor queryExecutor,
			Author authorPojo, long id) {
		Promise<Void> promise = Promise.promise();												
		
		Future<Integer> retVal = queryExecutor.transaction(qe -> {				
			return qe.execute(dsl -> dsl
				.update(AUTHOR)
				.set(AUTHOR.FIRST_NAME, authorPojo.getFirstName())
				.set(AUTHOR.LAST_NAME, authorPojo.getLastName())
				.where(AUTHOR.AUTHOR_ID.eq(Long.valueOf(id)))				
			);
		});		
		retVal.onComplete(ar -> promise.handle(Future.succeededFuture()));
		retVal.onFailure(handler -> promise.handle(Future.failedFuture(
				new NoSuchElementException("Error, author has not been updated for id = " + id + "! Cause: " + handler))));		
		return promise.future();
	}
	
	
	public static Future<Void> deleteAuthorJooq(ReactiveClassicGenericQueryExecutor queryExecutor, long id) {
		Promise<Void> promise = Promise.promise();
		Future<Integer> deleteAuthorFuture = queryExecutor.transaction(qe -> {
			return qe.execute(dsl -> dsl
				.delete(AUTHOR)
				.where(AUTHOR.AUTHOR_ID.eq(Long.valueOf(id))));
		});
		
		deleteAuthorFuture.onComplete(ar -> {
			if(ar.succeeded()) {
				promise.handle(Future.succeededFuture());
			} else {
				queryExecutor.rollback();
				promise.handle(Future.failedFuture(new NoSuchElementException("No author with id = " + id)));
			}
		});
		
		return promise.future();
	}
	
	
}
