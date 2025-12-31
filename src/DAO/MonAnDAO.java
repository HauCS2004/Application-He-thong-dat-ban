package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import connectDB.ConnectDB;
import Entity.MonAn;

public class MonAnDAO {

    // 1. Lấy tất cả món ăn
    public ArrayList<MonAn> getAll() {
        ArrayList<MonAn> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM MonAn";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new MonAn(
                    rs.getString("MaMon"),
                    rs.getString("TenMon"),
                    rs.getString("DonViTinh"),
                    rs.getDouble("DonGia"),
                    rs.getString("HinhAnh"),
                    rs.getString("MaLoai")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 2. Thêm món
    public boolean insert(MonAn m) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "INSERT INTO MonAn (MaMon, TenMon, DonViTinh, DonGia, HinhAnh, MaLoai) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, m.getMaMon());
            ps.setString(2, m.getTenMon());
            ps.setString(3, m.getDonViTinh());
            ps.setDouble(4, m.getDonGia());
            ps.setString(5, m.getHinhAnh());
            ps.setString(6, m.getMaLoai());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 3. Xóa món
    public boolean delete(String maMon) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "DELETE FROM MonAn WHERE MaMon = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maMon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 4. Sửa món
    public boolean update(MonAn m) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "UPDATE MonAn SET TenMon=?, DonViTinh=?, DonGia=?, HinhAnh=?, MaLoai=? WHERE MaMon=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, m.getTenMon());
            ps.setString(2, m.getDonViTinh());
            ps.setDouble(3, m.getDonGia());
            ps.setString(4, m.getHinhAnh());
            ps.setString(5, m.getMaLoai());
            ps.setString(6, m.getMaMon());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
 // Hàm tìm kiếm đa năng: Tìm theo tên GẦN ĐÚNG và theo Mã Loại
    public ArrayList<MonAn> timKiem(String keyword, String maLoai) {
        ArrayList<MonAn> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        
        // Tạo câu SQL động
        String sql = "SELECT * FROM MonAn WHERE TenMon LIKE ? ";
        
        // Nếu maLoai khác rỗng (tức là không phải chọn "Tất cả") thì thêm điều kiện
        if (maLoai != null && !maLoai.isEmpty()) {
            sql += " AND MaLoai = ?";
        }
        
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            // Tham số 1: Tên (thêm % để tìm gần đúng)
            ps.setString(1, "%" + keyword + "%");
            
            // Tham số 2: Mã loại (nếu có)
            if (maLoai != null && !maLoai.isEmpty()) {
                ps.setString(2, maLoai);
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new MonAn(
                    rs.getString("MaMon"),
                    rs.getString("TenMon"),
                    rs.getString("DonViTinh"),
                    rs.getDouble("DonGia"),
                    rs.getString("HinhAnh"),
                    rs.getString("MaLoai")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}