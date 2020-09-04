package com.ns.vertx.pg.service;

import static com.ns.vertx.pg.jooq.tables.Author.AUTHOR;
import static com.ns.vertx.pg.jooq.tables.AuthorBook.AUTHOR_BOOK;
import static com.ns.vertx.pg.jooq.tables.Book.BOOK;
import static com.ns.vertx.pg.jooq.tables.Category.CATEGORY;
import static com.ns.vertx.pg.jooq.tables.CategoryBook.CATEGORY_BOOK;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.jooq.CommonTableExpression;
import org.jooq.Configuration;
import org.jooq.JSON;
import org.jooq.Record2;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.jooq.tables.pojos.Book;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.QueryResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.SqlConnection;


public class BookServiceImpl implements BookService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BookServiceImpl.class);	
	private ReactiveClassicGenericQueryExecutor queryExecutor;
		
	public BookServiceImpl(PgPool pgClient, Configuration configuration, Handler<AsyncResult<BookService>> readyHandler) {
		pgClient.getConnection(ar -> {
			if (ar.failed()) {
				LOGGER.error("Could NOT OPEN DB connection!", ar.cause());
				readyHandler.handle(Future.failedFuture(ar.cause()));
			} else {
				SqlConnection connection = ar.result();						
				this.queryExecutor = new ReactiveClassicGenericQueryExecutor(configuration, pgClient);
				LOGGER.info("+++++ Connection succeded and queryExecutor instantiation is SUCCESSFUL! +++++");
				connection.close();		
				readyHandler.handle(Future.succeededFuture(this));
			}
		});		
	}		
	
	// ************************************************************************************************
	// ******************************* BookService CRUD methods *********************************** 
	// ************************************************************************************************	
	@Override
	public BookService getAllBooksJooqSP(Handler<AsyncResult<JsonObject>> resultHandler) {
		Future<QueryResult> bookFuture = queryExecutor.transaction(transactionQE -> {			
			return transactionQE.query(dsl -> dsl				
					.select(BOOK.BOOK_ID.as("b_id"), BOOK.TITLE, BOOK.PRICE, BOOK.AMOUNT, BOOK.IS_DELETED, 
							DSL.field( "to_json(array_agg(DISTINCT {0}.*))", JSON.class, AUTHOR).as("authors"),
							DSL.field("to_json(array_agg(DISTINCT {0}.*))", JSON.class, CATEGORY).as("categories")
					).from(BOOK
						.leftJoin(AUTHOR_BOOK).on(BOOK.BOOK_ID.eq(AUTHOR_BOOK.BOOK_ID))
						.leftJoin(AUTHOR).on(AUTHOR_BOOK.AUTHOR_ID.eq(AUTHOR.AUTHOR_ID))
						.leftJoin(CATEGORY_BOOK).on(BOOK.BOOK_ID.eq(CATEGORY_BOOK.BOOK_ID))
						.leftJoin(CATEGORY).on(CATEGORY_BOOK.CATEGORY_ID.eq(CATEGORY.CATEGORY_ID))
					).groupBy(BOOK.BOOK_ID).orderBy(BOOK.BOOK_ID.asc())
				);
		});						
	    bookFuture.onComplete(handler -> {
			if (handler.succeeded()) {								
				QueryResult booksQR = handler.result();				
				JsonObject booksJsonObject = BookUtilHelper.extractBooksFromQR(booksQR);
				resultHandler.handle(Future.succeededFuture(booksJsonObject));
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening ALL books! Cause: " 
	    				+ handler.cause().getMessage());
	    		queryExecutor.rollback();
	    		resultHandler.handle(Future.failedFuture(new NoSuchElementException("There are no saved books in database!")));
	    	}
	    }); 		
		return this;
	}


	@Override
	public BookService getBookByIdJooqSP(Long bookId, Handler<AsyncResult<JsonObject>> resultHandler) {
		Future<QueryResult> bookFuture = queryExecutor.transaction(transactionQE -> {
			return transactionQE.query(dsl -> dsl				
					.select(BOOK.BOOK_ID.as("b_id"), BOOK.TITLE, BOOK.PRICE, BOOK.AMOUNT, BOOK.IS_DELETED, 
							DSL.field( "to_json(array_agg(DISTINCT {0}.*))", JSON.class, AUTHOR).as("authors"),
							DSL.field("to_json(array_agg(DISTINCT {0}.*))", JSON.class, CATEGORY).as("categories")
					).from(BOOK
						.leftJoin(AUTHOR_BOOK).on(BOOK.BOOK_ID.eq(AUTHOR_BOOK.BOOK_ID))
						.leftJoin(AUTHOR).on(AUTHOR_BOOK.AUTHOR_ID.eq(AUTHOR.AUTHOR_ID))
						.leftJoin(CATEGORY_BOOK).on(BOOK.BOOK_ID.eq(CATEGORY_BOOK.BOOK_ID))
						.leftJoin(CATEGORY).on(CATEGORY_BOOK.CATEGORY_ID.eq(CATEGORY.CATEGORY_ID))
					).where(BOOK.BOOK_ID.eq(bookId)
					).groupBy(BOOK.BOOK_ID).orderBy(BOOK.BOOK_ID.asc())
				);			    
		});								    
	    bookFuture.onComplete(handler -> {
			if (handler.succeeded()) {									
				QueryResult booksQR = handler.result();				
				if (booksQR != null) {
					JsonObject bookJsonObject = BookUtilHelper.fillBook(booksQR);
					if (bookJsonObject == null) {
						resultHandler.handle(Future.failedFuture(new NoSuchElementException(
								"Error, no book has been found in DB for book_id = " + bookId)));
					}  else {
						resultHandler.handle(Future.succeededFuture(bookJsonObject));	
					}					
				} else {				
					resultHandler.handle(Future.succeededFuture(
							new JsonObject().put("message", "Book with id " + bookId + " does NOT exist in DB!")));
				}				
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening query by book_id = " + bookId);
	    		queryExecutor.rollback();
	    		resultHandler.handle(Future.failedFuture(handler.cause()));
	    	}
	    }); 
		return this;
	}

	
	@Override
	public BookService getAllBooksByAuthorIdJooqSP(Long authorId, Handler<AsyncResult<JsonObject>> resultHandler) {					
		Future<QueryResult> authorBooksFuture = queryExecutor.transaction(transactionQE -> {			
			return transactionQE.query(dsl -> dsl				
				.select(BOOK.BOOK_ID.as("b_id"), BOOK.TITLE, BOOK.PRICE, BOOK.AMOUNT, BOOK.IS_DELETED, 
						DSL.field( "to_json(array_agg(DISTINCT {0}.*))", JSON.class, AUTHOR).as("authors"),
						DSL.field("to_json(array_agg(DISTINCT {0}.*))", JSON.class, CATEGORY).as("categories")
				).from(BOOK
					.leftJoin(AUTHOR_BOOK).on(BOOK.BOOK_ID.eq(AUTHOR_BOOK.BOOK_ID))
					.leftJoin(AUTHOR).on(AUTHOR_BOOK.AUTHOR_ID.eq(AUTHOR.AUTHOR_ID))
					.leftJoin(CATEGORY_BOOK).on(BOOK.BOOK_ID.eq(CATEGORY_BOOK.BOOK_ID))
					.leftJoin(CATEGORY).on(CATEGORY_BOOK.CATEGORY_ID.eq(CATEGORY.CATEGORY_ID))
				).where(AUTHOR.AUTHOR_ID.eq(Long.valueOf(authorId))
				).groupBy(BOOK.BOOK_ID).orderBy(BOOK.BOOK_ID.asc())
			);
		});						
		authorBooksFuture.onComplete(handler -> {
			if (handler.succeeded()) {								
				QueryResult booksQR = handler.result();
				JsonObject booksJsonObject = BookUtilHelper.extractBooksFromQR(booksQR);
				LOGGER.info("bookJsonObject.encodePrettily(): " + booksJsonObject.encodePrettily());
				resultHandler.handle(Future.succeededFuture(booksJsonObject));
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening ALL books! Cause: " 
	    				+ handler.cause().getMessage());
	    		queryExecutor.rollback();
	    		resultHandler.handle(Future.failedFuture(handler.cause()));
	    	}
	    }); 		
		return this;
	}	

	@Override
	public BookService createBookJooqSP(JsonObject bookJO, Handler<AsyncResult<Void>> resultHandler) {
		Book bookPojo = new Book(bookJO); 		
		Future<Void> transactionFuture = queryExecutor.beginTransaction().compose(transactionQE -> 
			transactionQE.executeAny(dsl -> dsl		
				.insertInto(BOOK, BOOK.TITLE, BOOK.AMOUNT, BOOK.PRICE, BOOK.IS_DELETED)
				.values(bookPojo.getTitle(), bookPojo.getAmount(), bookPojo.getPrice(), bookJO.getBoolean("isDeleted"))
				.returningResult(BOOK.BOOK_ID, BOOK.TITLE, BOOK.AMOUNT, BOOK.PRICE, BOOK.IS_DELETED)
			).compose(insertedBook -> { 				
				JsonObject resultJO =  BookUtilHelper.extractSingleBookFromRS(insertedBook);
				final Long bookId = resultJO.getLong("book_id");
				
				Set<Long> authorIds = bookJO.getJsonArray("authors").stream().mapToLong(a -> Long.valueOf(String.valueOf(a))).boxed().collect(Collectors.toSet());															
				Set<Long> categoryIds = bookJO.getJsonArray("categories").stream().mapToLong(c -> Long.valueOf(String.valueOf(c))).boxed().collect(Collectors.toSet());																	
								
				return transactionQE.execute(dsl -> {			
					CommonTableExpression<Record2<Long, Long>> author_book_tbl = BookUtilHelper.author_book_tbl(dsl, bookId, authorIds);					
					return dsl.with(author_book_tbl)						
						.insertInto(AUTHOR_BOOK, AUTHOR_BOOK.BOOK_ID, AUTHOR_BOOK.AUTHOR_ID)
						.select(dsl.selectFrom(author_book_tbl));
				}).compose(res -> {
					return transactionQE.execute(dsl -> { 	
						CommonTableExpression<Record2<Long, Long>> category_book_tbl = BookUtilHelper.category_book_tbl(dsl, bookId, categoryIds);											
						return dsl.with(category_book_tbl)									
							.insertInto(CATEGORY_BOOK, CATEGORY_BOOK.BOOK_ID, CATEGORY_BOOK.CATEGORY_ID)
						    .select(dsl.selectFrom(category_book_tbl));					
					}); 
				}).compose(success -> {
						return transactionQE.commit();
					}, failure -> {
						LOGGER.info("Oops, rolling-back transcation...");
						return transactionQE.rollback();
					});		
		}));		
		transactionFuture.onComplete(handler -> {
			if (handler.succeeded()) {
				resultHandler.handle(Future.succeededFuture());
			} else {
				LOGGER.error("Error, somethin' WENT WRRRONG!! handler.result() = " + handler.cause());
				resultHandler.handle(Future.failedFuture(handler.cause()));
			}
		});
		return this;
	}


	@Override
	public BookService updateBookJooqSP(JsonObject bookJO, Handler<AsyncResult<Void>> resultHandler) {
		Set<Long> authorUpdatedIds = bookJO.getJsonArray("authors").stream()
				.mapToLong(a -> Long.valueOf(String.valueOf(a))).boxed().collect(Collectors.toSet());	
		
		Set<Long> categoryUpdatedIds = bookJO.getJsonArray("categories").stream()
				.mapToLong(a -> Long.valueOf(String.valueOf(a))).boxed().collect(Collectors.toSet());		
		Long bookId = bookJO.getLong("book_id"); 		
		
		Book bookPojo = new Book(bookJO);
		queryExecutor.beginTransaction().compose(transcationQE -> 
			transcationQE.execute( dsl -> dsl
				.update(BOOK).set(BOOK.TITLE, bookPojo.getTitle())
				.set(BOOK.PRICE, bookPojo.getPrice()).set(BOOK.AMOUNT, bookPojo.getAmount()).set(BOOK.IS_DELETED, bookJO.getBoolean("isDeleted"))
				.where(BOOK.BOOK_ID.eq(Long.valueOf(bookId)))
			).compose(res -> CompositeFuture.all(
					iterateCategoryBook(transcationQE, categoryUpdatedIds, bookId), 
					iterateAuthorBook(transcationQE, authorUpdatedIds, bookId)		
				).compose(success -> {
					  return transcationQE.commit();
				  }, failure -> {
					  return transcationQE.rollback()
							  .onSuccess(handler -> LOGGER.debug("Transaction successfully rolled-back!"))
							  .onFailure(handler -> LOGGER.error("Error, transaction FAILED to rollback!"));
				  })
			))
			.onSuccess(handler -> resultHandler.handle(Future.succeededFuture()))
			.onFailure(handler -> resultHandler.handle(Future.failedFuture(handler)));		
		return this;
	}
		
	// ***********************************************************************************************************************************************
	
	private Future<Void> iterateCategoryBook(ReactiveClassicGenericQueryExecutor queryExecutor, Set<Long> categoryUpdatedIds, long bookId) {
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
				return queryExecutor.execute(dsl -> dsl
						.deleteFrom(CATEGORY_BOOK)
						.where(CATEGORY_BOOK.BOOK_ID.eq(Long.valueOf(bookId)))
						.and(CATEGORY_BOOK.CATEGORY_ID.in(deleteCatIdsSet)))
					.compose(res -> queryExecutor.execute(dsl -> { // performs insertion of categories
						CommonTableExpression<Record2<Long, Long>> category_book_tbl = BookUtilHelper.category_book_tbl(dsl, bookId, toInsertCatIdsSet);											
						return dsl.with(category_book_tbl)									
							.insertInto(CATEGORY_BOOK, CATEGORY_BOOK.BOOK_ID, CATEGORY_BOOK.CATEGORY_ID)
						    .select(dsl.selectFrom(category_book_tbl));	
					})).mapEmpty(); 
			} else if (toInsertCatIdsSet.isEmpty() && !deleteCatIdsSet.isEmpty()) {				
				return queryExecutor.execute(dsl -> dsl.deleteFrom(CATEGORY_BOOK)
						.where(CATEGORY_BOOK.BOOK_ID.eq(Long.valueOf(bookId))).and(CATEGORY_BOOK.CATEGORY_ID.in(deleteCatIdsSet))).mapEmpty();								
			} else if (!toInsertCatIdsSet.isEmpty() && deleteCatIdsSet.isEmpty()) {
				return queryExecutor.execute(dsl -> { // performs insertion of categories
					CommonTableExpression<Record2<Long, Long>> category_book_tbl = BookUtilHelper.category_book_tbl(dsl, bookId, toInsertCatIdsSet);
											
					return dsl.with(category_book_tbl).insertInto(CATEGORY_BOOK, CATEGORY_BOOK.BOOK_ID, CATEGORY_BOOK.CATEGORY_ID)
						      .select(dsl.selectFrom(category_book_tbl));	
				}).onFailure(handler -> LOGGER.error("Error, FAILURE in !toInsertCatIdsSet.isEmpty() && deleteCatIdsSet.isEmpty()")).mapEmpty();
			} else { // nothing changes
				return Future.succeededFuture();
			}
		});
		iterateCBFuture.onSuccess(handler -> promise.complete());
		iterateCBFuture.onFailure(handler -> {LOGGER.error("Error, iterateCBFuture FAILED!"); promise.fail(handler);});		
		return promise.future();
	}	
	

	private Future<Void> iterateAuthorBook(ReactiveClassicGenericQueryExecutor queryExecutor, Set<Long> authorUpdatedIds, long bookId) {
		Promise<Void> promise = Promise.promise();				
		Future<Integer> iterateABFuture = queryExecutor.findManyRow(dsl -> dsl.select(AUTHOR_BOOK.AUTHOR_ID).from(AUTHOR_BOOK)
				.where(AUTHOR_BOOK.BOOK_ID.eq(Long.valueOf(bookId)))).compose(existingAC -> {
			Set<Long> existingBAuthorIds = BookUtilHelper.extractAuthorsFromLR(existingAC);
			Set<Long> deleteAutIdsSet = existingBAuthorIds.stream().filter(autId -> !authorUpdatedIds.contains(autId)).collect(Collectors.toSet());
			Set<Long> toInsertAutIdsSet = authorUpdatedIds.stream().filter(catId -> !existingBAuthorIds.contains(catId)).collect(Collectors.toSet());						
			
			if (!toInsertAutIdsSet.isEmpty() && !deleteAutIdsSet.isEmpty()) {
				 return queryExecutor.execute(dsl -> dsl
					.deleteFrom(AUTHOR_BOOK).where(AUTHOR_BOOK.BOOK_ID.eq(Long.valueOf(bookId))).and(AUTHOR_BOOK.AUTHOR_ID.in(deleteAutIdsSet))
				).compose(res ->  queryExecutor.execute(dsl -> {										
					CommonTableExpression<Record2<Long, Long>> author_book_tbl = BookUtilHelper.author_book_tbl(dsl, bookId, toInsertAutIdsSet);
					
					return dsl.with(author_book_tbl)						
						.insertInto(AUTHOR_BOOK, AUTHOR_BOOK.BOOK_ID, AUTHOR_BOOK.AUTHOR_ID)
						.select(dsl.selectFrom(author_book_tbl));
				})).mapEmpty();				
			} else if (!toInsertAutIdsSet.isEmpty() && deleteAutIdsSet.isEmpty()) {
				return queryExecutor.execute(dsl -> {										
					CommonTableExpression<Record2<Long, Long>> author_book_tbl = BookUtilHelper.author_book_tbl(dsl, bookId, toInsertAutIdsSet);
					
					return dsl.with(author_book_tbl)						
						.insertInto(AUTHOR_BOOK, AUTHOR_BOOK.BOOK_ID, AUTHOR_BOOK.AUTHOR_ID)
						.select(dsl.selectFrom(author_book_tbl));
				}).mapEmpty();
			} else if (toInsertAutIdsSet.isEmpty() && !deleteAutIdsSet.isEmpty()) {				
				return queryExecutor.execute(dsl -> dsl
					.deleteFrom(AUTHOR_BOOK).where(AUTHOR_BOOK.BOOK_ID.eq(Long.valueOf(bookId))).and(AUTHOR_BOOK.AUTHOR_ID.in(deleteAutIdsSet)))
					.mapEmpty();				
			} else { // nothing changes
				return Future.succeededFuture();
			}			
		});
		iterateABFuture.onSuccess(handler -> promise.complete());
		iterateABFuture.onFailure(handler -> {LOGGER.error("Error, iterateABFuture FAILED!"); promise.fail(handler);});
		return promise.future();
	}
	

	

}
