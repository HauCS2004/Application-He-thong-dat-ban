package Entity;

public class KhachHang {
    private String soDienThoai;
    private String tenKhach;
    private int diemTichLuy;

    public KhachHang() { }

    public KhachHang(String soDienThoai, String tenKhach, int diemTichLuy) {
        this.soDienThoai = soDienThoai;
        this.tenKhach = tenKhach;
        this.diemTichLuy = diemTichLuy;
    }

    // --- LOGIC TÍNH HẠNG VÀ GIẢM GIÁ ---
    public String getHangThanhVien() {
        if (diemTichLuy >= 1000) return "VIP Kim Cương";
        else if (diemTichLuy >= 500) return "VIP Vàng";
        else if (diemTichLuy >= 200) return "VIP Bạc";
        else return "Thành Viên";
    }

    public int getPhanTramGiam() {
        if (diemTichLuy >= 1000) return 15; // 15%
        else if (diemTichLuy >= 500) return 10; // 10%
        else if (diemTichLuy >= 200) return 5;  // 5%
        else return 0;
    }

    // --- GETTER & SETTER ---
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getTenKhach() { return tenKhach; }
    public void setTenKhach(String tenKhach) { this.tenKhach = tenKhach; }
    public int getDiemTichLuy() { return diemTichLuy; }
    public void setDiemTichLuy(int diemTichLuy) { this.diemTichLuy = diemTichLuy; }

    // --- TO STRING (Code của bạn) ---
    @Override
    public String toString() { 
        return tenKhach + " (" + diemTichLuy + " điểm)" + " (" + getHangThanhVien() + " - Giảm " + getPhanTramGiam() + "%)";
    }
}