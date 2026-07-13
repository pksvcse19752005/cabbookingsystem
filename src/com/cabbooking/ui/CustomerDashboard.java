package com.cabbooking.ui;

import com.cabbooking.dao.CustomerDAO;
import com.cabbooking.model.*;
import com.cabbooking.service.BookingService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CustomerDashboard extends JFrame {

    private final Customer customer;
    private final BookingService bookingService = new BookingService();
    private final CustomerDAO customerDAO = new CustomerDAO();

    // Booking tab fields
    private final JTextField pickupField = new JTextField(20);
    private final JTextField dropField = new JTextField(20);
    private final JTextField pickupLatField = new JTextField("12.9716", 6);
    private final JTextField pickupLngField = new JTextField("77.5946", 6);
    private final JTextField dropLatField = new JTextField("12.9352", 6);
    private final JTextField dropLngField = new JTextField("77.6245", 6);
    private final JComboBox<String> categoryBox =
            new JComboBox<>(new String[]{"Mini", "Sedan", "SUV", "Luxury"});
    private final JComboBox<String> paymentBox =
            new JComboBox<>(new String[]{"Cash", "Card", "UPI", "Wallet"});
    private final JLabel fareLabel = new JLabel("Estimated fare: --");

    private final DefaultTableModel historyModel =
            new DefaultTableModel(new String[]{"Booking ID", "Route", "Fare", "Status"}, 0);

    // Category rates matching CabCategory seed data (categoryId 1-4)
    private static final CabCategory[] CATEGORIES = {
            new CabCategory(1, "Mini", 30, 8, 1, 4),
            new CabCategory(2, "Sedan", 50, 11, 1.5, 4),
            new CabCategory(3, "SUV", 80, 14, 2, 6),
            new CabCategory(4, "Luxury", 150, 22, 3, 4)
    };

    public CustomerDashboard(Customer customer) {
        this.customer = customer;
        setTitle("Customer Dashboard - " + customer.getFullName());
        setSize(650, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Book a Ride", buildBookingPanel());
        tabs.addTab("Booking History", buildHistoryPanel());
        add(tabs);

        refreshHistory();
    }

    private JPanel buildBookingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Pickup Address:"), gbc);
        gbc.gridx = 1; panel.add(pickupField, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Drop Address:"), gbc);
        gbc.gridx = 1; panel.add(dropField, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Pickup Lat/Lng:"), gbc);
        JPanel pickCoord = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pickCoord.add(pickupLatField); pickCoord.add(pickupLngField);
        gbc.gridx = 1; panel.add(pickCoord, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Drop Lat/Lng:"), gbc);
        JPanel dropCoord = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        dropCoord.add(dropLatField); dropCoord.add(dropLngField);
        gbc.gridx = 1; panel.add(dropCoord, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Cab Category:"), gbc);
        gbc.gridx = 1; panel.add(categoryBox, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Payment Method:"), gbc);
        gbc.gridx = 1; panel.add(paymentBox, gbc); row++;

        JButton estimateBtn = new JButton("Estimate Fare");
        gbc.gridx = 0; gbc.gridy = row; panel.add(estimateBtn, gbc);
        gbc.gridx = 1; panel.add(fareLabel, gbc); row++;

        JButton bookBtn = new JButton("Book Now");
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(bookBtn, gbc);

        estimateBtn.addActionListener(e -> estimateFare());
        bookBtn.addActionListener(e -> handleBooking());

        return panel;
    }

    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTable table = new JTable(historyModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
        JButton cancelBtn = new JButton("Cancel Selected");
        JButton rateBtn = new JButton("Rate Driver");
        actions.add(refreshBtn); actions.add(cancelBtn); actions.add(rateBtn);
        panel.add(actions, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> refreshHistory());
        cancelBtn.addActionListener(e -> handleCancel(table));
        rateBtn.addActionListener(e -> handleRate(table));

        return panel;
    }

    private CabCategory selectedCategory() {
        return CATEGORIES[categoryBox.getSelectedIndex()];
    }

    private void estimateFare() {
        try {
            double lat1 = Double.parseDouble(pickupLatField.getText());
            double lng1 = Double.parseDouble(pickupLngField.getText());
            double lat2 = Double.parseDouble(dropLatField.getText());
            double lng2 = Double.parseDouble(dropLngField.getText());
            double distance = bookingService.calculateDistanceKm(lat1, lng1, lat2, lng2);
            double fare = bookingService.estimateFare(selectedCategory(), distance);
            fareLabel.setText(String.format("Estimated fare: ₹%.2f (%.1f km)", fare, distance));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid coordinates.", "Validation", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleBooking() {
        if (pickupField.getText().isBlank() || dropField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter pickup and drop addresses.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            double lat1 = Double.parseDouble(pickupLatField.getText());
            double lng1 = Double.parseDouble(pickupLngField.getText());
            double lat2 = Double.parseDouble(dropLatField.getText());
            double lng2 = Double.parseDouble(dropLngField.getText());
            double distance = bookingService.calculateDistanceKm(lat1, lng1, lat2, lng2);
            CabCategory category = selectedCategory();
            double fare = bookingService.estimateFare(category, distance);

            Booking b = new Booking();
            b.setCustomerId(customer.getCustomerId());
            b.setCategoryId(category.getCategoryId());
            b.setPickupAddress(pickupField.getText().trim());
            b.setDropAddress(dropField.getText().trim());
            b.setPickupLat(lat1); b.setPickupLng(lng1);
            b.setDropLat(lat2); b.setDropLng(lng2);
            b.setDistanceKm(distance);
            b.setEstimatedFare(fare);

            int bookingId = bookingService.createBooking(b);

            // NOTE: city is hardcoded here for demo purposes; a production build
            // would derive it from the pickup address or a geocoding lookup.
            Optional<Driver> assigned = bookingService.autoAssignDriver(bookingId, category.getCategoryId(), "Bangalore");

            if (assigned.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Booking created (#" + bookingId + ") but no drivers are available right now.",
                        "No Driver Available", JOptionPane.WARNING_MESSAGE);
            } else {
                bookingService.confirmBooking(bookingId);
                JOptionPane.showMessageDialog(this,
                        "Booking #" + bookingId + " confirmed!\nDriver: " + assigned.get().getFullName()
                                + "\nFare: ₹" + fare,
                        "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshHistory();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid coordinates.", "Validation", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshHistory() {
        historyModel.setRowCount(0);
        try {
            List<Booking> bookings = bookingService.customerHistory(customer.getCustomerId(), 0, 50);
            for (Booking b : bookings) {
                historyModel.addRow(new Object[]{
                        b.getBookingId(),
                        b.getPickupAddress() + " -> " + b.getDropAddress(),
                        String.format("₹%.2f", b.getEstimatedFare()),
                        b.getStatus()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load history: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCancel(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a booking first.");
            return;
        }
        int bookingId = (int) historyModel.getValueAt(row, 0);
        try {
            double fee = bookingService.cancelBooking(bookingId);
            JOptionPane.showMessageDialog(this,
                    fee > 0 ? "Cancelled with fee ₹" + fee : "Cancelled free of charge.");
            refreshHistory();
        } catch (SQLException | BookingService.ServiceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRate(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a completed booking first.");
            return;
        }
        int bookingId = (int) historyModel.getValueAt(row, 0);
        String status = (String) historyModel.getValueAt(row, 3);
        if (!"COMPLETED".equals(status)) {
            JOptionPane.showMessageDialog(this, "Only completed rides can be rated.");
            return;
        }
        try {
            Optional<Ride> ride = bookingService.getRide(bookingId);
            if (ride.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No ride record found.");
                return;
            }
            String starsStr = JOptionPane.showInputDialog(this, "Rate driver (1-5 stars):");
            if (starsStr == null) return;
            int stars = Integer.parseInt(starsStr.trim());
            if (stars < 1 || stars > 5) throw new NumberFormatException();
            String comment = JOptionPane.showInputDialog(this, "Optional review comment:");
            bookingService.rateDriver(bookingId, customer.getCustomerId(), ride.get().getDriverId(), stars, comment);
            JOptionPane.showMessageDialog(this, "Thanks for your feedback!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a whole number 1-5.", "Validation", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
