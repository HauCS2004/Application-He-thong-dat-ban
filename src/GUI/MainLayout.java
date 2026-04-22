package GUI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import connectDB.ConnectDB;

public class MainLayout extends JFrame {

    private JPanel pnlContent;
    private CardLayout cardLayout;

    // Khai báo các màn hình con
    private ManHinhTrangChu pnlTrangChu;
    private ManHinhDatBanV2 pnlDatBan;
    private ManHinhMonAn pnlQuanLyMonAn;
    private ManHinhHoaDon pnlHoaDon;
    private ManHinhKhachHang pnlKhachHang;
    private ManHinhThongKe pnlThongKe;
    private ManHinhQLBanVisual pnlQuanLyBanVisual;
    private ManHinhPhucVu pnlPhucVu;
    private ManHinhNhanVien pnlNhanVien;
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

        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);
        pnlContent.setBackground(new Color(245, 245, 250));

        pnlTrangChu = new ManHinhTrangChu();
        pnlDatBan = new ManHinhDatBanV2();
        pnlQuanLyMonAn = new ManHinhMonAn();
        pnlHoaDon = new ManHinhHoaDon();
        pnlKhachHang = new ManHinhKhachHang();
        pnlThongKe = new ManHinhThongKe();
        pnlQuanLyBanVisual = new ManHinhQLBanVisual();
        pnlPhucVu = new ManHinhPhucVu();
        pnlNhanVien = new ManHinhNhanVien();
        pnlTaiKhoan = new ManHinhTaiKhoan();

        pnlContent.add(pnlTrangChu, "home");
        pnlContent.add(pnlDatBan, "dat_ban");
        pnlContent.add(pnlPhucVu, "phuc_vu");
        pnlContent.add(pnlHoaDon, "hoa_don");
        pnlContent.add(pnlKhachHang, "khach_hang");
        pnlContent.add(pnlQuanLyBanVisual, "quan_ly_ban_visual");
        pnlContent.add(pnlQuanLyMonAn, "mon_an");
        pnlContent.add(new ManHinhKhuyenMai(), "khuyen_mai");
        pnlContent.add(pnlNhanVien, "nhan_vien");
        pnlContent.add(pnlTaiKhoan, "tai_khoan");
        pnlContent.add(pnlThongKe, "thong_ke");

        Sidebar sidebar = new Sidebar(cardLayout, pnlContent);
        add(sidebar, BorderLayout.WEST);
        add(pnlContent, BorderLayout.CENTER);
        add(createTopBar(), BorderLayout.NORTH);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
                new EmptyBorder(6, 20, 6, 20)));
        bar.setPreferredSize(new Dimension(0, 52));

        // ── LEFT: Tên nhà hàng ──────────────────────────────────────────────
        JLabel lblSystem = new JLabel("Nhà Hàng Hậu");
        lblSystem.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblSystem.setForeground(new Color(31, 41, 55));
        bar.add(lblSystem, BorderLayout.WEST);

        // ── RIGHT: [🔔] [⚙] [Giờ / Ngày] — thứ tự trái → phải ─────────────
        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRight.setOpaque(false);

        // 1. Chuông thông báo
        JButton btnBell = pnlDatBan.getNotifyButton();
        pnlRight.add(btnBell);

        // 2. Nút bánh răng cài đặt
        ImageIcon settingsIcon = GUI.utils.IconHelper.loadIcon("view/icons/settings.png");
        JButton btnSettings = new JButton(settingsIcon);
        if (settingsIcon == null) {
            btnSettings.setText("\u2699");
            btnSettings.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        }
        btnSettings.setBackground(Color.WHITE);
        btnSettings.setFocusPainted(false);
        btnSettings.setBorder(null);
        btnSettings.setPreferredSize(new Dimension(44, 36));
        btnSettings.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSettings.setToolTipText("Cài đặt hệ thống");
        btnSettings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnSettings.setBackground(new Color(243, 244, 246));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnSettings.setBackground(Color.WHITE);
            }
        });
        btnSettings.addActionListener(e -> showSystemSettingsDialog());
        pnlRight.add(btnSettings);

        // 3. Đồng hồ 2 hàng: Giờ (trên) / Ngày tháng năm (dưới)
        JLabel lblTime = new JLabel("--:--:--");
        lblTime.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTime.setForeground(new Color(31, 41, 55));
        lblTime.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel lblDate = new JLabel("--/--/----");
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDate.setForeground(new Color(107, 114, 128));
        lblDate.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel pnlClock = new JPanel(new GridLayout(2, 1, 0, 1));
        pnlClock.setOpaque(false);
        pnlClock.setPreferredSize(new Dimension(88, 40));
        pnlClock.add(lblTime);
        pnlClock.add(lblDate);

        Timer clockTimer = new Timer(true);
        clockTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Date now = new Date();
                String t = new SimpleDateFormat("HH:mm:ss").format(now);
                String d = new SimpleDateFormat("dd/MM/yyyy").format(now);
                SwingUtilities.invokeLater(() -> {
                    lblTime.setText(t);
                    lblDate.setText(d);
                });
            }
        }, 0, 1000);

        pnlRight.add(pnlClock);
        bar.add(pnlRight, BorderLayout.EAST);
        return bar;
    }

    private void showSystemSettingsDialog() {
        new ManHinhCaiDat(this).setVisible(true);
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
        try {
            Class.forName("com.formdev.flatlaf.FlatLightLaf").getMethod("setup").invoke(null);

            javax.swing.UIManager.put("Button.arc", 10);
            javax.swing.UIManager.put("Component.arc", 8);
            javax.swing.UIManager.put("TextComponent.arc", 8);
            javax.swing.UIManager.put("CheckBox.arc", 6);
            javax.swing.UIManager.put("ProgressBar.arc", 8);

            javax.swing.UIManager.put("Button.margin",
                    new java.awt.Insets(7, 16, 7, 16));

            javax.swing.UIManager.put("TextField.margin",
                    new java.awt.Insets(6, 10, 6, 10));
            javax.swing.UIManager.put("PasswordField.margin",
                    new java.awt.Insets(6, 10, 6, 10));
            javax.swing.UIManager.put("ComboBox.padding",
                    new java.awt.Insets(4, 8, 4, 8));

            javax.swing.UIManager.put("ScrollBar.thumbArc", 999);
            javax.swing.UIManager.put("ScrollBar.thumbInsets",
                    new java.awt.Insets(2, 2, 2, 2));
            javax.swing.UIManager.put("ScrollBar.width", 8);
            javax.swing.UIManager.put("ScrollBar.track", new java.awt.Color(0, 0, 0, 0));

            javax.swing.UIManager.put("Table.rowHeight", 40);
            javax.swing.UIManager.put("Table.showHorizontalLines", false);
            javax.swing.UIManager.put("Table.showVerticalLines", false);
            javax.swing.UIManager.put("Table.intercellSpacing",
                    new java.awt.Dimension(0, 0));
            javax.swing.UIManager.put("Table.selectionBackground",
                    new java.awt.Color(239, 246, 255));
            javax.swing.UIManager.put("Table.selectionForeground",
                    new java.awt.Color(17, 24, 39));

            javax.swing.UIManager.put("Component.focusWidth", 1);
            javax.swing.UIManager.put("Component.innerFocusWidth", 0);

            javax.swing.UIManager.put("TabbedPane.tabHeight", 38);

        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf: " + ex.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            new ManHinhDangNhap().setVisible(true);
        });
    }
}
