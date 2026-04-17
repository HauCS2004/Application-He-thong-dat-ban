package Entity;

public class MonAn {
    // ── Hằng số Trạng Thái ──────────────────────────────────────────────
    public static final String CON_MON = "Còn món";
    public static final String HET_MON = "Hết món";
    public static final String NGUNG_BAN = "Ngừng bán";

    public static final String[] ALL_STATUSES = { CON_MON, HET_MON, NGUNG_BAN };

    /** Chuyển mã thành nhãn hiển thị (giống nhau vì đã dùng tiếng Việt). */
    public static String toDisplayLabel(String trangThai) {
        if (trangThai == null)
            return CON_MON;
        return trangThai;
    }

    private String maMon;
    private String tenMon;
    private String donViTinh;
    private String hinhAnh;
    private String maLoai;
    private String trangThai; // "Còn món" | "Hết món" | "Ngừng bán"

    public MonAn() {
    }

    public MonAn(String maMon, String tenMon, String donViTinh, String hinhAnh, String maLoai) {
        this(maMon, tenMon, donViTinh, hinhAnh, maLoai, CON_MON);
    }

    public MonAn(String maMon, String tenMon, String donViTinh, String hinhAnh, String maLoai, String trangThai) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.donViTinh = donViTinh;
        this.hinhAnh = hinhAnh;
        this.maLoai = maLoai;
        this.trangThai = (trangThai != null) ? trangThai : CON_MON;
    }

    // Getter và Setter
    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(String maLoai) {
        this.maLoai = maLoai;
    }

    public String getTrangThai() {
        return trangThai != null ? trangThai : CON_MON;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return tenMon;
    }
}