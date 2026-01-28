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
            Connection con = ConnectDB.getInstance().getConnection();
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
            Connection con = ConnectDB.getInstance().getConnection();
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
            Connection con = ConnectDB.getInstance().getConnection();

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
}
