package tripmate;

// import required swing, awt and sql packages
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;

public class RoutePage implements ActionListener {

    // declare user and trip id variables
    int userId;
    int tripId;

    // declare swing components at the top
    JFrame f;
    JLabel l1, l2, l3, l4;
    JTextField t_from, t_via, t_to;
    JComboBox cb_type;
    JButton b_save, b_back;

    // constructor to build route preferences window
    public RoutePage(int userId, int tripId) {
        this.userId = userId;
        this.tripId = tripId;

        // create the main window
        f = new JFrame("TripMate - Configure Route");

        // create panel with 5 rows and 2 columns
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(5, 2));

        // create labels and text fields
        l1 = new JLabel("From:");
        t_from = new JTextField();

        l2 = new JLabel("Via:");
        t_via = new JTextField();

        l3 = new JLabel("To:");
        t_to = new JTextField();

        l4 = new JLabel("Route Type:");
        String[] types = {"Fastest", "Shortest", "Scenic"};
        cb_type = new JComboBox(types);

        // create buttons
        b_save = new JButton("Save & Return");
        b_back = new JButton("Back");

        // add components to the panel
        p.add(l1);
        p.add(t_from);
        p.add(l2);
        p.add(t_via);
        p.add(l3);
        p.add(t_to);
        p.add(l4);
        p.add(cb_type);
        p.add(b_save);
        p.add(b_back);

        // register action listeners for buttons
        b_save.addActionListener(this);
        b_back.addActionListener(this);

        // load previously saved route if any
        loadSavedRoute();

        // add panel to frame and configure frame
        f.add(p);
        f.setSize(400, 300);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // helper method to load route from database
    public void loadSavedRoute() {
        try {
            // Step 1: get the connection from DBConnection
            Connection con = DBConnection.getConnection();

            // Step 2: create statement
            PreparedStatement pst = con.prepareStatement("SELECT route_from, route_via, route_to, route_type FROM travel_preferences WHERE trip_id = ?");
            pst.setInt(1, tripId);

            // Step 3: execute query
            ResultSet rs = pst.executeQuery();

            // Step 4: read result
            if (rs.next()) {
                if (rs.getString("route_from") != null) {
                    t_from.setText(rs.getString("route_from"));
                }
                if (rs.getString("route_via") != null) {
                    t_via.setText(rs.getString("route_via"));
                }
                if (rs.getString("route_to") != null) {
                    t_to.setText(rs.getString("route_to"));
                }
                if (rs.getString("route_type") != null) {
                    cb_type.setSelectedItem(rs.getString("route_type"));
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
        // if Save button is clicked
        if (e.getSource() == b_save) {
            String from = t_from.getText();
            String via = t_via.getText();
            String to = t_to.getText();
            String type = (String) cb_type.getSelectedItem();

            if (from.equals("") || to.equals("")) {
                JOptionPane.showMessageDialog(f, "Please fill From and To fields");
                return;
            }

            try {
                // Step 1: get the connection from DBConnection
                Connection con = DBConnection.getConnection();

                // Step 2: check if travel_preferences row exists
                PreparedStatement checkPst = con.prepareStatement("SELECT trip_id FROM travel_preferences WHERE trip_id = ?");
                checkPst.setInt(1, tripId);
                ResultSet rs = checkPst.executeQuery();

                // update if exists, insert if new
                if (rs.next()) {
                    PreparedStatement pst = con.prepareStatement("UPDATE travel_preferences SET route_from = ?, route_via = ?, route_to = ?, route_type = ? WHERE trip_id = ?");
                    pst.setString(1, from);
                    pst.setString(2, via);
                    pst.setString(3, to);
                    pst.setString(4, type);
                    pst.setInt(5, tripId);
                    pst.executeUpdate();
                } else {
                    PreparedStatement pst = con.prepareStatement("INSERT INTO travel_preferences (trip_id, route_from, route_via, route_to, route_type) VALUES (?, ?, ?, ?, ?)");
                    pst.setInt(1, tripId);
                    pst.setString(2, from);
                    pst.setString(3, via);
                    pst.setString(4, to);
                    pst.setString(5, type);
                    pst.executeUpdate();
                }

                // Step 5: close the connection
                con.close();

                JOptionPane.showMessageDialog(f, "Route saved successfully!");
                // close window and return to TravelPreferencesPage
                f.dispose();
                new TravelPreferencesPage(userId, tripId);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
            }
        }
        // if Back button is clicked
        else if (e.getSource() == b_back) {
            f.dispose();
            new TravelPreferencesPage(userId, tripId);
        }
    }
}
