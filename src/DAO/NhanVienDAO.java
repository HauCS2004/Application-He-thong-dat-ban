package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import connectDB.ConnectDB;
import Entity.NhanVien;

public class NhanVienDAO {

    /**
     * Login - Xác thực nhân viên
     * 
     * @param maNV    - Mã nhân viên
     * @param matKhau - Mật khẩu (plain text)
     * @return NhanVien object nếu login thành công, null nếu thất bại
     */
    public NhanVien login(String maNV, String matKhau) {
        NhanVien nv = null;

        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM NhanVien WHERE MaNV = ? AND MatKhau = ?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, maNV);
            ps.setString(2, matKhau);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Login thành công - Tạo object NhanVien
                nv = new NhanVien(
                        rs.getString("MaNV"),
                        rs.getString("TenNV"),
                        rs.getString("MatKhau"),
                        rs.getString("ChucVu"),
                        rs.getString("SoDienThoai"),
                        rs.getString("Email"),
                        rs.getDate("NgayVaoLam"));

                System.out.println("✅ Login success: " + nv.getTenNV());
            } else {
                System.out.println("❌ Login failed: Sai mã NV hoặc mật khẩu");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Lỗi database khi login!");
        }

        return nv;
    }

    /**
     * Lấy thông tin nhân viên theo mã
     */
    public NhanVien getByMaNV(String maNV) {
        NhanVien nv = null;

        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM NhanVien WHERE MaNV = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maNV);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                nv = new NhanVien(
                        rs.getString("MaNV"),
                        rs.getString("TenNV"),
                        rs.getString("MatKhau"),
                        rs.getString("ChucVu"),
                        rs.getString("SoDienThoai"),
                        rs.getString("Email"),
                        rs.getDate("NgayVaoLam"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return nv;
    }

    /**
     * Đổi mật khẩu
     */
    public boolean changePassword(String maNV, String oldPass, String newPass) {
        try {
            Connection con = ConnectDB.getConnection();

            // Verify old password first
            String checkSql = "SELECT COUNT(*) FROM NhanVien WHERE MaNV = ? AND MatKhau = ?";
            PreparedStatement checkPs = con.prepareStatement(checkSql);
            checkPs.setString(1, maNV);
            checkPs.setString(2, oldPass);

            ResultSet rs = checkPs.executeQuery();
            rs.next();

            if (rs.getInt(1) == 0) {
                System.out.println("❌ Mật khẩu cũ không đúng!");
                return false;
            }

            // Update new password
            String updateSql = "UPDATE NhanVien SET MatKhau = ? WHERE MaNV = ?";
            PreparedStatement updatePs = con.prepareStatement(updateSql);
            updatePs.setString(1, newPass);
            updatePs.setString(2, maNV);

            return updatePs.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Lấy danh sách tất cả nhân viên
     */
    public java.util.ArrayList<NhanVien> getAll() {
        java.util.ArrayList<NhanVien> list = new java.util.ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM NhanVien";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                NhanVien nv = new NhanVien(
                        rs.getString("MaNV"),
                        rs.getString("TenNV"),
                        rs.getString("MatKhau"),
                        rs.getString("ChucVu"),
                        rs.getString("SoDienThoai"),
                        rs.getString("Email"),
                        rs.getDate("NgayVaoLam"));
                list.add(nv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thêm nhân viên mới
     */
    public boolean insert(NhanVien nv) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "INSERT INTO NhanVien (MaNV, TenNV, MatKhau, ChucVu, SoDienThoai, Email, NgayVaoLam) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, nv.getMaNV());
            ps.setString(2, nv.getTenNV());
            ps.setString(3, nv.getMatKhau());
            ps.setString(4, nv.getChucVu());
            ps.setString(5, nv.getSoDienThoai());
            ps.setString(6, nv.getEmail());
            ps.setDate(7, new java.sql.Date(nv.getNgayVaoLam().getTime()));

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật thông tin nhân viên
     */
    public boolean update(NhanVien nv) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE NhanVien SET TenNV = ?, MatKhau = ?, ChucVu = ?, SoDienThoai = ?, Email = ?, NgayVaoLam = ? WHERE MaNV = ?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, nv.getTenNV());
            ps.setString(2, nv.getMatKhau());
            ps.setString(3, nv.getChucVu());
            ps.setString(4, nv.getSoDienThoai());
            ps.setString(5, nv.getEmail());
            ps.setDate(6, new java.sql.Date(nv.getNgayVaoLam().getTime()));
            ps.setString(7, nv.getMaNV());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xóa nhân viên
     */
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

    /**
     * Tìm kiếm nhân viên gần đúng theo Tên hoặc SĐT hoặc Mã
     */
    public java.util.ArrayList<NhanVien> timKiem(String keyword) {
        java.util.ArrayList<NhanVien> list = new java.util.ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM NhanVien WHERE TenNV LIKE ? OR SoDienThoai LIKE ? OR MaNV LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            String query = "%" + keyword + "%";
            ps.setString(1, query);
            ps.setString(2, query);
            ps.setString(3, query);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                NhanVien nv = new NhanVien(
                        rs.getString("MaNV"),
                        rs.getString("TenNV"),
                        rs.getString("MatKhau"),
                        rs.getString("ChucVu"),
                        rs.getString("SoDienThoai"),
                        rs.getString("Email"),
                        rs.getDate("NgayVaoLam"));
                list.add(nv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
