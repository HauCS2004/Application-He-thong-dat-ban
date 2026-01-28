package Entity;

import java.util.Date;

public class KhuyenMai {
    private String maKM;
    private String tenKM;
    private String loaiKM; // "Giảm %", "Giảm tiền", "Tặng món"
    private double giaTri;
    private double dieuKienToiThieu;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private String trangThai;
    private String hangVIPApDung; // NULL = tất cả, hoặc "Bạc", "Vàng", "Kim cương"

    // Constructor mặc định
    public KhuyenMai() {
    }

    // Constructor đầy đủ
    public KhuyenMai(String maKM, String tenKM, String loaiKM, double giaTri,
            double dieuKienToiThieu, Date ngayBatDau, Date ngayKetThuc,
            String trangThai, String hangVIPApDung) {
        this.maKM = maKM;
        this.tenKM = tenKM;
        this.loaiKM = loaiKM;
        this.giaTri = giaTri;
        this.dieuKienToiThieu = dieuKienToiThieu;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai = trangThai;
        this.hangVIPApDung = hangVIPApDung;
    }

    // Helper method: Kiểm tra khuyến mãi đang hoạt động
    public boolean isHoatDong() {
        if (trangThai == null || !trangThai.equals("Đang hoạt động"))
            return false;
        Date now = new Date();
        return (ngayBatDau == null || now.after(ngayBatDau)) &&
                (ngayKetThuc == null || now.before(ngayKetThuc));
    }

    // Helper method: Kiểm tra áp dụng cho hạng VIP
    public boolean apDungChoHang(String hangVIP) {
        return hangVIPApDung == null || hangVIPApDung.isEmpty() ||
                hangVIPApDung.equals(hangVIP);
    }

    // Helper method: Tính giảm giá
    public double tinhGiamGia(double tongTien) {
        if (tongTien < dieuKienToiThieu)
            return 0;

        if (loaiKM.equals("Giảm %")) {
            return tongTien * (giaTri / 100);
        } else if (loaiKM.equals("Giảm tiền")) {
            return giaTri;
        }
        return 0;
    }

    // --- GETTERS & SETTERS ---
    public String getMaKM() {
        return maKM;
    }

    public void setMaKM(String maKM) {
        this.maKM = maKM;
    }

    public String getTenKM() {
        return tenKM;
    }

    public void setTenKM(String tenKM) {
        this.tenKM = tenKM;
    }

    public String getLoaiKM() {
        return loaiKM;
    }

    public void setLoaiKM(String loaiKM) {
        this.loaiKM = loaiKM;
    }

    public double getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(double giaTri) {
        this.giaTri = giaTri;
    }

    public double getDieuKienToiThieu() {
        return dieuKienToiThieu;
    }

    public void setDieuKienToiThieu(double dieuKienToiThieu) {
        this.dieuKienToiThieu = dieuKienToiThieu;
    }

    public Date getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(Date ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public Date getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(Date ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getHangVIPApDung() {
        return hangVIPApDung;
    }

    public void setHangVIPApDung(String hangVIPApDung) {
        this.hangVIPApDung = hangVIPApDung;
    }

    @Override
    public String toString() {
        return tenKM + " (" + loaiKM + ": " + giaTri +
                (loaiKM.equals("Giảm %") ? "%" : "đ") + ")";
    }
}
