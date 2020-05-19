/*
 * This file is generated by jOOQ.
 */
package com.ns.vertx.pg.jooq.tables;


import com.ns.vertx.pg.jooq.Keys;
import com.ns.vertx.pg.jooq.Public;
import com.ns.vertx.pg.jooq.tables.records.OrderItemRecord;

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
public class OrderItem extends TableImpl<OrderItemRecord> {

    private static final long serialVersionUID = -2043600556;

    /**
     * The reference instance of <code>public.order_item</code>
     */
    public static final OrderItem ORDER_ITEM = new OrderItem();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<OrderItemRecord> getRecordType() {
        return OrderItemRecord.class;
    }

    /**
     * The column <code>public.order_item.order_item_id</code>.
     */
    public final TableField<OrderItemRecord, Long> ORDER_ITEM_ID = createField(DSL.name("order_item_id"), org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('order_item_order_item_id_seq'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.order_item.amount</code>.
     */
    public final TableField<OrderItemRecord, Integer> AMOUNT = createField(DSL.name("amount"), org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.order_item.book_id</code>.
     */
    public final TableField<OrderItemRecord, Long> BOOK_ID = createField(DSL.name("book_id"), org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * The column <code>public.order_item.order_id</code>.
     */
    public final TableField<OrderItemRecord, Long> ORDER_ID = createField(DSL.name("order_id"), org.jooq.impl.SQLDataType.BIGINT, this, "");

    /**
     * Create a <code>public.order_item</code> table reference
     */
    public OrderItem() {
        this(DSL.name("order_item"), null);
    }

    /**
     * Create an aliased <code>public.order_item</code> table reference
     */
    public OrderItem(String alias) {
        this(DSL.name(alias), ORDER_ITEM);
    }

    /**
     * Create an aliased <code>public.order_item</code> table reference
     */
    public OrderItem(Name alias) {
        this(alias, ORDER_ITEM);
    }

    private OrderItem(Name alias, Table<OrderItemRecord> aliased) {
        this(alias, aliased, null);
    }

    private OrderItem(Name alias, Table<OrderItemRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> OrderItem(Table<O> child, ForeignKey<O, OrderItemRecord> key) {
        super(child, key, ORDER_ITEM);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public Identity<OrderItemRecord, Long> getIdentity() {
        return Keys.IDENTITY_ORDER_ITEM;
    }

    @Override
    public UniqueKey<OrderItemRecord> getPrimaryKey() {
        return Keys.ORDER_ITEM_PKEY;
    }

    @Override
    public List<UniqueKey<OrderItemRecord>> getKeys() {
        return Arrays.<UniqueKey<OrderItemRecord>>asList(Keys.ORDER_ITEM_PKEY);
    }

    @Override
    public List<ForeignKey<OrderItemRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<OrderItemRecord, ?>>asList(Keys.ORDER_ITEM__ORDER_ITEM_BOOK_ID_FKEY, Keys.ORDER_ITEM__ORDER_ITEM_ORDER_ID_FKEY);
    }

    public Book book() {
        return new Book(this, Keys.ORDER_ITEM__ORDER_ITEM_BOOK_ID_FKEY);
    }

    public Orders orders() {
        return new Orders(this, Keys.ORDER_ITEM__ORDER_ITEM_ORDER_ID_FKEY);
    }

    @Override
    public OrderItem as(String alias) {
        return new OrderItem(DSL.name(alias), this);
    }

    @Override
    public OrderItem as(Name alias) {
        return new OrderItem(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public OrderItem rename(String name) {
        return new OrderItem(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public OrderItem rename(Name name) {
        return new OrderItem(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Long, Integer, Long, Long> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
