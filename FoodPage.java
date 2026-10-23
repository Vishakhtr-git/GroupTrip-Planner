package tripmate;

// import required swing, awt and sql packages
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;

public class FoodPage implements ActionListener {

    // declare user and trip id variables
    int userId;
    int tripId;

    // declare swing components at the top
    JFrame f;
    JCheckBox[] checkBoxes;
    JButton b_save, b_back;
    JLabel status;

    // constructor to build food preferences window
    public FoodPage(int userId, int tripId) {
        this.userId = userId;
        this.tripId = tripId;

        // create the main window
        f = new JFrame("TripMate - Choose Foods");

        // create panel with 6 rows and 2 columns
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(6, 2));

        // initialize checkboxes array for 9 foods
        checkBoxes = new JCheckBox[9];

        // create buttons
        b_save = new JButton("Save & Return");
        b_back = new JButton("Back");
        status = new JLabel("Status: Choose foods");

        // load food items from foods table
        loadFoods(p);

        // add buttons and status to panel
        p.add(b_save);
        p.add(b_back);
        p.add(status);

        // register action listeners for buttons
        b_save.addActionListener(this);
        b_back.addActionListener(this);

        // load previously saved choices
        loadSavedFood();

        // add panel to frame and configure frame
        f.add(p);
        f.setSize(450, 400);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // helper method to load foods from database and add checkboxes
    public void loadFoods(JPanel p) {
        try {
            // Step 1: get the connection from DBConnection
            Connection con = DBConnection.getConnection();

            // Step 2: create statement
            Statement st = con.createStatement();

            // Step 3: execute query
            ResultSet rs = st.executeQuery("SELECT food_type FROM foods");

            // Step 4: read result into checkboxes
            int i = 0;
            while (rs.next() && i < checkBoxes.length) {
                String foodName = rs.getString("food_type");
                checkBoxes[i] = new JCheckBox(foodName);
                p.add(checkBoxes[i]);
                i++;
            }

            // Step 5: close the connection
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
        }
    }

    // helper method to check boxes for saved preferences
    public void loadSavedFood() {
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement("SELECT food FROM travel_preferences WHERE trip_id = ?");
            pst.setInt(1, tripId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String saved = rs.getString("food");
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
            String foodStr = "";
            for (int i = 0; i < checkBoxes.length; i++) {
                if (checkBoxes[i] != null && checkBoxes[i].isSelected()) {
                    if (foodStr.equals("")) {
                        foodStr = checkBoxes[i].getText();
                    } else {
                        foodStr = foodStr + ", " + checkBoxes[i].getText();
                    }
                }
            }

            if (foodStr.equals("")) {
                JOptionPane.showMessageDialog(f, "Please select at least one food option");
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
                    PreparedStatement pst = con.prepareStatement("UPDATE travel_preferences SET food = ? WHERE trip_id = ?");
                    pst.setString(1, foodStr);
                    pst.setInt(2, tripId);
                    pst.executeUpdate();
                } else {
                    PreparedStatement pst = con.prepareStatement("INSERT INTO travel_preferences (trip_id, food) VALUES (?, ?)");
                    pst.setInt(1, tripId);
                    pst.setString(2, foodStr);
                    pst.executeUpdate();
                }

                // Step 5: close the connection
                con.close();

                JOptionPane.showMessageDialog(f, "Foods saved successfully!");
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
