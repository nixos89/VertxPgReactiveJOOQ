/*
 * This file is generated by jOOQ.
 */
package com.ns.vertx.pg.jooq.tables.interfaces;


import io.github.jklingsporn.vertx.jooq.shared.UnexpectedJsonValueType;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import java.io.Serializable;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface ICategoryBook extends VertxPojo, Serializable {

    /**
     * Setter for <code>public.category_book.category_id</code>.
     */
    public ICategoryBook setCategoryId(Long value);

    /**
     * Getter for <code>public.category_book.category_id</code>.
     */
    public Long getCategoryId();

    /**
     * Setter for <code>public.category_book.book_id</code>.
     */
    public ICategoryBook setBookId(Long value);

    /**
     * Getter for <code>public.category_book.book_id</code>.
     */
    public Long getBookId();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface ICategoryBook
     */
    public void from(ICategoryBook from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface ICategoryBook
     */
    public <E extends ICategoryBook> E into(E into);

    @Override
    public default ICategoryBook fromJson(io.vertx.core.json.JsonObject json) {
        try {
            setCategoryId(json.getLong("category_id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("category_id","java.lang.Long",e);
        }
        try {
            setBookId(json.getLong("book_id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("book_id","java.lang.Long",e);
        }
        return this;
    }


    @Override
    public default io.vertx.core.json.JsonObject toJson() {
        io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
        json.put("category_id",getCategoryId());
        json.put("book_id",getBookId());
        return json;
    }

}
