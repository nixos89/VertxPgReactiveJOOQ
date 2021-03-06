/*
 * This file is generated by jOOQ.
 */
package com.ns.vertx.pg.jooq.tables.records;


import com.ns.vertx.pg.jooq.tables.Orders;
import com.ns.vertx.pg.jooq.tables.interfaces.IOrders;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import java.time.LocalDateTime;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OrdersRecord extends UpdatableRecordImpl<OrdersRecord> implements VertxPojo, Record4<Long, Double, LocalDateTime, Long>, IOrders {

    private static final long serialVersionUID = 685754679;

    /**
     * Setter for <code>public.orders.order_id</code>.
     */
    @Override
    public OrdersRecord setOrderId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.orders.order_id</code>.
     */
    @Override
    public Long getOrderId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.orders.total</code>.
     */
    @Override
    public OrdersRecord setTotal(Double value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.orders.total</code>.
     */
    @Override
    public Double getTotal() {
        return (Double) get(1);
    }

    /**
     * Setter for <code>public.orders.order_date</code>.
     */
    @Override
    public OrdersRecord setOrderDate(LocalDateTime value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.orders.order_date</code>.
     */
    @Override
    public LocalDateTime getOrderDate() {
        return (LocalDateTime) get(2);
    }

    /**
     * Setter for <code>public.orders.user_id</code>.
     */
    @Override
    public OrdersRecord setUserId(Long value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.orders.user_id</code>.
     */
    @Override
    public Long getUserId() {
        return (Long) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<Long, Double, LocalDateTime, Long> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<Long, Double, LocalDateTime, Long> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return Orders.ORDERS.ORDER_ID;
    }

    @Override
    public Field<Double> field2() {
        return Orders.ORDERS.TOTAL;
    }

    @Override
    public Field<LocalDateTime> field3() {
        return Orders.ORDERS.ORDER_DATE;
    }

    @Override
    public Field<Long> field4() {
        return Orders.ORDERS.USER_ID;
    }

    @Override
    public Long component1() {
        return getOrderId();
    }

    @Override
    public Double component2() {
        return getTotal();
    }

    @Override
    public LocalDateTime component3() {
        return getOrderDate();
    }

    @Override
    public Long component4() {
        return getUserId();
    }

    @Override
    public Long value1() {
        return getOrderId();
    }

    @Override
    public Double value2() {
        return getTotal();
    }

    @Override
    public LocalDateTime value3() {
        return getOrderDate();
    }

    @Override
    public Long value4() {
        return getUserId();
    }

    @Override
    public OrdersRecord value1(Long value) {
        setOrderId(value);
        return this;
    }

    @Override
    public OrdersRecord value2(Double value) {
        setTotal(value);
        return this;
    }

    @Override
    public OrdersRecord value3(LocalDateTime value) {
        setOrderDate(value);
        return this;
    }

    @Override
    public OrdersRecord value4(Long value) {
        setUserId(value);
        return this;
    }

    @Override
    public OrdersRecord values(Long value1, Double value2, LocalDateTime value3, Long value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(IOrders from) {
        setOrderId(from.getOrderId());
        setTotal(from.getTotal());
        setOrderDate(from.getOrderDate());
        setUserId(from.getUserId());
    }

    @Override
    public <E extends IOrders> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached OrdersRecord
     */
    public OrdersRecord() {
        super(Orders.ORDERS);
    }

    /**
     * Create a detached, initialised OrdersRecord
     */
    public OrdersRecord(Long orderId, Double total, LocalDateTime orderDate, Long userId) {
        super(Orders.ORDERS);

        set(0, orderId);
        set(1, total);
        set(2, orderDate);
        set(3, userId);
    }

    public OrdersRecord(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }
}
