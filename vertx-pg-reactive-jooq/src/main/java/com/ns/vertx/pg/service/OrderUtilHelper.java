package com.ns.vertx.pg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.jooq.tables.pojos.Orders;
import com.ns.vertx.pg.jooq.tables.pojos.Users;

import io.github.jklingsporn.vertx.jooq.shared.JsonObjectConverter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public class OrderUtilHelper {

//	private static final Logger LOGGER = LoggerFactory.getLogger(OrderUtilHelper.class);

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

	static Map<Long, Integer> mapOrderItemsFromOrderJA(JsonArray orderItemsJA) {
		Map<Long, Integer> bookIdAmountMap = new HashMap<Long, Integer>();
		orderItemsJA.forEach(oi -> {
			if (oi instanceof JsonObject) {
				bookIdAmountMap.put(((JsonObject) oi).getLong("book_id"), ((JsonObject) oi).getInteger("amount"));
			}
		});
		return bookIdAmountMap;
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

}
