package com.ns.vertx.pg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ns.vertx.pg.jooq.tables.Orders.ORDERS;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.QueryResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/* Copy-paste ADEQUATE methods from BookJooqQueries */
public class OrdersJooqQueries {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrdersJooqQueries.class);
	
	// FIXME
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
	
	// FIXME
	private static JsonObject extractOrderItemssFromQR(QueryResult queryResult){
		JsonArray orderItemsJA = new JsonArray();
		for(QueryResult qr: queryResult.asList()) {
			JsonObject orderItem = fillOrder(qr);
			orderItemsJA.add(orderItem);
		}
		return new JsonObject().put("orderItems", orderItemsJA);
	}	

	// *******************************************************************************************
	// TODO: create methods for POST and GET ALL (SAVE 1 Order and GetAllOrders)
	// *******************************************************************************************
	
	static Future<JsonObject> getAllOrdersJooq(ReactiveClassicGenericQueryExecutor queryExecutor) {
		Promise<JsonObject> finalRes = Promise.promise();	
		// FIXME: fix this query
		Future<QueryResult> ordersFuture = queryExecutor.query(dsl -> dsl
			.select()
			.from(ORDERS)			
		);	    
		
	    ordersFuture.setHandler(handler -> {
			if (handler.succeeded()) {								
				QueryResult ordersQR = handler.result();				
				JsonObject ordersJsonObject = extractOrderItemssFromQR(ordersQR);
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
	
	
}
