/*
 * This file is generated by jOOQ.
 */
package com.ns.vertx.pg.jooq;


import com.ns.vertx.pg.jooq.tables.Author;
import com.ns.vertx.pg.jooq.tables.AuthorBook;
import com.ns.vertx.pg.jooq.tables.Book;
import com.ns.vertx.pg.jooq.tables.Category;
import com.ns.vertx.pg.jooq.tables.CategoryBook;
import com.ns.vertx.pg.jooq.tables.OrderItem;
import com.ns.vertx.pg.jooq.tables.Orders;
import com.ns.vertx.pg.jooq.tables.Role;
import com.ns.vertx.pg.jooq.tables.Users;

import javax.annotation.Generated;


/**
 * Convenience access to all tables in public
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>public.author</code>.
     */
    public static final Author AUTHOR = com.ns.vertx.pg.jooq.tables.Author.AUTHOR;

    /**
     * The table <code>public.author_book</code>.
     */
    public static final AuthorBook AUTHOR_BOOK = com.ns.vertx.pg.jooq.tables.AuthorBook.AUTHOR_BOOK;

    /**
     * The table <code>public.book</code>.
     */
    public static final Book BOOK = com.ns.vertx.pg.jooq.tables.Book.BOOK;

    /**
     * The table <code>public.category</code>.
     */
    public static final Category CATEGORY = com.ns.vertx.pg.jooq.tables.Category.CATEGORY;

    /**
     * The table <code>public.category_book</code>.
     */
    public static final CategoryBook CATEGORY_BOOK = com.ns.vertx.pg.jooq.tables.CategoryBook.CATEGORY_BOOK;

    /**
     * The table <code>public.order_item</code>.
     */
    public static final OrderItem ORDER_ITEM = com.ns.vertx.pg.jooq.tables.OrderItem.ORDER_ITEM;

    /**
     * The table <code>public.orders</code>.
     */
    public static final Orders ORDERS = com.ns.vertx.pg.jooq.tables.Orders.ORDERS;

    /**
     * The table <code>public.role</code>.
     */
    public static final Role ROLE = com.ns.vertx.pg.jooq.tables.Role.ROLE;

    /**
     * The table <code>public.users</code>.
     */
    public static final Users USERS = com.ns.vertx.pg.jooq.tables.Users.USERS;
}
