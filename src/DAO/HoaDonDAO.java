package DAO;

import java.sql.*;
import java.util.ArrayList;
import connectDB.ConnectDB;
import Entity.HoaDon;

/**
 * HoaDonDAO — Cập nhật GĐ4: đọc/ghi các cột VAT, phí, giảm giá, thành tiền.
 * thanhToan() gọi SP_ThanhToan thay vì UPDATE trực tiếp.
 */
public class HoaDonDAO {

    // Helper: map ResultSet -> HoaDon (đọc đủ 13 cột mới)
    private HoaDon map(ResultSet rs) throws SQLException {
        return new HoaDon(
                rs.getInt("MaHD"),
                rs.getTimestamp("NgayTao"),
                rs.getDouble("TongTien"),
                rs.getDouble("PhanTramVAT"),
                rs.getDouble("PhiPhucVu"),
                rs.getDouble("TienGiamGia"),
                rs.getDouble("ThanhTien"),
                rs.getString("TrangThai"),
                rs.getString("PhuongThucThanhToan"),
                rs.getTimestamp("ThoiGianThanhToan"),
                rs.getString("MaBan"),
                rs.getInt("SoLuongKhach"),
                rs.getString("SDT_Khach"),
                rs.getString("GhiChu"),
                rs.getString("MaNV"));
    }

