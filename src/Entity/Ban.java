package Entity;

public class Ban {
    private String maBan;
    private String tenBan;
    private String trangThai; // Trống, Có Khách, Đã Đặt, Đang Gộp, ...
    private String maKV; // Mã khu vực (Tầng G, Tầng 1, ...)
    private int soGhe;
    private String maBanGop; // Nếu bàn này đang gộp vào bàn khác, đây là mã bàn đích

    public Ban() {
    }

    public Ban(String maBan, String tenBan, String trangThai, String maKV, int soGhe, String maBanGop) {
        this.maBan = maBan;
        this.tenBan = tenBan;
        this.trangThai = trangThai;
        this.maKV = maKV;
        this.soGhe = soGhe;
        this.maBanGop = maBanGop;
    }

    // Constructor rút gọn (không có maBanGop)
    public Ban(String maBan, String tenBan, String trangThai, String maKV, int soGhe) {
        this(maBan, tenBan, trangThai, maKV, soGhe, null);
    }

    public String getMaBan() {
        return maBan;
    }

    public void setMaBan(String maBan) {
        this.maBan = maBan;
    }

    public String getTenBan() {
        return tenBan;
    }

    public void setTenBan(String tenBan) {
        this.tenBan = tenBan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMaKV() {
        return maKV;
    }

    public void setMaKV(String maKV) {
        this.maKV = maKV;
    }

    public int getSoGhe() {
        return soGhe;
    }

    public void setSoGhe(int soGhe) {
        this.soGhe = soGhe;
    }

    public String getMaBanGop() {
        return maBanGop;
    }

    public void setMaBanGop(String maBanGop) {
        this.maBanGop = maBanGop;
    }

    @Override
    public String toString() {
        return tenBan; // Để hiển thị trong ComboBox nếu cần
    }
}
