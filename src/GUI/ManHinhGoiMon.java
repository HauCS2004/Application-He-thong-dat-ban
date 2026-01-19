package GUI;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import DAO.ChiTietHoaDonDAO;
import DAO.LoaiMonDAO;
import DAO.MonAnDAO;
import Entity.ChiTietHoaDon;
import Entity.LoaiMon;
import Entity.MonAn;
import UTILS.XImage;

public class ManHinhGoiMon extends JDialog {

    private int maHD; 
    private String tenBan;
    
    // Component bên trái (Menu)
    private JPanel pnlMenu;
    private JComboBox<LoaiMon> cboLoai;
    private JTextField txtTim;

    // Component bên phải (Bill)
    private JTable tableBill;
    private DefaultTableModel modelBill;
    private JLabel lblTongTien;
    
    // DAO
    private MonAnDAO monAnDAO = new MonAnDAO();
    private ChiTietHoaDonDAO ctDAO = new ChiTietHoaDonDAO();
    private LoaiMonDAO loaiDAO = new LoaiMonDAO();
    
    private DecimalFormat df = new DecimalFormat("#,###");

    // [SỬA 1] Thêm tham số boolean isViewOnly vào Constructor
    public ManHinhGoiMon(int maHD, String tenBan, boolean isViewOnly) {
        this.maHD = maHD;
        this.tenBan = tenBan;
        
        // [SỬA 2] Đổi tiêu đề nếu là xem lại
        setTitle((isViewOnly ? "CHI TIẾT HÓA ĐƠN: " : "GỌI MÓN: ") + tenBan + " (Mã HĐ: " + maHD + ")");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setModal(true);
        setLayout(new BorderLayout());

        // --- PHẦN TRÁI: DANH SÁCH MÓN ĂN (60%) ---
        // [SỬA 3] Chỉ tạo và thêm Menu bên trái nếu KHÔNG PHẢI chế độ xem lại
        if (!isViewOnly) {
            JPanel pnlLeft = new JPanel(new BorderLayout());
            pnlLeft.setPreferredSize(new Dimension(650, 0));
            pnlLeft.setBorder(new TitledBorder("THỰC ĐƠN NHÀ HÀNG"));

            // 1. Bộ lọc (Loại món & Tìm kiếm)
            JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT));
            cboLoai = new JComboBox<>();
            cboLoai.addItem(new LoaiMon(null, "--- Tất cả ---"));
            for(LoaiMon lm : loaiDAO.getAllLoai()) cboLoai.addItem(lm);
            
            txtTim = new JTextField(15);
            JButton btnTim = new JButton("Tìm");
            
            pnlFilter.add(new JLabel("Loại:")); pnlFilter.add(cboLoai);
            pnlFilter.add(new JLabel("Tìm:"));  pnlFilter.add(txtTim);
            pnlFilter.add(btnTim);
            pnlLeft.add(pnlFilter, BorderLayout.NORTH);

            // 2. Lưới hiển thị món ăn
            pnlMenu = new JPanel(new GridLayout(0, 3, 10, 10)); // 3 cột
            pnlMenu.setBackground(Color.WHITE);
            JScrollPane scrollMenu = new JScrollPane(pnlMenu);
            scrollMenu.getVerticalScrollBar().setUnitIncrement(16);
            pnlLeft.add(scrollMenu, BorderLayout.CENTER);

            add(pnlLeft, BorderLayout.WEST);
            
            // Các sự kiện của phần bên trái cũng chỉ cần khi !isViewOnly
            loadMenu(""); 

            cboLoai.addActionListener(e -> {
                LoaiMon lm = (LoaiMon) cboLoai.getSelectedItem();
                String maLoai = (lm.getMaLoai() == null) ? "" : lm.getMaLoai();
                loadMenu(maLoai); 
            });
            
