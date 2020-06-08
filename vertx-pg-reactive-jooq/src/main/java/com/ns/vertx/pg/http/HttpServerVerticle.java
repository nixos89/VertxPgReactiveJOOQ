package com.ns.vertx.pg.http;

import static com.ns.vertx.pg.http.ActionHelper.created;
import static com.ns.vertx.pg.http.ActionHelper.noContent;
import static com.ns.vertx.pg.http.ActionHelper.ok;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.jooq.tables.pojos.Category;
import com.ns.vertx.pg.service.CategoryService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;


public class HttpServerVerticle extends AbstractVerticle {

	private final static Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);
	private static int LISTEN_PORT = 8080;
	private static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";
	
	public static final String CONFIG_AUTHORDB_QUEUE = "author.queue";
	public static final String CONFIG_BOOKDB_QUEUE = "book.queue";
	public static final String CONFIG_CATEGORYDB_QUEUE = "category.queue";		
	public static final String CONFIG_ORDERSDB_QUEUE = "orders.queue";
	
	private CategoryService categoryService;
//	private AuthorService authorService;
//	private BookService bookService;
//	private OrderService orderService;
	
		
	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		String categoryAddress = config().getString(CONFIG_CATEGORYDB_QUEUE, "category.queue");
//		String authorAddress = config().getString(CONFIG_AUTHORDB_QUEUE, "author.queue");
//		String bookAddress = config().getString(CONFIG_BOOKDB_QUEUE, "book.queue");
//		String orderAddress = config().getString(CONFIG_ORDERSDB_QUEUE, "orders.queue");
		
		categoryService = CategoryService.createCategoryProxy(vertx, categoryAddress);
		// TODO: uncomment + initialize (same uncommented) service instances
		
		
		
		Router routerREST = Router.router(vertx);
		routerREST.post().handler(BodyHandler.create());
		routerREST.put().handler(BodyHandler.create());		
		
		// Categories REST API		
		routerREST.get("/categories").handler(this::getAllCategoriesHandler);
		routerREST.get("/categories/:id").handler(this::getCategoryByIdHandler);				
		routerREST.post("/categories").handler(this::createCategoryHandler);
		routerREST.put("/categories/:id").handler(this::updateCategoryHandler);
		routerREST.delete("/categories/:id").handler(this::deleteCategoryHandler);
		
		Router routerAPI = Router.router(vertx);
		routerAPI.mountSubRouter("/api", routerREST);
		routerAPI.errorHandler(500, error -> {
			Throwable failure = error.failure();
			if (failure != null) {
				failure.printStackTrace();
			}
		});
		
		LOGGER.info("Whole setUp in in start() went well...");
		// FIXME: 001-code DIRECTLY here CREATION of HttpServer
		HttpServer server = vertx.createHttpServer();
		int portNumber =  config().getInteger(CONFIG_HTTP_SERVER_PORT, LISTEN_PORT);
		server.requestHandler(routerAPI).listen(portNumber, ar -> {
			if (ar.succeeded()) {
				LOGGER.info("HTTP Server running on port " + portNumber);
				startPromise.complete();
			} else {
				LOGGER.error("Colud NOT start HTTP server!!!! Cause: " + ar.cause());
				startPromise.fail(ar.cause());
			}
		});		
	}// start::END


	public Future<Void> createHttpServer(Router router) {
		Promise<Void> promise = Promise.promise();		
		vertx.createHttpServer()
			 .requestHandler(router)
			 .listen(LISTEN_PORT, res -> {
				 if(res.succeeded()) {
					 LOGGER.info("Success, HttpServer running on port = " + LISTEN_PORT);
					 promise.complete();
				 } else {
					 LOGGER.error("Could NOT start HttpServer! Cause: " + res.cause());
					 promise.fail(res.cause());
				 }
			 });
		return promise.future();
	}
	

	private void getAllCategoriesHandler(RoutingContext rc) {
		categoryService.getAllCategoriesJooqSP(ok(rc));				
	}
	
	private void getCategoryByIdHandler(RoutingContext rc) {
		Long id = Long.valueOf(rc.request().getParam("id"));
		categoryService.getCategoryByIdJooqSP(id, ok(rc));
	}	
	
	private void updateCategoryHandler(RoutingContext rc) {
		Long id =  Long.valueOf(rc.request().getParam("id"));
		Category categoryPojo = new Category(rc.getBodyAsJson());
		categoryPojo.setCategoryId(id);
		JsonObject categoryJO = categoryPojo.toJson();
		categoryService.updateCategoryJooqSP(categoryJO, noContent(rc));
	}
	
	private void createCategoryHandler(RoutingContext rc) {
		JsonObject json = rc.getBodyAsJson();
		categoryService
			.createCategoryJooqSP(json.getString("name"), json.getBoolean("is_deleted"), created(rc));
	}
	
	private void deleteCategoryHandler(RoutingContext rc) {
		Long id =  Long.valueOf(rc.request().getParam("id"));
		categoryService.deleteCategoryJooqSP(id, noContent(rc));		
	}
	
	/*
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
	
	private void getAllBooksHandlerJooq(RoutingContext rc) {		
		BookServiceImpl.getAllBooksJooq(queryExecutor).onComplete(ok(rc));
	}
	
	private void getAllBooksByAuthorIdHandler(RoutingContext rc) {
		Long authorId = Long.valueOf(rc.request().getParam("id"));
		BookServiceImpl.getAllBooksByAuthorIdJooq(queryExecutor, authorId).onComplete(ok(rc));
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
		BookServiceImpl.createBookJooq(queryExecutor, bookJO).onComplete(created(rc));
	}

	private void updateAuthorHandler(RoutingContext rc) {
		Long id =  Long.valueOf(rc.request().getParam("id"));
		Author authorPojo = new Author(rc.getBodyAsJson());
		authorPojo.setAuthorId(id);
		AuthorServiceImpl.updateAuthorJooq(queryExecutor, authorPojo).onComplete(noContent(rc));
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
		OrderServiceImpl.getAllOrdersJooq(queryExecutor).onComplete(ok(rc));
	}

	
	private void createOrderHandler(RoutingContext rc) {
		JsonObject orderJO = rc.getBodyAsJson();
		MultiMap parameters = rc.request().params();	    
		String username = parameters.get("username");
		OrderServiceImpl.createOrderJooq(queryExecutor, orderJO, username).onComplete(created(rc));
	}
*/
}
