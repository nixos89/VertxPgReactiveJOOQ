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
public interface IAuthorBook extends VertxPojo, Serializable {

    /**
     * Setter for <code>public.author_book.author_id</code>.
     */
    public IAuthorBook setAuthorId(Long value);

    /**
     * Getter for <code>public.author_book.author_id</code>.
     */
    public Long getAuthorId();

    /**
     * Setter for <code>public.author_book.book_id</code>.
     */
    public IAuthorBook setBookId(Long value);

    /**
     * Getter for <code>public.author_book.book_id</code>.
     */
    public Long getBookId();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IAuthorBook
     */
    public void from(IAuthorBook from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IAuthorBook
     */
    public <E extends IAuthorBook> E into(E into);

    @Override
    public default IAuthorBook fromJson(io.vertx.core.json.JsonObject json) {
        try {
            setAuthorId(json.getLong("author_id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("author_id","java.lang.Long",e);
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
        json.put("author_id",getAuthorId());
        json.put("book_id",getBookId());
        return json;
    }

}
