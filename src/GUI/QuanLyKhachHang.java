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
import java.util.ArrayList;

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
    Font fontLabel = new Font("Segoe UI", Font.BOLD, 16);
    Font fontInput = new Font("Segoe UI", Font.PLAIN, 16);

    public QuanLyKhachHang() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 1. FORM NHẬP LIỆU (TOP) ---
        JPanel pnlTop = new JPanel(new BorderLayout(10, 0));
        pnlTop.setBackground(Color.WHITE);
        pnlTop.setPreferredSize(new Dimension(0, 200));
        pnlTop.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                "THÔNG TIN KHÁCH HÀNG", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
                new Font("Segoe UI", Font.BOLD, 18), Color.BLUE));

        JPanel pnlInputs = new JPanel(new GridLayout(2, 4, 15, 15)); // 2 dòng, 4 cột
        pnlInputs.setBackground(Color.WHITE);
        pnlInputs.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Dòng 1
        pnlInputs.add(createLabel("Số điện thoại:"));
        txtSDT = createTextField(); pnlInputs.add(txtSDT);
        
        pnlInputs.add(createLabel("Tên khách hàng:"));
        txtTen = createTextField(); pnlInputs.add(txtTen);

        // Dòng 2
        pnlInputs.add(createLabel("Điểm tích lũy:"));
        txtDiem = createTextField(); 
        txtDiem.setText("0"); // Mặc định là 0
        pnlInputs.add(txtDiem);
        
        // Ô trống để căn chỉnh cho đẹp
        pnlInputs.add(new JLabel("")); pnlInputs.add(new JLabel(""));

        pnlTop.add(pnlInputs, BorderLayout.CENTER);
        add(pnlTop, BorderLayout.NORTH);

        // --- 2. THANH CÔNG CỤ & BẢNG (CENTER) ---
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 10));
        pnlCenter.setBackground(Color.WHITE);

        // Thanh tìm kiếm
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSearch.setBackground(new Color(240, 248, 255));
        pnlSearch.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        pnlSearch.add(createLabel("Tìm kiếm (SĐT/Tên): "));
        txtTim = new JTextField(20);
        txtTim.setFont(fontInput);
        pnlSearch.add(txtTim);
        
        JButton btnTim = createButton("Tìm", new Color(46, 204, 113));
        btnTim.setPreferredSize(new Dimension(100, 35));
        pnlSearch.add(btnTim);

        pnlCenter.add(pnlSearch, BorderLayout.NORTH);

        // Bảng dữ liệu
        String[] headers = {"Số điện thoại", "Tên khách hàng", "Điểm tích lũy", "Hạng thành viên"};
        model = new DefaultTableModel(headers, 0);
        table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(fontInput);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(0, 102, 204));
        table.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scroll = new JScrollPane(table);
        pnlCenter.add(scroll, BorderLayout.CENTER);
        
        add(pnlCenter, BorderLayout.CENTER);

        // --- 3. NÚT CHỨC NĂNG (BOTTOM) ---
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pnlBottom.setBackground(Color.WHITE);
        
        JButton btnThem = createButton("THÊM", new Color(0, 102, 204));
        JButton btnSua = createButton("SỬA", new Color(0, 102, 204));
        JButton btnXoa = createButton("XÓA", new Color(220, 53, 69)); // Màu đỏ
        JButton btnMoi = createButton("LÀM MỚI", new Color(23, 162, 184));

        pnlBottom.add(btnThem); pnlBottom.add(btnSua);
        pnlBottom.add(btnXoa); pnlBottom.add(btnMoi);
        
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
                if(dao.insert(kh)) {
                    JOptionPane.showMessageDialog(this, "Thêm thành công!");
                    loadData(); clearForm();
                } else JOptionPane.showMessageDialog(this, "Trùng số điện thoại!");
            }
        });

        // Nút Sửa
        btnSua.addActionListener(e -> {
            if(dao.update(getForm())) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadData(); clearForm();
            }
        });

        // Nút Xóa
        btnXoa.addActionListener(e -> {
            String sdt = txtSDT.getText();
            if(JOptionPane.showConfirmDialog(this, "Xóa khách hàng này?") == 0) {
                if(dao.delete(sdt)) {
                    loadData(); clearForm();
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

    // Hàm thêm dòng vào bảng (Có logic tính hạng thành viên)
    void addModel(KhachHang kh) {
        // Gọi hàm thông minh bên Entity
        String hang = kh.getHangThanhVien(); 
        String uuDai = "Giảm " + kh.getPhanTramGiam() + "%";
        
        // Nếu là khách thường thì không hiện chữ "Giảm 0%" cho đỡ quê
        if(kh.getPhanTramGiam() == 0) uuDai = "Không có";

        model.addRow(new Object[]{
            kh.getSoDienThoai(),
            kh.getTenKhach(),
            kh.getDiemTichLuy(),
            hang,    // Cột Hạng
            uuDai    // Cột Ưu đãi
        });
    }
    KhachHang getForm() {
        String sdt = txtSDT.getText().trim();
        String ten = txtTen.getText().trim();
        if(sdt.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ SĐT và Tên!");
            return null;
        }
        int diem = 0;
        try { diem = Integer.parseInt(txtDiem.getText()); } catch(Exception e){}
        return new KhachHang(sdt, ten, diem);
    }

    void clearForm() {
        txtSDT.setText(""); txtTen.setText(""); txtDiem.setText("0");
        txtSDT.setEditable(true); // Mở lại cho nhập
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(140, 45));
        return btn;
    }
}