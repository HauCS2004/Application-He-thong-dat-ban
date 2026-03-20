package Entity;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class DatBan {
    private int maDat;
    private List<String> danhSachBan; // [GĐ2] Thay thế maBan đơn
    private String tenKhach;
    private String sdt;
    private Date thoiGianBatDau;
    private Date thoiGianKetThuc;
    private int soLuongKhach;
    private String trangThai;
    private double tienCoc;
    private String ghiChu;
    private Date ngayTao;
    private Integer maHD;

    // Constructor mặc định
    public DatBan() {
        this.danhSachBan = new ArrayList<>();
    }

    // Constructor đầy đủ (SELECT từ DB — truyền bàn đầu tiên vào list)
    public DatBan(int maDat, String maBan, String tenKhach, String sdt,
            Date thoiGianBatDau, Date thoiGianKetThuc, int soLuongKhach,
            String trangThai, double tienCoc, String ghiChu, Date ngayTao, Integer maHD) {
        this.maDat = maDat;
        this.danhSachBan = new ArrayList<>();
        if (maBan != null)
            this.danhSachBan.add(maBan);
        this.tenKhach = tenKhach;
        this.sdt = sdt;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.soLuongKhach = soLuongKhach;
        this.trangThai = trangThai;
        this.tienCoc = tienCoc;
        this.ghiChu = ghiChu;
        this.ngayTao = ngayTao;
        this.maHD = maHD;
    }

    // Backward-compat: maBan (String), tienCoc (int)
    public DatBan(String maBan, String tenKhach, String sdt,
            Date thoiGianBatDau, Date thoiGianKetThuc,
            int soLuongKhach, int tienCoc, String ghiChu) {
        this.danhSachBan = new ArrayList<>();
        if (maBan != null)
            this.danhSachBan.add(maBan);
        this.tenKhach = tenKhach;
        this.sdt = sdt;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.soLuongKhach = soLuongKhach;
        this.trangThai = "Chờ xác nhận";
        this.tienCoc = (double) tienCoc;
        this.ghiChu = ghiChu;
        this.ngayTao = new Date();
    }

    // Constructor cho INSERT mới (nhiều bàn)
    public DatBan(List<String> danhSachBan, String tenKhach, String sdt,
            Date thoiGianBatDau, Date thoiGianKetThuc,
            int soLuongKhach, double tienCoc, String ghiChu) {
        this.danhSachBan = danhSachBan != null ? danhSachBan : new ArrayList<>();
        this.tenKhach = tenKhach;
        this.sdt = sdt;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.soLuongKhach = soLuongKhach;
        this.trangThai = "Chờ xác nhận";
        this.tienCoc = tienCoc;
        this.ghiChu = ghiChu;
        this.ngayTao = new Date();
    }

    // Backward-compat: lấy bàn đầu tiên
    public String getMaBan() {
        return (danhSachBan != null && !danhSachBan.isEmpty()) ? danhSachBan.get(0) : null;
    }

    public void setMaBan(String maBan) {
        if (danhSachBan == null)
            danhSachBan = new ArrayList<>();
        if (maBan != null && !danhSachBan.contains(maBan))
            danhSachBan.add(0, maBan);
    }

    /** Helper: đang trong khung giờ đặt không? */
    public boolean isDangTrongGio() {
        if (thoiGianBatDau == null || thoiGianKetThuc == null)
            return false;
        Date now = new Date();
        return now.after(thoiGianBatDau) && now.before(thoiGianKetThuc);
    }

    // --- GETTERS & SETTERS ---
    public int getMaDat() {
        return maDat;
    }

    public void setMaDat(int v) {
        this.maDat = v;
    }

    public List<String> getDanhSachBan() {
        return danhSachBan;
    }

    public void setDanhSachBan(List<String> l) {
        this.danhSachBan = l;
    }

    public String getTenKhach() {
        return tenKhach;
    }

    public void setTenKhach(String v) {
        this.tenKhach = v;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String v) {
        this.sdt = v;
    }

    public Date getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(Date v) {
        this.thoiGianBatDau = v;
    }

    public Date getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(Date v) {
        this.thoiGianKetThuc = v;
    }

    public int getSoLuongKhach() {
        return soLuongKhach;
    }

    public void setSoLuongKhach(int v) {
        this.soLuongKhach = v;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String v) {
        this.trangThai = v;
    }

    public double getTienCoc() {
        return tienCoc;
    }

    public void setTienCoc(double v) {
        this.tienCoc = v;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String v) {
        this.ghiChu = v;
    }

    public Date getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Date v) {
        this.ngayTao = v;
    }

    public Integer getMaHD() {
        return maHD;
    }

    public void setMaHD(Integer v) {
        this.maHD = v;
    }
}