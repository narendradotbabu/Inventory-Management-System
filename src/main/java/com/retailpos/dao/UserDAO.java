package com.retailpos.dao;

import com.retailpos.model.User;
import com.retailpos.util.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private Connection conn() { return DatabaseHelper.getInstance().getConnection(); }

    public User authenticate(String username, String password) {
        try {
            PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM users WHERE username=? AND password=?");
            ps.setString(1, username); ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        try {
            ResultSet rs = conn().createStatement().executeQuery("SELECT * FROM users ORDER BY id");
            while (rs.next()) list.add(mapUser(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean addUser(User u) {
        try {
            PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO users(name,username,password,role) VALUES(?,?,?,?)");
            ps.setString(1, u.getName()); ps.setString(2, u.getUsername());
            ps.setString(3, u.getPassword()); ps.setString(4, u.getRole());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateUser(User u) {
        try {
            PreparedStatement ps = conn().prepareStatement(
                "UPDATE users SET name=?,username=?,password=?,role=? WHERE id=?");
            ps.setString(1, u.getName()); ps.setString(2, u.getUsername());
            ps.setString(3, u.getPassword()); ps.setString(4, u.getRole());
            ps.setInt(5, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteUser(int id) {
        try {
            PreparedStatement ps = conn().prepareStatement("DELETE FROM users WHERE id=?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean usernameExists(String username, Integer excludeUserId) {
        try {
            String sql = excludeUserId == null
                ? "SELECT COUNT(*) FROM users WHERE username=?"
                : "SELECT COUNT(*) FROM users WHERE username=? AND id<>?";
            PreparedStatement ps = conn().prepareStatement(sql);
            ps.setString(1, username);
            if (excludeUserId != null) ps.setInt(2, excludeUserId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean hasSales(int userId) {
        try {
            PreparedStatement ps = conn().prepareStatement(
                "SELECT COUNT(*) FROM sales WHERE cashier_id=?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("name"),
            rs.getString("username"), rs.getString("password"), rs.getString("role"));
    }
}
