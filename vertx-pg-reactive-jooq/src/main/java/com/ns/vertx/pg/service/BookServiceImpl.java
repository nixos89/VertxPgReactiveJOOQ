package com.ns.vertx.pg.service;

import static com.ns.vertx.pg.jooq.tables.Author.AUTHOR;
import static com.ns.vertx.pg.jooq.tables.AuthorBook.AUTHOR_BOOK;
import static com.ns.vertx.pg.jooq.tables.Book.BOOK;
import static com.ns.vertx.pg.jooq.tables.Category.CATEGORY;
import static com.ns.vertx.pg.jooq.tables.CategoryBook.CATEGORY_BOOK;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.jooq.CommonTableExpression;
import org.jooq.Configuration;
import org.jooq.InsertOnDuplicateStep;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.jooq.tables.daos.AuthorBookDao;
import com.ns.vertx.pg.jooq.tables.daos.BookDao;
import com.ns.vertx.pg.jooq.tables.daos.CategoryBookDao;
import com.ns.vertx.pg.jooq.tables.pojos.AuthorBook;
import com.ns.vertx.pg.jooq.tables.pojos.Book;
import com.ns.vertx.pg.jooq.tables.pojos.CategoryBook;
import com.ns.vertx.pg.jooq.tables.records.AuthorBookRecord;
import com.ns.vertx.pg.jooq.tables.records.BookRecord;
import com.ns.vertx.pg.jooq.tables.records.CategoryBookRecord;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.QueryResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;


