package DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import connectDB.ConnectDB;
import Entity.DatBan;

public class DatBanDAO {
    
    public boolean insertDatBan(DatBan db) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            // 1. Thêm vào bảng DatBan
            String sql = "INSERT INTO DatBan (MaBan, TenKhachDat, SDT, ThoiGianDat, SoLuongKhach, GhiChu) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, db.getMaBan());
            ps.setString(2, db.getTenKhach());
            ps.setString(3, db.getSdt());
            // Chuyển java.util.Date sang java.sql.Timestamp để lưu cả giờ phút
            ps.setTimestamp(4, new java.sql.Timestamp(db.getThoiGian().getTime()));
            ps.setInt(5, db.getSoLuongKhach());
            ps.setString(6, db.getGhiChu());
            
            if(ps.executeUpdate() > 0) {
                // 2. Cập nhật trạng thái Bàn thành "Đã Đặt"
                String sqlUpdate = "UPDATE Ban SET TrangThai = N'Đã Đặt' WHERE MaBan = ?";
                PreparedStatement psUp = con.prepareStatement(sqlUpdate);
                psUp.setString(1, db.getMaBan());
                psUp.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
 //// Sửa lại hàm này để bắt được dữ liệu dễ hơn
    public static DatBan getDatBanGanNhat(String maBan) {
        DatBan db = null;
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            
            // SỬA SQL: Bỏ điều kiện ngày tháng để test cho dễ ra dữ liệu
            String sql = "SELECT TOP 1 * FROM DatBan " +
                         "WHERE MaBan = ? " +
                         // "AND CAST(ThoiGianDat AS DATE) = CAST(GETDATE() AS DATE) " +  <-- Tạm comment dòng này lại
                         "ORDER BY ThoiGianDat DESC"; // Lấy cái mới nhất là được
            
            java.sql.PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maBan);
            
            // In ra console để kiểm tra xem SQL có chạy không
            System.out.println("Check SQL MaBan: " + maBan); 
            
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                db = new Entity.DatBan(
                    rs.getString("MaBan"),
                    rs.getString("TenKhachDat"), // Đảm bảo cột trong DB là TenKhachDat
                    rs.getString("SDT"),
                    rs.getTimestamp("ThoiGianDat"),
                    rs.getInt("SoLuongKhach"),
                    rs.getString("GhiChu")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return db;
    }
 // Thêm vào DatBanDAO.java
    public java.util.ArrayList<Entity.DatBan> getDanhSachDatBan(java.util.Date tuNgay, java.util.Date denNgay) {
        java.util.ArrayList<Entity.DatBan> list = new java.util.ArrayList<>();
        try {
            java.sql.Connection con = connectDB.ConnectDB.getInstance().getConnection();
            
            // SQL: Lấy đơn đặt trong khoảng ngày (so sánh phần ngày)
            String sql = "SELECT * FROM DatBan WHERE CAST(ThoiGianDat AS DATE) BETWEEN ? AND ? ORDER BY ThoiGianDat DESC";
            
            java.sql.PreparedStatement ps = con.prepareStatement(sql);
            ps.setDate(1, new java.sql.Date(tuNgay.getTime()));
            ps.setDate(2, new java.sql.Date(denNgay.getTime()));
            
            java.sql.ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                list.add(new Entity.DatBan(
                    rs.getString("MaBan"),
                    rs.getString("TenKhachDat"),
                    rs.getString("SDT"),
                    rs.getTimestamp("ThoiGianDat"),
                    rs.getInt("SoLuongKhach"),
                    rs.getString("GhiChu")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    
    
}