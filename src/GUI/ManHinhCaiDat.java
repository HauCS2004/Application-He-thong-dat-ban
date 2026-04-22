package GUI;

import GUI.utils.SystemConfig;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * ManHinhCaiDat – Dialog cài đặt hệ thống.
 * Quản lý có thể điều chỉnh:
 * 1. Thông tin quán (tên, địa chỉ, SĐT, logo)
 * 2. Thuế & Phí (VAT%, phí phục vụ%)
 * 3. Thông tin chuyển khoản (ngân hàng, STK, chủ TK, QR template)
 */
public class ManHinhCaiDat extends JDialog {

    // ── Thông tin quán ───────────────────────────────────────────────────────
    private JTextField txtResName;
    private JTextField txtResAddress;
    private JTextField txtResPhone;
    private JTextField txtResLogo;

    // ── Thuế & Phí ──────────────────────────────────────────────────────────
    private JSpinner spnVAT;
    private JSpinner spnService;

    // ── Chuyển khoản ────────────────────────────────────────────────────────
    private JComboBox<String> cboBankName;
    private JTextField txtBankAccount;
    private JTextField txtBankHolder;
    private JTextField txtBankQrTemplate;

    // Danh sách mã ngân hàng VietQR
    private static final String[] BANKS = {
            "MB", "VCB", "TCB", "ACB", "BIDV",
            "VCB", "CTG", "STB", "TPB", "OCB",
            "MSB", "VPB", "HDB", "SHB", "EIB",
            "SEAB", "NAB", "BAB", "ABB", "LPB"
    };

    public ManHinhCaiDat(Frame owner) {
        super(owner, "Cài đặt hệ thống", true);
        setSize(900, 520);
        setMinimumSize(new Dimension(820, 480));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setResizable(true);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadValues();
    }

    // ── HEADER ──────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));

        // JLabel ico = new JLabel("⚙");
        // ico.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        // ico.setForeground(new Color(59, 130, 246));

