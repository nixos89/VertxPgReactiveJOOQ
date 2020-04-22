package com.ns.vertx.pg;

import static com.ns.vertx.pg.ActionHelper.noContent;
import static com.ns.vertx.pg.ActionHelper.ok;
import static com.ns.vertx.pg.DBQueries.CREATE_CATEGORY_TABLE_SQL;

import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.jooq.tables.daos.AuthorBookDao;
import com.ns.vertx.pg.jooq.tables.daos.BookDao;
import com.ns.vertx.pg.jooq.tables.daos.CategoryBookDao;
import com.ns.vertx.pg.jooq.tables.pojos.Category;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;


// MainVerticle is ALSO HttpVerticle in this scenario
public class HttpVerticle extends AbstractVerticle {

	private final static Logger LOGGER = LoggerFactory.getLogger(HttpVerticle.class);
	private static int LISTEN_PORT = 8080;

	private PgPool pgClient;
	private ReactiveClassicGenericQueryExecutor queryExecutor;
	private BookDao bookDAO;
	private AuthorBookDao authorBookDAO;
	private CategoryBookDao categoryBookDAO;

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		Router routerREST = Router.router(vertx);
		
		// CATEGORIES REST API
		routerREST.get("/categories").handler(this::getAllCategoriesHandlerJooq);
		routerREST.get("/categories/:id").handler(this::getCategoryByIdJooqHandler);
		routerREST.delete("/categories/:id").handler(this::deleteCategoryHandler);
		routerREST.post().handler(BodyHandler.create());
		routerREST.put().handler(BodyHandler.create());
		routerREST.post("/categories").handler(this::createCategoryHandler);
		routerREST.put("/categories/:id").handler(this::updateCategoryHandler);

		// BOOKS REST API		
		routerREST.get("/books").handler(this::getAllBooksHandlerJooq);
		routerREST.get("/books/:id").handler(this::getBookByIdHandler);		
		routerREST.post().handler(BodyHandler.create());
		routerREST.put().handler(BodyHandler.create());
		routerREST.post("/books").handler(this::createBookHandler);
		routerREST.put("/books/:id").handler(this::updateBookHandler);
		routerREST.delete("/books/:id").handler(this::deleteBookHandler);
		
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
		Configuration configuration = new DefaultConfiguration();
		configuration.set(SQLDialect.POSTGRES);
		bookDAO = new BookDao(configuration, pgClient);
		authorBookDAO = new AuthorBookDao(configuration, pgClient);
		categoryBookDAO = new CategoryBookDao(configuration, pgClient);

		/* TODO: Check is DB conn CLOSED? Because of this 'DSLContext doesn't close the connection.' @:
		 * https://www.jooq.org/doc/3.11/manual/getting-started/tutorials/jooq-in-7-steps/jooq-in-7-steps-step5/ */
		queryExecutor = new ReactiveClassicGenericQueryExecutor(configuration, pgClient);
		// ================================================================================================
		// ========================== Testing classic-reactive-jOOQ:: START ===============================
		// no other DB-Configuration necessary because jOOQ is only used to render our
		// statements - not for execution		
		
		Future<Void> futureConnection = connect().compose(connection -> {
			Promise<Void> retFuture = Promise.promise();
			createTableIfNeeded().future().setHandler(x -> {
				connection.close();
				retFuture.handle(x.mapEmpty());
			});
			return retFuture.future();
		});
		
