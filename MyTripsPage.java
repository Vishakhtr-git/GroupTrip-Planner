package tripmate;

// import required swing, awt, table, sql and io packages
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.sql.*;
import java.io.*;

public class MyTripsPage implements ActionListener {

    // declare user and trip id variables
    int userId;
    int tripId;

    // declare swing components at the top
    JFrame f;
    JLabel l_title;
    JButton b_open, b_delete, b_card, b_back;
    JTable table;
    DefaultTableModel model;

    // constructor to build my trips window
    public MyTripsPage(int userId, int tripId) {
        this.userId = userId;
        this.tripId = tripId;

        // create the main window
        f = new JFrame("TripMate - My Trips");

        // create north header label
        l_title = new JLabel("Saved and Completed Trips (Select a row to Open, Delete, or Generate Card)");

        // create table and scroll pane for trips
        String[] columns = {"Trip ID", "Destination", "Start Date", "Days", "Members", "Status"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);

        // create south panel with 1 row and 4 columns for action buttons
        JPanel p_south = new JPanel();
        p_south.setLayout(new GridLayout(1, 4));

        b_open = new JButton("Open");
        b_delete = new JButton("Delete");
        b_card = new JButton("Generate Card");
        b_back = new JButton("Back");

        p_south.add(b_open);
        p_south.add(b_delete);
        p_south.add(b_card);
        p_south.add(b_back);

        // register action listeners for buttons
        b_open.addActionListener(this);
        b_delete.addActionListener(this);
        b_card.addActionListener(this);
        b_back.addActionListener(this);

        // add panels to frame using BorderLayout
        f.setLayout(new BorderLayout());
        f.add(l_title, BorderLayout.NORTH);
        f.add(sp, BorderLayout.CENTER);
        f.add(p_south, BorderLayout.SOUTH);

        // load saved and completed trips for this user
        loadData();

        // configure frame
        f.setSize(650, 450);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // helper method to load trips from trips table
    public void loadData() {
        try {
            // Step 1: get the connection from DBConnection
            Connection con = DBConnection.getConnection();

            // Step 2: create statement for saved or completed trips
            PreparedStatement pst = con.prepareStatement("SELECT trip_id, destination, start_date, days, members, status FROM trips WHERE user_id = ? AND (status = 'Saved' OR status = 'Completed')");
            pst.setInt(1, userId);

            // Step 3: execute query
            ResultSet rs = pst.executeQuery();

            // Step 4: read result into table
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("trip_id"),
                    rs.getString("destination"),
                    rs.getString("start_date"),
                    rs.getInt("days"),
                    rs.getInt("members"),
                    rs.getString("status")
                });
            }

