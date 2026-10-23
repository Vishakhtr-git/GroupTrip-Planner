package tripmate;

// import sql classes
import java.sql.*;

public class ConTest {

    public static void main(String[] args) {
        // test the database connection
        try {
            // call getConnection method from DBConnection
            Connection con = DBConnection.getConnection();

            // check if connection object is not null
            if (con != null) {
                System.out.println("connection established");

                // close the database connection
                con.close();
            } else {
                System.out.println("connection failed");
            }
        } catch (Exception ex) {
            // display error message
            System.out.println("Error: " + ex.getMessage());
        }
    }
}
