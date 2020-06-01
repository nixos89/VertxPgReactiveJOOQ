package com.ns.vertx.pg.service;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ns.vertx.pg.jooq.tables.Category.CATEGORY;
import com.ns.vertx.pg.jooq.tables.pojos.Category;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;


public class CategoryServiceImpl {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CategoryServiceImpl.class);
		
	private static JsonObject fillCategory(Row row) {
		if (row.getLong(0) == null) {
			return null;
		} else {
			return new JsonObject()
				.put("category_id", row.getLong(0))
				.put("name", row.getString(1))
				.put("is_deleted", row.getBoolean(2));
		}		
	}
	
	private static JsonObject convertListOfRowsToJO(List<Row> rowList) {
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
	
	public static Future<JsonObject> getAllCategoriesJooq(ReactiveClassicGenericQueryExecutor queryExecutor) {
		Promise<JsonObject> finalRes = Promise.promise();
		Future<List<Row>> queryRes = queryExecutor.transaction(qe -> {						
			return qe.findManyRow(dsl -> dsl
					.selectFrom(CATEGORY)
					.orderBy(CATEGORY.CATEGORY_ID.asc())
			);
		});		
		queryRes.onComplete(ar-> {
			if (ar.succeeded()) {
				List<Row> rowList = ar.result();				
				JsonObject categoriesFinal = convertListOfRowsToJO(rowList);
				LOGGER.info("All Categories:\n" + categoriesFinal.encodePrettily());
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
	    Future<Row> findOneCatFuture = queryExecutor.transaction(qe -> {
	    	return queryExecutor.findOneRow(dsl -> dsl
		    		.selectFrom(CATEGORY)
		    		.where(CATEGORY.CATEGORY_ID.eq(Long.valueOf(id))));	    	
	    }); 	    
		findOneCatFuture.onComplete(handler -> {
			if(handler.succeeded()) {
				if(handler.result() == null) {
					finalRes.fail(new NoSuchElementException("Error, no category found in DB for category_id = " + id));
				} else {
					JsonObject categoryJO = fillCategory(handler.result());
					finalRes.complete(categoryJO);
				}				
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
					.insertInto(CATEGORY, CATEGORY.NAME, CATEGORY.IS_DELETED)
					.values(name, isDeleted));
		});		
		retVal.onSuccess(ar -> promise.complete());
		retVal.onFailure(handler -> {
			queryExecutor.rollback();
			promise.fail(handler);
		});
		return promise.future();
	}		

	public static Future<Void> updateCategoryJooq(ReactiveClassicGenericQueryExecutor queryExecutor, Category categoryPOJO) {
		Promise<Void> promise = Promise.promise();									
		Future<Integer> retVal = queryExecutor.transaction(qe ->{						
			return qe.execute(dsl -> dsl.update(CATEGORY)
				.set(CATEGORY.NAME, categoryPOJO.getName())
				.set(CATEGORY.IS_DELETED, categoryPOJO.getIsDeleted())
				.where(CATEGORY.CATEGORY_ID.eq(categoryPOJO.getCategoryId()))
			);
		});		
		retVal.onSuccess(handler -> promise.complete());		
		retVal.onFailure(handler -> {
			queryExecutor.rollback();
			promise.handle(Future.failedFuture(
				new NoSuchElementException("Error, category has not been updated for id = " + categoryPOJO.getCategoryId() 
					+ ". Cause: " + retVal.cause().getStackTrace())));
		});		
		return promise.future();
	}
		
	public static Future<Void> deleteCategoryJooq(ReactiveClassicGenericQueryExecutor queryExecutor, long id) {
		Promise<Void> promise = Promise.promise();
		
		Future<Void> findAndDeleteFuture = queryExecutor.beginTransaction().compose(transactionQE -> 
			transactionQE.findOneRow(dsl -> dsl
				.selectFrom(CATEGORY).where(CATEGORY.CATEGORY_ID.eq(Long.valueOf(id))))
			.compose(searchedCat -> {					
				if(searchedCat == null) {					
					LOGGER.info("No category_id = " + id + " found in DB!");
					promise.fail(new NoSuchElementException("Error, no category_id = " + id + " found in DB!"));
					return transactionQE.rollback(); 
				} else {
					return transactionQE.execute(dsl -> dsl
						.delete(CATEGORY)
						.where(CATEGORY.CATEGORY_ID.eq(Long.valueOf(id))))
					.compose(success -> {
						LOGGER.info("Commiting transaction ...");
						return transactionQE.commit();	
					}, failure -> {
						LOGGER.info("Nooooo, rolling-back transcation...");
						return transactionQE.rollback();
					});
				}					
			})	    	
	    ); 				
		findAndDeleteFuture.onSuccess(ar -> promise.complete());
		findAndDeleteFuture.onFailure(ar -> {
			LOGGER.error("Error, something went WRONG in searching and deleting category by ID!");
			promise.fail(ar);
		});	
		return promise.future();
	}
			
}
