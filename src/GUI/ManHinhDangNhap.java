package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import DAO.NhanVienDAO;
import Entity.NhanVien;
import connectDB.ConnectDB;
import connectDB.SessionManager;

public class ManHinhDangNhap extends JFrame {

    private JTextField txtMaNV;
    private JPasswordField txtMatKhau;
    private JButton btnLogin;
    private JButton btnThoat;
    private JCheckBox chkHienMatKhau;

    private NhanVienDAO nvDAO;

    public ManHinhDangNhap() {
        // Connect database trước
        ConnectDB.getInstance().connect();
        nvDAO = new NhanVienDAO();

        initGUI();
    }

    private void initGUI() {
        setTitle("ĐĂNG NHẬP - Hệ Thống Quản Lý Nhà Hàng");
        setSize(850, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Main panel
        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new GridLayout(1, 2)); // Split into 2 columns
        pnlMain.setBackground(Color.WHITE);

        // --- LEFT PANEL (BRANDING) ---
        JPanel pnlLeft = new JPanel();
        pnlLeft.setBackground(new Color(0, 123, 255)); // Brand color
        pnlLeft.setLayout(new GridBagLayout());
        
        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        gbcLeft.insets = new Insets(10, 10, 20, 10);
        
        // Load Logo
        JLabel lblLogo = new JLabel();
        ImageIcon logoIcon = GUI.utils.IconHelper.loadIcon("view/icons/logo.png");
        if (logoIcon != null && logoIcon.getIconWidth() > 0) {
            Image img = logoIcon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(img));
        } else {
            lblLogo.setText("[LOGO HIỂN THỊ Ở ĐÂY]");
            lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblLogo.setForeground(Color.WHITE);
        }
        pnlLeft.add(lblLogo, gbcLeft);

        gbcLeft.gridy = 1;
        gbcLeft.insets = new Insets(0, 10, 5, 10);
        JLabel lblTitle = new JLabel("QUẢN LÝ NHÀ HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);
        pnlLeft.add(lblTitle, gbcLeft);

        gbcLeft.gridy = 2;
        JLabel lblSubtitleLeft = new JLabel("Chuyên nghiệp - Tận tâm");
        lblSubtitleLeft.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSubtitleLeft.setForeground(new Color(230, 230, 230));
        pnlLeft.add(lblSubtitleLeft, gbcLeft);

        pnlMain.add(pnlLeft);

        // --- RIGHT PANEL (FORM) ---
        JPanel pnlRight = new JPanel();
        pnlRight.setBackground(Color.WHITE);
        pnlRight.setLayout(new BorderLayout());

        // Header right
        JPanel pnlFormHeader = new JPanel(new GridBagLayout());
        pnlFormHeader.setBackground(Color.WHITE);
        pnlFormHeader.setBorder(new EmptyBorder(60, 0, 20, 0));
        
        GridBagConstraints gbcH = new GridBagConstraints();
        gbcH.gridx = 0; 
        gbcH.gridy = 0;
        JLabel lblLoginTitle = new JLabel("ĐĂNG NHẬP");
        lblLoginTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblLoginTitle.setForeground(new Color(33, 37, 41));
        pnlFormHeader.add(lblLoginTitle, gbcH);

        gbcH.gridy = 1;
        gbcH.insets = new Insets(5, 0, 0, 0);
        JLabel lblSubtitle = new JLabel("Vui lòng đăng nhập để tiếp tục");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(108, 117, 125));
        pnlFormHeader.add(lblSubtitle, gbcH);

        pnlRight.add(pnlFormHeader, BorderLayout.NORTH);

