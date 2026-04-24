package GUI.components;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import connectDB.SessionManager;
import GUI.ManHinhDangNhap;

public class GlobalHeader extends JPanel {

    private static final Color HEADER_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(17, 24, 39);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color HOVER_BG = new Color(243, 244, 246);
    private static final Color AVATAR_BG = new Color(99, 102, 241);

    private Timer clockTimer;
    private JLabel lblClock;
    private JLabel lblDate;
    private JFrame parentFrame;

    public GlobalHeader(JFrame parent) {
        this.parentFrame = parent;
        initGUI();
        startClock();
    }

    private void initGUI() {
        setLayout(new BorderLayout(20, 0));
        setBackground(HEADER_BG);
        setPreferredSize(new Dimension(0, 64)); // Standard modern header height
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(8, 24, 8, 24)
        ));

        // ─── LEFT: Branch / System Info ────────────────────────
        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        pnlLeft.setOpaque(false);
        
        JLabel lblSystemName = new JLabel("Hệ Thống Quản Lý Đặt Bàn");
        lblSystemName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSystemName.setForeground(TEXT_DARK);
        
        JLabel lblBranch = new JLabel("📍 Chi nhánh Trung Tâm");
        lblBranch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblBranch.setForeground(TEXT_MUTED);
        
        pnlLeft.add(lblSystemName);
        pnlLeft.add(lblBranch);
        
        // ─── RIGHT: Clock, Notifications & User Info ───────────
        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        pnlRight.setOpaque(false);

        // DateTime
        JPanel pnlDateTime = new JPanel();
        pnlDateTime.setLayout(new BoxLayout(pnlDateTime, BoxLayout.Y_AXIS));
        pnlDateTime.setOpaque(false);
        
        lblClock = new JLabel();
        lblClock.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblClock.setForeground(TEXT_DARK);
        lblClock.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        lblDate = new JLabel();
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDate.setForeground(TEXT_MUTED);
        lblDate.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        pnlDateTime.add(lblClock);
        pnlDateTime.add(lblDate);
        
        // Notification Icon
        JButton btnNotif = createIconButton("🔔");

        // User Profile
        JPanel pnlUser = createUserProfilePanel();

        pnlRight.add(pnlDateTime);
        pnlRight.add(new JSeparator(SwingConstants.VERTICAL)); // Needs preferred height to look good
        pnlRight.add(btnNotif);
        pnlRight.add(pnlUser);

        // Fixing separator
        Component[] comps = pnlRight.getComponents();
        for(Component c : comps) {
            if(c instanceof JSeparator) {
                c.setPreferredSize(new Dimension(1, 40));
                c.setForeground(BORDER_COLOR);
            }
        }

        add(pnlLeft, BorderLayout.WEST);
        add(pnlRight, BorderLayout.EAST);
    }

    private JPanel createUserProfilePanel() {
        JPanel pnlUser = new JPanel(new BorderLayout(10, 0));
        pnlUser.setOpaque(false);
        pnlUser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pnlUser.setBorder(new EmptyBorder(4, 8, 4, 8));

        String displayName = "Guest";
        String role = "Nhân viên";
        try {
            if (SessionManager.getCurrentUser() != null) {
                displayName = SessionManager.getCurrentUser().getTenNV();
                if (SessionManager.getCurrentUser().getTaiKhoan() != null) {
                    role = SessionManager.getCurrentUser().getTaiKhoan().getVaiTro();
                }
            }
        } catch (Exception e) {}

        String shortName = displayName.length() > 15 ? displayName.substring(0, 14) + "…" : displayName;
        String initial = displayName.length() > 0 ? displayName.substring(0, 1).toUpperCase() : "?";
        final String initLetter = initial;

        // Avatar
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AVATAR_BG);
                g2.fillOval(0, 0, getWidth(), getHeight());
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(initLetter)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initLetter, x, y);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(36, 36));

        // Text
        JPanel pnlText = new JPanel();
        pnlText.setLayout(new BoxLayout(pnlText, BoxLayout.Y_AXIS));
        pnlText.setOpaque(false);

        JLabel lblName = new JLabel(shortName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(TEXT_DARK);

        JLabel lblRole = new JLabel(role);
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRole.setForeground(TEXT_MUTED);

        pnlText.add(lblName);
        pnlText.add(lblRole);

        // Dropdown icon
        JLabel lblDrop = new JLabel("▼");
        lblDrop.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblDrop.setForeground(TEXT_MUTED);

        pnlUser.add(avatar, BorderLayout.WEST);
        pnlUser.add(pnlText, BorderLayout.CENTER);
        pnlUser.add(lblDrop, BorderLayout.EAST);

        // Hover effect
        pnlUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                pnlUser.setOpaque(true);
                pnlUser.setBackground(HOVER_BG);
                pnlUser.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                pnlUser.setOpaque(false);
                pnlUser.repaint();
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                // Show Popup menu on left or right click
                showUserMenu(pnlUser, e.getX(), e.getY() + 10);
            }
        });

        return pnlUser;
    }

    private JButton createIconButton(String emoji) {
        JButton btn = new JButton(emoji);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btn.setForeground(TEXT_DARK);
        btn.setBackground(HEADER_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(40, 40));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setContentAreaFilled(true);
                btn.setBackground(HOVER_BG);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setContentAreaFilled(false);
                btn.setBackground(HEADER_BG);
            }
        });
        return btn;
    }

    private void showUserMenu(Component invoker, int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(Color.WHITE);
        popup.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        
        JMenuItem mnuInfo = new JMenuItem("👤 Thông tin tài khoản");
        mnuInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mnuInfo.setBackground(Color.WHITE);
        mnuInfo.setBorder(new EmptyBorder(8, 12, 8, 12));
        mnuInfo.addActionListener(e -> {
            JOptionPane.showMessageDialog(parentFrame, 
                "Chức năng xem thông tin tài khoản đang phát triển.", 
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });

        JMenuItem mnuSettings = new JMenuItem("⚙️ Cài đặt");
        mnuSettings.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mnuSettings.setBackground(Color.WHITE);
        mnuSettings.setBorder(new EmptyBorder(8, 12, 8, 12));

        JMenuItem mnuLogout = new JMenuItem("🚪 Đăng xuất");
        mnuLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mnuLogout.setForeground(new Color(239, 68, 68));
        mnuLogout.setBackground(Color.WHITE);
        mnuLogout.setBorder(new EmptyBorder(8, 12, 8, 12));
        mnuLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(parentFrame,
                    "Bạn muốn đăng xuất khỏi hệ thống?",
                    "Xác nhận đăng xuất",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                SessionManager.logout();
                parentFrame.dispose();
                new ManHinhDangNhap().setVisible(true);
            }
        });

        popup.add(mnuInfo);
        popup.add(mnuSettings);
        popup.addSeparator();
        popup.add(mnuLogout);

        popup.show(invoker, x, y);
    }

    private void startClock() {
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
        
        clockTimer = new Timer(1000, e -> {
            Date now = new Date();
            lblClock.setText(sdfTime.format(now));
            lblDate.setText(sdfDate.format(now));
        });
        clockTimer.start();
        
        Date now = new Date();
        lblClock.setText(sdfTime.format(now));
        lblDate.setText(sdfDate.format(now));
    }
}
