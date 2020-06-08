package com.ns.vertx.pg.service;

import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.sqlclient.PoolOptions;

public class DatabaseVerticle extends AbstractVerticle {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseVerticle.class);
	
	// TODO: move DB classes/interfaces (from HttpVerticle) for instantiating in here IFF it's possible	
	public static final String CONFIG_AUTHOR_QUEUE = "author.queue";
	public static final String CONFIG_BOOK_QUEUE = "book.queue";
	public static final String CONFIG_CATEGORY_QUEUE = "category.queue";	
	public static final String CONFIG_ORDERS_QUEUE = "orders.queue";	
	
	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		PgConnectOptions connectOptions = new PgConnectOptions()
				.setPort(5432)
				.setHost("localhost")
				.setDatabase("vertx-jooq-cr")
				.setUser("postgres").setPassword("postgres"); // DB User credentials

		PoolOptions poolOptions = new PoolOptions().setMaxSize(30);
		PgPool pgClient = PgPool.pool(vertx, connectOptions, poolOptions);

		// setting up JOOQ configuration
		Configuration configuration = new DefaultConfiguration();
		configuration.set(SQLDialect.POSTGRES);
		
//		LOGGER.info("About to create ReactiveClassicGenericQueryExecutor instance...");
//		ReactiveClassicGenericQueryExecutor queryExecutor = new ReactiveClassicGenericQueryExecutor(configuration, pgClient);
		
		/* TODO: 001-create ServiceBinder (or whatever) to utilize queryExecutor through all ServiceImpl classes also try to 
		 *   create SEPARATE queue for EACH Service to communicate for it's appropriate (IMPLEMENTATION) class */
		LOGGER.info("About to createCategoryService...");
		CategoryService.createCategoryService(pgClient, configuration, readyHandler -> {
			if (readyHandler.succeeded()) {
				ServiceBinder binder = new ServiceBinder(vertx);
				binder
					.setAddress(CONFIG_CATEGORY_QUEUE)
					.register(CategoryService.class, readyHandler.result());
				startPromise.complete();
			} else {
				LOGGER.error("**********  Error, something failed in CREATING CATEGORY SERVICE!!!! **********");
				startPromise.fail(readyHandler.cause());
			}
		});
		LOGGER.info("Passed createCategoryService(..) !!!");
	}		
}
