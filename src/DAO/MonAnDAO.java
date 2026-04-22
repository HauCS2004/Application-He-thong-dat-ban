package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import connectDB.ConnectDB;
import Entity.MonAn;

public class MonAnDAO {

    // ── Helper đọc 1 dòng MonAn từ ResultSet ────────────────────────────────
    private MonAn fromRS(ResultSet rs) throws Exception {
        MonAn m = new MonAn(
                rs.getString("MaMon"),
                rs.getString("TenMon"),
                rs.getString("DonViTinh"),
                rs.getString("HinhAnh"),
                rs.getString("MaLoai"),
                rs.getString("TrangThai"));
        return m;
    }

    // 1. Lấy tất cả món ăn (kể cả Ngừng bán — dùng cho màn hình quản lý)
    public ArrayList<MonAn> getAll() {
        ArrayList<MonAn> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM MonAn ORDER BY TenMon");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(fromRS(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 1b. Chỉ lấy món CON_MON và HET_MON (dùng cho màn hình gọi món)
    public ArrayList<MonAn> getAllForOrder() {
        ArrayList<MonAn> list = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM MonAn WHERE TrangThai IN (N'" + MonAn.CON_MON + "', N'" + MonAn.HET_MON
                            + "') ORDER BY TenMon");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(fromRS(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Thêm món
    public boolean insert(MonAn m) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "INSERT INTO MonAn (MaMon, TenMon, DonViTinh, HinhAnh, MaLoai, TrangThai) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, m.getMaMon());
            ps.setString(2, m.getTenMon());
            ps.setString(3, m.getDonViTinh());
            ps.setString(4, m.getHinhAnh());
            ps.setString(5, m.getMaLoai());
            ps.setString(6, m.getTrangThai());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Xóa thật (chỉ khi món chưa từng có trong hóa đơn)
    public boolean delete(String maMon) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM MonAn WHERE MaMon = ?");
            ps.setString(1, maMon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. Sửa thông tin món (không đổi TrangThai)
    public boolean update(MonAn m) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE MonAn SET TenMon=?, DonViTinh=?, HinhAnh=?, MaLoai=? WHERE MaMon=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, m.getTenMon());
            ps.setString(2, m.getDonViTinh());
            ps.setString(3, m.getHinhAnh());
            ps.setString(4, m.getMaLoai());
            ps.setString(5, m.getMaMon());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. Đổi trạng thái (CON_MON / HET_MON / NGUNG_BAN)
    public boolean updateTrangThai(String maMon, String trangThai) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE MonAn SET TrangThai=? WHERE MaMon=?");
            ps.setString(1, trangThai);
            ps.setString(2, maMon);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 6. Kiểm tra món đã từng xuất hiện trong hóa đơn chưa (để cho phép xóa thật)
    public boolean isUsedInInvoice(String maMon) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM ChiTietHoaDon WHERE MaMon = ?");
            ps.setString(1, maMon);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true; // an toàn: coi như đang dùng nếu lỗi
    }

    // 7. Tìm kiếm đa năng (có lọc theo trạng thái nếu truyền vào)
    public ArrayList<MonAn> timKiem(String keyword, String maLoai) {
        return timKiem(keyword, maLoai, null); // null = tất cả trạng thái
    }

    public ArrayList<MonAn> timKiem(String keyword, String maLoai, String trangThai) {
        ArrayList<MonAn> list = new ArrayList<>();
        String sql = "SELECT * FROM MonAn WHERE TenMon LIKE ?";
        if (maLoai != null && !maLoai.isEmpty())
            sql += " AND MaLoai = ?";
        if (trangThai != null && !trangThai.isEmpty())
            sql += " AND TrangThai = ?";
        sql += " ORDER BY TenMon";

        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            int idx = 1;
            ps.setString(idx++, "%" + keyword + "%");
            if (maLoai != null && !maLoai.isEmpty())
                ps.setString(idx++, maLoai);
            if (trangThai != null && !trangThai.isEmpty())
                ps.setString(idx++, trangThai);

            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(fromRS(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 8. Lấy món theo mã
    public MonAn getByMaMon(String maMon) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM MonAn WHERE MaMon = ?");
            ps.setString(1, maMon);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return fromRS(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
