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

import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Sequence;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = -1384738117;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.author</code>.
     */
    public final Author AUTHOR = Author.AUTHOR;

    /**
     * The table <code>public.author_book</code>.
     */
    public final AuthorBook AUTHOR_BOOK = AuthorBook.AUTHOR_BOOK;

    /**
     * The table <code>public.book</code>.
     */
    public final Book BOOK = Book.BOOK;

    /**
     * The table <code>public.category</code>.
     */
    public final Category CATEGORY = Category.CATEGORY;

    /**
     * The table <code>public.category_book</code>.
     */
    public final CategoryBook CATEGORY_BOOK = CategoryBook.CATEGORY_BOOK;

    /**
     * The table <code>public.order_item</code>.
     */
    public final OrderItem ORDER_ITEM = OrderItem.ORDER_ITEM;

    /**
     * The table <code>public.orders</code>.
     */
    public final Orders ORDERS = Orders.ORDERS;

    /**
     * The table <code>public.role</code>.
     */
    public final Role ROLE = Role.ROLE;

    /**
     * The table <code>public.users</code>.
     */
    public final Users USERS = Users.USERS;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Sequence<?>> getSequences() {
        return Arrays.<Sequence<?>>asList(
            Sequences.AUTHOR_AUTHOR_ID_SEQ,
            Sequences.BOOK_BOOK_ID_SEQ,
            Sequences.CATEGORY_CATEGORY_ID_SEQ,
            Sequences.ORDER_ITEM_ORDER_ITEM_ID_SEQ,
            Sequences.ORDERS_ORDER_ID_SEQ,
            Sequences.ROLE_ROLE_ID_SEQ,
            Sequences.USERS_USER_ID_SEQ);
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.<Table<?>>asList(
            Author.AUTHOR,
            AuthorBook.AUTHOR_BOOK,
            Book.BOOK,
            Category.CATEGORY,
            CategoryBook.CATEGORY_BOOK,
            OrderItem.ORDER_ITEM,
            Orders.ORDERS,
            Role.ROLE,
            Users.USERS);
    }
}
