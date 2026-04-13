package GUI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import connectDB.ConnectDB;

public class MainLayout extends JFrame {

    private JPanel pnlContent;
    private CardLayout cardLayout;

    // Khai báo các màn hình con
    private ManHinhTrangChu pnlTrangChu;
    private ManHinhDatBanV2 pnlDatBan; // NEW: Redesigned booking screen
    private ManHinhMonAn pnlQuanLyMonAn;
    private ManHinhHoaDon pnlHoaDon;
    private ManHinhKhachHang pnlKhachHang;
    private ManHinhThongKe pnlThongKe;
    private ManHinhQLBanVisual pnlQuanLyBanVisual;
    private ManHinhPhucVu pnlPhucVu; // NEW
    private ManHinhNhanVien pnlNhanVien; // NEW: Employee Management
    private ManHinhTaiKhoan pnlTaiKhoan;

    public MainLayout() {
        ConnectDB.getInstance().connect();
        initGUI();
    }

    private void initGUI() {
        setTitle("QUẢN LÝ NHÀ HÀNG - " + connectDB.SessionManager.getDisplayName());
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // ✅ PHASE 3: SIDEBAR NAVIGATION
        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);
        pnlContent.setBackground(new Color(245, 245, 250));

        // >> Khởi tạo các Panel nội dung <<
        pnlTrangChu = new ManHinhTrangChu();
        pnlDatBan = new ManHinhDatBanV2(); // NEW: Redesigned
        pnlQuanLyMonAn = new ManHinhMonAn();
        pnlHoaDon = new ManHinhHoaDon();
        pnlKhachHang = new ManHinhKhachHang();
        pnlThongKe = new ManHinhThongKe();
        pnlQuanLyBanVisual = new ManHinhQLBanVisual(); // NEW: Visual table system
        pnlPhucVu = new ManHinhPhucVu(); // NEW: Service Screen
        pnlNhanVien = new ManHinhNhanVien(); // NEW: Employee Screen
        pnlTaiKhoan = new ManHinhTaiKhoan();

        // Add vào CardLayout
        pnlContent.add(pnlTrangChu, "home");
        pnlContent.add(pnlDatBan, "dat_ban");
        pnlContent.add(pnlPhucVu, "phuc_vu"); // NEW
        pnlContent.add(pnlHoaDon, "hoa_don");
        pnlContent.add(pnlKhachHang, "khach_hang");

        // Menu chỉ dành cho quản lý (card names match sidebar)
        pnlContent.add(pnlQuanLyBanVisual, "quan_ly_ban_visual"); // Giao diện quản lý bàn (Visual)
        pnlContent.add(pnlQuanLyMonAn, "mon_an");
        pnlContent.add(new ManHinhKhuyenMai(), "khuyen_mai"); // NEW: Promotion Screen
        pnlContent.add(pnlNhanVien, "nhan_vien");
        pnlContent.add(pnlTaiKhoan, "tai_khoan");
        pnlContent.add(pnlThongKe, "thong_ke");

        // Sidebar
        Sidebar sidebar = new Sidebar(cardLayout, pnlContent);
        add(sidebar, BorderLayout.WEST);

        add(pnlContent, BorderLayout.CENTER);
    }

    // --- NAVIGATION HELPERS ---
    public void showScreen(String screenName) {
        cardLayout.show(pnlContent, screenName);
    }

    public ManHinhHoaDon getPnlHoaDon() {
        return pnlHoaDon;
    }

    public ManHinhPhucVu getPnlPhucVu() {
        return pnlPhucVu;
    }

    public static void main(String[] args) {
        // ── KẾT HỢP CUỐI CÚNG: KHÓA TỈ LỆ VỀ CHUẨN 1.15X CỦA MÁY BẠN ──
        // Do máy bạn đang chạy tốt nhất ở 115% (1.15), nếu để các máy 100% 
        // tự chạy scale của họ thì Java sẽ tính toán padding bị lệch.
        // Giải pháp hoàn hảo nhất: Bắt TẤT CẢ các máy khi mở phần mềm này
        // đều phải dựng giao diện ở tỉ lệ 1.15 (115%) bất kể Windows của họ là bao nhiêu!
        // Như vậy, máy bạn sẽ không bị nhỏ, máy bạn của bạn sẽ không bị to.
        
        System.setProperty("sun.java2d.uiScale", "1.15");

        // Đồng thời bật tính năng cho FlatLaf tự canh chỉnh Font theo scale mới
        System.setProperty("flatlaf.uiScale", "1.15");

        // Setup FlatLaf theme
        try {
            Class.forName("com.formdev.flatlaf.FlatLightLaf").getMethod("setup").invoke(null);

            // ── Rounded corners (bo góc) ──────────────────────────────────────
            javax.swing.UIManager.put("Button.arc",          10);
            javax.swing.UIManager.put("Component.arc",       8);
            javax.swing.UIManager.put("TextComponent.arc",   8);  // Input fields
            javax.swing.UIManager.put("CheckBox.arc",        6);
            javax.swing.UIManager.put("ProgressBar.arc",     8);

            // ── Button padding ────────────────────────────────────────────────
            javax.swing.UIManager.put("Button.margin",
                new java.awt.Insets(7, 16, 7, 16));

            // ── TextField / ComboBox padding ──────────────────────────────────
            javax.swing.UIManager.put("TextField.margin",
                new java.awt.Insets(6, 10, 6, 10));
            javax.swing.UIManager.put("PasswordField.margin",
                new java.awt.Insets(6, 10, 6, 10));
            javax.swing.UIManager.put("ComboBox.padding",
                new java.awt.Insets(4, 8, 4, 8));

            // ── Scrollbar hiện đại (pill shape) ────────────────────────────────
            javax.swing.UIManager.put("ScrollBar.thumbArc",    999);
            javax.swing.UIManager.put("ScrollBar.thumbInsets",
                new java.awt.Insets(2, 2, 2, 2));
            javax.swing.UIManager.put("ScrollBar.width",        8);
            javax.swing.UIManager.put("ScrollBar.track",       new java.awt.Color(0, 0, 0, 0));

            // ── Table styling ─────────────────────────────────────────────────
            javax.swing.UIManager.put("Table.rowHeight",        40);
            javax.swing.UIManager.put("Table.showHorizontalLines", false);
            javax.swing.UIManager.put("Table.showVerticalLines",   false);
            javax.swing.UIManager.put("Table.intercellSpacing",
                new java.awt.Dimension(0, 0));
            javax.swing.UIManager.put("Table.selectionBackground",
                new java.awt.Color(239, 246, 255));
            javax.swing.UIManager.put("Table.selectionForeground",
                new java.awt.Color(17, 24, 39));

            // ── Focus ring đẹp hơn ───────────────────────────────────────────
            javax.swing.UIManager.put("Component.focusWidth",  1);
            javax.swing.UIManager.put("Component.innerFocusWidth", 0);

            // ── TabbedPane ────────────────────────────────────────────────────
            javax.swing.UIManager.put("TabbedPane.tabHeight",   38);

        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf: " + ex.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            new ManHinhDangNhap().setVisible(true);
        });
    }
}