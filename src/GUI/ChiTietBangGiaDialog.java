package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;

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

import DAO.BangGiaDAO;
import DAO.MonAnDAO;
import Entity.ChiTietBangGia;
import Entity.MonAn;
import GUI.utils.UIStyle;

public class ChiTietBangGiaDialog extends JDialog {

    private JTextField txtTimKiem;
    private JComboBox<MonAn> cboCTMon;
    private JTextField txtDonGia, txtGhiChu;
    
    private boolean saved = false;
    private BangGiaDAO bgDao;
    private MonAnDAO maDao;
    private int maBG;

    private ChiTietBangGia ctEdit;

    public ChiTietBangGiaDialog(JPanel parent, int maBG, BangGiaDAO bgDao, ChiTietBangGia ctEdit) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), true);
        this.bgDao = bgDao;
        this.maDao = new MonAnDAO();
        this.maBG = maBG;
        this.ctEdit = ctEdit;

        setTitle(ctEdit != null ? "CẬP NHẬT MÓN TRONG CHIẾN DỊCH" : "THÊM MÓN TRONG CHIẾN DỊCH");
        setSize(500, 350);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 0, 5, 0));
        pnlHeader.add(UIStyle.title(ctEdit != null ? "CẬP NHẬT GIÁ MÓN ƯU ĐÃI" : "ÁP GIÁ CHO MÓN ĂN"));
        add(pnlHeader, BorderLayout.NORTH);

        // --- FORM INPUTS ---
        JPanel pnlInputs = new JPanel(new GridBagLayout());
        pnlInputs.setBackground(Color.WHITE);
        pnlInputs.setBorder(new EmptyBorder(10, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        int row = 0;
        
        txtTimKiem = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Tìm kiếm:", txtTimKiem);

        cboCTMon = new JComboBox<>();
        UIStyle.styleComboBox(cboCTMon);
        java.util.List<MonAn> dsMon = maDao.getAll();
        for (MonAn m : dsMon) cboCTMon.addItem(m);
        addRow(pnlInputs, gbc, row++, "Món ăn:", cboCTMon);

        txtDonGia = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Đơn giá mới:", txtDonGia);

        txtGhiChu = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Ghi chú:", txtGhiChu);

        add(pnlInputs, BorderLayout.CENTER);

        // --- BUTTONS ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlButtons.setBackground(Color.WHITE);

        JButton btnLuu = UIStyle.button(UIStyle.BtnType.SUCCESS, ctEdit != null ? "CẬP NHẬT" : "THÊM MÓN");
        btnLuu.setPreferredSize(new java.awt.Dimension(120, 35));
        JButton btnHuy = UIStyle.button(UIStyle.BtnType.NEUTRAL, "HỦY");
        btnHuy.setPreferredSize(new java.awt.Dimension(100, 35));

        pnlButtons.add(btnHuy);
        pnlButtons.add(btnLuu);
        add(pnlButtons, BorderLayout.SOUTH);

        if (ctEdit != null) {
            txtTimKiem.setEnabled(false);
            cboCTMon.setEnabled(false);
            for (int i = 0; i < cboCTMon.getItemCount(); i++) {
                if (cboCTMon.getItemAt(i).getMaMon().equals(ctEdit.getMaMon())) {
                    cboCTMon.setSelectedIndex(i);
                    break;
                }
            }
            txtDonGia.setText(new DecimalFormat("#.##").format(ctEdit.getDonGia()).replace(",", ""));
            txtGhiChu.setText(ctEdit.getGhiChu());
        }

        // --- EVENT LISTENERS ---
        txtTimKiem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtTimKiem.getText().toLowerCase();
                cboCTMon.removeAllItems();
                for (MonAn m : dsMon) {
                    if (m.getTenMon().toLowerCase().contains(text)) {
                        cboCTMon.addItem(m);
                    }
                }
            }
        });

        cboCTMon.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                MonAn selectedMon = (MonAn) cboCTMon.getSelectedItem();
                if (selectedMon != null) {
                    txtDonGia.setText(new DecimalFormat("#.##").format(selectedMon.getDonGia()).replace(",", ""));
                }
            }
        });

        btnLuu.addActionListener(e -> save());
        btnHuy.addActionListener(e -> dispose());
    }

    private void addRow(JPanel parent, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = UIStyle.label(label);
        lbl.setPreferredSize(new java.awt.Dimension(115, 30));
        parent.add(lbl, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        parent.add(comp, gbc);
    }

    private void save() {
        if (maBG == -1) {
            JOptionPane.showMessageDialog(this, "Mã chiến dịch không hợp lệ!");
            return;
        }

        MonAn selectedMon = (MonAn) cboCTMon.getSelectedItem();
        if (selectedMon == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn món ăn!");
            return;
        }

        double donGia;
        try {
            donGia = Double.parseDouble(txtDonGia.getText().replace(",", ""));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đơn giá không hợp lệ!");
            return;
        }

        ChiTietBangGia ct = new ChiTietBangGia(maBG, selectedMon.getMaMon(), donGia, txtGhiChu.getText().trim());
        
        if (ctEdit != null) {
            if (bgDao.updateChiTiet(ct)) {
                JOptionPane.showMessageDialog(this, "Cập nhật giá món thành công!");
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi cập nhật món!");
            }
        } else {
            if (bgDao.insertChiTiet(ct)) {
                JOptionPane.showMessageDialog(this, "Áp giá món vào chiến dịch thành công!");
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi thêm món! Món này có thể đã tồn tại trong chiến dịch.");
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
