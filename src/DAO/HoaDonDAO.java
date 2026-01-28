package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import connectDB.ConnectDB;
import Entity.HoaDon;

public class HoaDonDAO {

    // 1. Lấy mã hóa đơn chưa thanh toán của bàn (trả về -1 nếu không có)
    public int getMaHDByBan(String maBan) {
        int maHD = -1;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT MaHD FROM HoaDon WHERE MaBan = ? AND TrangThai = 0";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maBan);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                maHD = rs.getInt("MaHD");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maHD;
    }

    // 2. Lấy thông tin chi tiết hóa đơn
    public HoaDon getThongTinHoaDon(int maHD) {
        HoaDon hd = null;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM HoaDon WHERE MaHD = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                hd = new HoaDon();
                hd.setMaHD(rs.getInt("MaHD"));
                hd.setMaBan(rs.getString("MaBan"));
                hd.setNgayTao(rs.getTimestamp("NgayTao"));
                hd.setTongTien(rs.getDouble("TongTien"));
                hd.setTrangThai(rs.getInt("TrangThai"));
                hd.setSdtKhach(rs.getString("SDT_Khach"));
                hd.setGhiChu(rs.getString("GhiChu")); // Tạm dùng ghi chu lưu tên khách nếu cần
                hd.setMaNV(rs.getString("MaNV"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hd;
    }

    // 3. Cập nhật SĐT khách vào hóa đơn
    public boolean updateSdtKhach(int maHD, String sdt) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE HoaDon SET SDT_Khach = ? WHERE MaHD = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, sdt);
            ps.setInt(2, maHD);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. Tính tổng tiền tạm tính (từ chi tiết hóa đơn)
    public double getTongTienTamTinh(int maHD) {
        double tongTien = 0;
        try {
            Connection con = ConnectDB.getConnection();
            // Tính tổng tiền từ bảng ChiTietHoaDon
            String sql = "SELECT SUM(SoLuong * DonGia) FROM ChiTietHoaDon WHERE MaHD = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tongTien = rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tongTien;
    }

    // 5. Thêm hóa đơn mới
    public int insertHoaDon(HoaDon hd) {
        int maHD = -1;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "INSERT INTO HoaDon(MaBan, MaNV, NgayTao, TrangThai, SoLuongKhach) VALUES(?, ?, GETDATE(), 0, ?)";
            PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, hd.getMaBan());
            ps.setString(2, hd.getMaNV());
            ps.setInt(3, hd.getSoLuongKhach()); // Nâng cấp: thêm số khách

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                maHD = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maHD;
    }

    // 6. Thanh toán hóa đơn
    public boolean thanhToan(int maHD, double tongTien) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE HoaDon SET TrangThai = 1, TongTien = ? WHERE MaHD = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setDouble(1, tongTien);
            ps.setInt(2, maHD);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 7. Lấy số lượng khách của hóa đơn
    public int getSoLuongKhach(int maHD) {
        int soKhach = 1;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT SoLuongKhach FROM HoaDon WHERE MaHD = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maHD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                soKhach = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return soKhach;
    }

    // Overload: Lấy số lượng khách hiện tại của bàn (dựa vào hóa đơn chưa thanh
    // toán)
    public int getSoLuongKhach(String maBan) {
        int soKhach = 0;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT SoLuongKhach FROM HoaDon WHERE MaBan = ? AND TrangThai = 0";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maBan);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                soKhach = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return soKhach;
    }

    // 8. Insert hóa đơn (Overload cho QuanLyBan) -> Trả về Mã HD
    public int insert(HoaDon hd) {
        return insertHoaDon(hd);
    }

    // 9. Lấy chi tiết hóa đơn (Trả về String[] để hiển thị Table)
    public ArrayList<String[]> getChiTietHoaDon(int maHD) {
        ArrayList<String[]> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT m.TenMon, c.SoLuong, c.DonGia, (c.SoLuong * c.DonGia) as ThanhTien, c.GhiChu " +
                    "FROM ChiTietHoaDon c JOIN MonAn m ON c.MaMon = m.MaMon WHERE MaHD = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, maHD);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[] {
                        rs.getString("TenMon"),
                        String.valueOf(rs.getInt("SoLuong")),
                        String.valueOf((int) rs.getDouble("DonGia")),
                        String.valueOf((int) rs.getDouble("ThanhTien")),
                        rs.getString("GhiChu")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
