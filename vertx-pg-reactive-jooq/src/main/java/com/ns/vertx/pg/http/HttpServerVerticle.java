package com.ns.vertx.pg.http;

import static com.ns.vertx.pg.http.ActionHelper.created;
import static com.ns.vertx.pg.http.ActionHelper.noContent;
import static com.ns.vertx.pg.http.ActionHelper.ok;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.jooq.tables.pojos.Category;
import com.ns.vertx.pg.service.AuthorService;
import com.ns.vertx.pg.service.BookService;
import com.ns.vertx.pg.service.CategoryService;
import com.ns.vertx.pg.service.OrderService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
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
	private AuthorService authorService;
	private BookService bookService;
	private OrderService orderService;	
		
	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		String categoryAddress = config().getString(CONFIG_CATEGORYDB_QUEUE, "category.queue");
		String authorAddress = config().getString(CONFIG_AUTHORDB_QUEUE, "author.queue");
		String bookAddress = config().getString(CONFIG_BOOKDB_QUEUE, "book.queue");
		String orderAddress = config().getString(CONFIG_ORDERSDB_QUEUE, "orders.queue");
		
		authorService = AuthorService.createAuthorProxy(vertx, authorAddress);
		categoryService = CategoryService.createCategoryProxy(vertx, categoryAddress);
		bookService = BookService.createBookProxy(vertx, bookAddress);
		orderService = OrderService.createOrderProxy(vertx, orderAddress);
		
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
		// Orders REST API
		
		routerREST.post("/orders").handler(this::createOrderHandler);
//		Router routerRESTNonEventLoop = Router.router(vertx);
		
		routerREST.get("/orders").blockingHandler(this::getAllOrdersHandler, false);		
		
		Router routerAPI = Router.router(vertx);
		routerAPI.mountSubRouter("/api", routerREST);
//		routerAPI.mountSubRouter("/api", routerRESTNonEventLoop);
		routerAPI.errorHandler(500, error -> {
			Throwable failure = error.failure();
			if (failure != null) {
				LOGGER.error("failure.printStackTrace(): " + failure.getMessage());
				failure.printStackTrace();
			}
		});
		
		LOGGER.info("Whole setUp in in start() went well...");
		HttpServer server = vertx.createHttpServer();
		int portNumber =  config().getInteger(CONFIG_HTTP_SERVER_PORT, LISTEN_PORT);
		server.exceptionHandler(handler -> LOGGER.error("exceptionHandler triggered, " + handler.getCause()));		
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

	
	// ******************************************************************	
	// ********************* Author handler methods *********************
	private void getAllAuthorsHandler(RoutingContext rc) {
		authorService.getAllAuthorsJooqSP(ok(rc));
	}		
	
	private void getAuthorByIdHandler(RoutingContext rc) {		
		Long id = Long.valueOf(rc.request().getParam("id"));
//		LOGGER.info("Thread: " + Thread.currentThread() + " recieveing author id = " + id);
		authorService.getAuthorByIdJooqSP(id, ok(rc));
	}
	
	private void createAuthorHandler(RoutingContext rc) {
		JsonObject json = rc.getBodyAsJson();
		authorService.createAuthorJooqSP(json.getString("firstName"), 
				json.getString("lastName"), created(rc));
	}
	
	private void updateAuthorHandler(RoutingContext rc) {
		Long id =  Long.valueOf(rc.request().getParam("id"));
		JsonObject authorJO = rc.getBodyAsJson();
		authorJO.put("authorId", id);
		authorService.updateAuthorJooqSP(authorJO, noContent(rc));
	}	
	
	private void deleteAuthorHandler(RoutingContext rc) {
		Long id =  Long.valueOf(rc.request().getParam("id"));
		authorService.deleteAuthorJooqSP(id, noContent(rc));
	}
	
	// ******************************************************************
	// ********************* Category handler methods *******************
	private void getAllCategoriesHandler(RoutingContext rc) {
		categoryService.getAllCategoriesJooqSP(ok(rc));
	}
	
	private void getCategoryByIdHandler(RoutingContext rc) {
		Long categoryId = Long.valueOf(rc.request().getParam("id"));
		categoryService.getCategoryByIdJooqSP(categoryId, ok(rc));
	}	
	
	private void updateCategoryHandler(RoutingContext rc) {
		Long id =  Long.valueOf(rc.request().getParam("id"));
		Category categoryPojo = new Category(rc.getBodyAsJson());
		categoryPojo.setCategoryId(id);
		JsonObject categoryJO = rc.getBodyAsJson();
		categoryJO.put("categoryId", id);
		categoryService.updateCategoryJooqSP(categoryJO, noContent(rc));
	}
	
	private void createCategoryHandler(RoutingContext rc) {
		JsonObject json = rc.getBodyAsJson();
		categoryService
			.createCategoryJooqSP(json.getString("name"), json.getBoolean("isDeleted"), created(rc));
	}
	
	private void deleteCategoryHandler(RoutingContext rc) {
		Long id =  Long.valueOf(rc.request().getParam("id"));
		categoryService.deleteCategoryJooqSP(id, noContent(rc));		
	}
	
	
	// **************************************************************
	// ********************* Book handler methods *******************
	private void getAllBooksHandlerJooq(RoutingContext rc) {		
		bookService.getAllBooksJooqSP(ok(rc));
	}
	
	private void getBookByIdHandler(RoutingContext rc) {
		Long id = Long.valueOf(rc.request().getParam("id"));
		bookService.getBookByIdJooqSP(id, ok(rc));
	}	
	
	private void getAllBooksByAuthorIdHandler(RoutingContext rc) {
		Long authorId = Long.valueOf(rc.request().getParam("id"));
		bookService.getAllBooksByAuthorIdJooqSP(authorId, ok(rc));
	}
	
	private void createBookHandler(RoutingContext rc) {
		JsonObject bookJO = rc.getBodyAsJson();						
		bookService.createBookJooqSP(bookJO, created(rc));
	}
	
	private void updateBookHandler(RoutingContext rc) {
		Long id = Long.valueOf(rc.request().getParam("id"));
		JsonObject bookJO = rc.getBodyAsJson();
		bookJO.put("book_id", id);
		bookService.updateBookJooqSP(bookJO, noContent(rc));
	}
		
	private void getAllOrdersHandler(RoutingContext rc) {
//		LOGGER.info("Thread: " + Thread.currentThread() + " invoked for getting ALL Orders!");
		orderService.getAllOrdersJooqSP(ok(rc));
	}
	
	private void createOrderHandler(RoutingContext rc) {
		JsonObject orderJO = rc.getBodyAsJson();
		MultiMap parameters = rc.request().params();	    
		String username = parameters.get("username");
		orderService.createOrderJooqSP(orderJO, username, created(rc));
	}
}
