package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Date;

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

import com.toedter.calendar.JDateChooser;

import DAO.KhuyenMaiDAO;
import Entity.KhuyenMai;
import GUI.utils.UIStyle;

public class KhuyenMaiDialog extends JDialog {

    private JTextField txtMaKM, txtTenKM, txtGiaTri, txtDieuKien;
    private JComboBox<String> cboLoaiKM, cboTrangThai, cboHangVIP;
    private JDateChooser dateBatDau, dateKetThuc;
    
    private boolean saved = false;
    private boolean isEdit = false;
    private KhuyenMaiDAO dao;

    public KhuyenMaiDialog(JPanel parent, KhuyenMai km, KhuyenMaiDAO dao) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), true);
        this.dao = dao;
        this.isEdit = (km != null);

        setTitle(isEdit ? "SỬA KHUYẾN MÃI" : "THÊM KHUYẾN MÃI");
        setSize(500, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 0, 5, 0));
        pnlHeader.add(UIStyle.title(isEdit ? "SỬA KHUYẾN MÃI" : "TẠO KHUYẾN MÃI"));
        add(pnlHeader, BorderLayout.NORTH);

        // --- FORM INPUTS ---
        JPanel pnlInputs = new JPanel(new GridBagLayout());
        pnlInputs.setBackground(Color.WHITE);
        pnlInputs.setBorder(new EmptyBorder(10, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        int row = 0;
        
        txtMaKM = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Mã KM:", txtMaKM);

        txtTenKM = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Tên KM:", txtTenKM);

        cboLoaiKM = new JComboBox<>(new String[] { "Giảm %", "Giảm tiền", "Tặng món" });
        UIStyle.styleComboBox(cboLoaiKM);
        addRow(pnlInputs, gbc, row++, "Loại giảm:", cboLoaiKM);

        txtGiaTri = UIStyle.textField("", 20);
        txtGiaTri.setText("0");
        addRow(pnlInputs, gbc, row++, "Giá trị:", txtGiaTri);

        txtDieuKien = UIStyle.textField("", 20);
        txtDieuKien.setText("0");
        addRow(pnlInputs, gbc, row++, "ĐK tối thiểu:", txtDieuKien);

        cboHangVIP = new JComboBox<>(new String[] { "Tất cả", "Bạc", "Vàng", "Kim cương" });
        UIStyle.styleComboBox(cboHangVIP);
        addRow(pnlInputs, gbc, row++, "Áp dụng VIP:", cboHangVIP);

        dateBatDau = new JDateChooser();
        dateBatDau.setDateFormatString("dd/MM/yyyy");
        dateBatDau.setFont(UIStyle.textField("", 1).getFont());
        addRow(pnlInputs, gbc, row++, "Từ ngày:", dateBatDau);

        dateKetThuc = new JDateChooser();
        dateKetThuc.setDateFormatString("dd/MM/yyyy");
        dateKetThuc.setFont(UIStyle.textField("", 1).getFont());
        addRow(pnlInputs, gbc, row++, "Đến ngày:", dateKetThuc);

        cboTrangThai = new JComboBox<>(new String[] { "Đang hoạt động", "Tạm ngưng", "Đã kết thúc" });
        UIStyle.styleComboBox(cboTrangThai);
        addRow(pnlInputs, gbc, row++, "Trạng thái:", cboTrangThai);

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
            txtMaKM.setText(km.getMaKM());
            txtMaKM.setEditable(false);
            txtMaKM.setBackground(new Color(240, 240, 240));
            txtTenKM.setText(km.getTenKM());
            cboLoaiKM.setSelectedItem(km.getLoaiKM());
            
            txtGiaTri.setText(String.format("%.0f", km.getGiaTri()));
            txtDieuKien.setText(String.format("%.0f", km.getDieuKienToiThieu()));
            
            if (km.getNgayBatDau() != null) dateBatDau.setDate(km.getNgayBatDau());
            if (km.getNgayKetThuc() != null) dateKetThuc.setDate(km.getNgayKetThuc());
            
            cboHangVIP.setSelectedItem(km.getHangVIPApDung() == null ? "Tất cả" : km.getHangVIPApDung());
            cboTrangThai.setSelectedItem(km.getTrangThai());
        }

        // --- EVENT LISTENERS ---
        btnLuu.addActionListener(e -> save());
        btnHuy.addActionListener(e -> dispose());
    }

    private void addRow(JPanel parent, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = UIStyle.label(label);
        lbl.setPreferredSize(new java.awt.Dimension(110, 30));
        parent.add(lbl, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        parent.add(comp, gbc);
    }

    private void save() {
        String ma = txtMaKM.getText().trim();
        String ten = txtTenKM.getText().trim();
        String loai = cboLoaiKM.getSelectedItem().toString();
        String vip = cboHangVIP.getSelectedItem().toString();
        if (vip.equals("Tất cả")) vip = null;
        String trangThai = cboTrangThai.getSelectedItem().toString();

        if (ma.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Mã và Tên!");
            return;
        }

        double giaTri = 0, dieuKien = 0;
        try {
            giaTri = Double.parseDouble(txtGiaTri.getText().trim().replace(",", ""));
            dieuKien = Double.parseDouble(txtDieuKien.getText().trim().replace(",", ""));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá trị và điều kiện phải là số!");
            return;
        }

        Date start = dateBatDau.getDate();
        Date end = dateKetThuc.getDate();

        if (start == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày bắt đầu!");
            return;
        }

        if (end != null && end.before(start)) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc phải sau ngày bắt đầu!");
            return;
        }

        KhuyenMai km = new KhuyenMai(ma, ten, loai, giaTri, dieuKien, start, end, trangThai, vip);

        if (isEdit) {
            if (dao.update(km)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
            }
        } else {
            if (dao.insert(km)) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại! Mã KM có thể đã tồn tại.");
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
