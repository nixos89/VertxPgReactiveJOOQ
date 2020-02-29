package com.ns.vertx.pg;

import static com.ns.vertx.pg.ActionHelper.*;
import static com.ns.vertx.pg.DBQueries.*;

import java.util.NoSuchElementException;

import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.jooq.tables.daos.CategoryDao;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

public class MainVerticle extends AbstractVerticle {

	private final static Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
	
	private PgPool pgClient; 
	
	@Override
	public void start(Promise<Void> startPromise) throws Exception {				
		Router routerREST = Router.router(vertx);
		
		routerREST.get("/categories").handler(this::getAllCategoriesHandler);
		routerREST.get("/categories/:id").handler(this::getCategoryByIdHandler);
		routerREST.delete("/categories/:id").handler(this::deleteCategoryHandler);
		routerREST.post().handler(BodyHandler.create());
		routerREST.post("/categories").handler(this::createCategoryHandler);
		routerREST.put().handler(BodyHandler.create());
		routerREST.put("/categories/:id").handler(this::updateCategoryHandler);
		
		Router routerAPI = Router.router(vertx);
		routerAPI.mountSubRouter("/api", routerREST);
		routerAPI.errorHandler(500, error -> {
			Throwable failure = error.failure();
			if (failure != null) {
				failure.printStackTrace();
			}
		});
		
		PgConnectOptions connectOptions = new PgConnectOptions()
				.setPort(5432).setHost("localhost")
				.setDatabase("vertx-jooq-cr")
				.setUser("postgres").setPassword("postgres");
		
		PoolOptions poolOptions = new PoolOptions().setMaxSize(30);
		pgClient = PgPool.pool(vertx, connectOptions, poolOptions);
		
		// setting up JOOQ configuration
		Configuration configuration= new DefaultConfiguration();
		configuration.set(SQLDialect.POSTGRES);

		//no other DB-Configuration necessary because jOOQ is only used to render our statements - not for execution
		CategoryDao categoryDAO = new CategoryDao(configuration, pgClient);
		
		categoryDAO.findOneById(1L).setHandler(res -> {
			if (res.succeeded()) {
				vertx.eventBus().send("Something", res.result().toJson());		
				LOGGER.info("res.result().toJson() = " + res.result().toJson());
			} else {
				System.err.println("Something failed badly: " + res.cause().getMessage());
			}
		});		
		
		ReactiveClassicGenericQueryExecutor queryExecutor = new ReactiveClassicGenericQueryExecutor(configuration, pgClient);
		Future<Integer> updatedCustom = queryExecutor.execute(dsl -> dsl 
					.update(com.ns.vertx.pg.jooq.tables.Category.CATEGORY)
					.set(com.ns.vertx.pg.jooq.tables.Category.CATEGORY.NAME, "Horror")
					.where(com.ns.vertx.pg.jooq.tables.Category.CATEGORY.CATEGORY_ID.eq(1L))				
				);
		
		updatedCustom.setHandler(res -> {
			if (res.succeeded()) {
				LOGGER.info("Rows updated: " + res.result());
			} else {
				LOGGER.error("Something failed badly (in updatedCustom): " + res.cause().getMessage());
			}
		});		
		
		Future<Void> futureConnection = connect().compose(connection -> {
			Promise<Void> retFuture = Promise.promise(); 
			createTableIfNeeded(connection).future()
				.setHandler(x -> {
					connection.close();
					retFuture.handle(x.mapEmpty());
				});
			return retFuture.future();
		});
		
		futureConnection.compose(v -> createHttpServer(pgClient, routerAPI)).setHandler(startPromise);	
	}// start::END
	
	
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
			 .listen(8080, res -> promise.handle(res.mapEmpty()));
		return promise.future();
	}

	
	private Promise<SqlConnection> createTableIfNeeded(SqlConnection connection) {
		Promise<SqlConnection> promise = Promise.promise();
		pgClient.getConnection(ar1 -> {
			if (ar1.succeeded()) {
				LOGGER.info("Connected!");
				SqlConnection conn = ar1.result();		
				conn.query(CREATE_CATEGORY_TABLE_SQL, ar2 -> {
					if (ar2.succeeded()) {
						promise.handle(ar2.map(conn));
					} else {
						LOGGER.error("Error, executing 'create_category_table_sql' query failed!", ar2.cause());
						conn.close();
					}
				});
			} else {
				LOGGER.error("Error, acquiring DB connection! Cause: ", ar1.cause());
			}
		});	
		return promise;
	}
	
	private JsonObject convertRowSetToJsonObject(RowSet<Row> rs) {
		JsonObject categories = new JsonObject();
		JsonObject category = new JsonObject();
		RowIterator<Row> ri = rs.iterator();
		JsonArray ja = new JsonArray();
		while(ri.hasNext()) {
			Row row = ri.next();
			category.put("category_id", row.getLong(0));
			category.put("name", row.getString(1));
			category.put("is_deleted", row.getBoolean(2));
			ja.add(category);							
			LOGGER.info("category = " + category.encodePrettily());
			category = new JsonObject();
		}
		categories.put("categories", ja);
		return categories;
	}
	
	private void getAllCategoriesHandler(RoutingContext rc) {
		pgClient.getConnection(ar -> {
			if (ar.succeeded()) {
				SqlConnection sqlConnection = ar.result();
				sqlConnection.query(GET_ALL_CATEGORIES_SQL, fetch -> {
					if (fetch.succeeded()) {
						RowSet<Row> rs = fetch.result();
						JsonObject categories = convertRowSetToJsonObject(rs);
						rc.response().setStatusCode(303);
						rc.response().putHeader("Content-Type", "application/json; UTF-8");
						rc.response().end(categories.encodePrettily());
						
						sqlConnection.close();
					} else {
						LOGGER.error("Error, connection not established! Cause: ", fetch.cause());
						sqlConnection.close();
					}
				});
			}
		});
	}
	
	private void getCategoryByIdHandler(RoutingContext rc) {
		pgClient.getConnection(ar -> {
			if (ar.succeeded()) {
				SqlConnection conn = ar.result();
				conn.preparedQuery(GET_CATEGORY_BY_ID_SQL, Tuple.of(Integer.valueOf(rc.request().getParam("id"))), 
						arCID -> {
					if (arCID.succeeded()) {						
						Row row = arCID.result().iterator().next();
						JsonObject category = new JsonObject()
								.put("category_id", row.getLong(0))
								.put("name", row.getString(1))
								.put("is_deleted", row.getBoolean(2));
						
						LOGGER.info("Succeeded in quering category by id!");
						rc.response().setStatusCode(200);
						rc.response().putHeader("Content-Type", "application/json; UTF-8");
						rc.response().end(category.encodePrettily());
					} else {
						LOGGER.error("Error, failed! Cause: ", arCID.cause());
					}
					conn.close();
				});
			} else {
				LOGGER.error("DB connection NOT obtained!", ar.cause());
			}
		});
	}
	
	private void createCategoryHandler(RoutingContext rc) {
		Category category = rc.getBodyAsJson().mapTo(Category.class);
		connect().compose(conn -> createCategory(conn, category, true))
				 .setHandler(created(rc));			
	}
	
	private Future<Category> createCategory(SqlConnection conn, Category category, boolean closeConnection) {
		Promise<Category> promise = Promise.promise();
		conn.preparedQuery(CREATE_CATEGORY_SQL, Tuple.of(category.getName(), category.getIsDeleted()), save -> {
			if (closeConnection) {
				conn.close();
			}			
			int id = save.result().iterator().next().getInteger(0);						
			Category cat = new Category(id, category.getName(), category.getIsDeleted());
			
			promise.handle(Future.succeededFuture(cat));
		});
		return promise.future();
	}
	
	// TODO: edit next 2 following methods for UPDATING Category to use Reactive-Classic-jOOQ implementation !!! 
	private void updateCategoryHandler(RoutingContext rc) {
		int id = Integer.valueOf(rc.request().getParam("id"));
		Category category = rc.getBodyAsJson().mapTo(Category.class);
		connect().compose(connection -> updateCategory(connection, category, id))
				 .setHandler(noContent(rc));
	}
	
	private Future<Void> updateCategory(SqlConnection connection, Category category, int id) {
		Promise<Void> promise = Promise.promise();
		connection.preparedQuery(UPDATE_CATEGORY_SQL, Tuple.of(category.getName(), category.getIsDeleted(), id), update -> {
			if (update.succeeded()) {
				Row row = update.result().iterator().next();
				LOGGER.info("Category id = " + row.getLong(0) + ", name = " + category.getName() 
								+ ", is_deleted = " + category.getIsDeleted() + " has been updated!");
				promise.handle(Future.succeededFuture());
			} else {				
				promise.handle(Future.failedFuture(update.cause()));
			}
		});
		
		return promise.future();
	}
	
	private Future<Void> deleteCategory(SqlConnection connection, int id) {
		Promise<Void> promise = Promise.promise();
		connection.preparedQuery(DBQueries.DELETE_CATEGORY_BY_ID_SQL, Tuple.of(id), fetch -> {
			if (fetch.succeeded()) {
				promise.handle(Future.succeededFuture());
			} else {
				promise.handle(Future.failedFuture(new NoSuchElementException("No category with id = " + id)));
			}					
		});
		return promise.future();
	}
	
	
	private void deleteCategoryHandler(RoutingContext rc) {
		int id = Integer.valueOf(rc.request().getParam("id"));
		connect().compose(connection -> deleteCategory(connection, id)).setHandler(noContent(rc));
	}
	
}
