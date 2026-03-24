package DAO;

import java.sql.*;
import java.util.ArrayList;
import connectDB.ConnectDB;
import Entity.BangGia;
import Entity.ChiTietBangGia;

public class BangGiaDAO {

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

    public ArrayList<BangGia> getAll() {
        ArrayList<BangGia> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM BangGia ORDER BY UuTien DESC, MaBG DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapHeader(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<ChiTietBangGia> getChiTietByMaBG(int maBG) {
        ArrayList<ChiTietBangGia> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM ChiTietBangGia WHERE MaBG = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maBG);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ChiTietBangGia(
                        rs.getInt("MaBG"),
                        rs.getString("MaMon"),
                        rs.getDouble("DonGia"),
                        rs.getString("GhiChu")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int insert(BangGia bg) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "INSERT INTO BangGia (TenBG, LoaiBG, NgayBatDau, NgayKetThuc, GioBatDau, GioKetThuc, UuTien, TrangThai, GhiChu) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, bg.getTenBG());
            ps.setString(2, bg.getLoaiBG());
            ps.setDate(3, bg.getNgayBatDau());
            ps.setDate(4, bg.getNgayKetThuc());
            ps.setTime(5, bg.getGioBatDau());
            ps.setTime(6, bg.getGioKetThuc());
            ps.setInt(7, bg.getUuTien());
            ps.setString(8, bg.getTrangThai());
            ps.setString(9, bg.getGhiChu());
            
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(BangGia bg) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE BangGia SET TenBG=?, LoaiBG=?, NgayBatDau=?, NgayKetThuc=?, " +
                    "GioBatDau=?, GioKetThuc=?, UuTien=?, TrangThai=?, GhiChu=? WHERE MaBG=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, bg.getTenBG());
            ps.setString(2, bg.getLoaiBG());
            ps.setDate(3, bg.getNgayBatDau());
            ps.setDate(4, bg.getNgayKetThuc());
            ps.setTime(5, bg.getGioBatDau());
            ps.setTime(6, bg.getGioKetThuc());
            ps.setInt(7, bg.getUuTien());
            ps.setString(8, bg.getTrangThai());
            ps.setString(9, bg.getGhiChu());
            ps.setInt(10, bg.getMaBG());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int maBG) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM BangGia WHERE MaBG=?");
            ps.setInt(1, maBG);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insertChiTiet(ChiTietBangGia ct) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "INSERT INTO ChiTietBangGia (MaBG, MaMon, DonGia, GhiChu) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, ct.getMaBG());
            ps.setString(2, ct.getMaMon());
            ps.setDouble(3, ct.getDonGia());
            ps.setString(4, ct.getGhiChu());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteChiTiet(int maBG, String maMon) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM ChiTietBangGia WHERE MaBG=? AND MaMon=?");
            ps.setInt(1, maBG);
            ps.setString(2, maMon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private BangGia mapHeader(ResultSet rs) throws SQLException {
        return new BangGia(
                rs.getInt("MaBG"),
                rs.getString("TenBG"),
                rs.getString("LoaiBG"),
                rs.getDate("NgayBatDau"),
                rs.getDate("NgayKetThuc"),
                rs.getTime("GioBatDau"),
                rs.getTime("GioKetThuc"),
                rs.getInt("UuTien"),
                rs.getString("TrangThai"),
                rs.getString("GhiChu"),
                rs.getTimestamp("NgayTao"));
    }
}
