package com.ns.vertx.pg;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		Promise<String> httpVerticleDepoyment = Promise.promise();
		vertx.deployVerticle(new HttpVerticle(), httpVerticleDepoyment);

		httpVerticleDepoyment.future().setHandler(ar -> {
			if (ar.succeeded()) {
				startPromise.complete();
			} else {
				startPromise.fail(ar.cause());
			}
		});
	}

}
