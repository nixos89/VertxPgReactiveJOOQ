/*
 * This file is generated by jOOQ.
 */
package com.ns.vertx.pg.jooq.tables.interfaces;


import io.github.jklingsporn.vertx.jooq.shared.UnexpectedJsonValueType;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IOrders extends VertxPojo, Serializable {

    /**
     * Setter for <code>public.orders.order_id</code>.
     */
    public IOrders setOrderId(Long value);

    /**
     * Getter for <code>public.orders.order_id</code>.
     */
    public Long getOrderId();

    /**
     * Setter for <code>public.orders.total</code>.
     */
    public IOrders setTotal(Double value);

    /**
     * Getter for <code>public.orders.total</code>.
     */
    public Double getTotal();

    /**
     * Setter for <code>public.orders.order_date</code>.
     */
    public IOrders setOrderDate(LocalDateTime value);

    /**
     * Getter for <code>public.orders.order_date</code>.
     */
    public LocalDateTime getOrderDate();

    /**
     * Setter for <code>public.orders.user_id</code>.
     */
    public IOrders setUserId(Long value);

    /**
     * Getter for <code>public.orders.user_id</code>.
     */
    public Long getUserId();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IOrders
     */
    public void from(IOrders from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IOrders
     */
    public <E extends IOrders> E into(E into);

    @Override
    public default IOrders fromJson(io.vertx.core.json.JsonObject json) {
        try {
            setOrderId(json.getLong("order_id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("order_id","java.lang.Long",e);
        }
        try {
            setTotal(json.getDouble("total"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("total","java.lang.Double",e);
        }
        try {
            // Omitting unrecognized type java.time.LocalDateTime for column order_date!
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("order_date","java.time.LocalDateTime",e);
        }
        try {
            setUserId(json.getLong("user_id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("user_id","java.lang.Long",e);
        }
        return this;
    }


    @Override
    public default io.vertx.core.json.JsonObject toJson() {
        io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
        json.put("order_id",getOrderId());
        json.put("total",getTotal());
        // Omitting unrecognized type java.time.LocalDateTime for column order_date!
        json.put("user_id",getUserId());
        return json;
    }

}
