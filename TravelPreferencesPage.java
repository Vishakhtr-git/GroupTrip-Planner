package tripmate;

// import required swing, awt and sql packages
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;

public class TravelPreferencesPage implements ActionListener {

    // declare user and trip id variables
    int userId;
    int tripId;

    // declare swing components at the top
    JFrame f;
    JLabel l1, l2, l3, l4, status;
    JTextField t_stay, t_food, t_act, t_route;
    JButton b_stay, b_food, b_act, b_route, b_edit, b_save, b_next, b_back;

    // constructor to build travel preferences window
    public TravelPreferencesPage(int userId, int tripId) {
        this.userId = userId;
        this.tripId = tripId;

        // create the main window
        f = new JFrame("TripMate - Travel Preferences");

        // create panel with 8 rows and 2 columns
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(8, 2));

        // create summary labels and read-only text fields
        l1 = new JLabel("Selected Stay:");
        t_stay = new JTextField();
        t_stay.setEditable(false);

        l2 = new JLabel("Selected Foods:");
        t_food = new JTextField();
        t_food.setEditable(false);

        l3 = new JLabel("Selected Activities:");
        t_act = new JTextField();
        t_act.setEditable(false);

        l4 = new JLabel("Selected Route:");
        t_route = new JTextField();
        t_route.setEditable(false);

        // create buttons to open preference sub-pages
        b_stay = new JButton("Select Stay");
        b_food = new JButton("Select Food");

        b_act = new JButton("Select Activities");
        b_route = new JButton("Configure Route");

        // create edit, save and back buttons
        b_edit = new JButton("Edit");
        b_save = new JButton("Save & Continue");

        b_back = new JButton("Back");
        b_next = new JButton("Next: Itinerary");

        // add components to the panel
        p.add(l1);
        p.add(t_stay);
        p.add(l2);
        p.add(t_food);
        p.add(l3);
        p.add(t_act);
        p.add(l4);
        p.add(t_route);
        p.add(b_stay);
        p.add(b_food);
        p.add(b_act);
        p.add(b_route);
        p.add(b_edit);
        p.add(b_save);
        p.add(b_back);
        p.add(b_next);

        // register action listeners for buttons
        b_stay.addActionListener(this);
        b_food.addActionListener(this);
        b_act.addActionListener(this);
        b_route.addActionListener(this);
        b_edit.addActionListener(this);
        b_save.addActionListener(this);
        b_next.addActionListener(this);
        b_back.addActionListener(this);

        // load saved preferences
        loadData();

        // add panel to frame and configure frame
        f.add(p);
        f.setSize(500, 450);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // helper method to load saved preferences from travel_preferences table
    public void loadData() {
        try {
            // Step 1: get the connection from DBConnection
            Connection con = DBConnection.getConnection();

            // Step 2: create statement
            PreparedStatement pst = con.prepareStatement("SELECT stay, food, activities, route_from, route_via, route_to, route_type FROM travel_preferences WHERE trip_id = ?");
            pst.setInt(1, tripId);

            // Step 3: execute query
            ResultSet rs = pst.executeQuery();

            // Step 4: read result
            if (rs.next()) {
                t_stay.setText(rs.getString("stay") != null ? rs.getString("stay") : "None");
                t_food.setText(rs.getString("food") != null ? rs.getString("food") : "None");
                t_act.setText(rs.getString("activities") != null ? rs.getString("activities") : "None");

                String from = rs.getString("route_from");
                String via = rs.getString("route_via");
                String to = rs.getString("route_to");
                String type = rs.getString("route_type");

                if (from != null && to != null) {
                    t_route.setText(from + " via " + via + " to " + to + " (" + type + ")");
                } else {
                    t_route.setText("None");
                }
            }

            // Step 5: close the connection
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
        }
    }

    // handle button click events
    public void actionPerformed(ActionEvent e) {
        // if Select Stay button is clicked
        if (e.getSource() == b_stay) {
            f.dispose();
            new StayPage(userId, tripId);
        }
        // if Select Food button is clicked
        else if (e.getSource() == b_food) {
            f.dispose();
            new FoodPage(userId, tripId);
        }
        // if Select Activities button is clicked
        else if (e.getSource() == b_act) {
            f.dispose();
            new ActivitiesPage(userId, tripId);
        }
        // if Configure Route button is clicked
        else if (e.getSource() == b_route) {
            f.dispose();
            new RoutePage(userId, tripId);
        }
        // if Edit button is clicked
        else if (e.getSource() == b_edit) {
            JOptionPane.showMessageDialog(f, "Click on Stay, Food, Activities, or Route to modify preferences.");
        }
        // if Save & Continue button is clicked
        else if (e.getSource() == b_save) {
            JOptionPane.showMessageDialog(f, "Preferences saved! Proceeding to Itinerary.");
            f.dispose();
            new ItineraryPage(userId, tripId);
        }
        // if Next button is clicked
        else if (e.getSource() == b_next) {
            f.dispose();
            new ItineraryPage(userId, tripId);
        }
        // if Back button is clicked
        else if (e.getSource() == b_back) {
            f.dispose();
            new BudgetSplitPage(userId, tripId);
        }
    }
}
