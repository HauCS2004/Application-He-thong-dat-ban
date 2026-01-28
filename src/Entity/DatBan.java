package Entity;

import java.util.Date;

public class DatBan {
    private int maDat;
    private String maBan;
    private String tenKhach;
    private String sdt;
    private Date thoiGianBatDau; // ✅ MỚI: Thời gian bắt đầu
    private Date thoiGianKetThuc; // ✅ MỚI: Thời gian kết thúc
    private int soLuongKhach;
    private String trangThai; // ✅ MỚI: Chờ xác nhận, Đã xác nhận, Đã hủy, Hoàn thành
    private double tienCoc; // ✅ MỚI: Tiền cọc
    private String ghiChu;
    private Date ngayTao; // ✅ MỚI: Ngày tạo booking
    private Integer maHD; // ✅ MỚI: Link với hóa đơn (nullable)

    // Constructor mặc định
    public DatBan() {
    }

    // Constructor đầy đủ (cho SELECT từ database)
    public DatBan(int maDat, String maBan, String tenKhach, String sdt,
            Date thoiGianBatDau, Date thoiGianKetThuc, int soLuongKhach,
            String trangThai, double tienCoc, String ghiChu, Date ngayTao, Integer maHD) {
        this.maDat = maDat;
        this.maBan = maBan;
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

    // Constructor cho INSERT (tạo mới booking)
    public DatBan(String maBan, String tenKhach, String sdt,
            Date thoiGianBatDau, Date thoiGianKetThuc, int soLuongKhach,
            double tienCoc, String ghiChu) {
        this.maBan = maBan;
        this.tenKhach = tenKhach;
        this.sdt = sdt;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.soLuongKhach = soLuongKhach;
        this.trangThai = "Chờ xác nhận"; // Mặc định
        this.tienCoc = tienCoc;
        this.ghiChu = ghiChu;
        this.ngayTao = new Date();
    }

    // --- GETTERS & SETTERS ---
    public int getMaDat() {
        return maDat;
    }

    public void setMaDat(int maDat) {
        this.maDat = maDat;
    }

    public String getMaBan() {
        return maBan;
    }

    public void setMaBan(String maBan) {
        this.maBan = maBan;
    }

    public String getTenKhach() {
        return tenKhach;
    }

    public void setTenKhach(String tenKhach) {
        this.tenKhach = tenKhach;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public Date getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(Date thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public Date getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(Date thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public int getSoLuongKhach() {
        return soLuongKhach;
    }

    public void setSoLuongKhach(int soLuongKhach) {
        this.soLuongKhach = soLuongKhach;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public double getTienCoc() {
        return tienCoc;
    }

    public void setTienCoc(double tienCoc) {
        this.tienCoc = tienCoc;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public Date getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Date ngayTao) {
        this.ngayTao = ngayTao;
    }

    public Integer getMaHD() {
        return maHD;
    }

    public void setMaHD(Integer maHD) {
        this.maHD = maHD;
    }

    // Helper method: Kiểm tra đang trong khung giờ hay không
    public boolean isDangTrongGio() {
        if (thoiGianBatDau == null || thoiGianKetThuc == null)
            return false;
        Date now = new Date();
        return now.after(thoiGianBatDau) && now.before(thoiGianKetThuc);
    }
}