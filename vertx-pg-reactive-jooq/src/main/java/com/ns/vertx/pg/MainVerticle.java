package com.ns.vertx.pg;

import com.ns.vertx.pg.http.HttpVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		Promise<String> httpVerticleDepoyment = Promise.promise();
		vertx.deployVerticle(HttpVerticle.class.getName(), httpVerticleDepoyment);

		httpVerticleDepoyment.future().onComplete(ar -> {
			if (ar.succeeded()) {
				startPromise.complete();
			} else {
				startPromise.fail(ar.cause());
			}
		});
	}

}
