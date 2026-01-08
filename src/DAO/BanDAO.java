package DAO;

import java.sql.*;
import java.util.ArrayList;
import connectDB.ConnectDB; // Kiểm tra lại package connectDB hay CONNECTDB của bạn
import Entity.Ban;          // Kiểm tra lại package Entity hay ENTITY của bạn

public class BanDAO {

    // 1. Lấy danh sách bàn theo khu vực (Cập nhật lấy thêm MaBanGop)
    public ArrayList<Ban> getBanTheoKhuVuc(String maKV) {
        ArrayList<Ban> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        String sql = "SELECT * FROM Ban WHERE MaKV = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maKV);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                // Xử lý lấy MaBanGop an toàn (tránh null pointer khi new)
                String maBanGop = rs.getString("MaBanGop");
                
                list.add(new Ban(
                    rs.getString("MaBan"), 
                    rs.getString("TenBan"), 
                    rs.getString("TrangThai"), 
                    rs.getString("MaKV"),
                    rs.getInt("SoGhe"),
                    maBanGop // Tham số thứ 6
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 2. Lấy danh sách bàn theo Trạng Thái (Fix lỗi thiếu cột)
    public ArrayList<Ban> getBanTheoTrangThai(String trangThaiCanTim) {
        ArrayList<Ban> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        String sql = "SELECT * FROM Ban WHERE TrangThai = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, trangThaiCanTim);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                list.add(new Ban(
                    rs.getString("MaBan"), 
                    rs.getString("TenBan"), 
                    rs.getString("TrangThai"), 
                    rs.getString("MaKV"),
                    rs.getInt("SoGhe"),
                    rs.getString("MaBanGop") // Lấy cột MaBanGop
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 3. Lấy bàn có khách (Hỗ trợ ghép bàn)
    public ArrayList<Ban> getBanCoKhach() {
        return getBanTheoTrangThai("Có Khách");
    }

    // 4. Cập nhật trạng thái đơn giản
    public void updateTrangThai(String maBan, String tt) {
        try {
            String sql = "UPDATE Ban SET TrangThai = ? WHERE MaBan = ?";
            PreparedStatement ps = ConnectDB.getInstance().getConnection().prepareStatement(sql);
            ps.setString(1, tt);
            ps.setString(2, maBan);
            ps.executeUpdate();
        } catch(Exception e) { e.printStackTrace(); }
    }

    // 5. Tự động cập nhật trạng thái Đặt Bàn (Dùng cho Timer)
    public void capNhatTrangThaiDatBan() {
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "UPDATE Ban SET TrangThai = N'Đã Đặt' " +
                         "WHERE MaBan IN (SELECT MaBan FROM DatBan " +
                         "WHERE ABS(DATEDIFF(MINUTE, ThoiGianDat, GETDATE())) <= 30) " +
                         "AND TrangThai = N'Trống'";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // 6. Chuyển Bàn
    public boolean chuyenBan(String maBanCu, String maBanMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            con.setAutoCommit(false);
            // Chuyển hóa đơn
            String sqlHD = "UPDATE HoaDon SET MaBan = ? WHERE MaBan = ? AND TrangThai = 0";
            PreparedStatement psHD = con.prepareStatement(sqlHD);
            psHD.setString(1, maBanMoi);
            psHD.setString(2, maBanCu);
            psHD.executeUpdate();

            // Cập nhật trạng thái
            updateTrangThai(maBanCu, "Trống");
            updateTrangThai(maBanMoi, "Có Khách");

            con.commit();
            return true;
        } catch (Exception e) {
            try { con.rollback(); } catch (Exception ex) {}
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (Exception ex) {}
        }
    }

    // 7. Ghép Nhiều Bàn (Logic gộp và set trạng thái 'Đang Gộp')
    public boolean ghepNhieuBan(String maBanDich, ArrayList<String> listMaBanNguon) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            con.setAutoCommit(false);
            
            HoaDonDAO hdDAO = new HoaDonDAO();
            int maHDDich = hdDAO.getMaHDByBan(maBanDich);
            if(maHDDich == -1) return false;

            for(String maBanNguon : listMaBanNguon) {
                int maHDNguon = hdDAO.getMaHDByBan(maBanNguon);
                
                // Chuyển món
                if(maHDNguon != -1) {
                    String sqlMove = "UPDATE ChiTietHoaDon SET MaHD = ? WHERE MaHD = ?";
                    PreparedStatement ps = con.prepareStatement(sqlMove);
                    ps.setInt(1, maHDDich); ps.setInt(2, maHDNguon);
                    ps.executeUpdate();
                    
                    // Xóa hóa đơn rỗng
                    String sqlDel = "DELETE FROM HoaDon WHERE MaHD = ?";
                    PreparedStatement psDel = con.prepareStatement(sqlDel);
                    psDel.setInt(1, maHDNguon); psDel.executeUpdate();
                }

                // Update Bàn Nguồn thành 'Đang Gộp' và trỏ về Bàn Đích
                String sqlUpBan = "UPDATE Ban SET TrangThai = N'Đang Gộp', MaBanGop = ? WHERE MaBan = ?";
                PreparedStatement psUp = con.prepareStatement(sqlUpBan);
                psUp.setString(1, maBanDich);
                psUp.setString(2, maBanNguon);
                psUp.executeUpdate();
            }

            con.commit();
            return true;
        } catch (Exception e) {
            try { con.rollback(); } catch (Exception ex) {}
            e.printStackTrace();
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (Exception ex) {}
        }
    }
    
    // 8. Hủy Gộp Bàn (Dùng khi thanh toán bàn chính)
    public void huyGopBan(String maBanChinh) {
        try {
            // Trả tất cả các bàn đang gộp vào bàn chính này về trạng thái Trống
            String sql = "UPDATE Ban SET TrangThai = N'Trống', MaBanGop = NULL WHERE MaBanGop = ?";
            PreparedStatement ps = ConnectDB.getInstance().getConnection().prepareStatement(sql);
            ps.setString(1, maBanChinh);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
// // Hàm mới: Lấy thông tin đơn đặt bàn gần nhất (trong khoảng 1 tiếng đổ lại) của bàn này
//    public void  getDatBanGannhat(String maBan) {
//        Entity.DatBan db = null;
//        try {
//            Connection con = connectDB.ConnectDB.getInstance().getConnection();
//            
//            // Lấy đơn đặt của bàn này, mà thời gian đặt nằm trong khoảng (Hiện tại +/- 60 phút)
//            // Sắp xếp lấy cái gần nhất
//            String sql = "SELECT TOP 1 * FROM DatBan " +
//                         "WHERE MaBan = ? " +
//                         "AND ABS(DATEDIFF(MINUTE, ThoiGianDat, GETDATE())) <= 60 " +
//                         "ORDER BY ThoiGianDat DESC";
//            
//            PreparedStatement ps = con.prepareStatement(sql);
//            ps.setString(1, maBan);
//            ResultSet rs = ps.executeQuery();
//            
//            if (rs.next()) {
//                db = new ENTITY.DatBan();
//                db.setMaDat(rs.getInt("MaDat"));
//                db.setMaBan(rs.getString("MaBan"));
//                db.setTenKhach(rs.getString("TenKhachDat"));
//                db.setSdt(rs.getString("SDT"));
//                db.setThoiGianDat(rs.getTimestamp("ThoiGianDat"));
//                db.setSoLuongKhach(rs.getInt("SoLuongKhach"));
//                db.setGhiChu(rs.getString("GhiChu"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return db;
//    }
}