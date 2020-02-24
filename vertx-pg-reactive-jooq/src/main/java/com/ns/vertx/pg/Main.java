package com.ns.vertx.pg;

import java.sql.*;

import org.jooq.*;
import org.jooq.impl.DSL;

import com.ns.vertx.pg.jooq.tables.Author;


public class Main {
	

	public static void main(String[] args) {	
		String userName = "postgres";
		String password = "postgres";
		String url = "jdbc:postgresql://localhost:5432/vertx-jooq-cr";
		
		Connection connection = null;
		try {
			Class.forName("org.postgresql.Driver");
			connection  = DriverManager.getConnection(url, userName, password);
			System.out.println("Connection succeded!");
/*	
  			// the old-fashioned JDBC querying approach 
			Statement statement = connection.createStatement();
			boolean executedInsertAuthorStartement = statement.execute("INSERT INTO Author(first_name, last_name) VALUES('Miroljub', 'Petrovic')");
			System.out.println("executedInsertAuthorStartement = " + executedInsertAuthorStartement);
			ResultSet rs = statement.executeQuery("SELECT * FROM Author");

			while (rs.next()) {
				System.out.println("author_id = " + rs.getLong("author_id") + ", first_name = " + rs.getString("first_name") + ", last_name = " + rs.getString("last_name"));
			}
*/		
			DSLContext dslContext = DSL.using(connection, SQLDialect.POSTGRES);
			Result<Record> result = dslContext.select().from(Author.AUTHOR).fetch();
			System.out.println("\n ========== About to print fetched results: ========");
			result.stream().forEach(rs -> {
				System.out.println("author_id = " + rs.getValue(Author.AUTHOR.AUTHOR_ID) + ", first_name = " 
						+ rs.getValue(Author.AUTHOR.FIRST_NAME) + ", last_name = " + rs.getValue(Author.AUTHOR.LAST_NAME));
			});
			
			// example of static methods in interfaces
			String name = "Nikola";
			System.out.println("Name '" + name + "' is " + IPerson.personsFirstNameLength(name) + " characters long.");
						
			
//			
			
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
