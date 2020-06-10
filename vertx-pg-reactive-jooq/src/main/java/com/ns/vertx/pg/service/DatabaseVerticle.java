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
		
		/* TODO: 001-create ServiceBinder (or whatever) to utilize queryExecutor through all ServiceImpl classes also try to 
		 *   create SEPARATE queue for EACH Service to communicate for it's appropriate (IMPLEMENTATION) class */
		LOGGER.info("About to createCategoryService...");
		
		AuthorService.createAuthorService(pgClient, configuration, readyHandler -> {
			if (readyHandler.succeeded()) {				
				ServiceBinder binder = new ServiceBinder(vertx);
				binder
					.setAddress(CONFIG_AUTHOR_QUEUE)
					.register(AuthorService.class, readyHandler.result());
				startPromise.complete();
			} else {
				LOGGER.error("**********  Error, something failed in CREATING AuthorService!!!! **********");
				startPromise.fail(readyHandler.cause());
			}
		});
		
		CategoryService.createCategoryService(pgClient, configuration, readyHandler -> {
			if (readyHandler.succeeded()) {				
				ServiceBinder binder = new ServiceBinder(vertx);
				LOGGER.info("++++++++ setting address 'category.queue' for CategoryService... ++++++++");
				binder
					.setAddress(CONFIG_CATEGORY_QUEUE)
					.register(CategoryService.class, readyHandler.result());
				startPromise.complete();
			} else {
				LOGGER.error("**********  Error, something failed in CREATING CategoryService!!!! **********");
				startPromise.fail(readyHandler.cause());
			}
		});
		
		BookService.createBookService(pgClient, configuration, readyHandler -> {
			if (readyHandler.succeeded()) {
				ServiceBinder binder = new ServiceBinder(vertx);
				LOGGER.info("++++++++ setting address 'book.queue' for BookService... ++++++++");
				binder
					.setAddress(CONFIG_BOOK_QUEUE)
					.register(BookService.class, readyHandler.result());
				startPromise.complete();
			} else {
				LOGGER.error("**********  Error, something failed in CREATING BookService!!!! **********");
				startPromise.fail(readyHandler.cause());
			}
		});
		
		OrderService.createOrderService(pgClient, configuration, readyHandler -> {
			if (readyHandler.succeeded()) {
				ServiceBinder binder = new ServiceBinder(vertx);
				LOGGER.info("++++++++ setting address 'orders.queue' for OrderService... ++++++++");
				binder
					.setAddress(CONFIG_ORDERS_QUEUE)
					.register(OrderService.class, readyHandler.result());
				startPromise.complete();
			} else {
				LOGGER.error("**********  Error, something failed in CREATING OrderService!!!! **********");
				startPromise.fail(readyHandler.cause());
			}
		});
		
	}		
}
