package com.cabbooking.service;

import com.cabbooking.dao.CustomerDAO;
import com.cabbooking.dao.DriverDAO;
import com.cabbooking.model.Customer;
import com.cabbooking.model.Driver;
import com.cabbooking.util.PasswordUtil;
import com.cabbooking.util.ValidationUtil;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final DriverDAO driverDAO = new DriverDAO();

    public static class AuthException extends Exception {
        public AuthException(String message) { super(message); }
    }

    // ---------- Customer ----------

    public Customer registerCustomer(String fullName, String email, String phone,
                                      String plainPassword, String securityQ, String securityA)
            throws AuthException, SQLException {
        if (!ValidationUtil.isNonEmpty(fullName)) throw new AuthException("Name required.");
        if (!ValidationUtil.isValidEmail(email)) throw new AuthException("Invalid email.");
        if (!ValidationUtil.isValidPhone(phone)) throw new AuthException("Phone must be 10 digits.");
        if (!ValidationUtil.isStrongPassword(plainPassword))
            throw new AuthException("Password needs 8+ chars, upper/lower/digit.");
        if (customerDAO.findByEmail(email).isPresent())
            throw new AuthException("Email already registered.");

        Customer c = new Customer();
        c.setFullName(fullName);
        c.setEmail(email);
        c.setPhone(phone);
        c.setPasswordHash(PasswordUtil.hash(plainPassword));
        c.setSecurityQuestion(securityQ);
        c.setSecurityAnswerHash(PasswordUtil.hash(securityA.toLowerCase().trim()));
        int id = customerDAO.register(c);
        c.setCustomerId(id);
        return c;
    }

    public Customer loginCustomer(String email, String plainPassword) throws AuthException, SQLException {
        Customer c = customerDAO.findByEmail(email)
                .orElseThrow(() -> new AuthException("No account with that email."));
        if (!"ACTIVE".equals(c.getStatus())) throw new AuthException("Account is blocked. Contact support.");
        if (!PasswordUtil.verify(plainPassword, c.getPasswordHash()))
            throw new AuthException("Incorrect password.");
        return c;
    }

    public boolean resetCustomerPassword(String email, String securityAnswer, String newPassword)
            throws AuthException, SQLException {
        Customer c = customerDAO.findByEmail(email)
                .orElseThrow(() -> new AuthException("No account with that email."));
        if (!PasswordUtil.verify(securityAnswer.toLowerCase().trim(), c.getSecurityAnswerHash()))
            throw new AuthException("Security answer incorrect.");
        if (!ValidationUtil.isStrongPassword(newPassword))
            throw new AuthException("Password needs 8+ chars, upper/lower/digit.");
        return customerDAO.updatePassword(c.getCustomerId(), PasswordUtil.hash(newPassword));
    }

    // ---------- Driver ----------

    public Driver registerDriver(String fullName, String email, String phone, String plainPassword,
                                  String licenseNumber, String city) throws AuthException, SQLException {
        if (!ValidationUtil.isValidEmail(email)) throw new AuthException("Invalid email.");
        if (!ValidationUtil.isValidPhone(phone)) throw new AuthException("Phone must be 10 digits.");
        if (!ValidationUtil.isStrongPassword(plainPassword))
            throw new AuthException("Password needs 8+ chars, upper/lower/digit.");
        if (driverDAO.findByEmail(email).isPresent())
            throw new AuthException("Email already registered.");

        Driver d = new Driver();
        d.setFullName(fullName);
        d.setEmail(email);
        d.setPhone(phone);
        d.setPasswordHash(PasswordUtil.hash(plainPassword));
        d.setLicenseNumber(licenseNumber);
        d.setCity(city);
        d.setApprovalStatus("PENDING");
        int id = driverDAO.register(d);
        d.setDriverId(id);
        return d;
    }

    public Driver loginDriver(String email, String plainPassword) throws AuthException, SQLException {
        Driver d = driverDAO.findByEmail(email)
                .orElseThrow(() -> new AuthException("No account with that email."));
        if (!PasswordUtil.verify(plainPassword, d.getPasswordHash()))
            throw new AuthException("Incorrect password.");
        if ("PENDING".equals(d.getApprovalStatus()))
            throw new AuthException("Account pending admin approval.");
        if ("REJECTED".equals(d.getApprovalStatus()))
            throw new AuthException("Account rejected. Contact support.");
        return d;
    }
}
