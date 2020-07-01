--
-- NOTE:
--
-- File paths need to be edited. Search for $$PATH$$ and
-- replace it with the path to the directory containing
-- the extracted data files.
--
--
-- PostgreSQL database dump
--

-- Dumped from database version 11.8
-- Dumped by pg_dump version 11.8

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP DATABASE "vertx-jooq-cr";
--
-- Name: vertx-jooq-cr; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE "vertx-jooq-cr" WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8';


ALTER DATABASE "vertx-jooq-cr" OWNER TO postgres;

\connect -reuse-previous=on "dbname='vertx-jooq-cr'"

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: get_all_orders(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.get_all_orders() RETURNS json
    LANGUAGE plpgsql
    AS $$
DECLARE
    single_order RECORD;
    single_order_json json;
    orders_array json[];
BEGIN

    FOR single_order IN SELECT * FROM public.orders ORDER BY order_id
    LOOP
        SELECT get_order_by_order_id2(single_order.order_id) INTO single_order_json;
        orders_array = array_append(orders_array, single_order_json);
    END LOOP;

    return (select json_build_object(
        'orders', orders_array
    ));
END;
$$;


ALTER FUNCTION public.get_all_orders() OWNER TO postgres;

--
-- Name: get_book_by_book_id(bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.get_book_by_book_id(b_id bigint) RETURNS json
    LANGUAGE plpgsql
    AS $$
declare
	found_book book;
	book_authors json;
	book_categories json;
    book_price double precision;
begin
	-- Load book data:
	select * into found_book
	from book b2
	where b2.book_id  = b_id;

	-- Get assigned authors
	select case when count(x) = 0 then '[]' else json_agg(x) end into book_authors
	from (select aut.*
		from book b
		inner join author_book as ab on b.book_id = ab.book_id
		inner join author as aut on ab.author_id = aut.author_id
		where b.book_id = b_id) x;

	-- Get assigned categories
	select case when count(y) = 0 then '[]' else json_agg(y) end into book_categories
	from (select cat.*
		from book b
		inner join category_book as cb on b.book_id = cb.book_id
		inner join category as cat on cb.category_id = cat.category_id
		where b.book_id = b_id) y;

	book_price = trunc(found_book.price::double precision::text::numeric, 2);
	-- Build the JSON response:
	return (select json_build_object(
		'book_id', found_book.book_id,
		'title', found_book.title,
		'price', trunc(found_book.price::double precision::text::numeric, 2),
		'amount', found_book.amount,
		'is_deleted', found_book.is_deleted,
		'authors', book_authors,
		'categories', book_categories
	));
end
$$;


ALTER FUNCTION public.get_book_by_book_id(b_id bigint) OWNER TO postgres;

--
-- Name: get_order_by_order_id(bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.get_order_by_order_id(o_id bigint) RETURNS json
    LANGUAGE plpgsql
    AS $$
DECLARE
	total_oi_price double precision;
	book_price double precision;
	total_price double precision;
	oi_amount integer;
    order_items json;
    item_recs RECORD;
    book_json json;
    single_order_item json;
    found_order "vertx-jooq-cr".public.orders;
    found_user json;
    item_array json[];
BEGIN
    select * into found_order
    from "vertx-jooq-cr".public.orders
    where order_id = o_id;

    select json_build_object('user_id', "vertx-jooq-cr".public.users.user_id, 'username', "vertx-jooq-cr".public.users.username)
    into found_user
    from "vertx-jooq-cr".public.users
    INNER JOIN "vertx-jooq-cr".public.orders as o USING (user_id)
    WHERE o.order_id = o_id;

	total_price = 0.00;

    FOR item_recs IN SELECT *
		FROM public.order_item AS oi WHERE oi.order_id = o_id
	LOOP
        select public.get_book_by_book_id(item_recs.book_id) into book_json
	    from public.order_item
	    where public.order_item.order_item_id IN (item_recs.order_item_id);

		select price INTO book_price FROM book AS b WHERE b.book_id = item_recs.book_id;
		select amount INTO oi_amount FROM order_item AS oi WHERE oi.amount = item_recs.amount;

		total_oi_price = book_price * oi_amount;

        SELECT json_build_object('order_item_id', item_recs.order_item_id,
		'amount', item_recs.amount,
		'book', book_json,
		'order_id', item_recs.order_id,
		'total_order_item_price', trunc(total_oi_price::double precision::text::numeric, 2)) INTO single_order_item;
		total_price := total_price + total_oi_price;
		item_array = array_append(item_array, single_order_item);
	END LOOP;
    order_items = array_to_json(item_array);

    return (select json_build_object(
        'order_id', found_order.order_id,
        'total_price', trunc(total_price::double precision::text::numeric, 2),
        'order_date', found_order.order_date,
        'user', found_user,
        'order_items', order_items
    ));

end;
$$;


ALTER FUNCTION public.get_order_by_order_id(o_id bigint) OWNER TO postgres;

--
-- Name: get_order_by_order_id2(bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.get_order_by_order_id2(o_id bigint) RETURNS json
    LANGUAGE plpgsql
    AS $$
DECLARE
	total_oi_price double precision;
	book_price double precision;
	total_price double precision;
	oi_amount integer;
    order_items json;
    item_recs RECORD;
    book_json json;
    single_order_item json;
    found_order "vertx-jooq-cr".public.orders;
    found_user json;
    item_array json[];
BEGIN
    select * into found_order
    from "vertx-jooq-cr".public.orders
    where order_id = o_id;

    select json_build_object('user_id', "vertx-jooq-cr".public.users.user_id, 'username', "vertx-jooq-cr".public.users.username)
    into found_user
    from "vertx-jooq-cr".public.users
    INNER JOIN "vertx-jooq-cr".public.orders as o USING (user_id)
    WHERE o.order_id = o_id;

	total_price = 0.00;

    FOR item_recs IN SELECT *
		FROM public.order_item AS oi WHERE oi.order_id = o_id
	LOOP
        select public.get_book_by_book_id(item_recs.book_id) into book_json;

		select price INTO book_price FROM book AS b WHERE b.book_id = item_recs.book_id;
		oi_amount  = item_recs.amount;
		total_oi_price = book_price * oi_amount;

        SELECT json_build_object('order_item_id', item_recs.order_item_id,
		'amount', item_recs.amount,
		'book', book_json,
		'order_id', item_recs.order_id,
		'total_order_item_price', trunc(total_oi_price::double precision::text::numeric, 2)) INTO single_order_item;
		total_price := total_price + total_oi_price;
		item_array = array_append(item_array, single_order_item);
	END LOOP;
    order_items = array_to_json(item_array);

    return (select json_build_object(
        'order_id', found_order.order_id,
        'total_price', trunc(total_price::double precision::text::numeric, 2),
        'order_date', found_order.order_date,
        'user', found_user,
        'order_items', order_items
    ));

end;
$$;


ALTER FUNCTION public.get_order_by_order_id2(o_id bigint) OWNER TO postgres;

--
-- Name: get_order_by_order_id8(bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.get_order_by_order_id8(o_id bigint) RETURNS json
    LANGUAGE plpgsql
    AS $$
DECLARE
	total_oi_price double precision;
	book_price double precision;
	total_price double precision;
	oi_amount integer;
    order_items json;
    item_recs RECORD;
    book_json json;
    single_order_item json;
    found_order "vertx-jooq-cr".public.orders;
    found_user json;
    _item_id bigint;
    item_array json[];
BEGIN
    select * into found_order
    from "vertx-jooq-cr".public.orders
    where order_id = o_id;

    select json_build_object('user_id', "vertx-jooq-cr".public.users.user_id, 'username', "vertx-jooq-cr".public.users.username)
    into found_user
    from "vertx-jooq-cr".public.users
    INNER JOIN "vertx-jooq-cr".public.orders as o USING (user_id)
    WHERE o.order_id = o_id;

	total_price = 0.00;

    FOR item_recs IN SELECT *
		FROM public.order_item AS oi WHERE oi.order_id = o_id
	LOOP
        select public.get_book_by_book_id(item_recs.book_id) into book_json
	    from public.order_item
	    where public.order_item.order_item_id IN (item_recs.order_item_id);

		select price INTO book_price FROM book AS b WHERE b.book_id = item_recs.book_id;
		select amount INTO oi_amount FROM order_item AS oi WHERE oi.amount = item_recs.amount;
		
		total_oi_price = book_price * oi_amount;

        SELECT json_build_object('order_item_id', item_recs.order_item_id,
		'amount', item_recs.amount,
		'book', book_json,
		'order_id', item_recs.order_id,
		'total_order_item_price', trunc(total_oi_price::double precision::text::numeric, 2)) INTO single_order_item;
		total_price := total_price + total_oi_price;
		item_array = array_append(item_array, single_order_item);
	END LOOP;
    order_items = array_to_json(item_array);

    return (select json_build_object(
        'order_id', found_order.order_id,
        'total_price', trunc(total_price::double precision::text::numeric, 2),
        'order_date', found_order.order_date,
        'user', found_user,
        'order_items', order_items
    ));

end;
$$;


ALTER FUNCTION public.get_order_by_order_id8(o_id bigint) OWNER TO postgres;

--
-- Name: get_order_by_order_idak(bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.get_order_by_order_idak(o_id bigint) RETURNS json
    LANGUAGE plpgsql
    AS $$
DECLARE
    order_items json;
    single_order_item json;
    found_order RECORD;
    found_user json;
    _item_id bigint;
begin
    select * into found_order
    from "vertx-jooq-cr".public.orders
    where order_id = o_id;

    select json_build_object('user_id', "vertx-jooq-cr".public.users.user_id, 'username', "vertx-jooq-cr".public.users.username)
    into found_user
    from "vertx-jooq-cr".public.users
    INNER JOIN "vertx-jooq-cr".public.orders as o USING (user_id)
    WHERE o.order_id = o_id;

    SELECT json_agg(row_to_json(x)) INTO order_items
    FROM (SELECT * FROM "vertx-jooq-cr".public.order_item AS oi WHERE oi.order_id = o_id) AS x;

    return (select json_build_object(
        'order_id', found_order.order_id,
        'total_price', trunc(found_order.total::double precision::text::numeric, 2),
        'order_date', found_order.order_date,
        'user', found_user,
        'order_items', order_items
    ));
end
$$;


ALTER FUNCTION public.get_order_by_order_idak(o_id bigint) OWNER TO postgres;

--
-- Name: get_orderitem_by_oi_id(bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.get_orderitem_by_oi_id(oi_id bigint) RETURNS json
    LANGUAGE plpgsql
    AS $$
declare
	found_oi "vertx-jooq-cr".public.order_item;
	book_json json;
    total_oi_price decimal;
    book_price double precision;
begin
	select * into found_oi
	from public.order_item AS oi2
	where oi2.order_item_id = oi_id;

	select public.get_book_by_book_id(public.order_item.book_id::bigint) into book_json
	from public.order_item
	where public.order_item.order_item_id = oi_id;

	select price into book_price
	from book AS b
	inner join public.order_item AS oi USING (book_id);

	total_oi_price = found_oi.amount * book_price;

	return (select json_build_object(
		'order_item_id', found_oi.order_item_id,
		'amount', found_oi.amount,
		'book', book_json,
		'order_id', found_oi.order_id,
		'total_order_item_price', trunc(total_oi_price::double precision::text::numeric, 2)
	));
end
$$;


ALTER FUNCTION public.get_orderitem_by_oi_id(oi_id bigint) OWNER TO postgres;

--
-- Name: get_orderitem_by_oi_id2(bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.get_orderitem_by_oi_id2(oi_id bigint) RETURNS record
    LANGUAGE plpgsql
    AS $$
declare
	found_oi "vertx-jooq-cr".public.order_item;
	book_json json;
    total_oi_price decimal;
    book_price double precision;
begin
	select * into found_oi
	from public.order_item AS oi2
	where oi2.order_item_id = oi_id;

	select public.get_book_by_book_id(public.order_item.book_id::bigint) into book_json
	from public.order_item
	where public.order_item.order_item_id = oi_id;

	select price into book_price
	from book AS b
	inner join public.order_item AS oi USING (book_id);

	total_oi_price = found_oi.amount * book_price;

	return (select row (
	    found_oi.order_item_id,
	    found_oi.amount,
	    book_json,
	    found_oi.order_id,
	    trunc(total_oi_price::double precision::text::numeric, 2) ));
end
$$;


ALTER FUNCTION public.get_orderitem_by_oi_id2(oi_id bigint) OWNER TO postgres;

--
-- Name: iterate_over_orderitem_ids(bigint); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.iterate_over_orderitem_ids(o_id bigint) RETURNS json
    LANGUAGE plpgsql
    AS $$
DECLARE
    order_items json[];
    found_order "vertx-jooq-cr".public.orders;
    _item_id bigint;
    _oitems_ids bigint[];
	item_recs RECORD;
    order_items_arr json;

begin
    select * into found_order
    from "vertx-jooq-cr".public.orders
    where order_id = o_id;

    FOR _item_id IN SELECT DISTINCT oi.order_item_id
		FROM public.order_item AS oi WHERE oi.order_id = o_id
	LOOP
		order_items := order_items || json_build_object('order_item_id', _item_id);
	END LOOP;

    return (select json_build_object(
        'order_items', order_items
    ));
end
$$;


ALTER FUNCTION public.iterate_over_orderitem_ids(o_id bigint) OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: author; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.author (
    author_id bigint NOT NULL,
    first_name character varying(30),
    last_name character varying(30)
);


ALTER TABLE public.author OWNER TO postgres;

--
-- Name: author_author_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.author_author_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.author_author_id_seq OWNER TO postgres;

--
-- Name: author_author_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.author_author_id_seq OWNED BY public.author.author_id;


--
-- Name: author_book; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.author_book (
    author_id bigint NOT NULL,
    book_id bigint NOT NULL
);


ALTER TABLE public.author_book OWNER TO postgres;

--
-- Name: book; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.book (
    book_id bigint NOT NULL,
    title character varying(255),
    price double precision,
    amount integer,
    is_deleted boolean
);


ALTER TABLE public.book OWNER TO postgres;

--
-- Name: book_book_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.book_book_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.book_book_id_seq OWNER TO postgres;

--
-- Name: book_book_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.book_book_id_seq OWNED BY public.book.book_id;


--
-- Name: category; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.category (
    category_id bigint NOT NULL,
    name character varying(30),
    is_deleted boolean
);


ALTER TABLE public.category OWNER TO postgres;

--
-- Name: category_book; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.category_book (
    category_id bigint NOT NULL,
    book_id bigint NOT NULL
);


ALTER TABLE public.category_book OWNER TO postgres;

--
-- Name: category_category_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.category_category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.category_category_id_seq OWNER TO postgres;

--
-- Name: category_category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.category_category_id_seq OWNED BY public.category.category_id;


--
-- Name: order_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.order_item (
    order_item_id bigint NOT NULL,
    amount integer,
    book_id bigint,
    order_id bigint
);


ALTER TABLE public.order_item OWNER TO postgres;

--
-- Name: order_item_order_item_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.order_item_order_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.order_item_order_item_id_seq OWNER TO postgres;

--
-- Name: order_item_order_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.order_item_order_item_id_seq OWNED BY public.order_item.order_item_id;


--
-- Name: orders; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.orders (
    order_id bigint NOT NULL,
    total double precision,
    order_date timestamp without time zone,
    user_id bigint
);


ALTER TABLE public.orders OWNER TO postgres;

--
-- Name: orders_order_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.orders_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.orders_order_id_seq OWNER TO postgres;

--
-- Name: orders_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.orders_order_id_seq OWNED BY public.orders.order_id;


--
-- Name: role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.role (
    role_id bigint NOT NULL,
    name character varying(30)
);


ALTER TABLE public.role OWNER TO postgres;

--
-- Name: role_role_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.role_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.role_role_id_seq OWNER TO postgres;

--
-- Name: role_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.role_role_id_seq OWNED BY public.role.role_id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    user_id bigint NOT NULL,
    first_name character varying(30),
    last_name character varying(30),
    email character varying(30),
    username character varying(15),
    password character varying(255),
    role_id bigint
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_user_id_seq OWNER TO postgres;

--
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- Name: author author_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.author ALTER COLUMN author_id SET DEFAULT nextval('public.author_author_id_seq'::regclass);


--
-- Name: book book_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.book ALTER COLUMN book_id SET DEFAULT nextval('public.book_book_id_seq'::regclass);


--
-- Name: category category_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category ALTER COLUMN category_id SET DEFAULT nextval('public.category_category_id_seq'::regclass);


--
-- Name: order_item order_item_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_item ALTER COLUMN order_item_id SET DEFAULT nextval('public.order_item_order_item_id_seq'::regclass);


--
-- Name: orders order_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders ALTER COLUMN order_id SET DEFAULT nextval('public.orders_order_id_seq'::regclass);


--
-- Name: role role_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role ALTER COLUMN role_id SET DEFAULT nextval('public.role_role_id_seq'::regclass);


--
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- Data for Name: author; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.author (author_id, first_name, last_name) FROM stdin;
\.
COPY public.author (author_id, first_name, last_name) FROM '$$PATH$$/3984.dat';

--
-- Data for Name: author_book; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.author_book (author_id, book_id) FROM stdin;
\.
COPY public.author_book (author_id, book_id) FROM '$$PATH$$/3995.dat';

--
-- Data for Name: book; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.book (book_id, title, price, amount, is_deleted) FROM stdin;
\.
COPY public.book (book_id, title, price, amount, is_deleted) FROM '$$PATH$$/3986.dat';

--
-- Data for Name: category; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.category (category_id, name, is_deleted) FROM stdin;
\.
COPY public.category (category_id, name, is_deleted) FROM '$$PATH$$/3982.dat';

--
-- Data for Name: category_book; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.category_book (category_id, book_id) FROM stdin;
\.
COPY public.category_book (category_id, book_id) FROM '$$PATH$$/3996.dat';

--
-- Data for Name: order_item; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.order_item (order_item_id, amount, book_id, order_id) FROM stdin;
\.
COPY public.order_item (order_item_id, amount, book_id, order_id) FROM '$$PATH$$/3994.dat';

--
-- Data for Name: orders; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.orders (order_id, total, order_date, user_id) FROM stdin;
\.
COPY public.orders (order_id, total, order_date, user_id) FROM '$$PATH$$/3992.dat';

--
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.role (role_id, name) FROM stdin;
\.
COPY public.role (role_id, name) FROM '$$PATH$$/3988.dat';

--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (user_id, first_name, last_name, email, username, password, role_id) FROM stdin;
\.
COPY public.users (user_id, first_name, last_name, email, username, password, role_id) FROM '$$PATH$$/3990.dat';

--
-- Name: author_author_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.author_author_id_seq', 7, true);


--
-- Name: book_book_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.book_book_id_seq', 386, true);


--
-- Name: category_category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.category_category_id_seq', 42, true);


--
-- Name: order_item_order_item_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.order_item_order_item_id_seq', 2042, true);


--
-- Name: orders_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.orders_order_id_seq', 1069, true);


--
-- Name: role_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.role_role_id_seq', 1, false);


--
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_user_id_seq', 1, false);


--
-- Name: author_book author_book_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.author_book
    ADD CONSTRAINT author_book_pkey PRIMARY KEY (author_id, book_id);


--
-- Name: author author_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.author
    ADD CONSTRAINT author_pkey PRIMARY KEY (author_id);


--
-- Name: book book_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.book
    ADD CONSTRAINT book_pkey PRIMARY KEY (book_id);


--
-- Name: category_book category_book_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category_book
    ADD CONSTRAINT category_book_pkey PRIMARY KEY (category_id, book_id);


--
-- Name: category category_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_pkey PRIMARY KEY (category_id);


--
-- Name: order_item order_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_item
    ADD CONSTRAINT order_item_pkey PRIMARY KEY (order_item_id);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (order_id);


--
-- Name: role role_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (role_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: author_book author_book_author_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.author_book
    ADD CONSTRAINT author_book_author_id_fkey FOREIGN KEY (author_id) REFERENCES public.author(author_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: author_book author_book_book_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.author_book
    ADD CONSTRAINT author_book_book_id_fkey FOREIGN KEY (book_id) REFERENCES public.book(book_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: category_book category_book_book_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category_book
    ADD CONSTRAINT category_book_book_id_fkey FOREIGN KEY (book_id) REFERENCES public.book(book_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: category_book category_book_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.category_book
    ADD CONSTRAINT category_book_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.category(category_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: order_item order_item_book_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_item
    ADD CONSTRAINT order_item_book_id_fkey FOREIGN KEY (book_id) REFERENCES public.book(book_id);


--
-- Name: order_item order_item_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.order_item
    ADD CONSTRAINT order_item_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(order_id);


--
-- Name: orders orders_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: users users_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.role(role_id);


--
-- PostgreSQL database dump complete
--

