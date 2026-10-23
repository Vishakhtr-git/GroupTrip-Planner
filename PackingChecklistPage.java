package tripmate;

// import required swing, awt and sql packages
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.sql.*;

public class PackingChecklistPage implements ActionListener {

    // declare user and trip id variables
    int userId;
    int tripId;
    int editingRow = -1;

    // declare swing components at the top
    JFrame f;
    JLabel l1, status;
    JTextField t_item;
    JButton b_add, b_remove, b_packed, b_edit, b_save, b_next, b_back;
    JTable table;
    DefaultTableModel model;

    // constructor to build packing checklist window
    public PackingChecklistPage(int userId, int tripId) {
        this.userId = userId;
        this.tripId = tripId;

        // create the main window
        f = new JFrame("TripMate - Packing Checklist");

        // create north panel with 5 rows and 2 columns
        JPanel p_north = new JPanel();
        p_north.setLayout(new GridLayout(5, 2));

        // create item name label and text field
        l1 = new JLabel("Item Name:");
        t_item = new JTextField();

        // create buttons
        b_add = new JButton("Add Item");
        b_remove = new JButton("Remove Item");

        b_packed = new JButton("Mark Packed");
        b_edit = new JButton("Edit Item");

        b_save = new JButton("Save");
        b_next = new JButton("Next: Finish Trip");

        b_back = new JButton("Back");
        status = new JLabel("Status: Ready");

        // add components to north panel
        p_north.add(l1);
        p_north.add(t_item);
        p_north.add(b_add);
        p_north.add(b_remove);
        p_north.add(b_packed);
        p_north.add(b_edit);
        p_north.add(b_save);
        p_north.add(b_next);
        p_north.add(b_back);
        p_north.add(status);

        // register action listeners for buttons
        b_add.addActionListener(this);
        b_remove.addActionListener(this);
        b_packed.addActionListener(this);
        b_edit.addActionListener(this);
        b_save.addActionListener(this);
        b_next.addActionListener(this);
        b_back.addActionListener(this);

        // create table and scroll pane for checklist
        String[] columns = {"Item", "Packed"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);

        // add panels to frame using BorderLayout
        f.setLayout(new BorderLayout());
        f.add(p_north, BorderLayout.NORTH);
        f.add(sp, BorderLayout.CENTER);

        // load saved checklist items
        loadData();

        // configure frame
        f.setSize(500, 480);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // helper method to make fields and buttons editable or read-only
    public void setFieldsEditable(boolean b) {
        t_item.setEditable(b);
        b_add.setEnabled(b);
        b_remove.setEnabled(b);
        b_packed.setEnabled(b);
    }

    // helper method to load checklist items from database
    public void loadData() {
        try {
            // Step 1: get the connection from DBConnection
            Connection con = DBConnection.getConnection();

            // Step 2: create statement
            PreparedStatement pst = con.prepareStatement("SELECT item_name, packed FROM packing_checklist WHERE trip_id = ?");
            pst.setInt(1, tripId);

            // Step 3: execute query
            ResultSet rs = pst.executeQuery();

            // Step 4: read result into table
            model.setRowCount(0);
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                model.addRow(new Object[]{rs.getString("item_name"), rs.getString("packed")});
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
        // if Add Item button is clicked
        if (e.getSource() == b_add) {
            String item = t_item.getText();
            if (item.equals("")) {
                JOptionPane.showMessageDialog(f, "Please fill all fields");
                return;
            }

            // if updating an existing row
            if (editingRow != -1) {
                model.setValueAt(item, editingRow, 0);
                editingRow = -1;
                b_add.setText("Add Item");
            } else {
                // add new row with default 'No'
                model.addRow(new Object[]{item, "No"});
            }

            // clear text field
            t_item.setText("");
        }
        // if Remove Item button is clicked
        else if (e.getSource() == b_remove) {
            int sel = table.getSelectedRow();
            if (sel == -1) {
                JOptionPane.showMessageDialog(f, "Select an item to remove");
                return;
            }
            model.removeRow(sel);
        }
        // if Mark Packed button is clicked
        else if (e.getSource() == b_packed) {
            int sel = table.getSelectedRow();
            if (sel == -1) {
                JOptionPane.showMessageDialog(f, "Select an item to toggle packed status");
                return;
            }
            // toggle between Yes and No
            String current = model.getValueAt(sel, 1).toString();
            if (current.equalsIgnoreCase("Yes")) {
                model.setValueAt("No", sel, 1);
            } else {
                model.setValueAt("Yes", sel, 1);
            }
        }
        // if Edit Item button is clicked
        else if (e.getSource() == b_edit) {
            int sel = table.getSelectedRow();
            if (sel == -1) {
                JOptionPane.showMessageDialog(f, "Select an item to edit");
                return;
            }
            // load item name into field
            t_item.setText(model.getValueAt(sel, 0).toString());
            editingRow = sel;
            b_add.setText("Update Item");
            setFieldsEditable(true);
        }
        // if Save button is clicked
        else if (e.getSource() == b_save) {
            try {
                // Step 1: get the connection from DBConnection
                Connection con = DBConnection.getConnection();

                // Step 2: delete old items for this trip
                PreparedStatement delPst = con.prepareStatement("DELETE FROM packing_checklist WHERE trip_id = ?");
                delPst.setInt(1, tripId);
                delPst.executeUpdate();

                // Step 3: insert rows from table
                for (int i = 0; i < model.getRowCount(); i++) {
                    String item = model.getValueAt(i, 0).toString();
                    String packed = model.getValueAt(i, 1).toString();

                    PreparedStatement insPst = con.prepareStatement("INSERT INTO packing_checklist (trip_id, item_name, packed) VALUES (?, ?, ?)");
                    insPst.setInt(1, tripId);
                    insPst.setString(2, item);
                    insPst.setString(3, packed);
                    insPst.executeUpdate();
                }

                // Step 5: close the connection
                con.close();

                JOptionPane.showMessageDialog(f, "Saved successfully! Proceeding to Trip Completion.");
                f.dispose();
                new NotePage(userId, tripId);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
            }
        }
        // if Next button is clicked
        else if (e.getSource() == b_next) {
            f.dispose();
            new NotePage(userId, tripId);
        }
        // if Back button is clicked
        else if (e.getSource() == b_back) {
            f.dispose();
            new ItineraryPage(userId, tripId);
        }
    }
}
