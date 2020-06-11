package com.ns.vertx.pg.service;

import org.jooq.Configuration;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;

@VertxGen
@ProxyGen
public interface OrderService {
	
	@GenIgnore
	static OrderService createOrderService(PgPool pgClient , Configuration configuration,
			Handler<AsyncResult<OrderService>> readyHandler) {
		return new OrderServiceImpl(pgClient, configuration, readyHandler);
	}
	
	@GenIgnore
	static OrderService createOrderProxy(Vertx vertx, String address) {
		return new OrderServiceVertxEBProxy(vertx, address);
	}
		
	@Fluent
	OrderService getAllOrdersJooqSP(Handler<AsyncResult<JsonObject>> resultHandler);
		
	@Fluent
	OrderService createOrderJooqSP(JsonObject orderJO, String username, Handler<AsyncResult<JsonObject>> resultHandler);
}
