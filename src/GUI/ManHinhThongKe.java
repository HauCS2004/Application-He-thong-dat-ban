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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.renderer.category.BarRenderer;

public class ManHinhThongKe extends JPanel {

    private ThongKeDAO tkDAO = new ThongKeDAO();

    // UI Components - KPIs
    private JLabel lblDoanhThuNgay = new JLabel("0 VNĐ");
    private JLabel lblDoanhThuThang = new JLabel("0 VNĐ");
    private JLabel lblDoanhThuNam = new JLabel("0 VNĐ");

    private JLabel lblPhanTramNgay = new JLabel("--");
    private JLabel lblPhanTramThang = new JLabel("--");
    private JLabel lblPhanTramNam = new JLabel("--");

    private JDateChooser dateFrom, dateTo, dateFromFood, dateToFood;
    private JTable tblDoanhThu, tblMonAn;
    private DefaultTableModel modelDoanhThu, modelMonAn;

    // JFreeChart Variables
    private DefaultCategoryDataset datasetDoanhThu;
    private ChartPanel chartPanelDoanhThu;

    // Insights
    private JLabel lblTopDay;
    private JLabel lblTopFood;

    // BarChart Variables (Food)
    private DefaultCategoryDataset datasetFood;
    private ChartPanel chartPanelFood;
    private JLabel lblTotalFoodSold;

    // BarChart Variables (Employee)
    private DefaultCategoryDataset datasetEmployee;
    private ChartPanel chartPanelEmployee;
    private JLabel lblNhanVienXuatSac;

