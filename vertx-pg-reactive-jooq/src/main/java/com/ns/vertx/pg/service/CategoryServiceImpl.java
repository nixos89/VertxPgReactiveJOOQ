package com.ns.vertx.pg.service;

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

// NOTE: refactor it later to implement CategoryService interface
public class CategoryServiceImpl {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryServiceImpl.class);
	
	
	private static JsonObject fillCategory(Row row) {
		return new JsonObject()
				.put("category_id", row.getLong(0))
				.put("name", row.getString(1))
				.put("is_deleted", row.getBoolean(2));
	}
	
	
	public static JsonObject convertListOfRowsToJO(List<Row> rowList) {
		JsonArray categoriesArr = new JsonArray();
		Iterator<Row> ir = rowList.iterator();		
		while(ir.hasNext()) {			
			Row row = ir.next();
			JsonObject category = fillCategory(row);
			categoriesArr.add(category);
		}
		JsonObject categoriesFinal = new JsonObject();
		categoriesFinal.put("categories", categoriesArr);
		return categoriesFinal;
	}
	
	// FIXME: change all methods BELOW to have return 'CategoryService ' type of (interface 2 b created LATER)
	public static Future<JsonObject> getAllCategoriesJooq(ReactiveClassicGenericQueryExecutor queryExecutor) {
		Promise<JsonObject> finalRes = Promise.promise();
		Future<List<Row>> queryRes = queryExecutor.transaction(qe -> {						
			return qe.findManyRow(dsl -> dsl
					.selectFrom(Category.CATEGORY)
					.orderBy(Category.CATEGORY.CATEGORY_ID)
			);
		});
		
		queryRes.onComplete(ar-> {
			if (ar.succeeded()) {
				List<Row> rowList = ar.result();				
				JsonObject categoriesFinal = convertListOfRowsToJO(rowList);				
				finalRes.complete(categoriesFinal);
			} else {
				queryExecutor.rollback();
				finalRes.fail(ar.cause());
			}
		});		
		return finalRes.future();
	}
	
	
	public static Future<JsonObject> getCategoryByIdJooq(ReactiveClassicGenericQueryExecutor queryExecutor, long id) {
		Promise<JsonObject> finalRes = Promise.promise();
	    Future<Row> qr = queryExecutor.transaction(qe -> {
	    	return queryExecutor.findOneRow(dsl -> dsl
		    		.selectFrom(Category.CATEGORY)
		    		.where(Category.CATEGORY.CATEGORY_ID.eq(Long.valueOf(id))));	    	
	    }); 
	    
	    qr.onComplete(handler -> {
			if (handler.succeeded()) {
				Row row = handler.result();
				JsonObject category = fillCategory(row);
				finalRes.complete(category);
	    	} else {
	    		queryExecutor.rollback();
	    		finalRes.fail(handler.cause());
	    	}
	    }); 
		
		return finalRes.future();
	}	
	
	 
	public static Future<Void> createCategoryJooq(ReactiveClassicGenericQueryExecutor queryExecutor,
			String name, boolean isDeleted) {		
		Promise<Void> promise = Promise.promise();			
		Future<Integer> retVal = queryExecutor.transaction(qe -> {
			return qe.execute(dsl -> dsl
					.insertInto(Category.CATEGORY, Category.CATEGORY.NAME, Category.CATEGORY.IS_DELETED)
					.values(name, isDeleted)
					.returningResult(Category.CATEGORY.CATEGORY_ID, Category.CATEGORY.NAME, 
							Category.CATEGORY.IS_DELETED));

		});
		
		retVal.onComplete(ar -> {
			if (ar.succeeded()) {
				promise.complete();
			} else {
				queryExecutor.rollback();
				promise.fail(ar.cause());
			}
		});
		return promise.future();
	}
	
	

	public static Future<Void> updateCategoryJooq(ReactiveClassicGenericQueryExecutor queryExecutor,
			com.ns.vertx.pg.jooq.tables.pojos.Category categoryPOJO, long id) {
		Promise<Void> promise = Promise.promise();				
				
		LOGGER.info("id = " + id);
		LOGGER.info("categoryPOJO.getName() = " + categoryPOJO.getName());
		LOGGER.info("categoryPOJO.getIsDeleted() = " + categoryPOJO.getIsDeleted());
		
		// FIXME: it returns null - don't know why :(
		Future<Integer> retVal = queryExecutor.transaction(qe ->{						
			System.out.println("********** updating categoryPojo ************\n"+categoryPOJO.toString()
			+"\n******************************************");
			
			return qe.execute(dsl -> dsl
				.update(Category.CATEGORY)
				.set(Category.CATEGORY.NAME, categoryPOJO.getName())
				.set(Category.CATEGORY.IS_DELETED, categoryPOJO.getIsDeleted())
				.where(Category.CATEGORY.CATEGORY_ID.eq(categoryPOJO.getCategoryId()))
				.returning()
				
			);
		});
		
		retVal.onComplete(ar -> {
			if (ar.succeeded()) {
				LOGGER.info("retVal SUCCEEDED! reVal = " + retVal.result());
				promise.handle(Future.succeededFuture());
			} else {				
				LOGGER.error("Error, something is wrong! retVal.result() = " + retVal.result() +
						", retVal.cause(): " + retVal.cause());
				queryExecutor.rollback();
				promise.handle(Future.failedFuture(
					new NoSuchElementException("Error, category has not been updated for id = " + id 
							+ ". Cause: " + retVal.cause().getStackTrace())));
			}
		});
		
		return promise.future();
	}
	
	
	public static Future<Void> deleteCategoryJooq(ReactiveClassicGenericQueryExecutor queryExecutor, long id) {
		Promise<Void> promise = Promise.promise();
		Future<Integer> deleteCategoryFuture = queryExecutor.transaction(qe -> {
			return qe.execute(dsl -> dsl
				.delete(Category.CATEGORY)
				.where(Category.CATEGORY.CATEGORY_ID.eq(Long.valueOf(id))));
		});
		
		deleteCategoryFuture.onComplete(ar -> {
			if(ar.succeeded()) {
				promise.handle(Future.succeededFuture());
			} else {
				queryExecutor.rollback();
				promise.handle(Future.failedFuture(new NoSuchElementException("No category with id = " + id)));
			}
		});
		
		return promise.future();
	}
		
	

}
