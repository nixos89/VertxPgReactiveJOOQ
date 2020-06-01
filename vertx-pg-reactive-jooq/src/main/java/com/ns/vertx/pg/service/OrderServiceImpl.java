package com.ns.vertx.pg.service;

import static com.ns.vertx.pg.jooq.tables.Author.AUTHOR;
import static com.ns.vertx.pg.jooq.tables.AuthorBook.AUTHOR_BOOK;
import static com.ns.vertx.pg.jooq.tables.Book.BOOK;
import static com.ns.vertx.pg.jooq.tables.Category.CATEGORY;
import static com.ns.vertx.pg.jooq.tables.CategoryBook.CATEGORY_BOOK;
import static com.ns.vertx.pg.jooq.tables.OrderItem.ORDER_ITEM;
import static com.ns.vertx.pg.jooq.tables.Orders.ORDERS;
import static com.ns.vertx.pg.jooq.tables.Users.USERS;

import static org.jooq.impl.DSL.jsonArray;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.TimestampToLocalDateTimeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.jooq.tables.pojos.Book;
import com.ns.vertx.pg.jooq.tables.pojos.Orders;
import com.ns.vertx.pg.jooq.tables.pojos.Users;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.github.jklingsporn.vertx.jooq.shared.internal.QueryResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

