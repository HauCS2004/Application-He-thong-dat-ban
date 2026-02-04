package GUI;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;

import DAO.KhuyenMaiDAO;
import Entity.KhuyenMai;

public class ManHinhKhuyenMai extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtMaKM, txtTenKM, txtGiaTri, txtDieuKien;
    private JComboBox<String> cboLoaiKM, cboTrangThai, cboHangVIP;
    private JDateChooser dateBatDau, dateKetThuc;
    private KhuyenMaiDAO kmDAO;

    public ManHinhKhuyenMai() {
        kmDAO = new KhuyenMaiDAO();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245));

        // Header
        JPanel pnlHeader = new JPanel();
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 20, 15, 20));
        pnlHeader.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel lblTitle = new JLabel("QUẢN LÝ KHUYẾN MÃI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(44, 62, 80));
        pnlHeader.add(lblTitle);

        add(pnlHeader, BorderLayout.NORTH);

        // Center: Info Panel & Table
        JPanel pnlCenter = new JPanel(new BorderLayout(10, 10));
        pnlCenter.setBorder(new EmptyBorder(10, 20, 20, 20));
        pnlCenter.setBackground(new Color(240, 242, 245));

        // -- Info Panel
        JPanel pnlInfo = new JPanel();
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setBorder(new TitledBorder(null, "Thông tin khuyến mãi", TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)));
        pnlInfo.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 1
        gbc.gridx = 0;
        gbc.gridy = 0;
        pnlInfo.add(new JLabel("Mã KM:"), gbc);
        gbc.gridx = 1;
        txtMaKM = new JTextField(20);
        pnlInfo.add(txtMaKM, gbc);

        gbc.gridx = 2;
        pnlInfo.add(new JLabel("Tên KM:"), gbc);
        gbc.gridx = 3;
        txtTenKM = new JTextField(20);
        pnlInfo.add(txtTenKM, gbc);

        gbc.gridx = 4;
        pnlInfo.add(new JLabel("Loại KM:"), gbc);
        gbc.gridx = 5;
        cboLoaiKM = new JComboBox<>(new String[] { "Giảm %", "Giảm tiền", "Tặng món" });
        pnlInfo.add(cboLoaiKM, gbc);

        // Row 2
        gbc.gridx = 0;
        gbc.gridy = 1;
        pnlInfo.add(new JLabel("Giá trị:"), gbc);
        gbc.gridx = 1;
        txtGiaTri = new JTextField(20);
        pnlInfo.add(txtGiaTri, gbc);

        gbc.gridx = 2;
        pnlInfo.add(new JLabel("Điều kiện tối thiểu:"), gbc);
        gbc.gridx = 3;
        txtDieuKien = new JTextField(20);
        pnlInfo.add(txtDieuKien, gbc);

        gbc.gridx = 4;
        pnlInfo.add(new JLabel("Hạng VIP áp dụng:"), gbc);
        gbc.gridx = 5;
        cboHangVIP = new JComboBox<>(new String[] { "Tất cả", "Bạc", "Vàng", "Kim cương" });
        pnlInfo.add(cboHangVIP, gbc);

        // Row 3
        gbc.gridx = 0;
        gbc.gridy = 2;
        pnlInfo.add(new JLabel("Ngày bắt đầu:"), gbc);
        gbc.gridx = 1;
        dateBatDau = new JDateChooser();
        dateBatDau.setDateFormatString("dd/MM/yyyy");
        pnlInfo.add(dateBatDau, gbc);

        gbc.gridx = 2;
        pnlInfo.add(new JLabel("Ngày kết thúc:"), gbc);
        gbc.gridx = 3;
        dateKetThuc = new JDateChooser();
        dateKetThuc.setDateFormatString("dd/MM/yyyy");
        pnlInfo.add(dateKetThuc, gbc);

        gbc.gridx = 4;
        pnlInfo.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 5;
        cboTrangThai = new JComboBox<>(new String[] { "Đang hoạt động", "Tạm ngưng", "Đã kết thúc" });
        pnlInfo.add(cboTrangThai, gbc);

        // -- Buttons Panel
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlButtons.setBackground(Color.WHITE);

        JButton btnAdd = createButton("Thêm", new Color(46, 204, 113));
        JButton btnUpdate = createButton("Sửa", new Color(52, 152, 219));
        JButton btnDelete = createButton("Xóa", new Color(231, 76, 60));
        JButton btnClear = createButton("Làm mới", new Color(149, 165, 166));

        btnAdd.addActionListener(e -> addKhuyenMai());
        btnUpdate.addActionListener(e -> updateKhuyenMai());
        btnDelete.addActionListener(e -> deleteKhuyenMai());
        btnClear.addActionListener(e -> clearForm());

        pnlButtons.add(btnAdd);
        pnlButtons.add(btnUpdate);
        pnlButtons.add(btnDelete);
        pnlButtons.add(btnClear);

        // Combine Info & Buttons
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.add(pnlInfo, BorderLayout.CENTER);
        pnlTop.add(pnlButtons, BorderLayout.SOUTH);

        pnlCenter.add(pnlTop, BorderLayout.NORTH);

        // -- Table Panel
        String[] headers = { "Mã KM", "Tên KM", "Loại", "Giá Trị", "Điều Kiện", "Bắt Đầu", "Kết Thúc", "VIP",
                "Trạng Thái" };
        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(236, 240, 241));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    fillForm(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        pnlCenter.add(scrollPane, BorderLayout.CENTER);

        add(pnlCenter, BorderLayout.CENTER);
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(100, 35));
        return btn;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        ArrayList<KhuyenMai> list = kmDAO.getAllKhuyenMai();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (KhuyenMai km : list) {
            tableModel.addRow(new Object[] {
                    km.getMaKM(),
                    km.getTenKM(),
                    km.getLoaiKM(),
                    String.format("%,.0f", km.getGiaTri()),
                    String.format("%,.0f", km.getDieuKienToiThieu()),
                    km.getNgayBatDau() != null ? sdf.format(km.getNgayBatDau()) : "",
                    km.getNgayKetThuc() != null ? sdf.format(km.getNgayKetThuc()) : "",
                    km.getHangVIPApDung() == null ? "Tất cả" : km.getHangVIPApDung(),
                    km.getTrangThai()
            });
        }
    }

    private void fillForm(int row) {
        txtMaKM.setText(tableModel.getValueAt(row, 0).toString());
        txtTenKM.setText(tableModel.getValueAt(row, 1).toString());
        cboLoaiKM.setSelectedItem(tableModel.getValueAt(row, 2).toString());

        String giaTri = tableModel.getValueAt(row, 3).toString().replace(".", "").replace(",", "");
        txtGiaTri.setText(giaTri);

        String dieuKien = tableModel.getValueAt(row, 4).toString().replace(".", "").replace(",", "");
        txtDieuKien.setText(dieuKien);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String sDate = tableModel.getValueAt(row, 5).toString();
            String eDate = tableModel.getValueAt(row, 6).toString();
            if (!sDate.isEmpty())
                dateBatDau.setDate(sdf.parse(sDate));
            if (!eDate.isEmpty())
                dateKetThuc.setDate(sdf.parse(eDate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String vip = tableModel.getValueAt(row, 7).toString();
        cboHangVIP.setSelectedItem(vip);

        cboTrangThai.setSelectedItem(tableModel.getValueAt(row, 8).toString());

        txtMaKM.setEditable(false);
    }

    private KhuyenMai getForm() {
        String ma = txtMaKM.getText().trim();
        String ten = txtTenKM.getText().trim();
        String loai = cboLoaiKM.getSelectedItem().toString();
        String vip = cboHangVIP.getSelectedItem().toString();
        if (vip.equals("Tất cả"))
            vip = null;
        String trangThai = cboTrangThai.getSelectedItem().toString();

        if (ma.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return null;
        }

        double giaTri = 0, dieuKien = 0;
        try {
            giaTri = Double.parseDouble(txtGiaTri.getText().trim());
            dieuKien = Double.parseDouble(txtDieuKien.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá trị và điều kiện phải là số!");
            return null;
        }

        Date start = dateBatDau.getDate();
        Date end = dateKetThuc.getDate();

        if (start == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày bắt đầu!");
            return null;
        }

        if (end != null && end.before(start)) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc phải sau ngày bắt đầu!");
            return null;
        }

        return new KhuyenMai(ma, ten, loai, giaTri, dieuKien, start, end, trangThai, vip);
    }

    private void clearForm() {
        txtMaKM.setText("");
        txtMaKM.setEditable(true);
        txtTenKM.setText("");
        txtGiaTri.setText("");
        txtDieuKien.setText("");
        dateBatDau.setDate(null);
        dateKetThuc.setDate(null);
        cboLoaiKM.setSelectedIndex(0);
        cboHangVIP.setSelectedIndex(0);
        cboTrangThai.setSelectedIndex(0);
        table.clearSelection();
    }

    private void addKhuyenMai() {
        KhuyenMai km = getForm();
        if (km != null) {
            if (kmDAO.insert(km)) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                loadData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm thất bại! Mã KM có thể đã tồn tại.");
            }
        }
    }

    private void updateKhuyenMai() {
        KhuyenMai km = getForm();
        if (km != null) {
            if (kmDAO.update(km)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
            }
        }
    }

    private void deleteKhuyenMai() {
        String ma = txtMaKM.getText().trim();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khuyến mãi cần xóa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xóa khuyến mãi này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (kmDAO.delete(ma)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!");
            }
        }
    }
}
