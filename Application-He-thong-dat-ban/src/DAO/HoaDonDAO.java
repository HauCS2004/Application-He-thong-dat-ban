package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import connectDB.ConnectDB;
import Entity.HoaDon;

public class HoaDonDAO {
    
    // 1. Tạo hóa đơn mới (Quan trọng: Trả về Mã HĐ vừa tạo)
	// Trong HoaDonDAO.java

	public int insert(HoaDon hd) {
	    int maHD = -1;
	    try {
	        Connection con = ConnectDB.getInstance().getConnection();
	        // Cập nhật câu SQL thêm cột SDT_Khach
	        String sql = "INSERT INTO HoaDon (MaBan, SoLuongKhach, SDT_Khach, GhiChu, TrangThai, TongTien) " +
	                     "VALUES (?, ?, ?, ?, 0, 0)";
	        
	        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	        ps.setString(1, hd.getMaBan());
	        ps.setInt(2, hd.getSoLuongKhach());
	        
	        // [QUAN TRỌNG] Lưu SĐT vào đây
	        ps.setString(3, hd.getSdtKhach()); 
	        
	        ps.setString(4, hd.getGhiChu());
	        
	        if (ps.executeUpdate() > 0) {
	            ResultSet rs = ps.getGeneratedKeys();
	            if (rs.next()) maHD = rs.getInt(1);
	        }
	    } catch (Exception e) { e.printStackTrace(); }
	    return maHD;
	}

    // 2. Tìm Mã HĐ đang hoạt động của bàn (TrangThai = 0)
    // Hàm này giúp biết bàn đó đang ăn dở hóa đơn nào để thêm món vào
    public int getMaHDByBan(String maBan) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT MaHD FROM HoaDon WHERE MaBan = ? AND TrangThai = 0";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maBan);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("MaHD");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1; // Bàn trống, không có hóa đơn
    }
    
    // 3. Thanh toán (Kết thúc hóa đơn)
    public void thanhToan(int maHD, double tongTienCuoi) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "UPDATE HoaDon SET TrangThai = 1, TongTien = ? WHERE MaHD = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDouble(1, tongTienCuoi);
            ps.setInt(2, maHD);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
 // Trong class HoaDonDAO
    public int getSoLuongKhach(String maBan) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            // Lấy số khách của hóa đơn đang mở (TrangThai = 0) tại bàn đó
            String sql = "SELECT SoLuongKhach FROM HoaDon WHERE MaBan = ? AND TrangThai = 0";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maBan);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("SoLuongKhach");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0; // Nếu bàn trống hoặc lỗi thì trả về 0
    }
 // ... (Giữ nguyên code cũ của bạn) ...

    // 4. [MỚI] Tính tổng tiền tạm (Tổng món ăn chưa trừ giảm giá)
    public double getTongTienTamTinh(int maHD) {
        double tong = 0;
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT SUM(SoLuong * DonGia) FROM ChiTietHoaDon WHERE MaHD = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) tong = rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return tong;
    }
    

    // 5. [MỚI] Lấy SĐT và Tên khách đã lưu trong Hóa Đơn (lúc mở bàn)
    public HoaDon getThongTinHoaDon(int maHD) {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM HoaDon WHERE MaHD = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maHD);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                HoaDon hd = new HoaDon();
                hd.setMaHD(maHD);
                hd.setSdtKhach(rs.getString("SDT_Khach"));
                hd.setGhiChu(rs.getString("GhiChu")); // Giả sử tên khách lưu trong GhiChu hoặc cột riêng
                return hd;
            }
        } catch(Exception e) {}
        return null;
    }
 // Thêm vào HoaDonDAO.java

    public ArrayList<HoaDon> timKiemHoaDon(int ngay, int thang, int nam) {
        ArrayList<HoaDon> list = new ArrayList<>();
        try {
            java.sql.Connection con = ConnectDB.getInstance().getConnection();
            
            // Tạo câu SQL động
            String sql = "SELECT * FROM HoaDon WHERE 1=1";
            
            // Nếu ngày > 0 thì thêm điều kiện ngày, ngược lại là tìm hết các ngày
            if (ngay > 0) sql += " AND DAY(NgayTao) = " + ngay;
            
            // Nếu tháng > 0 thì thêm điều kiện tháng
            if (thang > 0) sql += " AND MONTH(NgayTao) = " + thang;
            
            // Nếu năm > 0 thì thêm điều kiện năm
            if (nam > 0) sql += " AND YEAR(NgayTao) = " + nam;
            
            // Sắp xếp mới nhất lên đầu
            sql += " ORDER BY NgayTao DESC";

            java.sql.Statement st = con.createStatement();
            java.sql.ResultSet rs = st.executeQuery(sql);
            
            while(rs.next()) {
                HoaDon hd = new HoaDon();
                hd.setMaHD(rs.getInt("MaHD"));
                hd.setMaBan(rs.getString("MaBan"));
                hd.setNgayTao(rs.getTimestamp("NgayTao"));
                hd.setTongTien(rs.getDouble("TongTien"));
                hd.setTrangThai(rs.getInt("TrangThai"));
                hd.setSdtKhach(rs.getString("SDT_Khach"));
                hd.setGhiChu(rs.getString("GhiChu")); // Lấy tên khách để hiện cho rõ
                list.add(hd);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

 // Hàm lấy danh sách chi tiết món ăn để đổ vào bảng bên phải
	 public ArrayList<String[]> getChiTietHoaDon(int maHD) {
	     ArrayList<String[]> list = new ArrayList<>();
	     try {
	         java.sql.Connection con = ConnectDB.getInstance().getConnection();
	         // Join bảng ChiTiet với bảng MonAn để lấy tên và giá
	         String sql = "SELECT m.TenMon, c.SoLuong, m.DonGia, (c.SoLuong * m.DonGia) as ThanhTien " +
	                      "FROM ChiTietHoaDon c " +
	                      "JOIN MonAn m ON c.MaMon = m.MaMon " +
	                      "WHERE c.MaHD = ?";
	         
	         java.sql.PreparedStatement ps = con.prepareStatement(sql);
	         ps.setInt(1, maHD);
	         java.sql.ResultSet rs = ps.executeQuery();
	         
	         while(rs.next()) {
	             list.add(new String[]{
	                 rs.getString("TenMon"),
	                 String.valueOf(rs.getInt("SoLuong")),
	                 String.format("%,.0f", rs.getDouble("DonGia")),   // Format số tiền 100,000
	                 String.format("%,.0f", rs.getDouble("ThanhTien"))
	             });
	         }
	     } catch (Exception e) { e.printStackTrace(); }
	     return list;
	 }
	 public boolean updateSdtKhach(int maHD, String sdt) {
	        try {
	            java.sql.Connection con = ConnectDB.getInstance().getConnection();
	            String sql = "UPDATE HoaDon SET SDT_Khach = ? WHERE MaHD = ?";
	            java.sql.PreparedStatement ps = con.prepareStatement(sql);
	            ps.setString(1, sdt);
	            ps.setInt(2, maHD);
	            return ps.executeUpdate() > 0;
	        } catch (Exception e) {
	            e.printStackTrace();
	            return false;
	        }
	    }
}