package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import DAO.NhanVienDAO;
import Entity.NhanVien;
import connectDB.SessionManager;

public class ManHinhNhanVien extends JPanel {

    private JTextField txtTim;
    private JComboBox<String> cboTrangThai;
    private JTable table;
    private DefaultTableModel model;

    private NhanVienDAO dao = new NhanVienDAO();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    Font fontLabel = new Font("Segoe UI", Font.BOLD, 14);
    Font fontInput = new Font("Segoe UI", Font.PLAIN, 14);

    public ManHinhNhanVien() {
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
        JLabel lblTim = new JLabel("Tìm kiếm (Tên/SĐT/Mã): ");
        lblTim.setFont(fontLabel);
        pnlSearch.add(lblTim);

        txtTim = new JTextField(25);
        txtTim.setFont(fontInput);
        txtTim.setPreferredSize(new Dimension(200, 35));
        pnlSearch.add(txtTim);

        cboTrangThai = new JComboBox<>(new String[]{"Tất cả", "Đang làm việc", "Đã nghỉ"});
        cboTrangThai.setFont(fontInput);
        cboTrangThai.setPreferredSize(new Dimension(150, 35));
        cboTrangThai.setSelectedItem("Đang làm việc");
        pnlSearch.add(cboTrangThai);

        JButton btnTim = createButton("Tìm", new Color(52, 152, 219));
        pnlSearch.add(btnTim);

        // Action Buttons
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setBackground(new Color(243, 244, 246));
        
        JButton btnThem = createButton("THÊM", new Color(46, 204, 113));
        JButton btnSua = createButton("SỬA", new Color(245, 158, 11));
        JButton btnXoa = createButton("XÓA", new Color(220, 53, 69));

        pnlActions.add(btnThem);
        pnlActions.add(btnSua);
        pnlActions.add(btnXoa);

        pnlToolbar.add(pnlSearch, BorderLayout.WEST);
        pnlToolbar.add(pnlActions, BorderLayout.EAST);
        add(pnlToolbar, BorderLayout.NORTH);

        // --- BẢNG DỮ LIỆU (CENTER) ---
        String[] headers = { "Mã NV", "Tên nhân viên", "Giới tính", "SĐT", "Email", "CCCD", "Chức vụ", "Trạng thái", "Ngày vào làm" };
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
            NhanVienDialog dialog = new NhanVienDialog(this, null, dao);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadData();
            }
        });

        btnSua.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa!");
                return;
            }
            String maNV = table.getValueAt(row, 0).toString();
            NhanVien nv = dao.getByMaNV(maNV);
            if (nv != null) {
                NhanVienDialog dialog = new NhanVienDialog(this, nv, dao);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    loadData();
                }
            }
        });

        btnXoa.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xóa!");
                return;
            }
            String maNV = table.getValueAt(row, 0).toString();
            String tenNV = table.getValueAt(row, 1).toString();

            if (SessionManager.getCurrentUser() != null && SessionManager.getCurrentUser().getMaNV().equals(maNV)) {
                JOptionPane.showMessageDialog(this, "Không thể xóa chính tài khoản đang đăng nhập!");
                return;
            }

            if (JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn cho nhân viên " + tenNV + " nghỉ việc (Vô hiệu hóa)?") == 0) {
                if (dao.delete(maNV)) {
                    JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái nhân viên thành Đã nghỉ!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật!");
                }
            }
        });

        txtTim.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                loadDataTimKiem(txtTim.getText());
            }
        });

        cboTrangThai.addActionListener(e -> {
            if (txtTim.getText().trim().isEmpty()) {
                loadData();
            } else {
                loadDataTimKiem(txtTim.getText());
            }
        });

        btnTim.addActionListener(e -> loadDataTimKiem(txtTim.getText()));

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (txtTim.getText().trim().isEmpty()) {
                    loadData();
                } else {
                    loadDataTimKiem(txtTim.getText());
                }
            }
        });
    }

    void loadData() {
        model.setRowCount(0);
        String trangThai = cboTrangThai != null ? cboTrangThai.getSelectedItem().toString() : "Đang làm việc";
        for (NhanVien nv : dao.getAll(trangThai)) {
            addModel(nv);
        }
    }

    void loadDataTimKiem(String keyword) {
        model.setRowCount(0);
        String trangThai = cboTrangThai != null ? cboTrangThai.getSelectedItem().toString() : "Đang làm việc";
        for (NhanVien nv : dao.timKiem(keyword, trangThai)) {
            addModel(nv);
        }
    }

    void addModel(NhanVien nv) {
        String vaiTro = (nv.getTaiKhoan() != null) ? nv.getTaiKhoan().getVaiTro() : "Nhân viên";
        model.addRow(new Object[] {
                nv.getMaNV(),
                nv.getTenNV(),
                nv.getGioiTinh() != null ? nv.getGioiTinh() : "Nam",
                nv.getSoDienThoai(),
                nv.getEmail(),
                nv.getCccd() != null ? nv.getCccd() : "",
                vaiTro,
                nv.getTrangThai() != null ? nv.getTrangThai() : "Đang làm việc",
                nv.getNgayVaoLam() != null ? sdf.format(nv.getNgayVaoLam()) : ""
        });
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(110, 35));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }
}