            // Step 5: close the connection
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
        }
    }

    // helper method to generate a plain text trip card file
    public void generateCard(int tId) {
        try {
            // Step 1: get the connection from DBConnection
            Connection con = DBConnection.getConnection();

            // fetch trip details
            PreparedStatement pstTrip = con.prepareStatement("SELECT destination, start_date, days, members, travel_mode, status, note FROM trips WHERE trip_id = ?");
            pstTrip.setInt(1, tId);
            ResultSet rsTrip = pstTrip.executeQuery();

            String dest = "", sdate = "", mode = "", statusStr = "", note = "";
            int days = 0, numMembers = 0;
            if (rsTrip.next()) {
                dest = rsTrip.getString("destination");
                sdate = rsTrip.getString("start_date");
                days = rsTrip.getInt("days");
                numMembers = rsTrip.getInt("members");
                mode = rsTrip.getString("travel_mode");
                statusStr = rsTrip.getString("status");
                note = rsTrip.getString("note");
            }

            // fetch members
            PreparedStatement pstMem = con.prepareStatement("SELECT member_name FROM members WHERE trip_id = ?");
            pstMem.setInt(1, tId);
            ResultSet rsMem = pstMem.executeQuery();
            String memberList = "";
            while (rsMem.next()) {
                if (memberList.equals("")) {
                    memberList = rsMem.getString("member_name");
                } else {
                    memberList = memberList + ", " + rsMem.getString("member_name");
                }
            }

            // fetch budget
            PreparedStatement pstBud = con.prepareStatement("SELECT total, split_type, stay, vehicle, food, others FROM budget WHERE trip_id = ?");
            pstBud.setInt(1, tId);
            ResultSet rsBud = pstBud.executeQuery();
            String budgetInfo = "No budget saved";
            if (rsBud.next()) {
                budgetInfo = "Total Budget: Rs." + rsBud.getDouble("total") + " (" + rsBud.getString("split_type") + " Split)\n" +
                             "Stay: Rs." + rsBud.getDouble("stay") + ", Vehicle: Rs." + rsBud.getDouble("vehicle") +
                             ", Food: Rs." + rsBud.getDouble("food") + ", Others: Rs." + rsBud.getDouble("others");
            }

            // fetch member shares
            PreparedStatement pstShare = con.prepareStatement("SELECT member_name, amount, percentage FROM member_share WHERE trip_id = ?");
            pstShare.setInt(1, tId);
            ResultSet rsShare = pstShare.executeQuery();
            String shareInfo = "";
            while (rsShare.next()) {
                shareInfo = shareInfo + "  - " + rsShare.getString("member_name") + ": Rs." + rsShare.getDouble("amount") + " (" + rsShare.getDouble("percentage") + "%)\n";
            }

            // fetch travel preferences
            PreparedStatement pstPref = con.prepareStatement("SELECT stay, food, activities, route_from, route_via, route_to, route_type FROM travel_preferences WHERE trip_id = ?");
            pstPref.setInt(1, tId);
            ResultSet rsPref = pstPref.executeQuery();
            String stayPref = "None", foodPref = "None", actPref = "None", routePref = "None";
            if (rsPref.next()) {
                if (rsPref.getString("stay") != null) stayPref = rsPref.getString("stay");
                if (rsPref.getString("food") != null) foodPref = rsPref.getString("food");
                if (rsPref.getString("activities") != null) actPref = rsPref.getString("activities");
                String rf = rsPref.getString("route_from");
                String rv = rsPref.getString("route_via");
                String rt = rsPref.getString("route_to");
                String rtype = rsPref.getString("route_type");
                if (rf != null && rt != null) {
                    routePref = rf + " via " + rv + " to " + rt + " (" + rtype + ")";
                }
            }

            // fetch itinerary
            PreparedStatement pstItin = con.prepareStatement("SELECT day_no, time_slot, place_activity FROM itinerary WHERE trip_id = ? ORDER BY day_no, time_slot");
            pstItin.setInt(1, tId);
            ResultSet rsItin = pstItin.executeQuery();
            String itinInfo = "";
            while (rsItin.next()) {
                itinInfo = itinInfo + "  Day " + rsItin.getInt("day_no") + " [" + rsItin.getString("time_slot") + "]: " + rsItin.getString("place_activity") + "\n";
            }
            if (itinInfo.equals("")) itinInfo = "  None\n";

            // fetch packing checklist
            PreparedStatement pstPack = con.prepareStatement("SELECT item_name, packed FROM packing_checklist WHERE trip_id = ?");
            pstPack.setInt(1, tId);
            ResultSet rsPack = pstPack.executeQuery();
            String packInfo = "";
            while (rsPack.next()) {
                packInfo = packInfo + "  [" + rsPack.getString("packed") + "] " + rsPack.getString("item_name") + "\n";
            }
            if (packInfo.equals("")) packInfo = "  None\n";

            // close database connection
            con.close();

            // create text file with FileWriter
            File file = new File("trip_card_" + tId + ".txt");
            FileWriter fw = new FileWriter(file);

            fw.write("=========================================\n");
            fw.write("         TRIPMATE TRIP CARD              \n");
            fw.write("=========================================\n\n");
            fw.write("Trip ID: " + tId + "\n");
            fw.write("Destination: " + dest + "\n");
            fw.write("Start Date: " + sdate + "\n");
            fw.write("Duration: " + days + " days\n");
            fw.write("Travel Mode: " + mode + "\n");
            fw.write("Status: " + statusStr + "\n");
            fw.write("Members (" + numMembers + "): " + memberList + "\n\n");

            fw.write("--- BUDGET & EXPENSE SPLIT ---\n");
            fw.write(budgetInfo + "\n");
            if (!shareInfo.equals("")) {
                fw.write("Member Shares:\n" + shareInfo);
            }
            fw.write("\n");

            fw.write("--- TRAVEL PREFERENCES ---\n");
            fw.write("Stay: " + stayPref + "\n");
            fw.write("Foods: " + foodPref + "\n");
            fw.write("Activities: " + actPref + "\n");
            fw.write("Route: " + routePref + "\n\n");

            fw.write("--- ITINERARY ---\n");
            fw.write(itinInfo + "\n");

            fw.write("--- PACKING CHECKLIST ---\n");
            fw.write(packInfo + "\n");

            fw.write("--- NOTE ---\n");
            fw.write((note != null && !note.equals("") ? note : "No note added") + "\n\n");
            fw.write("=========================================\n");

            fw.close();

            // show popup with file path
            JOptionPane.showMessageDialog(f, "Trip card generated successfully!\nSaved at: " + file.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
        }
    }

    // handle button click events
    public void actionPerformed(ActionEvent e) {
        // if Open button is clicked
        if (e.getSource() == b_open) {
            int sel = table.getSelectedRow();
            if (sel == -1) {
                JOptionPane.showMessageDialog(f, "Select a trip from the table to open");
                return;
            }
            int selectedTripId = Integer.parseInt(model.getValueAt(sel, 0).toString());
            // open dashboard with the selected trip
            f.dispose();
            new Dashboard(userId, selectedTripId);
        }
        // if Delete button is clicked
        else if (e.getSource() == b_delete) {
            int sel = table.getSelectedRow();
            if (sel == -1) {
                JOptionPane.showMessageDialog(f, "Select a trip from the table to delete");
                return;
            }
            int selectedTripId = Integer.parseInt(model.getValueAt(sel, 0).toString());

            try {
                // Step 1: get the connection from DBConnection
                Connection con = DBConnection.getConnection();

                // Step 2: delete related records across all tables
                PreparedStatement p1 = con.prepareStatement("DELETE FROM members WHERE trip_id = ?");
                p1.setInt(1, selectedTripId);
                p1.executeUpdate();

                PreparedStatement p2 = con.prepareStatement("DELETE FROM budget WHERE trip_id = ?");
                p2.setInt(1, selectedTripId);
                p2.executeUpdate();

                PreparedStatement p3 = con.prepareStatement("DELETE FROM member_share WHERE trip_id = ?");
                p3.setInt(1, selectedTripId);
                p3.executeUpdate();

                PreparedStatement p4 = con.prepareStatement("DELETE FROM travel_preferences WHERE trip_id = ?");
                p4.setInt(1, selectedTripId);
                p4.executeUpdate();

                PreparedStatement p5 = con.prepareStatement("DELETE FROM itinerary WHERE trip_id = ?");
                p5.setInt(1, selectedTripId);
                p5.executeUpdate();

                PreparedStatement p6 = con.prepareStatement("DELETE FROM packing_checklist WHERE trip_id = ?");
                p6.setInt(1, selectedTripId);
                p6.executeUpdate();

                // delete main trip record
                PreparedStatement p7 = con.prepareStatement("DELETE FROM trips WHERE trip_id = ?");
                p7.setInt(1, selectedTripId);
                int rows = p7.executeUpdate();

                // Step 5: close the connection
                con.close();

                if (rows > 0) {
                    JOptionPane.showMessageDialog(f, "Trip deleted successfully");
                    model.removeRow(sel);
                    // if currently active trip was deleted, reset tripId
                    if (selectedTripId == tripId) {
                        tripId = 0;
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
            }
        }
        // if Generate Card button is clicked
        else if (e.getSource() == b_card) {
            int sel = table.getSelectedRow();
            if (sel == -1) {
                JOptionPane.showMessageDialog(f, "Select a trip from the table to generate card");
                return;
            }
            int selectedTripId = Integer.parseInt(model.getValueAt(sel, 0).toString());
            generateCard(selectedTripId);
        }
        // if Back button is clicked
        else if (e.getSource() == b_back) {
            f.dispose();
            new Dashboard(userId, tripId);
        }
    }
}
