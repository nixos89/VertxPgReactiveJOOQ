package com.ns.vertx.pg.service;

import static com.ns.vertx.pg.jooq.tables.Orders.ORDERS;

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

public class OrderServiceImpl {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);
	
	// FIXME: adjust values for Order(S)!!!
	private static JsonObject fillOrder(QueryResult booksQR) {
		return new JsonObject()
			.put("bookId", booksQR.get("b_id", Long.class))
			.put("title", booksQR.get("title", String.class))
			.put("price", booksQR.get("price", Double.class))
			.put("amount", booksQR.get("amount", Integer.class))
			.put("isDeleted", booksQR.get("is_deleted", Boolean.class))
			.put("authors", booksQR.get("authors", JsonArray.class))
			.put("categories", booksQR.get("categories", JsonArray.class));				
	}
		
		// FIXME: fix this
	private static JsonObject extractBooksFromLR(List<Row> booksLR){		
		JsonObject orderJO = new JsonObject();
		JsonObject categoryJO = new JsonObject();
		JsonObject authorJO = new JsonObject();
		JsonArray ordersJA = new JsonArray();
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

	// FIXME: GetAllOrders 	
	public static Future<JsonObject> getAllOrdersJooq(ReactiveClassicGenericQueryExecutor queryExecutor) {
		Promise<JsonObject> finalRes = Promise.promise();	
		// FIXME: fix this query to ALSO collect orderItems as well as Books with its Authors and Categories 
		Future<List<Row>> ordersFuture = queryExecutor.transaction(qe -> qe
			.findManyRow(dsl -> dsl
				.select()
				.from(ORDERS)
			));	    
		
	    ordersFuture.onComplete(handler -> {
			if (handler.succeeded()) {								
				List<Row> ordersLR = handler.result();				
				JsonObject ordersJsonObject = extractBooksFromLR(ordersLR);
				LOGGER.info("ordersJsonObject.encodePrettily(): " + ordersJsonObject.encodePrettily());
				finalRes.complete(ordersJsonObject);
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening ALL orders! Cause: " 
	    				+ handler.cause().getMessage());
	    		finalRes.fail(handler.cause());
	    	}
	    }); 
		
		return finalRes.future();
	}
	
	

	public static Future<JsonObject> createOrderJooq(ReactiveClassicGenericQueryExecutor queryExecutor, JsonObject orderJO, String username) {
		Promise<JsonObject> finalRes = Promise.promise();
		// TODO: implement this method
		
		
		return finalRes.future();
	}
	
		
		
}
