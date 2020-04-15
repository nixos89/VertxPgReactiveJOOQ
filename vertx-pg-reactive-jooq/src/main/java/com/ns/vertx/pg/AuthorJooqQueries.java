package com.ns.vertx.pg;

import com.ns.vertx.pg.jooq.tables.daos.AuthorDao;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/* This class holds STATIC methods which are used as helper methods for routing in order to reduce 
 * boiler-plate code from MainVerticle class */
public class AuthorJooqQueries {	
		
	// NOT declared with PUBLIC modifier so it can NOT be visible OUTSIDE of this package
	// TODO: put configured objects
	static Future<Integer> findAuthorById(ReactiveClassicGenericQueryExecutor queryExecutor, 
			AuthorDao authorDAO, Long id) {
		Promise<Integer> promise = Promise.promise();
		// TODO: do querying with vertx-ifed jOOQ
		
		
		return promise.future();
	}
	
	
	
	
	
}
