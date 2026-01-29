package GUI;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import DAO.ThongKeDAO;

public class ThongKeDoanhThu extends JPanel {

    private JSpinner spinTuNgay, spinDenNgay;
    private JLabel lblTongTien, lblTongDon, lblMonHot;
    private JTable tblDoanhThu, tblMonAn;
    private DefaultTableModel modelDoanhThu, modelMonAn;
    
    private ThongKeDAO dao = new ThongKeDAO();
    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public ThongKeDoanhThu() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- 1. THANH CHỌN NGÀY ---
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlTop.setBackground(Color.WHITE);
        
        SpinnerDateModel model1 = new SpinnerDateModel();
        SpinnerDateModel model2 = new SpinnerDateModel();
        spinTuNgay = new JSpinner(model1);
        spinDenNgay = new JSpinner(model2);
        spinTuNgay.setEditor(new JSpinner.DateEditor(spinTuNgay, "dd/MM/yyyy"));
        spinDenNgay.setEditor(new JSpinner.DateEditor(spinDenNgay, "dd/MM/yyyy"));
        
        // Mặc định từ đầu tháng
        Calendar cal = Calendar.getInstance();
        spinDenNgay.setValue(cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, 1);
        spinTuNgay.setValue(cal.getTime());

        JButton btnXem = new JButton("XEM BÁO CÁO");
        btnXem.setBackground(new Color(0, 123, 255));
        btnXem.setForeground(Color.WHITE);

        pnlTop.add(new JLabel("Từ ngày:")); pnlTop.add(spinTuNgay);
        pnlTop.add(new JLabel("Đến ngày:")); pnlTop.add(spinDenNgay);
        pnlTop.add(btnXem);
        add(pnlTop, BorderLayout.NORTH);

        // --- 2. CÁC THẺ KPI (SUMMARY CARDS) ---
        JPanel pnlKPI = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlKPI.setBackground(Color.WHITE);
        pnlKPI.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        pnlKPI.setPreferredSize(new Dimension(0, 100));

        lblTongTien = createCard("DOANH THU", "0 VNĐ", new Color(46, 204, 113));
        lblTongDon = createCard("TỔNG HÓA ĐƠN", "0", new Color(243, 156, 18));
        lblMonHot = createCard("MÓN BÁN CHẠY", "Không có", new Color(231, 76, 60));

        pnlKPI.add(lblTongTien);
        pnlKPI.add(lblTongDon);
        pnlKPI.add(lblMonHot);
        
        // Tạo Panel chứa KPI đặt ở trên cùng (dưới thanh ngày)
        JPanel pnlNorthContainer = new JPanel(new BorderLayout());
        pnlNorthContainer.add(pnlTop, BorderLayout.NORTH);
        pnlNorthContainer.add(pnlKPI, BorderLayout.CENTER);
        add(pnlNorthContainer, BorderLayout.NORTH);

        // --- 3. PHẦN BẢNG DỮ LIỆU (CENTER) ---
        JPanel pnlCenter = new JPanel(new GridLayout(1, 2, 10, 0)); // Chia đôi màn hình
        
        // Bảng TRÁI: Doanh thu chi tiết ngày
        JPanel pnlLeft = new JPanel(new BorderLayout());
        pnlLeft.setBorder(new TitledBorder("Chi Tiết Doanh Thu Theo Ngày"));
        modelDoanhThu = new DefaultTableModel(new String[]{"Ngày", "Số Đơn", "Doanh Thu"}, 0);
        tblDoanhThu = new JTable(modelDoanhThu);
        tblDoanhThu.setRowHeight(25);
        pnlLeft.add(new JScrollPane(tblDoanhThu));

        // Bảng PHẢI: Top Món Ăn
        JPanel pnlRight = new JPanel(new BorderLayout());
        pnlRight.setBorder(new TitledBorder("Top 5 Món Ăn Bán Chạy Nhất"));
        modelMonAn = new DefaultTableModel(new String[]{"Tên Món", "Số Lượng", "Doanh Số"}, 0);
        tblMonAn = new JTable(modelMonAn);
        tblMonAn.setRowHeight(25);
        pnlRight.add(new JScrollPane(tblMonAn));

        pnlCenter.add(pnlLeft);
        pnlCenter.add(pnlRight);
        add(pnlCenter, BorderLayout.CENTER);

        // --- SỰ KIỆN ---
        btnXem.addActionListener(e -> loadThongKe());
        loadThongKe();
    }

    private void loadThongKe() {
        Date tu = (Date) spinTuNgay.getValue();
        Date den = (Date) spinDenNgay.getValue();

        // 1. Load Doanh Thu Theo Ngày
        modelDoanhThu.setRowCount(0);
        ArrayList<Object[]> listDT = dao.getDoanhThuTheoNgay(tu, den);
        double totalTien = 0;
        int totalDon = 0;

        for(Object[] row : listDT) {
            modelDoanhThu.addRow(new Object[]{
                sdf.format((Date)row[0]),
                row[1],
                df.format(row[2])
            });
            totalDon += (int)row[1];
            totalTien += (double)row[2];
        }

        // 2. Load Top Món Ăn
        modelMonAn.setRowCount(0);
        ArrayList<Object[]> listMon = dao.getTopMonAn(tu, den);
        String topMon = "Chưa có";
        if(listMon.size() > 0) topMon = listMon.get(0)[0].toString(); // Lấy tên món đầu tiên

        for(Object[] row : listMon) {
            modelMonAn.addRow(new Object[]{
                row[0],
                row[1],
                df.format(row[2])
            });
        }

        // 3. Cập nhật thẻ KPI
        lblTongTien.setText("<html><center>DOANH THU<br><span style='font-size:18px'>" + df.format(totalTien) + "</span></center></html>");
        lblTongDon.setText("<html><center>TỔNG HÓA ĐƠN<br><span style='font-size:18px'>" + totalDon + "</span></center></html>");
        lblMonHot.setText("<html><center>MÓN BÁN CHẠY<br><span style='font-size:18px'>" + topMon + "</span></center></html>");
    }

    private JLabel createCard(String title, String value, Color color) {
        JLabel lbl = new JLabel("<html><center>" + title + "<br><span style='font-size:18px'>" + value + "</span></center></html>");
        lbl.setOpaque(true);
        lbl.setBackground(color);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }
}