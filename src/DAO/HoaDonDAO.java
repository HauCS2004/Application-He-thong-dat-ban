package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import connectDB.ConnectDB;
import Entity.HoaDon;

public class HoaDonDAO {
    
    // 1. Tạo hóa đơn mới (Quan trọng: Trả về Mã HĐ vừa tạo)
    public int insert(HoaDon hd) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "INSERT INTO HoaDon (MaBan, SoLuongKhach, SDT_Khach, GhiChu, TrangThai, TongTien) VALUES (?, ?, ?, ?, 0, 0)";
            
            // Tham số Statement.RETURN_GENERATED_KEYS để lấy ID tự tăng
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setString(1, hd.getMaBan());
            ps.setInt(2, hd.getSoLuongKhach());
            ps.setString(3, hd.getSdtKhach()); // Có thể null
            ps.setString(4, hd.getGhiChu());
            
            if (ps.executeUpdate() > 0) {
                // Lấy ngay cái ID vừa tạo ra
                ResultSet rs = ps.getGeneratedKeys();
                if(rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1; // Lỗi
    }

    // 2. Tìm Mã HĐ đang hoạt động của bàn (TrangThai = 0)
    // Hàm này giúp biết bàn đó đang ăn dở hóa đơn nào để thêm món vào
    public int getMaHDByBan(String maBan) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT MaHD FROM HoaDon WHERE MaBan = ? AND TrangThai = 0";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maBan);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("MaHD");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1; // Bàn trống, không có hóa đơn
    }
    
    // 3. Thanh toán (Kết thúc hóa đơn)
    public void thanhToan(int maHD, double tongTienCuoi) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "UPDATE HoaDon SET TrangThai = 1, TongTien = ? WHERE MaHD = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDouble(1, tongTienCuoi);
            ps.setInt(2, maHD);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
 // Trong class HoaDonDAO
    public int getSoLuongKhach(String maBan) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            // Lấy số khách của hóa đơn đang mở (TrangThai = 0) tại bàn đó
            String sql = "SELECT SoLuongKhach FROM HoaDon WHERE MaBan = ? AND TrangThai = 0";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maBan);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("SoLuongKhach");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0; // Nếu bàn trống hoặc lỗi thì trả về 0
    }
 // ... (Giữ nguyên code cũ của bạn) ...

    // 4. [MỚI] Tính tổng tiền tạm (Tổng món ăn chưa trừ giảm giá)
    public double getTongTienTamTinh(int maHD) {
        double tong = 0;
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT SUM(SoLuong * DonGia) FROM ChiTietHoaDon WHERE MaHD = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) tong = rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return tong;
    }

    // 5. [MỚI] Lấy SĐT và Tên khách đã lưu trong Hóa Đơn (lúc mở bàn)
    public HoaDon getThongTinHoaDon(int maHD) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM HoaDon WHERE MaHD = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maHD);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                HoaDon hd = new HoaDon();
                hd.setMaHD(maHD);
                hd.setSdtKhach(rs.getString("SDT_Khach"));
                hd.setGhiChu(rs.getString("GhiChu")); // Giả sử tên khách lưu trong GhiChu hoặc cột riêng
                return hd;
            }
        } catch(Exception e) {}
        return null;
    }
}