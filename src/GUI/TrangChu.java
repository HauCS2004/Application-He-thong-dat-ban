package GUI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import connectDB.ConnectDB;

public class TrangChu extends JFrame {

    private JPanel pnlContent;
    private CardLayout cardLayout;

    // Khai báo các màn hình con
    private ManHinhTrangChu pnlTrangChu;
    private ManHinhDatBanV2 pnlDatBan; // NEW: Redesigned booking screen
    private QuanLyMonAn pnlQuanLyMonAn;
    private QuanLyBan pnlBan;
    private ManHinhHoaDon pnlHoaDon;
    private QuanLyKhachHang pnlKhachHang;
    private ThongKeDoanhThu pnlThongKe;
    private QuanLyBanVisual pnlQuanLyBanVisual;

    public TrangChu() {
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
        pnlQuanLyMonAn = new QuanLyMonAn();
        pnlBan = new QuanLyBan();
        pnlHoaDon = new ManHinhHoaDon();
        pnlKhachHang = new QuanLyKhachHang();
        pnlThongKe = new ThongKeDoanhThu();
        pnlQuanLyBanVisual = new QuanLyBanVisual(); // NEW: Visual table system

        // Add vào CardLayout
        pnlContent.add(pnlTrangChu, "home");
        pnlContent.add(pnlDatBan, "dat_ban");
        pnlContent.add(pnlBan, "ban");
        // pnlContent.add(pnlHoaDon, "hoa_don"); // TODO
        pnlContent.add(pnlKhachHang, "khach_hang");

        // Menu chỉ dành cho quản lý (card names match sidebar)
        pnlContent.add(pnlBan, "quan_ly_ban"); // Alias cho quản lý bàn
        pnlContent.add(pnlQuanLyBanVisual, "quan_ly_ban_visual"); // NEW: Visual table system
        pnlContent.add(pnlQuanLyMonAn, "mon_an");
        // pnlContent.add(pnlThongKe, "thong_ke"); // TODO

        // Sidebar
        Sidebar sidebar = new Sidebar(cardLayout, pnlContent);
        add(sidebar, BorderLayout.WEST);

        add(pnlContent, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        // ✅ PHASE 1: Require login trước
        SwingUtilities.invokeLater(() -> {
            new ManHinhDangNhap().setVisible(true);
        });
    }
}