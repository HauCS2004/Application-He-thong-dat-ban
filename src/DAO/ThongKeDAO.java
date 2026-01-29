package DAO;

import java.sql.*;
import java.util.*;
import connectDB.ConnectDB;

public class ThongKeDAO {

    // 1. Lấy thống kê doanh thu theo từng ngày
    public ArrayList<Object[]> getDoanhThuTheoNgay(java.util.Date tuNgay, java.util.Date denNgay) {
        ArrayList<Object[]> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            
            // [SỬA LỖI Ở ĐÂY]: Thay 'NgayLap' thành tên cột đúng (ví dụ: NgayTao)
            String sql = "SELECT CAST(NgayTao AS DATE) as Ngay, COUNT(*) as SoDon, SUM(TongTien) as DoanhThu " +
                         "FROM HoaDon " +
                         "WHERE CAST(NgayTao AS DATE) BETWEEN ? AND ? " +
                         "GROUP BY CAST(NgayTao AS DATE)";
            
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDate(1, new java.sql.Date(tuNgay.getTime()));
            ps.setDate(2, new java.sql.Date(denNgay.getTime()));
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()) {
                list.add(new Object[]{ rs.getDate("Ngay"), rs.getInt("SoDon"), rs.getDouble("DoanhThu") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

 // 2. Lấy Top 5 món ăn bán chạy nhất
    public ArrayList<Object[]> getTopMonAn(java.util.Date tuNgay, java.util.Date denNgay) {
        ArrayList<Object[]> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            
            // [SỬA LỖI]: 
            // 1. Thay 'ct.ThanhTien' thành 'ct.SoLuong * ct.DonGia'
            // 2. Nhớ dùng đúng tên cột ngày (NgayTao hay NgayLap) mà bạn đã sửa ở bước trước
            
            String sql = "SELECT TOP 5 m.TenMon, " + 
                         "SUM(ct.SoLuong) as SoLuongBan, " + 
                         "SUM(ct.SoLuong * ct.DonGia) as Tien " + // <--- SỬA DÒNG NÀY (Nhân số lượng với đơn giá)
                         "FROM ChiTietHoaDon ct " +
                         "JOIN HoaDon hd ON ct.MaHD = hd.MaHD " +
                         "JOIN MonAn m ON ct.MaMon = m.MaMon " +
                         "WHERE CAST(hd.NgayTao AS DATE) BETWEEN ? AND ? " + // <--- Nhớ dùng NgayTao nếu DB của bạn là NgayTao
                         "GROUP BY m.TenMon " +
                         "ORDER BY SoLuongBan DESC";
            
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDate(1, new java.sql.Date(tuNgay.getTime()));
            ps.setDate(2, new java.sql.Date(denNgay.getTime()));
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()) {
                list.add(new Object[]{ rs.getString("TenMon"), rs.getInt("SoLuongBan"), rs.getDouble("Tien") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}