package com.ns.vertx.pg.service;

public class DBQueries {
	
	// Category CRUD queries
	static String GET_ALL_CATEGORIES_SQL = "SELECT category_id, name, is_deleted FROM Category ORDER BY category_id ASC;";
	static String GET_CATEGORY_BY_ID_SQL = "SELECT category_id, name, is_deleted FROM Category WHERE category_id = $1;";
	static String CREATE_CATEGORY_SQL = "INSERT INTO Category (name, is_deleted) VALUES($1, $2) RETURNING category_id;";
	static String UPDATE_CATEGORY_SQL = "UPDATE Category SET name = $1, is_deleted = $2 WHERE category_id = $3 RETURNING category_id;"; 
	static String DELETE_CATEGORY_BY_ID_SQL = "DELETE FROM Category WHERE category_id = $1;";
	
	// Book SELECT queries
	static String GET_BOOK_BY_BOOK_ID = "SELECT b.book_id AS b_id, b.title, b.price, b.amount, b.is_deleted, " +
			"to_json(array_agg(DISTINCT aut.*)) as authors, to_json(array_agg(DISTINCT cat.*)) as categories " +  			
			"FROM book b " + 
            "LEFT JOIN author_book AS ab ON b.book_id = ab.book_id " +
            "LEFT JOIN author AS aut ON ab.author_id = aut.author_id " +
            "LEFT JOIN category_book AS cb ON b.book_id = cb.book_id " +
            "LEFT JOIN category AS cat ON cb.category_id = cat.category_id " +
            "WHERE b.book_id = :id " + 
            "GROUP BY b_id ORDER BY b_id ASC;";
	
	static String GET_ALL_BOOKS = "SELECT b.book_id AS b_id, b.title, b.price, b.amount, b.is_deleted, " +
			"to_json(array_agg(DISTINCT aut.*)) as authors, to_json(array_agg(DISTINCT cat.*)) as categories " +  			
			"FROM book b " + 
            "LEFT JOIN author_book AS ab ON b.book_id = ab.book_id " +
            "LEFT JOIN author AS aut ON ab.author_id = aut.author_id " +
            "LEFT JOIN category_book AS cb ON b.book_id = cb.book_id " +
            "LEFT JOIN category AS cat ON cb.category_id = cat.category_id " +
            "GROUP BY b_id ORDER BY b_id ASC;";
	
	
	static String GET_ALL_BOOKS_BY_AUTHOR_ID = "SELECT b.book_id AS b_id, b.title, b.price, b.amount, b.is_deleted, " +
			"to_json(array_agg(DISTINCT aut.*)) as authors, to_json(array_agg(DISTINCT cat.*)) as categories " +  			
			"FROM book b " + 
            "LEFT JOIN author_book AS ab ON b.book_id = ab.book_id " +
            "LEFT JOIN author AS aut ON ab.author_id = aut.author_id " +
            "LEFT JOIN category_book AS cb ON b.book_id = cb.book_id " +
            "LEFT JOIN category AS cat ON cb.category_id = cat.category_id " +
            "WHERE aut.author_id = :id " + 
            "GROUP BY b_id ORDER BY b_id ASC;";	
	
	
	static String GET_ALL_ORDERS = "SELECT o.order_id, o.order_date, o.total, users.*, oi.*, "+ 
			"to_json(array_agg(DISTINCT aut.*)) as authors, to_json(array_agg(DISTINCT cat.*)) as categories," +
			"b.* " +
			"FROM orders AS o " + 
			"LEFT JOIN order_item AS oi ON o.order_id = oi.order_id " + 
			"LEFT JOIN users ON o.user_id = users.user_id " + 
			"LEFT JOIN book AS b ON oi.book_id = b.book_id " + 
			"LEFT JOIN author_book AS ab ON b.book_id = ab.book_id " +
            "LEFT JOIN author AS aut ON ab.author_id = aut.author_id " +
            "LEFT JOIN category_book AS cb ON b.book_id = cb.book_id " +
            "LEFT JOIN category AS cat ON cb.category_id = cat.category_id " +
            "GROUP BY o.order_id, users.user_id, oi.order_item_id, b.book_id, aut.author_id, cat.category_id " +
            "ORDER BY o.order_id ASC";		
}
