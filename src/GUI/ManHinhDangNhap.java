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
        setSize(450, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Main panel
        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BorderLayout());
        pnlMain.setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel pnlHeader = new JPanel();
        pnlHeader.setBackground(new Color(0, 123, 255));
        pnlHeader.setPreferredSize(new Dimension(0, 150));
        pnlHeader.setLayout(new GridBagLayout());

        JLabel lblTitle = new JLabel("QUẢN LÝ NHÀ HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Đăng nhập để tiếp tục");
        lblSubtitle.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSubtitle.setForeground(new Color(230, 230, 230));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 0);
        pnlHeader.add(lblTitle, gbc);

        gbc.gridy = 1;
        pnlHeader.add(lblSubtitle, gbc);

        pnlMain.add(pnlHeader, BorderLayout.NORTH);

        // --- FORM LOGIN ---
        JPanel pnlForm = new JPanel();
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(40, 50, 40, 50));
        pnlForm.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 0, 10, 0);

        // Mã NV
        JLabel lblMaNV = new JLabel("Mã Nhân Viên:");
        lblMaNV.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        pnlForm.add(lblMaNV, c);

        txtMaNV = new JTextField();
        GUI.utils.UIStyle.styleTextField(txtMaNV);
        txtMaNV.setPreferredSize(new Dimension(0, 44));
        c.gridy = 1;
        pnlForm.add(txtMaNV, c);

        // Mật khẩu
        JLabel lblMatKhau = new JLabel("Mật Khẩu:");
        lblMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.gridy = 2;
        pnlForm.add(lblMatKhau, c);

        txtMatKhau = new JPasswordField();
        GUI.utils.UIStyle.stylePasswordField(txtMatKhau);
        txtMatKhau.setPreferredSize(new Dimension(0, 44));
        c.gridy = 3;
        pnlForm.add(txtMatKhau, c);

        // Checkbox hiện mật khẩu
        chkHienMatKhau = new JCheckBox("Hiện mật khẩu");
        chkHienMatKhau.setBackground(Color.WHITE);
        chkHienMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkHienMatKhau.addActionListener(e -> {
            if (chkHienMatKhau.isSelected()) {
                txtMatKhau.setEchoChar((char) 0);
            } else {
                txtMatKhau.setEchoChar('●');
            }
        });
        c.gridy = 4;
        c.insets = new Insets(5, 0, 20, 0);
        pnlForm.add(chkHienMatKhau, c);

        // Button Login
        btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLogin.setBackground(new Color(0, 123, 255));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(0, 48));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        c.gridy = 5;
        c.insets = new Insets(10, 0, 10, 0);
        pnlForm.add(btnLogin, c);

        // Button Thoát
        btnThoat = new JButton("THOÁT");
        btnThoat.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnThoat.setBackground(new Color(220, 220, 220));
        btnThoat.setPreferredSize(new Dimension(0, 40));
        btnThoat.setFocusPainted(false);
        btnThoat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        c.gridy = 6;
        pnlForm.add(btnThoat, c);

        pnlMain.add(pnlForm, BorderLayout.CENTER);

        // --- FOOTER ---
        JPanel pnlFooter = new JPanel();
        pnlFooter.setBackground(new Color(245, 245, 245));
        pnlFooter.setPreferredSize(new Dimension(0, 50));
        JLabel lblFooter = new JLabel("© 2026 Restaurant Management System");
        lblFooter.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblFooter.setForeground(new Color(128, 128, 128));
        pnlFooter.add(lblFooter);
        pnlMain.add(pnlFooter, BorderLayout.SOUTH);

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
