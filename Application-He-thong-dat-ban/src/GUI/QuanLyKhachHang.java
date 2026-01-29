package GUI;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import DAO.KhachHangDAO;
import Entity.KhachHang;

public class QuanLyKhachHang extends JPanel {

    // Khai báo Component
    private JTextField txtSDT, txtTen, txtDiem, txtTim;
    private JTable table;
    private DefaultTableModel model;
    
    // Khai báo DAO
    private KhachHangDAO dao = new KhachHangDAO();
    
    // Font chữ & Màu sắc chuẩn
    Font fontLabel = new Font("Segoe UI", Font.BOLD, 14);
    Font fontInput = new Font("Segoe UI", Font.PLAIN, 14);
    Color colorPrimary = new Color(52, 152, 219); // Xanh dương
    Color colorSuccess = new Color(46, 204, 113); // Xanh lá
    Color colorDanger = new Color(231, 76, 60);   // Đỏ

    public QuanLyKhachHang() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- 1. FORM NHẬP LIỆU (TOP) ---
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setBackground(Color.WHITE);
        // Tạo viền có tiêu đề đẹp
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                " THÔNG TIN KHÁCH HÀNG ", 
                TitledBorder.DEFAULT_JUSTIFICATION, 
                TitledBorder.DEFAULT_POSITION, 
                new Font("Segoe UI", Font.BOLD, 16), 
                Color.BLUE
        );
        pnlTop.setBorder(titledBorder);
        pnlTop.setPreferredSize(new Dimension(0, 150));

        // Panel chứa các ô nhập liệu (Grid 2 dòng, 4 cột: Label - Input - Label - Input)
        JPanel pnlInputs = new JPanel(new GridLayout(2, 4, 20, 20)); 
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
        txtDiem.setText("0"); // Mặc định 0
        pnlInputs.add(txtDiem);
        
        // Ô trống để lấp đầy Grid cho đẹp
        pnlInputs.add(new JLabel("")); 
        pnlInputs.add(new JLabel(""));

        pnlTop.add(pnlInputs, BorderLayout.CENTER);
        add(pnlTop, BorderLayout.NORTH);

        // --- 2. THANH CÔNG CỤ & BẢNG (CENTER) ---
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 10));
        pnlCenter.setBackground(Color.WHITE);

        // Thanh tìm kiếm
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSearch.setBackground(new Color(245, 245, 245));
        pnlSearch.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        JLabel lblTim = createLabel("Tìm kiếm (SĐT/Tên): ");
        pnlSearch.add(lblTim);
        
        txtTim = new JTextField(25);
        txtTim.setFont(fontInput);
        pnlSearch.add(txtTim);
        
        JButton btnTim = createButton("Tìm", colorSuccess);
        btnTim.setPreferredSize(new Dimension(80, 30));
        pnlSearch.add(btnTim);

        pnlCenter.add(pnlSearch, BorderLayout.NORTH);

        // Bảng dữ liệu
        // [QUAN TRỌNG] Thêm cột "Ưu Đãi" cho khớp dữ liệu
        String[] headers = {"Số điện thoại", "Tên khách hàng", "Điểm tích lũy", "Hạng thành viên", "Ưu đãi VIP"};
        model = new DefaultTableModel(headers, 0) {
            @Override // Không cho sửa trực tiếp trên bảng
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(fontInput);
        
        // Style Header bảng
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(colorPrimary);
        table.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scroll = new JScrollPane(table);
        pnlCenter.add(scroll, BorderLayout.CENTER);
        
        add(pnlCenter, BorderLayout.CENTER);

        // --- 3. NÚT CHỨC NĂNG (BOTTOM) ---
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton btnThem = createButton("THÊM MỚI", colorPrimary);
        JButton btnSua = createButton("CẬP NHẬT", new Color(243, 156, 18)); // Màu Cam
        JButton btnXoa = createButton("XÓA KHÁCH", colorDanger);
        JButton btnMoi = createButton("LÀM MỚI", new Color(149, 165, 166)); // Màu Xám

        pnlBottom.add(btnThem); 
        pnlBottom.add(btnSua);
        pnlBottom.add(btnXoa); 
        pnlBottom.add(btnMoi);
        
        add(pnlBottom, BorderLayout.SOUTH);

        // --- 4. XỬ LÝ SỰ KIỆN ---
        
        // Load dữ liệu ban đầu
        loadData();

        // Sự kiện Click bảng -> Đổ dữ liệu lên form
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    txtSDT.setText(model.getValueAt(row, 0).toString());
                    txtTen.setText(model.getValueAt(row, 1).toString());
                    txtDiem.setText(model.getValueAt(row, 2).toString());
                    
                    // Khóa SĐT lại (Primary Key không được sửa)
                    txtSDT.setEditable(false); 
                    txtSDT.setBackground(new Color(240, 240, 240));
                }
            }
        });

        // Nút Thêm
        btnThem.addActionListener(e -> {
            KhachHang kh = getForm();
            if (kh != null) {
                // Kiểm tra SĐT đã có chưa
                if(dao.checkTonTai(kh.getSoDienThoai())) {
                    JOptionPane.showMessageDialog(this, "Số điện thoại này đã tồn tại!");
                    return;
                }
                if(dao.insert(kh)) {
                    JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!");
                    loadData(); clearForm();
                } else JOptionPane.showMessageDialog(this, "Thêm thất bại!");
            }
        });

        // Nút Sửa
        btnSua.addActionListener(e -> {
            KhachHang kh = getForm();
            if(kh != null) {
                if(dao.update(kh)) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công!");
                    loadData(); clearForm();
                } else JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
            }
        });

        // Nút Xóa
        btnXoa.addActionListener(e -> {
            String sdt = txtSDT.getText();
            if(sdt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để xóa!");
                return;
            }
            if(JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xóa khách hàng " + sdt + "?", "Cảnh báo", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if(dao.delete(sdt)) {
                    JOptionPane.showMessageDialog(this, "Đã xóa!");
                    loadData(); clearForm();
                } else JOptionPane.showMessageDialog(this, "Xóa thất bại (Có thể khách đã có hóa đơn)!");
            }
        });

        // Nút Làm mới
        btnMoi.addActionListener(e -> {
            clearForm();
            loadData(); // Load lại toàn bộ từ DB
        });

        // Tìm kiếm (Live Search - Gõ tới đâu tìm tới đó)
        txtTim.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String keyword = txtTim.getText().trim();
                loadDataTimKiem(keyword);
            }
        });
        
        // Nút Tìm (Cho chắc chắn)
        btnTim.addActionListener(e -> loadDataTimKiem(txtTim.getText().trim()));
    }

    // ================== CÁC HÀM HỖ TRỢ LOGIC ==================

    // Load tất cả
    public void loadData() {
        model.setRowCount(0);
        ArrayList<KhachHang> list = dao.getAll();
        for (KhachHang kh : list) {
            addModel(kh);
        }
    }
    
    // Load tìm kiếm
    public void loadDataTimKiem(String keyword) {
        model.setRowCount(0);
        ArrayList<KhachHang> list = dao.timKiem(keyword);
        for (KhachHang kh : list) {
            addModel(kh);
        }
    }

    // Thêm 1 dòng vào bảng (Có xử lý logic Hạng & Ưu đãi)
    private void addModel(KhachHang kh) {
        int diem = kh.getDiemTichLuy();
        
        // Tự tính hạng và ưu đãi tại đây (Không cần sửa Entity)
        String hang = "Thân thiết";
        String uuDai = "Không có";
        
        if (diem >= 1000) {
            hang = "VIP Kim Cương";
            uuDai = "Giảm 15%";
        } else if (diem >= 500) {
            hang = "VIP Vàng";
            uuDai = "Giảm 10%";
        } else if (diem >= 200) {
            hang = "VIP Bạc";
            uuDai = "Giảm 5%";
        }

        model.addRow(new Object[]{
            kh.getSoDienThoai(),
            kh.getTenKhach(),
            diem,
            hang,
            uuDai
        });
    }

    // Lấy dữ liệu từ Form nhập
    private KhachHang getForm() {
        String sdt = txtSDT.getText().trim();
        String ten = txtTen.getText().trim();
        String diemStr = txtDiem.getText().trim();

        if(sdt.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập SĐT và Tên khách hàng!");
            return null;
        }
        
        int diem = 0;
        try {
            diem = Integer.parseInt(diemStr);
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, "Điểm tích lũy phải là số!");
            return null;
        }
        
        return new KhachHang(sdt, ten, diem);
    }

    // Xóa trắng Form
    private void clearForm() {
        txtSDT.setText(""); 
        txtTen.setText(""); 
        txtDiem.setText("0");
        txtSDT.setEditable(true);
        txtSDT.setBackground(Color.WHITE);
        txtSDT.requestFocus();
    }

    // ================== CÁC HÀM TẠO GIAO DIỆN NHANH ==================
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
        btn.setPreferredSize(new Dimension(150, 40));
        // Hiệu ứng hover chuột
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}