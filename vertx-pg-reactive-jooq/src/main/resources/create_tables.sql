CREATE TABLE IF NOT EXISTS Author(author_id BIGSERIAL PRIMARY KEY, first_name VARCHAR(30), last_name VARCHAR(30) );
CREATE TABLE IF NOT EXISTS Category (category_id BIGSERIAL PRIMARY KEY, name VARCHAR(30), is_deleted boolean);
CREATE TABLE IF NOT EXISTS Book( book_id BIGSERIAL PRIMARY KEY, title VARCHAR(255), price double precision, amount INTEGER, is_deleted boolean);
CREATE TABLE IF NOT EXISTS Role(role_id BIGSERIAL PRIMARY KEY, name VARCHAR(30));
CREATE TABLE IF NOT EXISTS Users (user_id BIGSERIAL PRIMARY KEY, first_name VARCHAR(30), last_name VARCHAR(30), 
								  email VARCHAR(30), username VARCHAR(15), password VARCHAR(255), role_id INTEGER REFERENCES Role(role_id) );
CREATE TABLE IF NOT EXISTS Orders (order_id BIGSERIAL PRIMARY KEY, total double precision, order_date TIMESTAMP, user_id INTEGER REFERENCES Users(user_id) );
CREATE TABLE IF NOT EXISTS Order_Item ( order_item_id BIGSERIAL PRIMARY KEY, amount INTEGER, book_id BIGINT REFERENCES Book(book_id), 
									     order_id BIGINT REFERENCES Orders(order_id) );

CREATE TABLE IF NOT EXISTS Author_Book (
	author_id BIGINT REFERENCES Author(author_id) ON UPDATE CASCADE ON DELETE CASCADE,
	book_id BIGINT REFERENCES Book(book_id) ON UPDATE CASCADE ON DELETE CASCADE,
	CONSTRAINT Author_Book_pkey PRIMARY KEY (author_id, book_id) );

CREATE TABLE IF NOT EXISTS Category_Book(
	category_id BIGINT REFERENCES Category(category_id) ON UPDATE CASCADE ON DELETE CASCADE,
	book_id BIGINT REFERENCES Book(book_id) ON UPDATE CASCADE ON DELETE CASCADE, 
	CONSTRAINT Category_Book_pkey PRIMARY KEY (category_id, book_id) );
	                        