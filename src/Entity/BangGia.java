package Entity;

public class BangGia {
    private int maGia;
    private String maMon;
    private double donGia;
    private String tuNgay; // "yyyy-MM-dd" hoặc null
    private String denNgay; // "yyyy-MM-dd" hoặc null
    private String gioBatDau; // "HH:mm:ss" hoặc null
    private String gioKetThuc; // "HH:mm:ss" hoặc null
    private int uuTien;
    private String ghiChu;

    public BangGia() {
    }

    public BangGia(int maGia, String maMon, double donGia,
            String tuNgay, String denNgay,
            String gioBatDau, String gioKetThuc,
            int uuTien, String ghiChu) {
        this.maGia = maGia;
        this.maMon = maMon;
        this.donGia = donGia;
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.uuTien = uuTien;
        this.ghiChu = ghiChu;
    }

    // Constructor cho INSERT mới (không có maGia)
    public BangGia(String maMon, double donGia,
            String tuNgay, String denNgay,
            String gioBatDau, String gioKetThuc,
            int uuTien, String ghiChu) {
        this.maMon = maMon;
        this.donGia = donGia;
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.uuTien = uuTien;
        this.ghiChu = ghiChu;
    }

    // --- Getters & Setters ---
    public int getMaGia() {
        return maGia;
    }

    public void setMaGia(int maGia) {
        this.maGia = maGia;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    public String getTuNgay() {
        return tuNgay;
    }

    public void setTuNgay(String tuNgay) {
        this.tuNgay = tuNgay;
    }

    public String getDenNgay() {
        return denNgay;
    }

    public void setDenNgay(String denNgay) {
        this.denNgay = denNgay;
    }

    public String getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(String g) {
        this.gioBatDau = g;
    }

    public String getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(String g) {
        this.gioKetThuc = g;
    }

    public int getUuTien() {
        return uuTien;
    }

    public void setUuTien(int uuTien) {
        this.uuTien = uuTien;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    /** Mô tả ngắn cho hiển thị trong table */
    @Override
    public String toString() {
        String khung = "";
        if (gioBatDau != null && gioKetThuc != null)
            khung = " [" + gioBatDau.substring(0, 5) + " - " + gioKetThuc.substring(0, 5) + "]";
        return String.format("%.0f VNĐ%s", donGia, khung);
    }
}
