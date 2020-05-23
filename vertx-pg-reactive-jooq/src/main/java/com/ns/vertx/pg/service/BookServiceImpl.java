package com.ns.vertx.pg.service;

import static com.ns.vertx.pg.jooq.tables.Author.AUTHOR;
import static com.ns.vertx.pg.jooq.tables.AuthorBook.AUTHOR_BOOK;
import static com.ns.vertx.pg.jooq.tables.Book.BOOK;
import static com.ns.vertx.pg.jooq.tables.Category.CATEGORY;
import static com.ns.vertx.pg.jooq.tables.CategoryBook.CATEGORY_BOOK;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.jooq.CommonTableExpression;
import org.jooq.Record2;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.jooq.tables.daos.AuthorBookDao;
import com.ns.vertx.pg.jooq.tables.daos.BookDao;
import com.ns.vertx.pg.jooq.tables.daos.CategoryBookDao;
import com.ns.vertx.pg.jooq.tables.pojos.Book;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.QueryResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;


public class BookServiceImpl {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BookServiceImpl.class);
		
	public static Future<JsonObject> getAllBooksJooq(ReactiveClassicGenericQueryExecutor queryExecutor) {
		Promise<JsonObject> finalRes = Promise.promise();					
		Future<QueryResult> bookFuture = queryExecutor.transaction(qe -> {			
			return qe.query(dsl -> dsl.resultQuery(DBQueries.GET_ALL_BOOKS));
		});				
		
	    bookFuture.onComplete(handler -> {
			if (handler.succeeded()) {								
				QueryResult booksQR = handler.result();				
				JsonObject booksJsonObject = BookUtilHelper.extractBooksFromQR(booksQR);
				finalRes.complete(booksJsonObject);
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening ALL books! Cause: " 
	    				+ handler.cause().getMessage());
	    		queryExecutor.rollback();
	    		finalRes.fail(handler.cause());
	    	}
	    }); 
		
		return finalRes.future();
	}
	
	// *********************************************************************************************************************************	
	// FIXME: distinction of Authors and Categories to be returned as an JSON_ARRAY
	public static Future<JsonObject> getAllBooksByAuthorIdJooq(ReactiveClassicGenericQueryExecutor queryExecutor, long authorId) {
		Promise<JsonObject> finalRes = Promise.promise();					
		Future<List<Row>> bookFuture = queryExecutor.transaction(qe -> {			
			return qe.findManyRow(dsl -> dsl
				/* FIXME: must use DISTINCT for AUTHORS and CATEGORIES to be able to return ALL of them in 1 ROW!!! 
				 *   For now it's all being SINGLE row e.g.: 
				 *   bookID=1, category=8
				 *   bookID=1, category=9 */
				.select(BOOK.BOOK_ID, BOOK.TITLE, BOOK.PRICE, BOOK.AMOUNT, BOOK.IS_DELETED, 
						AUTHOR.AUTHOR_ID, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME, 
						CATEGORY.CATEGORY_ID, CATEGORY.NAME, CATEGORY.IS_DELETED)
				.from(BOOK
					.leftJoin(AUTHOR_BOOK).on(BOOK.BOOK_ID.eq(AUTHOR_BOOK.BOOK_ID))
					.leftJoin(AUTHOR).on(AUTHOR_BOOK.AUTHOR_ID.eq(AUTHOR.AUTHOR_ID))
					.leftJoin(CATEGORY_BOOK).on(BOOK.BOOK_ID.eq(CATEGORY_BOOK.BOOK_ID))
					.leftJoin(CATEGORY).on(CATEGORY_BOOK.CATEGORY_ID.eq(CATEGORY.CATEGORY_ID)))
				.where(AUTHOR.AUTHOR_ID.eq(Long.valueOf(authorId)))
				.groupBy(BOOK.BOOK_ID).orderBy(BOOK.BOOK_ID.asc())
			);
		});						
		
	    bookFuture.onComplete(handler -> {
			if (handler.succeeded()) {								
				List<Row> booksLR = handler.result();				
				JsonObject booksJsonObject = BookUtilHelper.extractBooksFromLR(booksLR);
				LOGGER.info("bookJsonObject.encodePrettily(): " + booksJsonObject.encodePrettily());
				finalRes.complete(booksJsonObject);
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening ALL books! Cause: " 
	    				+ handler.cause().getMessage());
	    		queryExecutor.rollback();
	    		finalRes.fail(handler.cause());
	    	}
	    }); 
		
		return finalRes.future();
	}
	
	// *********************************************************************************************************************************
	
	public static Future<JsonObject> getAllBooksByAuthorIdJooqMix(ReactiveClassicGenericQueryExecutor queryExecutor, long authorId) {
		Promise<JsonObject> finalRes = Promise.promise();
		
		Future<QueryResult> bookFuture = queryExecutor.transaction(qe -> {
			return qe.query(dsl -> dsl
			    .resultQuery(DBQueries.GET_ALL_BOOKS_BY_AUTHOR_ID, Long.valueOf(authorId)));
		});							
	    bookFuture.onComplete(handler -> {
			if (handler.succeeded()) {								
				QueryResult booksByAuthorIdQR = handler.result();				
				JsonObject booksJsonObject = BookUtilHelper.extractBooksFromQR(booksByAuthorIdQR);
				LOGGER.info("bookJsonObject.encodePrettily(): " + booksJsonObject.encodePrettily());
				finalRes.complete(booksJsonObject);
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening ALL books! Cause: " + handler.cause().getMessage());
	    		queryExecutor.rollback();
	    		finalRes.fail(handler.cause());
	    	}
	    }); 
		
		return finalRes.future();
	}
	
	// *********************************************************************************************************************************
		
	public static Future<JsonObject> getBookByIdJooq(ReactiveClassicGenericQueryExecutor queryExecutor, long bookId) {
		Promise<JsonObject> finalRes = Promise.promise();

		Future<QueryResult> bookFuture = queryExecutor.transaction(qe -> {
			return qe.query(dsl -> dsl
			    .resultQuery(DBQueries.GET_BOOK_BY_BOOK_ID, Long.valueOf(bookId)));
		});								    
	    bookFuture.onComplete(handler -> {
			if (handler.succeeded()) {
				//LOGGER.info("Success, query has passed for book ID = " + book_id);												
				QueryResult booksQR = handler.result();				
				if (booksQR != null) {
					JsonObject bookJsonObject = BookUtilHelper.fillBook(booksQR);				
					finalRes.complete(bookJsonObject);
				} else {				
					JsonObject resp = new JsonObject().put("message", "Book with id " + bookId + " does NOT exist in DB!");
					finalRes.complete(resp);
				}
				
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening query by book_id = " + bookId +
	    				" ! Cause: " + handler.cause());
	    		queryExecutor.rollback();
	    		finalRes.fail(handler.cause());
	    	}
	    }); 
		
		return finalRes.future();
	}
	
	// **************************************************************************************************************************	
	
	public static Future<Void> createBookJooq(ReactiveClassicGenericQueryExecutor queryExecutor, JsonObject bookJO) {
		Promise<Void> promise = Promise.promise();			
		Book bookPojo = new Book(bookJO); 
		
		Future<Void> transactionFuture = queryExecutor.beginTransaction().compose(transactionQE -> 
			transactionQE.executeAny(dsl -> dsl		
				.insertInto(BOOK, BOOK.TITLE, BOOK.AMOUNT, BOOK.PRICE, BOOK.IS_DELETED)
				.values(bookPojo.getTitle(), bookPojo.getAmount(), bookPojo.getPrice(), bookPojo.getIsDeleted())
				.returningResult(BOOK.BOOK_ID, BOOK.TITLE, BOOK.AMOUNT, BOOK.PRICE, BOOK.IS_DELETED)
			).compose(insertedBook -> { 				
				JsonObject resultJO =  BookUtilHelper.extractBookFromRS(insertedBook);
				final Long bookId = resultJO.getLong("book_id");
				LOGGER.info("saved book:\n" + resultJO.encodePrettily());
				
				List<Long> authorIds = bookJO.getJsonArray("author_ids").stream().mapToLong(a -> Long.valueOf(String.valueOf(a))).boxed().collect(Collectors.toList());															
				List<Long> categoryIds = bookJO.getJsonArray("category_ids").stream().mapToLong(c -> Long.valueOf(String.valueOf(c))).boxed().collect(Collectors.toList());																	
								
				return transactionQE.execute(dsl -> {										
					CommonTableExpression<Record2<Long, Long>> author_book_tbl = DSL.name("author_book_tbl").fields("book_id", "author_id")
							.as(dsl.select(BOOK.BOOK_ID, AUTHOR.AUTHOR_ID).from(BOOK)
								   .crossJoin(AUTHOR).where( BOOK.BOOK_ID.eq(bookId).and(AUTHOR.AUTHOR_ID.in(authorIds)) ));
										
					return dsl.with(author_book_tbl)						
						.insertInto(AUTHOR_BOOK, AUTHOR_BOOK.BOOK_ID, AUTHOR_BOOK.AUTHOR_ID)
						.select(dsl.selectFrom(author_book_tbl));
				}).compose(res -> {
					return transactionQE.execute(dsl -> { 						
						CommonTableExpression<Record2<Long, Long>> category_book_tbl = DSL.name("category_book_tbl").fields("book_id", "category_id")
							.as(dsl.select(BOOK.BOOK_ID, CATEGORY.CATEGORY_ID).from(BOOK)
								   .crossJoin(CATEGORY).where( BOOK.BOOK_ID.eq(bookId).and(CATEGORY.CATEGORY_ID.in(categoryIds)) ));
											
						return dsl.with(category_book_tbl)									
							.insertInto(CATEGORY_BOOK, CATEGORY_BOOK.BOOK_ID, CATEGORY_BOOK.CATEGORY_ID)
						    .select(dsl.selectFrom(category_book_tbl));					
					}); 
				}).compose(success -> {
						LOGGER.info("Commiting transcation...");
						return transactionQE.commit();
					}, failure -> {
						LOGGER.info("Oops, rolling-back transcation...");
						return transactionQE.rollback();
					});		
		}));		
		transactionFuture.onComplete(handler -> {
			if (handler.succeeded()) {
				LOGGER.info("Success, inserting is completed!");
				promise.complete();
			} else {
				LOGGER.error("Error, somethin' WENT WRRRONG!! handler.result() = " + handler.result());
				promise.handle(Future.failedFuture(handler.cause()));
			}
		});
		return promise.future();
	}		
	
	// *********************************************************************************************************************************	
	// FIXME: re-implement Book UPDATE method -> REMOVE DAO objects!!!
	public static Future<JsonObject> updateBookJooq(ReactiveClassicGenericQueryExecutor queryExecutor, JsonObject bookJO, 
			BookDao bookDAO, AuthorBookDao authorBookDAO, CategoryBookDao categoryBookDAO, long bookId) {		
		
		Promise<JsonObject> promise = Promise.promise();				 
		Set<Long> authorUpdatedIds = bookJO.getJsonArray("authors").stream()
				.mapToLong(a -> Long.valueOf(String.valueOf(a))).boxed().collect(Collectors.toSet());	
		
		Set<Long> categoryUpdatedIds = bookJO.getJsonArray("categories").stream()
				.mapToLong(a -> Long.valueOf(String.valueOf(a))).boxed().collect(Collectors.toSet());
		
		LOGGER.info("categoryUpdatedIds:");
		categoryUpdatedIds.stream().forEach(System.out::println);
		
		Future<Void> iterateCBFuture = iterateCategoryBook(queryExecutor, categoryUpdatedIds, bookId);
		Future<Void> iterateABFuture = iterateAuthorBook(queryExecutor, authorUpdatedIds, bookId);		
		
		Book bookPojo = new Book(bookJO);
		Future<Void> updateBookFuture = queryExecutor.beginTransaction().compose(transcationQE -> 
			transcationQE.execute( dsl -> dsl
				.update(BOOK).set(BOOK.TITLE, bookPojo.getTitle())
				.set(BOOK.PRICE, bookPojo.getPrice()).set(BOOK.AMOUNT, bookPojo.getAmount()).set(BOOK.IS_DELETED, bookPojo.getIsDeleted())
				.where(BOOK.BOOK_ID.eq(Long.valueOf(bookId)))
			).compose(res -> iterateCBFuture).compose(res -> iterateABFuture)
			 .compose(success -> {
				  LOGGER.info("Commiting transaction...");
				  return transcationQE.commit();
			  }, failure -> {
				  LOGGER.info("Rolling back transaction...");
				  return transcationQE.rollback();
			  }));
		updateBookFuture.onSuccess(handler -> { LOGGER.info("Success, book UPDATE is successful!"); promise.complete(); });
		updateBookFuture.onFailure(handler -> { LOGGER.error("Error, book UPDATE FAILED!"); promise.fail(handler); });		
		return promise.future();
	}	

	// ***************************************************************************************************************
	
	private static Future<Void> iterateCategoryBook(ReactiveClassicGenericQueryExecutor queryExecutor,
			Set<Long> categoryUpdatedIds, long bookId) {
		Promise<Void> promise = Promise.promise();				
		Future<Integer> iterateCBFuture = queryExecutor.findManyRow(dsl -> dsl
				.select(CATEGORY_BOOK.CATEGORY_ID).from(CATEGORY_BOOK)
				.where(CATEGORY_BOOK.BOOK_ID.eq(Long.valueOf(bookId)))).compose(existingBC -> {
					
			Set<Long> existingBCatIds = BookUtilHelper.extractCategoriesFromLR(existingBC);			
			Set<Long> deleteCatIdsSet = existingBCatIds.stream()
					.filter(catId -> !categoryUpdatedIds.contains(catId)).collect(Collectors.toSet());			
			Set<Long> toInsertCatIdsSet = categoryUpdatedIds.stream()
					.filter(catId -> !existingBCatIds.contains(catId)).collect(Collectors.toSet());
			
			if (!deleteCatIdsSet.isEmpty() && !toInsertCatIdsSet.isEmpty()) {			
				queryExecutor.execute(dsl -> dsl
						.deleteFrom(CATEGORY_BOOK)
						.where(CATEGORY_BOOK.BOOK_ID.eq(Long.valueOf(bookId)))
						.and(CATEGORY_BOOK.CATEGORY_ID.in(deleteCatIdsSet)))
					.compose(res -> queryExecutor.execute(dsl -> { // performs insertion of categories
						CommonTableExpression<Record2<Long, Long>> category_book_tbl = DSL.name("category_book_tbl").fields("book_id", "category_id")
								.as(dsl.select(BOOK.BOOK_ID, CATEGORY.CATEGORY_ID).from(BOOK)
									   .crossJoin(CATEGORY).where( BOOK.BOOK_ID.eq(bookId).and(CATEGORY.CATEGORY_ID.in(toInsertCatIdsSet)) ));
												
							return dsl.with(category_book_tbl)									
								.insertInto(CATEGORY_BOOK, CATEGORY_BOOK.BOOK_ID, CATEGORY_BOOK.CATEGORY_ID)
							    .select(dsl.selectFrom(category_book_tbl));	
					})); 
				promise.complete();
				return Future.succeededFuture();
			} else if (toInsertCatIdsSet.isEmpty() && !deleteCatIdsSet.isEmpty()) {				
				queryExecutor.execute(dsl -> dsl.deleteFrom(CATEGORY_BOOK).where(CATEGORY_BOOK.BOOK_ID.eq(Long.valueOf(bookId)))
						.and(CATEGORY_BOOK.CATEGORY_ID.in(deleteCatIdsSet)));
				promise.complete();
				return Future.succeededFuture();
			} else if (!toInsertCatIdsSet.isEmpty() && deleteCatIdsSet.isEmpty()) {
				Future<Integer> insertCBFuture = queryExecutor.execute(dsl -> { // performs insertion of categories
					CommonTableExpression<Record2<Long, Long>> category_book_tbl = DSL.name("category_book_tbl").fields("book_id", "category_id")
							.as(dsl.select(BOOK.BOOK_ID, CATEGORY.CATEGORY_ID).from(BOOK).crossJoin(CATEGORY)
								   .where( BOOK.BOOK_ID.eq(bookId).and(CATEGORY.CATEGORY_ID.in(toInsertCatIdsSet)) ));
											
					return dsl.with(category_book_tbl).insertInto(CATEGORY_BOOK, CATEGORY_BOOK.BOOK_ID, CATEGORY_BOOK.CATEGORY_ID)
						      .select(dsl.selectFrom(category_book_tbl));	
				});
				insertCBFuture.onSuccess(handler -> LOGGER.info("Succes, category-book has been inserted!"));
				insertCBFuture.onFailure(handler -> LOGGER.error("Error, category-book has NOT been inserted!"));
				promise.complete();
				return Future.succeededFuture();
			} else { // nothing changes
				return Future.succeededFuture();
			}
		});
		iterateCBFuture.onSuccess(handler -> {LOGGER.info("Success, iterateCBFuture passed!"); promise.complete();});
		iterateCBFuture.onFailure(handler -> {LOGGER.error("Error, iterateCBFuture FAILED!"); promise.fail(handler);});		
		return promise.future();
	}	
	
	// ***************************************************************************************************************
	// FIXME: re-implement iterateAuthorBook method -> REMOVE DAO objects!!!
	private static Future<Void> iterateAuthorBook(ReactiveClassicGenericQueryExecutor queryExecutor,
			Set<Long> authorUpdatedIds, long bookId) {
		Promise<Void> promise = Promise.promise();
				
		Future<Integer> iterateABFuture = queryExecutor.findManyRow(dsl -> dsl.select(AUTHOR_BOOK.AUTHOR_ID).from(AUTHOR_BOOK).where(AUTHOR_BOOK.BOOK_ID.eq(Long.valueOf(bookId)))).compose(existingAC -> {
			Set<Long> existingBAuhtorIds = BookUtilHelper.extractAuthorsFromLR(existingAC);
			Set<Long> deleteAutIdsSet = existingBAuhtorIds.stream().filter(autId -> !authorUpdatedIds.contains(autId)).collect(Collectors.toSet());
			Set<Long> toInsertAutIdsSet = authorUpdatedIds.stream().filter(catId -> !existingBAuhtorIds.contains(catId)).collect(Collectors.toSet());						
			
			if (!toInsertAutIdsSet.isEmpty() && !deleteAutIdsSet.isEmpty()) {
				 queryExecutor.execute(dsl -> dsl
					.deleteFrom(AUTHOR_BOOK).where(AUTHOR_BOOK.BOOK_ID.eq(Long.valueOf(bookId))).and(AUTHOR_BOOK.AUTHOR_ID.in(deleteAutIdsSet))
				).compose(res ->  queryExecutor.execute(dsl -> {										
					CommonTableExpression<Record2<Long, Long>> author_book_tbl = DSL.name("author_book_tbl").fields("book_id", "author_id")
						.as(dsl.select(BOOK.BOOK_ID, AUTHOR.AUTHOR_ID).from(BOOK).crossJoin(AUTHOR)
							   .where( BOOK.BOOK_ID.eq(bookId).and(AUTHOR.AUTHOR_ID.in(toInsertAutIdsSet))));
					
					return dsl.with(author_book_tbl)						
						.insertInto(AUTHOR_BOOK, AUTHOR_BOOK.BOOK_ID, AUTHOR_BOOK.AUTHOR_ID)
						.select(dsl.selectFrom(author_book_tbl));
				}));
				promise.complete();
				return Future.succeededFuture();
			} else if (!toInsertAutIdsSet.isEmpty() && deleteAutIdsSet.isEmpty()) {
				Future<Integer> insertABFuture = queryExecutor.execute(dsl -> {										
					CommonTableExpression<Record2<Long, Long>> author_book_tbl = DSL.name("author_book_tbl").fields("book_id", "author_id")
						.as(dsl.select(BOOK.BOOK_ID, AUTHOR.AUTHOR_ID).from(BOOK).crossJoin(AUTHOR)
							   .where( BOOK.BOOK_ID.eq(bookId).and(AUTHOR.AUTHOR_ID.in(toInsertAutIdsSet))));
					
					return dsl.with(author_book_tbl)						
						.insertInto(AUTHOR_BOOK, AUTHOR_BOOK.BOOK_ID, AUTHOR_BOOK.AUTHOR_ID)
						.select(dsl.selectFrom(author_book_tbl));
				});
				insertABFuture.onSuccess(handler -> LOGGER.info("Success, insertABFuture passed in iterateAuthorBook()!"));
				insertABFuture.onFailure(handler -> LOGGER.error("Error, insertABFuture FAILED in iterateAuthorBook()!"));
				promise.complete();
				return Future.succeededFuture();
			} else if (toInsertAutIdsSet.isEmpty() && !deleteAutIdsSet.isEmpty()) {				
				queryExecutor.execute(dsl -> dsl
					.deleteFrom(AUTHOR_BOOK)
					.where(AUTHOR_BOOK.BOOK_ID.eq(Long.valueOf(bookId)))
					.and(AUTHOR_BOOK.AUTHOR_ID.in(deleteAutIdsSet)));
				promise.complete();
				return Future.succeededFuture();
			} else { // nothing changes
				promise.complete();
				return Future.succeededFuture();
			}			
		});
		iterateABFuture.onSuccess(handler -> {LOGGER.info("Success, iterateABFuture passed!"); promise.complete();});
		iterateABFuture.onFailure(handler -> {LOGGER.error("Error, iterateABFuture FAILED!"); promise.fail(handler);});
		return promise.future();
	}
	
	// ***************************************************************************************************************
	// FIXME: maybe LEAVE out this method -> it's unnecessary since Book already contains 'is_deleted' field !
	public static Future<Void> deleteBookJooq(ReactiveClassicGenericQueryExecutor queryExecutor, long id) {
		Promise<Void> promise = Promise.promise();
		Future<Integer> deleteBookFuture =  queryExecutor.transaction(transactionQE -> {
			return transactionQE.execute(dsl -> dsl.delete(BOOK).where(BOOK.BOOK_ID.eq(Long.valueOf(id))));
		});
			
		deleteBookFuture.onComplete(ar -> {
			if (ar.succeeded()) {
				LOGGER.info("Success, deletion successful for Book id = " + id);
				promise.handle(Future.succeededFuture());
			} else {
				LOGGER.error("Error, deletion failed for Book id = " + id);
				Future<Void> rollbackFuture = queryExecutor.rollback();
				rollbackFuture.onSuccess(handler -> LOGGER.info("Transaction successfully rolledback!"));
				rollbackFuture.onFailure(handler -> LOGGER.info("Error, transcation did NOT rollback! Cause: " + handler.getCause()));				
				promise.handle(Future.failedFuture(new NoSuchElementException("No book with id = " + id)));
			}
		});		
		return promise.future();
	}
	

}
