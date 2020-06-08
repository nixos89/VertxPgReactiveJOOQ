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

@ProxyGen
@VertxGen
public interface CategoryService {
	
	@GenIgnore
	static CategoryService createCategoryService(PgPool pgClient , Configuration configuration,
			/*ReactiveClassicGenericQueryExecutor queryExecutor, */ Handler<AsyncResult<CategoryService>> readyHandler) {
//		return new CategoryServiceImpl(pgClient, configuration, readyHandler);
		return new CategoryServiceImpl(pgClient, configuration, readyHandler);
	}
	
	@GenIgnore
	static CategoryService createCategoryProxy(Vertx vertx, String address) {
		return new CategoryServiceVertxEBProxy(vertx, address);
	}
	
	@Fluent
	CategoryService getAllCategoriesJooqSP(Handler<AsyncResult<JsonObject>> resultHandler);
	
	@Fluent
	CategoryService getCategoryByIdJooqSP(Long id, Handler<AsyncResult<JsonObject>> resultHandler);
		
	@Fluent
	CategoryService createCategoryJooqSP(String name, Boolean isDeleted, Handler<AsyncResult<Void>> resultHandler);
	
	@Fluent
	CategoryService updateCategoryJooqSP(JsonObject categoryJO, Handler<AsyncResult<Void>> resultHandler);
	
	@Fluent
	CategoryService deleteCategoryJooqSP(Long id, Handler<AsyncResult<Void>> resultHandler);	
}
