package com.cabbooking.ui;

import com.cabbooking.model.Driver;
import com.cabbooking.model.Ride;
import com.cabbooking.service.DriverService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DriverDashboard extends JFrame {

    private final Driver driver;
    private final DriverService driverService = new DriverService();

    private final JToggleButton onlineToggle = new JToggleButton("Go Online");
    private final DefaultTableModel ridesModel =
            new DefaultTableModel(new String[]{"Ride ID", "Booking ID", "Status"}, 0);
    private final JLabel earningsLabel = new JLabel("Total earnings: --");

    public DriverDashboard(Driver driver) {
        this.driver = driver;
        setTitle("Driver Dashboard - " + driver.getFullName());
        setSize(600, 450);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(onlineToggle);
        top.add(earningsLabel);
        add(top, BorderLayout.NORTH);

        JTable table = new JTable(ridesModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel();
        JButton refreshBtn = new JButton("Refresh Rides");
        JButton acceptBtn = new JButton("Accept");
        JButton rejectBtn = new JButton("Reject");
        JButton startBtn = new JButton("Start Trip");
        JButton endBtn = new JButton("End Trip");
        actions.add(refreshBtn); actions.add(acceptBtn); actions.add(rejectBtn);
        actions.add(startBtn); actions.add(endBtn);
        add(actions, BorderLayout.SOUTH);

        onlineToggle.addActionListener(e -> toggleOnline());
        refreshBtn.addActionListener(e -> refreshRides());
        acceptBtn.addActionListener(e -> respond(table, true));
        rejectBtn.addActionListener(e -> respond(table, false));
        startBtn.addActionListener(e -> startTrip(table));
        endBtn.addActionListener(e -> endTrip(table));

        refreshRides();
        refreshEarnings();
    }

    private void toggleOnline() {
        try {
            if (onlineToggle.isSelected()) {
                driverService.goOnline(driver.getDriverId());
                onlineToggle.setText("Go Offline");
            } else {
                driverService.goOffline(driver.getDriverId());
                onlineToggle.setText("Go Online");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshRides() {
        ridesModel.setRowCount(0);
        try {
            List<Ride> rides = driverService.todaysRides(driver.getDriverId());
            for (Ride r : rides) {
                ridesModel.addRow(new Object[]{r.getRideId(), r.getBookingId(), r.getStatus()});
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load rides: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshEarnings() {
        try {
            double total = driverService.totalEarnings(driver.getDriverId());
            earningsLabel.setText(String.format("Total earnings: ₹%.2f", total));
        } catch (SQLException ex) {
            earningsLabel.setText("Total earnings: (error)");
        }
    }

    private int selectedRideId(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a ride first.");
            return -1;
        }
        return (int) ridesModel.getValueAt(row, 0);
    }

    private void respond(JTable table, boolean accept) {
        int rideId = selectedRideId(table);
        if (rideId == -1) return;
        try {
            driverService.respondToRide(rideId, accept);
            refreshRides();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startTrip(JTable table) {
        int rideId = selectedRideId(table);
        if (rideId == -1) return;
        try {
            driverService.startTrip(rideId);
            refreshRides();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void endTrip(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a ride first.");
            return;
        }
        int rideId = (int) ridesModel.getValueAt(row, 0);
        int bookingId = (int) ridesModel.getValueAt(row, 1);
        String distStr = JOptionPane.showInputDialog(this, "Actual distance travelled (km):");
        if (distStr == null) return;
        String fareStr = JOptionPane.showInputDialog(this, "Final fare charged (₹):");
        if (fareStr == null) return;
        try {
            double distance = Double.parseDouble(distStr);
            double fare = Double.parseDouble(fareStr);
            driverService.endTrip(rideId, distance, driver.getDriverId(), bookingId, fare);
            refreshRides();
            refreshEarnings();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid numbers.", "Validation", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
