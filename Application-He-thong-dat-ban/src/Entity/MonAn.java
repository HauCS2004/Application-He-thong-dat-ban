package Entity;

public class MonAn {
    private String maMon;
    private String tenMon;
    private String donViTinh;
    private double donGia;
    private String hinhAnh; // Chỉ lưu tên file (vd: ga.png)
    private String maLoai;  // Mã loại (L01, L02...)

    public MonAn() { }

    public MonAn(String maMon, String tenMon, String donViTinh, double donGia, String hinhAnh, String maLoai) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.donViTinh = donViTinh;
        this.donGia = donGia;
        this.hinhAnh = hinhAnh;
        this.maLoai = maLoai;
    }

    // Getter và Setter
    public String getMaMon() { return maMon; }
    public void setMaMon(String maMon) { this.maMon = maMon; }
    public String getTenMon() { return tenMon; }
    public void setTenMon(String tenMon) { this.tenMon = tenMon; }
    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }
    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
    public String getMaLoai() { return maLoai; }
    public void setMaLoai(String maLoai) { this.maLoai = maLoai; }
    
    @Override
    public String toString() { return tenMon; }
}