public class OrderServiceImpl {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);	
	
	/* JSON Request Body for creating an Order:
		 {
		    "orders": [
		        {
		            "book_id": 2,
		            "amount": 3
		        },
		        {
		            "book_id": 5,
		            "amount": 2
		        }
		    ],
		    "total_price": 89.92
		}
	 * */	
	public static Future<JsonObject> createOrderJooq(ReactiveClassicGenericQueryExecutor queryExecutor, JsonObject orderJO, String username) {
		Promise<JsonObject> promise = Promise.promise();
		if (orderJO == null) { promise.handle(Future.failedFuture(new IOException("Error, request body can not be empty!"))); }
		
		Future<Void> retVal = queryExecutor.beginTransaction().compose(transactionQE -> 
			transactionQE.executeAny(dsl -> dsl
				.selectFrom(USERS).where(USERS.USERNAME.eq(username))
			).compose(userRes -> {
				Users userPojo = OrderUtilHelper.getUserPojoFromRS(userRes); LOGGER.info("userPojo.toString() = " + userPojo.toString());															
				List<JsonObject> orderItemJObjectsToSave = OrderUtilHelper.extractOrderItemsFromOrderJA(orderJO.getJsonArray("orders"));
				orderItemJObjectsToSave.forEach(oiJO -> System.out.println("book_id = " 
						+ oiJO.getLong("book_id") + ", amount = " + oiJO.getInteger("amount")));
				List<Long> orderItemBookIds = orderItemJObjectsToSave.stream().mapToLong(oi -> oi.getLong("book_id")).boxed().collect(Collectors.toList());
				
				Map<Long, Integer> bookIdAmountMap = OrderUtilHelper.mapOrderItemsFromOrderJA(orderJO.getJsonArray("orders"));				
				bookIdAmountMap.forEach((k, v) -> LOGGER.info("bookId=" + k + ", value=" + v));								
				
				LocalDateTime orderDate = new TimestampToLocalDateTimeConverter().from(new Timestamp(System.currentTimeMillis()));				
				return transactionQE.executeAny(dsl -> dsl.insertInto(ORDERS).columns(ORDERS.TOTAL, ORDERS.ORDER_DATE, ORDERS.USER_ID)
						.values(orderJO.getDouble("total_price"), orderDate, userPojo.getUserId())
						.returning(ORDERS.ORDER_ID, ORDERS.TOTAL, ORDERS.ORDER_DATE, ORDERS.USER_ID)
				).compose(savedOrder -> {
					Orders savedOrderPojo = OrderUtilHelper.extractOrderRS(savedOrder);						
					LOGGER.info("savedOrderPojo.getOrderId() = " + savedOrderPojo.getOrderId()); 
					LOGGER.info("savedOrderPojo.getOrderDate() = " + savedOrderPojo.getOrderDate());
					/*  Window Functions example here: https://stackoverflow.com/a/6821925/6805866 ...based on this https://stackoverflow.com/q/38256871/6805866 question) */					
					// NOTE: to solve following selectFrom() check this QA at https://stackoverflow.com/q/2862080/6805866 ("SQL: Return “true” if list of records exists?")
					return transactionQE.executeAny(dsl -> dsl
							.select(BOOK.BOOK_ID, BOOK.PRICE, BOOK.TITLE, BOOK.AMOUNT, BOOK.IS_DELETED).from(BOOK)
							.where(BOOK.BOOK_ID.in(orderItemBookIds))
					).compose(searchedBooksRS -> {
						List<Book> searchedBookList = BookUtilHelper.extractBookPojosFromRS(searchedBooksRS);
						LOGGER.info("searchedBookList:"); searchedBookList.forEach(b -> LOGGER.info(b.toString()));					
						Map<Long, Integer> bookIdAmountMapUpdated = new LinkedHashMap<>();

						for (Book sb: searchedBookList) { 
							int updatedBookAmount = sb.getAmount() - bookIdAmountMap.get(sb.getBookId());
							if (updatedBookAmount < 0 ) {								
								promise.fail(new IOException("Error, it's ONLY possible (for book '" + sb.getTitle() + "', id = "
		                                + sb.getBookId() + ") to order up to " + sb.getAmount() + " copies!"));
//								return transactionQE.rollback(); // ...maaaybe this is ALSO necessary
							}
							bookIdAmountMapUpdated.put(sb.getBookId(), updatedBookAmount);
							sb.setAmount(updatedBookAmount);	
						}						
					    return transactionQE.execute(dsl -> { 					    	
					    	/*  
							    UPDATE book
								SET amount = bat.amount
								FROM (
									VALUES (2, 136),(5, 75)
								) AS bat(book_id, amount)
								WHERE book.book_id = bat.book_id;
					    	 * */
					    						    	
					    	Row2<Long,Integer> array[] = new Row2[bookIdAmountMap.size()];
					    	int i = 0;
							for (Map.Entry<Long, Integer> pair : bookIdAmountMap.entrySet()) {
								array[i] = DSL.row(pair.getKey(), pair.getValue());
								i++;
							}
							Table<Record2<Long, Integer>> batTmp = DSL.values(array);
							batTmp = batTmp.as("batTmp", "book_id", "amount");
							Field<Long> bookIdField = DSL.field(DSL.name("batTmp", "book_id"), Long.class);
					    	Field<Integer> amountField = DSL.field(DSL.name("batTmp", "amount"), Integer.class);
							
					    	return dsl.update(BOOK).set(BOOK.AMOUNT, amountField)
					    			.from(batTmp)  
					    			.where(BOOK.BOOK_ID.eq(bookIdField));					    
					    }).compose(updatedBooks -> {
					    	LOGGER.info("reached .compose(updatedBooks -> ...");
					    	Long orderId = savedOrderPojo.getOrderId();
					    	/* TODO: 002-might want to try for INSERT MULTIPLE OrderItems using `row(..)` like here: 
					    	 * 	https://www.jooq.org/doc/3.13/manual/sql-building/column-expressions/row-value-expressions/ */					    	
					    	
					    	transactionQE.execute(dsl -> dsl
					    		.insertInto(ORDER_ITEM, ORDER_ITEM.ORDER_ID, ORDER_ITEM.BOOK_ID, ORDER_ITEM.AMOUNT)
//					    		.values(`Collection which holds 'row(..)' construct as values`)
//					    		.values(savedOrderPojo.getOrderId(), value2, value3)
					    	);
					    	
						    LOGGER.info("Rolling-back transaction...");
							return transactionQE.rollback();
					    }); 								
					});								  		
			}); // savedOrder::END
		}));		
		retVal.onSuccess(handler -> promise.complete());
		retVal.onFailure(handler -> promise.fail(handler));
		return promise.future();
	}
		
	
	
	public static Future<JsonObject> getAllOrdersJooq(ReactiveClassicGenericQueryExecutor queryExecutor) {
		Promise<JsonObject> finalRes = Promise.promise();	
		// TODO: fix this query to ALSO collect orderItems as well as Books with its Authors and Categories 
		Future<List<Row>> ordersFuture = queryExecutor.transaction(qe -> qe
			.findManyRow(dsl -> dsl
				.select(ORDERS.ORDER_ID, ORDERS.ORDER_DATE, ORDERS.TOTAL, USERS.USERNAME, ORDER_ITEM.AMOUNT,						
//						jsonArray(DSL.arrayAgg(DSL.selectDistinct(AUTHOR.AUTHOR_ID, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME).from(AUTHOR))),
						
						jsonArray(AUTHOR.AUTHOR_ID, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME),
						jsonArray(CATEGORY.CATEGORY_ID, CATEGORY.NAME, CATEGORY.IS_DELETED),
						BOOK.TITLE, BOOK.PRICE)
				.from(ORDERS).leftJoin(ORDER_ITEM).on(ORDERS.ORDER_ID.eq(ORDER_ITEM.ORDER_ID))
				.leftJoin(USERS).on(ORDERS.USER_ID.eq(USERS.USER_ID))
				.leftJoin(BOOK).on(ORDER_ITEM.BOOK_ID.eq(BOOK.BOOK_ID))
				.leftJoin(AUTHOR_BOOK).on(BOOK.BOOK_ID.eq(AUTHOR_BOOK.BOOK_ID))
				.leftJoin(AUTHOR).on(AUTHOR_BOOK.AUTHOR_ID.eq(AUTHOR.AUTHOR_ID))
				.leftJoin(CATEGORY_BOOK).on(BOOK.BOOK_ID.eq(CATEGORY_BOOK.BOOK_ID))
				.leftJoin(CATEGORY).on(CATEGORY_BOOK.CATEGORY_ID.eq(CATEGORY.CATEGORY_ID))
				.groupBy(ORDERS.ORDER_ID, USERS.USERNAME, ORDER_ITEM.AMOUNT, BOOK.TITLE, BOOK.PRICE, AUTHOR.AUTHOR_ID, CATEGORY.CATEGORY_ID)
				.orderBy(ORDERS.ORDER_ID.asc())
		));	    				
		LOGGER.info("Passed ordersFuture...");
	    ordersFuture.onComplete(handler -> {
			if (handler.succeeded()) {								
				List<Row> ordersLR = handler.result();				
				JsonObject ordersJsonObject = OrderUtilHelper.extractOrdersFromLR(ordersLR);
				LOGGER.info("ordersJsonObject.encodePrettily(): " + ordersJsonObject.encodePrettily());
				finalRes.complete(ordersJsonObject);
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening ALL orders! handler.cause() = " + handler.cause());
	    		queryExecutor.rollback();	    		
	    		finalRes.fail(handler.cause());
	    	}
	    }); 		
		return finalRes.future();
	}
	
	
	public static Future<JsonObject> getAllOrdersJooq2(ReactiveClassicGenericQueryExecutor queryExecutor) {
		Promise<JsonObject> finalRes = Promise.promise();	
		// TODO: fix this query to ALSO collect orderItems as well as Books with its Authors and Categories 
		Future<QueryResult> ordersFuture = queryExecutor.transaction(qe -> 
			qe.query(dsl -> dsl.resultQuery(DBQueries.GET_ALL_ORDERS))
		);	    				
		LOGGER.info("Passed ordersFuture...");
	    ordersFuture.onComplete(handler -> {
			if (handler.succeeded()) {								
				QueryResult ordersQR = handler.result();				
				JsonObject ordersJsonObject = OrderUtilHelper.extractOrderItemsFromQR(ordersQR);
				LOGGER.info("ordersJsonObject.encodePrettily(): " + ordersJsonObject.encodePrettily());
				finalRes.complete(ordersJsonObject);
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening ALL orders! handler.cause() = " + handler.cause());
	    		queryExecutor.rollback();	    		
	    		finalRes.fail(handler.cause());
	    	}
	    }); 		
		return finalRes.future();
	}	
	
	
	// like in DW app
	public static Future<JsonObject> getAllOrdersJooq3(ReactiveClassicGenericQueryExecutor queryExecutor) {
		Promise<JsonObject> finalRes = Promise.promise();	
		// TODO: fix this query to ALSO collect orderItems as well as Books with its Authors and Categories 
		Future<JsonObject> ordersFuture = queryExecutor.beginTransaction().compose(transactionQE -> transactionQE
			.findManyRow(dsl -> dsl
				.select(ORDERS.ORDER_ID, ORDERS.ORDER_DATE, ORDERS.TOTAL, USERS.USERNAME)
				.from(ORDERS)
				.leftJoin(USERS).on(ORDERS.USER_ID.eq(USERS.USER_ID))
				.groupBy(ORDERS.ORDER_ID, USERS.USERNAME)
				.orderBy(ORDERS.ORDER_ID.asc())
		).compose(allOrders -> {
			Promise<JsonObject> promise = Promise.promise();
			JsonObject ordersJOFinal = new JsonObject();
			JsonArray ordersJA = new JsonArray();			
			List<Long> orderIdList = allOrders.stream()
					.mapToLong(row -> row.getLong("order_id")).boxed().collect(Collectors.toList());
			JsonArray orderItemJA = new JsonArray();
			for (Row ordersRow: allOrders) {// FIXME: 001-change collection to `orderItemLR` to extract `order_item_id` and `book_id`
				Long orderId = ordersRow.getLong("order_id");
				LOGGER.info("************** orderId = " + orderId + " **************");

				Future<QueryResult> orderItemQRFuture = transactionQE.query(dsl -> dsl
						.select(ORDER_ITEM.ORDER_ITEM_ID,ORDER_ITEM.ORDER_ID, ORDER_ITEM.BOOK_ID, ORDER_ITEM.AMOUNT)
						.from(ORDER_ITEM)
						.where(ORDER_ITEM.ORDER_ID.eq(orderId))
						.orderBy(ORDER_ITEM.ORDER_ITEM_ID.asc()));
				
				orderItemQRFuture.onComplete(oiRes -> {
					if (oiRes.succeeded()) {
						LOGGER.info("////////////////////// oiRes.result() = " + oiRes.result() + " //////////////////////");
						QueryResult orderItemsQR = oiRes.result();
				
						Long bookId = orderItemsQR.get("book_id", Long.class);
						Integer amount = orderItemsQR.get("amount", Integer.class);
						Long orderItemId = orderItemsQR.get("order_item_id", Long.class);
						LOGGER.info("bookId = " + bookId + ", amount = " + amount + ", order_item_id = " + orderItemId);
					} else {
						LOGGER.error("Error, orderItemQRFuture FAILED!!! oiRes.cause() = " + oiRes.cause());
					}
				});
				
				QueryResult orderItemQR = transactionQE.query(dsl -> dsl
						.select(ORDER_ITEM.ORDER_ITEM_ID,ORDER_ITEM.ORDER_ID, ORDER_ITEM.BOOK_ID, ORDER_ITEM.AMOUNT)
						.from(ORDER_ITEM)
						.where(ORDER_ITEM.ORDER_ID.eq(orderId))
						.orderBy(ORDER_ITEM.ORDER_ITEM_ID.asc())).result();
				
				if (orderItemQR != null) {
					while (orderItemQR.hasResults()) {
						LOGGER.info("============ +++++ orderItemQR HAS result! ++++ ============");
					}
				} else { LOGGER.error("Error, orderItemQR IS NULL!!!!"); }								
				Long oiQRBookId = orderItemQR.get("book_id", Long.class);
				Integer oiQRAmount = orderItemQR.get("amount", Integer.class);
				Long oiQROrderItemId = orderItemQR.get("order_item_id", Long.class); 
				LOGGER.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				LOGGER.info("bookId = " + oiQRBookId + ", amount = " + oiQRAmount + ", orderItemId = " + oiQROrderItemId);
				LOGGER.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
				
				JsonObject singleOrderJO = new JsonObject();				
				for (QueryResult oiQR: orderItemQR.asList()) { // `orderItemLR` to extract `order_item_id` and `book_id` 
					Long bookId = oiQR.get("book_id", Long.class);
					QueryResult bookQR = transactionQE.query(dsl -> dsl
							.resultQuery(DBQueries.GET_BOOK_BY_BOOK_ID, Long.valueOf(bookId))).result();
					if (bookQR == null) {
						transactionQE.rollback().onComplete(handler -> {
							LOGGER.info("Rolling-back transaction because bookQR is NULL! bookQR = " + bookQR);
							promise.fail( new NoSuchElementException("No book for boo_id = " + bookId));
						});																		
					}
					JsonObject bookJO = BookUtilHelper.fillBook(bookQR);
					double totalOrderItemPrice = (double)(bookJO.getDouble("price") * (double) oiQR.get("amount", Integer.class));
					JsonObject orderItemJO = new JsonObject()
						.put("book", bookJO)
						.put("order_item_id", oiQR.get("order_item_id", Long.class))
						.put("amount", oiQR.get("amount", Integer.class))
						.put("total_order_item_price", totalOrderItemPrice);
					orderItemJA.add(orderItemJO);					
				}								
				singleOrderJO.put("order_id", orderId)
					.put("total_price", ordersRow.getDouble("total"))
					.put("order_date", ordersRow.getLocalDate("order_date"))
					.put("username", ordersRow.getString("username"))
					.put("order_items", orderItemJA);												
				ordersJA.add(singleOrderJO);
				orderItemJA = new JsonArray();
				singleOrderJO = new JsonObject();				
			}
			ordersJOFinal.put("orders", ordersJA);
			promise.complete(ordersJOFinal);
			LOGGER.info("Rolling-back transcation...");
		    transactionQE.rollback(); // .. ORR transactionQE.commit();
			return promise.future();
			
		}));	    				
		LOGGER.info("Passed ordersFuture...");
	    ordersFuture.onComplete(handler -> {
			if (handler.succeeded()) {												
				JsonObject ordersJsonObject = handler.result();
				LOGGER.info("ordersJsonObject.encodePrettily(): " + ordersJsonObject.encodePrettily());
				finalRes.complete(ordersJsonObject);
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening ALL orders!!! handler.cause() = " + handler.cause());
	    		finalRes.fail(handler.cause());
	    	}
	    }); 		
		return finalRes.future();
	}
		
		
}
