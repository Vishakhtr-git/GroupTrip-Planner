package tripmate;

// import required swing, awt and sql packages
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;

public class Dashboard implements ActionListener {

    // declare user and trip id variables
    int userId;
    int tripId;
    String username = "";

    // declare swing components at the top
    JFrame f;
    JLabel l1, l2;
    JButton b_overview, b_budget, b_pref, b_itin, b_pack, b_mytrips, b_newtrip, b_saveall, b_completed, b_logout;

    // constructor to build the dashboard window
    public Dashboard(int userId, int tripId) {
        this.userId = userId;
        this.tripId = tripId;

        // load logged-in username from users table
        try {
            // Step 1: get the connection from DBConnection
            Connection con = DBConnection.getConnection();

            // Step 2: create the statement
            PreparedStatement pst = con.prepareStatement("SELECT username FROM users WHERE id = ?");
            pst.setInt(1, userId);

            // Step 3: execute the query
            ResultSet rs = pst.executeQuery();

            // Step 4: read the result
            if (rs.next()) {
                username = rs.getString("username");
            }

            // Step 5: close the connection
            con.close();
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }

        // create the main window
        f = new JFrame("TripMate - Dashboard");

        // create panel with 6 rows and 2 columns
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(6, 2));

        // create welcome labels
        l1 = new JLabel("Welcome:");
        l2 = new JLabel(username + " (Trip: " + (tripId == 0 ? "None" : tripId) + ")");

        // create navigation buttons
        b_overview = new JButton("Trip Overview");
        b_budget = new JButton("Budget and Expense Split");

        b_pref = new JButton("Travel Preferences");
        b_itin = new JButton("Itinerary");

        b_pack = new JButton("Packing Checklist");
        b_mytrips = new JButton("My Trips");

        b_newtrip = new JButton("New Trip");
        b_saveall = new JButton("Save All");

        b_completed = new JButton("Completed");
        b_logout = new JButton("Logout");

        // add components to the panel
        p.add(l1);
        p.add(l2);
        p.add(b_overview);
        p.add(b_budget);
        p.add(b_pref);
        p.add(b_itin);
        p.add(b_pack);
        p.add(b_mytrips);
        p.add(b_newtrip);
        p.add(b_saveall);
        p.add(b_completed);
        p.add(b_logout);

        // register action listeners for buttons
        b_overview.addActionListener(this);
        b_budget.addActionListener(this);
        b_pref.addActionListener(this);
        b_itin.addActionListener(this);
        b_pack.addActionListener(this);
        b_mytrips.addActionListener(this);
        b_newtrip.addActionListener(this);
        b_saveall.addActionListener(this);
        b_completed.addActionListener(this);
        b_logout.addActionListener(this);

        // add panel to frame and configure frame
        f.add(p);
        f.setSize(450, 400);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // handle button click events
    public void actionPerformed(ActionEvent e) {
        // if Trip Overview button is clicked
        if (e.getSource() == b_overview) {
            f.dispose();
            new TripOverviewPage(userId, tripId);
        }
        // if Budget and Expense Split button is clicked
        else if (e.getSource() == b_budget) {
            if (tripId == 0) {
                JOptionPane.showMessageDialog(f, "Save trip overview first");
                return;
            }
            f.dispose();
            new BudgetSplitPage(userId, tripId);
        }
        // if Travel Preferences button is clicked
        else if (e.getSource() == b_pref) {
            if (tripId == 0) {
                JOptionPane.showMessageDialog(f, "Save trip overview first");
                return;
            }
            f.dispose();
            new TravelPreferencesPage(userId, tripId);
        }
        // if Itinerary button is clicked
        else if (e.getSource() == b_itin) {
            if (tripId == 0) {
                JOptionPane.showMessageDialog(f, "Save trip overview first");
                return;
            }
            f.dispose();
            new ItineraryPage(userId, tripId);
        }
        // if Packing Checklist button is clicked
        else if (e.getSource() == b_pack) {
            if (tripId == 0) {
                JOptionPane.showMessageDialog(f, "Save trip overview first");
                return;
            }
            f.dispose();
            new PackingChecklistPage(userId, tripId);
        }
        // if My Trips button is clicked
        else if (e.getSource() == b_mytrips) {
            f.dispose();
            new MyTripsPage(userId, tripId);
        }
        // if New Trip button is clicked
        else if (e.getSource() == b_newtrip) {
            tripId = 0;
            l2.setText(username + " (Trip: None)");
            JOptionPane.showMessageDialog(f, "Starting a new trip! Opening Trip Overview...");
            f.dispose();
            new TripOverviewPage(userId, 0);
        }
        // if Save All button is clicked
        else if (e.getSource() == b_saveall) {
            if (tripId == 0) {
                JOptionPane.showMessageDialog(f, "Save trip overview first");
                return;
            }
            try {
                // Step 1: get the connection from DBConnection
                Connection con = DBConnection.getConnection();

                // Step 2: create the statement
                PreparedStatement pst = con.prepareStatement("UPDATE trips SET status = 'Saved' WHERE trip_id = ?");
                pst.setInt(1, tripId);

                // Step 3: execute the query
                int rows = pst.executeUpdate();

                // Step 4: read the result
                if (rows > 0) {
                    JOptionPane.showMessageDialog(f, "Trip saved successfully! Opening My Trips...");
                    f.dispose();
                    new MyTripsPage(userId, tripId);
                } else {
                    JOptionPane.showMessageDialog(f, "Unable to save trip");
                }

                // Step 5: close the connection
                con.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
            }
        }
        // if Completed button is clicked
        else if (e.getSource() == b_completed) {
            if (tripId == 0) {
                JOptionPane.showMessageDialog(f, "Save trip overview first");
                return;
            }
            f.dispose();
            new NotePage(userId, tripId);
        }
        // if Logout button is clicked
        else if (e.getSource() == b_logout) {
            f.dispose();
            new LoginPage();
        }
    }
}
