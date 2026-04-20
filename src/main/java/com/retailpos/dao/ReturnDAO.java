package com.retailpos.dao;

import com.retailpos.model.ReturnItem;
import com.retailpos.util.DatabaseHelper;
import java.sql.*;
import java.util.*;

public class ReturnDAO {
    private Connection conn() { return DatabaseHelper.getInstance().getConnection(); }
    private final ProductDAO productDAO = ProductDAO.getInstance();

    public boolean saveReturn(ReturnItem r) {
        try {
            conn().setAutoCommit(false);
            PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO returns(sale_id,product_id,quantity,return_type,return_date,refund_amount,status) VALUES(?,?,?,?,?,?,?)");
            ps.setInt(1, r.getSaleId()); ps.setInt(2, r.getProductId());
            ps.setInt(3, r.getQuantity()); ps.setString(4, r.getReturnType());
            ps.setString(5, r.getReturnDate()); ps.setDouble(6, r.getRefundAmount());
            ps.setString(7, r.getStatus());
            boolean inserted = ps.executeUpdate() > 0;
            if (!inserted || !productDAO.updateStock(r.getProductId(), r.getQuantity())) {
                conn().rollback();
                conn().setAutoCommit(true);
                return false;
            }
            conn().commit();
            conn().setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try { conn().rollback(); conn().setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        }
    }

    public List<ReturnItem> getAllReturns() {
        List<ReturnItem> list = new ArrayList<>();
        try {
            ResultSet rs = conn().createStatement().executeQuery("""
                SELECT r.*, p.name as product_name FROM returns r
                LEFT JOIN products p ON r.product_id=p.id
                ORDER BY r.id DESC
            """);
            while (rs.next()) {
                ReturnItem ri = new ReturnItem();
                ri.setId(rs.getInt("id"));
                ri.setSaleId(rs.getInt("sale_id"));
                ri.setProductId(rs.getInt("product_id"));
                ri.setQuantity(rs.getInt("quantity"));
                ri.setReturnType(rs.getString("return_type"));
                ri.setReturnDate(rs.getString("return_date"));
                ri.setRefundAmount(rs.getDouble("refund_amount"));
                ri.setStatus(rs.getString("status"));
                ri.setProductName(rs.getString("product_name"));
                list.add(ri);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int getReturnedQuantityForProduct(int saleId, int productId) {
        try {
            PreparedStatement ps = conn().prepareStatement("""
                SELECT COALESCE(SUM(quantity), 0)
                FROM returns
                WHERE sale_id=? AND product_id=?
            """);
            ps.setInt(1, saleId);
            ps.setInt(2, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
