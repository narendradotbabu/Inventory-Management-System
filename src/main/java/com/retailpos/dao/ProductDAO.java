package com.retailpos.dao;

import com.retailpos.model.Product;
import com.retailpos.util.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Singleton pattern for Inventory access
public class ProductDAO {
    private static ProductDAO instance;
    private Connection conn() { return DatabaseHelper.getInstance().getConnection(); }

    private ProductDAO() {}
    public static ProductDAO getInstance() {
        if (instance == null) instance = new ProductDAO();
        return instance;
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        try {
            ResultSet rs = conn().createStatement().executeQuery(
                "SELECT * FROM products ORDER BY category, name");
            while (rs.next()) list.add(mapProduct(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Product findByBarcode(String barcode) {
        try {
            PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM products WHERE barcode=?");
            ps.setString(1, barcode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapProduct(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Product findById(int id) {
        try {
            PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM products WHERE id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapProduct(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Product> searchProducts(String query) {
        List<Product> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM products WHERE name LIKE ? OR barcode LIKE ? OR category LIKE ?");
            String q = "%" + query + "%";
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapProduct(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean addProduct(Product p) {
        try {
            PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO products(barcode,name,category,price,stock_qty,tax_rate) VALUES(?,?,?,?,?,?)");
            ps.setString(1, p.getBarcode()); ps.setString(2, p.getName());
            ps.setString(3, p.getCategory()); ps.setDouble(4, p.getPrice());
            ps.setInt(5, p.getStockQty()); ps.setDouble(6, p.getTaxRate());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateProduct(Product p) {
        try {
            PreparedStatement ps = conn().prepareStatement(
                "UPDATE products SET barcode=?,name=?,category=?,price=?,stock_qty=?,tax_rate=? WHERE id=?");
            ps.setString(1, p.getBarcode()); ps.setString(2, p.getName());
            ps.setString(3, p.getCategory()); ps.setDouble(4, p.getPrice());
            ps.setInt(5, p.getStockQty()); ps.setDouble(6, p.getTaxRate());
            ps.setInt(7, p.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteProduct(int id) {
        try {
            PreparedStatement ps = conn().prepareStatement("DELETE FROM products WHERE id=?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateStock(int productId, int qty) {
        try {
            PreparedStatement ps = conn().prepareStatement(
                "UPDATE products SET stock_qty = stock_qty + ? WHERE id=?");
            ps.setInt(1, qty); ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean checkAvailability(int productId, int qty) {
        try {
            PreparedStatement ps = conn().prepareStatement(
                "SELECT stock_qty FROM products WHERE id=?");
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("stock_qty") >= qty;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public int getProductCount() {
        try {
            ResultSet rs = conn().createStatement().executeQuery(
                "SELECT COUNT(*) FROM products");
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        return new Product(rs.getInt("id"), rs.getString("barcode"), rs.getString("name"),
            rs.getString("category"), rs.getDouble("price"),
            rs.getInt("stock_qty"), rs.getDouble("tax_rate"));
    }
}
