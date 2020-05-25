package com.ns.vertx.pg.service;

import static com.ns.vertx.pg.jooq.tables.Orders.ORDERS;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

public class OrderServiceImpl {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);	
	
	public static Future<JsonObject> createOrderJooq(ReactiveClassicGenericQueryExecutor queryExecutor, JsonObject orderJO, String username) {
		Promise<JsonObject> finalRes = Promise.promise();
		// TODO: implement this method -> use TRANSACTIONS!!!
				
		
		return finalRes.future();
	}
	
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
				JsonObject ordersJsonObject = OrderUtilHelper.extractBooksFromLR(ordersLR);
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
