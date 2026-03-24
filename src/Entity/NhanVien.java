package Entity;

import java.util.Date;

public class NhanVien {
    private String maNV;
    private String tenNV;
    private String soDienThoai;
    private String email;
    private String cccd;
    private String gioiTinh;
    private String trangThai;
    private java.util.Date ngayVaoLam;
    // Tham chiếu tới TaiKhoan (load khi cần, có thể null)
    private TaiKhoan taiKhoan;

    // Constructor mặc định
    public NhanVien() {
    }

    // Constructor cho login (dùng TaiKhoan riêng)
    public NhanVien(String maNV, String tenNV) {
        this.maNV = maNV;
        this.tenNV = tenNV;
    }

    // Constructor đầy đủ (cho SELECT từ database)
    public NhanVien(String maNV, String tenNV, String gioiTinh, String soDienThoai,
            String email, String cccd, java.util.Date ngayVaoLam, String trangThai) {
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.gioiTinh = gioiTinh;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.cccd = cccd;
        this.ngayVaoLam = ngayVaoLam;
        this.trangThai = trangThai;
    }

    // Constructor cũ để tương thích ngược nếu có file gọi
    public NhanVien(String maNV, String tenNV, String soDienThoai,
            String email, java.util.Date ngayVaoLam) {
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.ngayVaoLam = ngayVaoLam;
        this.trangThai = "Đang làm việc"; // Mặc định
    }

    /** Tiện ích: kiểm tra vai trò qua TaiKhoan đính kèm */
    public boolean isQuanLy() {
        return taiKhoan != null && taiKhoan.isQuanLy();
    }

    // --- GETTERS & SETTERS ---
    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getTenNV() {
        return tenNV;
    }

    public void setTenNV(String tenNV) {
        this.tenNV = tenNV;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String s) {
        this.soDienThoai = s;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public java.util.Date getNgayVaoLam() {
        return ngayVaoLam;
    }

    public void setNgayVaoLam(java.util.Date d) {
        this.ngayVaoLam = d;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public TaiKhoan getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(TaiKhoan tk) {
        this.taiKhoan = tk;
    }

    @Override
    public String toString() {
        String vaiTro = (taiKhoan != null) ? taiKhoan.getVaiTro() : "Nhân viên";
        return tenNV + " (" + vaiTro + ")";
    }
}
