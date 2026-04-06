package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import DAO.KhachHangDAO;
import Entity.KhachHang;
import GUI.utils.UIStyle;

public class KhachHangDialog extends JDialog {

    private JTextField txtSDT, txtTen, txtDiem;
    private boolean saved = false;
    private boolean isEdit = false;
    private KhachHangDAO dao;
    private String oldSDT = null;

    public KhachHangDialog(JPanel parent, KhachHang kh, KhachHangDAO dao) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), true);
        this.dao = dao;
        this.isEdit = (kh != null);

        setTitle(isEdit ? "SỬA KHÁCH HÀNG" : "THÊM KHÁCH HÀNG");
        setSize(400, 320);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 0, 5, 0));
        pnlHeader.add(UIStyle.title(isEdit ? "CHỈNH SỬA KHÁCH" : "THÊM KHÁCH MỚI"));
        add(pnlHeader, BorderLayout.NORTH);

        // --- FORM INPUTS ---
        JPanel pnlInputs = new JPanel(new GridBagLayout());
        pnlInputs.setBackground(Color.WHITE);
        pnlInputs.setBorder(new EmptyBorder(10, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        int row = 0;
        
        txtSDT = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "SĐT:", txtSDT);

        txtTen = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Tên khách:", txtTen);

        txtDiem = UIStyle.textField("", 20);
        txtDiem.setText("0");
        txtDiem.setEditable(false);
        txtDiem.setEnabled(false);
        addRow(pnlInputs, gbc, row++, "Điểm:", txtDiem);

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
            oldSDT = kh.getSoDienThoai();
            txtSDT.setText(kh.getSoDienThoai());
            txtTen.setText(kh.getTenKhach());
            txtDiem.setText(String.valueOf(kh.getDiemTichLuy()));
            
            // Note: Trọng tâm thay đổi theo yêu cầu user, cho phép sửa số điện thoại!
        }

        // --- EVENT LISTENERS ---
        btnLuu.addActionListener(e -> save());
        btnHuy.addActionListener(e -> dispose());
    }

    private void addRow(JPanel parent, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = UIStyle.label(label);
        lbl.setPreferredSize(new java.awt.Dimension(90, 30));
        parent.add(lbl, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        parent.add(comp, gbc);
    }

    private void save() {
        String sdt = txtSDT.getText().trim();
        String ten = txtTen.getText().trim();

        if (sdt.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ SĐT và Tên!");
            return;
        }
        
        if (!sdt.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải bao gồm 10 chữ số!");
            return;
        }

        int diem = 0;
        try {
            diem = Integer.parseInt(txtDiem.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Điểm tích lũy phải là số nguyên!");
            return;
        }

        KhachHang kh = new KhachHang(sdt, ten, diem);

        if (isEdit) {
            boolean success = dao.update(kh, oldSDT);
            if (success) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi cập nhật! (Khách đã có hóa đơn không thể đổi SĐT hoặc trùng SĐT Mới)");
            }
        } else {
            if (dao.checkTonTai(sdt)) {
                JOptionPane.showMessageDialog(this, "Số điện thoại này đã tồn tại!");
                return;
            }
            if (dao.insert(kh)) {
                JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!");
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi thêm khách hàng!");
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
