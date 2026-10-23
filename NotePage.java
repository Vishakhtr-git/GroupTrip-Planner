package tripmate;

// import required swing, awt, sql and io packages
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
import java.io.*;

public class NotePage implements ActionListener {

    // declare user and trip id variables
    int userId;
    int tripId;

    // declare swing components at the top
    JFrame f;
    JLabel l1;
    JTextArea t_note;
    JButton b_ok, b_back;

    // constructor to build note window
    public NotePage(int userId, int tripId) {
        this.userId = userId;
        this.tripId = tripId;

        // create the main window
        f = new JFrame("TripMate - Trip Note and Completion");

        // create header label
        l1 = new JLabel("Enter a note or memory about the trip:");

        // create text area inside scroll pane for trip notes
        t_note = new JTextArea(8, 25);
        JScrollPane sp = new JScrollPane(t_note);

        // create button panel with 1 row and 2 columns
        JPanel p_south = new JPanel();
        p_south.setLayout(new GridLayout(1, 2));

        b_ok = new JButton("OK");
        b_back = new JButton("Back");

        p_south.add(b_ok);
        p_south.add(b_back);

        // register action listeners for buttons
        b_ok.addActionListener(this);
        b_back.addActionListener(this);

        // add components to frame using BorderLayout
        f.setLayout(new BorderLayout());
        f.add(l1, BorderLayout.NORTH);
        f.add(sp, BorderLayout.CENTER);
        f.add(p_south, BorderLayout.SOUTH);

        // load existing note if any
        loadNote();

        // configure frame
        f.setSize(450, 300);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // helper method to load saved note from trips table
    public void loadNote() {
        try {
            // Step 1: get the connection from DBConnection
            Connection con = DBConnection.getConnection();

            // Step 2: create statement
            PreparedStatement pst = con.prepareStatement("SELECT note FROM trips WHERE trip_id = ?");
            pst.setInt(1, tripId);

            // Step 3: execute query
            ResultSet rs = pst.executeQuery();

            // Step 4: read result
            if (rs.next()) {
                String note = rs.getString("note");
                if (note != null) {
                    t_note.setText(note);
                }
            }

            // Step 5: close the connection
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
        }
    }

    // helper method to write the trip card text file
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

            // fetch member names
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

            // fetch budget details
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

            // fetch itinerary items
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

            // write text file using FileWriter
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

            // show location in message box
            JOptionPane.showMessageDialog(f, "Trip card generated successfully!\nSaved at: " + file.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
        }
    }

    // handle button click events
    public void actionPerformed(ActionEvent e) {
        // if OK button is clicked
        if (e.getSource() == b_ok) {
            String note = t_note.getText();

            try {
                // Step 1: get the connection from DBConnection
                Connection con = DBConnection.getConnection();

                // Step 2: save note and set status to Completed
                PreparedStatement pst = con.prepareStatement("UPDATE trips SET note = ?, status = 'Completed' WHERE trip_id = ?");
                pst.setString(1, note);
                pst.setInt(2, tripId);

                // Step 3: execute query
                int rows = pst.executeUpdate();

                // Step 5: close the connection
                con.close();

                if (rows > 0) {
                    JOptionPane.showMessageDialog(f, "Trip marked as Completed!");

                    // prompt user to generate trip card
                    int choice = JOptionPane.showConfirmDialog(f, "Generate trip card?", "Trip Card", JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        generateCard(tripId);
                    }

                    // return to dashboard
                    f.dispose();
                    new Dashboard(userId, tripId);
                } else {
                    JOptionPane.showMessageDialog(f, "Failed to update trip status");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(f, "Error: " + ex.getMessage());
            }
        }
        // if Back button is clicked
        else if (e.getSource() == b_back) {
            f.dispose();
            new PackingChecklistPage(userId, tripId);
        }
    }
}
