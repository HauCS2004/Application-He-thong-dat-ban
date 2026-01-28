package GUI;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import com.toedter.calendar.JDateChooser;
import GUI.components.TableFloorPanel;
import GUI.components.TableCard;
import Entity.Ban;
import Entity.DatBan;
import DAO.DatBanDAO;
import DAO.BanDAO;
import java.util.ArrayList;
import java.util.Date;

/**
 * ManHinhDatBanV2 - Redesigned Booking Screen
 * Features: Search, Filters, Booking Table, Visual Table Floor
 */
public class ManHinhDatBanV2 extends JPanel {

    private JTextField txtSearch;
    private JDateChooser dateChooser;
    private JComboBox<String> cboTimeFilter;
    private JTable tblBookings;
    private DefaultTableModel modelBookings;
    private JTabbedPane tabFloors;
    private JPanel pnlStatusTabs;
    private String selectedStatus = "Tất cả";

    private DatBanDAO datBanDAO;
    private BanDAO banDAO;

    public ManHinhDatBanV2() {
        this.datBanDAO = new DatBanDAO();
        this.banDAO = new BanDAO();
        initUI();
        loadBookings();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(249, 250, 251));

        // Main content panel
        JPanel pnlMain = new JPanel(new BorderLayout(0, 15));
        pnlMain.setBackground(new Color(249, 250, 251));
        pnlMain.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top section
        pnlMain.add(createTopSection(), BorderLayout.NORTH);

        // Center: Split between booking table (top) and visual floor (bottom)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.5);
        splitPane.setTopComponent(createBookingTableSection());
        splitPane.setBottomComponent(createVisualFloorSection());

        pnlMain.add(splitPane, BorderLayout.CENTER);

