package Entity;

import java.util.Objects;

public class LoaiMon {
    private String maLoai;
    private String tenLoai;

    public LoaiMon(String maLoai, String tenLoai) {
        this.maLoai = maLoai;
        this.tenLoai = tenLoai;
    }
    
    // Getter Setter
    public String getMaLoai() { return maLoai; }
    public void setMaLoai(String maLoai) { this.maLoai = maLoai; }
    public String getTenLoai() { return tenLoai; }
    public void setTenLoai(String tenLoai) { this.tenLoai = tenLoai; }

    // --- QUAN TRỌNG NHẤT: ComboBox sẽ hiển thị cái này ---
    @Override
    public String toString() {
        return tenLoai; // Hiển thị tên (VD: Món khai vị)
    }

    // Hàm này để giúp so sánh khi chọn trên bảng (Optional nhưng nên có)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LoaiMon loaiMon = (LoaiMon) obj;
        return Objects.equals(maLoai, loaiMon.maLoai);
    }
}