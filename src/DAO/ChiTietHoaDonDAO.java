package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import connectDB.ConnectDB;
import Entity.ChiTietHoaDon;

public class ChiTietHoaDonDAO {

    /**
     * Kiểm tra món ăn đã có trong hóa đơn chưa
     */
    public ChiTietHoaDon getChiTiet(int maHD, String maMon) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM ChiTietHoaDon WHERE MaHD = ? AND MaMon = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maHD);
            ps.setString(2, maMon);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new ChiTietHoaDon(
                        rs.getInt("MaHD"),
                        rs.getString("MaMon"),
                        rs.getInt("SoLuong"),
                        rs.getDouble("DonGia"),
                        rs.getString("GhiChu"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Thêm món mới vào chi tiết hóa đơn
     */
    public boolean themMon(int maHD, String maMon, int soLuong, double donGia) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "INSERT INTO ChiTietHoaDon(MaHD, MaMon, SoLuong, DonGia) VALUES(?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maHD);
            ps.setString(2, maMon);
            ps.setInt(3, soLuong);
            ps.setDouble(4, donGia);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật số lượng món ăn
     */
    public boolean capNhatSoLuong(int maHD, String maMon, int soLuongMoi) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE ChiTietHoaDon SET SoLuong = ? WHERE MaHD = ? AND MaMon = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, soLuongMoi);
            ps.setInt(2, maHD);
            ps.setString(3, maMon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa món ăn khỏi hóa đơn
     */
    public boolean xoaMon(int maHD, String maMon) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "DELETE FROM ChiTietHoaDon WHERE MaHD = ? AND MaMon = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maHD);
            ps.setString(2, maMon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
