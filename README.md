# VertxPgReactiveJOOQ
RESTful web application built in Vertx using [vertx-pg-client](https://github.com/eclipse-vertx/vertx-sql-client/tree/master/vertx-pg-client) and [Reactive Vertx-ifed JOOQ library](https://github.com/jklingsporn/vertx-jooq/tree/master/vertx-jooq-classic-reactive)


## How to launch app
1) First in order to connect to existing database you must restore database using `vertx-jooq-cr-backup.tar` (located in root directory of this repo) in your DB tool (such as pgAdmin4)
2) Make sure that all parameters for database access (database name, db username, password, etc.) located in *pom.xml* file are properly set
3) Change location in your terminal to be inside of "vertx-pg-reactive-jooq" project
4) Run `mvn clean package` command
5) Run `sh ./redeploy.sh` (on Linux) or `java -jar ./target/vertx-pg-reactive-jooq-0.0.1-SNAPSHOT-fat.jar`


## REST API endpoints
Here are some API endpoints to target (using Postman or similar tool) including some JSON bodies for HTTP Request:

### GET methods
Get single Author with id=1 [http://localhost:8080/api/authors/1](http://localhost:8080/api/authors/1)<br/>
Get all Categories [http://localhost:8080/api/categories](http://localhost:8080/api/categories)<br/>
Get single Book[http://localhost:8080/api/books/1](http://localhost:8080/api/books/1)<br/>

## POST methods
Create Author [http://localhost:8080/api/authors](http://localhost:8080/api/authors)

```
{
	"first_name": "Franz",
	"last_name": "Kafka"
}
```

<br/>Create Category [http://localhost:8080/api/categories](http://localhost:8080/api/categories)
```
{
	"name" : "Crime",
	"is_deleted": false
}
```

<br/>Create Book [http://localhost:8080/api/books](http://localhost:8080/api/books)
```
{
	"title": "The Trial",
	"price": 15.50,
	"amount": 300,
	"is_deleted": false,
	"author_ids": [1],
	"category_ids": [1]
}	
```

Create Order [http://localhost:8080/api/orders?username=mica](http://localhost:8080/api/orders?username=mica)
**NOTE**: In order this method to work `user` table must contain a row with column `username` that has a value 'mica'.<br/>
JSON Body:
```
{
    "orders": [
        {
            "book_id": 1,
            "amount": 4
        }
    ],
    "total_price": 62.00
}
```

### PUT and DELETE methods
Endpoints are same like in GET method section and you need to change **same** JSON body yourself by following format from POST methods section (JSON bodies are of course for PUT methods)



