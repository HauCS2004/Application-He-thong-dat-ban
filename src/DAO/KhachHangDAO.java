package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import connectDB.ConnectDB;
import Entity.KhachHang;

public class KhachHangDAO {

    // 1. Lấy tất cả khách hàng (chỉ lấy khách hàng đang hoạt động, TrangThai = 1)
    public ArrayList<KhachHang> getAll() {
        return getAll("Đang hoạt động");
    }

    public ArrayList<KhachHang> getAll(String statusFilter) {
        ArrayList<KhachHang> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM KhachHang";
            if (statusFilter.equals("Đang hoạt động")) {
                sql += " WHERE TrangThai = 1";
            } else if (statusFilter.equals("Ngừng hoạt động")) {
                sql += " WHERE TrangThai = 0";
            }
            
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new KhachHang(
                        rs.getString("SoDienThoai"),
                        rs.getString("TenKhach"),
                        rs.getInt("DiemTichLuy")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Thêm khách
    public boolean insert(KhachHang kh) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "INSERT INTO KhachHang(SoDienThoai, TenKhach, DiemTichLuy) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, kh.getSoDienThoai());
            ps.setString(2, kh.getTenKhach());
            ps.setInt(3, kh.getDiemTichLuy()); // Mặc định thường là 0
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Sửa khách (Giữ PK cũ)
    public boolean update(KhachHang kh) {
        return update(kh, kh.getSoDienThoai());
    }

    // 3.1 Sửa khách (hỗ trợ đổi SDT)
    public boolean update(KhachHang kh, String oldSDT) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE KhachHang SET SoDienThoai=?, TenKhach=?, DiemTichLuy=? WHERE SoDienThoai=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, kh.getSoDienThoai());
            ps.setString(2, kh.getTenKhach());
            ps.setInt(3, kh.getDiemTichLuy());
            ps.setString(4, oldSDT);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. Xóa khách (Soft Delete: Đổi trạng thái thành 0)
    public boolean delete(String sdt) {
        try {
            Connection con = ConnectDB.getConnection();
            // Cập nhật trạng thái thành 0 thay vì xóa vật lý
            String sql = "UPDATE KhachHang SET TrangThai = 0 WHERE SoDienThoai=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sdt);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. Tìm kiếm (Theo Tên hoặc SĐT)
    public ArrayList<KhachHang> timKiem(String keyword) {
        return timKiem(keyword, "Đang hoạt động");
    }

    public ArrayList<KhachHang> timKiem(String keyword, String statusFilter) {
        ArrayList<KhachHang> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM KhachHang WHERE (TenKhach LIKE ? OR SoDienThoai LIKE ?)";
            
            if (statusFilter.equals("Đang hoạt động")) {
                sql += " AND TrangThai = 1";
            } else if (statusFilter.equals("Ngừng hoạt động")) {
                sql += " AND TrangThai = 0";
            }

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new KhachHang(
                        rs.getString("SoDienThoai"),
                        rs.getString("TenKhach"),
                        rs.getInt("DiemTichLuy")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 6. Thêm khách mới (shorthand)

    public boolean themKhachMoi(String sdt, String ten) {
        // System.out.println("DEBUG: Adding new customer: " + ten);
        KhachHang kh = new KhachHang(sdt, ten, 0);
        return insert(kh);
    }

    // 6. Kiểm tra tồn tại
    public boolean checkTonTai(String sdt) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT 1 FROM KhachHang WHERE SoDienThoai=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sdt);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 8. Tích điểm (1 điểm = 10,000đ)
    // 8. Tích điểm (1 điểm = 10,000đ)
    public void tichDiem(String sdt, double tongTien) {
        try {
            int diemThem = (int) (tongTien / 10000);
            System.out.println("DEBUG: Tich diem - SDT: " + sdt + " - Tien: " + tongTien + " - Diem them: " + diemThem);

            Connection con = ConnectDB.getConnection();
            // Sử dụng ISNULL để xử lý trường hợp DiemTichLuy ban đầu là NULL
            String sql = "UPDATE KhachHang SET DiemTichLuy = ISNULL(DiemTichLuy, 0) + ? WHERE SoDienThoai = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, diemThem);
            ps.setString(2, sdt);
            int rows = ps.executeUpdate();
            System.out.println("DEBUG: Updated rows: " + rows);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 9. Lấy phần trăm giảm giá VIP
    public int getPhanTramGiam(String sdt) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT DiemTichLuy FROM KhachHang WHERE SoDienThoai = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sdt);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int diem = rs.getInt(1);
                // Quy tắc: > 1000đ = 15%, > 500đ = 10%, > 200đ = 5%, còn lại 0%
                if (diem >= 1000)
                    return 15;
                if (diem >= 500)
                    return 10;
                if (diem >= 200)
                    return 5;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 10. Lấy tên khách hàng
    public String getTenKhachHang(String sdt) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT TenKhach FROM KhachHang WHERE SoDienThoai = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sdt);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("TenKhach");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 12. Lấy thông tin khách hàng đầy đủ theo SĐT
    public KhachHang getBySDT(String sdt) {
        KhachHang kh = null;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM KhachHang WHERE SoDienThoai = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sdt);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                kh = new KhachHang();
                kh.setSoDienThoai(rs.getString("SoDienThoai"));
                kh.setTenKhach(rs.getString("TenKhach"));
                kh.setEmail(rs.getString("Email"));
                kh.setDiemTichLuy(rs.getInt("DiemTichLuy"));
                kh.setHangVIP(rs.getString("HangVIP")); // Quan trọng
                // Có thể map thêm các trường khác nếu cần
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return kh;
    }

    // 11. Tính lại toàn bộ điểm từ lịch sử hóa đơn
    public boolean resetVaTinhLaiDiem() {
        try {
            Connection con = ConnectDB.getConnection();
            String sqlCalc = "UPDATE KhachHang " +
                    "SET DiemTichLuy = ( " +
                    "    SELECT ISNULL(SUM(CAST(ThanhTien / 10000 AS INT)), 0) " +
                    "    FROM HoaDon " +
                    "    WHERE HoaDon.SDT_Khach = KhachHang.SoDienThoai " +
                    "    AND HoaDon.TrangThai = N'Đã thanh toán' " + // Fix TrangThai condition
                    ")";

            int rows = con.createStatement().executeUpdate(sqlCalc);
            System.out.println("DEBUG: Recalculated points for " + rows + " customers.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 12. Lấy lịch sử tích điểm của khách hàng
    public ArrayList<Object[]> getLichSuTichDiem(String sdt) {
        ArrayList<Object[]> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT MaHD, NgayTao, ThanhTien, FLOOR(ThanhTien / 10000) as DiemCong " +
                         "FROM HoaDon " +
                         "WHERE SDT_Khach = ? AND TrangThai = N'Đã thanh toán' " +
                         "ORDER BY NgayTao DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sdt);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("MaHD"),
                    rs.getTimestamp("NgayTao"),
                    rs.getDouble("ThanhTien"),
                    rs.getInt("DiemCong")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 13. Lấy thông tin chi tiết khách hàng (số lần ăn, tổng chi tiêu, lần ăn cuối, món yêu thích)
    public Object[] getThongTinChiTiet(String sdt) {
        int soLanAn = 0;
        double tongChiTieu = 0;
        java.sql.Timestamp lanAnCuoi = null;
        String monYeuThich = "Chưa có";
        try {
            Connection con = ConnectDB.getConnection();
            // Số lần ăn + tổng chi tiêu + lần ăn cuối
            String sql1 = "SELECT COUNT(*) as SoLan, ISNULL(SUM(TongTien), 0) as TongTien, MAX(NgayTao) as LanCuoi " +
                    "FROM HoaDon WHERE SDT_Khach = ? AND TrangThai = N'Đã thanh toán'";
            PreparedStatement ps1 = con.prepareStatement(sql1);
            ps1.setString(1, sdt);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                soLanAn = rs1.getInt("SoLan");
                tongChiTieu = rs1.getDouble("TongTien");
                lanAnCuoi = rs1.getTimestamp("LanCuoi");
            }

            // Món yêu thích (món gọi nhiều nhất)
            String sql2 = "SELECT TOP 1 m.TenMon FROM ChiTietHoaDon ct " +
                    "JOIN HoaDon hd ON ct.MaHD = hd.MaHD " +
                    "JOIN MonAn m ON ct.MaMon = m.MaMon " +
                    "WHERE hd.SDT_Khach = ? AND hd.TrangThai = N'Đã thanh toán' " +
                    "GROUP BY m.TenMon ORDER BY SUM(ct.SoLuong) DESC";
            PreparedStatement ps2 = con.prepareStatement(sql2);
            ps2.setString(1, sdt);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                monYeuThich = rs2.getString("TenMon");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Object[]{soLanAn, tongChiTieu, lanAnCuoi, monYeuThich};
    }

    // 14. Lấy danh sách hóa đơn gần đây của khách hàng (top 10)
    public ArrayList<Object[]> getHoaDonGanDay(String sdt) {
        ArrayList<Object[]> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT TOP 10 hd.MaHD, hd.NgayTao, hd.TongTien, hd.MaBan " +
                    "FROM HoaDon hd " +
                    "WHERE hd.SDT_Khach = ? AND hd.TrangThai = N'Đã thanh toán' " +
                    "ORDER BY hd.NgayTao DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sdt);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("MaHD"),
                    rs.getTimestamp("NgayTao"),
                    rs.getDouble("TongTien"),
                    rs.getString("MaBan")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
