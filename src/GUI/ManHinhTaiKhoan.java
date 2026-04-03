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

public class ManHinhTaiKhoan extends JPanel {

    private JTextField txtTim;
    private JComboBox<String> cboTrangThai;
    private JTable table;
    private DefaultTableModel model;

    private NhanVienDAO dao = new NhanVienDAO();

    Font fontLabel = new Font("Segoe UI", Font.BOLD, 14);
    Font fontInput = new Font("Segoe UI", Font.PLAIN, 14);

    public ManHinhTaiKhoan() {
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

        JButton btnTim = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.PRIMARY, "Tìm Kiếm");
        pnlSearch.add(btnTim);

        // Action Buttons
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setBackground(new Color(243, 244, 246));
        
        JButton btnSua = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.WARNING, "SỬA MẬT KHẨU");

        pnlActions.add(btnSua);

        pnlToolbar.add(pnlSearch, BorderLayout.WEST);
        pnlToolbar.add(pnlActions, BorderLayout.EAST);
        add(pnlToolbar, BorderLayout.NORTH);

        // --- BẢNG DỮ LIỆU (CENTER) ---
        String[] headers = { "Mã NV / Tài khoản", "Tên nhân viên", "Vai trò", "Mật khẩu" };
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

        btnSua.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản cần sửa mật khẩu!");
                return;
            }
            String maNV = table.getValueAt(row, 0).toString();
            String tenNV = table.getValueAt(row, 1).toString();
            
            String newPass = JOptionPane.showInputDialog(this, "Nhập mật khẩu mới cho nhân viên " + tenNV + " (" + maNV + "):", "Đổi mật khẩu", JOptionPane.PLAIN_MESSAGE);
            if (newPass != null && !newPass.trim().isEmpty()) {
                if (dao.updatePasswordAdmin(maNV, newPass.trim())) {
                    JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!");
                    loadData(); // refresh
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi đổi mật khẩu!");
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
        for (NhanVien nv : dao.getAllWithPassword(trangThai)) {
            addModel(nv);
        }
    }

    void loadDataTimKiem(String keyword) {
        model.setRowCount(0);
        String trangThai = cboTrangThai != null ? cboTrangThai.getSelectedItem().toString() : "Đang làm việc";
        for (NhanVien nv : dao.timKiemWithPassword(keyword, trangThai)) {
            addModel(nv);
        }
    }

    void addModel(NhanVien nv) {
        String vaiTro = (nv.getTaiKhoan() != null) ? nv.getTaiKhoan().getVaiTro() : "Nhân viên";
        String matKhau = (nv.getTaiKhoan() != null) ? nv.getTaiKhoan().getMatKhau() : "";
        model.addRow(new Object[] {
                nv.getMaNV(),
                nv.getTenNV(),
                vaiTro,
                matKhau
        });
    }}
