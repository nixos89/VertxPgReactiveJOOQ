/*
 * This file is generated by jOOQ.
 */
package com.ns.vertx.pg.jooq.tables;


import com.ns.vertx.pg.jooq.Keys;
import com.ns.vertx.pg.jooq.Public;
import com.ns.vertx.pg.jooq.tables.records.OrdersRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row4;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Orders extends TableImpl<OrdersRecord> {

    private static final long serialVersionUID = -1630278714;

    /**
     * The reference instance of <code>public.orders</code>
     */
    public static final Orders ORDERS = new Orders();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<OrdersRecord> getRecordType() {
        return OrdersRecord.class;
    }

    /**
     * The column <code>public.orders.order_id</code>.
     */
    public final TableField<OrdersRecord, Long> ORDER_ID = createField(DSL.name("order_id"), org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('orders_order_id_seq'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.orders.total</code>.
     */
    public final TableField<OrdersRecord, Double> TOTAL = createField(DSL.name("total"), org.jooq.impl.SQLDataType.DOUBLE, this, "");

    /**
     * The column <code>public.orders.order_date</code>.
     */
    public final TableField<OrdersRecord, LocalDateTime> ORDER_DATE = createField(DSL.name("order_date"), org.jooq.impl.SQLDataType.LOCALDATETIME, this, "");

    /**
     * The column <code>public.orders.user_id</code>.
     */
    public final TableField<OrdersRecord, Integer> USER_ID = createField(DSL.name("user_id"), org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * Create a <code>public.orders</code> table reference
     */
    public Orders() {
        this(DSL.name("orders"), null);
    }

    /**
     * Create an aliased <code>public.orders</code> table reference
     */
    public Orders(String alias) {
        this(DSL.name(alias), ORDERS);
    }

    /**
     * Create an aliased <code>public.orders</code> table reference
     */
    public Orders(Name alias) {
        this(alias, ORDERS);
    }

    private Orders(Name alias, Table<OrdersRecord> aliased) {
        this(alias, aliased, null);
    }

    private Orders(Name alias, Table<OrdersRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> Orders(Table<O> child, ForeignKey<O, OrdersRecord> key) {
        super(child, key, ORDERS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public Identity<OrdersRecord, Long> getIdentity() {
        return Keys.IDENTITY_ORDERS;
    }

    @Override
    public UniqueKey<OrdersRecord> getPrimaryKey() {
        return Keys.ORDERS_PKEY;
    }

    @Override
    public List<UniqueKey<OrdersRecord>> getKeys() {
        return Arrays.<UniqueKey<OrdersRecord>>asList(Keys.ORDERS_PKEY);
    }

    @Override
    public List<ForeignKey<OrdersRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<OrdersRecord, ?>>asList(Keys.ORDERS__ORDERS_USER_ID_FKEY);
    }

    public Users users() {
        return new Users(this, Keys.ORDERS__ORDERS_USER_ID_FKEY);
    }

    @Override
    public Orders as(String alias) {
        return new Orders(DSL.name(alias), this);
    }

    @Override
    public Orders as(Name alias) {
        return new Orders(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Orders rename(String name) {
        return new Orders(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Orders rename(Name name) {
        return new Orders(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Long, Double, LocalDateTime, Integer> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
