package tripmate;

// import required swing, awt and sql packages
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;

public class LoginPage implements ActionListener {

    // declare components at the top
    JFrame f;
    JLabel l1, l2, l3, l4, result;
    JTextField t1;
    JPasswordField t2;
    JButton login, reset, signup;

    // constructor to build the login window
    public LoginPage() {
        // create the main window
        f = new JFrame("TripMate - Login");

        // create the panel with 5 rows and 2 columns
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(5, 2));

        // create username label and text field
        l1 = new JLabel("Username:");
        t1 = new JTextField();

        // create password label and password field
        l2 = new JLabel("Password:");
        t2 = new JPasswordField();

        // create login and reset buttons
        login = new JButton("Login");
        reset = new JButton("Reset");

        // create sign up button and an empty label beside it
        signup = new JButton("Sign Up");
        l3 = new JLabel("");

        // create status label and result label
        l4 = new JLabel("Status:");
        result = new JLabel("");

        // add components to the panel
        p.add(l1);
        p.add(t1);
        p.add(l2);
        p.add(t2);
        p.add(login);
        p.add(reset);
        p.add(signup);
        p.add(l3);
        p.add(l4);
        p.add(result);

        // register action listeners for buttons
        login.addActionListener(this);
        reset.addActionListener(this);
        signup.addActionListener(this);

        // add panel to frame and configure frame
        f.add(p);
        f.setSize(400, 300);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // handle button click events
    public void actionPerformed(ActionEvent e) {
        // if login button is clicked
        if (e.getSource() == login) {
            String u = t1.getText();
            String p = new String(t2.getPassword());

            // check if any field is empty
            if (u.equals("") || p.equals("")) {
                JOptionPane.showMessageDialog(f, "Please fill all fields");
                return;
            }

            try {
                // Step 1: get the connection from DBConnection
                Connection con = DBConnection.getConnection();

                // Step 2: create the statement
                PreparedStatement pst = con.prepareStatement("SELECT id AS user_id, username FROM users WHERE username = ? AND password = ?");
                pst.setString(1, u);
                pst.setString(2, p);

                // Step 3: execute the query
                ResultSet rs = pst.executeQuery();

                // Step 4: read the result
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    result.setText("Welcome " + u);
                    JOptionPane.showMessageDialog(f, "Welcome " + u);

                    // close login window and open dashboard
                    f.dispose();
                    new Dashboard(userId, 0);
                } else {
                    result.setText("Invalid User");
                }

                // Step 5: close the connection
                con.close();
            } catch (Exception ex) {
                // show error message on database failure
                JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
            }
        }
        // if reset button is clicked
        else if (e.getSource() == reset) {
            // clear all fields and result label
            t1.setText("");
            t2.setText("");
            result.setText("");
        }
        // if sign up button is clicked
        else if (e.getSource() == signup) {
            // close login window and open signup screen
            f.dispose();
            new SignUpPage();
        }
    }

    // main method to run login page
    public static void main(String[] args) {
        new LoginPage();
    }
}
