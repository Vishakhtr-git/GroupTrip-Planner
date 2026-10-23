package tripmate;

// import required swing, awt and sql packages
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;

public class TripOverviewPage implements ActionListener {

    // declare user and trip id variables
    int userId;
    int tripId;

    // declare swing components at the top
    JFrame f;
    JLabel l1, l2, l3, l4, l5, l6, l8, result;
    JTextField t1, t2, t3, t4, t5;
    JComboBox cb_mode;
    JButton b_edit, b_save, b_back, b_next;

    // constructor to build trip overview window
    public TripOverviewPage(int userId, int tripId) {
        this.userId = userId;
        this.tripId = tripId;

        // create the main window
        f = new JFrame("TripMate - Trip Overview");

        // create panel with 9 rows and 2 columns
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(9, 2));

        // create labels and text fields
        l1 = new JLabel("Destination:");
        t1 = new JTextField();

        l2 = new JLabel("Start Date (dd-mm-yyyy):");
        t2 = new JTextField();

        l3 = new JLabel("Duration (in days):");
        t3 = new JTextField();

        l4 = new JLabel("Number of Members:");
        t4 = new JTextField();

        l5 = new JLabel("Member Names (separated by commas):");
        t5 = new JTextField();

        l6 = new JLabel("Travel Mode:");
        String[] modes = {"Car", "Bus", "Train", "Flight", "Bike"};
        cb_mode = new JComboBox(modes);

        // create buttons
        b_edit = new JButton("Edit");
        b_save = new JButton("Save");

        b_back = new JButton("Back");
        b_next = new JButton("Next: Budget");

        l8 = new JLabel("Status:");
        result = new JLabel("");

        // add components to the panel
        p.add(l1);
        p.add(t1);
        p.add(l2);
        p.add(t2);
        p.add(l3);
        p.add(t3);
        p.add(l4);
        p.add(t4);
        p.add(l5);
        p.add(t5);
        p.add(l6);
        p.add(cb_mode);
        p.add(b_edit);
        p.add(b_save);
        p.add(b_back);
        p.add(b_next);
        p.add(l8);
        p.add(result);

        // register action listeners for buttons
        b_edit.addActionListener(this);
        b_save.addActionListener(this);
        b_back.addActionListener(this);
        b_next.addActionListener(this);

        // if existing trip, load data and make read-only
        if (tripId > 0) {
            loadData();
            setFieldsEditable(false);
        } else {
            setFieldsEditable(true);
        }

        // add panel to frame and configure frame
        f.add(p);
        f.setSize(480, 450);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // helper method to make fields editable or read-only
    public void setFieldsEditable(boolean b) {
        t1.setEditable(b);
        t2.setEditable(b);
        t3.setEditable(b);
        t4.setEditable(b);
        t5.setEditable(b);
        cb_mode.setEnabled(b);
    }

    // helper method to load saved trip data from database
    public void loadData() {
        try {
            // Step 1: get the connection from DBConnection
            Connection con = DBConnection.getConnection();

            // Step 2: create the statement for trip details
            PreparedStatement pst = con.prepareStatement("SELECT destination, start_date, days, members, travel_mode FROM trips WHERE trip_id = ?");
            pst.setInt(1, tripId);

            // Step 3: execute the query
            ResultSet rs = pst.executeQuery();

            // Step 4: read the result
            if (rs.next()) {
                t1.setText(rs.getString("destination"));
                t2.setText(rs.getString("start_date"));
                t3.setText(String.valueOf(rs.getInt("days")));
                t4.setText(String.valueOf(rs.getInt("members")));
                cb_mode.setSelectedItem(rs.getString("travel_mode"));
            }

            // load member names from members table
            PreparedStatement pstMem = con.prepareStatement("SELECT member_name FROM members WHERE trip_id = ?");
            pstMem.setInt(1, tripId);
            ResultSet rsMem = pstMem.executeQuery();

            String names = "";
            while (rsMem.next()) {
                if (names.equals("")) {
                    names = rsMem.getString("member_name");
                } else {
                    names = names + ", " + rsMem.getString("member_name");
                }
            }
            t5.setText(names);

            // Step 5: close the connection
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
        }
    }

