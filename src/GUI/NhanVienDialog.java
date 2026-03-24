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
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import DAO.NhanVienDAO;
import Entity.NhanVien;
import Entity.TaiKhoan;
import GUI.utils.UIStyle;

public class NhanVienDialog extends JDialog {

    private JTextField txtMaNV, txtTen, txtMatKhau, txtSDT, txtEmail, txtCCCD;
    private JComboBox<String> cboChucVu, cboGioiTinh, cboTrangThai;
    private JSpinner spinNgayVaoLam;
    private boolean saved = false;
    private boolean isEdit = false;
    private NhanVienDAO dao;

    public NhanVienDialog(JPanel parent, NhanVien nv, NhanVienDAO dao) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), true);
        this.dao = dao;
        this.isEdit = (nv != null);

        setTitle(isEdit ? "SỬA NHÂN VIÊN" : "THÊM NHÂN VIÊN");
        setSize(480, 650);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 0, 5, 0));
        pnlHeader.add(UIStyle.title(isEdit ? "CHỈNH SỬA NHÂN VIÊN" : "THÊM NHÂN VIÊN MỚI"));
        add(pnlHeader, BorderLayout.NORTH);

        // --- FORM INPUTS ---
        JPanel pnlInputs = new JPanel(new GridBagLayout());
        pnlInputs.setBackground(Color.WHITE);
        pnlInputs.setBorder(new EmptyBorder(10, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        int row = 0;
        
        txtMaNV = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Mã NV:", txtMaNV);

        txtTen = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Tên NV:", txtTen);
        
        cboGioiTinh = new JComboBox<>(new String[] { "Nam", "Nữ" });
        UIStyle.styleComboBox(cboGioiTinh);
        addRow(pnlInputs, gbc, row++, "Giới tính:", cboGioiTinh);

        txtCCCD = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "CCCD/CMND:", txtCCCD);

        txtSDT = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Số điện thoại:", txtSDT);

        txtEmail = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Email:", txtEmail);

        txtMatKhau = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Mật khẩu:", txtMatKhau);

        cboChucVu = new JComboBox<>(new String[] { "Nhân viên", "Quản lý" });
        UIStyle.styleComboBox(cboChucVu);
        addRow(pnlInputs, gbc, row++, "Vai trò:", cboChucVu);

        spinNgayVaoLam = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinNgayVaoLam, "yyyy-MM-dd");
        spinNgayVaoLam.setEditor(dateEditor);
        spinNgayVaoLam.setFont(UIStyle.textField("", 1).getFont());
        addRow(pnlInputs, gbc, row++, "Ngày làm:", spinNgayVaoLam);
        
        cboTrangThai = new JComboBox<>(new String[] { "Đang làm việc", "Đã nghỉ" });
        UIStyle.styleComboBox(cboTrangThai);
        if (!isEdit) {
            cboTrangThai.setSelectedItem("Đang làm việc");
            cboTrangThai.setEnabled(false); // Can't start as resigned
        }
        addRow(pnlInputs, gbc, row++, "Trạng thái:", cboTrangThai);

        add(pnlInputs, BorderLayout.CENTER);

        // --- BUTTONS ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlButtons.setBackground(Color.WHITE);

        JButton btnLuu = UIStyle.button(UIStyle.BtnType.SUCCESS, "LƯU DỮ LIỆU");
        btnLuu.setPreferredSize(new java.awt.Dimension(120, 35));
        JButton btnHuy = UIStyle.button(UIStyle.BtnType.NEUTRAL, "HỦY");
        btnHuy.setPreferredSize(new java.awt.Dimension(100, 35));

        pnlButtons.add(btnHuy); // Secondary action first
        pnlButtons.add(btnLuu);

        add(pnlButtons, BorderLayout.SOUTH);

        // --- BIND DATA IF EDIT ---
        if (isEdit) {
            txtMaNV.setText(nv.getMaNV());
            txtMaNV.setEditable(false);
            txtMaNV.setBackground(new Color(240, 240, 240));
            txtTen.setText(nv.getTenNV());
            cboGioiTinh.setSelectedItem(nv.getGioiTinh() != null ? nv.getGioiTinh() : "Nam");
            txtCCCD.setText(nv.getCccd() != null ? nv.getCccd() : "");
            txtSDT.setText(nv.getSoDienThoai());
            txtEmail.setText(nv.getEmail());
            
            cboTrangThai.setSelectedItem(nv.getTrangThai() != null ? nv.getTrangThai() : "Đang làm việc");
            cboTrangThai.setEnabled(true);
            
            if (nv.getTaiKhoan() != null) {
                txtMatKhau.setText(nv.getTaiKhoan().getMatKhau());
                cboChucVu.setSelectedItem(nv.getTaiKhoan().getVaiTro());
            }
            if (nv.getNgayVaoLam() != null) {
                spinNgayVaoLam.setValue(nv.getNgayVaoLam());
            }
        }

        // --- EVENT LISTENERS ---
        btnLuu.addActionListener(e -> save());
        btnHuy.addActionListener(e -> dispose());
    }

    private void addRow(JPanel parent, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = UIStyle.label(label);
        lbl.setPreferredSize(new java.awt.Dimension(95, 30));
        parent.add(lbl, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        parent.add(comp, gbc);
    }

    private void save() {
        String maNV = txtMaNV.getText().trim();
        String ten = txtTen.getText().trim();
        String gioiTinh = cboGioiTinh.getSelectedItem().toString();
        String cccd = txtCCCD.getText().trim();
        String matKhau = txtMatKhau.getText().trim();
        String sdt = txtSDT.getText().trim();
        String email = txtEmail.getText().trim();
        String vaiTro = cboChucVu.getSelectedItem().toString();
        String trangThai = cboTrangThai.getSelectedItem().toString();
        Date ngayVaoLam = (Date) spinNgayVaoLam.getValue();

        if (maNV.isEmpty() || ten.isEmpty() || matKhau.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ: Mã, Tên, Mật khẩu!");
            return;
        }

        NhanVien nv = new NhanVien(maNV, ten, gioiTinh, sdt, email, cccd, ngayVaoLam, trangThai);
        nv.setTaiKhoan(new TaiKhoan(maNV, matKhau, vaiTro));

        if (isEdit) {
            if (dao.update(nv)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi cập nhật!");
            }
        } else {
            if (dao.getByMaNV(maNV) != null) {
                JOptionPane.showMessageDialog(this, "Mã nhân viên đã tồn tại!");
                return;
            }
            if (dao.insert(nv)) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi thêm mới!");
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