        JLabel title = new JLabel("Cài đặt hệ thống");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(17, 24, 39));

        // pnl.add(ico);
        pnl.add(title);
        return pnl;
    }

    // ── BODY (3 cards) ──────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel pnl = new JPanel(new GridLayout(1, 3, 16, 0));
        pnl.setBackground(new Color(245, 247, 250));
        pnl.setBorder(new EmptyBorder(20, 20, 10, 20));

        pnl.add(buildRestaurantCard());
        pnl.add(buildTaxCard());
        pnl.add(buildBankCard());

        return pnl;
    }

    // ── CARD: Thông tin quán ────────────────────────────────────────────────
    private JPanel buildRestaurantCard() {
        JPanel card = card("Thông tin quán");
        BoxLayout box = new BoxLayout(card, BoxLayout.Y_AXIS);
        card.setLayout(box);
        card.add(cardTitle("Thông tin quán"));
        card.add(Box.createVerticalStrut(14));

        txtResName = field("Tên nhà hàng");
        txtResAddress = field("Địa chỉ");
        txtResPhone = field("Số điện thoại");
        txtResLogo = field("URL Logo (tuỳ chọn)");

        card.add(labeledField("Tên nhà hàng:", txtResName));
        card.add(Box.createVerticalStrut(8));
        card.add(labeledField("Địa chỉ:", txtResAddress));
        card.add(Box.createVerticalStrut(8));
        card.add(labeledField("Số điện thoại:", txtResPhone));
        card.add(Box.createVerticalStrut(8));
        card.add(labeledField("Logo URL:", txtResLogo));

        card.add(Box.createVerticalGlue());
        card.add(saveBtn("Lưu thông tin quán", () -> saveRestaurant()));
        card.add(Box.createVerticalStrut(6));
        return card;
    }

    // ── CARD: Thuế & Phí ────────────────────────────────────────────────────
    private JPanel buildTaxCard() {
        JPanel card = card("Thuế & Phí");
        BoxLayout box = new BoxLayout(card, BoxLayout.Y_AXIS);
        card.setLayout(box);
        card.add(cardTitle("Thuế & Phí"));
        card.add(Box.createVerticalStrut(14));

        spnVAT = spinner(0.0, 50.0, 0.5);
        spnService = spinner(0.0, 30.0, 0.5);

        card.add(labeledField("Thuế VAT (%):", spnVAT));
        card.add(Box.createVerticalStrut(8));
        card.add(labeledField("Phí phục vụ (%):", spnService));

        JLabel note = new JLabel("<html><i style='color:#6b7280;font-size:11px;'>"
                + "Thay đổi sẽ áp dụng cho hóa đơn mới.</i></html>");
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Box.createVerticalStrut(10));
        card.add(note);

        card.add(Box.createVerticalGlue());
        card.add(saveBtn("Lưu cài đặt thuế", () -> saveTax()));
        card.add(Box.createVerticalStrut(6));
        return card;
    }

    // ── CARD: Thông tin chuyển khoản ────────────────────────────────────────
    private JPanel buildBankCard() {
        JPanel card = card("Thông tin chuyển khoản");
        BoxLayout box = new BoxLayout(card, BoxLayout.Y_AXIS);
        card.setLayout(box);
        card.add(cardTitle("Thông tin chuyển khoản"));
        card.add(Box.createVerticalStrut(14));

        cboBankName = new JComboBox<>(BANKS);
        cboBankName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cboBankName.setAlignmentX(Component.LEFT_ALIGNMENT);
        cboBankName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JComponent) cboBankName).setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)));

        txtBankAccount = field("Số tài khoản");
        txtBankHolder = field("Tên chủ tài khoản");
        txtBankQrTemplate = field("URL template QR (tùy chọn)");

        card.add(labeledField("Ngân hàng:", cboBankName));
        card.add(Box.createVerticalStrut(8));
        card.add(labeledField("Số tài khoản:", txtBankAccount));
        card.add(Box.createVerticalStrut(8));
        card.add(labeledField("Chủ tài khoản:", txtBankHolder));
        card.add(Box.createVerticalStrut(8));
        card.add(labeledField("QR Template:", txtBankQrTemplate));

        card.add(Box.createVerticalGlue());
        card.add(saveBtn("Lưu thông tin bank", () -> saveBank()));
        card.add(Box.createVerticalStrut(6));
        return card;
    }

    // ── FOOTER ──────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        JButton btnSaveAll = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.SUCCESS, "Lưu tất cả");
        btnSaveAll.setPreferredSize(new Dimension(160, 38));
        btnSaveAll.addActionListener(e -> {
            saveRestaurant();
            saveTax();
            saveBank();
            JOptionPane.showMessageDialog(this,
                    "✅ Đã lưu toàn bộ cài đặt hệ thống!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnClose = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.NEUTRAL, "Đóng");
        btnClose.setPreferredSize(new Dimension(110, 38));
        btnClose.addActionListener(e -> dispose());

        pnl.add(btnClose);
        pnl.add(btnSaveAll);
        return pnl;
    }

    // ── LOAD from config ────────────────────────────────────────────────────
    private void loadValues() {
        txtResName.setText(SystemConfig.getResName());
        txtResAddress.setText(SystemConfig.getResAddress());
        txtResPhone.setText(SystemConfig.getResPhone());
        txtResLogo.setText(SystemConfig.getResLogo());

        spnVAT.setValue(SystemConfig.getVAT());
        spnService.setValue(SystemConfig.getServiceFee());

        // Bank name combobox
        String bankName = SystemConfig.getBankName();
        boolean found = false;
        for (int i = 0; i < cboBankName.getItemCount(); i++) {
            if (cboBankName.getItemAt(i).equalsIgnoreCase(bankName)) {
                cboBankName.setSelectedIndex(i);
                found = true;
                break;
            }
        }
        if (!found) {
            // Add custom bank name
            cboBankName.addItem(bankName.toUpperCase());
            cboBankName.setSelectedItem(bankName.toUpperCase());
        }
        txtBankAccount.setText(SystemConfig.getBankAccount());
        txtBankHolder.setText(SystemConfig.getBankHolder());
        txtBankQrTemplate.setText(SystemConfig.getBankQrTemplate());
    }

    // ── SAVE actions ────────────────────────────────────────────────────────
    private void saveRestaurant() {
        SystemConfig.setResName(txtResName.getText().trim());
        SystemConfig.setResAddress(txtResAddress.getText().trim());
        SystemConfig.setResPhone(txtResPhone.getText().trim());
        SystemConfig.setResLogo(txtResLogo.getText().trim());
        SystemConfig.save();
    }

    private void saveTax() {
        SystemConfig.setVAT((double) spnVAT.getValue());
        SystemConfig.setServiceFee((double) spnService.getValue());
        SystemConfig.save();
    }

    private void saveBank() {
        String selectedBank = cboBankName.getSelectedItem() != null
                ? cboBankName.getSelectedItem().toString()
                : "";
        SystemConfig.setBankName(selectedBank);
        SystemConfig.setBankAccount(txtBankAccount.getText().trim());
        SystemConfig.setBankHolder(txtBankHolder.getText().trim());
        SystemConfig.setBankQrTemplate(txtBankQrTemplate.getText().trim());
        SystemConfig.save();
    }

    // ── UI Helpers ──────────────────────────────────────────────────────────

    /** Tạo một card panel với border bo góc */
    private JPanel card(String name) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new CompoundBorder(
                new LineBorder(new Color(229, 231, 235), 1, true),
                new EmptyBorder(16, 18, 16, 18)));
        return p;
    }

    /** Tiêu đề bên trong card */
    private JLabel cardTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(new Color(17, 24, 39));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /** Text field với placeholder */
    private JTextField field(String placeholder) {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                new EmptyBorder(4, 8, 4, 8)));
        f.putClientProperty("JTextField.placeholderText", placeholder);
        return f;
    }

    /** JSpinner styled */
    private JSpinner spinner(double min, double max, double step) {
        JSpinner sp = new JSpinner(new SpinnerNumberModel(0.0, min, max, step));
        sp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sp;
    }

    /** Wrapper: label phía trên component */
    private JPanel labeledField(String labelText, JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(75, 85, 99));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(3));
        p.add(comp);
        return p;
    }

    /** Nút Lưu nhỏ bên trong card */
    private JButton saveBtn(String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(new Color(59, 130, 246));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(37, 99, 235));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(59, 130, 246));
            }
        });
        btn.addActionListener(e -> {
            action.run();
            JOptionPane.showMessageDialog(this, "Đã lưu thành công!", "OK",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        return btn;
    }
}
