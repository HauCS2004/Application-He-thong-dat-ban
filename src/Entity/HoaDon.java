package Entity;

import java.util.Date;

public class HoaDon {
    private int maHD;
    private java.util.Date ngayTao;
    private double tongTien; // Tổng tiền món ăn (chưa VAT)
    private double phanTramVAT; // % VAT (VD: 10)
    private double phiPhucVu; // % phí phục vụ (VD: 5)
    private double tienGiamGia; // Tổng số tiền giảm (KM + VIP)
    private double thanhTien; // Tổng cuối = tongTien*(1+VAT+Phi) - Giam
    private String trangThai; // "Chưa thanh toán", "Đã thanh toán", "Đã hủy"
    private String phuongThucThanhToan;
    private java.util.Date thoiGianThanhToan;
    private String maBan;
    private int soLuongKhach;
    private String sdtKhach;
    private String ghiChu;
    private String maNV;

    public HoaDon() {
    }

    // Constructor tạo mới (lúc mở bàn) — VAT/phí dùng mặc định
    public HoaDon(String maBan, int soLuongKhach, String sdtKhach, String ghiChu, String maNV) {
        this.maBan = maBan;
        this.soLuongKhach = soLuongKhach;
        this.sdtKhach = sdtKhach;
        this.ghiChu = ghiChu;
        this.maNV = maNV;
        this.ngayTao = new java.util.Date();
        this.trangThai = "Chưa thanh toán";
        this.tongTien = 0;
        this.phanTramVAT = 10; // Mặc định 10%
        this.phiPhucVu = 5; // Mặc định 5%
        this.tienGiamGia = 0;
        this.thanhTien = 0;
    }

    // Constructor đầy đủ (lúc đọc từ SQL)
    public HoaDon(int maHD, java.util.Date ngayTao, double tongTien,
            double phanTramVAT, double phiPhucVu, double tienGiamGia, double thanhTien,
            String trangThai, String phuongThucThanhToan, java.util.Date thoiGianThanhToan,
            String maBan, int soLuongKhach,
            String sdtKhach, String ghiChu, String maNV) {
        this.maHD = maHD;
        this.ngayTao = ngayTao;
        this.tongTien = tongTien;
        this.phanTramVAT = phanTramVAT;
        this.phiPhucVu = phiPhucVu;
        this.tienGiamGia = tienGiamGia;
        this.thanhTien = thanhTien;
        this.trangThai = trangThai;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.thoiGianThanhToan = thoiGianThanhToan;
        this.maBan = maBan;
        this.soLuongKhach = soLuongKhach;
        this.sdtKhach = sdtKhach;
        this.ghiChu = ghiChu;
        this.maNV = maNV;
    }

    // --- GETTER & SETTER ---
    public int getMaHD() {
        return maHD;
    }

    public void setMaHD(int maHD) {
        this.maHD = maHD;
    }

    public Date getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Date ngayTao) {
        this.ngayTao = ngayTao;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public java.util.Date getThoiGianThanhToan() {
        return thoiGianThanhToan;
    }

    public void setThoiGianThanhToan(java.util.Date thoiGianThanhToan) {
        this.thoiGianThanhToan = thoiGianThanhToan;
    }

    public String getMaBan() {
        return maBan;
    }

    public void setMaBan(String maBan) {
        this.maBan = maBan;
    }

    public int getSoLuongKhach() {
        return soLuongKhach;
    }

    public void setSoLuongKhach(int soLuongKhach) {
        this.soLuongKhach = soLuongKhach;
    }

    public String getSdtKhach() {
        return sdtKhach;
    }

    public void setSdtKhach(String sdtKhach) {
        this.sdtKhach = sdtKhach;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    // --- GĐ4: Getters/Setters mới ---
    public double getPhanTramVAT() {
        return phanTramVAT;
    }

    public void setPhanTramVAT(double v) {
        this.phanTramVAT = v;
    }

    public double getPhiPhucVu() {
        return phiPhucVu;
    }

    public void setPhiPhucVu(double v) {
        this.phiPhucVu = v;
    }

    public double getTienGiamGia() {
        return tienGiamGia;
    }

    public void setTienGiamGia(double v) {
        this.tienGiamGia = v;
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double v) {
        this.thanhTien = v;
    }

    /** Tính thành tiền dự kiến (chưa commit vào DB) */
    public double tinhThanhTienDuKien() {
        return tongTien * (1 + phanTramVAT / 100.0 + phiPhucVu / 100.0) - tienGiamGia;
    }
}
