package Entity;

public class ChiTietBangGia {
    private int maBG;
    private String maMon;
    private double donGia;
    private String ghiChu;

    public ChiTietBangGia() {}

    public ChiTietBangGia(int maBG, String maMon, double donGia, String ghiChu) {
        this.maBG = maBG;
        this.maMon = maMon;
        this.donGia = donGia;
        this.ghiChu = ghiChu;
    }

    public int getMaBG() { return maBG; }
    public void setMaBG(int maBG) { this.maBG = maBG; }

    public String getMaMon() { return maMon; }
    public void setMaMon(String maMon) { this.maMon = maMon; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    @Override
    public String toString() {
        return String.format("%.0f VNĐ", donGia);
    }
}
