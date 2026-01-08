package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import connectDB.ConnectDB;
import Entity.ChiTietHoaDon;

public class ChiTietHoaDonDAO {

    // 1. Lấy danh sách món ăn của hóa đơn này
    public ArrayList<ChiTietHoaDon> getChiTiet(int maHD) {
        ArrayList<ChiTietHoaDon> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            // Join với bảng MonAn để lấy Tên và Giá
            String sql = "SELECT ct.MaHD, ct.MaMon, m.TenMon, ct.SoLuong, ct.DonGia " +
                         "FROM ChiTietHoaDon ct JOIN MonAn m ON ct.MaMon = m.MaMon " +
                         "WHERE ct.MaHD = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maHD);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                ChiTietHoaDon ct = new ChiTietHoaDon(
                    rs.getInt("MaHD"),
                    rs.getString("MaMon"),
                    rs.getInt("SoLuong"),
                    rs.getDouble("DonGia")
                );
                ct.setTenMonAn(rs.getString("TenMon")); // Cần thêm setter này bên Entity
                list.add(ct);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 2. Thêm món (Nếu có rồi thì cộng dồn số lượng, chưa có thì thêm mới)
    public void themMon(int maHD, String maMon, int soLuongThem, double donGia) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            
            // A. Kiểm tra xem món này đã có trong bill chưa
            String checkSQL = "SELECT SoLuong FROM ChiTietHoaDon WHERE MaHD=? AND MaMon=?";
            PreparedStatement psCheck = con.prepareStatement(checkSQL);
            psCheck.setInt(1, maHD);
            psCheck.setString(2, maMon);
            ResultSet rs = psCheck.executeQuery();
            
            if(rs.next()) {
                // B. Nếu có rồi -> UPDATE cộng thêm số lượng
                int slCu = rs.getInt(1);
                String updateSQL = "UPDATE ChiTietHoaDon SET SoLuong = ? WHERE MaHD=? AND MaMon=?";
                PreparedStatement psUp = con.prepareStatement(updateSQL);
                psUp.setInt(1, slCu + soLuongThem);
                psUp.setInt(2, maHD);
                psUp.setString(3, maMon);
                psUp.executeUpdate();
            } else {
                // C. Nếu chưa có -> INSERT mới
                String insertSQL = "INSERT INTO ChiTietHoaDon (MaHD, MaMon, SoLuong, DonGia) VALUES (?, ?, ?, ?)";
                PreparedStatement psIn = con.prepareStatement(insertSQL);
                psIn.setInt(1, maHD);
                psIn.setString(2, maMon);
                psIn.setInt(3, soLuongThem);
                psIn.setDouble(4, donGia);
                psIn.executeUpdate();
            }
            
            // D. Cập nhật Tổng Tiền cho Hóa Đơn (Quan trọng)
            updateTongTien(maHD);
            
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 3. Xóa món khỏi bill
    public void xoaMon(int maHD, String maMon) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "DELETE FROM ChiTietHoaDon WHERE MaHD=? AND MaMon=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maHD);
            ps.setString(2, maMon);
            ps.executeUpdate();
            
            updateTongTien(maHD); // Tính lại tiền
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    // Hàm phụ: Tự động tính tổng tiền hóa đơn
    private void updateTongTien(int maHD) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "UPDATE HoaDon SET TongTien = (SELECT SUM(SoLuong * DonGia) FROM ChiTietHoaDon WHERE MaHD = ?) WHERE MaHD = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maHD);
            ps.setInt(2, maHD);
            ps.executeUpdate();
        } catch (Exception e) {}
    }
}