        // Form fields
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(0, 50, 20, 50));
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 0, 8, 0);
        c.weightx = 1.0;

        // Mã NV
        JLabel lblMaNV = new JLabel("Mã Tài Khoản / Số Điện Thoại:");
        lblMaNV.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMaNV.setForeground(new Color(73, 80, 87));
        c.gridx = 0;
        c.gridy = 0;
        pnlForm.add(lblMaNV, c);

        txtMaNV = new JTextField();
        GUI.utils.UIStyle.styleTextField(txtMaNV);
        txtMaNV.setPreferredSize(new Dimension(0, 44));
        c.gridy = 1;
        c.insets = new Insets(0, 0, 15, 0);
        pnlForm.add(txtMaNV, c);

        // Mật khẩu
        JLabel lblMatKhau = new JLabel("Mật Khẩu:");
        lblMatKhau.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMatKhau.setForeground(new Color(73, 80, 87));
        c.gridy = 2;
        c.insets = new Insets(5, 0, 8, 0);
        pnlForm.add(lblMatKhau, c);

        txtMatKhau = new JPasswordField();
        GUI.utils.UIStyle.stylePasswordField(txtMatKhau);
        txtMatKhau.setPreferredSize(new Dimension(0, 44));
        c.gridy = 3;
        c.insets = new Insets(0, 0, 5, 0);
        pnlForm.add(txtMatKhau, c);

        // Checkbox hiện mật khẩu
        chkHienMatKhau = new JCheckBox("Hiện mật khẩu");
        chkHienMatKhau.setBackground(Color.WHITE);
        chkHienMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkHienMatKhau.setForeground(new Color(108, 117, 125));
        chkHienMatKhau.addActionListener(e -> {
            if (chkHienMatKhau.isSelected()) {
                txtMatKhau.setEchoChar((char) 0);
            } else {
                txtMatKhau.setEchoChar('●');
            }
        });
        c.gridy = 4;
        c.insets = new Insets(0, 0, 25, 0);
        pnlForm.add(chkHienMatKhau, c);

        // Button Login
        btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setBackground(new Color(0, 123, 255));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(0, 48));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        c.gridy = 5;
        c.insets = new Insets(0, 0, 10, 0);
        pnlForm.add(btnLogin, c);

        // Button Thoát
        btnThoat = new JButton("THOÁT");
        btnThoat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnThoat.setBackground(new Color(241, 243, 245));
        btnThoat.setForeground(new Color(73, 80, 87));
        btnThoat.setPreferredSize(new Dimension(0, 44));
        btnThoat.setFocusPainted(false);
        btnThoat.setBorderPainted(false);
        btnThoat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        c.gridy = 6;
        c.insets = new Insets(0, 0, 0, 0);
        pnlForm.add(btnThoat, c);

        pnlRight.add(pnlForm, BorderLayout.CENTER);

        // Right panel Footer
        JPanel pnlFooterRight = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlFooterRight.setBackground(Color.WHITE);
        pnlFooterRight.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel lblFooter = new JLabel("© 2026 Restaurant Management System");
        lblFooter.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblFooter.setForeground(new Color(173, 181, 189));
        pnlFooterRight.add(lblFooter);
        pnlRight.add(pnlFooterRight, BorderLayout.SOUTH);

        pnlMain.add(pnlRight);

        add(pnlMain);

        // --- EVENTS ---
        btnLogin.addActionListener(e -> handleLogin());
        btnThoat.addActionListener(e -> System.exit(0));

        // Enter key để login
        txtMatKhau.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });

        // Focus mặc định vào txtMaNV
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                txtMaNV.requestFocus();
            }
        });
    }

    private void handleLogin() {
        String maNV = txtMaNV.getText().trim();
        String matKhau = new String(txtMatKhau.getPassword());

        // Validation
        if (maNV.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã nhân viên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtMaNV.requestFocus();
            return;
        }

        if (matKhau.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtMatKhau.requestFocus();
            return;
        }

        // Thử login
        btnLogin.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");

        // Simulate loading (optional)
        Timer timer = new Timer(500, e -> {
            NhanVien nv = nvDAO.login(maNV, matKhau);

            if (nv != null) {
                // Login thành công
                SessionManager.login(nv);

                JOptionPane.showMessageDialog(this,
                        "Đăng nhập thành công!\nChào mừng " + nv.getTenNV(),
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

                // Mở TrangChu
                this.dispose();
                new MainLayout().setVisible(true);

            } else {
                // Login thất bại
                JOptionPane.showMessageDialog(this,
                        "Sai mã nhân viên hoặc mật khẩu!",
                        "Lỗi đăng nhập",
                        JOptionPane.ERROR_MESSAGE);

                btnLogin.setEnabled(true);
                btnLogin.setText("ĐĂNG NHẬP");
                txtMatKhau.setText("");
                txtMaNV.requestFocus();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    // Test main
    public static void main(String[] args) {
        try {
            Class.forName("com.formdev.flatlaf.FlatLightLaf").getMethod("setup").invoke(null);
            // ── same FlatLaf properties as TrangChu.main() ──
            javax.swing.UIManager.put("Button.arc",          10);
            javax.swing.UIManager.put("Component.arc",       8);
            javax.swing.UIManager.put("TextComponent.arc",   8);
            javax.swing.UIManager.put("CheckBox.arc",        6);
            javax.swing.UIManager.put("Button.margin",       new java.awt.Insets(7, 16, 7, 16));
            javax.swing.UIManager.put("TextField.margin",    new java.awt.Insets(6, 10, 6, 10));
            javax.swing.UIManager.put("PasswordField.margin",new java.awt.Insets(6, 10, 6, 10));
            javax.swing.UIManager.put("ScrollBar.thumbArc",  999);
            javax.swing.UIManager.put("ScrollBar.thumbInsets",new java.awt.Insets(2, 2, 2, 2));
            javax.swing.UIManager.put("ScrollBar.width",     8);
            javax.swing.UIManager.put("Component.focusWidth",1);
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }
        SwingUtilities.invokeLater(() -> {
            new ManHinhDangNhap().setVisible(true);
        });
    }
}
