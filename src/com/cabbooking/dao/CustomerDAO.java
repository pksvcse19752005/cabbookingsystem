package com.cabbooking.dao;

import com.cabbooking.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAO {

    public int register(Customer c) throws SQLException {
        String sql = "INSERT INTO Customer (full_name, email, phone, password_hash, " +
                "security_question, security_answer_hash) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getFullName());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getPasswordHash());
            ps.setString(5, c.getSecurityQuestion());
            ps.setString(6, c.getSecurityAnswerHash());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public Optional<Customer> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Customer WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Customer> findById(int customerId) throws SQLException {
        String sql = "SELECT * FROM Customer WHERE customer_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public List<Customer> findAll(int limit, int offset) throws SQLException {
        String sql = "SELECT * FROM Customer ORDER BY customer_id LIMIT ? OFFSET ?";
        List<Customer> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public boolean updateProfile(Customer c) throws SQLException {
        String sql = "UPDATE Customer SET full_name=?, phone=? WHERE customer_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getFullName());
            ps.setString(2, c.getPhone());
            ps.setInt(3, c.getCustomerId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int customerId, String newHash) throws SQLException {
        String sql = "UPDATE Customer SET password_hash=? WHERE customer_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, customerId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int customerId, String status) throws SQLException {
        String sql = "UPDATE Customer SET status=? WHERE customer_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, customerId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean adjustWallet(int customerId, double delta) throws SQLException {
        String sql = "UPDATE Customer SET wallet_balance = wallet_balance + ? WHERE customer_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, delta);
            ps.setInt(2, customerId);
            return ps.executeUpdate() > 0;
        }
    }

    private Customer map(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setFullName(rs.getString("full_name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setPasswordHash(rs.getString("password_hash"));
        c.setSecurityQuestion(rs.getString("security_question"));
        c.setSecurityAnswerHash(rs.getString("security_answer_hash"));
        c.setWalletBalance(rs.getDouble("wallet_balance"));
        c.setRewardPoints(rs.getInt("reward_points"));
        c.setStatus(rs.getString("status"));
        return c;
    }
}
