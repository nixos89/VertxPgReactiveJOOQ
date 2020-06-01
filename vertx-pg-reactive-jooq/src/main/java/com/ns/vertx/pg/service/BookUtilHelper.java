package com.ns.vertx.pg.service;

import static com.ns.vertx.pg.jooq.tables.Author.AUTHOR;
import static com.ns.vertx.pg.jooq.tables.Book.BOOK;
import static com.ns.vertx.pg.jooq.tables.Category.CATEGORY;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jooq.CommonTableExpression;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.jooq.tables.pojos.Book;

import io.github.jklingsporn.vertx.jooq.shared.internal.QueryResult;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;

public class BookUtilHelper {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(BookUtilHelper.class);
	
	static JsonObject fillBook(QueryResult booksQR) {
		return new JsonObject()
			.put("book_id", booksQR.get("b_id", Long.class))
			.put("title", booksQR.get("title", String.class))
			.put("price", booksQR.get("price", Double.class))
			.put("amount", booksQR.get("amount", Integer.class))
			.put("is_deleted", booksQR.get("is_deleted", Boolean.class))
			.put("authors", booksQR.get("authors", JsonArray.class))
			.put("categories", booksQR.get("categories", JsonArray.class));						
	}
	
	static JsonObject extractBooksFromQR(QueryResult queryResult) {
		JsonArray booksJA = new JsonArray();
		if (queryResult == null) {
			return null;
		}
		for (QueryResult qr : queryResult.asList()) {
			JsonObject book = fillBook(qr);
			booksJA.add(book);
		}
		return new JsonObject().put("books", booksJA);
	}

	static JsonObject extractBooksFromLR(List<Row> booksLR){		
		JsonObject bookJO = new JsonObject();
		JsonObject categoryJO = new JsonObject();
		JsonObject authorJO = new JsonObject();
		JsonArray booksJA = new JsonArray();		
		for (Row row : booksLR) {
			bookJO.put("book_id", row.getLong("book_id"));
			bookJO.put("title", row.getString("title"));
			bookJO.put("amount", row.getInteger("amount"));
			bookJO.put("price", row.getDouble("price"));
			bookJO.put("is_deleted", row.getBoolean("is_deleted"));
			categoryJO.put("category_id", row.getLong("category_id"));
			categoryJO.put("name", row.getString("name"));
			categoryJO.put("is_deleted", row.getString("is_deleted"));
			
			booksJA.add(bookJO);
			bookJO.clear();
		}
		JsonObject joBooks= new JsonObject().put("books", booksJA);		
		return joBooks;
	}
	
	static JsonObject extractSingleBookFromRS(RowSet<Row> rowSetBook){		
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
	
	
	static List<Book> extractBookPojosFromRS(RowSet<Row> rowSetBook){
		if(rowSetBook.size() == 0) { LOGGER.info("rowSetBook is EMPTY!!!");}
		RowIterator<Row> rowIterator = rowSetBook.iterator();
		List<Book> books = new ArrayList<>();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			JsonObject bookJO = new JsonObject();
			bookJO.put("book_id", row.getLong("book_id"));
			bookJO.put("title", row.getString("title"));
			bookJO.put("amount", row.getInteger("amount"));
			bookJO.put("price", row.getDouble("price"));
			bookJO.put("is_deleted", row.getBoolean("is_deleted"));
			books.add(new Book(bookJO));
		}
		return books;
	}
	
	
	static JsonObject extractManyBooksFromRS(RowSet<Row> rowSetBook){		
		RowIterator<Row> rowIterator = rowSetBook.iterator();
		JsonArray bookJA = new JsonArray();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			JsonObject bookJO = new JsonObject();
			bookJO.put("book_id", row.getLong("book_id"));
			bookJO.put("title", row.getString("title"));
			bookJO.put("amount", row.getInteger("amount"));
			bookJO.put("price", row.getDouble("price"));
			bookJO.put("is_deleted", row.getBoolean("is_deleted"));
			bookJA.add(bookJO);
		}
		return new JsonObject().put("books", bookJA);
	}
	
	
	static Set<Long> extractCategoriesFromLR(List<Row> categoryLR){		
		Set<Long> categoryIds = new HashSet<>();
		for (Row row : categoryLR) {
			categoryIds.add(row.getLong("category_id"));
		}
		return categoryIds;
	}
	
	static Set<Long> extractAuthorsFromLR(List<Row> authorLR){		
		Set<Long> authorIds = new HashSet<>();
		for (Row row : authorLR) {
			authorIds.add(row.getLong("author_id"));
		}
		return authorIds;
	}

	// **************************************************
	// ************ jOOQ helper methods *****************
	// **************************************************	
	static CommonTableExpression<Record2<Long, Long>> author_book_tbl(DSLContext dsl, Long bookId, Set<Long> toInsertAutIdsSet){
		return DSL.name("author_book_tbl").fields("book_id", "author_id")
				  .as(dsl.select(BOOK.BOOK_ID, AUTHOR.AUTHOR_ID).from(BOOK).crossJoin(AUTHOR)
						 .where( BOOK.BOOK_ID.eq(bookId).and(AUTHOR.AUTHOR_ID.in(toInsertAutIdsSet))));
	}
	
	
	static CommonTableExpression<Record2<Long, Long>> category_book_tbl(DSLContext dsl, Long bookId, Set<Long> toInsertCatIdsSet){
		return DSL.name("category_book_tbl").fields("book_id", "category_id")
				  .as(dsl.select(BOOK.BOOK_ID, CATEGORY.CATEGORY_ID).from(BOOK).crossJoin(CATEGORY)
						 .where( BOOK.BOOK_ID.eq(bookId).and(CATEGORY.CATEGORY_ID.in(toInsertCatIdsSet)) ));
	}
	
	
	@SuppressWarnings("unchecked")
	static CommonTableExpression<Record2<Long, Integer>> bookAmountTbl(DSLContext dsl, Map<Long, Integer> bookIdAmountMap) {
		Row2<Long,Integer> array[] = new Row2[0];
		int i = 0;
		for (Map.Entry<Long, Integer> pair : bookIdAmountMap.entrySet()) {
			array[i]=DSL.row(pair.getKey(), pair.getValue());
			i++;
		}
		Table<Record2<Long, Integer>> batTmp = DSL.values(array);
//		CommonTableExpression<Record2<Long, Integer>> batTmp2 = DSL.name("btaCte").fields("book_id, amount").as(DSL.rowsFrom(batTmp));
		
		CommonTableExpression<Record2<Long, Integer>> cte = (CommonTableExpression<Record2<Long, Integer>>) batTmp;
		cte.fields("book_id", "amount");
		LOGGER.info("batTmp.toString() = " + batTmp.toString());		
//		CommonTableExpression<Record2<Long, Integer>> 
		return cte;		
	}
	
	@SuppressWarnings("unchecked")
	static Table<Record2<Long, Integer>> batTmp(DSLContext dsl, Map<Long, Integer> bookIdAmountMap) {
		List<Row2<Long,Integer>> list = new ArrayList<>();
		LOGGER.info("bookIdAmountMap.size() = " + bookIdAmountMap.size());
		Row2<Long,Integer> array[] = new Row2[bookIdAmountMap.size()];
		int i = 0;
		for (Map.Entry<Long, Integer> pair : bookIdAmountMap.entrySet()) {
			list.add(DSL.row(pair.getKey(), pair.getValue()));
			array[i]=DSL.row(pair.getKey(), pair.getValue());
			i++;
		}
		Table<Record2<Long, Integer>> batTmp = DSL.values(list.toArray(array));	
		return batTmp.as("batTmp", "book_id", "amount");
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
