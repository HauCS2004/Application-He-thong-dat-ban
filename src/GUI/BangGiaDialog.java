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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.toedter.calendar.JDateChooser;

import DAO.BangGiaDAO;
import Entity.BangGia;
import GUI.utils.UIStyle;

public class BangGiaDialog extends JDialog {

    private JTextField txtTen, txtGhiChu;
    private JComboBox<String> cboLoai, cboTrangThai;
    private JDateChooser dpTuNgay, dpDenNgay;
    private JSpinner spGioBD, spGioKT, spUuTien;
    
    private boolean saved = false;
    private boolean isEdit = false;
    private BangGiaDAO dao;
    private int bangGiaId = 0; // for edit context

    public BangGiaDialog(JPanel parent, BangGia bg, BangGiaDAO dao) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), true);
        this.dao = dao;
        this.isEdit = (bg != null);
        if (isEdit) this.bangGiaId = bg.getMaBG();

        setTitle(isEdit ? "SỬA BẢNG GIÁ" : "THÊM BẢNG GIÁ MỚI");
        setSize(550, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 0, 5, 0));
        pnlHeader.add(UIStyle.title(isEdit ? "CHỈNH SỬA CHIẾN DỊCH" : "TẠO CHIẾN DỊCH GIÁ"));
        add(pnlHeader, BorderLayout.NORTH);

        // --- FORM INPUTS ---
        JPanel pnlInputs = new JPanel(new GridBagLayout());
        pnlInputs.setBackground(Color.WHITE);
        pnlInputs.setBorder(new EmptyBorder(10, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        int row = 0;
        
        txtTen = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Tên C.Dịch:", txtTen);

        cboLoai = new JComboBox<>(new String[] { "Ngày lễ", "Sự kiện", "Khung giờ vàng", "Khác" });
        UIStyle.styleComboBox(cboLoai);
        addRow(pnlInputs, gbc, row++, "Loại:", cboLoai);

        dpTuNgay = new JDateChooser();
        dpTuNgay.setDateFormatString("yyyy-MM-dd");
        dpTuNgay.setFont(UIStyle.textField("", 1).getFont());
        addRow(pnlInputs, gbc, row++, "Từ ngày:", dpTuNgay);

        dpDenNgay = new JDateChooser();
        dpDenNgay.setDateFormatString("yyyy-MM-dd");
        dpDenNgay.setFont(UIStyle.textField("", 1).getFont());
        addRow(pnlInputs, gbc, row++, "Đến ngày:", dpDenNgay);

        spGioBD = new JSpinner(new SpinnerDateModel());
        spGioBD.setEditor(new JSpinner.DateEditor(spGioBD, "HH:mm"));
        spGioBD.setFont(UIStyle.textField("", 1).getFont());
        addRow(pnlInputs, gbc, row++, "Giờ BĐ:", spGioBD);

        spGioKT = new JSpinner(new SpinnerDateModel());
        spGioKT.setEditor(new JSpinner.DateEditor(spGioKT, "HH:mm"));
        spGioKT.setFont(UIStyle.textField("", 1).getFont());
        addRow(pnlInputs, gbc, row++, "Giờ KT:", spGioKT);

        spUuTien = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        spUuTien.setFont(UIStyle.textField("", 1).getFont());
        addRow(pnlInputs, gbc, row++, "Ưu tiên:", spUuTien);

        cboTrangThai = new JComboBox<>(new String[] { "Hoạt động", "Tạm ngưng", "Đã kết thúc" });
        UIStyle.styleComboBox(cboTrangThai);
        addRow(pnlInputs, gbc, row++, "Trạng thái:", cboTrangThai);

        txtGhiChu = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Ghi chú:", txtGhiChu);

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
            txtTen.setText(bg.getTenBG());
            cboLoai.setSelectedItem(bg.getLoaiBG());
            if (bg.getNgayBatDau() != null) dpTuNgay.setDate(bg.getNgayBatDau());
            if (bg.getNgayKetThuc() != null) dpDenNgay.setDate(bg.getNgayKetThuc());
            if (bg.getGioBatDau() != null) spGioBD.setValue(bg.getGioBatDau());
            if (bg.getGioKetThuc() != null) spGioKT.setValue(bg.getGioKetThuc());
            spUuTien.setValue(bg.getUuTien());
            cboTrangThai.setSelectedItem(bg.getTrangThai());
            txtGhiChu.setText(bg.getGhiChu());
        } else {
            try { spGioBD.setValue(new java.text.SimpleDateFormat("HH:mm").parse("00:00")); } catch (Exception e){}
            try { spGioKT.setValue(new java.text.SimpleDateFormat("HH:mm").parse("00:00")); } catch (Exception e){}
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
        String ten = txtTen.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên chiến dịch!");
            return;
        }

        String loai = cboLoai.getSelectedItem().toString();
        
        java.sql.Date tuNgay = null;
        if (dpTuNgay.getDate() != null) tuNgay = new java.sql.Date(dpTuNgay.getDate().getTime());
        
        java.sql.Date denNgay = null;
        if (dpDenNgay.getDate() != null) denNgay = new java.sql.Date(dpDenNgay.getDate().getTime());
        
        java.sql.Time gioBD = null;
        try { gioBD = new java.sql.Time(((Date) spGioBD.getValue()).getTime()); } catch (Exception e){}
        
        java.sql.Time gioKT = null;
        try { gioKT = new java.sql.Time(((Date) spGioKT.getValue()).getTime()); } catch (Exception e){}

        int uuTien = (int) spUuTien.getValue();
        String trangThai = cboTrangThai.getSelectedItem().toString();
        String ghiChu = txtGhiChu.getText().trim();

        BangGia bg = new BangGia(bangGiaId, ten, loai, tuNgay, denNgay, gioBD, gioKT, uuTien, trangThai, ghiChu, null);

        if (isEdit) {
            if (dao.update(bg)) {
                JOptionPane.showMessageDialog(this, "Sửa thành công!");
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi cập nhật chiến dịch!");
            }
        } else {
            if (dao.insert(bg) != -1) {
                JOptionPane.showMessageDialog(this, "Thêm chiến dịch thành công!");
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi thêm chiến dịch!");
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
