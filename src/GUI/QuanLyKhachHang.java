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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import DAO.KhachHangDAO;
import Entity.KhachHang;

public class QuanLyKhachHang extends JPanel {

    private JTextField txtSDT, txtTen, txtDiem, txtTim;
    private JTable table;
    private DefaultTableModel model;

    private KhachHangDAO dao = new KhachHangDAO();

    // Font chữ chuẩn
    Font fontLabel = new Font("Segoe UI", Font.BOLD, 14);
    Font fontInput = new Font("Segoe UI", Font.PLAIN, 14);

    public QuanLyKhachHang() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 1. FORM NHẬP LIỆU (TOP) ---
        JPanel pnlTop = new JPanel(new BorderLayout(10, 0));
        pnlTop.setBackground(Color.WHITE);
        pnlTop.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "THÔNG TIN KHÁCH HÀNG", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 16), new Color(0, 102, 204)));

        JPanel pnlInputs = new JPanel(new GridLayout(2, 4, 15, 15)); // 2 dòng, 4 cột (Input + Label)
        pnlInputs.setBackground(Color.WHITE);
        pnlInputs.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Dòng 1
        pnlInputs.add(createLabel("Số điện thoại:"));
        txtSDT = createTextField();
        pnlInputs.add(txtSDT);

        pnlInputs.add(createLabel("Tên khách hàng:"));
        txtTen = createTextField();
        pnlInputs.add(txtTen);

        // Dòng 2
        pnlInputs.add(createLabel("Điểm tích lũy:"));
        txtDiem = createTextField();
        txtDiem.setText("0"); // Mặc định là 0
        pnlInputs.add(txtDiem);

        // Ô trống để căn chỉnh
        pnlInputs.add(new JLabel(""));
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

        JLabel lblTim = new JLabel("Tìm kiếm (SĐT/Tên): ");
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
        JButton btnTinhLai = createButton("TÍNH LẠI ĐIỂM", new Color(100, 50, 150)); // Purple

        pnlBottom.add(btnThem);
        pnlBottom.add(btnSua);
        pnlBottom.add(btnXoa);
        pnlBottom.add(btnMoi);
        pnlBottom.add(btnTinhLai);

        add(pnlBottom, BorderLayout.SOUTH);

        // --- XỬ LÝ SỰ KIỆN ---
        loadData();

        // Click bảng -> Đổ dữ liệu lên form
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtSDT.setText(table.getValueAt(row, 0).toString());
                    txtTen.setText(table.getValueAt(row, 1).toString());
                    txtDiem.setText(table.getValueAt(row, 2).toString());
                    txtSDT.setEditable(false); // Khóa SĐT không cho sửa khi đang xem
                }
            }
        });

        // Nút Thêm
        btnThem.addActionListener(e -> {
            KhachHang kh = getForm();
            if (kh != null) {
                if (dao.insert(kh)) {
                    JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!");
                    loadData();
                    clearForm();
                } else
                    JOptionPane.showMessageDialog(this, "Số điện thoại này đã tồn tại!");
            }
        });

        // Nút Sửa
        btnSua.addActionListener(e -> {
            KhachHang kh = getForm();
            if (kh == null)
                return;

            if (dao.update(kh)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật!");
            }
        });

        // Nút Xóa
        btnXoa.addActionListener(e -> {
            String sdt = txtSDT.getText();
            if (sdt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa!");
                return;
            }
            if (JOptionPane.showConfirmDialog(this,
                    "Bạn chắc chắn muốn xóa khách hàng " + txtTen.getText() + "?") == 0) {
                if (dao.delete(sdt)) {
                    loadData();
                    clearForm();
                }
            }
        });

        // Nút Làm mới
        btnMoi.addActionListener(e -> clearForm());

        // Nút Tính Lại Điểm
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

        // Tìm kiếm (Live Search)
        txtTim.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                loadDataTimKiem(txtTim.getText());
            }
        });

        btnTim.addActionListener(e -> loadDataTimKiem(txtTim.getText()));

        // Tự động làm mới khi chuyển tab
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadData();
            }
        });
    }

    // ================== HÀM HỖ TRỢ ==================

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

    // Hàm thêm dòng vào bảng
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
                uuDai // Giờ đã khớp với 5 headers
        });
    }

    KhachHang getForm() {
        String sdt = txtSDT.getText().trim();
        String ten = txtTen.getText().trim();
        if (sdt.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ SĐT và Tên!");
            return null;
        }
        int diem = 0;
        try {
            diem = Integer.parseInt(txtDiem.getText());
        } catch (Exception e) {
        }

        // Use basic constructor for now
        return new KhachHang(sdt, ten, diem);
    }

    void clearForm() {
        txtSDT.setText("");
        txtTen.setText("");
        txtDiem.setText("0");
        txtSDT.setEditable(true);
        table.clearSelection();
        txtSDT.requestFocus();
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