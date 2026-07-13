package com.cabbooking.ui;

import com.cabbooking.dao.BookingDAO;
import com.cabbooking.model.Booking;
import com.cabbooking.model.Driver;
import com.cabbooking.service.DriverService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class AdminDashboard extends JFrame {

    private final DriverService driverService = new DriverService();
    private final BookingDAO bookingDAO = new BookingDAO();

    private final DefaultTableModel pendingModel =
            new DefaultTableModel(new String[]{"Driver ID", "Name", "Email", "License", "City"}, 0);
    private final DefaultTableModel bookingModel =
            new DefaultTableModel(new String[]{"Booking ID", "Customer", "Route", "Fare", "Status"}, 0);

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(750, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Driver Approvals", buildApprovalsPanel());
        tabs.addTab("All Bookings", buildBookingsPanel());
        add(tabs);

        refreshPending();
        refreshBookings("PENDING");
    }

    private JPanel buildApprovalsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTable table = new JTable(pendingModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
        JButton approveBtn = new JButton("Approve");
        JButton rejectBtn = new JButton("Reject");
        actions.add(refreshBtn); actions.add(approveBtn); actions.add(rejectBtn);
        panel.add(actions, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> refreshPending());
        approveBtn.addActionListener(e -> handleApproval(table, true));
        rejectBtn.addActionListener(e -> handleApproval(table, false));

        return panel;
    }

    private JPanel buildBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTable table = new JTable(bookingModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel filterPanel = new JPanel();
        JComboBox<String> statusFilter = new JComboBox<>(
                new String[]{"PENDING", "ASSIGNED", "CONFIRMED", "COMPLETED", "CANCELLED"});
        JButton filterBtn = new JButton("Filter");
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusFilter);
        filterPanel.add(filterBtn);
        panel.add(filterPanel, BorderLayout.NORTH);

        filterBtn.addActionListener(e -> refreshBookings((String) statusFilter.getSelectedItem()));

        return panel;
    }

    private void refreshPending() {
        pendingModel.setRowCount(0);
        try {
            List<Driver> pending = driverService.pendingApprovals();
            for (Driver d : pending) {
                pendingModel.addRow(new Object[]{
                        d.getDriverId(), d.getFullName(), d.getEmail(), d.getLicenseNumber(), d.getCity()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load drivers: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshBookings(String status) {
        bookingModel.setRowCount(0);
        try {
            List<Booking> bookings = bookingDAO.findByStatus(status);
            for (Booking b : bookings) {
                bookingModel.addRow(new Object[]{
                        b.getBookingId(), b.getCustomerId(),
                        b.getPickupAddress() + " -> " + b.getDropAddress(),
                        String.format("₹%.2f", b.getEstimatedFare()),
                        b.getStatus()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load bookings: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleApproval(JTable table, boolean approve) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a driver first.");
            return;
        }
        int driverId = (int) pendingModel.getValueAt(row, 0);
        try {
            if (approve) driverService.approveDriver(driverId);
            else driverService.rejectDriver(driverId);
            refreshPending();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
