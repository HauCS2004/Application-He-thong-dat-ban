package DAO;

import java.sql.*;
import java.util.ArrayList;
import connectDB.ConnectDB; // Kiểm tra lại package connectDB hay CONNECTDB của bạn
import Entity.Ban; // Kiểm tra lại package Entity hay ENTITY của bạn

public class BanDAO {

    // 1. Lấy danh sách bàn theo khu vực (Cập nhật lấy thêm MaBanGop)
    public ArrayList<Ban> getBanTheoKhuVuc(String maKV) {
        ArrayList<Ban> list = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT * FROM Ban WHERE MaKV = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maKV);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Lấy danh sách bàn theo Trạng Thái (Fix lỗi thiếu cột)
    public ArrayList<Ban> getBanTheoTrangThai(String trangThaiCanTim) {
        ArrayList<Ban> list = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT * FROM Ban WHERE TrangThai = ?";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, trangThaiCanTim);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Ban(
                        rs.getString("MaBan"),
                        rs.getString("TenBan"),
                        rs.getString("TrangThai"),
                        rs.getString("MaKV"),
                        rs.getInt("SoGhe"),
                        rs.getString("MaBanGop") // Lấy cột MaBanGop
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 3. Lấy bàn có khách (Hỗ trợ ghép bàn)
    public ArrayList<Ban> getBanCoKhach() {
        return getBanTheoTrangThai("Có Khách");
    }

    // 4. Cập nhật trạng thái đơn giản
    public void updateTrangThai(String maBan, String tt) {
        try {
            // Sử dụng tiền tố N cho Unicode để đảm bảo tính đúng đắn trong SQL Server
            String sql = "UPDATE Ban SET TrangThai = ? WHERE MaBan = ?";

            // Xử lý cứng các trạng thái phổ biến với N'...'
            // Dùng PreparedStatement setString thường đã tự xử lý,
            // nhưng để chắc chắn với một số driver cũ hoặc cấu hình DB đặc biệt:

            if (tt.equals("Trống")) {
                sql = "UPDATE Ban SET TrangThai = N'Trống' WHERE MaBan = ?";
                PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql);
                ps.setString(1, maBan);
                ps.executeUpdate();
            } else if (tt.equals("Có Khách")) {
                sql = "UPDATE Ban SET TrangThai = N'Có Khách' WHERE MaBan = ?";
                PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql);
                ps.setString(1, maBan);
                ps.executeUpdate();
            } else {
                // Trường hợp mặc định
                PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql);
                ps.setString(1, tt);
                ps.setString(2, maBan);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void capNhatTrangThaiDatBan() {
        try {
            Connection con = ConnectDB.getConnection();
            // FIX: Exclude completed/canceled bookings from triggering "Đã Đặt"
            String sql = "UPDATE Ban SET TrangThai = N'Đã Đặt' " +
                    "WHERE MaBan IN (SELECT MaBan FROM DatBan " +
                    "WHERE ABS(DATEDIFF(MINUTE, ThoiGianBatDau, GETDATE())) <= 30 " +
                    "AND TrangThai NOT LIKE N'%Đã hủy%' " +
                    "AND TrangThai NOT LIKE N'%hoàn%' " +
                    "AND TrangThai NOT LIKE N'%đã thanh toán%') " +
                    "AND TrangThai = N'Trống'";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 6. Chuyển Bàn
    public boolean chuyenBan(String maBanCu, String maBanMoi) {
        Connection con = ConnectDB.getConnection();
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
            try {
                con.rollback();
            } catch (Exception ex) {
            }
            return false;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception ex) {
            }
        }
    }

    // 7. Ghép Nhiều Bàn (Logic gộp và set trạng thái 'Đang Gộp')
    public boolean ghepNhieuBan(String maBanDich, ArrayList<String> listMaBanNguon) {
        Connection con = ConnectDB.getConnection();
        try {
            con.setAutoCommit(false);

            HoaDonDAO hdDAO = new HoaDonDAO();
            int maHDDich = hdDAO.getMaHDByBan(maBanDich);
            if (maHDDich == -1)
                return false;

            for (String maBanNguon : listMaBanNguon) {
                int maHDNguon = hdDAO.getMaHDByBan(maBanNguon);

                // Chuyển món
                if (maHDNguon != -1) {
                    String sqlMove = "UPDATE ChiTietHoaDon SET MaHD = ? WHERE MaHD = ?";
                    PreparedStatement ps = con.prepareStatement(sqlMove);
                    ps.setInt(1, maHDDich);
                    ps.setInt(2, maHDNguon);
                    ps.executeUpdate();

                    // Xóa hóa đơn rỗng
                    String sqlDel = "DELETE FROM HoaDon WHERE MaHD = ?";
                    PreparedStatement psDel = con.prepareStatement(sqlDel);
                    psDel.setInt(1, maHDNguon);
                    psDel.executeUpdate();
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
            try {
                con.rollback();
            } catch (Exception ex) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception ex) {
            }
        }
    }

    // 8. Hủy Gộp Bàn (Dùng khi thanh toán bàn chính)
    public void huyGopBan(String maBanChinh) {
        try {
            // Trả tất cả các bàn đang gộp vào bàn chính này về trạng thái Trống
            String sql = "UPDATE Ban SET TrangThai = N'Trống', MaBanGop = NULL WHERE MaBanGop = ?";
            PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql);
            ps.setString(1, maBanChinh);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // // Hàm mới: Lấy thông tin đơn đặt bàn gần nhất (trong khoảng 1 tiếng đổ lại)
    // của bàn này
    // public void getDatBanGannhat(String maBan) {
    // Entity.DatBan db = null;
    // try {
    // Connection con = connectDB.ConnectDB.getConnection();
    //
    // // Lấy đơn đặt của bàn này, mà thời gian đặt nằm trong khoảng (Hiện tại +/-
    // 60 phút)
    // // Sắp xếp lấy cái gần nhất
    // String sql = "SELECT TOP 1 * FROM DatBan " +
    // "WHERE MaBan = ? " +
    // "AND ABS(DATEDIFF(MINUTE, ThoiGianDat, GETDATE())) <= 60 " +
    // "ORDER BY ThoiGianDat DESC";
    //
    // PreparedStatement ps = con.prepareStatement(sql);
    // ps.setString(1, maBan);
    // ResultSet rs = ps.executeQuery();
    //
    // if (rs.next()) {
    // db = new ENTITY.DatBan();
    // db.setMaDat(rs.getInt("MaDat"));
    // db.setMaBan(rs.getString("MaBan"));
    // db.setTenKhach(rs.getString("TenKhachDat"));
    // db.setSdt(rs.getString("SDT"));
    // db.setThoiGianDat(rs.getTimestamp("ThoiGianDat"));
    // db.setSoLuongKhach(rs.getInt("SoLuongKhach"));
    // db.setGhiChu(rs.getString("GhiChu"));
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // return db;
    // }
    // 9. Lấy tất cả bàn
    public ArrayList<Ban> getAllBan() {
        ArrayList<Ban> list = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        String sql = "SELECT * FROM Ban";
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Ban(
                        rs.getString("MaBan"),
                        rs.getString("TenBan"),
                        rs.getString("TrangThai"),
                        rs.getString("MaKV"),
                        rs.getInt("SoGhe"),
                        rs.getString("MaBanGop")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 5. Thêm bàn mới
    public boolean insert(Ban b) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "INSERT INTO Ban(MaBan, TenBan, MaKV, SoGhe, TrangThai, MaBanGop) VALUES(?, ?, ?, ?, N'Trống', NULL)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, b.getMaBan());
            ps.setString(2, b.getTenBan());
            ps.setString(3, b.getMaKV());
            ps.setInt(4, b.getSoGhe());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 6. Cập nhật thông tin bàn (Tên, Khu vực, Số ghế)
    public boolean updateInfo(Ban b) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE Ban SET TenBan = ?, MaKV = ?, SoGhe = ? WHERE MaBan = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, b.getTenBan());
            ps.setString(2, b.getMaKV());
            ps.setInt(3, b.getSoGhe());
            ps.setString(4, b.getMaBan());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 7. Xóa bàn
    public boolean delete(String maBan) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "DELETE FROM Ban WHERE MaBan = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maBan);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
