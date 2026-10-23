package tripmate;

// import required swing, awt and sql packages
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;

public class StayPage implements ActionListener {

    // declare user and trip id variables
    int userId;
    int tripId;

    // declare swing components at the top
    JFrame f;
    JLabel l1, l2, status;
    JComboBox cb_stay;
    JButton b_save, b_back;

    // constructor to build stay selection window
    public StayPage(int userId, int tripId) {
        this.userId = userId;
        this.tripId = tripId;

        // create the main window
        f = new JFrame("TripMate - Choose Stay");

        // create panel with 3 rows and 2 columns
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(3, 2));

        // create stay dropdown
        l1 = new JLabel("Available Stays:");
        cb_stay = new JComboBox();

        // create buttons
        b_save = new JButton("Save & Return");
        b_back = new JButton("Back");

        // create status labels
        l2 = new JLabel("Status:");
        status = new JLabel("Select a stay option");

        // add components to panel
        p.add(l1);
        p.add(cb_stay);
        p.add(b_save);
        p.add(b_back);
        p.add(l2);
        p.add(status);

        // register action listeners for buttons
        b_save.addActionListener(this);
        b_back.addActionListener(this);

        // load stays from database into dropdown
        loadStays();

        // add panel to frame and configure frame
        f.add(p);
        f.setSize(500, 250);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // helper method to populate dropdown from stays table
    public void loadStays() {
        try {
            // Step 1: get the connection from DBConnection
            Connection con = DBConnection.getConnection();

            // Step 2: create statement
            Statement st = con.createStatement();

            // Step 3: execute query
            ResultSet rs = st.executeQuery("SELECT stay_name, category, price_per_night FROM stays");

            // Step 4: read result into dropdown
            while (rs.next()) {
                String item = rs.getString("stay_name") + " [" + rs.getString("category") + " - Rs." + rs.getInt("price_per_night") + "/night]";
                cb_stay.addItem(item);
            }

            // load currently saved stay if any
            PreparedStatement pst = con.prepareStatement("SELECT stay FROM travel_preferences WHERE trip_id = ?");
            pst.setInt(1, tripId);
            ResultSet rsSaved = pst.executeQuery();
            if (rsSaved.next()) {
                String savedStay = rsSaved.getString("stay");
                if (savedStay != null) {
                    cb_stay.setSelectedItem(savedStay);
                    status.setText("Current: " + savedStay);
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
            String selectedStay = (String) cb_stay.getSelectedItem();
            if (selectedStay == null) {
                JOptionPane.showMessageDialog(f, "Please select a stay");
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
                    PreparedStatement pst = con.prepareStatement("UPDATE travel_preferences SET stay = ? WHERE trip_id = ?");
                    pst.setString(1, selectedStay);
                    pst.setInt(2, tripId);
                    pst.executeUpdate();
                } else {
                    PreparedStatement pst = con.prepareStatement("INSERT INTO travel_preferences (trip_id, stay) VALUES (?, ?)");
                    pst.setInt(1, tripId);
                    pst.setString(2, selectedStay);
                    pst.executeUpdate();
                }

                // Step 5: close the connection
                con.close();

                JOptionPane.showMessageDialog(f, "Stay saved successfully!");
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
