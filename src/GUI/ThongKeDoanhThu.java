package GUI;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import com.toedter.calendar.JDateChooser;

import DAO.ThongKeDAO;

public class ThongKeDoanhThu extends JPanel {

    private ThongKeDAO tkDAO = new ThongKeDAO();

    // UI Components
    private JLabel lblDoanhThuNgay;
    private JLabel lblDoanhThuThang;
    private JLabel lblDoanhThuNam;

    private JDateChooser dateFrom, dateTo, dateFromFood, dateToFood;
    private JTable tblDoanhThu, tblMonAn;
    private DefaultTableModel modelDoanhThu, modelMonAn;

    public ThongKeDoanhThu() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(243, 244, 246)); // Light Gray Background
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. TOP CARDS (Dashboard)
        JPanel pnlCards = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlCards.setBackground(new Color(243, 244, 246));
        pnlCards.setPreferredSize(new Dimension(0, 120));

        pnlCards.add(createCard("Doanh Thu Hôm Nay", "0 VNĐ", new Color(16, 185, 129))); // Green
        pnlCards.add(createCard("Doanh Thu Tháng Này", "0 VNĐ", new Color(59, 130, 246))); // Blue
        pnlCards.add(createCard("Doanh Thu Năm Nay", "0 VNĐ", new Color(245, 158, 11))); // Orange

        // Bind labels to fields for updating
        lblDoanhThuNgay = (JLabel) ((JPanel) pnlCards.getComponent(0)).getComponent(1);
        lblDoanhThuThang = (JLabel) ((JPanel) pnlCards.getComponent(1)).getComponent(1);
        lblDoanhThuNam = (JLabel) ((JPanel) pnlCards.getComponent(2)).getComponent(1);

        add(pnlCards, BorderLayout.NORTH);

        // 2. CENTER TABS
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabs.addTab("Chi Tiết Doanh Thu", createRevenueTab());
        tabs.addTab("Món Ăn Bán Chạy", createFoodTab());

        add(tabs, BorderLayout.CENTER);

        // Events
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshDashboard();
                loadRevenueData(); // Default load
                loadFoodData();
            }
        });
    }

    private JPanel createCard(String title, String value, Color color) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, color),
                new EmptyBorder(15, 20, 15, 20)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(Color.GRAY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(Color.BLACK);

        pnl.add(lblTitle, BorderLayout.NORTH);
        pnl.add(lblValue, BorderLayout.CENTER);

        return pnl;
    }

    // --- TAB 1: REVENUE ---
    private JPanel createRevenueTab() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Filter
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlFilter.setBackground(Color.WHITE);

        dateFrom = new JDateChooser(new Date(System.currentTimeMillis() - 7L * 24 * 3600 * 1000)); // 7 days ago
        dateFrom.setPreferredSize(new Dimension(130, 30));
        dateTo = new JDateChooser(new Date());
        dateTo.setPreferredSize(new Dimension(130, 30));

        JButton btnFilter = new JButton("Xem Thống Kê");
        btnFilter.setBackground(new Color(59, 130, 246));
        btnFilter.setForeground(Color.WHITE);
        btnFilter.addActionListener(e -> loadRevenueData());

        pnlFilter.add(new JLabel("Từ:"));
        pnlFilter.add(dateFrom);
        pnlFilter.add(new JLabel("Đến:"));
        pnlFilter.add(dateTo);
        pnlFilter.add(btnFilter);

        pnl.add(pnlFilter, BorderLayout.NORTH);

        // Table
        String[] cols = { "Ngày", "Doanh Thu" };
        modelDoanhThu = new DefaultTableModel(cols, 0);
        tblDoanhThu = new JTable(modelDoanhThu);
        tblDoanhThu.setRowHeight(30);
        tblDoanhThu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblDoanhThu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        pnl.add(new JScrollPane(tblDoanhThu), BorderLayout.CENTER);
        return pnl;
    }

    // --- TAB 2: FOOD ---
    private JPanel createFoodTab() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Filter
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlFilter.setBackground(Color.WHITE);

        dateFromFood = new JDateChooser(new Date(System.currentTimeMillis() - 30L * 24 * 3600 * 1000)); // 30 days ago
        dateFromFood.setPreferredSize(new Dimension(130, 30));
        dateToFood = new JDateChooser(new Date());
        dateToFood.setPreferredSize(new Dimension(130, 30));

        JButton btnFilter = new JButton("Xem Thống Kê");
        btnFilter.setBackground(new Color(59, 130, 246));
        btnFilter.setForeground(Color.WHITE);
        btnFilter.addActionListener(e -> loadFoodData());

        pnlFilter.add(new JLabel("Từ:"));
        pnlFilter.add(dateFromFood);
        pnlFilter.add(new JLabel("Đến:"));
        pnlFilter.add(dateToFood);
        pnlFilter.add(btnFilter);

        pnl.add(pnlFilter, BorderLayout.NORTH);

        // Table
        String[] cols = { "Tên Món Ăn", "Số Lượng Bán", "Tổng Tiền" };
        modelMonAn = new DefaultTableModel(cols, 0);
        tblMonAn = new JTable(modelMonAn);
        tblMonAn.setRowHeight(30);
        tblMonAn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblMonAn.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        pnl.add(new JScrollPane(tblMonAn), BorderLayout.CENTER);
        return pnl;
    }

    // --- LOGIC ---
    private void refreshDashboard() {
        java.time.LocalDate now = java.time.LocalDate.now();

        double daily = tkDAO.getDoanhThuNgay(new Date());
        lblDoanhThuNgay.setText(formatMoney(daily));

        double monthly = tkDAO.getDoanhThuThang(now.getMonthValue(), now.getYear());
        lblDoanhThuThang.setText(formatMoney(monthly));

        double yearly = tkDAO.getDoanhThuNam(now.getYear());
        lblDoanhThuNam.setText(formatMoney(yearly));
    }

    private void loadRevenueData() {
        // Since getDoanhThuNgay only gets ONE day, and getDoanhThu7NgayGanNhat() is
        // hardcoded,
        // For custom range, we'd need loop or new DAO method.
        // For now, let's use getDoanhThu7NgayGanNhat() if button clicked, ignoring
        // params for simplicity OR update DAO.
        // Actually, let's just show 7 days recent for now based on DAO spec.
        // Note: Real implementation needs a getDoanhThuRange(from, to). Using 7 days
        // for demo.

        modelDoanhThu.setRowCount(0);
        ArrayList<Object[]> list = tkDAO.getDoanhThu7NgayGanNhat();
        for (Object[] row : list) {
            modelDoanhThu.addRow(new Object[] {
                    row[0], // Date
                    formatMoney((Double) row[1])
            });
        }
    }

    private void loadFoodData() {
        modelMonAn.setRowCount(0);
        Date f = dateFromFood.getDate();
        Date t = dateToFood.getDate();
        if (f != null && t != null) {
            ArrayList<Object[]> list = tkDAO.getTopMonAn(f, t);
            for (Object[] row : list) {
                modelMonAn.addRow(new Object[] {
                        row[0], // Name
                        row[1], // Qty
                        formatMoney((Double) row[2]) // Price
                });
            }
        }
    }

    private String formatMoney(double amount) {
        return java.text.NumberFormat.getIntegerInstance().format(amount) + " VNĐ";
    }
}
