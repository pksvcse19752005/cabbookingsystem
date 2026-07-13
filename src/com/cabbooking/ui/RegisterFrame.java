package com.cabbooking.ui;

import com.cabbooking.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class RegisterFrame extends JFrame {

    private final String role;
    private final AuthService authService = new AuthService();

    private final JTextField nameField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JTextField phoneField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    // Customer-only
    private final JTextField securityQField = new JTextField(18);
    private final JTextField securityAField = new JTextField(18);

    // Driver-only
    private final JTextField licenseField = new JTextField(18);
    private final JTextField cityField = new JTextField(18);

    public RegisterFrame(String role) {
        this.role = role;
        setTitle(role + " Registration");
        setSize(420, role.equals("Customer") ? 420 : 420);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        row = addRow(gbc, row, "Full Name:", nameField);
        row = addRow(gbc, row, "Email:", emailField);
        row = addRow(gbc, row, "Phone (10 digits):", phoneField);
        row = addRow(gbc, row, "Password:", passwordField);

        if ("Customer".equals(role)) {
            row = addRow(gbc, row, "Security Question:", securityQField);
            row = addRow(gbc, row, "Security Answer:", securityAField);
        } else { // Driver
            row = addRow(gbc, row, "License Number:", licenseField);
            row = addRow(gbc, row, "City:", cityField);
        }

        JButton submitBtn = new JButton("Register");
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        add(submitBtn, gbc);

        submitBtn.addActionListener(e -> handleSubmit());
    }

    private int addRow(GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; add(new JLabel(label), gbc);
        gbc.gridx = 1; add(field, gbc);
        return row + 1;
    }

    private void handleSubmit() {
        try {
            if ("Customer".equals(role)) {
                authService.registerCustomer(
                        nameField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        new String(passwordField.getPassword()),
                        securityQField.getText().trim(),
                        securityAField.getText().trim());
            } else {
                authService.registerDriver(
                        nameField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        new String(passwordField.getPassword()),
                        licenseField.getText().trim(),
                        cityField.getText().trim());
                JOptionPane.showMessageDialog(this,
                        "Registered. Your account needs admin approval before you can log in.",
                        "Pending Approval", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                return;
            }
            JOptionPane.showMessageDialog(this, "Registration successful. You can now log in.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (AuthService.AuthException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
