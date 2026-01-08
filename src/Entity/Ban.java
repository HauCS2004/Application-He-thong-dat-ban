package Entity;

public class Ban {
    private String maBan;
    private String tenBan;
    private String trangThai;
    private String maKV;
    private int soGhe; // Mới thêm
    private String maBanGop;

    public Ban() {}

    public Ban(String maBan, String tenBan, String trangThai, String maKV, int soGhe,String maBanGop) {
        this.maBan = maBan;
        this.tenBan = tenBan;
        this.trangThai = trangThai;
        this.maKV = maKV;
        this.soGhe = soGhe;
        this.maBanGop = maBanGop;
    }

    // Nhớ thêm Getter/Setter cho soGhe
    public int getSoGhe() { return soGhe; }
    public void setSoGhe(int soGhe) { this.soGhe = soGhe; }

    // Các getter cũ giữ nguyên...
    public String getMaBan() { return maBan; }
    public String getTenBan() { return tenBan; }
    public String getTrangThai() { return trangThai; }
    public String getMaKV() { return maKV; }
    public String getMaBanGop() { return maBanGop; }
    public void setMaBanGop(String maBanGop) { this.maBanGop = maBanGop; }
 // --- KHẮC PHỤC LỖI HIỂN THỊ TÊN BÀN ---
    @Override
    public String toString() {
        // Trả về tên bàn mà bạn muốn hiển thị
        return tenBan; 
        
        // Hoặc nếu muốn xịn hơn (hiện cả số ghế) thì dùng dòng dưới:
        // return tenBan + " (" + soGhe + " ghế)";
    }
}