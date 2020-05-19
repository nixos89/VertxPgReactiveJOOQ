/*
 * This file is generated by jOOQ.
 */
package com.ns.vertx.pg.jooq.tables.records;


import com.ns.vertx.pg.jooq.tables.Category;
import com.ns.vertx.pg.jooq.tables.interfaces.ICategory;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CategoryRecord extends UpdatableRecordImpl<CategoryRecord> implements VertxPojo, Record3<Long, String, Boolean>, ICategory {

    private static final long serialVersionUID = 2026036254;

    /**
     * Setter for <code>public.category.category_id</code>.
     */
    @Override
    public CategoryRecord setCategoryId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.category.category_id</code>.
     */
    @Override
    public Long getCategoryId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.category.name</code>.
     */
    @Override
    public CategoryRecord setName(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.category.name</code>.
     */
    @Override
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.category.is_deleted</code>.
     */
    @Override
    public CategoryRecord setIsDeleted(Boolean value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.category.is_deleted</code>.
     */
    @Override
    public Boolean getIsDeleted() {
        return (Boolean) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<Long, String, Boolean> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<Long, String, Boolean> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return Category.CATEGORY.CATEGORY_ID;
    }

    @Override
    public Field<String> field2() {
        return Category.CATEGORY.NAME;
    }

    @Override
    public Field<Boolean> field3() {
        return Category.CATEGORY.IS_DELETED;
    }

    @Override
    public Long component1() {
        return getCategoryId();
    }

    @Override
    public String component2() {
        return getName();
    }

    @Override
    public Boolean component3() {
        return getIsDeleted();
    }

    @Override
    public Long value1() {
        return getCategoryId();
    }

    @Override
    public String value2() {
        return getName();
    }

    @Override
    public Boolean value3() {
        return getIsDeleted();
    }

    @Override
    public CategoryRecord value1(Long value) {
        setCategoryId(value);
        return this;
    }

    @Override
    public CategoryRecord value2(String value) {
        setName(value);
        return this;
    }

    @Override
    public CategoryRecord value3(Boolean value) {
        setIsDeleted(value);
        return this;
    }

    @Override
    public CategoryRecord values(Long value1, String value2, Boolean value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(ICategory from) {
        setCategoryId(from.getCategoryId());
        setName(from.getName());
        setIsDeleted(from.getIsDeleted());
    }

    @Override
    public <E extends ICategory> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached CategoryRecord
     */
    public CategoryRecord() {
        super(Category.CATEGORY);
    }

    /**
     * Create a detached, initialised CategoryRecord
     */
    public CategoryRecord(Long categoryId, String name, Boolean isDeleted) {
        super(Category.CATEGORY);

        set(0, categoryId);
        set(1, name);
        set(2, isDeleted);
    }

    public CategoryRecord(io.vertx.core.json.JsonObject json) {
        this();
        fromJson(json);
    }
}
