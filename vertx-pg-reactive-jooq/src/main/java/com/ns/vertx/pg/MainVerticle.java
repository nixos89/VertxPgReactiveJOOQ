package com.ns.vertx.pg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.http.HttpServerVerticle;
import com.ns.vertx.pg.service.DatabaseVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.impl.cpu.CpuCoreSensor;

public class MainVerticle extends AbstractVerticle {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
	private static final int HTTP_INSTANCE_NUM = 4; // number of HttpServerVerticle instances to deploy
	
	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		LOGGER.info("++++ CpuCoreSensor.availableProcessors() = " + CpuCoreSensor.availableProcessors() + " ++++");
		LOGGER.info("MainVerticle start() method invoked on thread: " + Thread.currentThread());
		Promise<String> dbVerticleDepoyment = Promise.promise();	
		
		vertx.deployVerticle(new DatabaseVerticle(), dbVerticleDepoyment );			
		dbVerticleDepoyment.future().compose(ar -> {			
			Promise<String> httpVerticleDeployment = Promise.promise();
			vertx.deployVerticle(HttpServerVerticle.class.getName(), 
					new DeploymentOptions().setInstances(HTTP_INSTANCE_NUM),
					httpVerticleDeployment);
			
			LOGGER.info(" ======== Deploying " + HTTP_INSTANCE_NUM + " instances of "
					+ HttpServerVerticle.class.getName() + "... ====== ");			
			return httpVerticleDeployment.future();
		}).onComplete(handler -> {
			if (handler.succeeded()) {
				LOGGER.info((HTTP_INSTANCE_NUM + 1) + " Verticles have been deployed !!!!!!!");
				startPromise.complete();
			} else {
				LOGGER.error("Error, something went wrong with deploying verticles!");
				startPromise.fail(handler.cause());
			}
		});
	}		
	

}
