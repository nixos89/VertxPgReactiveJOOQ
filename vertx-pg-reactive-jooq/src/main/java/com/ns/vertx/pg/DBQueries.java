package com.ns.vertx.pg;

public class DBQueries {
	
	// create tables::START
	public static String CREATE_AUTHOR_TABLE_SQL = "CREATE TABLE IF NOT EXISTS Author(author_id BIGSERIAL PRIMARY KEY, first_name VARCHAR(30), last_name VARCHAR(30) );";
	public static String CREATE_CATEGORY_TABLE_SQL = "CREATE TABLE IF NOT EXISTS Category (category_id BIGSERIAL PRIMARY KEY, name VARCHAR(30), is_deleted boolean);";	
	public static String CREATE_BOOK_TABLE_SQL = "CREATE TABLE IF NOT EXISTS Book( book_id BIGSERIAL PRIMARY KEY, title VARCHAR(255), price double precision, amount INTEGER, is_deleted boolean);";			
	public static String CREATE_ROLE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS Role(role_id BIGSERIAL PRIMARY KEY, name VARCHAR(30));";
	public static String CREATE_USER_TABLE_SQL = "CREATE TABLE IF NOT EXISTS Users (user_id BIGSERIAL PRIMARY KEY, first_name VARCHAR(30), last_name VARCHAR(30), " +
            "email VARCHAR(30), username VARCHAR(15), password VARCHAR(255), role_id INTEGER REFERENCES Role(role_id) );";
	public static String CREATE_ORDER_TABLE_SQL = "CREATE TABLE IF NOT EXISTS Orders (order_id BIGSERIAL PRIMARY KEY, total double precision, order_date DATE, user_id INTEGER REFERENCES Users(user_id) );";
	public static String CREATE_ORDER_ITEM_TABLE_SQL = "CREATE TABLE IF NOT EXISTS Order_Item ( order_item_id BIGSERIAL PRIMARY KEY, amount INTEGER, book_id BIGINT REFERENCES Book(book_id), order_id BIGINT REFERENCES Orders(order_id) );";
	
	// intermediate tables
	public static String CREATE_AUTHOR_BOOK_TABLE_SQL =  "CREATE TABLE IF NOT EXISTS Author_Book (author_id BIGINT REFERENCES Author(author_id) ON UPDATE CASCADE ON DELETE CASCADE," +
            " book_id BIGINT REFERENCES Book(book_id) ON UPDATE CASCADE ON DELETE CASCADE," +
            " CONSTRAINT Author_Book_pkey PRIMARY KEY (author_id, book_id) );";
	
	public static String CREATE_BOOK_CATEGORY_TABLE_SQL = "CREATE TABLE IF NOT EXISTS Category_Book(category_id BIGINT REFERENCES Category(category_id) ON UPDATE CASCADE ON DELETE CASCADE," +
														    " book_id BIGINT REFERENCES Book(book_id) ON UPDATE CASCADE ON DELETE CASCADE, CONSTRAINT Category_Book_pkey PRIMARY KEY (category_id, book_id) );";	
	// create tables::END	
	
	// Category CRUD queries
	public static String GET_ALL_CATEGORIES_SQL = "SELECT category_id, name, is_deleted FROM Category;";
	public static String GET_CATEGORY_BY_ID_SQL = "SELECT category_id, name, is_deleted FROM Category WHERE category_id = $1;";
	public static String CREATE_CATEGORY_SQL = "INSERT INTO Category (name, is_deleted) VALUES($1, $2) RETURNING category_id;";
	public static String UPDATE_CATEGORY_SQL = "UPDATE Category SET name = $1, is_deleted = $2 WHERE category_id = $3 RETURNING category_id;"; 
	public static String DELETE_CATEGORY_BY_ID_SQL = "DELETE FROM Category WHERE category_id = $1;";
	
	
}
