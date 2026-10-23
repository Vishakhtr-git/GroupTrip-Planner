package tripmate;

// import required swing, awt and sql packages
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.sql.*;

public class ItineraryPage implements ActionListener {

    // declare user, trip id and duration variables
    int userId;
    int tripId;
    int tripDuration = 1;
    int editingRow = -1;

    // declare swing components at the top
    JFrame f;
    JLabel l1, l2, l3, status;
    JTextField t_day, t_time, t_place;
    JButton b_add, b_remove, b_edit, b_save, b_next, b_back;
    JTable table;
    DefaultTableModel model;

    // constructor to build itinerary window
    public ItineraryPage(int userId, int tripId) {
        this.userId = userId;
        this.tripId = tripId;

        // create the main window
        f = new JFrame("TripMate - Itinerary Planner");

        // create north panel with 6 rows and 2 columns for inputs and buttons
        JPanel p_north = new JPanel();
        p_north.setLayout(new GridLayout(6, 2));

        // create labels and text fields
        l1 = new JLabel("Day Number:");
        t_day = new JTextField();

        l2 = new JLabel("Time Slot (e.g. 10:00 AM):");
        t_time = new JTextField();

        l3 = new JLabel("Place or Activity:");
        t_place = new JTextField();

        // create buttons
        b_add = new JButton("Add");
        b_remove = new JButton("Remove");

        b_edit = new JButton("Edit");
        b_save = new JButton("Save");

        b_back = new JButton("Back");
        b_next = new JButton("Next: Packing");

        // add components to the north panel
        p_north.add(l1);
        p_north.add(t_day);
        p_north.add(l2);
        p_north.add(t_time);
        p_north.add(l3);
        p_north.add(t_place);
        p_north.add(b_add);
        p_north.add(b_remove);
        p_north.add(b_edit);
        p_north.add(b_save);
        p_north.add(b_back);
        p_north.add(b_next);

        // register action listeners for buttons
        b_add.addActionListener(this);
        b_remove.addActionListener(this);
        b_edit.addActionListener(this);
        b_save.addActionListener(this);
        b_next.addActionListener(this);
        b_back.addActionListener(this);

        // create table and scroll pane for itinerary items
        String[] columns = {"Day", "Time", "Place or Activity"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);

        // add panels to frame using BorderLayout
        f.setLayout(new BorderLayout());
        f.add(p_north, BorderLayout.NORTH);
        f.add(sp, BorderLayout.CENTER);

        // load trip duration and saved itinerary data
        loadData();

        // configure frame
        f.setSize(600, 500);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // helper method to make fields and buttons editable or read-only
    public void setFieldsEditable(boolean b) {
        t_day.setEditable(b);
        t_time.setEditable(b);
        t_place.setEditable(b);
        b_add.setEnabled(b);
        b_remove.setEnabled(b);
    }

    // helper method to load duration and saved items from database
    public void loadData() {
        try {
            // Step 1: get the connection from DBConnection
            Connection con = DBConnection.getConnection();

            // Step 2: query trip duration
            PreparedStatement pstTrip = con.prepareStatement("SELECT days FROM trips WHERE trip_id = ?");
            pstTrip.setInt(1, tripId);
            ResultSet rsTrip = pstTrip.executeQuery();
            if (rsTrip.next()) {
                tripDuration = rsTrip.getInt("days");
            }

            // Step 3: query saved itinerary sorted by day and time
            PreparedStatement pst = con.prepareStatement("SELECT day_no, time_slot, place_activity FROM itinerary WHERE trip_id = ? ORDER BY day_no, time_slot");
            pst.setInt(1, tripId);

            // Step 4: read result into table
            ResultSet rs = pst.executeQuery();
            model.setRowCount(0);
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                model.addRow(new Object[]{rs.getInt("day_no"), rs.getString("time_slot"), rs.getString("place_activity")});
            }

            // Step 5: close the connection
            con.close();

            if (hasData) {
                setFieldsEditable(false);
            } else {
                setFieldsEditable(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
        }
    }

    // handle button click events
    public void actionPerformed(ActionEvent e) {
        // if Add button is clicked
        if (e.getSource() == b_add) {
            String s_day = t_day.getText();
            String time = t_time.getText();
            String place = t_place.getText();

            // check if any field is empty
            if (s_day.equals("") || time.equals("") || place.equals("")) {
                JOptionPane.showMessageDialog(f, "Please fill all fields");
                return;
            }

            // validate day number
            int day = 0;
            try {
                day = Integer.parseInt(s_day);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(f, "Enter numbers only");
                return;
            }

            // check day is between 1 and duration
            if (day < 1 || day > tripDuration) {
                JOptionPane.showMessageDialog(f, "Day number must be between 1 and " + tripDuration);
                return;
            }

            // if editing an existing row in table
            if (editingRow != -1) {
                model.setValueAt(day, editingRow, 0);
                model.setValueAt(time, editingRow, 1);
                model.setValueAt(place, editingRow, 2);
                editingRow = -1;
                b_add.setText("Add");
            } else {
                // add new row to table
                model.addRow(new Object[]{day, time, place});
            }

            // clear input fields
            t_day.setText("");
            t_time.setText("");
            t_place.setText("");
        }
        // if Remove button is clicked
        else if (e.getSource() == b_remove) {
            int sel = table.getSelectedRow();
            if (sel == -1) {
                JOptionPane.showMessageDialog(f, "Select a row to remove");
                return;
            }
            model.removeRow(sel);
        }
        // if Edit button is clicked
        else if (e.getSource() == b_edit) {
            int sel = table.getSelectedRow();
            if (sel == -1) {
                JOptionPane.showMessageDialog(f, "Select a row to edit");
                return;
            }
            // load selected row data into fields
            t_day.setText(model.getValueAt(sel, 0).toString());
            t_time.setText(model.getValueAt(sel, 1).toString());
            t_place.setText(model.getValueAt(sel, 2).toString());

            editingRow = sel;
            b_add.setText("Update Row");
            setFieldsEditable(true);
        }
        // if Save button is clicked
        else if (e.getSource() == b_save) {
            try {
                // Step 1: get the connection from DBConnection
                Connection con = DBConnection.getConnection();

                // Step 2: delete old itinerary items for this trip
                PreparedStatement delPst = con.prepareStatement("DELETE FROM itinerary WHERE trip_id = ?");
                delPst.setInt(1, tripId);
                delPst.executeUpdate();

                // Step 3: insert each row from table into itinerary table
                for (int i = 0; i < model.getRowCount(); i++) {
                    int day = Integer.parseInt(model.getValueAt(i, 0).toString());
                    String time = model.getValueAt(i, 1).toString();
                    String place = model.getValueAt(i, 2).toString();

                    PreparedStatement insPst = con.prepareStatement("INSERT INTO itinerary (trip_id, day_no, time_slot, place_activity) VALUES (?, ?, ?, ?)");
                    insPst.setInt(1, tripId);
                    insPst.setInt(2, day);
                    insPst.setString(3, time);
                    insPst.setString(4, place);
                    insPst.executeUpdate();
                }

                // Step 5: close the connection
                con.close();

                JOptionPane.showMessageDialog(f, "Saved successfully! Proceeding to Packing Checklist.");
                f.dispose();
                new PackingChecklistPage(userId, tripId);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
            }
        }
        // if Next button is clicked
        else if (e.getSource() == b_next) {
            f.dispose();
            new PackingChecklistPage(userId, tripId);
        }
        // if Back button is clicked
        else if (e.getSource() == b_back) {
            f.dispose();
            new TravelPreferencesPage(userId, tripId);
        }
    }
}
