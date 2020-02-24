package com.ns.vertx.pg;

import java.sql.*;


import org.jooq.*;
import org.jooq.impl.DSL;

import com.ns.vertx.pg.jooq.tables.Author;

public class Main2 {

	public static void main(String[] args) {
		
		String userName = "postgres";
		String password = "postgres";
		String url = "jdbc:postgresql://localhost:5432/vertx-jooq-cr";
		
		Connection connection = null;
		try {
			Class.forName("org.postgresql.Driver");
			connection  = DriverManager.getConnection(url, userName, password);

			DSLContext dslContext = DSL.using(connection, SQLDialect.POSTGRES);
			Result<Record> result = dslContext.select().from(Author.AUTHOR).fetch();
			
//			List<Integer> ids = dslContext.create? 
			
			
			connection.close();
			if (connection.isClosed()) {
				System.out.println("Connection has been closed!");
			}			
		} catch(Exception e) {
			System.out.println("Error! Connection NOT established!");
			e.printStackTrace();
		}

	}

}
