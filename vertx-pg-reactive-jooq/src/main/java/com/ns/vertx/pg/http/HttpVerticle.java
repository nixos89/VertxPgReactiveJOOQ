package com.ns.vertx.pg.http;

import static com.ns.vertx.pg.http.ActionHelper.created;
import static com.ns.vertx.pg.http.ActionHelper.noContent;
import static com.ns.vertx.pg.http.ActionHelper.ok;

import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.jooq.tables.pojos.Author;
import com.ns.vertx.pg.jooq.tables.pojos.Category;
import com.ns.vertx.pg.service.AuthorServiceImpl;
import com.ns.vertx.pg.service.BookServiceImpl;
import com.ns.vertx.pg.service.CategoryServiceImpl;
import com.ns.vertx.pg.service.OrderServiceImpl;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;


public class HttpVerticle extends AbstractVerticle {

	private final static Logger LOGGER = LoggerFactory.getLogger(HttpVerticle.class);
	private static int LISTEN_PORT = 8080;

	private PgPool pgClient;
	private Configuration configuration;
	private ReactiveClassicGenericQueryExecutor queryExecutor;	
		
	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		Router routerREST = Router.router(vertx);
		routerREST.post().handler(BodyHandler.create());
		routerREST.put().handler(BodyHandler.create());		
		// Authors REST API
		routerREST.get("/authors").handler(this::getAllAuthorsHandler);
		routerREST.get("/authors/:id").handler(this::getAuthorByIdHandler);				
		routerREST.post("/authors").handler(this::createAuthorHandler);
		routerREST.put("/authors/:id").handler(this::updateAuthorHandler);
		routerREST.delete("/authors/:id").handler(this::deleteAuthorHandler);				
		// Categories REST API		
		routerREST.get("/categories").handler(this::getAllCategoriesHandler);
		routerREST.get("/categories/:id").handler(this::getCategoryByIdHandler);				
		routerREST.post("/categories").handler(this::createCategoryHandler);
		routerREST.put("/categories/:id").handler(this::updateCategoryHandler);
		routerREST.delete("/categories/:id").handler(this::deleteCategoryHandler);
		// Books REST API		
		routerREST.get("/books").handler(this::getAllBooksHandlerJooq);
		routerREST.get("/authors/:id/books").handler(this::getAllBooksByAuthorIdHandler);
		routerREST.get("/books/:id").handler(this::getBookByIdHandler);				
		routerREST.post("/books").handler(this::createBookHandler);
		routerREST.put("/books/:id").handler(this::updateBookHandler);
		routerREST.delete("/books/:id").handler(this::deleteBookHandler);		
		// Orders REST API
		routerREST.get("/orders").handler(this::getAllOrdersHandler);
		routerREST.post("/orders").handler(this::createOrderHandler);
		
		Router routerAPI = Router.router(vertx);
		routerAPI.mountSubRouter("/api", routerREST);
		routerAPI.errorHandler(500, error -> {
			Throwable failure = error.failure();
			if (failure != null) {
				failure.printStackTrace();
			}
		});

		PgConnectOptions connectOptions = new PgConnectOptions()
			.setPort(5432)
			.setHost("localhost")
			.setDatabase("vertx-jooq-cr")
			.setUser("postgres").setPassword("postgres"); // DB User credentials

		PoolOptions poolOptions = new PoolOptions().setMaxSize(30);
		pgClient = PgPool.pool(vertx, connectOptions, poolOptions);

		// setting up JOOQ configuration
		configuration = new DefaultConfiguration();
		configuration.set(SQLDialect.POSTGRES);
		
		/* NOT: D connection is AUTOMATICALY CLOSED! More info at:
		 * https://www.jooq.org/doc/3.11/manual/getting-started/tutorials/jooq-in-7-steps/jooq-in-7-steps-step5/ */
		queryExecutor = new ReactiveClassicGenericQueryExecutor(configuration, pgClient);
		// no other DB-Configuration necessary because jOOQ is only used to render our statements - not for execution		
		
