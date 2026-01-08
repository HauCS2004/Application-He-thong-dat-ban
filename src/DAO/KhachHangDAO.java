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
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM KhachHang";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                list.add(new KhachHang(
                    rs.getString("SoDienThoai"),
                    rs.getString("TenKhach"),
                    rs.getInt("DiemTichLuy")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 2. Thêm khách
    public boolean insert(KhachHang kh) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "INSERT INTO KhachHang VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, kh.getSoDienThoai());
            ps.setString(2, kh.getTenKhach());
            ps.setInt(3, kh.getDiemTichLuy()); // Mặc định thường là 0
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 3. Sửa khách
    public boolean update(KhachHang kh) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "UPDATE KhachHang SET TenKhach=?, DiemTichLuy=? WHERE SoDienThoai=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, kh.getTenKhach());
            ps.setInt(2, kh.getDiemTichLuy());
            ps.setString(3, kh.getSoDienThoai());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 4. Xóa khách
    public boolean delete(String sdt) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "DELETE FROM KhachHang WHERE SoDienThoai=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sdt);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
    
    // 5. Tìm kiếm (Theo Tên hoặc SĐT)
    public ArrayList<KhachHang> timKiem(String keyword) {
        ArrayList<KhachHang> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM KhachHang WHERE TenKhach LIKE ? OR SoDienThoai LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new KhachHang(rs.getString(1), rs.getString(2), rs.getInt(3)));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
 // Thêm vào KhachHangDAO.java
    public void tichDiem(String sdt, String tenKhach, double tongTienDaTra) {
        if(sdt == null || sdt.trim().isEmpty()) return;
        
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            // Quy đổi: 10.000 VNĐ = 1 điểm
            int diemMoi = (int) (tongTienDaTra / 10000);

            // Kiểm tra tồn tại
            String checkSQL = "SELECT DiemTichLuy FROM KhachHang WHERE SoDienThoai = ?";
            PreparedStatement psCheck = con.prepareStatement(checkSQL);
            psCheck.setString(1, sdt);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                // UPDATE
                int diemCu = rs.getInt(1);
                String sqlUp = "UPDATE KhachHang SET DiemTichLuy = ?, TenKhach = ? WHERE SoDienThoai = ?";
                PreparedStatement psUp = con.prepareStatement(sqlUp);
                psUp.setInt(1, diemCu + diemMoi);
                psUp.setString(2, tenKhach); // Cập nhật tên mới nhất (lỡ khách đổi tên)
                psUp.setString(3, sdt);
                psUp.executeUpdate();
            } else {
                // INSERT
                String sqlIn = "INSERT INTO KhachHang (SoDienThoai, TenKhach, DiemTichLuy) VALUES (?, ?, ?)";
                PreparedStatement psIn = con.prepareStatement(sqlIn);
                psIn.setString(1, sdt);
                psIn.setString(2, tenKhach);
                psIn.setInt(3, diemMoi);
                psIn.executeUpdate();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    // Hàm lấy % giảm giá dựa trên SĐT (Dùng để hiển thị lúc thanh toán)
    public int getPhanTramGiam(String sdt) {
        try {
            String sql = "SELECT DiemTichLuy FROM KhachHang WHERE SoDienThoai = ?";
            PreparedStatement ps = ConnectDB.getInstance().getConnection().prepareStatement(sql);
            ps.setString(1, sdt);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int diem = rs.getInt(1);
                // Quy định hạng:
                if (diem >= 1000) return 15; // VIP Kim Cương
                if (diem >= 500) return 10;  // VIP Vàng
                if (diem >= 200) return 5;   // VIP Bạc
            }
        } catch (Exception e) {}
        return 0; // Khách thường hoặc không tìm thấy
    }
    public String getTenKhach(String sdt) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT TenKhach FROM KhachHang WHERE SoDienThoai = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sdt);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getString(1);
        } catch(Exception e) {}
        return "Khách vãng lai";
    }
}