/*
 * This file is generated by jOOQ.
 */
package com.ns.vertx.pg.jooq.routines;


import com.ns.vertx.pg.jooq.Public;

import org.jooq.Field;
import org.jooq.Parameter;
import org.jooq.impl.AbstractRoutine;
import org.jooq.impl.Internal;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class GetOrderByOrderId extends AbstractRoutine<String> {

    private static final long serialVersionUID = 1388543727;

    /**
     * The parameter <code>public.get_order_by_order_id.RETURN_VALUE</code>.
     */
    public static final Parameter<String> RETURN_VALUE = Internal.createParameter("RETURN_VALUE", org.jooq.impl.SQLDataType.JSON, false, false, org.jooq.Converter.ofNullable(org.jooq.JSON.class, String.class, Object::toString, org.jooq.JSON::valueOf));

    /**
     * The parameter <code>public.get_order_by_order_id.o_id</code>.
     */
    public static final Parameter<String> O_ID = Internal.createParameter("o_id", org.jooq.impl.SQLDataType.BIGINT, false, false, org.jooq.Converter.ofNullable(java.lang.Long.class, String.class, Object::toString, java.lang.Long::valueOf));

    /**
     * Create a new routine call instance
     */
    public GetOrderByOrderId() {
        super("get_order_by_order_id", Public.PUBLIC, org.jooq.impl.SQLDataType.JSON, org.jooq.Converter.ofNullable(org.jooq.JSON.class, String.class, Object::toString, org.jooq.JSON::valueOf));

        setReturnParameter(RETURN_VALUE);
        addInParameter(O_ID);
    }

    /**
     * Set the <code>o_id</code> parameter IN value to the routine
     */
    public void setOId(String value) {
        setValue(O_ID, value);
    }

    /**
     * Set the <code>o_id</code> parameter to the function to be used with a {@link org.jooq.Select} statement
     */
    public GetOrderByOrderId setOId(Field<String> field) {
        setField(O_ID, field);
        return this;
    }
}
