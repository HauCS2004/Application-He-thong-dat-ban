package DAO;

import java.sql.*;
import java.util.ArrayList;
import connectDB.ConnectDB;
import Entity.DatBan;

public class DatBanDAO {

    /**
     * Đặt bàn mới (sử dụng stored procedure SP_DatBan)
     * 
     * @param db Entity DatBan
     * @return true nếu thành công
     */
    public boolean insertDatBan(DatBan db) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            // Sử dụng Stored Procedure SP_DatBan từ database mới
            String sql = "{CALL SP_DatBan(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
            CallableStatement cs = con.prepareCall(sql);

            // Input parameters
            cs.setString(1, db.getMaBan());
            cs.setString(2, db.getTenKhach());
            cs.setString(3, db.getSdt());
            cs.setTimestamp(4, new java.sql.Timestamp(db.getThoiGianBatDau().getTime()));
            cs.setTimestamp(5, new java.sql.Timestamp(db.getThoiGianKetThuc().getTime()));
            cs.setInt(6, db.getSoLuongKhach());
            cs.setString(7, db.getGhiChu());
            cs.setDouble(8, db.getTienCoc());

            // Output parameters
            cs.registerOutParameter(9, Types.INTEGER); // @KetQua
            cs.registerOutParameter(10, Types.NVARCHAR); // @ThongBao

            cs.execute();

            // Lấy kết quả
            int ketQua = cs.getInt(9);
            String thongBao = cs.getString(10);

            System.out.println("Kết quả đặt bàn: " + thongBao);

            if (ketQua == 1) {
                // Cập nhật trạng thái bàn
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

    /**
     * Lấy đặt bàn gần nhất theo mã bàn
     */
    public static DatBan getDatBanGanNhat(String maBan) {
        DatBan db = null;
        try {
            Connection con = ConnectDB.getInstance().getConnection();

            // Query theo schema mới
            String sql = "SELECT TOP 1 * FROM DatBan " +
                    "WHERE MaBan = ? " +
                    "AND TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận') " +
                    "ORDER BY ThoiGianBatDau DESC";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maBan);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                db = new DatBan(
                        rs.getInt("MaDat"),
                        rs.getString("MaBan"),
                        rs.getString("TenKhachDat"),
                        rs.getString("SDT"),
                        rs.getTimestamp("ThoiGianBatDau"),
                        rs.getTimestamp("ThoiGianKetThuc"),
                        rs.getInt("SoLuongKhach"),
                        rs.getString("TrangThai"),
                        rs.getDouble("TienCoc"),
                        rs.getString("GhiChu"),
                        rs.getTimestamp("NgayTao"),
                        (Integer) rs.getObject("MaHD") // nullable
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return db;
    }

    /**
     * Lấy danh sách đặt bàn trong khoảng thời gian
     */
    public ArrayList<DatBan> getDanhSachDatBan(java.util.Date tuNgay, java.util.Date denNgay) {
        ArrayList<DatBan> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();

            // Query theo schema mới
            String sql = "SELECT * FROM DatBan " +
                    "WHERE CAST(ThoiGianBatDau AS DATE) BETWEEN ? AND ? " +
                    "ORDER BY ThoiGianBatDau DESC";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDate(1, new java.sql.Date(tuNgay.getTime()));
            ps.setDate(2, new java.sql.Date(denNgay.getTime()));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new DatBan(
                        rs.getInt("MaDat"),
                        rs.getString("MaBan"),
                        rs.getString("TenKhachDat"),
                        rs.getString("SDT"),
                        rs.getTimestamp("ThoiGianBatDau"),
                        rs.getTimestamp("ThoiGianKetThuc"),
                        rs.getInt("SoLuongKhach"),
                        rs.getString("TrangThai"),
                        rs.getDouble("TienCoc"),
                        rs.getString("GhiChu"),
                        rs.getTimestamp("NgayTao"),
                        (Integer) rs.getObject("MaHD")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Kiểm tra xung đột đặt bàn (sử dụng SP_KiemTraDatBan)
     */
    public int kiemTraXungDot(String maBan, java.util.Date thoiGianBatDau, java.util.Date thoiGianKetThuc) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "{CALL SP_KiemTraDatBan(?, ?, ?)}";
            CallableStatement cs = con.prepareCall(sql);

            cs.setString(1, maBan);
            cs.setTimestamp(2, new Timestamp(thoiGianBatDau.getTime()));
            cs.setTimestamp(3, new Timestamp(thoiGianKetThuc.getTime()));

            ResultSet rs = cs.executeQuery();
            if (rs.next()) {
                return rs.getInt("SoLuongXungDot");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Cập nhật trạng thái đặt bàn
     */
    public boolean capNhatTrangThai(int maDat, String trangThai) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "UPDATE DatBan SET TrangThai = ? WHERE MaDat = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, trangThai);
            ps.setInt(2, maDat);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Gợi ý bàn phù hợp (sử dụng SP_GoiYBan)
     */
    public ArrayList<String> goiYBan(int soKhach, java.util.Date thoiGianBatDau,
            java.util.Date thoiGianKetThuc, String maKV) {
        ArrayList<String> danhSachBan = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "{CALL SP_GoiYBan(?, ?, ?, ?)}";
            CallableStatement cs = con.prepareCall(sql);

            cs.setInt(1, soKhach);
            cs.setTimestamp(2, new Timestamp(thoiGianBatDau.getTime()));
            cs.setTimestamp(3, new Timestamp(thoiGianKetThuc.getTime()));
            cs.setString(4, maKV);

            ResultSet rs = cs.executeQuery();
            while (rs.next()) {
                danhSachBan.add(rs.getString("MaBan"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return danhSachBan;
    }
}