    // ----------------------------------------------------------------
    // 1. Lấy mã hóa đơn chưa thanh toán của bàn (-1 nếu không có)
    // ----------------------------------------------------------------
    public int getMaHDByBan(String maBan) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT MaHD FROM HoaDon WHERE MaBan=? AND TrangThai=N'Chưa thanh toán'");
            ps.setString(1, maBan);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt("MaHD");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ----------------------------------------------------------------
    // 2. Lấy thông tin chi tiết hóa đơn
    // ----------------------------------------------------------------
    public HoaDon getThongTinHoaDon(int maHD) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM HoaDon WHERE MaHD=?");
            ps.setInt(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return map(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------------------------------------------
    // 3. Cập nhật SĐT khách vào hóa đơn
    // ----------------------------------------------------------------
    public boolean updateSdtKhach(int maHD, String sdt) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE HoaDon SET SDT_Khach=? WHERE MaHD=?");
            ps.setString(1, sdt);
            ps.setInt(2, maHD);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ----------------------------------------------------------------
    // 4. Tổng tiền tạm tính (chỉ tiền món, chưa VAT)
    // ----------------------------------------------------------------
    public double getTongTienTamTinh(int maHD) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT ISNULL(SUM(SoLuong*DonGia),0) FROM ChiTietHoaDon WHERE MaHD=?");
            ps.setInt(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getDouble(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ----------------------------------------------------------------
    // 5. Thêm hóa đơn mới
    // ----------------------------------------------------------------
    public int insertHoaDon(HoaDon hd) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = ConnectDB.getConnection();
            String sql = "INSERT INTO HoaDon(MaBan, MaNV, NgayTao, TrangThai, SoLuongKhach, " +
                    "SDT_Khach, GhiChu, PhanTramVAT, PhiPhucVu) " +
                    "VALUES(?, ?, GETDATE(), N'Chưa thanh toán', ?, ?, ?, ?, ?)";
            ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, hd.getMaBan());
            ps.setString(2, hd.getMaNV());
            ps.setInt(3, hd.getSoLuongKhach());
            ps.setString(4, hd.getSdtKhach());
            ps.setString(5, hd.getGhiChu());
            ps.setDouble(6, hd.getPhanTramVAT());
            ps.setDouble(7, hd.getPhiPhucVu());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int insert(HoaDon hd) {
        return insertHoaDon(hd);
    }

    // ----------------------------------------------------------------
    // 6. [CẬP NHẬT GĐ4] Thanh toán — gọi SP_ThanhToan
    // ----------------------------------------------------------------
    public boolean thanhToan(int maHD, String maKM, String sdt, double vat, double phiPhucVu) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "{CALL SP_ThanhToan(?, ?, ?, ?, ?, ?)}";
            CallableStatement cs = con.prepareCall(sql);
            cs.setInt(1, maHD);
            if (maKM != null && !maKM.isEmpty())
                cs.setString(2, maKM);
            else
                cs.setNull(2, Types.VARCHAR);
            if (sdt != null && !sdt.isEmpty())
                cs.setString(3, sdt);
            else
                cs.setNull(3, Types.VARCHAR);
            cs.setDouble(4, vat);
            cs.setDouble(5, phiPhucVu);
            cs.setString(6, "Tiền mặt");
            cs.execute();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Overload đơn giản (dùng giá trị mặc định VAT=10, phí=5)
    public boolean thanhToan(int maHD, double tongTien) {
        // Gọi SP với VAT/phí mặc định, không KM, không SDT
        return thanhToan(maHD, null, null, 10, 5);
    }

    // ----------------------------------------------------------------
    // 7. Lấy số lượng khách
    // ----------------------------------------------------------------
    public int getSoLuongKhach(int maHD) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT SoLuongKhach FROM HoaDon WHERE MaHD=?");
            ps.setInt(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public int getSoLuongKhach(String maBan) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT SoLuongKhach FROM HoaDon WHERE MaBan=? AND TrangThai=N'Chưa thanh toán'");
            ps.setString(1, maBan);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ----------------------------------------------------------------
    // 8. Chi tiết hóa đơn (String[] cho JTable)
    // ----------------------------------------------------------------
    public ArrayList<String[]> getChiTietHoaDon(int maHD) {
        ArrayList<String[]> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT m.MaMon, m.TenMon, c.SoLuong, c.DonGia, " +
                    "(c.SoLuong*c.DonGia) as ThanhTien, c.GhiChu " +
                    "FROM ChiTietHoaDon c JOIN MonAn m ON c.MaMon=m.MaMon WHERE MaHD=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maHD);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[] {
                        rs.getString("TenMon"),
                        String.valueOf(rs.getInt("SoLuong")),
                        String.valueOf((int) rs.getDouble("DonGia")),
                        String.valueOf((int) rs.getDouble("ThanhTien")),
                        rs.getString("GhiChu"),
                        rs.getString("MaMon")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ----------------------------------------------------------------
    // 9. Lịch sử hóa đơn (Đã thanh toán) — trả về ThanhTien thay TongTien
    // ----------------------------------------------------------------
    public ArrayList<HoaDon> getLichSuHoaDon(java.util.Date fromDate,
            java.util.Date toDate, String search) {
        ArrayList<HoaDon> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            StringBuilder sql = new StringBuilder("SELECT * FROM HoaDon WHERE TrangThai=N'Đã thanh toán'");
            
            if (fromDate != null && toDate != null) {
                sql.append(" AND NgayTao BETWEEN ? AND ?");
            }
            if (search != null && !search.isEmpty()) {
                sql.append(" AND (SDT_Khach LIKE ? OR CONVERT(NVARCHAR, MaHD) LIKE ?)");
            }
            sql.append(" ORDER BY NgayTao DESC");

            PreparedStatement ps = con.prepareStatement(sql.toString());
            int pIndex = 1;

            if (fromDate != null && toDate != null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(fromDate);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0); cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0); cal.set(java.util.Calendar.MILLISECOND, 0);
                ps.setTimestamp(pIndex++, new Timestamp(cal.getTimeInMillis()));
                
                cal.setTime(toDate);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23); cal.set(java.util.Calendar.MINUTE, 59);
                cal.set(java.util.Calendar.SECOND, 59); cal.set(java.util.Calendar.MILLISECOND, 999);
                ps.setTimestamp(pIndex++, new Timestamp(cal.getTimeInMillis()));
            }

            if (search != null && !search.isEmpty()) {
                ps.setString(pIndex++, "%" + search + "%");
                ps.setString(pIndex++, "%" + search + "%");
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(map(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
