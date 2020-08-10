package com.ns.vertx.pg.service;

import static com.ns.vertx.pg.jooq.tables.Book.BOOK;
import static com.ns.vertx.pg.jooq.tables.OrderItem.ORDER_ITEM;
import static com.ns.vertx.pg.jooq.tables.Orders.ORDERS;
import static com.ns.vertx.pg.jooq.tables.Users.USERS;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSONFormat;
import org.jooq.JSONFormat.RecordFormat;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Result;
import org.jooq.Row2;
import org.jooq.Row3;
import org.jooq.SQLDialect;
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
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
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
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/vertx-jooq-cr",
					"postgres", "postgres");
			DSLContext create = DSL.using(connection, SQLDialect.POSTGRES);
			Result<Record1<String>> resultR1S = create.select(Routines.getAllOrders()).fetch();
			String strResultFinal = resultR1S.formatJSON(
					new JSONFormat()
					.header(false)
					.recordFormat(RecordFormat.ARRAY)
			);
			final String fixedJSONString = strResultFinal
					.substring(3, strResultFinal.length() - 3)
					.replaceAll("\\\\n", "")
					.replaceAll("\\\\", "");
			
//			StringUtils.substring(strResultFinal, 3, strResultFinal.length() - 3);
			JsonObject ordersJA = new JsonObject(fixedJSONString);
			connection.close();
			resultHandler.handle(Future.succeededFuture(ordersJA));			
		} catch (SQLException e) {
			e.printStackTrace();			
		}
		return this;
	}


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
						.values(orderJO.getDouble("totalPrice"), orderDate, userPojo.getUserId())
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
					    		return Future.succeededFuture(new JsonObject().put("orderId", orderId));					    		
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
