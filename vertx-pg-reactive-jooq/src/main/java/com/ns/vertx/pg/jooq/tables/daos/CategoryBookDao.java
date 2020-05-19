/*
 * This file is generated by jOOQ.
 */
package com.ns.vertx.pg.jooq.tables.daos;


import com.ns.vertx.pg.jooq.tables.CategoryBook;
import com.ns.vertx.pg.jooq.tables.records.CategoryBookRecord;

import io.github.jklingsporn.vertx.jooq.shared.reactive.AbstractReactiveVertxDAO;

import java.util.Collection;

import org.jooq.Configuration;
import org.jooq.Record2;


import java.util.List;
import io.vertx.core.Future;
import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicQueryExecutor;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CategoryBookDao extends AbstractReactiveVertxDAO<CategoryBookRecord, com.ns.vertx.pg.jooq.tables.pojos.CategoryBook, Record2<Long, Long>, Future<List<com.ns.vertx.pg.jooq.tables.pojos.CategoryBook>>, Future<com.ns.vertx.pg.jooq.tables.pojos.CategoryBook>, Future<Integer>, Future<Record2<Long, Long>>> implements io.github.jklingsporn.vertx.jooq.classic.VertxDAO<CategoryBookRecord,com.ns.vertx.pg.jooq.tables.pojos.CategoryBook,Record2<Long, Long>> {

    /**
     * @param configuration Used for rendering, so only SQLDialect must be set and must be one of the POSTGREs types.
     * @param delegate A configured AsyncSQLClient that is used for query execution
     */
    public CategoryBookDao(Configuration configuration, io.vertx.sqlclient.SqlClient delegate) {
        super(CategoryBook.CATEGORY_BOOK, com.ns.vertx.pg.jooq.tables.pojos.CategoryBook.class, new ReactiveClassicQueryExecutor<CategoryBookRecord,com.ns.vertx.pg.jooq.tables.pojos.CategoryBook,Record2<Long, Long>>(configuration,delegate,com.ns.vertx.pg.jooq.tables.mappers.RowMappers.getCategoryBookMapper()));
    }

    @Override
    protected Record2<Long, Long> getId(com.ns.vertx.pg.jooq.tables.pojos.CategoryBook object) {
        return compositeKeyRecord(object.getCategoryId(), object.getBookId());
    }

    /**
     * Find records that have <code>book_id IN (values)</code> asynchronously
     */
    public Future<List<com.ns.vertx.pg.jooq.tables.pojos.CategoryBook>> findManyByBookId(Collection<Long> values) {
        return findManyByCondition(CategoryBook.CATEGORY_BOOK.BOOK_ID.in(values));
    }

    @Override
    public ReactiveClassicQueryExecutor<CategoryBookRecord,com.ns.vertx.pg.jooq.tables.pojos.CategoryBook,Record2<Long, Long>> queryExecutor(){
        return (ReactiveClassicQueryExecutor<CategoryBookRecord,com.ns.vertx.pg.jooq.tables.pojos.CategoryBook,Record2<Long, Long>>) super.queryExecutor();
    }
}