		createHttpServer(routerAPI).onComplete(startPromise);	
	}// start::END


	public Future<Void> createHttpServer(Router router) {
		Promise<Void> promise = Promise.promise();		
		vertx.createHttpServer()
			 .requestHandler(router)
			 .listen(LISTEN_PORT, res -> promise.handle(res.mapEmpty()));
		return promise.future();
	}
	
	private void getAllAuthorsHandler(RoutingContext rc) {
		AuthorServiceImpl.getAllAuthorsJooq(queryExecutor).onComplete((ok(rc)));				
	}	

	private void getAllCategoriesHandler(RoutingContext rc) {
		CategoryServiceImpl.getAllCategoriesJooq(queryExecutor).onComplete((ok(rc)));				
	}
	
	private void getAuthorByIdHandler(RoutingContext rc) {
		Long id = Long.valueOf(rc.request().getParam("id"));
		AuthorServiceImpl.getAuthorByIdJooq(queryExecutor, id).onComplete(ok(rc));
	}
	
	private void getCategoryByIdHandler(RoutingContext rc) {
		Long id = Long.valueOf(rc.request().getParam("id"));
		CategoryServiceImpl.getCategoryByIdJooq(queryExecutor, id).onComplete(ok(rc));
	}	
	
	private void getAllBooksHandlerJooq(RoutingContext rc) {		
		BookServiceImpl.getAllBooksJooq(queryExecutor).onComplete(ok(rc));
	}
	
	private void getAllBooksByAuthorIdHandler(RoutingContext rc) {
		Long authorId = Long.valueOf(rc.request().getParam("id"));
		BookServiceImpl.getAllBooksByAuthorIdJooqMix(queryExecutor, authorId).onComplete(ok(rc));
	}
	

	private void getBookByIdHandler(RoutingContext rc) {
		Long id = Long.valueOf(rc.request().getParam("id"));
		BookServiceImpl.getBookByIdJooq(queryExecutor, id).onComplete((ok(rc)));
	}
	
	private void createAuthorHandler(RoutingContext rc) {
		JsonObject json = rc.getBodyAsJson();
		AuthorServiceImpl
			.createAuthorJooq(queryExecutor, json.getString("first_name"), json.getString("last_name"))
			.onComplete(created(rc));
	}
	
	private void createCategoryHandler(RoutingContext rc) {
		JsonObject json = rc.getBodyAsJson();
		CategoryServiceImpl
			.createCategoryJooq(queryExecutor, json.getString("name"), json.getBoolean("is_deleted"))
			.onComplete(created(rc));
	}
	
	
	private void createBookHandler(RoutingContext rc) {
		JsonObject bookJO = rc.getBodyAsJson();		
		LOGGER.info("In 'createBookHandler(..)' bookJO =\n" + bookJO.encodePrettily());		
		BookServiceImpl.createBookJooq(queryExecutor, bookJO).onComplete(created(rc));
	}

	private void updateAuthorHandler(RoutingContext rc) {
		Long id =  Long.valueOf(rc.request().getParam("id"));
		Author authorPojo = new Author(rc.getBodyAsJson());
		authorPojo.setAuthorId(id);
		LOGGER.info("(in updateAuthorHandler) authorPojo.toString(): " + authorPojo.toString());
		AuthorServiceImpl.updateAuthorJooq(queryExecutor, authorPojo).onComplete(noContent(rc));
	}	
	
	
	private void updateCategoryHandler(RoutingContext rc) {
		Long id =  Long.valueOf(rc.request().getParam("id"));
		Category categoryPojo = new Category(rc.getBodyAsJson());
		categoryPojo.setCategoryId(id);
		LOGGER.info("(in updateCategoryHandler) categoryPojo.toString(): " + categoryPojo.toString());
		CategoryServiceImpl.updateCategoryJooq(queryExecutor, categoryPojo).onComplete(noContent(rc));
	}	
	
	private void updateBookHandler(RoutingContext rc) {
		long id = (long) Integer.valueOf(rc.request().getParam("id"));
		JsonObject bookJO = rc.getBodyAsJson();
		bookJO.put("book_id", id);
		BookServiceImpl.updateBookJooq(queryExecutor, bookJO, id)
		   .onComplete( (ok(rc)) );
	}
	
	private void deleteAuthorHandler(RoutingContext rc) {
		Long id =  Long.valueOf(rc.request().getParam("id"));
		AuthorServiceImpl.deleteAuthorJooq(queryExecutor, id).onComplete(noContent(rc));
	}
	
	private void deleteCategoryHandler(RoutingContext rc) {
		Long id =  Long.valueOf(rc.request().getParam("id"));
		CategoryServiceImpl.deleteCategoryJooq(queryExecutor, id).onComplete(noContent(rc));		
	}
	
	private void deleteBookHandler(RoutingContext rc) {
		Long id =  Long.valueOf(rc.request().getParam("id"));
		BookServiceImpl.deleteBookJooq(queryExecutor, id).onComplete(noContent(rc));
	}
	
	private void getAllOrdersHandler(RoutingContext rc) {
		OrderServiceImpl.getAllOrdersJooq3(queryExecutor).onComplete(ok(rc));
	}

	
	private void createOrderHandler(RoutingContext rc) {
		JsonObject orderJO = rc.getBodyAsJson();
		LOGGER.info("about to read username from URL...");
		MultiMap parameters = rc.request().params();	    
		String username = parameters.get("username");
		LOGGER.info("username: " + username);
		OrderServiceImpl.createOrderJooq(queryExecutor, orderJO, username).onComplete(created(rc));
	}

}
