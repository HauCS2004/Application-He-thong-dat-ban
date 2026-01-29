package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import connectDB.ConnectDB;
import Entity.KhachHang;

public class KhachHangDAO {

    // 1. Lấy tất cả
    public ArrayList<KhachHang> getAll() {
        ArrayList<KhachHang> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM KhachHang";
            ResultSet rs = con.createStatement().executeQuery(sql);
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

    // 3. Sửa khách
    public boolean update(KhachHang kh) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE KhachHang SET TenKhach=?, DiemTichLuy=? WHERE SoDienThoai=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, kh.getTenKhach());
            ps.setInt(2, kh.getDiemTichLuy());
            ps.setString(3, kh.getSoDienThoai());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. Xóa khách
    public boolean delete(String sdt) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "DELETE FROM KhachHang WHERE SoDienThoai=?";
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
        ArrayList<KhachHang> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM KhachHang WHERE TenKhach LIKE ? OR SoDienThoai LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new KhachHang(rs.getString(1), rs.getString(2), rs.getInt(3)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 6. Thêm khách mới (shorthand)
    public boolean themKhachMoi(String sdt, String ten) {
        KhachHang kh = new KhachHang(sdt, ten, 0);
        return insert(kh);
    }

    // 7. Check khách hàng đã tồn tại chưa
    public boolean checkTonTai(String sdt) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT COUNT(*) FROM KhachHang WHERE SoDienThoai = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sdt);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 8. Tích điểm (1 điểm = 10,000đ)
    public void tichDiem(String sdt, double tongTien) {
        try {
            int diemThem = (int) (tongTien / 10000);
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE KhachHang SET DiemTichLuy = DiemTichLuy + ? WHERE SoDienThoai = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, diemThem);
            ps.setString(2, sdt);
            ps.executeUpdate();
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
}
