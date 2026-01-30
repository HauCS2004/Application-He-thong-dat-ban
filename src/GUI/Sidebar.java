package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import connectDB.SessionManager;

/**
 * Sidebar Navigation - Menu dọc hiện đại với role-based access
 */
public class Sidebar extends JPanel {

    private Color colorBg = new Color(44, 62, 80); // Dark blue-gray
    private Color colorHover = new Color(52, 73, 94); // Lighter blue-gray
    private Color colorActive = new Color(52, 152, 219); // Blue
    private Color colorText = new Color(236, 240, 241); // Light gray
    private Color colorTextActive = Color.WHITE;

    private JPanel pnlMenuItems;
    private String currentCard = "home";
    private CardLayout parentCardLayout;
    private JPanel parentContent;

    public Sidebar(CardLayout cardLayout, JPanel content) {
        this.parentCardLayout = cardLayout;
        this.parentContent = content;

        initGUI();
    }

    private void initGUI() {
        setLayout(new BorderLayout());
        setBackground(colorBg);
        setPreferredSize(new Dimension(220, 0));

        // Header
        JPanel pnlHeader = new JPanel();
        pnlHeader.setBackground(colorBg);
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel lblTitle = new JLabel("QUẢN LÝ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(colorTextActive);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("NHÀ HÀNG");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSubtitle.setForeground(colorText);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlHeader.add(lblTitle);
        pnlHeader.add(Box.createVerticalStrut(5));
        pnlHeader.add(lblSubtitle);

        add(pnlHeader, BorderLayout.NORTH);

        // Menu Items
        pnlMenuItems = new JPanel();
        pnlMenuItems.setLayout(new BoxLayout(pnlMenuItems, BoxLayout.Y_AXIS));
        pnlMenuItems.setBackground(colorBg);
        pnlMenuItems.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Build menu dựa trên role
        buildMenu();

        JScrollPane scroll = new JScrollPane(pnlMenuItems);
        scroll.setBorder(null);
        scroll.setBackground(colorBg);
        scroll.getViewport().setBackground(colorBg);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);

        // Footer - User info
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(new Color(34, 49, 63));
        pnlFooter.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblUser = new JLabel("▸ " + SessionManager.getDisplayName());
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUser.setForeground(colorText);

        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnLogout.setForeground(new Color(231, 76, 60));
        btnLogout.setBackground(colorBg);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Bạn muốn đăng xuất?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                SessionManager.logout();

                // Close TrangChu và mở login
                SwingUtilities.getWindowAncestor(this).dispose();
                new ManHinhDangNhap().setVisible(true);
            }
        });

        pnlFooter.add(lblUser, BorderLayout.CENTER);
        pnlFooter.add(btnLogout, BorderLayout.SOUTH);

        add(pnlFooter, BorderLayout.SOUTH);
    }

    private void buildMenu() {
        boolean isManager = SessionManager.isManager();

        // 1. NHÓM VẬN HÀNH (Chung)
        addSeparator("VẬN HÀNH");
        addMenuItem("home.png", "Trang Chủ", "home");
        addMenuItem("serving.png", "Phục Vụ", "phuc_vu"); // Most used -> High priority
        addMenuItem("booking.png", "Quản Lý Đặt Bàn", "dat_ban");
        addMenuItem("payment.png", "Quản Lý Hóa Đơn", "hoa_don"); // Renamed per user request
        addMenuItem("customer.png", "Khách Hàng", "khach_hang"); // Moved to Operations

        // 2. NHÓM QUẢN TRỊ (Manager Only)
        if (isManager) {
            addSeparator("QUẢN TRỊ");
            addMenuItem("table.png", "Phòng Bàn", "quan_ly_ban_visual");
            addMenuItem("menu.png", "Thực Đơn", "mon_an");

            // 3. NHÓM BÁO CÁO
            addSeparator("BÁO CÁO");
            addMenuItem("report.png", "Thống Kê", "thong_ke");
        }
    }

    private void addMenuItem(String iconFileName, String text, String cardName) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(colorBg);
        item.setBorder(new EmptyBorder(12, 15, 12, 15));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Load icon từ file
        JLabel lblIcon = new JLabel();
        // Load icon using helper
        ImageIcon icon = GUI.utils.IconHelper.loadIcon("view/icons/" + iconFileName);

        if (icon != null && icon.getIconWidth() > 0) {
            Image img = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            lblIcon.setIcon(new ImageIcon(img));
        } else {
            lblIcon.setText("");
        }
        lblIcon.setForeground(colorText);

        JLabel lblText = new JLabel(text);
        lblText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblText.setForeground(colorText);

        item.add(lblIcon, BorderLayout.WEST);
        item.add(lblText, BorderLayout.CENTER);

        // Hover effect
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!cardName.equals(currentCard)) {
                    item.setBackground(colorHover);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!cardName.equals(currentCard)) {
                    item.setBackground(colorBg);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                setActive(item, cardName);
                parentCardLayout.show(parentContent, cardName);
            }
        });

        // Mặc định active item đầu tiên
        if (cardName.equals("home")) {
            item.setBackground(colorActive);
            lblText.setForeground(colorTextActive);
        }

        pnlMenuItems.add(item);
    }

    private void addSeparator(String label) {
        JPanel separator = new JPanel(new BorderLayout());
        separator.setBackground(colorBg);
        separator.setBorder(new EmptyBorder(15, 15, 8, 15));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lblSeparator = new JLabel(label);
        lblSeparator.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSeparator.setForeground(new Color(149, 165, 166));

        separator.add(lblSeparator, BorderLayout.WEST);
        pnlMenuItems.add(separator);
    }

    private void setActive(JPanel activeItem, String cardName) {
        currentCard = cardName;

        // Reset tất cả items
        for (Component comp : pnlMenuItems.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;

                // Skip separator panels
                if (panel.getComponentCount() >= 2) {
                    Component centerComp = ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.CENTER);

                    if (centerComp instanceof JLabel) {
                        panel.setBackground(colorBg);
                        ((JLabel) centerComp).setForeground(colorText);
                    }
                }
            }
        }

        // Set active
        activeItem.setBackground(colorActive);

        Component centerComp = ((BorderLayout) activeItem.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (centerComp instanceof JLabel) {
            ((JLabel) centerComp).setForeground(colorTextActive);
        }
    }
}
