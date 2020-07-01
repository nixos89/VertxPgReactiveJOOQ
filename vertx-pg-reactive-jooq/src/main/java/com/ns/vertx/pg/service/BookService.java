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
public interface BookService {

	@GenIgnore
	static BookService createBookService(PgPool pgClient , Configuration configuration,
			Handler<AsyncResult<BookService>> readyHandler) {
		return new BookServiceImpl(pgClient, configuration, readyHandler);
	}
	
	@GenIgnore
	static BookService createBookProxy(Vertx vertx, String address) {
		return new BookServiceVertxEBProxy(vertx, address);
	}
	
	@Fluent
	BookService getAllBooksJooqSP(Handler<AsyncResult<JsonObject>> resultHandler);
	
	@Fluent
	BookService getBookByIdJooqSP(Long id, Handler<AsyncResult<JsonObject>> resultHandler);
	
	@Fluent
	BookService getAllBooksByAuthorIdJooqSP(Long authorId, Handler<AsyncResult<JsonObject>> resultHandler);
		
	@Fluent
	BookService createBookJooqSP(JsonObject bookJO, Handler<AsyncResult<Void>> resultHandler);
	
	@Fluent
	BookService updateBookJooqSP(JsonObject bookJO, Handler<AsyncResult<Void>> resultHandler);
	
	@Fluent
	BookService deleteBookJooqSP(Long id, Handler<AsyncResult<Void>> resultHandler);
	
}
