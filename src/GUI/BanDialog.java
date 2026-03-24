package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import DAO.BanDAO;
import Entity.Ban;
import GUI.utils.UIStyle;

public class BanDialog extends JDialog {

    private JTextField txtMa, txtTen, txtGhe;
    private JComboBox<String> cboKV;
    private boolean saved = false;
    private boolean isEdit = false;
    private BanDAO banDAO;

    public BanDialog(JPanel parent, Ban ban, BanDAO dao) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), true);
        this.banDAO = dao;
        this.isEdit = (ban != null);

        setTitle(isEdit ? "SỬA BÀN: " + ban.getTenBan() : "THÊM BÀN MỚI");
        setSize(400, 350);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 0, 5, 0));
        pnlHeader.add(UIStyle.title(isEdit ? "CHỈNH SỬA BÀN" : "THÊM BÀN MỚI"));
        add(pnlHeader, BorderLayout.NORTH);

        // --- FORM INPUTS ---
        JPanel pnlInputs = new JPanel(new GridBagLayout());
        pnlInputs.setBackground(Color.WHITE);
        pnlInputs.setBorder(new EmptyBorder(10, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        int row = 0;
        
        txtMa = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Mã bàn:", txtMa);

        txtTen = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Tên bàn:", txtTen);

        String[] khuVucs = { "KV01", "KV02", "KV03", "KV04" };
        cboKV = new JComboBox<>(khuVucs);
        UIStyle.styleComboBox(cboKV);
        addRow(pnlInputs, gbc, row++, "Khu vực:", cboKV);

        txtGhe = UIStyle.textField("", 20);
        txtGhe.setText("4"); // default
        addRow(pnlInputs, gbc, row++, "Số ghế:", txtGhe);

        add(pnlInputs, BorderLayout.CENTER);

        // --- BUTTONS ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlButtons.setBackground(Color.WHITE);

        JButton btnLuu = UIStyle.button(UIStyle.BtnType.SUCCESS, "LƯU DỮ LIỆU");
        btnLuu.setPreferredSize(new java.awt.Dimension(120, 35));
        JButton btnHuy = UIStyle.button(UIStyle.BtnType.NEUTRAL, "HỦY");
        btnHuy.setPreferredSize(new java.awt.Dimension(100, 35));

        pnlButtons.add(btnHuy);
        pnlButtons.add(btnLuu);

        add(pnlButtons, BorderLayout.SOUTH);

        // --- BIND DATA IF EDIT ---
        if (isEdit) {
            txtMa.setText(ban.getMaBan());
            txtMa.setEditable(false);
            txtMa.setBackground(new Color(240, 240, 240));
            txtTen.setText(ban.getTenBan());
            cboKV.setSelectedItem(ban.getMaKV());
            txtGhe.setText(String.valueOf(ban.getSoGhe()));
        }

        // --- EVENT LISTENERS ---
        btnLuu.addActionListener(e -> save());
        btnHuy.addActionListener(e -> dispose());
    }

    private void addRow(JPanel parent, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = UIStyle.label(label);
        lbl.setPreferredSize(new java.awt.Dimension(80, 30));
        parent.add(lbl, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        parent.add(comp, gbc);
    }

    private void save() {
        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        String kv = cboKV.getSelectedItem().toString();
        int ghe = 4;
        try {
            ghe = Integer.parseInt(txtGhe.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Số ghế phải là số nguyên!");
            return;
        }

        if (ma.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin (Mã, Tên)!");
            return;
        }

        Ban newBan = new Ban(ma, ten, "Trống", kv, ghe, null);
        
        if (isEdit) {
            if (banDAO.updateInfo(newBan)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật!");
            }
        } else {
            if (banDAO.insert(newBan)) {
                JOptionPane.showMessageDialog(this, "Lưu thành công!");
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu (Trùng mã bàn?)!");
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
