package com.ns.vertx.pg;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ns.vertx.pg.jooq.tables.Category;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

public class CategoryJooqQueries {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryJooqQueries.class);
	
	private static JsonObject fillCategory(Row row) {
		return new JsonObject()
				.put("category_id", row.getLong(0))
				.put("name", row.getString(1))
				.put("is_deleted", row.getBoolean(2));
	}
	
	static Future<JsonObject> getCategoryByIdJooq(ReactiveClassicGenericQueryExecutor queryExecutor, long id) {
		Promise<JsonObject> finalRes = Promise.promise();
	    Future<Row> qr = queryExecutor.findOneRow(dsl -> dsl
	    		.selectFrom(Category.CATEGORY)
	    		.where(Category.CATEGORY.CATEGORY_ID.eq(Long.valueOf(id)))	    		
	    );
	    qr.setHandler(handler -> {
			if (handler.succeeded()) {
				Row row = handler.result();
				JsonObject category = fillCategory(row);
				finalRes.complete(category);
	    	} else {
	    		finalRes.fail(handler.cause());
	    	}
	    }); 
		
		return finalRes.future();
	}
	
	
	static JsonObject convertListOfRowsToJO(List<Row> rowList) {
		JsonArray categoriesArr = new JsonArray();
		Iterator<Row> ir = rowList.iterator();		
		while(ir.hasNext()) {			
			Row row = ir.next();
			JsonObject category = fillCategory(row);
			LOGGER.info("category:\n" + category.encodePrettily());
			categoriesArr.add(category);
		}
		JsonObject categoriesFinal = new JsonObject();
		categoriesFinal.put("categories", categoriesArr);
		return categoriesFinal;
	}
	
	
	static Future<JsonObject> getAllCategoriesJooq(ReactiveClassicGenericQueryExecutor queryExecutor) {
		Promise<JsonObject> finalRes = Promise.promise();
		Future<List<Row>> queryRes = queryExecutor.findManyRow(dsl -> dsl
				.selectFrom(Category.CATEGORY)
				.orderBy(Category.CATEGORY.CATEGORY_ID)
		);
		queryRes.setHandler(ar-> {
			if (ar.succeeded()) {
				List<Row> rowList = ar.result();				
				JsonObject categoriesFinal = convertListOfRowsToJO(rowList);				
				finalRes.complete(categoriesFinal);
			} else {
				finalRes.fail(ar.cause());
			}
		});		
		return finalRes.future();
	}
	
	
	static Future<JsonObject> createCategoryJooq(ReactiveClassicGenericQueryExecutor queryExecutor,
			String name, boolean isDeleted) {
		
		Promise<JsonObject> promise = Promise.promise();		
		Future<Integer> retVal = queryExecutor.execute(dsl -> dsl
				.insertInto(Category.CATEGORY, Category.CATEGORY.NAME, Category.CATEGORY.IS_DELETED)
				.values(name, isDeleted)
				.returningResult(Category.CATEGORY.CATEGORY_ID, Category.CATEGORY.NAME, Category.CATEGORY.IS_DELETED)
				);		
		retVal.setHandler(ar -> {
			if (ar.succeeded()) {
				JsonObject result = new JsonObject().put("name", name).put("is_deleted", isDeleted);
				promise.complete(result);
			} else {
				promise.fail(ar.cause());
			}
		});
		return promise.future();
	}
	

	static Future<Integer> updateCategoryJooq(ReactiveClassicGenericQueryExecutor queryExecutor,
			com.ns.vertx.pg.jooq.tables.pojos.Category categoryPOJO, long id) {

		Future<Integer> retVal = queryExecutor.execute(dsl -> dsl
			.update(Category.CATEGORY)
			.set(Category.CATEGORY.NAME, categoryPOJO.getName())
			.set(Category.CATEGORY.IS_DELETED, categoryPOJO.getIsDeleted())
			.where(Category.CATEGORY.CATEGORY_ID.eq(Long.valueOf(id)))
		);
		return retVal;
	}
	

	static Future<Void> deleteCategoryJooq(ReactiveClassicGenericQueryExecutor queryExecutor, long id) {
		Promise<Void> promise = Promise.promise();
		Future<Integer> retVal =  queryExecutor.execute(dsl -> dsl
			.delete(Category.CATEGORY)
			.where(Category.CATEGORY.CATEGORY_ID.eq(Long.valueOf(id)))
		);
		retVal.setHandler(ar -> {
			if(ar.succeeded()) {
				promise.handle(Future.succeededFuture());
			} else {
				promise.handle(Future.failedFuture(new NoSuchElementException("No category with id = " + id)));
			}
		});
		
		return promise.future();
	}

}
