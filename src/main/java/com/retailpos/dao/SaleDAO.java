package com.retailpos.dao;

import com.retailpos.model.*;
import com.retailpos.util.DatabaseHelper;
import java.sql.*;
import java.util.*;

public class SaleDAO {
    private Connection conn() { return DatabaseHelper.getInstance().getConnection(); }
    private final ProductDAO productDAO = ProductDAO.getInstance();

    public int saveSale(Sale sale) {
        try {
            conn().setAutoCommit(false);
            PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO sales(cashier_id,sale_date,subtotal,tax_amount,total_amount,payment_method,payment_status) VALUES(?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, sale.getCashierId());
            ps.setString(2, sale.getSaleDate());
            ps.setDouble(3, sale.getSubtotal());
            ps.setDouble(4, sale.getTaxAmount());
            ps.setDouble(5, sale.getTotalAmount());
            ps.setString(6, sale.getPaymentMethod());
            ps.setString(7, sale.getPaymentStatus());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int saleId = keys.next() ? keys.getInt(1) : -1;
            if (saleId < 0) { conn().rollback(); return -1; }

            for (SaleItem item : sale.getItems()) {
                PreparedStatement ps2 = conn().prepareStatement(
                    "INSERT INTO sale_items(sale_id,product_id,quantity,unit_price,subtotal) VALUES(?,?,?,?,?)");
                ps2.setInt(1, saleId);
                ps2.setInt(2, item.getProduct().getId());
                ps2.setInt(3, item.getQuantity());
                ps2.setDouble(4, item.getUnitPrice());
                ps2.setDouble(5, item.getSubtotal());
                ps2.executeUpdate();
                // Deduct stock
                productDAO.updateStock(item.getProduct().getId(), -item.getQuantity());
            }
            conn().commit();
            conn().setAutoCommit(true);
            return saleId;
        } catch (SQLException e) {
            e.printStackTrace();
            try { conn().rollback(); conn().setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            return -1;
        }
    }

    public List<Sale> getRecentSales(int limit) {
        List<Sale> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM sales ORDER BY id DESC LIMIT ?");
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapSale(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Sale getSaleById(int id) {
        try {
            PreparedStatement ps = conn().prepareStatement("SELECT * FROM sales WHERE id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Sale sale = mapSale(rs);
                sale.setItems(getSaleItems(id));
                return sale;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public int getSoldQuantityForProduct(int saleId, int productId) {
        try {
            PreparedStatement ps = conn().prepareStatement("""
                SELECT COALESCE(SUM(quantity), 0)
                FROM sale_items
                WHERE sale_id=? AND product_id=?
            """);
            ps.setInt(1, saleId);
            ps.setInt(2, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private List<SaleItem> getSaleItems(int saleId) {
        List<SaleItem> items = new ArrayList<>();
        try {
            PreparedStatement ps = conn().prepareStatement(
                "SELECT si.*, p.* FROM sale_items si JOIN products p ON si.product_id=p.id WHERE si.sale_id=?");
            ps.setInt(1, saleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Product product = new Product(rs.getInt("product_id"), rs.getString("barcode"),
                    rs.getString("name"), rs.getString("category"),
                    rs.getDouble("price"), rs.getInt("stock_qty"), rs.getDouble("tax_rate"));
                SaleItem item = new SaleItem(product, rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unit_price"));
                item.setSubtotal(rs.getDouble("subtotal"));
                items.add(item);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return items;
    }

    public Map<String, Double> getDailySummary() {
        Map<String, Double> summary = new LinkedHashMap<>();
        try {
            ResultSet rs = conn().createStatement().executeQuery("""
                SELECT DATE(sale_date) as day, SUM(total_amount) as total
                FROM sales WHERE DATE(sale_date) >= DATE('now', '-7 days')
                GROUP BY day ORDER BY day
            """);
            while (rs.next()) summary.put(rs.getString("day"), rs.getDouble("total"));
        } catch (SQLException e) { e.printStackTrace(); }
        return summary;
    }

    public double getTodayRevenue() {
        try {
            ResultSet rs = conn().createStatement().executeQuery(
                "SELECT COALESCE(SUM(total_amount),0) FROM sales WHERE DATE(sale_date)=DATE('now')");
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getTodayTransactions() {
        try {
            ResultSet rs = conn().createStatement().executeQuery(
                "SELECT COUNT(*) FROM sales WHERE DATE(sale_date)=DATE('now')");
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private Sale mapSale(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setId(rs.getInt("id"));
        sale.setCashierId(rs.getInt("cashier_id"));
        sale.setSaleDate(rs.getString("sale_date"));
        sale.setSubtotal(rs.getDouble("subtotal"));
        sale.setTaxAmount(rs.getDouble("tax_amount"));
        sale.setTotalAmount(rs.getDouble("total_amount"));
        sale.setPaymentMethod(rs.getString("payment_method"));
        sale.setPaymentStatus(rs.getString("payment_status"));
        return sale;
    }
}
