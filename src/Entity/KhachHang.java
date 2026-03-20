package Entity;

import java.util.Date;

public class KhachHang {
    private String soDienThoai;
    private String tenKhach;
    private String email;
    private Date ngaySinh;
    private int diemTichLuy;
    private String hangVIP; // ✅ MỚI: Đồng, Bạc, Vàng, Kim cương
    private Date ngayTao;
    private Date lanGiaoDichCuoi; 
    private double tongChiTieu; 
    private String ghiChu;

    // Constructor mặc định
    public KhachHang() {
    }

    // Constructor cơ bản (backward compatible)
    public KhachHang(String soDienThoai, String tenKhach, int diemTichLuy) {
        this.soDienThoai = soDienThoai;
        this.tenKhach = tenKhach;
        this.diemTichLuy = diemTichLuy;
        this.hangVIP = getHangThanhVien(); // Tự động set
    }

    // Constructor đầy đủ (cho SELECT từ database)
    public KhachHang(String soDienThoai, String tenKhach, String email, Date ngaySinh,
            int diemTichLuy, String hangVIP, Date ngayTao, Date lanGiaoDichCuoi,
            double tongChiTieu, String ghiChu) {
        this.soDienThoai = soDienThoai;
        this.tenKhach = tenKhach;
        this.email = email;
        this.ngaySinh = ngaySinh;
        this.diemTichLuy = diemTichLuy;
        this.hangVIP = hangVIP;
        this.ngayTao = ngayTao;
        this.lanGiaoDichCuoi = lanGiaoDichCuoi;
        this.tongChiTieu = tongChiTieu;
        this.ghiChu = ghiChu;
    }

    // --- LOGIC TÍNH HẠNG VÀ GIẢM GIÁ (cập nhật theo DB) ---
    public String getHangThanhVien() {
        if (hangVIP != null && !hangVIP.isEmpty()) {
            return hangVIP; // Dùng giá trị từ DB nếu có
        }
        // Tính toán theo điểm (fallback)
        if (diemTichLuy >= 1000)
            return "Kim cương";
        else if (diemTichLuy >= 500)
            return "Vàng";
        else if (diemTichLuy >= 200)
            return "Bạc";
        else
            return "Đồng";
    }

    public int getPhanTramGiam() {
        String hang = getHangThanhVien();
        switch (hang) {
            case "Kim cương":
                return 15;
            case "Vàng":
                return 10;
            case "Bạc":
                return 5;
            default:
                return 0;
        }
    }

    // --- GETTERS & SETTERS ---
    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getTenKhach() {
        return tenKhach;
    }

    public void setTenKhach(String tenKhach) {
        this.tenKhach = tenKhach;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
        // Tự động cập nhật hạng khi điểm thay đổi (nếu chưa có từ DB)
        if (this.hangVIP == null || this.hangVIP.isEmpty()) {
            this.hangVIP = getHangThanhVien();
        }
    }

    public String getHangVIP() {
        return hangVIP;
    }

    public void setHangVIP(String hangVIP) {
        this.hangVIP = hangVIP;
    }

    public Date getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Date ngayTao) {
        this.ngayTao = ngayTao;
    }

    public Date getLanGiaoDichCuoi() {
        return lanGiaoDichCuoi;
    }

    public void setLanGiaoDichCuoi(Date lanGiaoDichCuoi) {
        this.lanGiaoDichCuoi = lanGiaoDichCuoi;
    }

    public double getTongChiTieu() {
        return tongChiTieu;
    }

    public void setTongChiTieu(double tongChiTieu) {
        this.tongChiTieu = tongChiTieu;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    // --- TO STRING ---
    @Override
    public String toString() {
        return tenKhach + " (" + diemTichLuy + " điểm) - "
                + getHangThanhVien() + " (Giảm " + getPhanTramGiam() + "%)";
    }
}