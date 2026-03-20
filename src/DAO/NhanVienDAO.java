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
                rs.getString("SoDienThoai"),
                rs.getString("Email"),
                rs.getDate("NgayVaoLam"));
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
                    "WHERE nv.MaNV = ? AND tk.MatKhau = ?";
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
    public ArrayList<NhanVien> getAll() {
        ArrayList<NhanVien> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            // LEFT JOIN để lấy vaiTro
            String sql = "SELECT nv.*, tk.VaiTro " +
                    "FROM NhanVien nv LEFT JOIN TaiKhoan tk ON nv.MaNV = tk.MaTK";
            PreparedStatement ps = con.prepareStatement(sql);
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

    // ----------------------------------------------------------------
    // 5. Thêm nhân viên mới (kèm tạo TaiKhoan)
    // ----------------------------------------------------------------
    public boolean insert(NhanVien nv, String matKhau, String vaiTro) {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            // Insert NhanVien
            String sqlNV = "INSERT INTO NhanVien (MaNV, TenNV, SoDienThoai, Email, NgayVaoLam) " +
                    "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement psNV = con.prepareStatement(sqlNV);
            psNV.setString(1, nv.getMaNV());
            psNV.setString(2, nv.getTenNV());
            psNV.setString(3, nv.getSoDienThoai());
            psNV.setString(4, nv.getEmail());
            psNV.setDate(5, nv.getNgayVaoLam() != null
                    ? new java.sql.Date(nv.getNgayVaoLam().getTime())
                    : new java.sql.Date(System.currentTimeMillis()));
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

            String sqlNV = "UPDATE NhanVien SET TenNV=?, SoDienThoai=?, Email=?, NgayVaoLam=? WHERE MaNV=?";
            PreparedStatement psNV = con.prepareStatement(sqlNV);
            psNV.setString(1, nv.getTenNV());
            psNV.setString(2, nv.getSoDienThoai());
            psNV.setString(3, nv.getEmail());
            psNV.setDate(4, nv.getNgayVaoLam() != null
                    ? new java.sql.Date(nv.getNgayVaoLam().getTime())
                    : new java.sql.Date(System.currentTimeMillis()));
            psNV.setString(5, nv.getMaNV());
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

    // ----------------------------------------------------------------
    // 7. Xóa nhân viên (TaiKhoan tự xóa cascade)
    // ----------------------------------------------------------------
    public boolean delete(String maNV) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "DELETE FROM NhanVien WHERE MaNV = ?";
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
    public ArrayList<NhanVien> timKiem(String keyword) {
        ArrayList<NhanVien> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT nv.*, tk.VaiTro FROM NhanVien nv " +
                    "LEFT JOIN TaiKhoan tk ON nv.MaNV = tk.MaTK " +
                    "WHERE nv.TenNV LIKE ? OR nv.SoDienThoai LIKE ? OR nv.MaNV LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            String q = "%" + keyword + "%";
            ps.setString(1, q);
            ps.setString(2, q);
            ps.setString(3, q);
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
}
