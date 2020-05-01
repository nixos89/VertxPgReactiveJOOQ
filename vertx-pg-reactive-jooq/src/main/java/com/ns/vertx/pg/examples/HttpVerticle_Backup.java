package com.ns.vertx.pg.examples;

import static com.ns.vertx.pg.examples.ActionHelper.*;
import static com.ns.vertx.pg.DBQueries.*;

import java.util.NoSuchElementException;

import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.DBQueries;
import com.ns.vertx.pg.jooq.tables.Category;
import com.ns.vertx.pg.jooq.tables.daos.CategoryDao;
import com.ns.vertx.pg.jooq.tables.interfaces.ICategory;

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
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

public class HttpVerticle_Backup extends AbstractVerticle {

	private final static Logger LOGGER = LoggerFactory.getLogger(HttpVerticle_Backup.class);
	private static int LISTEN_PORT = 8080;

	private PgPool pgClient;
	private ReactiveClassicGenericQueryExecutor queryExecutor;

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		Router routerREST = Router.router(vertx);
		routerREST.get("/categories").handler(this::getAllCategoriesHandler);
		routerREST.get("/categories/:id").handler(this::getCategoryByIdHandler);
		routerREST.delete("/categories/:id").handler(this::deleteCategoryHandler);
		routerREST.post().handler(BodyHandler.create());
		routerREST.put().handler(BodyHandler.create());
		routerREST.post("/categories").handler(this::createCategoryHandler);
		routerREST.put("/categories/:id").handler(this::updateCategoryHandler);

		Router routerAPI = Router.router(vertx);
		routerAPI.mountSubRouter("/api", routerREST);
		routerAPI.errorHandler(500, error -> {
			Throwable failure = error.failure();
			if (failure != null) {
				failure.printStackTrace();
			}
		});

		System.out.println("===============================================================================");
		// prints all EXISTING (sub)routes
		routerREST.getRoutes().forEach(r -> {
//			Route route = r;
//			Route subRoute = r.subRouter(subRouter)
			System.out.println("Route: " + r.getPath());
		});
		System.out.println("===============================================================================");

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

		// ================================================================================================
		// ========================== Testing classic-reactive-jOOQ:: START ===============================
		// no other DB-Configuration necessary because jOOQ is only used to render our
		// statements - not for execution
		CategoryDao categoryDAO = new CategoryDao(configuration, pgClient);
		categoryDAO.findOneById(1L).setHandler(res -> {
			if (res.succeeded()) {
				//
				vertx.eventBus().send("Something", res.result().toJson());
				LOGGER.info("res.result().toJson() = " + res.result().toJson()
						+ "\n(res.result()) instanceof com.ns.vertx.pg.jooq.tables.pojos.Category <=> "
						+ ((res.result()) instanceof com.ns.vertx.pg.jooq.tables.pojos.Category));
			} else {
				System.err.println("Something failed badly: " + res.cause().getMessage());
			}
		});

		queryExecutor = new ReactiveClassicGenericQueryExecutor(configuration, pgClient);
		// UPDATING Category with hard-coded values
		Future<Integer> updatedCategory = queryExecutor
				.execute(dsl -> dsl.update(com.ns.vertx.pg.jooq.tables.Category.CATEGORY)
						.set(com.ns.vertx.pg.jooq.tables.Category.CATEGORY.NAME, "Virus Horror")
						.where(com.ns.vertx.pg.jooq.tables.Category.CATEGORY.CATEGORY_ID.eq(1L)));
		updatedCategory.setHandler(res -> {
			if (res.succeeded()) {
				LOGGER.info("Rows updated: " + res.result());
			} else {
				LOGGER.error("Something failed badly (in updatedCategory): " + res.cause().getMessage());
			}
		});
		


		// INSERTING Category with hard-coded values

//		Future<Integer> insertCategory = queryExecutor.execute(dsl -> dsl
//			.insertInto(com.ns.vertx.pg.jooq.tables.Category.CATEGORY, com.ns.vertx.pg.jooq.tables.Category.CATEGORY.NAME, 
//				com.ns.vertx.pg.jooq.tables.Category.CATEGORY.IS_DELETED)
//			.values("Adventure", false)
//			.values("Adventure", false)
//			.values("Adventure", false)
//			.values("Adventure", false)
//			.values("Adventure", false)
//			.values("History", false)
//			.values("Sport", true)
//		);
//		insertCategory.setHandler(res -> {
//			if (res.succeeded()) {
//				LOGGER.info("Rows inserted: " + res.result());
//			} else {
//				LOGGER.error("Something failed badly (in insertCategory): " + res.cause().getMessage());
//			}
//		});	

		// DELETING ALREADY inserted Categories
		Future<Integer> deletedCategories = queryExecutor.execute(dsl -> dsl
			.deleteFrom(com.ns.vertx.pg.jooq.tables.Category.CATEGORY)
			.where(com.ns.vertx.pg.jooq.tables.Category.CATEGORY.NAME.eq("Adventure")));
		
		deletedCategories.setHandler(res -> {
			if (res.succeeded()) {
				LOGGER.info("Rows deleted: " + res.result());
			} else {
				LOGGER.error("Something failed badly (in deletedCategories): " + res.cause().getMessage());
			}
		});
		// ================ Testing classic-reactive-jOOQ implementation::END ================		

