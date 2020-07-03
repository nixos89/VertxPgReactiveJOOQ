package com.ns.vertx.pg.service;

import static com.ns.vertx.pg.jooq.tables.Author.AUTHOR;
import static com.ns.vertx.pg.jooq.tables.AuthorBook.AUTHOR_BOOK;
import static com.ns.vertx.pg.jooq.tables.Book.BOOK;
import static com.ns.vertx.pg.jooq.tables.Category.CATEGORY;
import static com.ns.vertx.pg.jooq.tables.CategoryBook.CATEGORY_BOOK;
import static com.ns.vertx.pg.jooq.tables.OrderItem.ORDER_ITEM;
import static com.ns.vertx.pg.jooq.tables.Orders.ORDERS;
import static com.ns.vertx.pg.jooq.tables.Users.USERS;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.JSON;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Row2;
import org.jooq.Row3;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TimestampToLocalDateTimeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.jooq.Routines;
import com.ns.vertx.pg.jooq.tables.pojos.Book;
import com.ns.vertx.pg.jooq.tables.pojos.Orders;
import com.ns.vertx.pg.jooq.tables.pojos.Users;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.QueryResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;

public class OrderServiceImpl implements OrderService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);	
	private ReactiveClassicGenericQueryExecutor queryExecutor;
	
	public OrderServiceImpl(PgPool pgClient, Configuration configuration, Handler<AsyncResult<OrderService>> readyHandler) {
		pgClient.getConnection(ar -> {
			if (ar.failed()) {
				LOGGER.error("Could NOT OPEN DB connection!" , ar.cause());
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
	
	// ********************************************************************************************************************************
	// *********************************************** OrderService CRUD methods ****************************************************** 
	// ********************************************************************************************************************************
	
	@Override
	public OrderService getAllOrdersJooqSP(Handler<AsyncResult<JsonObject>> resultHandler) {
		Future<QueryResult> ordersFuture = queryExecutor.transaction(qe -> qe
				// FIXME: try out executeAny() or findOneRow() method instead of query() !!!!
			.query(dsl -> dsl
				.select(Routines.getAllOrders()) 
		));
		
//		Future<Row> ordersFuture = queryExecutor.transaction(qe -> qe
//			.findOneRow(dsl -> dsl
//				.select(Routines.getAllOrders()) 
//		));	    	
		LOGGER.info("Passed ordersFuture...");
	    ordersFuture.onComplete(handler -> {
			if (handler.succeeded()) {								
				QueryResult qRes = handler.result();					
				JsonObject ordersJsonObject = OrderUtilHelper.convertGetAllOrdersQRToJsonObject(qRes);
//				JsonObject ordersJsonObject = OrderUtilHelper.extractJOFromRow(handler.result());
//				LOGGER.info("ordersJsonObject.encodePrettily(): " + ordersJsonObject.encodePrettily());
				resultHandler.handle(Future.succeededFuture(ordersJsonObject));
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening ALL orders! handler.cause() = " + handler.cause());
	    		queryExecutor.rollback();	    		
	    		resultHandler.handle(Future.failedFuture(handler.cause()));
	    	}
	    }); 		
		return this;
	}
	
	
	/*
	@Override
	public OrderService getAllOrdersJooqSP(Handler<AsyncResult<JsonObject>> resultHandler) {		
		Future<List<Row>> ordersFuture = queryExecutor.transaction(qe -> qe
				.findManyRow(dsl -> dsl
						.select(ORDERS.ORDER_ID, ORDERS.ORDER_DATE, ORDERS.TOTAL, USERS.USERNAME, ORDER_ITEM.AMOUNT,						
								DSL.field( "to_json(array_agg(DISTINCT {0}.*))", JSON.class, AUTHOR).as("authors"),
								DSL.field("to_json(array_agg(DISTINCT {0}.*))", JSON.class, CATEGORY).as("categories"),
								BOOK.TITLE, BOOK.PRICE)
						.from(ORDERS).leftJoin(ORDER_ITEM).on(ORDERS.ORDER_ID.eq(ORDER_ITEM.ORDER_ID))
						.leftJoin(USERS).on(ORDERS.USER_ID.eq(USERS.USER_ID))
						.leftJoin(BOOK).on(ORDER_ITEM.BOOK_ID.eq(BOOK.BOOK_ID))
						.leftJoin(AUTHOR_BOOK).on(BOOK.BOOK_ID.eq(AUTHOR_BOOK.BOOK_ID))
						.leftJoin(AUTHOR).on(AUTHOR_BOOK.AUTHOR_ID.eq(AUTHOR.AUTHOR_ID))
						.leftJoin(CATEGORY_BOOK).on(BOOK.BOOK_ID.eq(CATEGORY_BOOK.BOOK_ID))
						.leftJoin(CATEGORY).on(CATEGORY_BOOK.CATEGORY_ID.eq(CATEGORY.CATEGORY_ID))
						.groupBy(ORDERS.ORDER_ID, USERS.USERNAME, ORDER_ITEM.AMOUNT, BOOK.TITLE, BOOK.PRICE, 
								 AUTHOR.AUTHOR_ID, CATEGORY.CATEGORY_ID)
						.orderBy(ORDERS.ORDER_ID.asc())
				));	    				
			LOGGER.info("Passed ordersFuture...");
		    ordersFuture.onComplete(handler -> {
				if (handler.succeeded()) {								
					List<Row> ordersLR = handler.result();				
					JsonObject ordersJsonObject = OrderUtilHelper.extractOrdersFromLR(ordersLR);
//					LOGGER.info("ordersJsonObject.encodePrettily(): " + ordersJsonObject.encodePrettily());
					resultHandler.handle(Future.succeededFuture(ordersJsonObject));
		    	} else {
		    		LOGGER.error("Error, something failed in retrivening ALL orders! handler.cause() = " + handler.cause());
		    		queryExecutor.rollback();	    		
		    		resultHandler.handle(Future.failedFuture(handler.cause()));
		    	}
		    }); 		
		return this;
	}*/


	@SuppressWarnings("unchecked")
	@Override
	public OrderService createOrderJooqSP(JsonObject orderJO, String username, Handler<AsyncResult<JsonObject>> resultHandler) {
		if (orderJO == null) {
			resultHandler.handle(Future.failedFuture(new IOException("Error, request body can not be empty!")));
		}
		
		Future<JsonObject> retVal = queryExecutor.beginTransaction().compose(transactionQE -> 
			transactionQE.executeAny(dsl -> dsl
				.selectFrom(USERS).where(USERS.USERNAME.eq(username))
			).compose(userRes -> {
				Users userPojo = OrderUtilHelper.getUserPojoFromRS(userRes); 
				List<JsonObject> orderItemJObjectsToSave = OrderUtilHelper.extractOrderItemsFromOrderJA(orderJO.getJsonArray("orders"));
				List<Long> orderItemBookIds = orderItemJObjectsToSave.stream()
						.mapToLong(oi -> oi.getLong("book_id")).boxed().collect(Collectors.toList());
				
				Map<Long, Integer> bookIdAmountMap = OrderUtilHelper.mapOrderItemsFromOrderJA(orderJO.getJsonArray("orders"));												
				
				LocalDateTime orderDate = new TimestampToLocalDateTimeConverter().from(new Timestamp(System.currentTimeMillis()));				
				return transactionQE.executeAny(dsl -> dsl.insertInto(ORDERS).columns(ORDERS.TOTAL, ORDERS.ORDER_DATE, ORDERS.USER_ID)
						.values(orderJO.getDouble("total_price"), orderDate, userPojo.getUserId())
						.returning(ORDERS.ORDER_ID, ORDERS.TOTAL, ORDERS.ORDER_DATE, ORDERS.USER_ID)
				).compose(savedOrder -> {
					Orders savedOrderPojo = OrderUtilHelper.extractOrderRS(savedOrder);																
					return transactionQE.executeAny(dsl -> dsl
							.select(BOOK.BOOK_ID, BOOK.PRICE, BOOK.TITLE, BOOK.AMOUNT, BOOK.IS_DELETED).from(BOOK)
							.where(BOOK.BOOK_ID.in(orderItemBookIds))
					).compose(searchedBooksRS -> {
						List<Book> searchedBookList = BookUtilHelper.extractBookPojosFromRS(searchedBooksRS);					
						Map<Long, Integer> bookIdAmountMapUpdated = new LinkedHashMap<>();

						for (Book sb: searchedBookList) { 
							int updatedBookAmount = sb.getAmount() - bookIdAmountMap.get(sb.getBookId());
							if (updatedBookAmount < 0 ) {								
								resultHandler.handle(Future.failedFuture(new IOException(
										"Error, it's ONLY possible (for book '" + sb.getTitle() + "', id = "
												+ sb.getBookId() + ") to order up to " + sb.getAmount() + " copies!")));
								transactionQE.rollback();
							}
							bookIdAmountMapUpdated.put(sb.getBookId(), updatedBookAmount);
							sb.setAmount(updatedBookAmount);	
						}						
					    return transactionQE.execute(dsl -> { 					    						    						    	
					    	Row2<Long,Integer> array[] = new Row2[bookIdAmountMapUpdated.size()];
					    	int i = 0;
							for (Map.Entry<Long, Integer> pair : bookIdAmountMapUpdated.entrySet()) {
								array[i] = DSL.row(DSL.val(pair.getKey()).cast(SQLDataType.BIGINT), DSL.val(pair.getValue()).cast(SQLDataType.INTEGER));
								i++;
							}
							Table<Record2<Long, Integer>> batTmp = DSL.values(array);
							batTmp = batTmp.as("bat", "book_id", "amount");
							Field<Long> bookIdField = DSL.field(DSL.name("bat", "book_id"), Long.class);
					    	Field<Integer> amountField = DSL.field(DSL.name("bat", "amount"), Integer.class);

					    	return dsl.update(BOOK).set(BOOK.AMOUNT, amountField)
					    			.from(batTmp)  
					    			.where(BOOK.BOOK_ID.eq(bookIdField));					    
					    }).compose(updatedBooks -> {
					    	Long orderId = savedOrderPojo.getOrderId();					    	
					    	Row3<Long, Long, Integer> oiArr[] = new Row3[bookIdAmountMap.size()];
					    	int i = 0;
							for (Map.Entry<Long, Integer> pair : bookIdAmountMap.entrySet()) {
								oiArr[i] = DSL.row(DSL.val(orderId).cast(SQLDataType.BIGINT), 
											DSL.val(pair.getKey()).cast(SQLDataType.BIGINT), 
											DSL.val(pair.getValue()).cast(SQLDataType.INTEGER));
								i++;
							}
							Table<Record3<Long, Long, Integer>> oiTmp = DSL.values(oiArr).as("oiTmp", "order_id", "book_id", "amount");					    	
					    	return transactionQE.execute(dsl -> dsl
					    		.insertInto(ORDER_ITEM, ORDER_ITEM.ORDER_ID, ORDER_ITEM.BOOK_ID, ORDER_ITEM.AMOUNT)
					    		.select(dsl.selectFrom(oiTmp))					    		
					    	).compose(success -> {
							    LOGGER.info("Commiting transaction...");
					    		transactionQE.commit();
					    		return Future.succeededFuture(new JsonObject().put("order_id", orderId));					    		
					    	}, failure -> {
					    		LOGGER.info("Rolling-back transaction...");
					    		transactionQE.rollback();
					    		return Future.failedFuture(failure);
					    	});					    								
					    }); 								
					});								  		
			}); // savedOrder::END
		}));
		retVal.onSuccess(result -> resultHandler.handle(Future.succeededFuture(result)));
		retVal.onFailure(handler -> resultHandler.handle(Future.failedFuture(handler)));
		return this;
	}
		
}
