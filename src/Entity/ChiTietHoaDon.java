package Entity;

public class ChiTietHoaDon {
    private int maHD;       // Khóa chính (cùng với maMon)
    private String maMon;   // Khóa chính
    private int soLuong;
    private double donGia;  // Giá tại thời điểm bán
    
    // Biến phụ (Helper) - Dùng để hiển thị lên bảng, không lưu xuống CSDL
    private String tenMonAn; 

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(int maHD, String maMon, int soLuong, double donGia) {
        this.maHD = maHD;
        this.maMon = maMon;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    // --- GETTER & SETTER ---
    public int getMaHD() {
        return maHD;
    }

    public void setMaHD(int maHD) {
        this.maHD = maHD;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    // Getter/Setter cho tên món (Quan trọng để hiện lên bảng)
    public String getTenMonAn() {
        return tenMonAn;
    }

    public void setTenMonAn(String tenMonAn) {
        this.tenMonAn = tenMonAn;
    }
    
    // Tính thành tiền (Số lượng * Đơn giá) - Tiện lợi khi dùng
    public double getThanhTien() {
        return this.soLuong * this.donGia;
    }
}