		Future<Void> futureConnection = connect().compose(connection -> {
			Promise<Void> retFuture = Promise.promise();
			createTableIfNeeded().future().setHandler(x -> {
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
		vertx.createHttpServer().requestHandler(router).listen(LISTEN_PORT, res -> promise.handle(res.mapEmpty()));
		return promise.future();
	}

	private Promise<SqlConnection> createTableIfNeeded(/* SqlConnection connection */) {
		Promise<SqlConnection> promise = Promise.promise();
		pgClient.getConnection(ar1 -> {
			if (ar1.succeeded()) {
				LOGGER.info("Connected!");
				SqlConnection conn = ar1.result();
				conn.prepare(CREATE_CATEGORY_TABLE_SQL, rs -> {
					if (rs.succeeded()) {
						
						promise.handle(rs.map(conn));
					} else {
						LOGGER.error("Error, executing 'create_category_table_sql' query failed!", rs.cause());
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
		while (ri.hasNext()) {
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
				sqlConnection.prepare(GET_ALL_CATEGORIES_SQL, fetch -> {
					if (fetch.succeeded()) {
						PreparedStatement ps = fetch.result();
						ps.query().execute(handler -> {
							RowSet<Row> rs = handler.result();
							JsonObject categories = convertRowSetToJsonObject(rs);
							rc.response().setStatusCode(303);
							rc.response().putHeader("Content-Type", "application/json; UTF-8");
							rc.response().end(categories.encodePrettily());
						});
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
				int id = Integer.valueOf(rc.request().getParam("id"));
				conn.preparedQuery(GET_CATEGORY_BY_ID_SQL).execute(Tuple.of(id), arCID -> {
					JsonObject responseJO = new JsonObject();
					if (arCID.succeeded()) {
						RowIterator<Row> ri = arCID.result().iterator();
						if (ri.hasNext()) {
							Row row = ri.next();
							responseJO
								.put("category_id", row.getLong(0))
								.put("name", row.getString(1))
								.put("is_deleted", row.getBoolean(2));

							LOGGER.info("Succeeded in quering category by id = " + id);
							rc.response().setStatusCode(200);
							rc.response().putHeader("Content-Type", "application/json; UTF-8");
							rc.response().end(responseJO.encodePrettily());
						} else {
							LOGGER.error("Error, row is NULL! Cause: ", arCID.cause());
							rc.response().setStatusCode(404);
							rc.response().putHeader("Content-Type", "application/json; UTF-8");
							responseJO.put("message", "No content found for category ID = " + id);
							rc.response().end(responseJO.encodePrettily());
						}

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
		// TODO:implement it
		String name = rc.request().getParam("name");
		Boolean isDeleted = Boolean.valueOf(rc.request().getParam("is_deleted"));
		createCategoryJooq(queryExecutor, name, isDeleted).setHandler(ok(rc));
	}

	private Future<Integer> createCategoryJooq(ReactiveClassicGenericQueryExecutor queryExecutor, String name,
			boolean isDeleted) {
		// TODO: fix this problem with returning 'T' in Future<T> (so object which holds
		// 'category_id' can be returned) ... AND also replace "queryExecutor.query(..)" with "queryExecutor.execute(..)"
		Future<Integer> retVal = queryExecutor.execute(dsl -> dsl
				.insertInto(Category.CATEGORY)
				.columns(Category.CATEGORY.NAME, Category.CATEGORY.IS_DELETED)
				.values(name, isDeleted)
		);		
		
//		Future<Integer> retVal = Future.future();
		// next invocation of query(..) method causes HTTP 500
//		Future<QueryResult> res = queryExecutor.query(dsl -> (ResultQuery<Record1<Long>>) dsl
//			.insertInto(Category.CATEGORY)
//			.columns(Category.CATEGORY.NAME, Category.CATEGORY.IS_DELETED)
//			.values(name, isDeleted)
//			.returningResult(Category.CATEGORY.CATEGORY_ID, Category.CATEGORY.NAME, Category.CATEGORY.IS_DELETED)
//			.fetchOne()
//		);
//		QueryResult qr = res.result();
//		System.out.println("ID = " + qr.get(0, Long.class) + ", catName = " + qr.get(1, String.class) + " is_deleted = " + qr.get(2, boolean.class));
		return retVal;
	}

	// TODO: Before testing ADJUST Connection (before it was connect() method) so it
	// can run properly
	private void updateCategoryHandler(RoutingContext rc) {
		long id = (long) Integer.valueOf(rc.request().getParam("id"));
		JsonObject catJO = rc.getBodyAsJson(); // NOTE: Use this approach when extracting value from RECEIVED JSON!
		ICategory iCat = new com.ns.vertx.pg.jooq.tables.pojos.Category().fromJson(catJO);
		com.ns.vertx.pg.jooq.tables.pojos.Category categoryPojo = new com.ns.vertx.pg.jooq.tables.pojos.Category(iCat);
		updateCategoryJooq(queryExecutor, categoryPojo, id).onComplete(ok(rc));
	}

	
	private Future<Integer> updateCategoryJooq(ReactiveClassicGenericQueryExecutor queryExecutor,
			com.ns.vertx.pg.jooq.tables.pojos.Category categoryPOJO, long id) {

		Future<Integer> retVal = queryExecutor.execute(dsl -> dsl
			.update(Category.CATEGORY)
			.set(Category.CATEGORY.NAME, categoryPOJO.getName())
			.set(Category.CATEGORY.IS_DELETED, categoryPOJO.getIsDeleted())
			.where(Category.CATEGORY.CATEGORY_ID.eq(Long.valueOf(id)))
		);
		return retVal;
	}

	
	private Future<Void> deleteCategory(SqlConnection connection, int id) {
		Promise<Void> promise = Promise.promise();
		connection.preparedQuery(DBQueries.DELETE_CATEGORY_BY_ID_SQL).execute(Tuple.of(id), fetch -> {
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
//		connect().compose(connection -> deleteCategory(connection, id)).setHandler(noContent(rc));
	}

}
