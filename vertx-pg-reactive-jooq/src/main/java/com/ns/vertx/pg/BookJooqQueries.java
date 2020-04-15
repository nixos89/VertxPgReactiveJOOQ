package com.ns.vertx.pg;


import com.ns.vertx.pg.jooq.tables.pojos.Book;

import static com.ns.vertx.pg.jooq.tables.Book.BOOK;

import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.QueryResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/* This class holds STATIC methods which are used as helper methods for routing in order to reduce 
 * boiler-plate code from MainVerticle class */
public class BookJooqQueries {	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BookJooqQueries.class);	
	
	static JsonObject fillBook(QueryResult booksQR) {
		JsonArray authors = booksQR.get("aut_ids", JsonArray.class);				
		JsonArray categories = booksQR.get("cat_ids", JsonArray.class);
		LOGGER.info("(fillBook) authors = " + authors.encodePrettily());
		LOGGER.info("(fillBook) categories = " + categories.encodePrettily());

		return new JsonObject()
			.put("bookId", booksQR.get("b_id", Long.class))
			.put("title", booksQR.get("title", String.class))
			.put("price", booksQR.get("price", Double.class))
			.put("amount", booksQR.get("amount", Integer.class))
			.put("isDeleted", booksQR.get("is_deleted", Boolean.class))
			.put("authors", authors)
			.put("categories", categories);				
	}
	
	// FIXME:
//	static List<JsonObject>
	
	static Future<JsonObject> getBookByIdJooq(ReactiveClassicGenericQueryExecutor queryExecutor, long book_id) {
		Promise<JsonObject> finalRes = Promise.promise();
		Future<QueryResult> bookFuture = queryExecutor.query(dsl -> dsl
		    	.resultQuery(DBQueries.GET_BOOK_BY_BOOK_ID_VER4, Long.valueOf(book_id)));
	    
	    bookFuture.setHandler(handler -> {
			if (handler.succeeded()) {
				LOGGER.info("Success, query has passed for book ID = " + book_id);												
				QueryResult booksQR = handler.result();				
				JsonObject bookJsonObject = fillBook(booksQR);
				
				LOGGER.info("bookJsonObject.encodePrettily(): " + bookJsonObject.encodePrettily());
				LOGGER.info("Success, ABOUT to complete result for for book ID = " + book_id + " ...");
				finalRes.complete(bookJsonObject);
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening query by book_id = " + book_id +
	    				" ! Cause: " + handler.cause().getMessage());
	    		finalRes.fail(handler.cause());
	    	}
	    }); 
		
		return finalRes.future();
	}
	
	
	static Future<JsonObject> getAllBooksJooq(ReactiveClassicGenericQueryExecutor queryExecutor) {
		Promise<JsonObject> finalRes = Promise.promise();
		
		/* FIXME: make sure that this query returns as SINGLE Row, not as multiple rows!!!
		 * Research for 'ARRAY_AGG' function implementation in (vertx-)jOOQ */ 
		Future<QueryResult> bookFuture = queryExecutor.query(dsl -> dsl
		    	.resultQuery(DBQueries.GET_ALL_BOOKS));
	    
	    bookFuture.setHandler(handler -> {
			if (handler.succeeded()) {
				LOGGER.info("Success, query has passed for all books!");												
				QueryResult booksQR = handler.result();				
				JsonObject booksJsonObject = fillBook(booksQR);
				LOGGER.info("bookJsonObject.encodePrettily(): " + booksJsonObject.encodePrettily());
				finalRes.complete(booksJsonObject);
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening ALL books! Cause: " 
	    				+ handler.cause().getMessage());
	    		finalRes.fail(handler.cause());
	    	}
	    }); 
		
		return finalRes.future();
	}
	
	
	static Future<Integer> createBookJooq(ReactiveClassicGenericQueryExecutor queryExecutor,
			String title, double price, int amount, boolean isDeleted
			/*, Set<Long> authorIds, Set<Long> categoryIds*/) {
		Future<Integer> retVal = queryExecutor.execute(dsl -> dsl
			.insertInto(BOOK, BOOK.TITLE, BOOK.PRICE, BOOK.AMOUNT, BOOK.IS_DELETED)
			.values(title, price, amount, isDeleted)
			.returningResult(BOOK.BOOK_ID, BOOK.TITLE, BOOK.PRICE, BOOK.AMOUNT, BOOK.IS_DELETED)
		);
		return retVal;
	}
	
	
	/* TODO: try out passing 'Set<Long> categoryIds' as param (IFF necessary check for @DataObject element) to 
	 * method signature in order to JOIN Book and Category via 'CategoryBook' table */
	static Future<JsonObject> createBookJooqRetVal(ReactiveClassicGenericQueryExecutor queryExecutor,
			String title, double price, int amount, boolean isDeleted
			/*, Set<Long> authorIds, Set<Long> categoryIds*/) {
		Promise<JsonObject> promise = Promise.promise();
		Future<Integer> retVal = queryExecutor.execute(dsl -> dsl
			.insertInto(BOOK, BOOK.TITLE, BOOK.PRICE, BOOK.AMOUNT, BOOK.IS_DELETED)
			.values(title, price, amount, isDeleted)
			.returningResult(BOOK.BOOK_ID, BOOK.TITLE, BOOK.PRICE, BOOK.AMOUNT, BOOK.IS_DELETED)
		);

		retVal.setHandler(ar -> {
			if (ar.succeeded()) {
				JsonObject result = new JsonObject()
					.put("title", title)
					.put("price", price)
					.put("amount", amount)
					.put("is_deleted", isDeleted);	
				promise.complete(result);
			} else {
				promise.fail(ar.cause());
			}
		});				
		return promise.future();
	}
	
	
	static Future<Integer> updateBookJooq(ReactiveClassicGenericQueryExecutor queryExecutor,
			Book book, long id) {

		Future<Integer> retVal = queryExecutor.execute(dsl -> dsl
			.update(BOOK)
			.set(BOOK.TITLE, book.getTitle())
			.set(BOOK.PRICE, book.getPrice())
			.set(BOOK.AMOUNT, book.getAmount())
			.set(BOOK.IS_DELETED, book.getIsDeleted())
			.where(BOOK.BOOK_ID.eq(Long.valueOf(id)))
		);
		return retVal;
	}
	
	
	static Future<Void> deleteBookJooq(ReactiveClassicGenericQueryExecutor queryExecutor, long id) {
		Promise<Void> promise = Promise.promise();
		Future<Integer> retVal =  queryExecutor.execute(dsl -> dsl
			.delete(BOOK)
			.where(BOOK.BOOK_ID.eq(Long.valueOf(id)))
		);
		retVal.setHandler(ar -> {
			if(ar.succeeded()) {
				promise.handle(Future.succeededFuture());
			} else {
				promise.handle(Future.failedFuture(new NoSuchElementException("No book with id = " + id)));
			}
		});
		
		return promise.future();
	}
	
	
}
