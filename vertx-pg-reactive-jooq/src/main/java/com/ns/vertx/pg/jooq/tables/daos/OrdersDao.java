/*
 * This file is generated by jOOQ.
 */
package com.ns.vertx.pg.jooq.tables.daos;


import com.ns.vertx.pg.jooq.tables.Orders;
import com.ns.vertx.pg.jooq.tables.records.OrdersRecord;

import io.github.jklingsporn.vertx.jooq.shared.reactive.AbstractReactiveVertxDAO;

import java.time.LocalDateTime;
import java.util.Collection;

import org.jooq.Configuration;


import java.util.List;
import io.vertx.core.Future;
import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicQueryExecutor;
/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OrdersDao extends AbstractReactiveVertxDAO<OrdersRecord, com.ns.vertx.pg.jooq.tables.pojos.Orders, Long, Future<List<com.ns.vertx.pg.jooq.tables.pojos.Orders>>, Future<com.ns.vertx.pg.jooq.tables.pojos.Orders>, Future<Integer>, Future<Long>> implements io.github.jklingsporn.vertx.jooq.classic.VertxDAO<OrdersRecord,com.ns.vertx.pg.jooq.tables.pojos.Orders,Long> {

    /**
     * @param configuration Used for rendering, so only SQLDialect must be set and must be one of the POSTGREs types.
     * @param delegate A configured AsyncSQLClient that is used for query execution
     */
    public OrdersDao(Configuration configuration, io.vertx.sqlclient.SqlClient delegate) {
        super(Orders.ORDERS, com.ns.vertx.pg.jooq.tables.pojos.Orders.class, new ReactiveClassicQueryExecutor<OrdersRecord,com.ns.vertx.pg.jooq.tables.pojos.Orders,Long>(configuration,delegate,com.ns.vertx.pg.jooq.tables.mappers.RowMappers.getOrdersMapper()));
    }

    @Override
    protected Long getId(com.ns.vertx.pg.jooq.tables.pojos.Orders object) {
        return object.getOrderId();
    }

    /**
     * Find records that have <code>total IN (values)</code> asynchronously
     */
    public Future<List<com.ns.vertx.pg.jooq.tables.pojos.Orders>> findManyByTotal(Collection<Double> values) {
        return findManyByCondition(Orders.ORDERS.TOTAL.in(values));
    }

    /**
     * Find records that have <code>order_date IN (values)</code> asynchronously
     */
    public Future<List<com.ns.vertx.pg.jooq.tables.pojos.Orders>> findManyByOrderDate(Collection<LocalDateTime> values) {
        return findManyByCondition(Orders.ORDERS.ORDER_DATE.in(values));
    }

    /**
     * Find records that have <code>user_id IN (values)</code> asynchronously
     */
    public Future<List<com.ns.vertx.pg.jooq.tables.pojos.Orders>> findManyByUserId(Collection<Long> values) {
        return findManyByCondition(Orders.ORDERS.USER_ID.in(values));
    }

    @Override
    public ReactiveClassicQueryExecutor<OrdersRecord,com.ns.vertx.pg.jooq.tables.pojos.Orders,Long> queryExecutor(){
        return (ReactiveClassicQueryExecutor<OrdersRecord,com.ns.vertx.pg.jooq.tables.pojos.Orders,Long>) super.queryExecutor();
    }
}
