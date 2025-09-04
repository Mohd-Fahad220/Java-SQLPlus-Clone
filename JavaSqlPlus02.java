package cloneSql;

import java.util.Properties;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Scanner;


public class JavaSqlPlus02 {
	
	    public static void main(String[] args) {
	        System.out.println("************ Welcome to Java SQLPlus **********");

	        Scanner scn = new Scanner(System.in);
	        Connection con = null;
	        Statement stmt = null;

	        try {
	            // Redirect standard error to error.log file
	            System.setErr(new PrintStream(new FileOutputStream("error.log")));

	            // Load JDBC driver and DB connection info from properties file
	            Properties props = new Properties();
	            props.load(new FileReader("driverinfo.properties"));

	            final String DRIVER = props.getProperty("driver");
	            final String DB_URL = props.getProperty("db_url");

	            // Load the JDBC driver class
	            Class.forName(DRIVER);

	            String DB_USN = "";
	            String DB_PWD = "";

	            // ==== User Credential Input Loop ====
	            while (true) {
	                System.out.print("\nEnter username/password: ");
	                String input = scn.nextLine();
	                String[] usnpwd = input.split("/");

	                if (usnpwd.length == 1) {
	                    DB_USN = usnpwd[0];
	                    System.out.print("Enter password: ");
	                    DB_PWD = scn.next(); scn.nextLine();
	                } else if (usnpwd.length == 2) {
	                    DB_USN = usnpwd[0];
	                    DB_PWD = usnpwd[1];
	                } else {
	                    System.out.println("❌ Invalid input format. Use: username/password");
	                    continue;
	                }

	                // Try to establish DB connection
	                try {
	                    con = DriverManager.getConnection(DB_URL, DB_USN, DB_PWD);
	                    System.out.println("\n✅ Connected successfully to Oracle Database!");
	                    con.setAutoCommit(false);  // Manual commit for DML
	                } catch (SQLException e) {
	                    System.out.println("\n❌ Connection failed: " + e.getMessage());
	                    continue;
	                }

	                break;  // Exit loop if connected
	            } // === End of credential input loop ===

	            // Create Statement object
	            stmt = con.createStatement();

	            System.out.println("\n**** Type any SQL Query ending with ';' and press Enter to run. Type 'exit;' to quit.");

	            // ==== SQL Query Execution Loop ====
	            while (true) {
	                try {
	                    System.out.print("\nJava SQLPlus> ");
	                    int line = 2;
	                    String query = scn.nextLine();

	                    // Exit command check
	                    if (query.trim().toLowerCase().contains("exit;")) {
	                        System.out.println("\n👋 Thank You, Visit Again!");
	                        break;
	                    }

	                    // Multi-line query input support
	                    while (!query.trim().endsWith(";")) {
	                        System.out.print(" " + line++ + " ");
	                        query += "\n" + scn.nextLine();
	                    }

	                    // Remove trailing semicolon
	                    query = query.substring(0, query.length() - 1);

	                    // Execute query (generic execute for both DML/DQL)
	                    boolean resultSet = stmt.execute(query);

	                    // ==== ResultSet Handling ====
	                    if (resultSet) {
	                        try (ResultSet rs = stmt.getResultSet()) {
	                            ResultSetMetaData rsmd = rs.getMetaData();
	                            int columnCount = rsmd.getColumnCount();

	                            // -- Print Column Headers --
	                            for (int i = 1; i <= columnCount; i++) {
	                                int width = rsmd.getColumnDisplaySize(i);
	                                System.out.printf("%-" + width + "s ", rsmd.getColumnName(i));
	                            }
	                            System.out.println();

	                            // -- Print Underlines --
	                            for (int i = 1; i <= columnCount; i++) {
	                                int width = rsmd.getColumnDisplaySize(i);
	                                System.out.print("-".repeat(width) + " ");
	                            }
	                            System.out.println();

	                            // -- Print Row Data --
	                            int count = 0;
	                            while (rs.next()) {
	                                for (int i = 1; i <= columnCount; i++) {
	                                    int width = rsmd.getColumnDisplaySize(i);
	                                    String format = "%-" + width + "s ";
	                                    String data = rs.getString(i);
	                                    System.out.printf(format, data != null ? data : "NULL");
	                                }
	                                System.out.println();
	                                count++;
	                            }

	                            System.out.println("\n" + count + " rows selected");
	                        } // -- End of ResultSet try-with-resources
	                    }
	                    // ==== DML / DDL / TCL Handling ====
	                    else {
	                        int updatedCount = stmt.getUpdateCount();
	                        query = query.toUpperCase();

	                        if (query.contains("INSERT")) {
	                            System.out.println("✅ 1 row inserted");
	                        } else if (query.contains("UPDATE")) {
	                            System.out.println("✅ " + updatedCount + " row(s) updated");
	                        } else if (query.contains("DELETE")) {
	                            System.out.println("✅ " + updatedCount + " row(s) deleted");
	                        } else if (query.contains("COMMIT")) {
	                            System.out.println("✅ Commit complete.");
	                        } else if (query.contains("ROLLBACK")) {
	                            System.out.println("⏪ Rollback complete.");
	                        } else if (query.contains("CREATE TABLE")) {
	                            System.out.println("✅ Table created.");
	                        } else if (query.contains("ALTER TABLE")) {
	                            System.out.println("✅ Table altered.");
	                        } else if (query.contains("DROP TABLE")) {
	                            System.out.println("🗑️ Table dropped.");
	                        } else {
	                            System.out.println("✅ Query executed. " + updatedCount + " row(s) affected.");
	                        }
	                    }
	                    // ==== End of SQL Execution Block ====
	                } catch (SQLException e) {
	                    // Minimal message on screen
	                    System.out.println("SQL Error: " + e.getMessage());
	                    // Full error written to error.log
	                    e.printStackTrace();
	                }
	            } // === End of SQL Query Execution Loop ===

	        } catch (ClassNotFoundException e) {
	            System.out.println("❌ JDBC Driver class not found.");
	        } catch (IOException | SQLException e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                if (stmt != null) stmt.close();
	            } catch (SQLException e) { /* ignore */ }

	            try {
	                if (con != null) con.close();
	            } catch (SQLException e) { /* ignore */ }
	            try {
	                if (scn != null) scn.close();
	            } catch (Exception e) { } // == closing scn 
	        } // === End of main try-catch-finally ===
	    } // === End of main method ===
	} // === End of Sql_new1 class ===





