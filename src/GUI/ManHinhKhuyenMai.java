package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import DAO.KhuyenMaiDAO;
import Entity.KhuyenMai;

public class ManHinhKhuyenMai extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private KhuyenMaiDAO kmDAO;

    public ManHinhKhuyenMai() {
        kmDAO = new KhuyenMaiDAO();
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245));

        // --- HEADER ---
        JPanel pnlHeader = new JPanel();
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 20, 15, 20));
        pnlHeader.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel lblTitle = new JLabel("QUẢN LÝ KHUYẾN MÃI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(44, 62, 80));
        pnlHeader.add(lblTitle);

        add(pnlHeader, BorderLayout.NORTH);

        // --- TOOLBAR & TABLE (CENTER) ---
        JPanel pnlCenter = new JPanel(new BorderLayout(10, 10));
        pnlCenter.setBorder(new EmptyBorder(10, 20, 20, 20));
        pnlCenter.setBackground(new Color(240, 242, 245));

        // Toolbar
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlToolbar.setBackground(new Color(240, 242, 245));

        JButton btnAdd = createButton("THÊM", new Color(46, 204, 113));
        JButton btnUpdate = createButton("SỬA", new Color(52, 152, 219));
        JButton btnDelete = createButton("XÓA", new Color(231, 76, 60));
        JButton btnClear = createButton("LÀM MỚI", new Color(149, 165, 166));

        pnlToolbar.add(btnAdd);
        pnlToolbar.add(btnUpdate);
        pnlToolbar.add(btnDelete);
        pnlToolbar.add(btnClear);

        pnlCenter.add(pnlToolbar, BorderLayout.NORTH);

        // Table
        String[] headers = { "Mã KM", "Tên KM", "Loại", "Giá Trị", "Điều Kiện", "Bắt Đầu", "Kết Thúc", "VIP", "Trạng Thái" };
        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(236, 240, 241));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));
        pnlCenter.add(scrollPane, BorderLayout.CENTER);

        add(pnlCenter, BorderLayout.CENTER);

        // --- EVENT LISTENERS ---
        btnAdd.addActionListener(e -> {
            KhuyenMaiDialog dialog = new KhuyenMaiDialog(this, null, kmDAO);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadData();
            }
        });

        btnUpdate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khuyến mãi cần sửa!");
                return;
            }
            String maKM = table.getValueAt(row, 0).toString();
            KhuyenMai km = kmDAO.getByMaKM(maKM);
            
            if (km != null) {
                KhuyenMaiDialog dialog = new KhuyenMaiDialog(this, km, kmDAO);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    loadData();
                }
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khuyến mãi cần xóa!");
                return;
            }
            String maKM = table.getValueAt(row, 0).toString();
            String tenKM = table.getValueAt(row, 1).toString();

            int confirm = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xóa khuyến mãi " + tenKM + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (kmDAO.delete(maKM)) {
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa thất bại!");
                }
            }
        });

        btnClear.addActionListener(e -> loadData());

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadData();
            }
        });
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(110, 35));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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
}
