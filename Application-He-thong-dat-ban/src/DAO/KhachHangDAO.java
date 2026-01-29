package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import connectDB.ConnectDB;
import Entity.KhachHang; // Chú ý: Package ENTITY hay Entity thì sửa lại cho đúng project của bạn

public class KhachHangDAO {

    // 1. Lấy tất cả khách hàng
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

    // 2. Thêm khách hàng mới (Full thông tin)
    public boolean insert(KhachHang kh) {
        try {
            // Kiểm tra trùng trước khi thêm
            if(checkTonTai(kh.getSoDienThoai())) return false;

            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "INSERT INTO KhachHang (SoDienThoai, TenKhach, DiemTichLuy) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, kh.getSoDienThoai());
            ps.setString(2, kh.getTenKhach());
            ps.setInt(3, kh.getDiemTichLuy());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
    
    // 2.1. Thêm khách nhanh (Chỉ cần SĐT và Tên, mặc định 0 điểm)
    // Hàm này dùng cho nút Thanh Toán khi gặp khách mới
    public void themKhachMoi(String sdt, String tenKhach) {
        try {
            if(checkTonTai(sdt)) return; // Có rồi thì thôi
            
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "INSERT INTO KhachHang (SoDienThoai, TenKhach, DiemTichLuy) VALUES (?, ?, 0)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sdt);
            ps.setString(2, tenKhach);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 3. Cập nhật thông tin khách
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

    // 4. Xóa khách hàng
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

    // 6. Kiểm tra SĐT đã tồn tại chưa? (Hỗ trợ validate)
    public boolean checkTonTai(String sdt) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT SoDienThoai FROM KhachHang WHERE SoDienThoai = ?");
            ps.setString(1, sdt);
            return ps.executeQuery().next();
        } catch (Exception e) { return false; }
    }

    // 7. Lấy tên khách hàng theo SĐT (Dùng để hiện tên khi nhập SĐT)
    public String getTenKhach(String sdt) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT TenKhach FROM KhachHang WHERE SoDienThoai = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sdt);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getString(1);
        } catch(Exception e) {}
        return "Khách vãng lai"; // Hoặc trả về null tùy logic
    }

    // 8. TÍCH ĐIỂM (Logic chuẩn: 100.000 VNĐ = 1 điểm)
    public void tichDiem(String sdt, double tongTien) {
        try {
            // Quy đổi điểm (Lấy phần nguyên)
            int diemMoi = (int) (tongTien / 100000); 
            if (diemMoi <= 0) return; // Không đủ điểm thì thôi

            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "UPDATE KhachHang SET DiemTichLuy = DiemTichLuy + ? WHERE SoDienThoai = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, diemMoi);
            ps.setString(2, sdt);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 9. Lấy % giảm giá (Dựa theo hạng thành viên)
    public int getPhanTramGiam(String sdt) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT DiemTichLuy FROM KhachHang WHERE SoDienThoai = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sdt);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int diem = rs.getInt(1);
                // Quy định hạng:
                if (diem >= 1000) return 15; // Kim Cương: Giảm 15%
                if (diem >= 500) return 10;  // Vàng: Giảm 10%
                if (diem >= 200) return 5;   // Bạc: Giảm 5%
            }
        } catch (Exception e) {}
        return 0; // Không giảm
    }
}