package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import connectDB.ConnectDB;

public class ThongKeDAO {

    // 1. Thống kê doanh thu theo ngày
    public double getDoanhThuNgay(Date date) {
        double total = 0;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT SUM(TongTien) FROM HoaDon WHERE TrangThai = 1 AND CAST(NgayTao AS DATE) = CAST(? AS DATE)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDate(1, new java.sql.Date(date.getTime()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    // 2. Thống kê doanh thu theo tháng
    public double getDoanhThuThang(int month, int year) {
        double total = 0;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT SUM(TongTien) FROM HoaDon WHERE TrangThai = 1 AND MONTH(NgayTao) = ? AND YEAR(NgayTao) = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, month);
            ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    // 3. Thống kê doanh thu theo năm
    public double getDoanhThuNam(int year) {
        double total = 0;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT SUM(TongTien) FROM HoaDon WHERE TrangThai = 1 AND YEAR(NgayTao) = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    // 4. Lấy doanh thu 7 ngày gần nhất (để vẽ biểu đồ)
    // Trả về List object[] {Date, Double}
    public ArrayList<Object[]> getDoanhThu7NgayGanNhat() {
        ArrayList<Object[]> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT CAST(NgayTao AS DATE) as Ngay, SUM(TongTien) as Tien " +
                    "FROM HoaDon " +
                    "WHERE TrangThai = 1 AND NgayTao >= DATEADD(day, -7, GETDATE()) " +
                    "GROUP BY CAST(NgayTao AS DATE) " +
                    "ORDER BY Ngay ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[] { rs.getDate("Ngay"), rs.getDouble("Tien") });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 5. Top món ăn bán chạy (Theo số lượng)
    public ArrayList<Object[]> getTopMonAn(Date from, Date to) {
        ArrayList<Object[]> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            // Cần join ChiTietHoaDon, HoaDon, MonAn ??
            // Giả sử bảng ChiTietHoaDon lưu tên món luôn (như code cũ) hoặc join MonAn
            // Code cũ ManHinhHoaDon dùng TenMon trong ChiTietHoaDon
            String sql = "SELECT m.TenMon, SUM(ct.SoLuong) as SL, SUM(ct.SoLuong * ct.DonGia) as Tien " +
                    "FROM ChiTietHoaDon ct " +
                    "JOIN HoaDon hd ON ct.MaHD = hd.MaHD " +
                    "JOIN MonAn m ON ct.MaMon = m.MaMon " +
                    "WHERE hd.TrangThai = 1 " +
                    "AND hd.NgayTao BETWEEN ? AND ? " +
                    "GROUP BY m.TenMon " +
                    "ORDER BY SL DESC";

            // Adjust Date Range
            java.sql.Timestamp start = new java.sql.Timestamp(from.getTime());
            start.setHours(0);
            start.setMinutes(0);
            start.setSeconds(0);
            java.sql.Timestamp end = new java.sql.Timestamp(to.getTime());
            end.setHours(23);
            end.setMinutes(59);
            end.setSeconds(59);

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[] { rs.getString("TenMon"), rs.getInt("SL"), rs.getDouble("Tien") });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
