package GUI;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;

import DAO.HoaDonDAO;
import Entity.HoaDon;

public class ManHinhHoaDon extends JPanel {
    
    private JTable tblHoaDon;
    private DefaultTableModel model;
    private HoaDonDAO hdDAO = new HoaDonDAO();
    private JLabel lblDoanhThu;
    
    // Các ComboBox để lọc
    private JComboBox<String> cboNgay, cboThang, cboNam;
    private JButton btnTimKiem, btnXemChiTiet,btnReload;

    public ManHinhHoaDon() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- 1. THANH CÔNG CỤ TÌM KIẾM (NORTH) ---
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlTop.setBackground(Color.WHITE);
        pnlTop.setBorder(BorderFactory.createTitledBorder("Bộ Lọc Tìm Kiếm"));

        // Tạo dữ liệu cho Combo Box
        Vector<String> vecNgay = new Vector<>(); vecNgay.add("Tất cả ngày");
        for(int i=1; i<=31; i++) vecNgay.add("Ngày " + i);
        
        Vector<String> vecThang = new Vector<>(); vecThang.add("Tất cả tháng");
        for(int i=1; i<=12; i++) vecThang.add("Tháng " + i);
        
        Vector<String> vecNam = new Vector<>(); 
        int namHienTai = Calendar.getInstance().get(Calendar.YEAR);
        for(int i=namHienTai; i>=2020; i--) vecNam.add("Năm " + i);

        cboNgay = new JComboBox<>(vecNgay);
        cboThang = new JComboBox<>(vecThang);
        cboNam = new JComboBox<>(vecNam); // Mặc định chọn năm hiện tại
        
        // Mặc định chọn tháng hiện tại cho tiện
        cboThang.setSelectedIndex(Calendar.getInstance().get(Calendar.MONTH) + 1);

        btnTimKiem = new JButton("TÌM KIẾM");
        btnTimKiem.setBackground(new Color(52, 152, 219));
        btnTimKiem.setForeground(Color.WHITE);
        
        btnReload = new JButton("Làm Mới");
        btnReload.setBackground(new Color(46, 204, 113)); // Màu xanh lá
        btnReload.setForeground(Color.WHITE);
        pnlTop.add(cboNgay);
        pnlTop.add(cboThang);
        pnlTop.add(cboNam);
        pnlTop.add(btnTimKiem);
        pnlTop.add(btnReload);
        add(pnlTop, BorderLayout.NORTH);

        // --- 2. BẢNG DANH SÁCH (CENTER) ---
        String[] cols = {"Mã HĐ", "Bàn", "Ngày Tạo", "Khách Hàng", "Tổng Tiền", "Trạng Thái"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        tblHoaDon = new JTable(model);
        tblHoaDon.setRowHeight(30);
        tblHoaDon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblHoaDon.getTableHeader().setBackground(new Color(52, 152, 219));
        tblHoaDon.getTableHeader().setForeground(Color.WHITE);

        add(new JScrollPane(tblHoaDon), BorderLayout.CENTER);

        // --- 3. TỔNG KẾT & CHỨC NĂNG (SOUTH) ---
        JPanel pnlBot = new JPanel(new BorderLayout());
        pnlBot.setBackground(Color.WHITE);
        pnlBot.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        btnXemChiTiet = new JButton("Xem Chi Tiết Hóa Đơn (Giải quyết khiếu nại)");
        btnXemChiTiet.setBackground(new Color(243, 156, 18)); // Màu cam
        btnXemChiTiet.setForeground(Color.WHITE);
        
        lblDoanhThu = new JLabel("Tổng Doanh Thu: 0 VNĐ");
        lblDoanhThu.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDoanhThu.setForeground(Color.RED);
        
        pnlBot.add(btnXemChiTiet, BorderLayout.WEST);
        pnlBot.add(lblDoanhThu, BorderLayout.EAST);
        add(pnlBot, BorderLayout.SOUTH);

        // --- 4. SỰ KIỆN ---
        
        // Nút Tìm Kiếm
        btnTimKiem.addActionListener(e -> xuLyTimKiem());
        
        // Sự kiện Click đúp vào bảng để xem chi tiết
        tblHoaDon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) { // Double click
                    xemChiTietHD();
                }
            }
        });
        
        // Nút Xem chi tiết
        btnXemChiTiet.addActionListener(e -> xemChiTietHD());

        // Load lần đầu (Theo tháng hiện tại)
        xuLyTimKiem();
    }

    private void xuLyTimKiem() {
        int ngay = cboNgay.getSelectedIndex(); // Index 0 là "Tất cả" -> giá trị 0
        int thang = cboThang.getSelectedIndex();
        
        String namStr = (String) cboNam.getSelectedItem(); // "Năm 2024"
        int nam = Integer.parseInt(namStr.replace("Năm ", ""));

        // Gọi DAO
        ArrayList<HoaDon> list = hdDAO.timKiemHoaDon(ngay, thang, nam);
        loadTable(list);
    }
    
    private void loadTable(ArrayList<HoaDon> list) {
        model.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        DecimalFormat df = new DecimalFormat("#,###");
        double total = 0;
        
        for (HoaDon hd : list) {
            String trangThai = (hd.getTrangThai() == 1) ? "Đã Thanh Toán" : "Đang Phục Vụ";
            // Lấy tên khách từ Ghi chú (do lúc tạo HD mình lưu tên vào đó) hoặc SĐT
            String khach = (hd.getGhiChu() != null) ? hd.getGhiChu() : hd.getSdtKhach(); 
            if(khach == null) khach = "Vãng lai";

            model.addRow(new Object[]{
                hd.getMaHD(),
                hd.getMaBan(),
                sdf.format(hd.getNgayTao()),
                khach,
                df.format(hd.getTongTien()),
                trangThai
            });
            
            if(hd.getTrangThai() == 1) total += hd.getTongTien();
        }
        lblDoanhThu.setText("Tổng Doanh Thu: " + df.format(total) + " VNĐ");
    }
    
    // Hàm mở lại màn hình gọi món (chế độ xem lại)
    private void xemChiTietHD() {
        int row = tblHoaDon.getSelectedRow();
        if(row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần xem!");
            return;
        }
        
        int maHD = Integer.parseInt(model.getValueAt(row, 0).toString());
        String tenBan = model.getValueAt(row, 1).toString();
        
        // Mở lại màn hình gọi món, nhưng chỉ để xem
        ManHinhGoiMon frm = new ManHinhGoiMon(maHD, "Xem lại: " + tenBan, true);
        frm.setVisible(true);
        // Mẹo: Bạn có thể disable các nút trong ManHinhGoiMon nếu muốn chặn sửa
    }
}