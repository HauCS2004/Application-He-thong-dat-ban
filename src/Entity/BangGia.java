package Entity;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class BangGia {
    private int maBG;
    private String tenBG;
    private String loaiBG;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private Time gioBatDau;
    private Time gioKetThuc;
    private int uuTien;
    private String trangThai;
    private String ghiChu;
    private Timestamp ngayTao;

    public BangGia() {
    }

    public BangGia(int maBG, String tenBG, String loaiBG, Date ngayBatDau, Date ngayKetThuc, Time gioBatDau, Time gioKetThuc, int uuTien, String trangThai, String ghiChu, Timestamp ngayTao) {
        this.maBG = maBG;
        this.tenBG = tenBG;
        this.loaiBG = loaiBG;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.uuTien = uuTien;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.ngayTao = ngayTao;
    }

    public int getMaBG() { return maBG; }
    public void setMaBG(int maBG) { this.maBG = maBG; }

    public String getTenBG() { return tenBG; }
    public void setTenBG(String tenBG) { this.tenBG = tenBG; }

    public String getLoaiBG() { return loaiBG; }
    public void setLoaiBG(String loaiBG) { this.loaiBG = loaiBG; }

    public Date getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(Date ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public Date getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Date ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public Time getGioBatDau() { return gioBatDau; }
    public void setGioBatDau(Time gioBatDau) { this.gioBatDau = gioBatDau; }

    public Time getGioKetThuc() { return gioKetThuc; }
    public void setGioKetThuc(Time gioKetThuc) { this.gioKetThuc = gioKetThuc; }

    public int getUuTien() { return uuTien; }
    public void setUuTien(int uuTien) { this.uuTien = uuTien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }

    @Override
    public String toString() {
        return tenBG + " (" + trangThai + ")";
    }
}
