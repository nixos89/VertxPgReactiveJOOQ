/*
 * This file is generated by jOOQ.
 */
package com.ns.vertx.pg.jooq.tables.interfaces;


import io.github.jklingsporn.vertx.jooq.shared.UnexpectedJsonValueType;
import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import java.io.Serializable;

import javax.annotation.Generated;


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
public interface IUsers extends VertxPojo, Serializable {

    /**
     * Setter for <code>public.users.user_id</code>.
     */
    public IUsers setUserId(Long value);

    /**
     * Getter for <code>public.users.user_id</code>.
     */
    public Long getUserId();

    /**
     * Setter for <code>public.users.first_name</code>.
     */
    public IUsers setFirstName(String value);

    /**
     * Getter for <code>public.users.first_name</code>.
     */
    public String getFirstName();

    /**
     * Setter for <code>public.users.last_name</code>.
     */
    public IUsers setLastName(String value);

    /**
     * Getter for <code>public.users.last_name</code>.
     */
    public String getLastName();

    /**
     * Setter for <code>public.users.email</code>.
     */
    public IUsers setEmail(String value);

    /**
     * Getter for <code>public.users.email</code>.
     */
    public String getEmail();

    /**
     * Setter for <code>public.users.username</code>.
     */
    public IUsers setUsername(String value);

    /**
     * Getter for <code>public.users.username</code>.
     */
    public String getUsername();

    /**
     * Setter for <code>public.users.password</code>.
     */
    public IUsers setPassword(String value);

    /**
     * Getter for <code>public.users.password</code>.
     */
    public String getPassword();

    /**
     * Setter for <code>public.users.role_id</code>.
     */
    public IUsers setRoleId(Integer value);

    /**
     * Getter for <code>public.users.role_id</code>.
     */
    public Integer getRoleId();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IUsers
     */
    public void from(com.ns.vertx.pg.jooq.tables.interfaces.IUsers from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IUsers
     */
    public <E extends com.ns.vertx.pg.jooq.tables.interfaces.IUsers> E into(E into);

    @Override
    public default IUsers fromJson(io.vertx.core.json.JsonObject json) {
        try {
            setUserId(json.getLong("user_id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("user_id","java.lang.Long",e);
        }
        try {
            setFirstName(json.getString("first_name"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("first_name","java.lang.String",e);
        }
        try {
            setLastName(json.getString("last_name"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("last_name","java.lang.String",e);
        }
        try {
            setEmail(json.getString("email"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("email","java.lang.String",e);
        }
        try {
            setUsername(json.getString("username"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("username","java.lang.String",e);
        }
        try {
            setPassword(json.getString("password"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("password","java.lang.String",e);
        }
        try {
            setRoleId(json.getInteger("role_id"));
        } catch (java.lang.ClassCastException e) {
            throw new UnexpectedJsonValueType("role_id","java.lang.Integer",e);
        }
        return this;
    }


    @Override
    public default io.vertx.core.json.JsonObject toJson() {
        io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
        json.put("user_id",getUserId());
        json.put("first_name",getFirstName());
        json.put("last_name",getLastName());
        json.put("email",getEmail());
        json.put("username",getUsername());
        json.put("password",getPassword());
        json.put("role_id",getRoleId());
        return json;
    }

}
