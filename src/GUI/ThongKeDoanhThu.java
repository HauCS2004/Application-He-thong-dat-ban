package GUI;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

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
        tabs.addTab("Khung Giờ Vàng", createGoldenHourTab());
        tabs.addTab("Hiệu Suất Nhân Viên", createEmployeePerformanceTab());

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
        Date f = dateFrom.getDate();
        Date t = dateTo.getDate();
        if (f == null || t == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày bắt đầu và ngày kết thúc!",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        modelDoanhThu.setRowCount(0);
        // Gọi đúng method với filter ngày thực sự
        ArrayList<Object[]> list = tkDAO.getDoanhThuTheoKhoang(f, t);
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

    // --- TAB 3: GOLDEN HOUR ---
    private JDateChooser dateFromGolden, dateToGolden;
    private JTable tblGoldenHour;
    private DefaultTableModel modelGoldenHour;

    private JPanel createGoldenHourTab() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Filter
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlFilter.setBackground(Color.WHITE);

        dateFromGolden = new JDateChooser(new Date(System.currentTimeMillis() - 7L * 24 * 3600 * 1000));
        dateFromGolden.setPreferredSize(new Dimension(150, 30));
        dateToGolden = new JDateChooser(new Date());
        dateToGolden.setPreferredSize(new Dimension(150, 30));

        JButton btnFilter = new JButton("Thống kê");
        btnFilter.setBackground(new Color(245, 158, 11)); // Orange
        btnFilter.setForeground(Color.WHITE);
        btnFilter.addActionListener(e -> loadGoldenHourData());

        pnlFilter.add(new JLabel("Từ:"));
        pnlFilter.add(dateFromGolden);
        pnlFilter.add(new JLabel("Đến:"));
        pnlFilter.add(dateToGolden);
        pnlFilter.add(btnFilter);

        pnl.add(pnlFilter, BorderLayout.NORTH);

        // Table (Heatmap Style)
        String[] cols = { "Khung Giờ", "Số Đơn", "Doanh Thu", "Mức Độ" };
        modelGoldenHour = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblGoldenHour = new JTable(modelGoldenHour);
        tblGoldenHour.setRowHeight(30);
        tblGoldenHour.getColumnModel().getColumn(3).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
                if (value instanceof Integer) {
                    int intensity = (int) value;
                    int alpha = Math.min(255, Math.max(0, intensity * 25)); // Scale intensity
                    setBackground(new Color(255, 165, 0, alpha));
                    setText(intensity + "/10");
                    setHorizontalAlignment(SwingConstants.CENTER);
                }
                return this;
            }
        });

        pnl.add(new JScrollPane(tblGoldenHour), BorderLayout.CENTER);

        return pnl;
    }

    private void loadGoldenHourData() {
        modelGoldenHour.setRowCount(0);
        Date from = dateFromGolden.getDate();
        Date to = dateToGolden.getDate();
        if (from == null || to == null)
            return;

        ArrayList<Object[]> list = tkDAO.getDoanhThuTheoKhungGio(from, to);

        // Find max for scaling
        double maxRevenue = 0;
        for (Object[] row : list) {
            maxRevenue = Math.max(maxRevenue, (double) row[2]);
        }

        for (Object[] row : list) {
            int hour = (int) row[0];
            int count = (int) row[1];
            double revenue = (double) row[2];

            // Calculate intensity 1-10
            int intensity = maxRevenue > 0 ? (int) ((revenue / maxRevenue) * 10) : 0;

            modelGoldenHour.addRow(new Object[] {
                    String.format("%02d:00 - %02d:00", hour, hour + 1),
                    count,
                    formatMoney(revenue),
                    intensity
            });
        }
    }

    // --- TAB 4: EMPLOYEE PERFORMANCE ---
    private JDateChooser dateFromEmp, dateToEmp;
    private JTable tblEmployee;
    private DefaultTableModel modelEmployee;

    private JPanel createEmployeePerformanceTab() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Filter
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlFilter.setBackground(Color.WHITE);

        dateFromEmp = new JDateChooser(new Date(System.currentTimeMillis() - 30L * 24 * 3600 * 1000)); // 30 days
        dateFromEmp.setPreferredSize(new Dimension(150, 30));
        dateToEmp = new JDateChooser(new Date());
        dateToEmp.setPreferredSize(new Dimension(150, 30));

        JButton btnFilter = new JButton("Thống kê");
        btnFilter.setBackground(new Color(59, 130, 246)); // Blue
        btnFilter.setForeground(Color.WHITE);
        btnFilter.addActionListener(e -> loadEmployeeData());

        pnlFilter.add(new JLabel("Từ:"));
        pnlFilter.add(dateFromEmp);
        pnlFilter.add(new JLabel("Đến:"));
        pnlFilter.add(dateToEmp);
        pnlFilter.add(btnFilter);

        pnl.add(pnlFilter, BorderLayout.NORTH);

        // Table
        String[] cols = { "Mã NV", "Tên Nhân Viên", "Số Hóa Đơn", "Tổng Doanh Thu", "TB/Đơn" };
        modelEmployee = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2)
                    return Integer.class;
                if (columnIndex == 3 || columnIndex == 4)
                    return Double.class; // For sorting
                return String.class;
            }
        };

        tblEmployee = new JTable(modelEmployee);
        tblEmployee.setRowHeight(30);
        tblEmployee.setAutoCreateRowSorter(true);
        tblEmployee.getColumnModel().getColumn(3).setCellRenderer(new CurrencyRenderer());
        tblEmployee.getColumnModel().getColumn(4).setCellRenderer(new CurrencyRenderer());

        pnl.add(new JScrollPane(tblEmployee), BorderLayout.CENTER);

        return pnl;
    }

    private void loadEmployeeData() {
        modelEmployee.setRowCount(0);
        Date from = dateFromEmp.getDate();
        Date to = dateToEmp.getDate();
        if (from == null || to == null)
            return;

        ArrayList<Object[]> list = tkDAO.getHieuSuatNhanVien(from, to);

        for (Object[] row : list) {
            String id = (String) row[0];
            String name = (String) row[1];
            int count = (int) row[2];
            double revenue = (double) row[3];
            double avg = count > 0 ? revenue / count : 0;

            modelEmployee.addRow(new Object[] {
                    id,
                    name,
                    count,
                    revenue, // Pass Double
                    avg // Pass Double
            });
        }
    }

    // --- RENDERER FOR CURRENCY ---
    private class CurrencyRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Double) {
                setText(formatMoney((Double) value));
                setHorizontalAlignment(SwingConstants.RIGHT);
            }
            return this;
        }
    }

    private String formatMoney(double amount) {
        return java.text.NumberFormat.getIntegerInstance().format(amount) + " VNĐ";
    }
}
