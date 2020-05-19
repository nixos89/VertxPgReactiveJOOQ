package com.ns.vertx.pg.jooq.tables.mappers;

import io.vertx.sqlclient.Row;
import java.util.function.Function;

public class RowMappers {

    private RowMappers(){}

    public static Function<Row,com.ns.vertx.pg.jooq.tables.pojos.Author> getAuthorMapper() {
        return row -> {
            com.ns.vertx.pg.jooq.tables.pojos.Author pojo = new com.ns.vertx.pg.jooq.tables.pojos.Author();
            pojo.setAuthorId(row.getLong("author_id"));
            pojo.setFirstName(row.getString("first_name"));
            pojo.setLastName(row.getString("last_name"));
            return pojo;
        };
    }

    public static Function<Row,com.ns.vertx.pg.jooq.tables.pojos.AuthorBook> getAuthorBookMapper() {
        return row -> {
            com.ns.vertx.pg.jooq.tables.pojos.AuthorBook pojo = new com.ns.vertx.pg.jooq.tables.pojos.AuthorBook();
            pojo.setAuthorId(row.getLong("author_id"));
            pojo.setBookId(row.getLong("book_id"));
            return pojo;
        };
    }

    public static Function<Row,com.ns.vertx.pg.jooq.tables.pojos.Book> getBookMapper() {
        return row -> {
            com.ns.vertx.pg.jooq.tables.pojos.Book pojo = new com.ns.vertx.pg.jooq.tables.pojos.Book();
            pojo.setBookId(row.getLong("book_id"));
            pojo.setTitle(row.getString("title"));
            pojo.setPrice(row.getDouble("price"));
            pojo.setAmount(row.getInteger("amount"));
            pojo.setIsDeleted(row.getBoolean("is_deleted"));
            return pojo;
        };
    }

    public static Function<Row,com.ns.vertx.pg.jooq.tables.pojos.Category> getCategoryMapper() {
        return row -> {
            com.ns.vertx.pg.jooq.tables.pojos.Category pojo = new com.ns.vertx.pg.jooq.tables.pojos.Category();
            pojo.setCategoryId(row.getLong("category_id"));
            pojo.setName(row.getString("name"));
            pojo.setIsDeleted(row.getBoolean("is_deleted"));
            return pojo;
        };
    }

    public static Function<Row,com.ns.vertx.pg.jooq.tables.pojos.CategoryBook> getCategoryBookMapper() {
        return row -> {
            com.ns.vertx.pg.jooq.tables.pojos.CategoryBook pojo = new com.ns.vertx.pg.jooq.tables.pojos.CategoryBook();
            pojo.setCategoryId(row.getLong("category_id"));
            pojo.setBookId(row.getLong("book_id"));
            return pojo;
        };
    }

    public static Function<Row,com.ns.vertx.pg.jooq.tables.pojos.OrderItem> getOrderItemMapper() {
        return row -> {
            com.ns.vertx.pg.jooq.tables.pojos.OrderItem pojo = new com.ns.vertx.pg.jooq.tables.pojos.OrderItem();
            pojo.setOrderItemId(row.getLong("order_item_id"));
            pojo.setAmount(row.getInteger("amount"));
            pojo.setBookId(row.getLong("book_id"));
            pojo.setOrderId(row.getLong("order_id"));
            return pojo;
        };
    }

    public static Function<Row,com.ns.vertx.pg.jooq.tables.pojos.Orders> getOrdersMapper() {
        return row -> {
            com.ns.vertx.pg.jooq.tables.pojos.Orders pojo = new com.ns.vertx.pg.jooq.tables.pojos.Orders();
            pojo.setOrderId(row.getLong("order_id"));
            pojo.setTotal(row.getDouble("total"));
            pojo.setOrderDate(row.getLocalDateTime("order_date"));
            pojo.setUserId(row.getInteger("user_id"));
            return pojo;
        };
    }

    public static Function<Row,com.ns.vertx.pg.jooq.tables.pojos.Role> getRoleMapper() {
        return row -> {
            com.ns.vertx.pg.jooq.tables.pojos.Role pojo = new com.ns.vertx.pg.jooq.tables.pojos.Role();
            pojo.setRoleId(row.getLong("role_id"));
            pojo.setName(row.getString("name"));
            return pojo;
        };
    }

    public static Function<Row,com.ns.vertx.pg.jooq.tables.pojos.Users> getUsersMapper() {
        return row -> {
            com.ns.vertx.pg.jooq.tables.pojos.Users pojo = new com.ns.vertx.pg.jooq.tables.pojos.Users();
            pojo.setUserId(row.getLong("user_id"));
            pojo.setFirstName(row.getString("first_name"));
            pojo.setLastName(row.getString("last_name"));
            pojo.setEmail(row.getString("email"));
            pojo.setUsername(row.getString("username"));
            pojo.setPassword(row.getString("password"));
            pojo.setRoleId(row.getInteger("role_id"));
            return pojo;
        };
    }

}
