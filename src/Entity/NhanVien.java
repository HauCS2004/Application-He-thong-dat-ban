package Entity;

import java.util.Date;

public class NhanVien {
    private String maNV;
    private String tenNV;
    private String matKhau;
    private String chucVu;
    private String soDienThoai;
    private String email;
    private Date ngayVaoLam;

    // Constructor mặc định
    public NhanVien() {
    }

    // Constructor cho login (chỉ cần mã NV và mật khẩu)
    public NhanVien(String maNV, String matKhau) {
        this.maNV = maNV;
        this.matKhau = matKhau;
    }

    // Constructor đầy đủ
    public NhanVien(String maNV, String tenNV, String matKhau, String chucVu,
            String soDienThoai, String email, Date ngayVaoLam) {
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.matKhau = matKhau;
        this.chucVu = chucVu;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.ngayVaoLam = ngayVaoLam;
    }

    // Helper methods
    public boolean isQuanLy() {
        return chucVu != null && chucVu.equals("Quản lý");
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

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getNgayVaoLam() {
        return ngayVaoLam;
    }

    public void setNgayVaoLam(Date ngayVaoLam) {
        this.ngayVaoLam = ngayVaoLam;
    }

    @Override
    public String toString() {
        return tenNV + " (" + chucVu + ")";
    }
}