		futureConnection
			.compose(v -> createHttpServer(pgClient, routerAPI))
			.setHandler(startPromise);
	}// start::END
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++	
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	public Future<SqlConnection> connect() {
		Promise<SqlConnection> promise = Promise.promise();
		pgClient.getConnection(ar -> {
			if (ar.succeeded()) {
				promise.handle(ar.map(connection -> connection));
			}
		});
		return promise.future();
	}

	public Future<Void> createHttpServer(PgPool pgClient, Router router) {
		Promise<Void> promise = Promise.promise();
		
		vertx.createHttpServer()
			 .requestHandler(router)
			 .listen(LISTEN_PORT, res -> promise.handle(res.mapEmpty()));
		return promise.future();
	}

	private Promise<SqlConnection> createTableIfNeeded(/* SqlConnection connection */) {
		Promise<SqlConnection> promise = Promise.promise();
		pgClient.getConnection(ar1 -> {
			if (ar1.succeeded()) {
				LOGGER.info("Connected!");
				SqlConnection conn = ar1.result();
				conn.query(CREATE_CATEGORY_TABLE_SQL, rs -> {
					if (rs.succeeded()) {
						promise.handle(rs.map(conn));
					} else {
						LOGGER.error("Error, executing 'create_category_table_sql' "
								+ "query failed!", rs.cause());
						conn.close();
					}
				});
			} else {
				LOGGER.error("Error, acquiring DB connection! Cause: ", ar1.cause());
			}
		});
		return promise;
	}

	private void getAllCategoriesHandlerJooq(RoutingContext rc) {
		Future<JsonObject> future = CategoryJooqQueries.getAllCategoriesJooq(queryExecutor);
		future.setHandler(ok(rc));		
	}
	
	// FIXME: fix handling of NON-EXISTING 'category_id' which has been sent in HTTP GET request!!!
	private void getCategoryByIdJooqHandler(RoutingContext rc) {
		long id = Long.valueOf(rc.request().getParam("id"));
		Future<JsonObject> future = CategoryJooqQueries.getCategoryByIdJooq(queryExecutor, id);
		future.setHandler(ok(rc));		
	}
	
	
	private void getAllBooksHandlerJooq(RoutingContext rc) {
		BookJooqQueries.getAllBooksJooq(queryExecutor).setHandler(ok(rc));		
	}
	
	// FIXME: fix handling of NON-EXISTING 'book_id' which has been sent in HTTP GET request!!!
	private void getBookByIdHandler(RoutingContext rc) {
		long id = Long.valueOf(rc.request().getParam("id"));
		BookJooqQueries.getBookByIdJooq(queryExecutor, id).setHandler(ok(rc));		
	}
	
	
	private void createCategoryHandler(RoutingContext rc) {
		JsonObject json = rc.getBodyAsJson();
		CategoryJooqQueries
			.createCategoryJooq(queryExecutor, json.getString("name"), json.getBoolean("is_deleted"))
			.setHandler(ok(rc));
	}
	
	
	private void createBookHandler(RoutingContext rc) {
		JsonObject bookJO = rc.getBodyAsJson();		
		LOGGER.info("In 'createBookHandler(..)' bookJO = " + bookJO.encodePrettily());
		BookJooqQueries
			.createBookJooq(queryExecutor, bookDAO, authorBookDAO, categoryBookDAO, bookJO)
			.setHandler(ok(rc));
	}

	
	private void updateCategoryHandler(RoutingContext rc) {
		long id = (long) Integer.valueOf(rc.request().getParam("id"));
		Category categoryPojo = new Category(rc.getBodyAsJson());
		CategoryJooqQueries.updateCategoryJooq(queryExecutor, categoryPojo, id).setHandler(ok(rc));
	}	
	
	private void updateBookHandler(RoutingContext rc) {
		long id = (long) Integer.valueOf(rc.request().getParam("id"));
		JsonObject bookJO = rc.getBodyAsJson();
		bookJO.put("book_id", id);
		BookJooqQueries.updateBookJooq(queryExecutor, bookJO, bookDAO, authorBookDAO, categoryBookDAO, id)
					   .setHandler(ok(rc));
	}
	
	private void deleteCategoryHandler(RoutingContext rc) {
		long id = Long.valueOf(rc.request().getParam("id"));
		CategoryJooqQueries.deleteCategoryJooq(queryExecutor, id).setHandler(noContent(rc));		
	}
	
	private void deleteBookHandler(RoutingContext rc) {
		long id = Long.valueOf(rc.request().getParam("id"));
		BookJooqQueries.deleteBookJooq(queryExecutor, id).setHandler(noContent(rc));		
	}
	

}
