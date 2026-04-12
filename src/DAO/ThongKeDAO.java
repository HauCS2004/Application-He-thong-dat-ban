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
            String sql = "SELECT SUM(TongTien) FROM HoaDon WHERE TrangThai = N'Đã thanh toán' AND CAST(NgayTao AS DATE) = CAST(? AS DATE)";
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
            String sql = "SELECT SUM(TongTien) FROM HoaDon WHERE TrangThai = N'Đã thanh toán' AND MONTH(NgayTao) = ? AND YEAR(NgayTao) = ?";
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
            String sql = "SELECT SUM(TongTien) FROM HoaDon WHERE TrangThai = N'Đã thanh toán' AND YEAR(NgayTao) = ?";
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

    // 3.1 So sánh Hôm qua
    public double getDoanhThuHomQua(Date date) {
        double total = 0;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT SUM(TongTien) FROM HoaDon WHERE TrangThai = N'Đã thanh toán' AND CAST(NgayTao AS DATE) = CAST(DATEADD(day, -1, ?) AS DATE)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDate(1, new java.sql.Date(date.getTime()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) total = rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return total;
    }

    // 3.2 So sánh Tháng trước
    public double getDoanhThuThangTruoc(int month, int year) {
        double total = 0;
        try {
            Connection con = ConnectDB.getConnection();
            // Nếu tháng 1 thì lùi về tháng 12 năm trước
            int prevMonth = (month == 1) ? 12 : month - 1;
            int prevYear = (month == 1) ? year - 1 : year;
            String sql = "SELECT SUM(TongTien) FROM HoaDon WHERE TrangThai = N'Đã thanh toán' AND MONTH(NgayTao) = ? AND YEAR(NgayTao) = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, prevMonth);
            ps.setInt(2, prevYear);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) total = rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return total;
    }

    // 3.3 So sánh Năm trước
    public double getDoanhThuNamTruoc(int year) {
        double total = 0;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT SUM(TongTien) FROM HoaDon WHERE TrangThai = N'Đã thanh toán' AND YEAR(NgayTao) = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, year - 1);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) total = rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
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
                    "WHERE TrangThai = N'Đã thanh toán' AND NgayTao >= DATEADD(day, -7, GETDATE()) " +
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
                    "WHERE hd.TrangThai = N'Đã thanh toán' " +
                    "AND hd.NgayTao BETWEEN ? AND ? " +
                    "GROUP BY m.TenMon " +
                    "ORDER BY SL DESC";

            // Adjust Date Range
            // Adjust Date Range using Calendar
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(from);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            java.sql.Timestamp start = new java.sql.Timestamp(cal.getTimeInMillis());

            cal.setTime(to);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
            cal.set(java.util.Calendar.MINUTE, 59);
            cal.set(java.util.Calendar.SECOND, 59);
            cal.set(java.util.Calendar.MILLISECOND, 999);
            java.sql.Timestamp end = new java.sql.Timestamp(cal.getTimeInMillis());

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

    // 6. Thống kê theo khung giờ (Golden Hour)
    public ArrayList<Object[]> getDoanhThuTheoKhungGio(Date from, Date to) {
        ArrayList<Object[]> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT DATEPART(HOUR, NgayTao) as Gio, COUNT(*) as SoDon, SUM(TongTien) as DoanhThu " +
                    "FROM HoaDon " +
                    "WHERE TrangThai = N'Đã thanh toán' AND NgayTao BETWEEN ? AND ? " +
                    "GROUP BY DATEPART(HOUR, NgayTao) " +
                    "ORDER BY Gio";

            // Adjust Date Range using Calendar
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(from);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            java.sql.Timestamp start = new java.sql.Timestamp(cal.getTimeInMillis());

            cal.setTime(to);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
            cal.set(java.util.Calendar.MINUTE, 59);
            cal.set(java.util.Calendar.SECOND, 59);
            cal.set(java.util.Calendar.MILLISECOND, 999);
            java.sql.Timestamp end = new java.sql.Timestamp(cal.getTimeInMillis());

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[] { rs.getInt("Gio"), rs.getInt("SoDon"), rs.getDouble("DoanhThu") });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 7. Thống kê hiệu suất nhân viên
    public ArrayList<Object[]> getHieuSuatNhanVien(Date from, Date to) {
        ArrayList<Object[]> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            // Left Join để lấy cả nhân viên không bán được?
            // Tạm thời Inner Join để lấy người có doanh thu
            // Cần bảng NhanVien để lấy tên. Nếu mã NV null (admin/system) thì xử lý sau.
            String sql = "SELECT hd.MaNV, nv.TenNV, COUNT(hd.MaHD) as SoDon, SUM(hd.TongTien) as DoanhThu " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN NhanVien nv ON hd.MaNV = nv.MaNV " +
                    "WHERE hd.TrangThai = N'Đã thanh toán' AND hd.NgayTao BETWEEN ? AND ? " +
                    "GROUP BY hd.MaNV, nv.TenNV " +
                    "ORDER BY DoanhThu DESC";

            // Adjust Date Range using Calendar
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(from);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            java.sql.Timestamp start = new java.sql.Timestamp(cal.getTimeInMillis());

            cal.setTime(to);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
            cal.set(java.util.Calendar.MINUTE, 59);
            cal.set(java.util.Calendar.SECOND, 59);
            cal.set(java.util.Calendar.MILLISECOND, 999);
            java.sql.Timestamp end = new java.sql.Timestamp(cal.getTimeInMillis());

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String maNV = rs.getString("MaNV");
                String tenNV = rs.getString("TenNV");
                if (maNV == null) {
                    maNV = "N/A";
                    tenNV = "System/Undefined";
                }
                list.add(new Object[] { maNV, tenNV, rs.getInt("SoDon"), rs.getDouble("DoanhThu") });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ----------------------------------------------------------------
    // 8. Doanh thu theo khoảng ngày tùy chọn (FIX: filter thực sự)
    // ----------------------------------------------------------------
    public ArrayList<Object[]> getDoanhThuTheoKhoang(Date from, Date to) {
        ArrayList<Object[]> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT CAST(NgayTao AS DATE) as Ngay, SUM(TongTien) as Tien " +
                    "FROM HoaDon " +
                    "WHERE TrangThai = N'Đã thanh toán' AND NgayTao BETWEEN ? AND ? " +
                    "GROUP BY CAST(NgayTao AS DATE) " +
                    "ORDER BY Ngay ASC";

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(from);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            java.sql.Timestamp start = new java.sql.Timestamp(cal.getTimeInMillis());

            cal.setTime(to);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
            cal.set(java.util.Calendar.MINUTE, 59);
            cal.set(java.util.Calendar.SECOND, 59);
            cal.set(java.util.Calendar.MILLISECOND, 999);
            java.sql.Timestamp end = new java.sql.Timestamp(cal.getTimeInMillis());

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[] { rs.getDate("Ngay"), rs.getDouble("Tien") });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ----------------------------------------------------------------
    // 9. Dashboard Trang Chủ: Số hóa đơn hôm nay (tất cả trạng thái)
    // ----------------------------------------------------------------
    public int getSoHoaDonHomNay() {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT COUNT(*) FROM HoaDon WHERE CAST(NgayTao AS DATE) = CAST(GETDATE() AS DATE)";
            ResultSet rs = con.createStatement().executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ----------------------------------------------------------------
    // 10. Dashboard Trang Chủ: Số khách hôm nay (từ hóa đơn đang mở)
    // ----------------------------------------------------------------
    public int getSoKhachHomNay() {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT COALESCE(SUM(SoLuongKhach), 0) FROM HoaDon " +
                    "WHERE TrangThai = N'Chưa thanh toán' AND CAST(NgayTao AS DATE) = CAST(GETDATE() AS DATE)";
            ResultSet rs = con.createStatement().executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ----------------------------------------------------------------
    // 11. Dashboard Trang Chủ: Số bàn đang có khách
    // ----------------------------------------------------------------
    public int getSoBanDangMo() {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT COUNT(*) FROM Ban WHERE TrangThai = N'Có Khách'";
            ResultSet rs = con.createStatement().executeQuery(sql);
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }
}