            btnTim.addActionListener(e -> loadMenuSearch(txtTim.getText()));
        }

        // --- PHẦN PHẢI: HÓA ĐƠN TẠM TÍNH (40%) ---
        JPanel pnlRight = new JPanel(new BorderLayout());
        pnlRight.setBorder(new TitledBorder("DANH SÁCH " + (isViewOnly ? "ĐÃ GỌI: " : "ĐANG GỌI: ") + tenBan.toUpperCase()));
        
        // 1. Bảng chi tiết
        String[] cols = {"Tên Món", "SL", "Đơn Giá", "Thành Tiền"};
        modelBill = new DefaultTableModel(cols, 0) {
            @Override // [SỬA 4] Chặn không cho sửa trực tiếp trên bảng
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableBill = new JTable(modelBill);
        tableBill.setRowHeight(30);
        tableBill.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableBill.getColumnModel().getColumn(0).setPreferredWidth(150);
        tableBill.getColumnModel().getColumn(1).setPreferredWidth(30);
        
        JScrollPane scrollBill = new JScrollPane(tableBill);
        pnlRight.add(scrollBill, BorderLayout.CENTER);

        // 2. Tổng tiền & Nút
        JPanel pnlBot = new JPanel(new BorderLayout());
        pnlBot.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlBot.setBackground(new Color(230, 230, 230));

        lblTongTien = new JLabel("TỔNG CỘNG: 0 VNĐ", JLabel.RIGHT);
        lblTongTien.setFont(new Font("Arial", Font.BOLD, 20));
        lblTongTien.setForeground(Color.RED);
        
        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnXoaMon = new JButton("Xóa Món");
        btnXoaMon.setBackground(Color.RED); btnXoaMon.setForeground(Color.WHITE);
        
        JButton btnXong = new JButton(isViewOnly ? "ĐÓNG (ESC)" : "HOÀN TẤT GỌI MÓN");
        btnXong.setBackground(isViewOnly ? Color.GRAY : new Color(46, 204, 113)); 
        btnXong.setForeground(Color.WHITE);
        btnXong.setPreferredSize(new Dimension(200, 40));
        btnXong.setFont(new Font("Arial", Font.BOLD, 14));

        // [SỬA 5] Chỉ hiện nút Xóa Món nếu KHÔNG PHẢI xem lại
        if (!isViewOnly) {
            pnlAction.add(btnXoaMon);
        }
        
        pnlBot.add(lblTongTien, BorderLayout.NORTH);
        pnlBot.add(btnXong, BorderLayout.CENTER);
        pnlBot.add(pnlAction, BorderLayout.SOUTH);

        pnlRight.add(pnlBot, BorderLayout.SOUTH);
        add(pnlRight, BorderLayout.CENTER);

        // --- EVENTS CHUNG ---
        loadBill();   // Luôn luôn load bill

        // Sự kiện nút Xóa Món (Chỉ cần khi !isViewOnly)
        if (!isViewOnly) {
            btnXoaMon.addActionListener(e -> {
                int row = tableBill.getSelectedRow();
                if(row == -1) return;
                // Lưu ý: Đoạn này nên lấy mã món từ list ẩn, lấy theo tên trên bảng có thể bị trùng nếu tên giống nhau
                ArrayList<ChiTietHoaDon> list = ctDAO.getChiTiet(maHD);
                String maMonXoa = list.get(row).getMaMon();
                
                ctDAO.xoaMon(maHD, maMonXoa);
                loadBill();
            });
        }

        // Nút Xong
        btnXong.addActionListener(e -> dispose());
    }

    // --- CÁC HÀM LOGIC (GIỮ NGUYÊN) ---

    private void loadMenu(String maLoai) {
        pnlMenu.removeAll();
        ArrayList<MonAn> list = monAnDAO.timKiem("", maLoai); 
        for(MonAn m : list) {
            JPanel item = createItemMenu(m);
            pnlMenu.add(item);
        }
        pnlMenu.revalidate();
        pnlMenu.repaint();
    }
    
    private void loadMenuSearch(String ten) {
        pnlMenu.removeAll();
        ArrayList<MonAn> list = monAnDAO.timKiem(ten, "");
        for(MonAn m : list) pnlMenu.add(createItemMenu(m));
        pnlMenu.revalidate();
        pnlMenu.repaint();
    }

    private JPanel createItemMenu(MonAn m) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(150, 180));
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblAnh = new JLabel();
        lblAnh.setHorizontalAlignment(JLabel.CENTER);
        ImageIcon icon = XImage.read(m.getHinhAnh());
        if(icon!=null) {
            Image img = icon.getImage().getScaledInstance(120, 100, Image.SCALE_SMOOTH);
            lblAnh.setIcon(new ImageIcon(img));
        }
        p.add(lblAnh, BorderLayout.CENTER);

        JPanel pInfo = new JPanel(new GridLayout(2, 1));
        pInfo.setBackground(new Color(245, 245, 245));
        JLabel lblTen = new JLabel(m.getTenMon(), JLabel.CENTER);
        lblTen.setFont(new Font("Arial", Font.BOLD, 12));
        JLabel lblGia = new JLabel(df.format(m.getDonGia()) + " đ", JLabel.CENTER);
        lblGia.setForeground(Color.RED);
        pInfo.add(lblTen); pInfo.add(lblGia);
        p.add(pInfo, BorderLayout.SOUTH);

        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ctDAO.themMon(maHD, m.getMaMon(), 1, m.getDonGia());
                loadBill();
            }
        });

        return p;
    }

    private void loadBill() {
        modelBill.setRowCount(0);
        ArrayList<ChiTietHoaDon> list = ctDAO.getChiTiet(maHD);
        double tongTien = 0;
        
        for(ChiTietHoaDon ct : list) {
            double thanhTien = ct.getSoLuong() * ct.getDonGia();
            tongTien += thanhTien;
            modelBill.addRow(new Object[]{
                ct.getTenMonAn(),
                ct.getSoLuong(),
                df.format(ct.getDonGia()),
                df.format(thanhTien)
            });
        }
        lblTongTien.setText("TỔNG CỘNG: " + df.format(tongTien) + " VNĐ");
    }
}