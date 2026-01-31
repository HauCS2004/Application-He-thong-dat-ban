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
        Connection con = ConnectDB.getConnection();
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
                // FIXED: Do NOT hardcode "Đã Đặt" immediately.
                // Let syncTableStatus handle it based on time.
                syncTableStatus();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy đơn đặt bàn sắp tới gần nhất của một bàn (trong tương lai)
     * Để cảnh báo khi xếp khách vãng lai.
     */
    public DatBan getDatBanSapToi(String maBan) {
        DatBan db = null;
        try {
            Connection con = ConnectDB.getConnection();
            // Lấy đơn đặt có thời gian bắt đầu > hiện tại, sắp xếp gần nhất
            // Chỉ lấy đơn Chờ xác nhận hoặc Đã xác nhận
            String sql = "SELECT TOP 1 * FROM DatBan " +
                    "WHERE MaBan = ? " +
                    "AND ThoiGianBatDau > GETDATE() " +
                    "AND TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận') " +
                    "ORDER BY ThoiGianBatDau ASC";

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
                        (Integer) rs.getObject("MaHD"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return db;
    }

    /**
     * Lấy đặt bàn gần nhất theo mã bàn
     */
    public static DatBan getDatBanGanNhat(String maBan) {
        DatBan db = null;
        try {
            Connection con = ConnectDB.getConnection();

            // Query theo schema mới (FIXED: ASC to get nearest future booking)
            String sql = "SELECT TOP 1 * FROM DatBan " +
                    "WHERE MaBan = ? " +
                    "AND TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận') " +
                    "ORDER BY ThoiGianBatDau ASC";

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
    /**
     * Lấy danh sách đặt bàn trong khoảng thời gian (Sử dụng TIMESTAMP để filter
     * chính xác)
     */
    public ArrayList<DatBan> getDanhSachDatBan(java.util.Date tuNgay, java.util.Date denNgay) {
        ArrayList<DatBan> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();

            // Filter exact Range using Timestamp
            String sql = "SELECT * FROM DatBan " +
                    "WHERE ThoiGianBatDau BETWEEN ? AND ? " +
                    "AND TrangThai NOT IN (N'Đã hủy') " +
                    "ORDER BY ThoiGianBatDau ASC";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setTimestamp(1, new java.sql.Timestamp(tuNgay.getTime()));
            ps.setTimestamp(2, new java.sql.Timestamp(denNgay.getTime()));

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
     * Lấy danh sách đặt bàn gần đây (không lọc ngày, giới hạn số lượng)
     */
    public ArrayList<DatBan> getDanhSachDatBanGanDay(int limit) {
        ArrayList<DatBan> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();

            String sql = "SELECT TOP " + limit + " * FROM DatBan ORDER BY ThoiGianBatDau DESC";

            PreparedStatement ps = con.prepareStatement(sql);
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
            Connection con = ConnectDB.getConnection();
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
            Connection con = ConnectDB.getConnection();
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
            Connection con = ConnectDB.getConnection();
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

    /**
     * Hoàn tất đơn đặt hàng của bàn (Khi thanh toán)
     */
    public void completeBookingOfTable(String maBan) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE DatBan SET TrangThai = N'Đã hoàn tất' WHERE MaBan = ? AND TrangThai = N'Đã nhận bàn'";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maBan);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tự động hủy các đơn đặt quá 30 phút mà chưa check-in
     * Cập nhật trạng thái Booking -> "Đã hủy (Quá giờ)"
     * Cập nhật trạng thái Bàn -> "Trống"
     */
    public int autoCancelOverdueBookings() {
        int count = 0;
        try {
            Connection con = ConnectDB.getConnection();
            String validStatuses = "N'Đã xác nhận', N'Chờ xác nhận'";

            // 1. Get Tables to release
            String sqlSelect = "SELECT MaBan FROM DatBan WHERE TrangThai IN (" + validStatuses
                    + ") AND DATEDIFF(MINUTE, ThoiGianBatDau, GETDATE()) > 30";
            PreparedStatement psSel = con.prepareStatement(sqlSelect);
            ResultSet rs = psSel.executeQuery();
            while (rs.next()) {
                String maBan = rs.getString("MaBan");
                // Reset Table
                new BanDAO().updateTrangThai(maBan, "Trống");
            }

            // 2. Update Bookings
            String sqlUpdate = "UPDATE DatBan SET TrangThai = N'Đã hủy (Quá giờ)' WHERE TrangThai IN (" + validStatuses
                    + ") AND DATEDIFF(MINUTE, ThoiGianBatDau, GETDATE()) > 30";
            PreparedStatement psUp = con.prepareStatement(sqlUpdate);
            count = psUp.executeUpdate();

            if (count > 0)
                System.out.println("Auto-Cancelled " + count + " bookings.");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Lấy danh sách sắp đến giờ (trong vòng X phút)
     */
    public ArrayList<DatBan> getUpcomingBookings(int minutes) {
        ArrayList<DatBan> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            // Include 'Chờ xác nhận' to notify staff of pending requests too
            String sql = "SELECT * FROM DatBan WHERE TrangThai IN (N'Đã xác nhận', N'Chờ xác nhận') " +
                    "AND DATEDIFF(MINUTE, GETDATE(), ThoiGianBatDau) BETWEEN -15 AND ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, minutes);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new DatBan(
                        rs.getInt("MaDat"), rs.getString("MaBan"), rs.getString("TenKhachDat"),
                        rs.getString("SDT"), rs.getTimestamp("ThoiGianBatDau"), rs.getTimestamp("ThoiGianKetThuc"),
                        rs.getInt("SoLuongKhach"), rs.getString("TrangThai"), rs.getDouble("TienCoc"),
                        rs.getString("GhiChu"), rs.getTimestamp("NgayTao"), (Integer) rs.getObject("MaHD")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy danh sách quá giờ (đã trễ X phút nhưng chưa bị hủy tự động)
     */
    public ArrayList<DatBan> getOverdueBookings() {
        ArrayList<DatBan> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM DatBan WHERE TrangThai IN (N'Đã xác nhận', N'Chờ xác nhận') " +
                    "AND GETDATE() > ThoiGianBatDau";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new DatBan(
                        rs.getInt("MaDat"), rs.getString("MaBan"), rs.getString("TenKhachDat"),
                        rs.getString("SDT"), rs.getTimestamp("ThoiGianBatDau"), rs.getTimestamp("ThoiGianKetThuc"),
                        rs.getInt("SoLuongKhach"), rs.getString("TrangThai"), rs.getDouble("TienCoc"),
                        rs.getString("GhiChu"), rs.getTimestamp("NgayTao"), (Integer) rs.getObject("MaHD")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Đồng bộ trạng thái bàn với dữ liệu đặt bàn và hóa đơn
     * (Fix lỗi bàn ảo màu vàng)
     */
    public void syncTableStatus() {
        try {
            Connection con = ConnectDB.getConnection();

            // 1. Reset all to Trống first (Clean state)
            String sqlReset = "UPDATE Ban SET TrangThai = N'Trống'";
            con.createStatement().executeUpdate(sqlReset);

            // 2. Mark 'Có Khách' for tables with Active Unpaid Invoice
            String sqlServing = "UPDATE Ban SET TrangThai = N'Có Khách' WHERE MaBan IN (SELECT MaBan FROM HoaDon WHERE TrangThai = 0)";
            con.createStatement().executeUpdate(sqlServing);

            // 3. Mark 'Đã Đặt' for Confirmed/Pending Bookings in valid time range (Start -
            // 30m <= Now <= End)
            // Only if not already 'Có Khách'
            String sqlBooked = "UPDATE Ban SET TrangThai = N'Đã Đặt' WHERE TrangThai = N'Trống' AND MaBan IN (" +
                    "SELECT MaBan FROM DatBan WHERE TrangThai IN (N'Đã xác nhận', N'Chờ xác nhận') " +
                    "AND GETDATE() BETWEEN DATEADD(MINUTE, -30, ThoiGianBatDau) AND ThoiGianKetThuc)";
            con.createStatement().executeUpdate(sqlBooked);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DatBan getDatBanByID(int maDat) {
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT * FROM DatBan WHERE MaDat = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maDat);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new DatBan(
                        rs.getInt("MaDat"),
                        rs.getString("MaBan"),
                        rs.getString("TenKhachDat"),
                        rs.getString("SDT"),
                        rs.getTimestamp("ThoiGianBatDau"),
                        rs.getTimestamp("ThoiGianKetThuc"),
                        rs.getInt("SoLuongKhach"),
                        rs.getString("TrangThai"), // 8. Status
                        rs.getDouble("TienCoc"), // 9. Deposit
                        rs.getString("GhiChu"), // 10. Note
                        rs.getTimestamp("NgayTao"), // 11. Created
                        (Integer) rs.getObject("MaHD")); // 12. Invoice ID (Nullable)
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
