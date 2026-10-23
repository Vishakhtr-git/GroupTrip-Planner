package tripmate;

// import required swing, awt and sql packages
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.sql.*;

public class BudgetSplitPage implements ActionListener {

    // declare user and trip id variables
    int userId;
    int tripId;

    // declare swing components at the top
    JFrame f;
    JLabel l1, l2, l3, l4, l5, l6, status;
    JTextField t_total, t_stay, t_vehicle, t_food, t_others;
    JComboBox cb_split;
    JButton b_calc, b_edit, b_save, b_next, b_back;
    JTable table;
    DefaultTableModel model;

    // constructor to build budget and expense split window
    public BudgetSplitPage(int userId, int tripId) {
        this.userId = userId;
        this.tripId = tripId;

        // create the main window
        f = new JFrame("TripMate - Budget and Expense Split");

        // create north panel with 9 rows and 2 columns for inputs and buttons
        JPanel p_north = new JPanel();
        p_north.setLayout(new GridLayout(9, 2));

        // create input labels and fields
        l1 = new JLabel("Total Budget:");
        t_total = new JTextField();

        l2 = new JLabel("Split Type:");
        String[] types = {"Equal", "Customized", "Percentage"};
        cb_split = new JComboBox(types);

        l3 = new JLabel("Stay Expense:");
        t_stay = new JTextField("0");

        l4 = new JLabel("Vehicle Expense:");
        t_vehicle = new JTextField("0");

        l5 = new JLabel("Food Expense:");
        t_food = new JTextField("0");

        l6 = new JLabel("Others Expense:");
        t_others = new JTextField("0");

        // create buttons
        b_calc = new JButton("Calculate Split");
        b_edit = new JButton("Edit");

        b_save = new JButton("Save");
        b_next = new JButton("Next: Preferences");

        b_back = new JButton("Back");
        status = new JLabel("Status: Ready");

        // add components to the north panel
        p_north.add(l1);
        p_north.add(t_total);
        p_north.add(l2);
        p_north.add(cb_split);
        p_north.add(l3);
        p_north.add(t_stay);
        p_north.add(l4);
        p_north.add(t_vehicle);
        p_north.add(l5);
        p_north.add(t_food);
        p_north.add(l6);
        p_north.add(t_others);
        p_north.add(b_calc);
        p_north.add(b_edit);
        p_north.add(b_save);
        p_north.add(b_next);
        p_north.add(b_back);
        p_north.add(status);

        // register action listeners for buttons
        b_calc.addActionListener(this);
        b_edit.addActionListener(this);
        b_save.addActionListener(this);
        b_next.addActionListener(this);
        b_back.addActionListener(this);

        // create table and scroll pane for member expense share
        String[] columns = {"Member", "Amount", "Percentage"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);

        // add panels to frame using BorderLayout
        f.setLayout(new BorderLayout());
        f.add(p_north, BorderLayout.NORTH);
        f.add(sp, BorderLayout.CENTER);

        // load saved budget or load trip members
        loadData();

        // configure frame
        f.setSize(550, 580);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // helper method to make fields editable or read-only
    public void setFieldsEditable(boolean b) {
        t_total.setEditable(b);
        cb_split.setEnabled(b);
        t_stay.setEditable(b);
        t_vehicle.setEditable(b);
        t_food.setEditable(b);
        t_others.setEditable(b);
        table.setEnabled(b);
    }

    // helper method to load data from database
    public void loadData() {
        try {
            // Step 1: get the connection from DBConnection
            Connection con = DBConnection.getConnection();

            // Step 2: check if budget is already saved for this trip
            PreparedStatement pst = con.prepareStatement("SELECT total, split_type, stay, vehicle, food, others FROM budget WHERE trip_id = ?");
            pst.setInt(1, tripId);

            // Step 3: execute the query
            ResultSet rs = pst.executeQuery();

            // Step 4: read result
            if (rs.next()) {
                t_total.setText(String.valueOf(rs.getDouble("total")));
                cb_split.setSelectedItem(rs.getString("split_type"));
                t_stay.setText(String.valueOf(rs.getDouble("stay")));
                t_vehicle.setText(String.valueOf(rs.getDouble("vehicle")));
                t_food.setText(String.valueOf(rs.getDouble("food")));
                t_others.setText(String.valueOf(rs.getDouble("others")));

                // load saved member shares
                PreparedStatement pstShare = con.prepareStatement("SELECT member_name, amount, percentage FROM member_share WHERE trip_id = ?");
                pstShare.setInt(1, tripId);
                ResultSet rsShare = pstShare.executeQuery();

                model.setRowCount(0);
                while (rsShare.next()) {
                    model.addRow(new Object[]{rsShare.getString("member_name"), rsShare.getDouble("amount"), rsShare.getDouble("percentage")});
                }

                // make fields read-only
                setFieldsEditable(false);
                status.setText("Status: Saved data loaded");
            } else {
                // if no saved budget yet, populate member names from members table
                PreparedStatement pstMem = con.prepareStatement("SELECT member_name FROM members WHERE trip_id = ?");
                pstMem.setInt(1, tripId);
                ResultSet rsMem = pstMem.executeQuery();

                model.setRowCount(0);
                while (rsMem.next()) {
                    model.addRow(new Object[]{rsMem.getString("member_name"), 0.0, 0.0});
                }
                setFieldsEditable(true);
                status.setText("Status: New budget");
            }

            // Step 5: close the connection
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
        }
    }

    // helper method to calculate split values in table
    public boolean calculateSplit() {
        // stop active table editing if user is typing
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        String s_total = t_total.getText();
        String s_stay = t_stay.getText();
        String s_veh = t_vehicle.getText();
        String s_food = t_food.getText();
        String s_oth = t_others.getText();

        if (s_total.equals("") || s_stay.equals("") || s_veh.equals("") || s_food.equals("") || s_oth.equals("")) {
            JOptionPane.showMessageDialog(f, "Please fill all fields");
            return false;
        }

        double total = 0;
        double stay = 0;
        double vehicle = 0;
        double food = 0;
        double others = 0;

        try {
            total = Double.parseDouble(s_total);
            stay = Double.parseDouble(s_stay);
            vehicle = Double.parseDouble(s_veh);
            food = Double.parseDouble(s_food);
            others = Double.parseDouble(s_oth);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(f, "Enter numbers only");
            return false;
        }

        // check category sum does not exceed total budget
        if ((stay + vehicle + food + others) > total) {
            JOptionPane.showMessageDialog(f, "Expenses (Stay + Vehicle + Food + Others) cannot exceed Total Budget");
            return false;
        }

        int count = model.getRowCount();
        if (count == 0) {
            JOptionPane.showMessageDialog(f, "No members found for this trip");
            return false;
        }

        String splitType = (String) cb_split.getSelectedItem();

        // 1. Equal split calculation
        if (splitType.equals("Equal")) {
            double share = total / count;
            double pct = 100.0 / count;
            for (int i = 0; i < count; i++) {
                model.setValueAt(Math.round(share * 100.0) / 100.0, i, 1);
                model.setValueAt(Math.round(pct * 100.0) / 100.0, i, 2);
            }
            status.setText("Status: Equal split calculated");
            return true;
        }
        // 2. Customized split calculation
        else if (splitType.equals("Customized")) {
            double sum = 0;
            for (int i = 0; i < count; i++) {
                double amt = 0;
                try {
                    amt = Double.parseDouble(model.getValueAt(i, 1).toString());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(f, "Enter valid numbers in Amount column");
                    return false;
                }
                sum += amt;
                double pct = (amt / total) * 100.0;
                model.setValueAt(Math.round(pct * 100.0) / 100.0, i, 2);
            }
            if (Math.abs(sum - total) > 0.01) {
                JOptionPane.showMessageDialog(f, "Sum of member amounts (" + sum + ") must equal Total Budget (" + total + ")");
                return false;
            }
            status.setText("Status: Customized split verified");
            return true;
        }
        // 3. Percentage split calculation
        else if (splitType.equals("Percentage")) {
            double pctSum = 0;
            for (int i = 0; i < count; i++) {
                double pct = 0;
                try {
                    pct = Double.parseDouble(model.getValueAt(i, 2).toString());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(f, "Enter valid numbers in Percentage column");
                    return false;
                }
                pctSum += pct;
                double amt = (pct / 100.0) * total;
                model.setValueAt(Math.round(amt * 100.0) / 100.0, i, 1);
            }
            if (Math.abs(pctSum - 100.0) > 0.01) {
                JOptionPane.showMessageDialog(f, "Total percentage (" + pctSum + "%) must equal 100%");
                return false;
            }
            status.setText("Status: Percentage split verified");
            return true;
        }

        return true;
    }

    // handle button click events
    public void actionPerformed(ActionEvent e) {
        // if Calculate Split button is clicked
        if (e.getSource() == b_calc) {
            calculateSplit();
        }
        // if Edit button is clicked
        else if (e.getSource() == b_edit) {
            setFieldsEditable(true);
            status.setText("Status: Editing enabled");
        }
        // if Save button is clicked
        else if (e.getSource() == b_save) {
            // calculate and validate split first
            if (!calculateSplit()) {
                return;
            }

            double total = Double.parseDouble(t_total.getText());
            String splitType = (String) cb_split.getSelectedItem();
            double stay = Double.parseDouble(t_stay.getText());
            double vehicle = Double.parseDouble(t_vehicle.getText());
            double food = Double.parseDouble(t_food.getText());
            double others = Double.parseDouble(t_others.getText());

            try {
                // Step 1: get the connection from DBConnection
                Connection con = DBConnection.getConnection();

                // Step 2: check if row exists in budget table
                PreparedStatement checkPst = con.prepareStatement("SELECT trip_id FROM budget WHERE trip_id = ?");
                checkPst.setInt(1, tripId);
                ResultSet rs = checkPst.executeQuery();

                // if row exists, update it
                if (rs.next()) {
                    PreparedStatement pst = con.prepareStatement("UPDATE budget SET total = ?, split_type = ?, stay = ?, vehicle = ?, food = ?, others = ? WHERE trip_id = ?");
                    pst.setDouble(1, total);
                    pst.setString(2, splitType);
                    pst.setDouble(3, stay);
                    pst.setDouble(4, vehicle);
                    pst.setDouble(5, food);
                    pst.setDouble(6, others);
                    pst.setInt(7, tripId);
                    pst.executeUpdate();
                }
                // if row does not exist, insert it
                else {
                    PreparedStatement pst = con.prepareStatement("INSERT INTO budget (trip_id, total, split_type, stay, vehicle, food, others) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    pst.setInt(1, tripId);
                    pst.setDouble(2, total);
                    pst.setString(3, splitType);
                    pst.setDouble(4, stay);
                    pst.setDouble(5, vehicle);
                    pst.setDouble(6, food);
                    pst.setDouble(7, others);
                    pst.executeUpdate();
                }

                // delete existing member_share rows for this trip
                PreparedStatement delPst = con.prepareStatement("DELETE FROM member_share WHERE trip_id = ?");
                delPst.setInt(1, tripId);
                delPst.executeUpdate();

                // insert updated member_share rows from table
                for (int i = 0; i < model.getRowCount(); i++) {
                    String mName = model.getValueAt(i, 0).toString();
                    double amt = Double.parseDouble(model.getValueAt(i, 1).toString());
                    double pct = Double.parseDouble(model.getValueAt(i, 2).toString());

                    PreparedStatement insPst = con.prepareStatement("INSERT INTO member_share (trip_id, member_name, amount, percentage) VALUES (?, ?, ?, ?)");
                    insPst.setInt(1, tripId);
                    insPst.setString(2, mName);
                    insPst.setDouble(3, amt);
                    insPst.setDouble(4, pct);
                    insPst.executeUpdate();
                }

                // Step 5: close the connection
                con.close();

                JOptionPane.showMessageDialog(f, "Saved successfully! Proceeding to Travel Preferences.");
                f.dispose();
                new TravelPreferencesPage(userId, tripId);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
            }
        }
        // if Next button is clicked
        else if (e.getSource() == b_next) {
            f.dispose();
            new TravelPreferencesPage(userId, tripId);
        }
        // if Back button is clicked
        else if (e.getSource() == b_back) {
            f.dispose();
            new TripOverviewPage(userId, tripId);
        }
    }
}
