package tripmate;

// import required swing, awt and sql packages
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;

public class ActivitiesPage implements ActionListener {

    // declare user and trip id variables
    int userId;
    int tripId;

    // declare swing components at the top
    JFrame f;
    JCheckBox[] checkBoxes;
    JButton b_save, b_back;
    JLabel status;

    // constructor to build activities selection window
    public ActivitiesPage(int userId, int tripId) {
        this.userId = userId;
        this.tripId = tripId;

        // create the main window
        f = new JFrame("TripMate - Choose Activities");

        // create panel with 5 rows and 2 columns
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(5, 2));

        // initialize checkboxes array for 8 activities
        checkBoxes = new JCheckBox[8];

        // create buttons
        b_save = new JButton("Save & Return");
        b_back = new JButton("Back");

        // load activities from activities table
        loadActivities(p);

        // add buttons to panel
        p.add(b_save);
        p.add(b_back);

        // register action listeners for buttons
        b_save.addActionListener(this);
        b_back.addActionListener(this);

        // load previously saved choices
        loadSavedActivities();

        // add panel to frame and configure frame
        f.add(p);
        f.setSize(450, 350);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // helper method to load activities from database and add checkboxes
    public void loadActivities(JPanel p) {
        try {
            // Step 1: get the connection from DBConnection
            Connection con = DBConnection.getConnection();

            // Step 2: create statement
            Statement st = con.createStatement();

            // Step 3: execute query
            ResultSet rs = st.executeQuery("SELECT act_name FROM activities");

            // Step 4: read result into checkboxes
            int i = 0;
            while (rs.next() && i < checkBoxes.length) {
                String actName = rs.getString("act_name");
                checkBoxes[i] = new JCheckBox(actName);
                p.add(checkBoxes[i]);
                i++;
            }

            // Step 5: close the connection
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
        }
    }

    // helper method to check boxes for saved activities
    public void loadSavedActivities() {
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("SELECT activities FROM travel_preferences WHERE trip_id = ?");
            pst.setInt(1, tripId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String saved = rs.getString("activities");
                if (saved != null) {
                    for (int i = 0; i < checkBoxes.length; i++) {
                        if (checkBoxes[i] != null && saved.contains(checkBoxes[i].getText())) {
                            checkBoxes[i].setSelected(true);
                        }
                    }
                }
            }
            con.close();
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    // handle button click events
    public void actionPerformed(ActionEvent e) {
        // if Save button is clicked
        if (e.getSource() == b_save) {
            String actStr = "";
            for (int i = 0; i < checkBoxes.length; i++) {
                if (checkBoxes[i] != null && checkBoxes[i].isSelected()) {
                    if (actStr.equals("")) {
                        actStr = checkBoxes[i].getText();
                    } else {
                        actStr = actStr + ", " + checkBoxes[i].getText();
                    }
                }
            }

            if (actStr.equals("")) {
                JOptionPane.showMessageDialog(f, "Please select at least one activity");
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
                    PreparedStatement pst = con.prepareStatement("UPDATE travel_preferences SET activities = ? WHERE trip_id = ?");
                    pst.setString(1, actStr);
                    pst.setInt(2, tripId);
                    pst.executeUpdate();
                } else {
                    PreparedStatement pst = con.prepareStatement("INSERT INTO travel_preferences (trip_id, activities) VALUES (?, ?)");
                    pst.setInt(1, tripId);
                    pst.setString(2, actStr);
                    pst.executeUpdate();
                }

                // Step 5: close the connection
                con.close();

                JOptionPane.showMessageDialog(f, "Activities saved successfully!");
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
