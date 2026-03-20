package DAO;

import java.sql.*;
import java.util.ArrayList;
import connectDB.ConnectDB;
import Entity.BangGia;

/**
 * BangGiaDAO — Quản lý bảng giá theo thời gian / khung giờ (GĐ3)
 */
public class BangGiaDAO {

    // ----------------------------------------------------------------
    // 1. Lấy giá hiện tại của một món (gọi SP_LayGiaHienTai)
    // ----------------------------------------------------------------
    public double getGiaHienTai(String maMon) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "{CALL SP_LayGiaHienTai(?, ?)}";
            CallableStatement cs = con.prepareCall(sql);
            cs.setString(1, maMon);
            cs.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ResultSet rs = cs.executeQuery();
            if (rs.next())
                return rs.getDouble("DonGia");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Overload tại thời điểm cụ thể
    public double getGiaHienTai(String maMon, java.util.Date thoiDiem) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "{CALL SP_LayGiaHienTai(?, ?)}";
            CallableStatement cs = con.prepareCall(sql);
            cs.setString(1, maMon);
            cs.setTimestamp(2, new Timestamp(thoiDiem.getTime()));
            ResultSet rs = cs.executeQuery();
            if (rs.next())
                return rs.getDouble("DonGia");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ----------------------------------------------------------------
    // 2. Lấy tất cả giá của 1 món
    // ----------------------------------------------------------------
    public ArrayList<BangGia> getByMon(String maMon) {
        ArrayList<BangGia> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM BangGia WHERE MaMon = ? ORDER BY UuTien DESC, MaGia";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maMon);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(map(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ----------------------------------------------------------------
    // 3. Lấy all (cho màn hình quản lý)
    // ----------------------------------------------------------------
    public ArrayList<BangGia> getAll() {
        ArrayList<BangGia> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT bg.*, m.TenMon FROM BangGia bg " +
                    "JOIN MonAn m ON bg.MaMon = m.MaMon " +
                    "ORDER BY bg.MaMon, bg.UuTien DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(map(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ----------------------------------------------------------------
    // 4. Thêm mức giá mới
    // ----------------------------------------------------------------
    public boolean insert(BangGia bg) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "INSERT INTO BangGia (MaMon, DonGia, TuNgay, DenNgay, GioBatDau, GioKetThuc, UuTien, GhiChu) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, bg.getMaMon());
            ps.setDouble(2, bg.getDonGia());
            ps.setObject(3, bg.getTuNgay() != null ? java.sql.Date.valueOf(bg.getTuNgay()) : null);
            ps.setObject(4, bg.getDenNgay() != null ? java.sql.Date.valueOf(bg.getDenNgay()) : null);
            ps.setObject(5, bg.getGioBatDau() != null ? java.sql.Time.valueOf(bg.getGioBatDau()) : null);
            ps.setObject(6, bg.getGioKetThuc() != null ? java.sql.Time.valueOf(bg.getGioKetThuc()) : null);
            ps.setInt(7, bg.getUuTien());
            ps.setString(8, bg.getGhiChu());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ----------------------------------------------------------------
    // 5. Sửa mức giá
    // ----------------------------------------------------------------
    public boolean update(BangGia bg) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE BangGia SET DonGia=?, TuNgay=?, DenNgay=?, " +
                    "GioBatDau=?, GioKetThuc=?, UuTien=?, GhiChu=? WHERE MaGia=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDouble(1, bg.getDonGia());
            ps.setObject(2, bg.getTuNgay() != null ? java.sql.Date.valueOf(bg.getTuNgay()) : null);
            ps.setObject(3, bg.getDenNgay() != null ? java.sql.Date.valueOf(bg.getDenNgay()) : null);
            ps.setObject(4, bg.getGioBatDau() != null ? java.sql.Time.valueOf(bg.getGioBatDau()) : null);
            ps.setObject(5, bg.getGioKetThuc() != null ? java.sql.Time.valueOf(bg.getGioKetThuc()) : null);
            ps.setInt(6, bg.getUuTien());
            ps.setString(7, bg.getGhiChu());
            ps.setInt(8, bg.getMaGia());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ----------------------------------------------------------------
    // 6. Xóa mức giá
    // ----------------------------------------------------------------
    public boolean delete(int maGia) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM BangGia WHERE MaGia=?");
            ps.setInt(1, maGia);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ----------------------------------------------------------------
    // Helper: Map ResultSet -> BangGia
    // ----------------------------------------------------------------
    private BangGia map(ResultSet rs) throws SQLException {
        return new BangGia(
                rs.getInt("MaGia"),
                rs.getString("MaMon"),
                rs.getDouble("DonGia"),
                rs.getDate("TuNgay") != null ? rs.getDate("TuNgay").toString() : null,
                rs.getDate("DenNgay") != null ? rs.getDate("DenNgay").toString() : null,
                rs.getTime("GioBatDau") != null ? rs.getTime("GioBatDau").toString() : null,
                rs.getTime("GioKetThuc") != null ? rs.getTime("GioKetThuc").toString() : null,
                rs.getInt("UuTien"),
                rs.getString("GhiChu"));
    }
}