public class BookServiceImpl {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BookServiceImpl.class);
	
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
	
	// ******************************************************************************** 
	
	private static JsonObject extractBooksFromQR(QueryResult queryResult) {
		JsonArray booksJA = new JsonArray();
		for(QueryResult qr: queryResult.asList()) {
			JsonObject book = fillBook(qr);
			booksJA.add(book);
		}
		return new JsonObject().put("books", booksJA);
	}	
	
	// ********************************************************************************
	
	private static JsonObject extractBookFromRS(RowSet<Row> rowSetBook){
		LOGGER.info("rowSetBook.columnsNames(): " + rowSetBook.columnsNames());
		
		RowIterator<Row> rowIterator = rowSetBook.iterator();
		JsonObject bookJO = new JsonObject(); 
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			bookJO.put("book_id", row.getLong("book_id"));
			bookJO.put("title", row.getString("title"));
			bookJO.put("amount", row.getInteger("amount"));
			bookJO.put("price", row.getDouble("price"));
			bookJO.put("is_deleted", row.getBoolean("is_deleted"));	
		}
		return bookJO;
	}
	
	// ********************************************************************************
	
	private static JsonObject extractBooksFromLR(List<Row> booksLR){		
		JsonObject bookJO = new JsonObject();
		JsonObject categoryJO = new JsonObject();
		JsonArray booksJA = new JsonArray();
//		JsonObject authorJO = new JsonObject();
//		JsonArray categoriesJA = new JsonArray();
//		JsonArray authorsJA = new JsonArray(); 
		
		for (Row row : booksLR) {
			bookJO.put("book_id", row.getLong("book_id"));
			bookJO.put("title", row.getString("title"));
			bookJO.put("amount", row.getInteger("amount"));
			bookJO.put("price", row.getDouble("price"));
			bookJO.put("price", row.getDouble("price"));
			categoryJO.put("category_id", row.getLong("category_id"));
			categoryJO.put("name", row.getString("name"));
			categoryJO.put("is_deleted", row.getString("is_deleted"));
			
			booksJA.add(bookJO);
			bookJO.clear();
		}
		JsonObject joBooks= new JsonObject().put("books", booksJA);		
		return joBooks;
	}
	
	// *********************************************************************************************************************************
	
	public static Future<JsonObject> getAllBooksJooq(ReactiveClassicGenericQueryExecutor queryExecutor) {
		Promise<JsonObject> finalRes = Promise.promise();					
		Future<QueryResult> bookFuture = queryExecutor.transaction(qe -> {			
			return qe.query(dsl -> dsl.resultQuery(DBQueries.GET_ALL_BOOKS));
		});				
		
	    bookFuture.onComplete(handler -> {
			if (handler.succeeded()) {								
				QueryResult booksQR = handler.result();				
				JsonObject booksJsonObject = extractBooksFromQR(booksQR);
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
				// FIXME: must use DISTINCT for AUTHORS and CATEGORIES to be able to return ALL of them in 1 ROW!!!
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
				JsonObject booksJsonObject = extractBooksFromLR(booksLR);
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
				JsonObject booksJsonObject = extractBooksFromQR(booksByAuthorIdQR);
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
					JsonObject bookJsonObject = fillBook(booksQR);				
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
	
	public static Future<Void> createBookJooq(ReactiveClassicGenericQueryExecutor queryExecutor, JsonObject bookJO,  
			Configuration configuration, PgPool pgClient, AuthorBookDao authorBookDAO, CategoryBookDao categoryBookDAO) {
		Promise<Void> promise = Promise.promise();			
		Book bookPojo = new Book(bookJO); 
		
		Future<Void> transactionFuture = queryExecutor.beginTransaction().compose(transactionQE-> 
			transactionQE.executeAny(dsl -> dsl		
				.insertInto(BOOK, BOOK.TITLE, BOOK.AMOUNT, BOOK.PRICE, BOOK.IS_DELETED)
				.values(bookPojo.getTitle(), bookPojo.getAmount(), bookPojo.getPrice(), bookPojo.getIsDeleted())
				.returningResult(BOOK.BOOK_ID, BOOK.TITLE, BOOK.AMOUNT, BOOK.PRICE, BOOK.IS_DELETED)
			).compose(insertedBook -> { 				
				JsonObject resultJO =  extractBookFromRS(insertedBook);
				final Long bookId = resultJO.getLong("book_id");
				LOGGER.info("saved book:\n" + resultJO.encodePrettily());
				
				List<Long> authorIds = bookJO.getJsonArray("author_ids").stream()
						.mapToLong(a -> Long.valueOf(String.valueOf(a))).boxed().collect(Collectors.toList());								
							
				List<Long> categoryIds = bookJO.getJsonArray("category_ids").stream()
						.mapToLong(c -> Long.valueOf(String.valueOf(c))).boxed().collect(Collectors.toList());																	
								
				return transactionQE.execute(dsl -> {										
					CommonTableExpression<Record2<Long, Long>> author_book_tbl = DSL.name("author_book_tbl").fields("book_id", "author_id")
							.as(dsl.select(BOOK.BOOK_ID, AUTHOR.AUTHOR_ID).from(BOOK)
								   .crossJoin(AUTHOR).where( BOOK.BOOK_ID.eq(bookId).and(AUTHOR.AUTHOR_ID.in(authorIds)) ));
										
					return dsl.with(author_book_tbl)						
						.insertInto(AUTHOR_BOOK, AUTHOR_BOOK.BOOK_ID, AUTHOR_BOOK.AUTHOR_ID)
						.select(dsl.selectFrom(author_book_tbl));
				}
					).compose(res -> {
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
				LOGGER.info("Maybeeee all went well...");
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
	
	// FIXME: re-implement iterateCategoryBook method -> REMOVE DAO objects!!!
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
				return categoryBookDAO.insert(bookCategories); // FIXME: use 'queryExecutor' object instead of categoryBookDAO
			} else {
				return Future.succeededFuture();
			}
		});
	}	
	
	// ***************************************************************************************************************
	// FIXME: re-implement iterateAuthorBook method -> REMOVE DAO objects!!!
	private static Future<Integer> iterateAuthorBook(ReactiveClassicGenericQueryExecutor queryExecutor,
			AuthorBookDao authorBookDAO, Set<Long> authorUpdatedIds, long bookId) {
		
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
				return authorBookDAO.insert(bookAuthors); // FIXME: use 'queryExecutor' object instead of authorBookDAO
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
	
	public static Future<Void> deleteBookJooq(ReactiveClassicGenericQueryExecutor queryExecutor, long id) {
		Promise<Void> promise = Promise.promise();
		Future<Integer> deleteBookFuture =  queryExecutor.transaction(qe -> {
			return qe.execute(dsl -> dsl.delete(BOOK).where(BOOK.BOOK_ID.eq(Long.valueOf(id))));
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
