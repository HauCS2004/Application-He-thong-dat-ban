package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import DAO.NhanVienDAO;
import Entity.NhanVien;
import Entity.TaiKhoan;
import connectDB.SessionManager;

public class ManHinhNhanVien extends JPanel {

    private JTextField txtMaNV, txtTen, txtMatKhau, txtSDT, txtEmail;
    private JComboBox<String> cboChucVu;
    private JSpinner spinNgayVaoLam; // Date Picker
    private JTextField txtTim;
    private JTable table;
    private DefaultTableModel model;

    private NhanVienDAO dao = new NhanVienDAO();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    // Font chữ chuẩn
    Font fontLabel = new Font("Segoe UI", Font.BOLD, 14);
    Font fontInput = new Font("Segoe UI", Font.PLAIN, 14);

    public ManHinhNhanVien() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 1. FORM NHẬP LIỆU (TOP) ---
        JPanel pnlTop = new JPanel(new BorderLayout(10, 0));
        pnlTop.setBackground(Color.WHITE);
        pnlTop.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "THÔNG TIN NHÂN VIÊN", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 16), new Color(0, 102, 204)));

        JPanel pnlInputs = new JPanel(new GridLayout(4, 4, 15, 10)); // 4 rows
        pnlInputs.setBackground(Color.WHITE);
        pnlInputs.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Dòng 1
        pnlInputs.add(createLabel("Mã nhân viên:"));
        txtMaNV = createTextField();
        pnlInputs.add(txtMaNV);

        pnlInputs.add(createLabel("Tên nhân viên:"));
        txtTen = createTextField();
        pnlInputs.add(txtTen);

        // Dòng 2
        pnlInputs.add(createLabel("Mật khẩu:"));
        txtMatKhau = createTextField();
        pnlInputs.add(txtMatKhau);

        pnlInputs.add(createLabel("Chức vụ / Vai trò:"));
        cboChucVu = new JComboBox<>(new String[] { "Nhân viên", "Quản lý" });
        cboChucVu.setFont(fontInput);
        cboChucVu.setBackground(Color.WHITE);
        pnlInputs.add(cboChucVu);

        // Dòng 3
        pnlInputs.add(createLabel("Số điện thoại:"));
        txtSDT = createTextField();
        pnlInputs.add(txtSDT);

        pnlInputs.add(createLabel("Email:"));
        txtEmail = createTextField();
        pnlInputs.add(txtEmail);

        // Dòng 4
        pnlInputs.add(createLabel("Ngày vào làm:"));
        spinNgayVaoLam = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinNgayVaoLam, "yyyy-MM-dd");
        spinNgayVaoLam.setEditor(dateEditor);
        spinNgayVaoLam.setFont(fontInput);
        pnlInputs.add(spinNgayVaoLam);

        pnlInputs.add(new JLabel(""));

        pnlTop.add(pnlInputs, BorderLayout.CENTER);
        add(pnlTop, BorderLayout.NORTH);

        // --- 2. THANH CÔNG CỤ & BẢNG (CENTER) ---
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 10));
        pnlCenter.setBackground(Color.WHITE);

        // Thanh tìm kiếm
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSearch.setBackground(new Color(243, 244, 246));
        pnlSearch.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel lblTim = new JLabel("Tìm kiếm (Tên/SĐT/Mã): ");
        lblTim.setFont(fontLabel);
        pnlSearch.add(lblTim);

        txtTim = new JTextField(25);
        txtTim.setFont(fontInput);
        txtTim.setPreferredSize(new Dimension(200, 35));
        pnlSearch.add(txtTim);

        JButton btnTim = createButton("Tìm", new Color(52, 152, 219)); // Blue
        btnTim.setPreferredSize(new Dimension(100, 35));
        pnlSearch.add(btnTim);

        pnlCenter.add(pnlSearch, BorderLayout.NORTH);

        // Bảng dữ liệu
        String[] headers = { "Mã NV", "Tên nhân viên", "Chức vụ", "SĐT", "Email", "Ngày vào làm" };
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
        scroll.setBorder(BorderFactory.createEmptyBorder());
        pnlCenter.add(scroll, BorderLayout.CENTER);

        add(pnlCenter, BorderLayout.CENTER);

        // --- 3. NÚT CHỨC NĂNG (BOTTOM) ---
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pnlBottom.setBackground(Color.WHITE);

        JButton btnThem = createButton("THÊM", new Color(0, 102, 204));
        JButton btnSua = createButton("SỬA", new Color(255, 193, 7)); // Yellow/Orange
        btnSua.setForeground(Color.BLACK);
        JButton btnXoa = createButton("XÓA", new Color(220, 53, 69)); // Red
        JButton btnMoi = createButton("LÀM MỚI", new Color(108, 117, 125)); // Grey

        pnlBottom.add(btnThem);
        pnlBottom.add(btnSua);
        pnlBottom.add(btnXoa);
        pnlBottom.add(btnMoi);

        add(pnlBottom, BorderLayout.SOUTH);

        // --- XỬ LÝ SỰ KIỆN ---
        loadData();

        // Click bảng -> Đổ dữ liệu lên form
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    fillForm(row);
                }
            }
        });

        // Nút Thêm
        btnThem.addActionListener(e -> {
            NhanVien nv = getForm();
            if (nv != null) {
                // Validate duplicate
                if (dao.getByMaNV(nv.getMaNV()) != null) {
                    JOptionPane.showMessageDialog(this, "Mã nhân viên đã tồn tại!");
                    return;
                }
                if (dao.insert(nv)) {
                    JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
                    loadData();
                    clearForm();
                } else
                    JOptionPane.showMessageDialog(this, "Lỗi khi thêm nhân viên!");
            }
        });

        // Nút Sửa
        btnSua.addActionListener(e -> {
            NhanVien nv = getForm();
            if (nv == null)
                return;

            if (dao.update(nv)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật!");
            }
        });

        // Nút Xóa
        btnXoa.addActionListener(e -> {
            String maNV = txtMaNV.getText();
            if (maNV.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xóa!");
                return;
            }
            // Prevent deleting self
            if (SessionManager.getCurrentUser() != null && SessionManager.getCurrentUser().getMaNV().equals(maNV)) {
                JOptionPane.showMessageDialog(this, "Không thể xóa chính tài khoản đang đăng nhập!");
                return;
            }

            if (JOptionPane.showConfirmDialog(this,
                    "Bạn chắc chắn muốn xóa nhân viên " + txtTen.getText() + "?") == 0) {
                if (dao.delete(maNV)) {
                    loadData();
                    clearForm();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa!");
                }
            }
        });

        // Nút Làm mới
        btnMoi.addActionListener(e -> clearForm());

        // Tìm kiếm (Live Search)
        txtTim.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                loadDataTimKiem(txtTim.getText());
            }
        });

        btnTim.addActionListener(e -> loadDataTimKiem(txtTim.getText()));
    }

    // ================== HÀM HỖ TRỢ ==================

    void loadData() {
        model.setRowCount(0);
        for (NhanVien nv : dao.getAll()) {
            addModel(nv);
        }
    }

    void loadDataTimKiem(String keyword) {
        model.setRowCount(0);
        for (NhanVien nv : dao.timKiem(keyword)) {
            addModel(nv);
        }
    }

    void addModel(NhanVien nv) {
        String vaiTro = (nv.getTaiKhoan() != null) ? nv.getTaiKhoan().getVaiTro() : "Nhân viên";
        model.addRow(new Object[] {
                nv.getMaNV(),
                nv.getTenNV(),
                vaiTro,
                nv.getSoDienThoai(),
                nv.getEmail(),
                nv.getNgayVaoLam() != null ? sdf.format(nv.getNgayVaoLam()) : ""
        });
    }

    void fillForm(int row) {
        String maNV = table.getValueAt(row, 0).toString();
        NhanVien nv = dao.getByMaNV(maNV);
        if (nv != null) {
            txtMaNV.setText(nv.getMaNV());
            txtTen.setText(nv.getTenNV());
            // Hiển thị mật khẩu từ TaiKhoan
            if (nv.getTaiKhoan() != null) {
                txtMatKhau.setText(nv.getTaiKhoan().getMatKhau());
                cboChucVu.setSelectedItem(nv.getTaiKhoan().getVaiTro());
            }
            txtSDT.setText(nv.getSoDienThoai());
            txtEmail.setText(nv.getEmail());
            spinNgayVaoLam.setValue(nv.getNgayVaoLam() != null ? nv.getNgayVaoLam() : new Date());
            txtMaNV.setEditable(false);
        }
    }

    NhanVien getForm() {
        String maNV    = txtMaNV.getText().trim();
        String ten     = txtTen.getText().trim();
        String matKhau = txtMatKhau.getText().trim();
        String sdt     = txtSDT.getText().trim();
        String email   = txtEmail.getText().trim();
        String vaiTro  = cboChucVu.getSelectedItem().toString();
        Date   ngayVaoLam = (Date) spinNgayVaoLam.getValue();

        if (maNV.isEmpty() || ten.isEmpty() || matKhau.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ: Mã, Tên, Mật khẩu!");
            return null;
        }

        // Tạo NhanVien + TaiKhoan liên kết
        NhanVien nv = new NhanVien(maNV, ten, sdt, email, ngayVaoLam);
        nv.setTaiKhoan(new TaiKhoan(maNV, matKhau, vaiTro));
        return nv;
    }

    void clearForm() {
        txtMaNV.setText("");
        txtTen.setText("");
        txtMatKhau.setText("");
        txtSDT.setText("");
        txtEmail.setText("");
        cboChucVu.setSelectedIndex(0);
        spinNgayVaoLam.setValue(new Date());

        txtMaNV.setEditable(true);
        table.clearSelection();
        txtMaNV.requestFocus();
    }

    // Helper tạo component nhanh
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(fontLabel);
        return lbl;
    }

    private JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setFont(fontInput);
        return txt;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(130, 40));
        btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }
}
