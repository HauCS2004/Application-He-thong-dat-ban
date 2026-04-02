package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import DAO.KhachHangDAO;
import Entity.KhachHang;

public class QuanLyKhachHang extends JPanel {

    private JTextField txtTim;
    private JTable table;
    private DefaultTableModel model;

    private KhachHangDAO dao = new KhachHangDAO();

    Font fontLabel = new Font("Segoe UI", Font.BOLD, 14);
    Font fontInput = new Font("Segoe UI", Font.PLAIN, 14);

    public QuanLyKhachHang() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- TOOLBAR (NORTH) ---
        JPanel pnlToolbar = new JPanel(new BorderLayout());
        pnlToolbar.setBackground(new Color(243, 244, 246));
        pnlToolbar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSearch.setBackground(new Color(243, 244, 246));
        JLabel lblTim = new JLabel("Tìm kiếm (SĐT/Tên): ");
        lblTim.setFont(fontLabel);
        pnlSearch.add(lblTim);

        txtTim = new JTextField(25);
        txtTim.setFont(fontInput);
        txtTim.setPreferredSize(new Dimension(200, 35));
        pnlSearch.add(txtTim);

        JButton btnTim = createButton("Tìm", new Color(52, 152, 219)); // Blue
        pnlSearch.add(btnTim);

        // Action Buttons
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setBackground(new Color(243, 244, 246));
        
        JButton btnThem = createButton("THÊM", new Color(46, 204, 113));
        JButton btnSua = createButton("SỬA", new Color(245, 158, 11));
        JButton btnXoa = createButton("XÓA", new Color(220, 53, 69));
        JButton btnTinhLai = createButton("TÍNH LẠI ĐIỂM", new Color(100, 50, 150));

        pnlActions.add(btnThem);
        pnlActions.add(btnSua);
        pnlActions.add(btnXoa);
        pnlActions.add(btnTinhLai);

        pnlToolbar.add(pnlSearch, BorderLayout.WEST);
        pnlToolbar.add(pnlActions, BorderLayout.EAST);
        add(pnlToolbar, BorderLayout.NORTH);

        // --- BẢNG DỮ LIỆU (CENTER) ---
        String[] headers = { "Số điện thoại", "Tên khách hàng", "Điểm tích lũy", "Hạng thành viên", "Ưu đãi" };
        model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(fontInput);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getTableHeader().setForeground(Color.BLACK);
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(scroll, BorderLayout.CENTER);

        // --- XỬ LÝ SỰ KIỆN ---
        loadData();

        btnThem.addActionListener(e -> {
            KhachHangDialog dialog = new KhachHangDialog(this, null, dao);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadData();
            }
        });

        btnSua.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần sửa!");
                return;
            }
            String sdt = table.getValueAt(row, 0).toString();
            String ten = table.getValueAt(row, 1).toString();
            int diem = Integer.parseInt(table.getValueAt(row, 2).toString());
            
            KhachHang kh = new KhachHang(sdt, ten, diem);
            
            KhachHangDialog dialog = new KhachHangDialog(this, kh, dao);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadData();
            }
        });

        btnXoa.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa!");
                return;
            }
            String sdt = table.getValueAt(row, 0).toString();
            String ten = table.getValueAt(row, 1).toString();

            if (JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xóa khách hàng " + ten + "?") == JOptionPane.YES_OPTION) {
                if (dao.delete(sdt)) {
                    JOptionPane.showMessageDialog(this, "Đã ngừng hoạt động khách hàng thành công!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa!");
                }
            }
        });

        btnTinhLai.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn tính lại điểm cho TẤT CẢ khách hàng dựa trên lịch sử hóa đơn?\nThao tác này sẽ ghi đè điểm hiện tại.",
                    "Xác nhận tính lại điểm", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (dao.resetVaTinhLaiDiem()) {
                    JOptionPane.showMessageDialog(this, "Đã tính lại điểm thành công!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi tính lại điểm!");
                }
            }
        });

        txtTim.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                loadDataTimKiem(txtTim.getText());
            }
        });

        btnTim.addActionListener(e -> loadDataTimKiem(txtTim.getText()));

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadData();
            }
        });
    }

    void loadData() {
        model.setRowCount(0);
        for (KhachHang kh : dao.getAll()) {
            addModel(kh);
        }
    }

    void loadDataTimKiem(String keyword) {
        model.setRowCount(0);
        for (KhachHang kh : dao.timKiem(keyword)) {
            addModel(kh);
        }
    }

    void addModel(KhachHang kh) {
        String hang = kh.getHangThanhVien();
        String uuDai = "Giảm " + kh.getPhanTramGiam() + "%";
        if (kh.getPhanTramGiam() == 0)
            uuDai = "Không";

        model.addRow(new Object[] {
                kh.getSoDienThoai(),
                kh.getTenKhach(),
                kh.getDiemTichLuy(),
                hang,
                uuDai
        });
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(130, 35));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }
}