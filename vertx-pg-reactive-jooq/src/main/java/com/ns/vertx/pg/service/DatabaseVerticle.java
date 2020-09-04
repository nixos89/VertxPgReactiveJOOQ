package com.ns.vertx.pg.service;

import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.sqlclient.PoolOptions;

public class DatabaseVerticle extends AbstractVerticle {
	
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

		PoolOptions poolOptions = new PoolOptions().setMaxSize(100); // changed from 2000 to 100
		PgPool pgClient = PgPool.pool(vertx, connectOptions, poolOptions);

		// setting up JOOQ configuration
		Configuration configuration = new DefaultConfiguration();
		configuration.set(SQLDialect.POSTGRES);	
		
		
		AuthorService.createAuthorService(pgClient, configuration, readyHandler -> {
			if (readyHandler.succeeded()) {				
				ServiceBinder binder = new ServiceBinder(vertx);
				binder
					.setAddress(CONFIG_AUTHOR_QUEUE)
					.register(AuthorService.class, readyHandler.result());
				startPromise.complete();
			} else {
				startPromise.fail(readyHandler.cause());
			}
		});
		
		CategoryService.createCategoryService(pgClient, configuration, readyHandler -> {
			if (readyHandler.succeeded()) {				
				ServiceBinder binder = new ServiceBinder(vertx);
				binder
					.setAddress(CONFIG_CATEGORY_QUEUE)
					.register(CategoryService.class, readyHandler.result());
				startPromise.complete();
			} else {
				startPromise.fail(readyHandler.cause());
			}
		});
		
		BookService.createBookService(pgClient, configuration, readyHandler -> {
			if (readyHandler.succeeded()) {
				ServiceBinder binder = new ServiceBinder(vertx);
				binder
					.setAddress(CONFIG_BOOK_QUEUE)
					.register(BookService.class, readyHandler.result());
				startPromise.complete();
			} else {
				startPromise.fail(readyHandler.cause());
			}
		});
		
		OrderService.createOrderService(pgClient, configuration, readyHandler -> {
			if (readyHandler.succeeded()) {
				ServiceBinder binder = new ServiceBinder(vertx);
				binder
					.setAddress(CONFIG_ORDERS_QUEUE)
					.register(OrderService.class, readyHandler.result());
				startPromise.complete();
			} else {
				startPromise.fail(readyHandler.cause());
			}
		});
		
	}		
}
