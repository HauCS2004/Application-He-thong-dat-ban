package Entity;
import java.util.Date;

public class DatBan {
    private int maDat;
    private String maBan;
    private String tenKhach;
    private String sdt;
    private Date thoiGian;
    private int soLuongKhach;
    private String ghiChu;

    public DatBan(String maBan, String tenKhach, String sdt, Date thoiGian, int soLuongKhach, String ghiChu) {
        this.maBan = maBan;
        this.tenKhach = tenKhach;
        this.sdt = sdt;
        this.thoiGian = thoiGian;
        this.soLuongKhach = soLuongKhach;
        this.ghiChu = ghiChu;
    }
    // Bạn tự Generate Getter/Setter nhé cho ngắn code
    public String getMaBan() { return maBan; }
    public String getTenKhach() { return tenKhach; }
    public String getSdt() { return sdt; }
    public Date getThoiGian() { return thoiGian; }
    public int getSoLuongKhach() { return soLuongKhach; }
    public String getGhiChu() { return ghiChu; }
}