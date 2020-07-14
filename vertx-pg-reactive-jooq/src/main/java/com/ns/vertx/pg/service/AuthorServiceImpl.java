package com.ns.vertx.pg.service;

import static com.ns.vertx.pg.jooq.tables.Author.AUTHOR;

import java.util.List;
import java.util.NoSuchElementException;

import org.jooq.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;

public class AuthorServiceImpl implements AuthorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorServiceImpl.class);	
	private ReactiveClassicGenericQueryExecutor queryExecutor;
	
	public AuthorServiceImpl(PgPool pgClient, Configuration configuration, Handler<AsyncResult<AuthorService>> readyHandler) {
		LOGGER.info("+++++++++ going to instantiate (ReactiveClassicGenericQueryExecutor) queryExecutor in AuthorServiceImpl! +++++++++");
		pgClient.getConnection(ar -> {
			if (ar.failed()) {
				LOGGER.error("Could NOT OPEN DB connection!" , ar.cause());
				readyHandler.handle(Future.failedFuture(ar.cause()));
			} else {
				SqlConnection connection = ar.result();						
				this.queryExecutor = new ReactiveClassicGenericQueryExecutor(configuration, pgClient);
				LOGGER.info("+++++ Connection succeded and queryExecutor instantiation is SUCCESSFUL! +++++");
				connection.close();		
				readyHandler.handle(Future.succeededFuture(this));
			}
		});	
	}
	
	// ************************************************************************************************
	// ******************************* AuthorService CRUD methods ************************************* 
	// ************************************************************************************************		
	@Override
	public AuthorService getAllAuthorsJooqSP(Handler<AsyncResult<JsonObject>> resultHandler) {
		 Future<List<Row>> bookFuture = queryExecutor.transaction(qe -> {			
			return qe.findManyRow(dsl -> dsl.select(AUTHOR.AUTHOR_ID, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME)
				.from(AUTHOR).orderBy(AUTHOR.AUTHOR_ID.asc()));
		});						
	    bookFuture.onComplete(handler -> {
			if (handler.succeeded()) {								
				List<Row> authorsLR = handler.result();				
				JsonObject authorsJsonObject = extractAuthorsFromLR(authorsLR);
				resultHandler.handle(Future.succeededFuture(authorsJsonObject));
	    	} else {
	    		LOGGER.error("Error, something failed in retrivening ALL authors! Cause: " 
	    				+ handler.cause().getMessage());
	    		queryExecutor.rollback();
	    		resultHandler.handle(Future.failedFuture(handler.cause()));
	    	}
	    }); 		
		return this;
	}


	@Override
	public AuthorService getAuthorByIdJooqSP(Long authorId, Handler<AsyncResult<JsonObject>> resultHandler) {
		Future<Row> bookFuture = queryExecutor.transaction(qe -> {			
			return qe.findOneRow(dsl -> dsl
				.select(AUTHOR.AUTHOR_ID, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME)
				.from(AUTHOR).where(AUTHOR.AUTHOR_ID.eq(authorId)));
		});						
	    bookFuture.onComplete(handler -> {
			if (handler.succeeded()) {										
				if (handler.result() == null) {
					resultHandler.handle(Future.failedFuture(new NoSuchElementException("No author with id = " + authorId)));				
				} else {
					JsonObject authorJsonObject = fillAuthor(handler.result());
					resultHandler.handle(Future.succeededFuture(authorJsonObject));
				}				
	    	} else {
				LOGGER.error("Error, something failed in retrivening author by id " + authorId 
						+ " ! Cause: " + handler.cause().getMessage());
	    		queryExecutor.rollback();
	    		resultHandler.handle(Future.failedFuture(handler.cause()));
	    	}
	    }); 
		return this;
	}


	@Override
	public AuthorService createAuthorJooqSP(String firstName, String lastName,
			Handler<AsyncResult<Void>> resultHandler) {
		Future<Integer> retVal = queryExecutor.transaction(transactionQE -> { 
			return transactionQE.execute(dsl -> dsl 
				.insertInto(AUTHOR, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME)
				.values(firstName, lastName)
				.returningResult(AUTHOR.AUTHOR_ID));			
		});	
		LOGGER.info("About to go into retVal.onSuccess(..) method...");
		retVal.onSuccess(ar -> {
			LOGGER.info("Author with ID  = " + ar + "is inserted...");
			resultHandler.handle(Future.succeededFuture());
		});
		retVal.onFailure(throwable -> {
			queryExecutor.rollback();
			resultHandler.handle(Future.failedFuture(throwable));
		});	
		return this;
	}


	@Override
	public AuthorService updateAuthorJooqSP(JsonObject authorJO, Handler<AsyncResult<Void>> resultHandler) {
		// TODO Auto-generated method stub
		Future<Integer> retVal = queryExecutor.transaction(transactionQE -> {				
			return transactionQE.execute(dsl -> dsl
				.update(AUTHOR)
				.set(AUTHOR.FIRST_NAME, authorJO.getString("first_name"))
				.set(AUTHOR.LAST_NAME, authorJO.getString("last_name"))
				.where(AUTHOR.AUTHOR_ID.eq(authorJO.getLong("author_id")))				
			);
		});		
		retVal.onSuccess(ar -> resultHandler.handle(Future.succeededFuture()));
		retVal.onFailure(handler -> {
			LOGGER.error("Error, something went wrong! Cause:\n" + handler.getStackTrace());
			resultHandler.handle(Future.failedFuture(new NoSuchElementException("Error, author has not been updated for id = "
					+ authorJO.getLong("author_id") + "! Cause: " + handler)));
		});				
		return this;
	}


	@Override
	public AuthorService deleteAuthorJooqSP(Long id, Handler<AsyncResult<Void>> resultHandler) {
		Future<Integer> deleteAuthorFuture = queryExecutor.transaction(transactionQE -> {
			return transactionQE.execute(dsl -> dsl
				.delete(AUTHOR)
				.where(AUTHOR.AUTHOR_ID.eq(Long.valueOf(id))));
		});		
		deleteAuthorFuture.onComplete(ar -> {
			if(ar.succeeded()) {
				resultHandler.handle(Future.succeededFuture());
			} else {
				queryExecutor.rollback();
				resultHandler.handle(Future.failedFuture(new NoSuchElementException("No author with id = " + id)));
			}
		});		
		return this;
	}
	
	
	// **************************************************************************************************
	//  **************************************** Helper methods *****************************************
	// **************************************************************************************************	
	private static JsonObject fillAuthor(Row row) {
		return new JsonObject()
			.put("authorId", row.getLong("author_id"))
			.put("firstName", row.getString("first_name"))
			.put("lastName", row.getString("last_name"));							
	}	
	
	private static JsonObject extractAuthorsFromLR(List<Row> authorLR){		
		JsonObject authorJO = new JsonObject(); 
		JsonArray authorsJA = new JsonArray();
		for(Row row: authorLR) {
			authorJO = fillAuthor(row);
			authorsJA.add(authorJO);
			authorJO = new JsonObject();
		}
		JsonObject joAuthors= new JsonObject().put("authors", authorsJA);		
		return joAuthors;
	}
	
	
}
