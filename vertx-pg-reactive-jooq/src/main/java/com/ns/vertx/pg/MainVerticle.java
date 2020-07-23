package com.ns.vertx.pg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.http.HttpServerVerticle;
import com.ns.vertx.pg.service.DatabaseVerticle;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.jmx.JmxMeterRegistry;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxJmxMetricsOptions;

public class MainVerticle extends AbstractVerticle {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
	private static final int HTTP_INSTANCE_NUM = 4; // number of HttpServerVerticle instance to deploy
	
	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		Promise<String> dbVerticleDepoyment = Promise.promise();
/*				
		CompositeMeterRegistry myRegistry = new CompositeMeterRegistry();
		myRegistry.add(new JmxMeterRegistry(s -> null, Clock.SYSTEM));
		
		// Default JMX options will publish MBeans under domain "metrics"
		MicrometerMetricsOptions options = new MicrometerMetricsOptions()
		  .setJmxMetricsOptions(new VertxJmxMetricsOptions()
				  .setEnabled(true)
				  .setStep(5)
				  .setDomain("com.ns.vertx.pg"))
		  .setEnabled(true).setMicrometerRegistry(myRegistry)
		    .setEnabled(true);
		vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(options));
		// ...then deploy verticles with this vertx instance */
		
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
	
	public static void main(String[] args) {
		CompositeMeterRegistry myRegistry = new CompositeMeterRegistry();
		myRegistry.add(new JmxMeterRegistry(s -> null, Clock.SYSTEM));
		
		// Default JMX options will publish MBeans under domain "metrics"
		MicrometerMetricsOptions options = new MicrometerMetricsOptions()
		  .setJmxMetricsOptions(new VertxJmxMetricsOptions()
				  .setEnabled(true)
				  .setStep(5)
				  .setDomain("com.ns.vertx.pg"))
		  .setEnabled(true).setMicrometerRegistry(myRegistry)
		    .setEnabled(true);
		
		Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(options));
		vertx.deployVerticle(MainVerticle.class.getName());
		LOGGER.info(MainVerticle.class.getName() + " has successfully been deployed!!!!!! Woooo...");
	}

}