    public ManHinhThongKe() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(243, 244, 246)); // Light Gray Background
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. TOP CARDS (Dashboard)
        JPanel pnlCards = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlCards.setBackground(new Color(243, 244, 246));
        pnlCards.setPreferredSize(new Dimension(0, 110));

        pnlCards.add(createKPICard("Doanh Thu Hôm Nay", lblDoanhThuNgay, lblPhanTramNgay, new Color(16, 185, 129))); // Green
        pnlCards.add(createKPICard("Doanh Thu Tháng Này", lblDoanhThuThang, lblPhanTramThang, new Color(59, 130, 246))); // Blue
        pnlCards.add(createKPICard("Doanh Thu Năm Nay", lblDoanhThuNam, lblPhanTramNam, new Color(245, 158, 11))); // Orange

        add(pnlCards, BorderLayout.NORTH);

        // 2. CENTER TABS
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabs.addTab("Chi Tiết Doanh Thu", createRevenueTab());
        tabs.addTab("Món Ăn Bán Chạy", createFoodTab());
        // Bỏ qua tab Khung Giờ Vàng theo yêu cầu
        // tabs.addTab("Khung Giờ Vàng", createGoldenHourTab());
        // tabs.addTab("Hiệu Suất Nhân Viên", createEmployeePerformanceTab());

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

    private JPanel createKPICard(String title, JLabel lblValue, JLabel lblPercent, Color color) {
        JPanel pnl = new JPanel(new BorderLayout(5, 5));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, color),
                new EmptyBorder(15, 20, 15, 20)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(107, 114, 128)); // gray-500

        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValue.setForeground(new Color(17, 24, 39)); // gray-900

        lblPercent.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel pnlValue = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlValue.setOpaque(false);
        pnlValue.add(lblValue);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlBottom.setOpaque(false);
        pnlBottom.add(lblPercent);

        pnl.add(lblTitle, BorderLayout.NORTH);
        pnl.add(pnlValue, BorderLayout.CENTER);
        pnl.add(pnlBottom, BorderLayout.SOUTH);

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

        // JFreeChart - Line Chart
        datasetDoanhThu = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createLineChart(
                "Biểu Đồ Xu Hướng Doanh Thu",
                "Thời Gian",
                "Doanh Thu (VNĐ)",
                datasetDoanhThu,
                PlotOrientation.VERTICAL,
                false, true, false);

        // Style Chart
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(249, 250, 251)); // gray-50
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(37, 99, 235)); // Solid blue (primary)
        renderer.setSeriesStroke(0, new BasicStroke(2.0f)); // Thinner line
        renderer.setSeriesShapesVisible(0, true);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator(
                "{2}", new java.text.DecimalFormat("#,###")));
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.setRenderer(renderer);

        chartPanelDoanhThu = new ChartPanel(chart);
        chartPanelDoanhThu.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 70-30 Split Layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.7); // 70% left
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);

        splitPane.setLeftComponent(chartPanelDoanhThu);

        // Right Panel (Insights + Table)
        JPanel pnlRight = new JPanel(new BorderLayout(0, 15));
        pnlRight.setBackground(Color.WHITE);
        pnlRight.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Insights Panel
        JPanel pnlInsights = new JPanel(new GridLayout(2, 1, 5, 10));
        pnlInsights.setOpaque(false);
        pnlInsights.setBorder(BorderFactory.createTitledBorder("Top Insights"));

        lblTopDay = new JLabel("Ngày cao điểm: N/A");
        lblTopFood = new JLabel("Món bán chạy: N/A");
        lblTopDay.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTopFood.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTopDay.setForeground(new Color(17, 24, 39));
        lblTopFood.setForeground(new Color(17, 24, 39));

        pnlInsights.add(lblTopDay);
        pnlInsights.add(lblTopFood);

        pnlRight.add(pnlInsights, BorderLayout.NORTH);
        pnlRight.add(new JScrollPane(tblDoanhThu), BorderLayout.CENTER);

        splitPane.setRightComponent(pnlRight);

        pnl.add(splitPane, BorderLayout.CENTER);
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
        tblMonAn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblMonAn.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // BarChart (Food)
        datasetFood = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createBarChart(
                "Top Món Ăn Bán Chạy",
                "", // Category Axis Label
                "Số Lượng",
                datasetFood,
                PlotOrientation.HORIZONTAL,
                false, true, false);

        // Style Chart
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(249, 250, 251)); // gray-50
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(59, 130, 246)); // Solid blue
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter()); // Remove gradient
        renderer.setMaximumBarWidth(0.2); // avoid thick bars
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator(
                "{2}", new java.text.DecimalFormat("#,###")));
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 12));

        chartPanelFood = new ChartPanel(chart);
        chartPanelFood.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 70-30 Split Layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);

        splitPane.setLeftComponent(chartPanelFood);

        // Right Panel (Insights + Table)
        JPanel pnlRight = new JPanel(new BorderLayout(0, 15));
        pnlRight.setBackground(Color.WHITE);
        pnlRight.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Insights Panel
        JPanel pnlInsights = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlInsights.setOpaque(false);
        lblTotalFoodSold = new JLabel("Tổng số đĩa phục vụ: 0");
        lblTotalFoodSold.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalFoodSold.setForeground(new Color(17, 24, 39));
        pnlInsights.add(lblTotalFoodSold);

        pnlRight.add(pnlInsights, BorderLayout.NORTH);
        pnlRight.add(new JScrollPane(tblMonAn), BorderLayout.CENTER);

        splitPane.setRightComponent(pnlRight);

        pnl.add(splitPane, BorderLayout.CENTER);
        return pnl;
    }

    // --- LOGIC ---
    private void refreshDashboard() {
        java.time.LocalDate now = java.time.LocalDate.now();
        Date today = new Date();

        // 1. Hôm nay vs Hôm qua
        double daily = tkDAO.getDoanhThuNgay(today);
        double yesterday = tkDAO.getDoanhThuHomQua(today);
        lblDoanhThuNgay.setText(formatCompactMoney(daily));
        updatePercentLabel(lblPhanTramNgay, daily, yesterday);

        // 2. Tháng này vs Tháng trước
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();
        double monthly = tkDAO.getDoanhThuThang(currentMonth, currentYear);
        double lastMonth = tkDAO.getDoanhThuThangTruoc(currentMonth, currentYear);
        lblDoanhThuThang.setText(formatCompactMoney(monthly));
        updatePercentLabel(lblPhanTramThang, monthly, lastMonth);

        // 3. Năm nay vs Năm trước
        double yearly = tkDAO.getDoanhThuNam(currentYear);
        double lastYear = tkDAO.getDoanhThuNamTruoc(currentYear);
        lblDoanhThuNam.setText(formatCompactMoney(yearly));
        updatePercentLabel(lblPhanTramNam, yearly, lastYear);
    }

    private void updatePercentLabel(JLabel lbl, double current, double previous) {
        if (previous == 0) {
            if (current > 0) {
                lbl.setText("Tăng 100% so với kỳ trước");
                lbl.setForeground(new Color(22, 163, 74)); // green-600
            } else {
                lbl.setText("--");
                lbl.setForeground(Color.GRAY);
            }
            return;
        }

        double diff = current - previous;
        double percent = (diff / previous) * 100;

        if (percent > 0) {
            lbl.setText(String.format("Tăng %.1f%% so với kỳ trước", percent));
            lbl.setForeground(new Color(22, 163, 74));
        } else if (percent < 0) {
            lbl.setText(String.format("Giảm %.1f%% so với kỳ trước", Math.abs(percent)));
            lbl.setForeground(new Color(220, 38, 38)); // red-600
        } else {
            lbl.setText("Không đổi so với kỳ trước");
            lbl.setForeground(Color.GRAY);
        }
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
        datasetDoanhThu.clear(); // Xóa data cũ của biểu đồ

        ArrayList<Object[]> list = tkDAO.getDoanhThuTheoKhoang(f, t);

        String topDay = "N/A";
        double maxRev = 0;

        for (Object[] row : list) {
            String dateStr = String.valueOf(row[0]);
            double revenue = (Double) row[1];

            modelDoanhThu.addRow(new Object[] {
                    dateStr,
                    formatMoney(revenue)
            });

            if (revenue > maxRev) {
                maxRev = revenue;
                topDay = dateStr;
            }

            // Thêm dữ liệu vào biểu đồ
            datasetDoanhThu.setValue(revenue, "Doanh Thu", dateStr);
        }

        // Update Insights
        lblTopDay.setText("Ngày cao điểm nhất: " + topDay + " (" + formatCompactMoney(maxRev) + ")");

        ArrayList<Object[]> foods = tkDAO.getTopMonAn(f, t);
        if (!foods.isEmpty()) {
            Object[] top1 = foods.get(0);
            lblTopFood.setText("Món bán chạy nhất: " + top1[0]);
        } else {
            lblTopFood.setText("Món bán chạy nhất: N/A");
        }
    }

    private void loadFoodData() {
        modelMonAn.setRowCount(0);
        if (datasetFood != null)
            datasetFood.clear();

        Date f = dateFromFood.getDate();
        Date t = dateToFood.getDate();
        if (f != null && t != null) {
            ArrayList<Object[]> list = tkDAO.getTopMonAn(f, t);
            int totalQty = 0;
            int index = 0;

            for (Object[] row : list) {
                String name = (String) row[0];
                int qty = (int) row[1];
                double price = (Double) row[2];

                modelMonAn.addRow(new Object[] {
                        name,
                        qty,
                        formatMoney(price)
                });

                totalQty += qty;

                // Top 5 vào BarChart
                if (index < 5) {
                    datasetFood.addValue(qty, "So Luong", name);
                }

                index++;
            }

            if (lblTotalFoodSold != null) {
                lblTotalFoodSold.setText(
                        "Tổng số phần đã phục vụ: " + java.text.NumberFormat.getIntegerInstance().format(totalQty));
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

        // BarChart (Horizontal)
        datasetEmployee = new DefaultCategoryDataset();
        JFreeChart chart = ChartFactory.createBarChart(
                "Top Doanh Thu Nhân Viên",
                "", // Category Axis Label
                "Doanh Thu (VNĐ)",
                datasetEmployee,
                PlotOrientation.HORIZONTAL,
                false, true, false);

        // Style Chart
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(249, 250, 251)); // gray-50
        plot.setOutlineVisible(false);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(16, 185, 129)); // flat green
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter()); // Remove gradient
        renderer.setMaximumBarWidth(0.2);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator(
                "{2}", new java.text.DecimalFormat("#,###")));
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 13)); // larger label

        chartPanelEmployee = new ChartPanel(chart);
        chartPanelEmployee.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 50-50 Split Layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);

        splitPane.setLeftComponent(chartPanelEmployee);

        // Right Panel (Insights + Table)
        JPanel pnlRight = new JPanel(new BorderLayout(0, 15));
        pnlRight.setBackground(Color.WHITE);
        pnlRight.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Insights Panel - 2 labels
        JPanel pnlInsights = new JPanel(new GridLayout(2, 1, 5, 8));
        pnlInsights.setOpaque(false);
        pnlInsights.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(16, 185, 129)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        lblNhanVienXuatSac = new JLabel("Xuat sac nhat: N/A");
        lblNhanVienXuatSac.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNhanVienXuatSac.setForeground(new Color(245, 158, 11)); // Amber

        JLabel lblAvgRevPerNV = new JLabel("TB doanh thu / nhan vien: N/A");
        lblAvgRevPerNV.setName("lblAvgRevPerNV");
        lblAvgRevPerNV.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblAvgRevPerNV.setForeground(new Color(75, 85, 99));

        pnlInsights.add(lblNhanVienXuatSac);
        pnlInsights.add(lblAvgRevPerNV);

        pnlRight.add(pnlInsights, BorderLayout.NORTH);
        pnlRight.add(new JScrollPane(tblEmployee), BorderLayout.CENTER);

        splitPane.setRightComponent(pnlRight);

        pnl.add(splitPane, BorderLayout.CENTER);

        return pnl;
    }

    private void loadEmployeeData() {
        modelEmployee.setRowCount(0);
        if (datasetEmployee != null)
            datasetEmployee.clear();

        Date from = dateFromEmp.getDate();
        Date to = dateToEmp.getDate();
        if (from == null || to == null)
            return;

        ArrayList<Object[]> list = tkDAO.getHieuSuatNhanVien(from, to);

        int index = 0;
        String topNV = "N/A";
        double totalRevAll = 0;

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
                    revenue,
                    avg
            });

            totalRevAll += revenue;

            if (index == 0) {
                topNV = name;
            }

            // Top 5 NV vao chart
            if (index < 5 && datasetEmployee != null) {
                datasetEmployee.addValue(revenue, "Doanh Thu", name);
            }
            index++;
        }

        // Update insights
        if (lblNhanVienXuatSac != null) {
            lblNhanVienXuatSac.setText("★ Xuat sac nhat: " + (index > 0 ? topNV : "N/A"));
        }

        // Update avg per employee label
        double avgPerNV = index > 0 ? totalRevAll / index : 0;
        // Find the avg label via parent panel component traversal
        updateAvgNVLabel(avgPerNV);
    }

    // Helper to find and update the avgRevPerNV label
    private void updateAvgNVLabel(double avg) {
        // Traverse the component tree to find the label by name
        if (chartPanelEmployee != null) {
            Container parent = chartPanelEmployee.getParent();
            if (parent instanceof JSplitPane) {
                JSplitPane sp = (JSplitPane) parent;
                Component right = sp.getRightComponent();
                if (right instanceof JPanel) {
                    findAndUpdateLabel((JPanel) right, "lblAvgRevPerNV",
                            "TB doanh thu / nhan vien: " + formatCompactMoney(avg));
                }
            }
        }
    }

    private void findAndUpdateLabel(JPanel panel, String name, String text) {
        for (Component c : panel.getComponents()) {
            if (c instanceof JLabel && name.equals(c.getName())) {
                ((JLabel) c).setText(text);
                return;
            }
            if (c instanceof JPanel) {
                findAndUpdateLabel((JPanel) c, name, text);
            }
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

    private String formatCompactMoney(double amount) {
        if (amount >= 1_000_000_000) {
            return String.format("%.2fB", amount / 1_000_000_000.0); // Tỷ
        } else if (amount >= 1_000_000) {
            return String.format("%.2fM", amount / 1_000_000.0); // Triệu
        } else if (amount >= 1_000) {
            return String.format("%.2fK", amount / 1_000.0); // Ngàn
        }
        return formatMoney(amount);
    }
}
