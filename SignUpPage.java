package tripmate;

// import required swing, awt and sql packages
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
import java.util.concurrent.ExecutionException;

public class SignUpPage implements ActionListener {

    // declare components at the top
    JFrame f;
    JLabel l1, l2, l3, l4, result, existingAccount;
    JTextField t1;
    JPasswordField t2, t3;
    JButton create, back, login;

    // constructor to build the sign up window
    public SignUpPage() {
        // create the main window
        f = new JFrame("TripMate - Sign Up");

        // create the panel with 6 rows and 2 columns
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(6, 2));

        // create username label and text field
        l1 = new JLabel("Username:");
        t1 = new JTextField();

        // create password label and password field
        l2 = new JLabel("Password:");
        t2 = new JPasswordField();

        // create confirm password label and password field
        l3 = new JLabel("Confirm Password:");
        t3 = new JPasswordField();

        // create buttons for create account and back
        create = new JButton("Create Account");
        back = new JButton("Back");
        existingAccount = new JLabel("Already have an account?");
        login = new JButton("Login");

        // create status and result labels
        l4 = new JLabel("Status:");
        result = new JLabel("");

        // add components to the panel
        p.add(l1);
        p.add(t1);
        p.add(l2);
        p.add(t2);
        p.add(l3);
        p.add(t3);
        p.add(create);
        p.add(back);
        p.add(existingAccount);
        p.add(login);
        p.add(l4);
        p.add(result);

        // register action listeners for buttons
        create.addActionListener(this);
        back.addActionListener(this);
        login.addActionListener(this);

        // add panel to frame and configure frame
        f.add(p);
        f.setSize(400, 330);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // handle button click events
    public void actionPerformed(ActionEvent e) {
        // if create account button is clicked
        if (e.getSource() == create) {
            String u = t1.getText().trim();
            String p = new String(t2.getPassword());
            String cp = new String(t3.getPassword());

            // check if any field is empty
            if (u.isEmpty() || p.isEmpty() || cp.isEmpty()) {
                JOptionPane.showMessageDialog(f, "Please fill all fields");
                return;
            }

            // check if password and confirm password match
            if (!p.equals(cp)) {
                JOptionPane.showMessageDialog(f, "Passwords do not match");
                return;
            }

            create.setEnabled(false);
            result.setText("Creating account...");

            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                protected Boolean doInBackground() throws Exception {
                    Connection con = DBConnection.getConnection();
                    if (con == null) {
                        throw new SQLException("Database connection failed. Check that MySQL is running and the connection settings are correct.");
                    }

                    try (Connection connection = con;
                         PreparedStatement checkPst = connection.prepareStatement("SELECT 1 FROM users WHERE username = ?")) {
                        checkPst.setString(1, u);

                        try (ResultSet rs = checkPst.executeQuery()) {
                            if (rs.next()) {
                                return false;
                            }
                        }

                        try (PreparedStatement pst = connection.prepareStatement(
                                "INSERT INTO users (username, password) VALUES (?, ?)")) {
                            pst.setString(1, u);
                            pst.setString(2, p);
                            if (pst.executeUpdate() == 0) {
                                throw new SQLException("The account could not be created.");
                            }
                        }
                        return true;
                    }
                }

                protected void done() {
                    create.setEnabled(true);
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(f, "Account created");
                            f.dispose();
                            new LoginPage();
                        } else {
                            result.setText("Username already exists");
                            JOptionPane.showMessageDialog(f, "Username already exists");
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        result.setText("Account creation was interrupted");
                        JOptionPane.showMessageDialog(f, "Account creation was interrupted. Please try again.");
                    } catch (ExecutionException ex) {
                        Throwable cause = ex.getCause();
                        String message = cause == null ? ex.getMessage() : cause.getMessage();
                        result.setText("Account creation failed");
                        JOptionPane.showMessageDialog(f, "Could not create account: " + message);
                    }
                }
            };
            worker.execute();
        }
        // if back button is clicked
        else if (e.getSource() == back) {
            // close signup window and return to login page
            f.dispose();
            new LoginPage();
        }
        // if Login button is clicked
        else if (e.getSource() == login) {
            f.dispose();
            new LoginPage();
        }
    }

    // main method to run sign up page
    public static void main(String[] args) {
        new SignUpPage();
    }
}
