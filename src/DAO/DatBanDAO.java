package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import connectDB.ConnectDB;
import Entity.DatBan;

/**
 * DatBanDAO — Rewrite GĐ2: query qua ChiTietDatBan thay vì DatBan.MaBan trực
 * tiếp.
 */
public class DatBanDAO {

    // ----------------------------------------------------------------
    // Helper: load danh sách bàn cho một MaDat
    // ----------------------------------------------------------------
    private List<String> loadDanhSachBan(Connection con, int maDat) throws SQLException {
        List<String> list = new ArrayList<>();
        PreparedStatement ps = con.prepareStatement(
                "SELECT MaBan FROM ChiTietDatBan WHERE MaDat=?");
        ps.setInt(1, maDat);
        ResultSet rs = ps.executeQuery();
        while (rs.next())
            list.add(rs.getString("MaBan"));
        return list;
    }

    // ----------------------------------------------------------------
    // Helper: map ResultSet -> DatBan (load thêm ChiTietDatBan)
    // ----------------------------------------------------------------
    private DatBan map(ResultSet rs, Connection con) throws SQLException {
        int maDat = rs.getInt("MaDat");
        DatBan db = new DatBan(
                maDat,
                null, // maBan tạm null — sẽ load từ ChiTietDatBan
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
        // Load danh sách bàn
        try {
            db.setDanhSachBan(loadDanhSachBan(con, maDat));
        } catch (Exception e) {
            /* bỏ qua nếu lỗi */ }
        return db;
    }

    // ----------------------------------------------------------------
    // 1. Đặt bàn mới (gọi SP_DatBan — hỗ trợ nhiều bàn)
    // ----------------------------------------------------------------
    public boolean insertDatBan(DatBan db) {
        Connection con = ConnectDB.getConnection();
        try {
            // Ghép danh sách bàn thành chuỗi "B01,B02"
            String danhSachBanStr = String.join(",", db.getDanhSachBan());

            String sql = "{CALL SP_DatBan(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
            CallableStatement cs = con.prepareCall(sql);
            cs.setString(1, db.getTenKhach());
            cs.setString(2, db.getSdt());
            cs.setTimestamp(3, new Timestamp(db.getThoiGianBatDau().getTime()));
            cs.setTimestamp(4, new Timestamp(db.getThoiGianKetThuc().getTime()));
            cs.setInt(5, db.getSoLuongKhach());
            cs.setString(6, danhSachBanStr);
            cs.setString(7, db.getGhiChu());
            cs.setDouble(8, db.getTienCoc());
            cs.registerOutParameter(9, Types.INTEGER); // @KetQua
            cs.registerOutParameter(10, Types.NVARCHAR); // @ThongBao
            cs.execute();

            int ketQua = cs.getInt(9);
            System.out.println("Đặt bàn: " + cs.getString(10));
            if (ketQua == 1) {
                syncTableStatus();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ----------------------------------------------------------------
    // 2. Lấy đặt bàn sắp tới gần nhất cho một bàn (cảnh báo khi xếp vãng lai)
    // ----------------------------------------------------------------
    public DatBan getDatBanSapToi(String maBan) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT TOP 1 db.* FROM DatBan db " +
                    "INNER JOIN ChiTietDatBan ctdb ON db.MaDat = ctdb.MaDat " +
                    "WHERE ctdb.MaBan=? AND db.ThoiGianBatDau > GETDATE() " +
                    "AND db.TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận') " +
                    "ORDER BY db.ThoiGianBatDau ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maBan);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return map(rs, con);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------------------------------------------
    // 3. Lấy đặt bàn gần nhất theo mã bàn (cho Check-in)
    // ----------------------------------------------------------------
    public static DatBan getDatBanGanNhat(String maBan) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT TOP 1 db.* FROM DatBan db " +
                    "INNER JOIN ChiTietDatBan ctdb ON db.MaDat = ctdb.MaDat " +
                    "WHERE ctdb.MaBan=? " +
                    "AND db.TrangThai IN (N'Chờ xác nhận', N'Đã xác nhận') " +
                    "ORDER BY db.ThoiGianBatDau ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maBan);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int maDat = rs.getInt("MaDat");
                DatBan db = new DatBan(
                        maDat, maBan,
                        rs.getString("TenKhachDat"), rs.getString("SDT"),
                        rs.getTimestamp("ThoiGianBatDau"), rs.getTimestamp("ThoiGianKetThuc"),
                        rs.getInt("SoLuongKhach"), rs.getString("TrangThai"),
                        rs.getDouble("TienCoc"), rs.getString("GhiChu"),
                        rs.getTimestamp("NgayTao"), (Integer) rs.getObject("MaHD"));
                return db;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------------------------------------------
    // 4. Lấy danh sách đặt bàn theo khoảng thời gian
    // ----------------------------------------------------------------
    public ArrayList<DatBan> getDanhSachDatBan(java.util.Date tuNgay, java.util.Date denNgay) {
        ArrayList<DatBan> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT DISTINCT db.* FROM DatBan db " +
                    "LEFT JOIN ChiTietDatBan ctdb ON db.MaDat = ctdb.MaDat " +
                    "WHERE db.ThoiGianBatDau BETWEEN ? AND ? " +
                    "AND db.TrangThai NOT IN (N'Đã hủy') ORDER BY db.ThoiGianBatDau ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setTimestamp(1, new Timestamp(tuNgay.getTime()));
            ps.setTimestamp(2, new Timestamp(denNgay.getTime()));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(map(rs, con));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ----------------------------------------------------------------
    // 5. Lấy gần đây (không lọc ngày)
    // ----------------------------------------------------------------
    public ArrayList<DatBan> getDanhSachDatBanGanDay(int limit) {
        ArrayList<DatBan> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT TOP " + limit + " * FROM DatBan ORDER BY ThoiGianBatDau DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(map(rs, con));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ----------------------------------------------------------------
    // 6. Kiểm tra xung đột (SP_KiemTraDatBan)
    // ----------------------------------------------------------------
    public int kiemTraXungDot(String maBan, java.util.Date thoiGianBatDau,
            java.util.Date thoiGianKetThuc) {
        try {
            Connection con = ConnectDB.getConnection();
            CallableStatement cs = con.prepareCall("{CALL SP_KiemTraDatBan(?, ?, ?)}");
            cs.setString(1, maBan);
            cs.setTimestamp(2, new Timestamp(thoiGianBatDau.getTime()));
            cs.setTimestamp(3, new Timestamp(thoiGianKetThuc.getTime()));
            ResultSet rs = cs.executeQuery();
            if (rs.next())
                return rs.getInt("SoLuongXungDot");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ----------------------------------------------------------------
    // 7. Cập nhật trạng thái đặt bàn
    // ----------------------------------------------------------------
    public boolean capNhatTrangThai(int maDat, String trangThai) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE DatBan SET TrangThai=? WHERE MaDat=?");
            ps.setString(1, trangThai);
            ps.setInt(2, maDat);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ----------------------------------------------------------------
    // 8. Gợi ý bàn (SP_GoiYBan)
    // ----------------------------------------------------------------
    public ArrayList<String> goiYBan(int soKhach, java.util.Date thoiGianBatDau,
            java.util.Date thoiGianKetThuc, String maKV) {
        ArrayList<String> danhSachBan = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            CallableStatement cs = con.prepareCall("{CALL SP_GoiYBan(?, ?, ?, ?)}");
            cs.setInt(1, soKhach);
            cs.setTimestamp(2, new Timestamp(thoiGianBatDau.getTime()));
            cs.setTimestamp(3, new Timestamp(thoiGianKetThuc.getTime()));
            cs.setString(4, maKV);
            ResultSet rs = cs.executeQuery();
            while (rs.next())
                danhSachBan.add(rs.getString("MaBan"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return danhSachBan;
    }

    // ----------------------------------------------------------------
    // 9. Hoàn tất booking khi thanh toán
    // ----------------------------------------------------------------
    public void completeBookingOfTable(String maBan) {
        try {
            Connection con = ConnectDB.getConnection();
            // Qua ChiTietDatBan để tìm DatBan chứa bàn này
            String sql = "UPDATE DatBan SET TrangThai=N'Đã hoàn tất' " +
                    "WHERE MaDat IN (SELECT MaDat FROM ChiTietDatBan WHERE MaBan=?) " +
                    "AND TrangThai=N'Đã nhận bàn'";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maBan);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------
    // 10. Tự động hủy booking quá giờ
    // ----------------------------------------------------------------
    public int autoCancelOverdueBookings() {
        int count = 0;
        try {
            Connection con = ConnectDB.getConnection();
            String validStatuses = "N'Đã xác nhận', N'Chờ xác nhận'";

            // Lấy bàn cần reset
            String sqlSel = "SELECT DISTINCT ctdb.MaBan FROM DatBan db " +
                    "INNER JOIN ChiTietDatBan ctdb ON db.MaDat=ctdb.MaDat " +
                    "WHERE db.TrangThai IN (" + validStatuses + ") " +
                    "AND DATEDIFF(MINUTE, db.ThoiGianBatDau, GETDATE()) > 30";
            ResultSet rs = con.createStatement().executeQuery(sqlSel);
            while (rs.next())
                new BanDAO().updateTrangThai(rs.getString("MaBan"), "Trống");

            // Cập nhật booking
            String sqlUpd = "UPDATE DatBan SET TrangThai=N'Đã hủy (Quá giờ)' " +
                    "WHERE TrangThai IN (" + validStatuses + ") " +
                    "AND DATEDIFF(MINUTE, ThoiGianBatDau, GETDATE()) > 30";
            count = con.createStatement().executeUpdate(sqlUpd);
            if (count > 0)
                System.out.println("Auto-Cancelled " + count + " bookings.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    // ----------------------------------------------------------------
    // 11. Lấy booking sắp đến
    // ----------------------------------------------------------------
    public ArrayList<DatBan> getUpcomingBookings(int minutes) {
        ArrayList<DatBan> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT DISTINCT db.* FROM DatBan db " +
                    "LEFT JOIN ChiTietDatBan ctdb ON db.MaDat=ctdb.MaDat " +
                    "WHERE db.TrangThai IN (N'Đã xác nhận', N'Chờ xác nhận') " +
                    "AND DATEDIFF(MINUTE, GETDATE(), db.ThoiGianBatDau) BETWEEN 0 AND ?;";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, minutes);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(map(rs, con));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ----------------------------------------------------------------
    // 12. Lấy booking theo ID
    // ----------------------------------------------------------------
    public DatBan getDatBanByID(int maDat) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM DatBan WHERE MaDat=?");
            ps.setInt(1, maDat);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return map(rs, con);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------------------------------------------
    // 13. Đồng bộ trạng thái bàn
    // ----------------------------------------------------------------
    public void syncTableStatus() {
        try {
            Connection con = ConnectDB.getConnection();
            // Reset (Bỏ qua những bàn đang gộp)
            con.createStatement().executeUpdate("UPDATE Ban SET TrangThai=N'Trống' WHERE TrangThai != N'Đang Gộp'");
            // Có khách
            con.createStatement().executeUpdate(
                    "UPDATE Ban SET TrangThai=N'Có Khách' WHERE MaBan IN " +
                            "(SELECT MaBan FROM HoaDon WHERE TrangThai=N'Chưa thanh toán')");
            // Đã đặt (qua ChiTietDatBan)
            con.createStatement().executeUpdate(
                    "UPDATE Ban SET TrangThai=N'Đã Đặt' WHERE TrangThai=N'Trống' AND MaBan IN (" +
                            "SELECT ctdb.MaBan FROM DatBan db " +
                            "INNER JOIN ChiTietDatBan ctdb ON db.MaDat=ctdb.MaDat " +
                            "WHERE db.TrangThai IN (N'Đã xác nhận', N'Chờ xác nhận') " +
                            "AND GETDATE() BETWEEN DATEADD(MINUTE, -30, db.ThoiGianBatDau) AND db.ThoiGianKetThuc)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 
     * Lấy danh sách booking quá giờ (trễ 1-30 phút) — READ-ONLY, không hủy.
     * Dùng để hiển thị cho nhân viên xử lý.
     */
    public ArrayList<DatBan> getDatBanQuaGio() {
        ArrayList<DatBan> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT DISTINCT db.* FROM DatBan db " +
                    "LEFT JOIN ChiTietDatBan ctdb ON db.MaDat = ctdb.MaDat " +
                    "WHERE db.TrangThai IN (N'\u0110ã xác nhận', N'Chờ xác nhận') " +
                    "AND DATEDIFF(MINUTE, db.ThoiGianBatDau, GETDATE()) BETWEEN 1 AND 30 " +
                    "ORDER BY db.ThoiGianBatDau ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(map(rs, con));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Nhân viên hủy thủ công một booking kèm lý do ghi chú.
     * Reset bàn về Trống, ghi GhiChu vào record.
     */
    public boolean huyDatBanManual(int maDat, String ghiChu) {
        try {
            Connection con = ConnectDB.getConnection();
            // 1. Lấy danh sách bàn cần reset
            List<String> danhSachBan = loadDanhSachBan(con, maDat);
            // 2. Cập nhật trạng thái + ghi chú
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE DatBan SET TrangThai = N'\u0110ã hủy', GhiChu = ? WHERE MaDat = ?");
            ps.setString(1, ghiChu);
            ps.setInt(2, maDat);
            boolean ok = ps.executeUpdate() > 0;
            // 3. Reset bàn
            if (ok) {
                BanDAO banDAO = new BanDAO();
                for (String maBan : danhSachBan)
                    banDAO.updateTrangThai(maBan, "Trống");
            }
            return ok;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 
     * Backward-compat: trả về danh sách booking quá giờ (1-30p) — không còn tự động hủy.
     * Auto-cancel cần được gọi riêng biệt qua autoCancelOverdueBookings().
     */
    public ArrayList<DatBan> getOverdueBookings() {
        return getDatBanQuaGio();
    }
}
