package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import connectDB.ConnectDB;
import Entity.KhuyenMai;

public class KhuyenMaiDAO {

    public ArrayList<KhuyenMai> getAllKhuyenMai() {
        ArrayList<KhuyenMai> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM KhuyenMai";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                KhuyenMai km = new KhuyenMai();
                km.setMaKM(rs.getString("MaKM"));
                km.setTenKM(rs.getString("TenKM"));
                km.setLoaiKM(rs.getString("LoaiKM"));
                km.setGiaTri(rs.getDouble("GiaTri"));
                km.setDieuKienToiThieu(rs.getDouble("DieuKienToiThieu"));
                km.setNgayBatDau(rs.getTimestamp("NgayBatDau"));
                km.setNgayKetThuc(rs.getTimestamp("NgayKetThuc"));
                km.setTrangThai(rs.getString("TrangThai"));
                km.setHangVIPApDung(rs.getString("HangVIPApDung"));
                list.add(km);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy danh sách khuyến mãi ĐANG HOẠT ĐỘNG
    public ArrayList<KhuyenMai> getKhuyenMaiDangHoatDong() {
        ArrayList<KhuyenMai> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            // Check dates and status
            String sql = "SELECT * FROM KhuyenMai WHERE TrangThai = N'Đang hoạt động' AND NgayBatDau <= GETDATE() AND (NgayKetThuc IS NULL OR NgayKetThuc >= GETDATE())";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                KhuyenMai km = new KhuyenMai();
                km.setMaKM(rs.getString("MaKM"));
                km.setTenKM(rs.getString("TenKM"));
                km.setLoaiKM(rs.getString("LoaiKM"));
                km.setGiaTri(rs.getDouble("GiaTri"));
                km.setDieuKienToiThieu(rs.getDouble("DieuKienToiThieu"));
                km.setNgayBatDau(rs.getTimestamp("NgayBatDau"));
                km.setNgayKetThuc(rs.getTimestamp("NgayKetThuc"));
                km.setTrangThai(rs.getString("TrangThai"));
                km.setHangVIPApDung(rs.getString("HangVIPApDung"));
                list.add(km);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
