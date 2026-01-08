package DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
}