    // handle button click events
    public void actionPerformed(ActionEvent e) {
        // if Edit button is clicked
        if (e.getSource() == b_edit) {
            setFieldsEditable(true);
            result.setText("Editing enabled");
        }
        // if Save button is clicked
        else if (e.getSource() == b_save) {
            String dest = t1.getText();
            String sdate = t2.getText();
            String sdays = t3.getText();
            String smembers = t4.getText();
            String snames = t5.getText();
            String mode = (String) cb_mode.getSelectedItem();

            // check if any field is empty
            if (dest.equals("") || sdate.equals("") || sdays.equals("") || smembers.equals("") || snames.equals("")) {
                JOptionPane.showMessageDialog(f, "Please fill all fields");
                return;
            }

            // validate number formats
            int days = 0;
            int numMembers = 0;
            try {
                days = Integer.parseInt(sdays);
                numMembers = Integer.parseInt(smembers);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(f, "Enter numbers only");
                return;
            }

            // split member names by comma
            String[] nameList = snames.split(",");

            try {
                // Step 1: get the connection from DBConnection
                Connection con = DBConnection.getConnection();

                // if this is a new trip (tripId is 0), insert new row
                if (tripId == 0) {
                    // Step 2: create the statement with RETURN_GENERATED_KEYS
                    PreparedStatement pst = con.prepareStatement("INSERT INTO trips (user_id, destination, start_date, days, members, travel_mode, status) VALUES (?, ?, ?, ?, ?, ?, 'Draft')", Statement.RETURN_GENERATED_KEYS);
                    pst.setInt(1, userId);
                    pst.setString(2, dest);
                    pst.setString(3, sdate);
                    pst.setInt(4, days);
                    pst.setInt(5, numMembers);
                    pst.setString(6, mode);

                    // Step 3: execute query
                    int rows = pst.executeUpdate();

                    // Step 4: read generated trip_id
                    if (rows > 0) {
                        ResultSet gk = pst.getGeneratedKeys();
                        if (gk.next()) {
                            tripId = gk.getInt(1);
                        }

                        // insert members into members table
                        for (int i = 0; i < nameList.length; i++) {
                            String m = nameList[i].trim();
                            if (!m.equals("")) {
                                PreparedStatement pstMem = con.prepareStatement("INSERT INTO members (trip_id, member_name) VALUES (?, ?)");
                                pstMem.setInt(1, tripId);
                                pstMem.setString(2, m);
                                pstMem.executeUpdate();
                            }
                        }

                        JOptionPane.showMessageDialog(f, "Saved successfully! Proceeding to Budget and Expense Split.");
                        f.dispose();
                        new BudgetSplitPage(userId, tripId);
                    } else {
                        JOptionPane.showMessageDialog(f, "Failed to save trip");
                    }
                }
                // if trip already exists, update the existing row
                else {
                    // Step 2: create update statement
                    PreparedStatement pst = con.prepareStatement("UPDATE trips SET destination = ?, start_date = ?, days = ?, members = ?, travel_mode = ? WHERE trip_id = ?");
                    pst.setString(1, dest);
                    pst.setString(2, sdate);
                    pst.setInt(3, days);
                    pst.setInt(4, numMembers);
                    pst.setString(5, mode);
                    pst.setInt(6, tripId);

                    // Step 3: execute query
                    int rows = pst.executeUpdate();

                    // Step 4: update members table
                    if (rows > 0) {
                        // delete old members
                        PreparedStatement delMem = con.prepareStatement("DELETE FROM members WHERE trip_id = ?");
                        delMem.setInt(1, tripId);
                        delMem.executeUpdate();

                        // insert updated members
                        for (int i = 0; i < nameList.length; i++) {
                            String m = nameList[i].trim();
                            if (!m.equals("")) {
                                PreparedStatement pstMem = con.prepareStatement("INSERT INTO members (trip_id, member_name) VALUES (?, ?)");
                                pstMem.setInt(1, tripId);
                                pstMem.setString(2, m);
                                pstMem.executeUpdate();
                            }
                        }

                        JOptionPane.showMessageDialog(f, "Updated successfully! Proceeding to Budget and Expense Split.");
                        f.dispose();
                        new BudgetSplitPage(userId, tripId);
                    } else {
                        JOptionPane.showMessageDialog(f, "Failed to update trip");
                    }
                }

                // Step 5: close the connection
                con.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
            }
        }
        // if Next button is clicked
        else if (e.getSource() == b_next) {
            if (tripId == 0) {
                JOptionPane.showMessageDialog(f, "Save trip overview first");
                return;
            }
            f.dispose();
            new BudgetSplitPage(userId, tripId);
        }
        // if Back button is clicked
        else if (e.getSource() == b_back) {
            f.dispose();
            new Dashboard(userId, tripId);
        }
    }
}
