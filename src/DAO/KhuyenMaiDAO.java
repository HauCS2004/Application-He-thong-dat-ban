package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

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

    // Thêm khuyến mãi mới
    public boolean insert(KhuyenMai km) {
        Connection con = ConnectDB.getConnection();
        String sql = "INSERT INTO KhuyenMai VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, km.getMaKM());
            ps.setString(2, km.getTenKM());
            ps.setString(3, km.getLoaiKM());
            ps.setDouble(4, km.getGiaTri());
            ps.setDouble(5, km.getDieuKienToiThieu());
            ps.setTimestamp(6, new java.sql.Timestamp(km.getNgayBatDau().getTime()));
            ps.setTimestamp(7, new java.sql.Timestamp(km.getNgayKetThuc().getTime()));
            ps.setString(8, km.getTrangThai());
            ps.setString(9, km.getHangVIPApDung());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật khuyến mãi
    public boolean update(KhuyenMai km) {
        Connection con = ConnectDB.getConnection();
        String sql = "UPDATE KhuyenMai SET TenKM=?, LoaiKM=?, GiaTri=?, DieuKienToiThieu=?, " +
                "NgayBatDau=?, NgayKetThuc=?, TrangThai=?, HangVIPApDung=? WHERE MaKM=?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, km.getTenKM());
            ps.setString(2, km.getLoaiKM());
            ps.setDouble(3, km.getGiaTri());
            ps.setDouble(4, km.getDieuKienToiThieu());
            ps.setTimestamp(5, new java.sql.Timestamp(km.getNgayBatDau().getTime()));
            ps.setTimestamp(6, new java.sql.Timestamp(km.getNgayKetThuc().getTime()));
            ps.setString(7, km.getTrangThai());
            ps.setString(8, km.getHangVIPApDung());
            ps.setString(9, km.getMaKM());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa khuyến mãi
    public boolean delete(String maKM) {
        Connection con = ConnectDB.getConnection();
        String sql = "DELETE FROM KhuyenMai WHERE MaKM=?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maKM);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy khuyến mãi theo mã
    public KhuyenMai getByCode(String code) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM KhuyenMai WHERE MaKM = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
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
                return km;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