        add(pnlMain, BorderLayout.CENTER);
    }

    private JPanel createTopSection() {
        JPanel pnl = new JPanel(new BorderLayout(10, 15));
        pnl.setOpaque(false);

        // Title + Button
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JPanel pnlTitle = new JPanel();
        pnlTitle.setLayout(new BoxLayout(pnlTitle, BoxLayout.Y_AXIS));
        pnlTitle.setOpaque(false);

        JLabel lblTitle = new JLabel("Danh sách đặt bàn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(31, 41, 55));

        JLabel lblSubtitle = new JLabel("Quản lý và theo dõi trạng thái đặt bàn hôm nay");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(107, 114, 128));

        pnlTitle.add(lblTitle);
        pnlTitle.add(Box.createVerticalStrut(5));
        pnlTitle.add(lblSubtitle);

        JButton btnCreate = new JButton("+ Thêm đặt bàn mới");
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setBackground(new Color(34, 197, 94));
        btnCreate.setFocusPainted(false);
        btnCreate.setBorderPainted(false);
        btnCreate.setPreferredSize(new Dimension(180, 40));
        btnCreate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCreate.addActionListener(e -> showCreateBookingDialog());

        pnlHeader.add(pnlTitle, BorderLayout.WEST);
        pnlHeader.add(btnCreate, BorderLayout.EAST);

        pnl.add(pnlHeader, BorderLayout.NORTH);
        pnl.add(createFilterSection(), BorderLayout.SOUTH);

        return pnl;
    }

    private JPanel createFilterSection() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setOpaque(false);

        // Search + Date + Time filters
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlFilters.setOpaque(false);

        // Search
        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(250, 38));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                new EmptyBorder(5, 10, 5, 10)));
        txtSearch.putClientProperty("JTextField.placeholderText", "🔍 Tìm theo tên hoặc SĐT...");

        // Date picker
        dateChooser = new JDateChooser(new Date());
        dateChooser.setPreferredSize(new Dimension(150, 38));
        dateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Time filter
        String[] timeFilters = { "Tất cả khung giờ", "Sáng (6h-12h)", "Chiều (12h-18h)", "Tối (18h-23h)" };
        cboTimeFilter = new JComboBox<>(timeFilters);
        cboTimeFilter.setPreferredSize(new Dimension(160, 38));
        cboTimeFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        pnlFilters.add(txtSearch);
        pnlFilters.add(new JLabel("📅"));
        pnlFilters.add(dateChooser);
        pnlFilters.add(new JLabel("🕐"));
        pnlFilters.add(cboTimeFilter);

        pnl.add(pnlFilters, BorderLayout.NORTH);
        pnl.add(createStatusTabs(), BorderLayout.SOUTH);

        return pnl;
    }

    private JPanel createStatusTabs() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(10, 0, 0, 0));

        String[] statuses = { "Tất cả", "Đang chờ", "Đã xác nhận", "Đã nhận bàn", "Đã hủy" };

        for (String status : statuses) {
            JButton btn = createStatusTabButton(status);
            pnl.add(btn);
        }

        return pnl;
    }

    private JButton createStatusTabButton(String status) {
        JButton btn = new JButton(status);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(120, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (status.equals(selectedStatus)) {
            btn.setBackground(new Color(31, 41, 55));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(107, 114, 128));
        }

        btn.addActionListener(e -> {
            selectedStatus = status;
            refreshStatusTabs();
            loadBookings();
        });

        return btn;
    }

    private void refreshStatusTabs() {
        pnlStatusTabs = createStatusTabs();
        // Trigger UI refresh
    }

    private JPanel createBookingTableSection() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                new EmptyBorder(10, 10, 10, 10)));

        // Table
        String[] columns = { "", "Tên khách hàng", "Số điện thoại", "SL", "Thời gian đặt", "Số bàn", "Trạng thái",
                "⋮" };
        modelBookings = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only action column
            }
        };

        tblBookings = new JTable(modelBookings);
        tblBookings.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblBookings.setRowHeight(50);
        tblBookings.setShowGrid(false);
        tblBookings.setIntercellSpacing(new Dimension(0, 0));
        tblBookings.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblBookings.getTableHeader().setBackground(new Color(249, 250, 251));

        // Set column widths
        tblBookings.getColumnModel().getColumn(0).setPreferredWidth(50); // Avatar
        tblBookings.getColumnModel().getColumn(1).setPreferredWidth(150); // Name
        tblBookings.getColumnModel().getColumn(2).setPreferredWidth(120); // Phone
        tblBookings.getColumnModel().getColumn(3).setPreferredWidth(40); // SL
        tblBookings.getColumnModel().getColumn(4).setPreferredWidth(130); // Time
        tblBookings.getColumnModel().getColumn(5).setPreferredWidth(80); // Table
        tblBookings.getColumnModel().getColumn(6).setPreferredWidth(120); // Status
        tblBookings.getColumnModel().getColumn(7).setPreferredWidth(50); // Actions

        JScrollPane scroll = new JScrollPane(tblBookings);
        scroll.setBorder(null);

        pnl.add(scroll, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel createVisualFloorSection() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(249, 250, 251));

        // Tabs: ALL + individual floors
        tabFloors = new JTabbedPane();
        tabFloors.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // ALL tab - shows all tables
        TableFloorPanel allPanel = new TableFloorPanel("ALL");
        tabFloors.addTab("Tất cả", allPanel);

        // Individual floor tabs
        tabFloors.addTab("Tầng G", new TableFloorPanel("KV01"));
        tabFloors.addTab("Tầng 1", new TableFloorPanel("KV02"));
        tabFloors.addTab("VIP Room", new TableFloorPanel("KV03"));
        tabFloors.addTab("Ngoài trời", new TableFloorPanel("KV04"));

        pnl.add(tabFloors, BorderLayout.CENTER);

        return pnl;
    }

    private void loadBookings() {
        modelBookings.setRowCount(0);

        Date selectedDate = dateChooser.getDate();
        ArrayList<DatBan> bookings = datBanDAO.getDanhSachDatBan(selectedDate, selectedDate);

        for (DatBan booking : bookings) {
            // Filter by status if not "Tất cả"
            if (!selectedStatus.equals("Tất cả") && !booking.getTrangThai().equals(selectedStatus)) {
                continue;
            }

            Object[] row = {
                    getInitials(booking.getTenKhach()), // Fixed method name
                    booking.getTenKhach(), // Fixed method name
                    booking.getSdt(), // Fixed method name
                    booking.getSoLuongKhach(),
                    formatDateTime(booking.getThoiGianBatDau()),
                    booking.getMaBan(),
                    booking.getTrangThai(),
                    "⋮"
            };

            modelBookings.addRow(row);
        }
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty())
            return "?";
        String[] parts = name.split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return name.substring(0, 1).toUpperCase();
    }

    private String formatDateTime(Date date) {
        if (date == null)
            return "";
        return new java.text.SimpleDateFormat("HH:mm").format(date) + "\nHôm nay";
    }

    private void showCreateBookingDialog() {
        JOptionPane.showMessageDialog(this,
                "Chức năng Tạo Đặt Bàn\n(Sẽ được implement với visual table selector)",
                "Thêm Đặt Bàn",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
