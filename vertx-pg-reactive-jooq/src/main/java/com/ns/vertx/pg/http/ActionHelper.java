package com.ns.vertx.pg.http;

import java.io.IOException;
import java.util.NoSuchElementException;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;


public class ActionHelper {
	
	private static <T> Handler<AsyncResult<T>> writeJsonResponse(RoutingContext context, int status) {
		return ar -> {
			if (ar.failed()) {
				if (ar.cause() instanceof NoSuchElementException) {
					context.response().setStatusCode(404).end(ar.cause().getMessage());
				} else if(ar.cause() instanceof IOException) {
					context.response().setStatusCode(400).end(ar.cause().getMessage());
				} else {
					context.fail(ar.cause());
				}
			} else {
				context.response()
					.setStatusCode(status)
					.putHeader("content-type", "application/json; charset=utf-8")
					.end(Json.encodePrettily(ar.result()));
			}
		};
	}
	
	
	static <T> Handler<AsyncResult<T>> ok(RoutingContext rc) {
		return writeJsonResponse(rc, 200);
	}
	
	static <T> Handler<AsyncResult<T>> created(RoutingContext rc) {
		return writeJsonResponse(rc, 201);
	}
	
	
	static Handler<AsyncResult<Void>> noContent(RoutingContext rc) {
		return ar -> {
			if(ar.failed()) {
				if (ar.cause() instanceof NoSuchElementException) {
					rc.response().setStatusCode(404).end(ar.cause().getMessage());
				} else {
					rc.fail(ar.cause());
				}
			} else {
				rc.response().setStatusCode(204).end();
			}
		};
	}
	
}
