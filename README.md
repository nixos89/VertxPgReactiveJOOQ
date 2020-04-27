# VertxPgReactiveJOOQ
RESTful web application built in Vertx using [vertx-pg-client](https://github.com/eclipse-vertx/vertx-sql-client/tree/master/vertx-pg-client) and [Reactive Vertx-ifed JOOQ library](https://github.com/jklingsporn/vertx-jooq/tree/master/vertx-jooq-classic-reactive)


## How to build and run this app
To build this app run following commands (in directory where this repo has been downloaded/cloned) in your terminal:

```cd vertx-pg-reactive-jooq```

```mvn clean install```

To run this app (on Linux) simply type:

```sh ./redeploy.sh```


REST Endpoint for CRUD on Book:<br/>
[Get ALL/Save Books](http://localhost:8080/api/books)<br/>
[Get/Update/Delete Book where book_id = 1](http://localhost:8080/api/books/1)<br/>


JSON Body for Saving/Updating:

```
{
	"title": "C# 7.0 Pocket Reference",
	"price": 11.14,
	"amount": 333,
	"is_deleted": false,
	"author_ids": [14,15],
	"category_ids": [17]
}
```
