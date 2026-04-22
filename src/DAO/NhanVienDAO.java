package DAO;

import java.sql.*;
import java.util.ArrayList;
import connectDB.ConnectDB;
import Entity.NhanVien;
import Entity.TaiKhoan;

/**
 * NhanVienDAO — Sau GĐ1: tách TaiKhoan ra bảng riêng.
 * Login kiểm tra cả NhanVien + TaiKhoan (JOIN).
 */
public class NhanVienDAO {

    // ----------------------------------------------------------------
    // Helper: map ResultSet -> NhanVien (không có TaiKhoan)
    // ----------------------------------------------------------------
    private NhanVien mapNhanVien(ResultSet rs) throws SQLException {
        NhanVien nv = new NhanVien(
                rs.getString("MaNV"),
                rs.getString("TenNV"),
                rs.getString("GioiTinh"),
                rs.getString("SoDienThoai"),
                rs.getString("Email"),
                rs.getString("CCCD"),
                rs.getDate("NgayVaoLam"),
                rs.getString("TrangThai"));
        return nv;
    }

    // ----------------------------------------------------------------
    // Helper: load TaiKhoan cho một NhanVien
    // ----------------------------------------------------------------
    private TaiKhoan loadTaiKhoan(Connection con, String maNV) throws SQLException {
        String sql = "SELECT * FROM TaiKhoan WHERE MaTK = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, maNV);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new TaiKhoan(rs.getString("MaTK"), rs.getString("MatKhau"), rs.getString("VaiTro"));
        }
        return null;
    }

    // ----------------------------------------------------------------
    // 1. Login (JOIN NhanVien + TaiKhoan)
    // ----------------------------------------------------------------
    public NhanVien login(String maNV, String matKhau) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT nv.*, tk.MatKhau, tk.VaiTro " +
                    "FROM NhanVien nv " +
                    "INNER JOIN TaiKhoan tk ON nv.MaNV = tk.MaTK " +
                    "WHERE nv.MaNV = ? AND tk.MatKhau = ? AND nv.TrangThai = N'Đang làm việc'";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maNV);
            ps.setString(2, matKhau);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                NhanVien nv = mapNhanVien(rs);
                TaiKhoan tk = new TaiKhoan(
                        rs.getString("MaNV"),
                        rs.getString("MatKhau"),
                        rs.getString("VaiTro"));
                nv.setTaiKhoan(tk);
                System.out.println("✅ Login success: " + nv.getTenNV());
                return nv;
            } else {
                System.out.println("❌ Login failed: Sai mã NV hoặc mật khẩu");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------------------------------------------
    // 2. Lấy thông tin nhân viên theo mã (kèm TaiKhoan)
    // ----------------------------------------------------------------
    public NhanVien getByMaNV(String maNV) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM NhanVien WHERE MaNV = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                NhanVien nv = mapNhanVien(rs);
                nv.setTaiKhoan(loadTaiKhoan(con, maNV));
                return nv;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------------------------------------------
    // 3. Đổi mật khẩu (cập nhật TaiKhoan)
    // ----------------------------------------------------------------
    public boolean changePassword(String maNV, String oldPass, String newPass) {
        try {
            Connection con = ConnectDB.getConnection();
            // Xác minh mật khẩu cũ
            String checkSql = "SELECT COUNT(*) FROM TaiKhoan WHERE MaTK = ? AND MatKhau = ?";
            PreparedStatement checkPs = con.prepareStatement(checkSql);
            checkPs.setString(1, maNV);
            checkPs.setString(2, oldPass);
            ResultSet rs = checkPs.executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) {
                System.out.println("❌ Mật khẩu cũ không đúng!");
                return false;
            }
            // Cập nhật mật khẩu mới
            String updateSql = "UPDATE TaiKhoan SET MatKhau = ? WHERE MaTK = ?";
            PreparedStatement updatePs = con.prepareStatement(updateSql);
            updatePs.setString(1, newPass);
            updatePs.setString(2, maNV);
            return updatePs.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ----------------------------------------------------------------
    // 4. Lấy tất cả nhân viên (kèm vaiTro)
    // ----------------------------------------------------------------
    public ArrayList<NhanVien> getAll(String trangThai) {
        ArrayList<NhanVien> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT nv.*, tk.VaiTro " +
                    "FROM NhanVien nv LEFT JOIN TaiKhoan tk ON nv.MaNV = tk.MaTK ";
            
            if (!"Tất cả".equals(trangThai)) {
                sql += "WHERE nv.TrangThai = ? ";
            }
            
            PreparedStatement ps = con.prepareStatement(sql);
            if (!"Tất cả".equals(trangThai)) {
                ps.setString(1, trangThai);
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                NhanVien nv = mapNhanVien(rs);
                String vaiTro = rs.getString("VaiTro");
                if (vaiTro != null) {
                    nv.setTaiKhoan(new TaiKhoan(nv.getMaNV(), "", vaiTro));
                }
                list.add(nv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<NhanVien> getAll() {
        return getAll("Đang làm việc");
    }

    // ----------------------------------------------------------------
    // 5. Thêm nhân viên mới (kèm tạo TaiKhoan)
    // ----------------------------------------------------------------
    public boolean insert(NhanVien nv, String matKhau, String vaiTro) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            // Insert NhanVien
            String sqlNV = "INSERT INTO NhanVien (MaNV, TenNV, GioiTinh, SoDienThoai, Email, CCCD, NgayVaoLam, TrangThai) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement psNV = con.prepareStatement(sqlNV);
            psNV.setString(1, nv.getMaNV());
            psNV.setString(2, nv.getTenNV());
            psNV.setString(3, nv.getGioiTinh() != null ? nv.getGioiTinh() : "Nam"); // default
            psNV.setString(4, nv.getSoDienThoai());
            psNV.setString(5, nv.getEmail());
            psNV.setString(6, nv.getCccd() != null ? nv.getCccd() : "");
            psNV.setDate(7, nv.getNgayVaoLam() != null
                    ? new java.sql.Date(nv.getNgayVaoLam().getTime())
                    : new java.sql.Date(System.currentTimeMillis()));
            psNV.setString(8, nv.getTrangThai() != null ? nv.getTrangThai() : "Đang làm việc");
            psNV.executeUpdate();

            // Insert TaiKhoan
            String sqlTK = "INSERT INTO TaiKhoan (MaTK, MatKhau, VaiTro) VALUES (?, ?, ?)";
            PreparedStatement psTK = con.prepareStatement(sqlTK);
            psTK.setString(1, nv.getMaNV());
            psTK.setString(2, matKhau);
            psTK.setString(3, vaiTro != null ? vaiTro : "Nhân viên");
            psTK.executeUpdate();

            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (con != null)
                    con.rollback();
            } catch (Exception ex) {
            }
        } finally {
            try {
                if (con != null)
                    con.setAutoCommit(true);
            } catch (Exception ex) {
            }
        }
        return false;
    }

    // Overload cho backward-compat (không có vaiTro)
    public boolean insert(NhanVien nv) {
        String matKhau = (nv.getTaiKhoan() != null) ? nv.getTaiKhoan().getMatKhau() : "123";
        String vaiTro = (nv.getTaiKhoan() != null) ? nv.getTaiKhoan().getVaiTro() : "Nhân viên";
        return insert(nv, matKhau, vaiTro);
    }

    // ----------------------------------------------------------------
    // 6. Cập nhật thông tin nhân viên (và vaiTro nếu có TaiKhoan)
    // ----------------------------------------------------------------
    public boolean update(NhanVien nv) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            String sqlNV = "UPDATE NhanVien SET TenNV=?, GioiTinh=?, SoDienThoai=?, Email=?, CCCD=?, NgayVaoLam=?, TrangThai=? WHERE MaNV=?";
            PreparedStatement psNV = con.prepareStatement(sqlNV);
            psNV.setString(1, nv.getTenNV());
            psNV.setString(2, nv.getGioiTinh() != null ? nv.getGioiTinh() : "Nam");
            psNV.setString(3, nv.getSoDienThoai());
            psNV.setString(4, nv.getEmail());
            psNV.setString(5, nv.getCccd() != null ? nv.getCccd() : "");
            psNV.setDate(6, nv.getNgayVaoLam() != null
                    ? new java.sql.Date(nv.getNgayVaoLam().getTime())
                    : new java.sql.Date(System.currentTimeMillis()));
            psNV.setString(7, nv.getTrangThai() != null ? nv.getTrangThai() : "Đang làm việc");
            psNV.setString(8, nv.getMaNV());
            psNV.executeUpdate();

            // Cập nhật VaiTro nếu có
            if (nv.getTaiKhoan() != null) {
                String sqlTK = "UPDATE TaiKhoan SET VaiTro=? WHERE MaTK=?";
                PreparedStatement psTK = con.prepareStatement(sqlTK);
                psTK.setString(1, nv.getTaiKhoan().getVaiTro());
                psTK.setString(2, nv.getMaNV());
                psTK.executeUpdate();
            }

            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (con != null)
                    con.rollback();
            } catch (Exception ex) {
            }
        } finally {
            try {
                if (con != null)
                    con.setAutoCommit(true);
            } catch (Exception ex) {
            }
        }
        return false;
    }

    // 7. Xóa nhân viên (Soft Delete)
    public boolean delete(String maNV) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE NhanVien SET TrangThai = N'Đã nghỉ' WHERE MaNV = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maNV);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ----------------------------------------------------------------
    // 8. Tìm kiếm nhân viên
    // ----------------------------------------------------------------
    public ArrayList<NhanVien> timKiem(String keyword, String trangThai) {
        ArrayList<NhanVien> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT nv.*, tk.VaiTro FROM NhanVien nv " +
                    "LEFT JOIN TaiKhoan tk ON nv.MaNV = tk.MaTK " +
                    "WHERE (nv.TenNV LIKE ? OR nv.SoDienThoai LIKE ? OR nv.MaNV LIKE ?) ";
            
            if (!"Tất cả".equals(trangThai)) {
                sql += "AND nv.TrangThai = ? ";
            }
                    
            PreparedStatement ps = con.prepareStatement(sql);
            String q = "%" + keyword + "%";
            ps.setString(1, q);
            ps.setString(2, q);
            ps.setString(3, q);
            
            if (!"Tất cả".equals(trangThai)) {
                ps.setString(4, trangThai);
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                NhanVien nv = mapNhanVien(rs);
                String vaiTro = rs.getString("VaiTro");
                if (vaiTro != null)
                    nv.setTaiKhoan(new TaiKhoan(nv.getMaNV(), "", vaiTro));
                list.add(nv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<NhanVien> timKiem(String keyword) {
        return timKiem(keyword, "Đang làm việc");
    }

    // ----------------------------------------------------------------
    // 9. Lấy tất cả nhân viên (kèm MatKhau) dùng cho Quản lý tài khoản
    // ----------------------------------------------------------------
    public ArrayList<NhanVien> getAllWithPassword(String trangThai) {
        ArrayList<NhanVien> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT nv.*, tk.VaiTro, tk.MatKhau " +
                    "FROM NhanVien nv LEFT JOIN TaiKhoan tk ON nv.MaNV = tk.MaTK ";
            
            if (!"Tất cả".equals(trangThai)) {
                sql += "WHERE nv.TrangThai = ?";
            }
            
            PreparedStatement ps = con.prepareStatement(sql);
            if (!"Tất cả".equals(trangThai)) {
                ps.setString(1, trangThai);
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                NhanVien nv = mapNhanVien(rs);
                String vaiTro = rs.getString("VaiTro");
                String matKhau = rs.getString("MatKhau");
                if (vaiTro != null) {
                    nv.setTaiKhoan(new TaiKhoan(nv.getMaNV(), matKhau != null ? matKhau : "", vaiTro));
                }
                list.add(nv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public ArrayList<NhanVien> getAllWithPassword() {
        return getAllWithPassword("Đang làm việc");
    }

    // ----------------------------------------------------------------
    // 10. Tìm kiếm nhân viên (kèm MatKhau) dùng cho Quản lý tài khoản
    // ----------------------------------------------------------------
    public ArrayList<NhanVien> timKiemWithPassword(String keyword, String trangThai) {
        ArrayList<NhanVien> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT nv.*, tk.VaiTro, tk.MatKhau FROM NhanVien nv " +
                    "LEFT JOIN TaiKhoan tk ON nv.MaNV = tk.MaTK " +
                    "WHERE (nv.TenNV LIKE ? OR nv.SoDienThoai LIKE ? OR nv.MaNV LIKE ?) ";
            
            if (!"Tất cả".equals(trangThai)) {
                sql += "AND nv.TrangThai = ?";
            }
            
            PreparedStatement ps = con.prepareStatement(sql);
            String q = "%" + keyword + "%";
            ps.setString(1, q);
            ps.setString(2, q);
            ps.setString(3, q);
            
            if (!"Tất cả".equals(trangThai)) {
                ps.setString(4, trangThai);
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                NhanVien nv = mapNhanVien(rs);
                String vaiTro = rs.getString("VaiTro");
                String matKhau = rs.getString("MatKhau");
                if (vaiTro != null)
                    nv.setTaiKhoan(new TaiKhoan(nv.getMaNV(), matKhau != null ? matKhau : "", vaiTro));
                list.add(nv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public ArrayList<NhanVien> timKiemWithPassword(String keyword) {
        return timKiemWithPassword(keyword, "Đang làm việc");
    }

    // ----------------------------------------------------------------
    // 11. Đổi mật khẩu (dành cho Admin/Quản lý bỏ qua MK cũ)
    // ----------------------------------------------------------------
    public boolean updatePasswordAdmin(String maNV, String newPass) {
        try {
            Connection con = ConnectDB.getConnection();
            String updateSql = "UPDATE TaiKhoan SET MatKhau = ? WHERE MaTK = ?";
            PreparedStatement updatePs = con.prepareStatement(updateSql);
            updatePs.setString(1, newPass);
            updatePs.setString(2, maNV);
            return updatePs.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ----------------------------------------------------------------
    // 12. Lấy thông tin chi tiết nhân viên (số hóa đơn, tổng doanh thu, lần phục vụ cuối)
    // ----------------------------------------------------------------
    public Object[] getThongTinChiTiet(String maNV) {
        int soHoaDon = 0;
        double tongDoanhThu = 0;
        java.sql.Timestamp lanLamCuoi = null;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT COUNT(*) as SoHD, ISNULL(SUM(TongTien), 0) as TongTien, MAX(NgayTao) as LanCuoi " +
                    "FROM HoaDon WHERE MaNV = ? AND TrangThai = N'Đã thanh toán'";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                soHoaDon = rs.getInt("SoHD");
                tongDoanhThu = rs.getDouble("TongTien");
                lanLamCuoi = rs.getTimestamp("LanCuoi");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Object[]{soHoaDon, tongDoanhThu, lanLamCuoi};
    }

    // ----------------------------------------------------------------
    // 13. Lấy danh sách hóa đơn nhân viên đã phục vụ (top 10 gần nhất)
    // ----------------------------------------------------------------
    public ArrayList<Object[]> getHoaDonPhucVu(String maNV) {
        ArrayList<Object[]> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT TOP 10 hd.MaHD, hd.NgayTao, hd.TongTien, hd.MaBan, " +
                    "kh.TenKhach " +
                    "FROM HoaDon hd " +
                    "LEFT JOIN KhachHang kh ON hd.SDT_Khach = kh.SoDienThoai " +
                    "WHERE hd.MaNV = ? AND hd.TrangThai = N'Đã thanh toán' " +
                    "ORDER BY hd.NgayTao DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maNV);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("MaHD"),
                    rs.getTimestamp("NgayTao"),
                    rs.getDouble("TongTien"),
                    rs.getString("MaBan"),
                    rs.getString("TenKhach")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
