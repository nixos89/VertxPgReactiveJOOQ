package com.ns.vertx.pg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jooq.DataType;
import org.jooq.JSON;
import org.jooq.impl.SQLDataType;
import org.jooq.tools.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.converters.JooqJsonToVertxJsonArrayConverter;
import com.ns.vertx.pg.converters.PostgresJSONVertxJsonObjectBinding;
import com.ns.vertx.pg.jooq.tables.pojos.Orders;
import com.ns.vertx.pg.jooq.tables.pojos.Users;

import io.github.jklingsporn.vertx.jooq.shared.JsonObjectConverter;
import io.github.jklingsporn.vertx.jooq.shared.internal.QueryResult;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public class OrderUtilHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderUtilHelper.class);
	
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
	
	
	static List<JsonObject> extractOrderItemsFromOrderJA(JsonArray orderItemsJA) {
		List<String> oiJAStringList = orderItemsJA.stream()
				.map(o -> o.toString()).collect(Collectors.toList());
		
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
	
	
	static Map<Long, Integer> mapOrderItemsFromOrderJA(JsonArray orderItemsJA) {
		Map<Long, Integer> bookIdAmountMap = new HashMap<Long, Integer>();		 
		orderItemsJA.forEach(oi -> {
			if (oi instanceof JsonObject) {
				bookIdAmountMap
					.put(((JsonObject) oi).getLong("book_id"), ((JsonObject) oi).getInteger("amount"));				
			}
		});		
		return bookIdAmountMap;
	}		
	
	static JsonObject fillOrder(QueryResult orderQR) {
		return new JsonObject()
			.put("order_id", orderQR.get("order_id", Long.class))
			.put("order_date", orderQR.get("order_date", String.class))
			.put("total", orderQR.get("totla", Double.class))
			.put("user_id", orderQR.get("user_id", Long.class));							
	}	
	
	static Orders extractOrderRS(RowSet<Row> orderRS) {
		Orders orderPojo = new Orders();
		for (Row row : orderRS) {
			orderPojo.setOrderId(row.getLong("order_id"));
			orderPojo.setOrderDate(row.getLocalDateTime("order_date"));
			orderPojo.setTotal(row.getDouble("total"));
			orderPojo.setUserId(row.getLong("user_id"));			
		}		
		return orderPojo;
	}	
			
	// needed for `getAllOrdersJooq(..)` method
	static JsonObject extractOrdersFromLR(List<Row> ordersLR) {
		LOGGER.info("Entered extractOrdersFromLR() method...");
		JsonArray ordersJA = new JsonArray();	
		JsonArray bookJA = new JsonArray();
		LOGGER.info("entering FOR-loop in extractOrdersFromLR()...");
		for (Row row : ordersLR) {
			JsonObject orderJO = new JsonObject();				
			orderJO.put("order_id", row.getLong("order_id"));
			orderJO.put("order_date", row.getLocalDateTime("order_date").toString());			
			orderJO.put("username", row.getString("username"));
			orderJO.put("amount", row.getInteger("amount"));
			// TODO: finish iteration throught for-loop of 'extractOrdersFromLR()' method
			JsonArray authorJA = new JsonArray();
			authorJA = row.get(JsonArray.class, 5);
			JsonArray categoryJA = new JsonArray();	
			categoryJA = row.get(JsonArray.class, 6);
			JsonObject bookJO = new JsonObject();
			bookJO.put("title", row.getString("title"));
			bookJO.put("price", row.getDouble("price"));
			bookJO.put("authors", authorJA);
			bookJO.put("categories", categoryJA);
			bookJA.add(bookJO);
			orderJO.put("books", bookJA);
			orderJO.put("total_price", row.getDouble("total"));
			ordersJA.add(orderJO);
		}
		JsonObject ordersJO = new JsonObject().put("orders", ordersJA);		
		return ordersJO;
	}
		
	// **************************************************************************************************
	// ********************* Helper methods for extracting info from GetAllOrderS ***********************
	// **************************************************************************************************	
	
	static JsonObject fillOrderQR(QueryResult ordersQR) {
		JsonObject orderJO = new JsonObject();
		JsonArray orderItemsJA = new JsonArray();
//		JsonArray authorsJA = ordersQR.get("authors", JsonArray.class);
//		JsonArray categoryJA = ordersQR.get("categories", JsonArray.class);
		
		Long orderItemId = ordersQR.get("order_item_id", Long.class);		
		Long orderItemIdNew = null;
		
		JsonObject bookJO = new JsonObject()
			.put("book_id", ordersQR.get("book_id", Long.class))
			.put("title", ordersQR.get("title", String.class))
			.put("price", ordersQR.get("price", Double.class))
			.put("deleted", ordersQR.get("is_deleted", Boolean.class))
			.put("authors", ordersQR.get("authors", JsonArray.class))
			.put("categories", ordersQR.get("categories", JsonArray.class));
			
		if (orderItemId != orderItemIdNew) {
			orderItemsJA.add(bookJO);
			orderItemIdNew = orderItemId;
		}
		
		orderJO
			.put("order_id", ordersQR.get("order_id", Long.class))
			.put("order_date", ordersQR.get("order_date", String.class))
			.put("total_price", ordersQR.get("total", Double.class))
			.put("username", ordersQR.get("username", String.class))
			.put("order_items", orderItemsJA);
				
		return orderJO;				
	}
	
	static JsonObject extractOrdersFromQR(QueryResult queryResult) {
		JsonArray ordersJA = new JsonArray();
		if (queryResult == null) {
			return null;
		}
		for (QueryResult qr : queryResult.asList()) {
			JsonObject order = fillOrderQR(qr);
			ordersJA.add(order);
		}
		return new JsonObject().put("orders", ordersJA);
	}
	
		
	static JsonObject fillOrderItemFromQR(QueryResult ordersQR) {
		JsonObject orderItemJO = new JsonObject();
		JsonArray orderItemsJA = new JsonArray();				
		Long orderItemId = ordersQR.get("order_item_id", Long.class);		
		Long orderItemIdNew = null;
		
		JsonObject bookJO = new JsonObject()
			.put("book_id", ordersQR.get("book_id", Long.class))
			.put("title", ordersQR.get("title", String.class))
			.put("price", ordersQR.get("price", Double.class))
			.put("deleted", ordersQR.get("is_deleted", Boolean.class))
			.put("authors", ordersQR.get("authors", JsonArray.class))
			.put("categories", ordersQR.get("categories", JsonArray.class));
			
		if (orderItemId != orderItemIdNew) {
			orderItemsJA.add(bookJO);
			orderItemIdNew = orderItemId;
		}
		
		orderItemJO
			.put("order_id", ordersQR.get("order_id", Long.class))
			.put("order_date", ordersQR.get("order_date", String.class))
			.put("total_price", ordersQR.get("total", Double.class))
			.put("username", ordersQR.get("username", String.class))
			.put("order_items", orderItemsJA);
				
		return orderItemJO;				
	}
	
	// used for OrderServiceImpl.getAllOrdersJooq2(..) method
	static JsonObject extractOrderItemsFromQR(QueryResult queryResult) {
		JsonArray orderItemsJA = new JsonArray();
		if (queryResult == null) {
			return null;
		}
		LOGGER.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++");
		LOGGER.info("queryResult = " + queryResult);
		LOGGER.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++");
		for (QueryResult qr : queryResult.asList()) {
			JsonObject orderItem = fillOrderItemFromQR(qr);
			orderItemsJA.add(orderItem);
		}
		return new JsonObject().put("order_items", orderItemsJA);
	}	
	
	
	// needed for `getAllOrdersJooq3(..)` method
	static JsonObject extractOrdersFromLR2(List<Row> ordersLR) {
		LOGGER.info("Entered extractOrdersFromLR() method...");
		JsonArray ordersJA = new JsonArray();	
		LOGGER.info("entering FOR-loop in extractOrdersFromLR()...");
		for (Row row : ordersLR) {
			JsonObject orderJO = new JsonObject();	
			orderJO.put("order_id", row.getLong("order_id"));
			orderJO.put("order_date", row.getLocalDate("order_date"));			
			orderJO.put("user_id", row.getString("username"));
			orderJO.put("total_price", row.getDouble("total"));
			ordersJA.add(orderJO);
		}
		JsonObject ordersJO = new JsonObject().put("orders", ordersJA);		
		return ordersJO;
	}
	
	
	// used for OrderServiceImpl.getAllOrdersJooq2(..) method
	static JsonObject extractOrderItemsFromQR2(QueryResult queryResult) {
		JsonObject ordersJOFinal = new JsonObject();
		if (queryResult == null) {
			return null;
		}
		LOGGER.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++");
		LOGGER.info("queryResult = " + queryResult);
		LOGGER.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++"); 
		for (QueryResult ordersQR : queryResult.asList()) {
			JsonObject orderItemJO = new JsonObject();
			JsonArray orderItemsJA = new JsonArray();				
			Long orderItemId = ordersQR.get("order_item_id", Long.class);		
			Long orderItemIdNew = null;
			
			JsonObject bookJO = new JsonObject()
				.put("book_id", ordersQR.get("book_id", Long.class))
				.put("title", ordersQR.get("title", String.class))
				.put("price", ordersQR.get("price", Double.class))
				.put("deleted", ordersQR.get("is_deleted", Boolean.class))
				.put("authors", ordersQR.get("authors", JsonArray.class))
				.put("categories", ordersQR.get("categories", JsonArray.class));
				
			if (orderItemId != orderItemIdNew) {
				orderItemsJA.add(bookJO);
				orderItemIdNew = orderItemId;
			}
			
			orderItemJO
				.put("order_id", ordersQR.get("order_id", Long.class))
				.put("order_date", ordersQR.get("order_date", String.class))
				.put("total_price", ordersQR.get("total", Double.class))
				.put("username", ordersQR.get("username", String.class))
				.put("order_items", orderItemsJA);
		}
		return ordersJOFinal;
	}
	
	public static JsonObject convertGetAllOrdersQRToJsonObject_Old(QueryResult qr) {
		JsonArray ordersJA = qr.get("orders", JsonArray.class);
//		JsonObject orders = qr.get("orders", JsonObject.class);		
//		DataType<JsonArray> jsonArrayType = SQLDataType.JSON.asConvertedDataType(new JsonArrayConverter());
//		JSON jooqJson = qr.get("orders", JSON.class);
//		JsonObject ordersJOFromJooqJson = new JsonObject().put("result", jooqJson.toString());
//		String strJson = qr.get("orders", String.class);
//		JsonObject convertedStrJson = new JsonObjectConverter().from(strJson);

//		DataType<JsonObject> jsonObjectType = SQLDataType.JSON.asConvertedDataType(new JSONJsonObjectConverter());
//		DataType<JsonObject> jsonObjectTypeDefault = SQLDataType.JSON.asConvertedDataType((Binding<? super JSON, JsonObject>) new JsonObjectConverter());

//		Field<JsonObject> ordersFieldDefault = DSL.field("get_all_orders", jsonObjectTypeDefault);
//		Field<JsonObject> ordersField = DSL.field("get_all_orders", jsonObjectType);
		
		return new JsonObject().put("orders", ordersJA); // TODO: CHANGE input-value of final result
	}
	
	public static JsonObject convertGetAllOrdersQRToJsonObject(QueryResult qr) {
		JsonObject finalRes2 = new JsonObject();
		LOGGER.info("qr.hasResults() = " + qr.hasResults());
		
		LOGGER.info("qr.get(\"orders\", JSON.class) = " + qr.get("orders", JSON.class));
		Row ordersRow = qr.<Row>unwrap();
		
		String strAllOrders = ordersRow.get(String.class, 0);
		LOGGER.info("strAllOrders = " + strAllOrders);
		JSON jooqJSON = ordersRow.get(JSON.class, 0);
		LOGGER.info("jooqJSON = " + jooqJSON);
		LOGGER.info("jooqJSON.toString() = " + jooqJSON.toString());
		JsonArray newConverterJA = new JooqJsonToVertxJsonArrayConverter().from(qr.get("orders", JSON.class));
		for (QueryResult qRes: qr.asList()) {			
			LOGGER.info("qRes.toString() = " + qRes.toString());
			LOGGER.info("qRes.get(\"orders\", JSON.class) = " + qRes.get("orders", JSON.class));
			LOGGER.info("qRes.get(\"orders\", JSONArray.class) = " + qRes.get("orders", JSONArray.class));
			LOGGER.info("qRes.get(\"orders\", JsonArray.class) = " + qRes.get("orders", JsonArray.class));
			LOGGER.info("qRes.get(\"orders\", String.class) = " + qRes.get("orders", String.class));
			LOGGER.info("qRes.get(\"orders\", JsonObject.class) = " + qRes.get("orders", JsonObject.class));
			Row ordersRowIn = qRes.<Row>unwrap();
			Long orderId = ordersRowIn.getLong("order_id");
			LOGGER.info("(in da for-loop) orderId = " + orderId);
			JsonArray val1 = qRes.get("orders", JsonArray.class);
			finalRes2.put("orders", val1);
		}				
//		JsonObject newConverterJO = new PostgresJSONVertxJsonObjectBinding().from(qr.get("orders", JSON.class)); 
		LOGGER.info("newConverterJA.encodePrettily() = \n" + newConverterJA.encodePrettily());
		LOGGER.info("finalRes2.encodePrettily() = \n" + finalRes2.encodePrettily());
		LOGGER.info("Moving on...");
		return new JsonObject().put("orders", newConverterJA);
	}
	
	
	public static JsonObject extractJOFromRow(Row row) {
		Buffer jsonBuffer = row.getBuffer("orders");
		LOGGER.info("retrived value into io.vertx.core.buffer.Buffer variable..");
		
		
		return new JsonObject().put("orders", "bla, bla");
	}
		

}
