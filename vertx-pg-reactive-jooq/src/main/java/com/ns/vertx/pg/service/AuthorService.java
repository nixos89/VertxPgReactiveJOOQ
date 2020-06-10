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
public interface AuthorService {

	@GenIgnore
	static AuthorService createAuthorService(PgPool pgClient , Configuration configuration,
			Handler<AsyncResult<AuthorService>> readyHandler) {
		return new AuthorServiceImpl(pgClient, configuration, readyHandler);
	}
	
	@GenIgnore
	static AuthorService createAuthorProxy(Vertx vertx, String address) {
		return new AuthorServiceVertxEBProxy(vertx, address);
	}
	
	@Fluent
	AuthorService getAllAuthorsJooqSP(Handler<AsyncResult<JsonObject>> resultHandler);
	
	@Fluent
	AuthorService getAuthorByIdJooqSP(Long id, Handler<AsyncResult<JsonObject>> resultHandler);
		
	@Fluent
	AuthorService createAuthorJooqSP(String firstName, String lastName, Handler<AsyncResult<Void>> resultHandler);
	
	@Fluent
	AuthorService updateAuthorJooqSP(JsonObject authorJO, Handler<AsyncResult<Void>> resultHandler);
	
	@Fluent
	AuthorService deleteAuthorJooqSP(Long id, Handler<AsyncResult<Void>> resultHandler);	
	
	
}
