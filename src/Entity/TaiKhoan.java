package Entity;

public class TaiKhoan {
    private String maTK; // = MaNV
    private String matKhau;
    private String vaiTro; // Nhân viên, Quản lý

    public TaiKhoan() {
    }

    public TaiKhoan(String maTK, String matKhau, String vaiTro) {
        this.maTK = maTK;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
    }

    public boolean isQuanLy() {
        return "Quản lý".equals(vaiTro);
    }

    // --- Getters & Setters ---
    public String getMaTK() {
        return maTK;
    }

    public void setMaTK(String maTK) {
        this.maTK = maTK;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    @Override
    public String toString() {
        return maTK + " (" + vaiTro + ")";
    }
}
