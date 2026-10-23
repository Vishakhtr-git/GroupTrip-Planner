package tripmate;

// import sql classes for database connection
import java.sql.*;

public class DBConnection {

    // static method to get connection to MySQL database
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("MySQL JDBC driver is missing from the project classpath.", ex);
        }

        String password = System.getenv("TRIPMATE_DB_PASSWORD");
        if (password == null || password.isEmpty()) {
            throw new SQLException("Set the TRIPMATE_DB_PASSWORD environment variable before connecting.");
        }

        return DriverManager.getConnection("jdbc:mysql://localhost:3303/tripmate_db", "root", password);
    }
}
