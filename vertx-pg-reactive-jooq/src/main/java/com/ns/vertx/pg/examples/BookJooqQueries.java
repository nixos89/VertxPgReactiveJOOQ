package com.ns.vertx.pg.examples;


import static com.ns.vertx.pg.jooq.tables.AuthorBook.AUTHOR_BOOK;
import static com.ns.vertx.pg.jooq.tables.Book.BOOK;
import static com.ns.vertx.pg.jooq.tables.CategoryBook.CATEGORY_BOOK;

import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.jooq.tables.daos.AuthorBookDao;
import com.ns.vertx.pg.jooq.tables.daos.BookDao;
import com.ns.vertx.pg.jooq.tables.daos.CategoryBookDao;
import com.ns.vertx.pg.jooq.tables.pojos.AuthorBook;
import com.ns.vertx.pg.jooq.tables.pojos.Book;
import com.ns.vertx.pg.jooq.tables.pojos.CategoryBook;
import com.ns.vertx.pg.service.DBQueries;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.QueryResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class BookJooqQueries {	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BookJooqQueries.class);	
	 
	/* NOTE: using QueryResult as generic and driver agnostic response is a very good thing.
	 * from https://github.com/jklingsporn/vertx-jooq/issues/120#issuecomment-533879183 */
	private static JsonObject fillBook(QueryResult booksQR) {
		return new JsonObject()
			.put("book_id", booksQR.get("b_id", Long.class))
			.put("title", booksQR.get("title", String.class))
			.put("price", booksQR.get("price", Double.class))
			.put("amount", booksQR.get("amount", Integer.class))
			.put("deleted", booksQR.get("is_deleted", Boolean.class))
			.put("authors", booksQR.get("authors", JsonArray.class))
			.put("categories", booksQR.get("categories", JsonArray.class));				
	}
	
	private static JsonObject extractBooksFromQR(QueryResult queryResult){
		JsonArray booksJA = new JsonArray();
		for(QueryResult qr: queryResult.asList()) {
			JsonObject book = fillBook(qr);
			booksJA.add(book);
		}
		return new JsonObject().put("books", booksJA);
	}	
	
	static Future<JsonObject> getBookByIdJooq(ReactiveClassicGenericQueryExecutor queryExecutor, long book_id) {
		Promise<JsonObject> finalRes = Promise.promise();
		/* NOTE: might want to check into https://www.jooq.org/doc/3.11/manual/sql-building/bind-values/sql-injection/
		 *   ...to use dsl.fetch(SQL_QUERY, bindingParam) instead of dls.resultQuery */
		Future<QueryResult> bookFuture = queryExecutor.query(dsl -> dsl
		    	.resultQuery(DBQueries.GET_BOOK_BY_BOOK_ID, Long.valueOf(book_id)));
	    
	    bookFuture.onComplete(handler -> {
			if (handler.succeeded()) {
				LOGGER.info("Success, query has passed for book ID = " + book_id);												
				QueryResult booksQR = handler.result();				
				if(booksQR != null) {
					JsonObject bookJsonObject = fillBook(booksQR);				
					finalRes.complete(bookJsonObject);
				} else {				
					JsonObject resp = new JsonObject().put("message", "Book with id " + book_id + " does NOT exist in DB!");
					finalRes.complete(resp);
				}
				
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening query by book_id = " + book_id +
	    				" ! Cause: " + handler.cause());
	    		finalRes.fail(handler.cause());
	    	}
	    }); 
		
		return finalRes.future();
	}
	
	// ***************************************************************************************************************
	
	static Future<JsonObject> getAllBooksJooq(ReactiveClassicGenericQueryExecutor queryExecutor) {
		Promise<JsonObject> finalRes = Promise.promise();					
		Future<QueryResult> bookFuture = queryExecutor.query(dsl -> dsl.resultQuery(DBQueries.GET_ALL_BOOKS));	    
	    bookFuture.onComplete(handler -> {
			if (handler.succeeded()) {								
				QueryResult booksQR = handler.result();				
				JsonObject booksJsonObject = extractBooksFromQR(booksQR);
				LOGGER.info("bookJsonObject.encodePrettily(): " + booksJsonObject.encodePrettily());
				finalRes.complete(booksJsonObject);
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening ALL books! Cause: " + handler.cause().getMessage());
	    		finalRes.fail(handler.cause());
	    	}
	    }); 
		
		return finalRes.future();
	}
	
	// ***************************************************************************************************************
	
	static Future<JsonObject> createBookJooq(ReactiveClassicGenericQueryExecutor queryExecutor,
			BookDao bookDAO, AuthorBookDao authorBookDAO, CategoryBookDao categoryBookDAO, JsonObject bookJO) {
		Promise<JsonObject> promise = Promise.promise();			
		Book bookPojo = new Book(bookJO);		
		
		Future<Long> futureBookDao = bookDAO.insertReturningPrimary(bookPojo);
		futureBookDao.onComplete(handler -> {
			if (handler.succeeded()) {
				Long bookId = handler.result();
				Set<Long> authorUpdatedIds = bookJO.getJsonArray("author_ids").stream()
						.mapToLong(a -> Long.valueOf(String.valueOf(a))).boxed().collect(Collectors.toSet());		
				Set<Long> categoryUpdatedIds = bookJO.getJsonArray("category_ids").stream()
						.mapToLong(a -> Long.valueOf(String.valueOf(a))).boxed().collect(Collectors.toSet());	
				
				Set<AuthorBook> bookAuthors = new HashSet<>();
				Set<CategoryBook> bookCategories = new HashSet<>();
				
				for (Long authorId : authorUpdatedIds) {
					AuthorBook ab = new AuthorBook(authorId, bookId);
					bookAuthors.add(ab);					
				}				
				
				for (Long catId : categoryUpdatedIds) {
					CategoryBook cb = new CategoryBook(catId, bookId);
					bookCategories.add(cb);					
				}
				
				authorBookDAO.insert(bookAuthors);
				categoryBookDAO.insert(bookCategories);

				bookJO.put("book_id", bookId);									
				promise.complete(bookJO);
			} else {
				LOGGER.error("Error, insertion of book failed! Cause: " + handler.cause());
				promise.fail(handler.cause());
			}
		});
		
		return promise.future();
	}
	
	// ***************************************************************************************************************
	
	public static Future<JsonObject> updateBookJooq(ReactiveClassicGenericQueryExecutor queryExecutor, JsonObject bookJO, 
			BookDao bookDAO, AuthorBookDao authorBookDAO, CategoryBookDao categoryBookDAO, long bookId) {		
		
		Promise<JsonObject> promise = Promise.promise();				 
		Set<Long> authorUpdatedIds = bookJO.getJsonArray("authors").stream()
				.mapToLong(a -> Long.valueOf(String.valueOf(a))).boxed().collect(Collectors.toSet());	
		
		Set<Long> categoryUpdatedIds = bookJO.getJsonArray("categories").stream()
				.mapToLong(a -> Long.valueOf(String.valueOf(a))).boxed().collect(Collectors.toSet());
		
		LOGGER.info("categoryUpdatedIds:");
		categoryUpdatedIds.stream().forEach(System.out::println);
		
		Future<Integer> iterateCBFuture = iterateCategoryBook(queryExecutor, categoryBookDAO, categoryUpdatedIds, bookId);
		Future<Integer> iterateABFuture = iterateAuthorBook(queryExecutor, authorBookDAO, authorUpdatedIds, bookId);		
		Future<Integer> updateBookFuture = bookDAO.update(new Book(bookJO)); // UPDATES Book
		
		iterateCBFuture.compose(res -> iterateABFuture).compose(res -> updateBookFuture).onComplete(ar -> {
			if (ar.succeeded()) {
				LOGGER.info("Success, all went well!");
				promise.complete(bookJO);
			} else {
				LOGGER.error("Error, something FAILED in composition of 3 Futures!!! Cause: " + ar.cause() );
				promise.fail(ar.cause());
			}
		});		
		
		return promise.future();
	}
	

	// ***************************************************************************************************************
	
	private static Future<Integer> iterateCategoryBook(ReactiveClassicGenericQueryExecutor queryExecutor,
			CategoryBookDao categoryBookDAO, Set<Long> categoryUpdatedIds, long bookId) {

		return categoryBookDAO.findManyByBookId(Arrays.asList(bookId)).compose(existingBC -> {
			Set<Long> existingBCategoriesIds = existingBC.stream().map(cb -> cb.getCategoryId())
					.collect(Collectors.toSet());

			Set<Long> deleteCategoryIdsSet = existingBCategoriesIds.stream()
					.filter(catId -> !categoryUpdatedIds.contains(catId)).collect(Collectors.toSet());

			LOGGER.info("Going to DELETE next category IDs: ");
			deleteCategoryIdsSet.stream().forEach(System.out::println);

			Set<Long> toInsertCategoryIdsSet = categoryUpdatedIds.stream()
					.filter(catId -> !existingBCategoriesIds.contains(catId)).collect(Collectors.toSet());

			LOGGER.info("Category IDs to INSERT:");
			toInsertCategoryIdsSet.stream().forEach(System.out::println);
			
			Set<CategoryBook> bookCategories = new HashSet<>();
			for (Long catId : toInsertCategoryIdsSet) {
				CategoryBook cb = new CategoryBook(catId, bookId);
				bookCategories.add(cb);
			}
			// FIXME: make sure that NO EMPTY collection of CategoryBook has been INSERTED/DELETED!!!
			if (!deleteCategoryIdsSet.isEmpty() && !toInsertCategoryIdsSet.isEmpty()) {			
				return queryExecutor.execute(dsl -> dsl
						.deleteFrom(CATEGORY_BOOK)
						.where(CATEGORY_BOOK.BOOK_ID.eq(Long.valueOf(bookId)))
						.and(CATEGORY_BOOK.CATEGORY_ID.in(deleteCategoryIdsSet)))
					.compose(res -> categoryBookDAO.insert(bookCategories));				
			} else if (toInsertCategoryIdsSet.isEmpty() && !deleteCategoryIdsSet.isEmpty()) {				
				return queryExecutor.execute(dsl -> dsl
						.deleteFrom(CATEGORY_BOOK)
						.where(CATEGORY_BOOK.BOOK_ID.eq(Long.valueOf(bookId)))
						.and(CATEGORY_BOOK.CATEGORY_ID.in(deleteCategoryIdsSet)));
			} else if (!toInsertCategoryIdsSet.isEmpty() && deleteCategoryIdsSet.isEmpty()) {
				return categoryBookDAO.insert(bookCategories);
			} else {
				return Future.succeededFuture();
			}
		});
	}	
	
	// ***************************************************************************************************************
	
	private static Future<Integer> iterateAuthorBook(ReactiveClassicGenericQueryExecutor queryExecutor,
			AuthorBookDao authorBookDAO, Set<Long> authorUpdatedIds, long bookId) {
		
//		queryExecutor.beginTransaction()

		return authorBookDAO.findManyByBookId(Arrays.asList(bookId)).compose(existingAC -> {

			Set<Long> existingBAuhtorIds = existingAC.stream().map(ab -> ab.getAuthorId()).collect(Collectors.toSet());

			Set<Long> deleteAuthorIdsSet = existingBAuhtorIds.stream()
					.filter(autId -> !authorUpdatedIds.contains(autId)).collect(Collectors.toSet());

			LOGGER.info("Author IDs to DELETE:");
			deleteAuthorIdsSet.stream().forEach(System.out::println);

			Set<Long> toInsertAuthorIdsSet = authorUpdatedIds.stream()
					.filter(catId -> !existingBAuhtorIds.contains(catId)).collect(Collectors.toSet());

			LOGGER.info("Author IDs to INSERT:");
			toInsertAuthorIdsSet.stream().forEach(System.out::println);

			Set<AuthorBook> bookAuthors = new HashSet<>();
			for (Long autId : toInsertAuthorIdsSet) {
				AuthorBook ab = new AuthorBook(autId, bookId);
				bookAuthors.add(ab);
			}
			
			if (!toInsertAuthorIdsSet.isEmpty() && !deleteAuthorIdsSet.isEmpty()) {
				return queryExecutor.execute(dsl -> dsl
						.deleteFrom(AUTHOR_BOOK)
						.where(AUTHOR_BOOK.BOOK_ID.eq(Long.valueOf(bookId)))
						.and(AUTHOR_BOOK.AUTHOR_ID.in(deleteAuthorIdsSet))
				).compose(res -> authorBookDAO.insert(bookAuthors));
				
			} else if (!toInsertAuthorIdsSet.isEmpty() && deleteAuthorIdsSet.isEmpty()) {
				return authorBookDAO.insert(bookAuthors);
			} else if (toInsertAuthorIdsSet.isEmpty() && !deleteAuthorIdsSet.isEmpty()) {
				
				return queryExecutor.execute(dsl -> dsl
						.deleteFrom(AUTHOR_BOOK)
						.where(AUTHOR_BOOK.BOOK_ID.eq(Long.valueOf(bookId)))
						.and(AUTHOR_BOOK.AUTHOR_ID.in(deleteAuthorIdsSet)));
			} else {
				return Future.succeededFuture();
			}			
		});
	}
	
	// ***************************************************************************************************************
	
	static Future<Void> deleteBookJooq(ReactiveClassicGenericQueryExecutor queryExecutor, long id) {
		Promise<Void> promise = Promise.promise();
		Future<Integer> deleteBookFuture =  queryExecutor.execute(dsl -> dsl
			.delete(BOOK)
			.where(BOOK.BOOK_ID.eq(Long.valueOf(id)))
		);

		deleteBookFuture.onComplete(ar -> {
			if (ar.succeeded()) {
				LOGGER.info("Success, deletion successful for Book id = " + id);
				promise.handle(Future.succeededFuture());
			} else {
				LOGGER.error("Error, deletion failed for Book id = " + id);
				promise.handle(Future.failedFuture(new NoSuchElementException("No book with id = " + id)));
			}
		});
		
		return promise.future();
	}
	
	/*
	public void someMethod() {
		DSLContext context;
		Optional<Integer> result = Optional.of(context
			.insertInto(
				table("table"), field("f1"), field("f2"), field("f3"), field("f4"), field("f5"), 
				field("f6"), field("f7"), field("f8"), field("f9")))
			.map(statement -> {
				for (Media media : mediaList) {
					statement.values(media.f1(), media.f2);
				}
				return statement.execute();
			});		
	}	
	*/
}
