/*
 * This file is generated by jOOQ.
 */
package com.ns.vertx.pg.jooq.tables.records;


import com.ns.vertx.pg.jooq.tables.Author;
import com.ns.vertx.pg.jooq.tables.interfaces.IAuthor;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AuthorRecord extends UpdatableRecordImpl<AuthorRecord> implements VertxPojo, Record3<Long, String, String>, IAuthor {

    private static final long serialVersionUID = -1590192907;

    /**
     * Setter for <code>public.author.author_id</code>.
     */
    @Override
    public AuthorRecord setAuthorId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.author.author_id</code>.
     */
    @Override
    public Long getAuthorId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.author.first_name</code>.
     */
    @Override
    public AuthorRecord setFirstName(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.author.first_name</code>.
     */
    @Override
    public String getFirstName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.author.last_name</code>.
     */
    @Override
    public AuthorRecord setLastName(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.author.last_name</code>.
     */
    @Override
    public String getLastName() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<Long, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<Long, String, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return Author.AUTHOR.AUTHOR_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return Author.AUTHOR.FIRST_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return Author.AUTHOR.LAST_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component1() {
        return getAuthorId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component2() {
        return getFirstName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getLastName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value1() {
        return getAuthorId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getFirstName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getLastName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorRecord value1(Long value) {
        setAuthorId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorRecord value2(String value) {
        setFirstName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorRecord value3(String value) {
        setLastName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthorRecord values(Long value1, String value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void from(IAuthor from) {
        setAuthorId(from.getAuthorId());
        setFirstName(from.getFirstName());
        setLastName(from.getLastName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E extends IAuthor> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached AuthorRecord
     */
    public AuthorRecord() {
        super(Author.AUTHOR);
    }

    /**
     * Create a detached, initialised AuthorRecord
     */
    public AuthorRecord(Long authorId, String firstName, String lastName) {
        super(Author.AUTHOR);

        set(0, authorId);
        set(1, firstName);
        set(2, lastName);
    }

    public AuthorRecord(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }
}
