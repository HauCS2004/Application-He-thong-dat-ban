package Entity;

import java.util.Date;

public class HoaDon {
    private int maHD;           // Khóa chính (Tự tăng)
    private Date ngayTao;
    private double tongTien;
    private int trangThai;      // 0: Chưa thanh toán, 1: Đã thanh toán
    private String maBan;       // Bàn nào đang ngồi
    private int soLuongKhach;   // Số người
    private String sdtKhach;    // Khách thành viên (có thể null)
    private String ghiChu;

    public HoaDon() {
    }

    // Constructor dùng để tạo mới (Lúc mở bàn)
    public HoaDon(String maBan, int soLuongKhach, String sdtKhach, String ghiChu) {
        this.maBan = maBan;
        this.soLuongKhach = soLuongKhach;
        this.sdtKhach = sdtKhach;
        this.ghiChu = ghiChu;
        this.ngayTao = new Date();
        this.trangThai = 0; // Mặc định là chưa thanh toán
        this.tongTien = 0;
    }

    // Constructor đầy đủ (Lúc lấy từ SQL lên)
    public HoaDon(int maHD, Date ngayTao, double tongTien, int trangThai, String maBan, int soLuongKhach, String sdtKhach, String ghiChu) {
        this.maHD = maHD;
        this.ngayTao = ngayTao;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
        this.maBan = maBan;
        this.soLuongKhach = soLuongKhach;
        this.sdtKhach = sdtKhach;
        this.ghiChu = ghiChu;
    }

    // --- GETTER & SETTER ---
    public int getMaHD() { return maHD; }
    public void setMaHD(int maHD) { this.maHD = maHD; }
    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }
    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }
    public int getTrangThai() { return trangThai; }
    public void setTrangThai(int trangThai) { this.trangThai = trangThai; }
    public String getMaBan() { return maBan; }
    public void setMaBan(String maBan) { this.maBan = maBan; }
    public int getSoLuongKhach() { return soLuongKhach; }
    public void setSoLuongKhach(int soLuongKhach) { this.soLuongKhach = soLuongKhach; }
    public String getSdtKhach() { return sdtKhach; }
    public void setSdtKhach(String sdtKhach) { this.sdtKhach = sdtKhach; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}