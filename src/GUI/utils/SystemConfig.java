package GUI.utils;

import java.io.*;
import java.util.Properties;

/**
 * SystemConfig – Quản lý cấu hình hệ thống nhà hàng.
 * Đọc/ghi từ file `system_config.properties` ở thư mục gốc dự án.
 *
 * Các key được lưu:
 *   res.name       – Tên nhà hàng
 *   res.address    – Địa chỉ
 *   res.phone      – Số điện thoại / Hotline
 *   res.logo       – URL logo (tuỳ chọn)
 *   tax.vat        – Thuế VAT (%)
 *   tax.service    – Phí phục vụ (%)
 *   bank.name      – Tên ngân hàng (viết tắt cho VietQR, vd: MB, VCB, TCB…)
 *   bank.account   – Số tài khoản
 *   bank.holder    – Tên chủ tài khoản
 *   bank.qr_template – Template URL QR (dùng %s %s %.0f %s %s)
 */
public class SystemConfig {

    private static final String CONFIG_FILE = "system_config.properties";
    private static final Properties props = new Properties();
    private static boolean loaded = false;

    // ── Default values ─────────────────────────────────────────────────────────
    private static final String DEF_RES_NAME    = "NHÀ HÀNG HẬU";
    private static final String DEF_RES_ADDRESS = "12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM";
    private static final String DEF_RES_PHONE   = "0123.456.789";
    private static final String DEF_RES_LOGO    = "";
    private static final String DEF_TAX_VAT     = "10.0";
    private static final String DEF_TAX_SERVICE = "5.0";
    private static final String DEF_BANK_NAME   = "MB";
    private static final String DEF_BANK_ACCT   = "88810102004888";
    private static final String DEF_BANK_HOLDER = "CAO TRONG NGUYEN";
    private static final String DEF_BANK_QR     =
        "https://img.vietqr.io/image/%s-%s-compact.png?amount=%.0f&addInfo=%s&accountName=%s";

    // ── Load ───────────────────────────────────────────────────────────────────
    private static synchronized void ensureLoaded() {
        if (loaded) return;
        File f = new File(CONFIG_FILE);
        if (f.exists()) {
            try (InputStream in = new FileInputStream(f)) {
                props.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.err.println("[SystemConfig] Cannot load config: " + e.getMessage());
            }
        }
        loaded = true;
    }

    // ── Save ───────────────────────────────────────────────────────────────────
    public static synchronized void save() {
        try (OutputStream out = new FileOutputStream(CONFIG_FILE);
             Writer writer = new OutputStreamWriter(out, java.nio.charset.StandardCharsets.UTF_8)) {
            props.store(writer, "System Configuration – Restaurant Management");
            System.out.println("[SystemConfig] Saved to " + new File(CONFIG_FILE).getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[SystemConfig] Cannot save config: " + e.getMessage());
        }
    }

    // ── Generic get/set ────────────────────────────────────────────────────────
    private static String get(String key, String defaultValue) {
        ensureLoaded();
        return props.getProperty(key, defaultValue);
    }

    private static void set(String key, String value) {
        ensureLoaded();
        props.setProperty(key, value);
    }

    // ── Restaurant Info ────────────────────────────────────────────────────────
    public static String getResName()    { return get("res.name",    DEF_RES_NAME); }
    public static String getResAddress() { return get("res.address", DEF_RES_ADDRESS); }
    public static String getResPhone()   { return get("res.phone",   DEF_RES_PHONE); }
    public static String getResLogo()    { return get("res.logo",    DEF_RES_LOGO); }

    public static void setResName(String v)    { set("res.name",    v); }
    public static void setResAddress(String v) { set("res.address", v); }
    public static void setResPhone(String v)   { set("res.phone",   v); }
    public static void setResLogo(String v)    { set("res.logo",    v); }

    // ── Tax / Fee ──────────────────────────────────────────────────────────────
    public static double getVAT() {
        try { return Double.parseDouble(get("tax.vat", DEF_TAX_VAT)); }
        catch (NumberFormatException e) { return 10.0; }
    }

    public static double getServiceFee() {
        try { return Double.parseDouble(get("tax.service", DEF_TAX_SERVICE)); }
        catch (NumberFormatException e) { return 5.0; }
    }

    public static void setVAT(double v)        { set("tax.vat",     String.valueOf(v)); }
    public static void setServiceFee(double v) { set("tax.service", String.valueOf(v)); }

    // ── Bank Info ──────────────────────────────────────────────────────────────
    public static String getBankName()       { return get("bank.name",   DEF_BANK_NAME); }
    public static String getBankAccount()    { return get("bank.account",DEF_BANK_ACCT); }
    public static String getBankHolder()     { return get("bank.holder", DEF_BANK_HOLDER); }
    public static String getBankQrTemplate() { return get("bank.qr_template", DEF_BANK_QR); }

    public static void setBankName(String v)       { set("bank.name",        v); }
    public static void setBankAccount(String v)    { set("bank.account",     v); }
    public static void setBankHolder(String v)     { set("bank.holder",      v); }
    public static void setBankQrTemplate(String v) { set("bank.qr_template", v); }

    /** Tạo URL QR dựa trên config và số tiền cụ thể. */
    public static String buildQrUrl(double amount, String addInfo) {
        String template = getBankQrTemplate();
        return String.format(template,
                getBankName(),
                getBankAccount(),
                amount,
                addInfo.replace(" ", "%20"),
                getBankHolder().replace(" ", "%20"));
    }
}
