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
import java.util.ArrayList;
import java.util.HashMap;
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
import org.jooq.conf.Settings;
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
import io.github.jklingsporn.vertx.jooq.shared.JsonObjectConverter;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
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
//			LOGGER.info("getAllOrdersJooqSP try-block invoked on thread: " + Thread.currentThread());
			connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/vertx-jooq-cr",
					"postgres", "postgres");
			Settings settings = new Settings().withExecuteLogging(false);
			DSLContext dslContext = DSL.using(connection, SQLDialect.POSTGRES, settings);
			Result<Record1<String>> resultR1S = dslContext.select(Routines.getAllOrders()).fetch();
			String strResultFinal = resultR1S.formatJSON(
					new JSONFormat()
					.header(false)
					.recordFormat(RecordFormat.ARRAY)
			);
			final String fixedJSONString = strResultFinal
					.substring(3, strResultFinal.length() - 3)
					.replaceAll("\\\\n", "")
					.replaceAll("\\\\", "");
			
			JsonObject ordersJA = new JsonObject(fixedJSONString);
			connection.close();
			resultHandler.handle(Future.succeededFuture(ordersJA));			
		} catch (SQLException e) {
//			LOGGER.info("getAllOrdersJooqSP catch(SQLException e)-block invoked on thread: " + Thread.currentThread());
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
				Users userPojo = getUserPojoFromRS(userRes); 
				List<JsonObject> orderItemJObjectsToSave = extractOrderItemsFromOrderJA(orderJO.getJsonArray("orders"));
				List<Long> orderItemBookIds = orderItemJObjectsToSave.stream()
						.mapToLong(oi -> oi.getLong("book_id")).boxed().collect(Collectors.toList()); 
				
				Map<Long, Integer> bookIdAmountMap = mapOrderItemsFromOrderJA(orderJO.getJsonArray("orders"));												
				
				LocalDateTime orderDate = new TimestampToLocalDateTimeConverter().from(new Timestamp(System.currentTimeMillis()));				
				return transactionQE.executeAny(dsl -> dsl.insertInto(ORDERS).columns(ORDERS.TOTAL, ORDERS.ORDER_DATE, ORDERS.USER_ID)
						.values(orderJO.getDouble("totalPrice"), orderDate, userPojo.getUserId())
						.returning(ORDERS.ORDER_ID, ORDERS.TOTAL, ORDERS.ORDER_DATE, ORDERS.USER_ID)
				).compose(savedOrder -> {
					Orders savedOrderPojo = extractOrderRS(savedOrder);																
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
					    		transactionQE.commit();
					    		return Future.succeededFuture(new JsonObject().put("orderId", orderId));					    		
					    	}, failure -> {
					    		LOGGER.info("Rolling-back transaction...");
					    		transactionQE.rollback().onFailure(handler -> LOGGER.error("Error, rolling-back failed for Order creation!"));
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
	
	static Users getUserPojoFromRS(RowSet<Row> userRS) {
		JsonObject userJO = new JsonObject();
		for (Row row : userRS) {
			userJO.put("user_id", row.getLong("user_id"));
			userJO.put("role_id", row.getLong("role_id"));
			userJO.put("first_name", row.getString("first_name"));
			userJO.put("last_name", row.getString("last_name"));
			userJO.put("email", row.getString("email"));
			userJO.put("username", row.getString("username"));
			userJO.put("password", row.getString("password"));
		}
		Users userPojo = new Users(userJO);
		return userPojo;
	}

	private List<JsonObject> extractOrderItemsFromOrderJA(JsonArray orderItemsJA) {
		List<String> oiJAStringList = orderItemsJA.stream().map(o -> o.toString()).collect(Collectors.toList());

		List<JsonObject> oiJsonObjectList = new ArrayList<JsonObject>();
		JsonObjectConverter joConverter = new JsonObjectConverter();
		for (String oiStr : oiJAStringList) {
			oiJsonObjectList.add(joConverter.from(oiStr));
		}
		if (!oiJsonObjectList.isEmpty()) {
			return oiJsonObjectList;
		} else {
			return null;
		}
	}

	private Map<Long, Integer> mapOrderItemsFromOrderJA(JsonArray orderItemsJA) {
		Map<Long, Integer> bookIdAmountMap = new HashMap<Long, Integer>();
		orderItemsJA.forEach(oi -> {
			if (oi instanceof JsonObject) {
				bookIdAmountMap.put(((JsonObject) oi).getLong("book_id"), ((JsonObject) oi).getInteger("amount"));
			}
		});
		return bookIdAmountMap;
	}

	private Orders extractOrderRS(RowSet<Row> orderRS) {
		Orders orderPojo = new Orders();
		for (Row row : orderRS) {
			orderPojo.setOrderId(row.getLong("order_id"));
			orderPojo.setOrderDate(row.getLocalDateTime("order_date"));
			orderPojo.setTotal(row.getDouble("total"));
			orderPojo.setUserId(row.getLong("user_id"));
		}
		return orderPojo;
	}
		
}
