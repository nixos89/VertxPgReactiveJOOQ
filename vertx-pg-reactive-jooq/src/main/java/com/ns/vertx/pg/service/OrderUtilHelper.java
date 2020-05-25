package com.ns.vertx.pg.service;

import java.util.List;

import io.github.jklingsporn.vertx.jooq.shared.internal.QueryResult;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

public class OrderUtilHelper {

	// FIXME: adjust values for Order(S)!!!
	static JsonObject fillOrder(QueryResult booksQR) {
		return new JsonObject()
			.put("bookId", booksQR.get("b_id", Long.class))
			.put("title", booksQR.get("title", String.class))
			.put("price", booksQR.get("price", Double.class))
			.put("amount", booksQR.get("amount", Integer.class))
			.put("isDeleted", booksQR.get("is_deleted", Boolean.class))
			.put("authors", booksQR.get("authors", JsonArray.class))
			.put("categories", booksQR.get("categories", JsonArray.class));				
	}
			
	// FIXME: fix extractBooksFromLR method
	static JsonObject extractBooksFromLR(List<Row> booksLR){		
		JsonObject orderJO = new JsonObject();		
		JsonArray ordersJA = new JsonArray();
		JsonObject orderItemJO = new JsonObject();
		JsonObject categoryJO = new JsonObject();
		JsonObject authorJO = new JsonObject();
		JsonArray categoriesJA = new JsonArray();
		JsonArray authorsJA = new JsonArray(); 
		
		for (Row row : booksLR) {
			orderJO.put("order_id", row.getLong("book_id"));
			orderJO.put("order_date", row.getString("order_date"));
			orderJO.put("total_price", row.getDouble("total"));
			orderJO.put("user_id", row.getLong("user_id"));						
			ordersJA.add(orderJO);
			orderJO.clear();
		}
		JsonObject joBooks= new JsonObject().put("orders", ordersJA);		
		return joBooks;
	}
	
	
}
