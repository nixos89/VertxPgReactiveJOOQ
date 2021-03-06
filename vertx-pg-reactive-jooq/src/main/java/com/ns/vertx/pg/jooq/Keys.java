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
import com.ns.vertx.pg.jooq.tables.records.AuthorBookRecord;
import com.ns.vertx.pg.jooq.tables.records.AuthorRecord;
import com.ns.vertx.pg.jooq.tables.records.BookRecord;
import com.ns.vertx.pg.jooq.tables.records.CategoryBookRecord;
import com.ns.vertx.pg.jooq.tables.records.CategoryRecord;
import com.ns.vertx.pg.jooq.tables.records.OrderItemRecord;
import com.ns.vertx.pg.jooq.tables.records.OrdersRecord;
import com.ns.vertx.pg.jooq.tables.records.RoleRecord;
import com.ns.vertx.pg.jooq.tables.records.UsersRecord;

import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code>public</code> schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<AuthorRecord, Long> IDENTITY_AUTHOR = Identities0.IDENTITY_AUTHOR;
    public static final Identity<BookRecord, Long> IDENTITY_BOOK = Identities0.IDENTITY_BOOK;
    public static final Identity<CategoryRecord, Long> IDENTITY_CATEGORY = Identities0.IDENTITY_CATEGORY;
    public static final Identity<OrderItemRecord, Long> IDENTITY_ORDER_ITEM = Identities0.IDENTITY_ORDER_ITEM;
    public static final Identity<OrdersRecord, Long> IDENTITY_ORDERS = Identities0.IDENTITY_ORDERS;
    public static final Identity<RoleRecord, Long> IDENTITY_ROLE = Identities0.IDENTITY_ROLE;
    public static final Identity<UsersRecord, Long> IDENTITY_USERS = Identities0.IDENTITY_USERS;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<AuthorRecord> AUTHOR_PKEY = UniqueKeys0.AUTHOR_PKEY;
    public static final UniqueKey<AuthorBookRecord> AUTHOR_BOOK_PKEY = UniqueKeys0.AUTHOR_BOOK_PKEY;
    public static final UniqueKey<BookRecord> BOOK_PKEY = UniqueKeys0.BOOK_PKEY;
    public static final UniqueKey<CategoryRecord> CATEGORY_PKEY = UniqueKeys0.CATEGORY_PKEY;
    public static final UniqueKey<CategoryBookRecord> CATEGORY_BOOK_PKEY = UniqueKeys0.CATEGORY_BOOK_PKEY;
    public static final UniqueKey<OrderItemRecord> ORDER_ITEM_PKEY = UniqueKeys0.ORDER_ITEM_PKEY;
    public static final UniqueKey<OrdersRecord> ORDERS_PKEY = UniqueKeys0.ORDERS_PKEY;
    public static final UniqueKey<RoleRecord> ROLE_PKEY = UniqueKeys0.ROLE_PKEY;
    public static final UniqueKey<UsersRecord> USERS_PKEY = UniqueKeys0.USERS_PKEY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<AuthorBookRecord, AuthorRecord> AUTHOR_BOOK__AUTHOR_BOOK_AUTHOR_ID_FKEY = ForeignKeys0.AUTHOR_BOOK__AUTHOR_BOOK_AUTHOR_ID_FKEY;
    public static final ForeignKey<AuthorBookRecord, BookRecord> AUTHOR_BOOK__AUTHOR_BOOK_BOOK_ID_FKEY = ForeignKeys0.AUTHOR_BOOK__AUTHOR_BOOK_BOOK_ID_FKEY;
    public static final ForeignKey<CategoryBookRecord, CategoryRecord> CATEGORY_BOOK__CATEGORY_BOOK_CATEGORY_ID_FKEY = ForeignKeys0.CATEGORY_BOOK__CATEGORY_BOOK_CATEGORY_ID_FKEY;
    public static final ForeignKey<CategoryBookRecord, BookRecord> CATEGORY_BOOK__CATEGORY_BOOK_BOOK_ID_FKEY = ForeignKeys0.CATEGORY_BOOK__CATEGORY_BOOK_BOOK_ID_FKEY;
    public static final ForeignKey<OrderItemRecord, BookRecord> ORDER_ITEM__ORDER_ITEM_BOOK_ID_FKEY = ForeignKeys0.ORDER_ITEM__ORDER_ITEM_BOOK_ID_FKEY;
    public static final ForeignKey<OrderItemRecord, OrdersRecord> ORDER_ITEM__ORDER_ITEM_ORDER_ID_FKEY = ForeignKeys0.ORDER_ITEM__ORDER_ITEM_ORDER_ID_FKEY;
    public static final ForeignKey<OrdersRecord, UsersRecord> ORDERS__ORDERS_USER_ID_FKEY = ForeignKeys0.ORDERS__ORDERS_USER_ID_FKEY;
    public static final ForeignKey<UsersRecord, RoleRecord> USERS__USERS_ROLE_ID_FKEY = ForeignKeys0.USERS__USERS_ROLE_ID_FKEY;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 {
        public static Identity<AuthorRecord, Long> IDENTITY_AUTHOR = Internal.createIdentity(Author.AUTHOR, Author.AUTHOR.AUTHOR_ID);
        public static Identity<BookRecord, Long> IDENTITY_BOOK = Internal.createIdentity(Book.BOOK, Book.BOOK.BOOK_ID);
        public static Identity<CategoryRecord, Long> IDENTITY_CATEGORY = Internal.createIdentity(Category.CATEGORY, Category.CATEGORY.CATEGORY_ID);
        public static Identity<OrderItemRecord, Long> IDENTITY_ORDER_ITEM = Internal.createIdentity(OrderItem.ORDER_ITEM, OrderItem.ORDER_ITEM.ORDER_ITEM_ID);
        public static Identity<OrdersRecord, Long> IDENTITY_ORDERS = Internal.createIdentity(Orders.ORDERS, Orders.ORDERS.ORDER_ID);
        public static Identity<RoleRecord, Long> IDENTITY_ROLE = Internal.createIdentity(Role.ROLE, Role.ROLE.ROLE_ID);
        public static Identity<UsersRecord, Long> IDENTITY_USERS = Internal.createIdentity(Users.USERS, Users.USERS.USER_ID);
    }

    private static class UniqueKeys0 {
        public static final UniqueKey<AuthorRecord> AUTHOR_PKEY = Internal.createUniqueKey(Author.AUTHOR, "author_pkey", new TableField[] { Author.AUTHOR.AUTHOR_ID }, true);
        public static final UniqueKey<AuthorBookRecord> AUTHOR_BOOK_PKEY = Internal.createUniqueKey(AuthorBook.AUTHOR_BOOK, "author_book_pkey", new TableField[] { AuthorBook.AUTHOR_BOOK.AUTHOR_ID, AuthorBook.AUTHOR_BOOK.BOOK_ID }, true);
        public static final UniqueKey<BookRecord> BOOK_PKEY = Internal.createUniqueKey(Book.BOOK, "book_pkey", new TableField[] { Book.BOOK.BOOK_ID }, true);
        public static final UniqueKey<CategoryRecord> CATEGORY_PKEY = Internal.createUniqueKey(Category.CATEGORY, "category_pkey", new TableField[] { Category.CATEGORY.CATEGORY_ID }, true);
        public static final UniqueKey<CategoryBookRecord> CATEGORY_BOOK_PKEY = Internal.createUniqueKey(CategoryBook.CATEGORY_BOOK, "category_book_pkey", new TableField[] { CategoryBook.CATEGORY_BOOK.CATEGORY_ID, CategoryBook.CATEGORY_BOOK.BOOK_ID }, true);
        public static final UniqueKey<OrderItemRecord> ORDER_ITEM_PKEY = Internal.createUniqueKey(OrderItem.ORDER_ITEM, "order_item_pkey", new TableField[] { OrderItem.ORDER_ITEM.ORDER_ITEM_ID }, true);
        public static final UniqueKey<OrdersRecord> ORDERS_PKEY = Internal.createUniqueKey(Orders.ORDERS, "orders_pkey", new TableField[] { Orders.ORDERS.ORDER_ID }, true);
        public static final UniqueKey<RoleRecord> ROLE_PKEY = Internal.createUniqueKey(Role.ROLE, "role_pkey", new TableField[] { Role.ROLE.ROLE_ID }, true);
        public static final UniqueKey<UsersRecord> USERS_PKEY = Internal.createUniqueKey(Users.USERS, "users_pkey", new TableField[] { Users.USERS.USER_ID }, true);
    }

    private static class ForeignKeys0 {
        public static final ForeignKey<AuthorBookRecord, AuthorRecord> AUTHOR_BOOK__AUTHOR_BOOK_AUTHOR_ID_FKEY = Internal.createForeignKey(Keys.AUTHOR_PKEY, AuthorBook.AUTHOR_BOOK, "author_book_author_id_fkey", new TableField[] { AuthorBook.AUTHOR_BOOK.AUTHOR_ID }, true);
        public static final ForeignKey<AuthorBookRecord, BookRecord> AUTHOR_BOOK__AUTHOR_BOOK_BOOK_ID_FKEY = Internal.createForeignKey(Keys.BOOK_PKEY, AuthorBook.AUTHOR_BOOK, "author_book_book_id_fkey", new TableField[] { AuthorBook.AUTHOR_BOOK.BOOK_ID }, true);
        public static final ForeignKey<CategoryBookRecord, CategoryRecord> CATEGORY_BOOK__CATEGORY_BOOK_CATEGORY_ID_FKEY = Internal.createForeignKey(Keys.CATEGORY_PKEY, CategoryBook.CATEGORY_BOOK, "category_book_category_id_fkey", new TableField[] { CategoryBook.CATEGORY_BOOK.CATEGORY_ID }, true);
        public static final ForeignKey<CategoryBookRecord, BookRecord> CATEGORY_BOOK__CATEGORY_BOOK_BOOK_ID_FKEY = Internal.createForeignKey(Keys.BOOK_PKEY, CategoryBook.CATEGORY_BOOK, "category_book_book_id_fkey", new TableField[] { CategoryBook.CATEGORY_BOOK.BOOK_ID }, true);
        public static final ForeignKey<OrderItemRecord, BookRecord> ORDER_ITEM__ORDER_ITEM_BOOK_ID_FKEY = Internal.createForeignKey(Keys.BOOK_PKEY, OrderItem.ORDER_ITEM, "order_item_book_id_fkey", new TableField[] { OrderItem.ORDER_ITEM.BOOK_ID }, true);
        public static final ForeignKey<OrderItemRecord, OrdersRecord> ORDER_ITEM__ORDER_ITEM_ORDER_ID_FKEY = Internal.createForeignKey(Keys.ORDERS_PKEY, OrderItem.ORDER_ITEM, "order_item_order_id_fkey", new TableField[] { OrderItem.ORDER_ITEM.ORDER_ID }, true);
        public static final ForeignKey<OrdersRecord, UsersRecord> ORDERS__ORDERS_USER_ID_FKEY = Internal.createForeignKey(Keys.USERS_PKEY, Orders.ORDERS, "orders_user_id_fkey", new TableField[] { Orders.ORDERS.USER_ID }, true);
        public static final ForeignKey<UsersRecord, RoleRecord> USERS__USERS_ROLE_ID_FKEY = Internal.createForeignKey(Keys.ROLE_PKEY, Users.USERS, "users_role_id_fkey", new TableField[] { Users.USERS.ROLE_ID }, true);
    }
}
