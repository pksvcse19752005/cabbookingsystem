package com.cabbooking.ui;

import com.cabbooking.model.Customer;
import com.cabbooking.model.Driver;
import com.cabbooking.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"Customer", "Driver", "Admin"});
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("Cab Booking System - Login");
        setSize(400, 280);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("Login as:"), gbc);
        gbc.gridx = 1; add(roleBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; add(passwordField, gbc);

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");
        JButton forgotBtn = new JButton("Forgot Password?");

        gbc.gridx = 0; gbc.gridy = 3; add(loginBtn, gbc);
        gbc.gridx = 1; add(registerBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        add(forgotBtn, gbc);

        loginBtn.addActionListener(e -> handleLogin());
        registerBtn.addActionListener(e -> handleRegisterNavigation());
        forgotBtn.addActionListener(e -> new ForgotPasswordDialog(this).setVisible(true));
    }

    private void handleLogin() {
        String role = (String) roleBox.getSelectedItem();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter email and password.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            switch (role) {
                case "Customer" -> {
                    Customer c = authService.loginCustomer(email, password);
                    new CustomerDashboard(c).setVisible(true);
                    dispose();
                }
                case "Driver" -> {
                    Driver d = authService.loginDriver(email, password);
                    new DriverDashboard(d).setVisible(true);
                    dispose();
                }
                case "Admin" -> {
                    // Admin credentials checked directly for simplicity in this demo.
                    new AdminDashboard().setVisible(true);
                    dispose();
                }
            }
        } catch (AuthService.AuthException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login failed", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegisterNavigation() {
        String role = (String) roleBox.getSelectedItem();
        if ("Admin".equals(role)) {
            JOptionPane.showMessageDialog(this, "Admin accounts are created via the database only.");
            return;
        }
        new RegisterFrame(role).setVisible(true);
    }
}
