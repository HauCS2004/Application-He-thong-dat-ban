package GUI;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.event.*;
import com.toedter.calendar.JDateChooser;
import GUI.components.TableFloorPanel;
import GUI.components.TableCard;
import Entity.Ban;
import Entity.DatBan;
import Entity.HoaDon;
import DAO.DatBanDAO;
import DAO.BanDAO;
import DAO.HoaDonDAO;
import DAO.KhachHangDAO;
import java.util.ArrayList;
import java.util.Date;

/**
 * ManHinhDatBanV2 - Redesigned Booking Screen
 * Features: Search, Filters, Booking Table, Visual Table Floor
 */
public class ManHinhDatBanV2 extends JPanel implements TableCard.TableCardListener {

    private JTextField txtSearch;
    private JDateChooser dateChooser; // Optional date filter
    private JButton btnNotify; // Notification button
    private JComboBox<String> cboTimeFilter;
    private JTable tblBookings;
    private DefaultTableModel modelBookings;
    private JTabbedPane tabFloors;
    private JPanel pnlStatusTabs;
    private String selectedStatus = "Tất cả";
    private JPopupMenu popupTableAction;

    // Map Filter UI
    private JDateChooser dateMap;
    private JComboBox<String> cboMapGio, cboMapPhut;
    private JButton btnCheckMap, btnResetMap;

    // Detail Panel
    private JPanel pnlDetailContainer;
    private JPanel pnlDetailContent;
    private java.util.ArrayList<Entity.DatBan> dailyBookings;

    private DatBanDAO datBanDAO;
    private BanDAO banDAO;
    private HoaDonDAO hoaDonDAO;

    // --- FILTER STATE ---
    private boolean isFilterActive = false;
    private Date filterDate;
    private int filterHour;
    private int filterMinute;

    public ManHinhDatBanV2() {
        this.datBanDAO = new DatBanDAO();
        this.banDAO = new BanDAO();
        this.hoaDonDAO = new HoaDonDAO();

        initUI();
        initTablePopupMenu();
        loadBookings();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(249, 250, 251));

        // Create Main Tabbed Pane
        JTabbedPane mainTabs = new JTabbedPane();
        mainTabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainTabs.setBackground(Color.WHITE);

        // TAB 1: Sơ Đồ Bàn (Visual Map)
        // Combine Map Filter + Floor Tabs
        JPanel pnlMapTab = new JPanel(new BorderLayout());
        pnlMapTab.setBackground(new Color(249, 250, 251));
        pnlMapTab.add(createVisualFloorSection(), BorderLayout.CENTER);

        mainTabs.addTab("Sơ Đồ Bàn", new ImageIcon("view/icons/table.png"), pnlMapTab);

        // TAB 2: Danh Sách Đặt Bàn (Booking List)
        // Combine Top Search Filters + Table
        JPanel pnlListTab = new JPanel(new BorderLayout(0, 15));
        pnlListTab.setBackground(new Color(249, 250, 251));
        pnlListTab.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header for List
        pnlListTab.add(createListHeaderSection(), BorderLayout.NORTH);
        pnlListTab.add(createBookingTableSection(), BorderLayout.CENTER);

        mainTabs.addTab("Danh Sách Đặt Bàn", new ImageIcon("view/icons/menu.png"), pnlListTab);

        add(mainTabs, BorderLayout.CENTER);
    }

    private JPanel createListHeaderSection() {
        JPanel pnl = new JPanel(new BorderLayout(10, 15));
        pnl.setOpaque(false);

        // Title + Create Button
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JPanel pnlTitle = new JPanel();
        pnlTitle.setLayout(new BoxLayout(pnlTitle, BoxLayout.Y_AXIS));
        pnlTitle.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý đặt bàn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(31, 41, 55));

        JLabel lblSubtitle = new JLabel("Xem và quản lý danh sách đặt bàn");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(107, 114, 128));

        pnlTitle.add(lblTitle);
        pnlTitle.add(Box.createVerticalStrut(5));
        pnlTitle.add(lblSubtitle);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setOpaque(false);

        btnNotify = new JButton("🔔");
        btnNotify.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNotify.setForeground(new Color(234, 179, 8)); // Yellow/Gold
        btnNotify.setBackground(Color.WHITE);
        btnNotify.setFocusPainted(false);
        btnNotify.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        btnNotify.setPreferredSize(new Dimension(50, 40));
        btnNotify.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNotify.setToolTipText("Thông báo đặt bàn");
        btnNotify.addActionListener(e -> showNotificationDialog());
        pnlActions.add(btnNotify);

        JButton btnCreate = new JButton("+ Thêm đặt bàn mới");
        btnCreate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCreate.setForeground(Color.WHITE);
        btnCreate.setBackground(new Color(34, 197, 94));
        btnCreate.setFocusPainted(false);
        btnCreate.setBorderPainted(false);
        btnCreate.setPreferredSize(new Dimension(180, 40));
        btnCreate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCreate.addActionListener(e -> {
            if (isFilterActive) {
                showCreateBookingDialog(null, filterDate, filterHour, filterMinute);
            } else {
                showCreateBookingDialog(null);
            }
        });
        pnlActions.add(btnCreate);

        pnlHeader.add(pnlTitle, BorderLayout.WEST);
        pnlHeader.add(pnlActions, BorderLayout.EAST);

        pnl.add(pnlHeader, BorderLayout.NORTH);
        pnl.add(createFilterSection(), BorderLayout.SOUTH);

        return pnl;
    }

    private JCheckBox chkEnableDateFilter; // New Checkbox

    // Kept createFilterSection as is, but it is now inside createListHeaderSection
    private JPanel createFilterSection() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setOpaque(false);
        // ... (rest of search/filter UI)

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
        txtSearch.addActionListener(e -> loadBookings()); // Enter to search

        // Date picker toggle
        chkEnableDateFilter = new JCheckBox("Lọc: ");
        chkEnableDateFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkEnableDateFilter.setOpaque(false);
        chkEnableDateFilter.addActionListener(e -> {
            dateChooser.setEnabled(chkEnableDateFilter.isSelected());
            loadBookings();
        });

        // Date picker
        dateChooser = new JDateChooser(new Date());
        dateChooser.setPreferredSize(new Dimension(150, 38));
        dateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateChooser.setEnabled(false); // Default DISABLED (Show all recent)
        dateChooser.addPropertyChangeListener("date", e -> loadBookings());

        // Time filter
        String[] timeFilters = { "Tất cả khung giờ", "Sáng (6h-12h)", "Chiều (12h-18h)", "Tối (18h-23h)" };
        cboTimeFilter = new JComboBox<>(timeFilters);
        cboTimeFilter.setPreferredSize(new Dimension(160, 38));
        cboTimeFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboTimeFilter.addActionListener(e -> loadBookings());

        pnlFilters.add(txtSearch);
        pnlFilters.add(chkEnableDateFilter); // Add toggle
        pnlFilters.add(dateChooser);
        pnlFilters.add(new JLabel("🕐"));
        pnlFilters.add(cboTimeFilter);

        JButton btnReload = new JButton("↻");
        btnReload.setPreferredSize(new Dimension(40, 38));
        btnReload.addActionListener(e -> loadBookings());
        pnlFilters.add(btnReload);

        pnl.add(pnlFilters, BorderLayout.NORTH);
        pnl.add(createStatusTabs(), BorderLayout.SOUTH);

        return pnl;
    }

    private JPanel createStatusTabs() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(10, 0, 0, 0));

        // SIMPLIFIED STATUSES
        String[] statuses = { "Tất cả", "Chờ xác nhận", "Đã nhận bàn", "Đã hủy" };

        for (String status : statuses) {
            String label = status;
            if ("Chờ xác nhận".equals(status))
                label = "Đang chờ"; // UI Label rename

            JButton btn = createStatusTabButton(status, label); // Pass value and label
            pnl.add(btn);
        }

        return pnl;
    }

    private JButton createStatusTabButton(String statusValue, String statusLabel) {
        JButton btn = new JButton(statusLabel);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(120, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (statusValue.equals(selectedStatus)) {
            btn.setBackground(new Color(31, 41, 55));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(107, 114, 128));
        }

        btn.addActionListener(e -> {
            selectedStatus = statusValue; // Use Value for logic

            // Refresh Tabs UI
            Component[] comps = btn.getParent().getComponents();
            for (Component c : comps) {
                if (c instanceof JButton) {
                    ((JButton) c).setBackground(Color.WHITE);
                    ((JButton) c).setForeground(new Color(107, 114, 128));
                }
            }
            btn.setBackground(new Color(31, 41, 55));
            btn.setForeground(Color.WHITE);

            loadBookings();
        });

        return btn;
    }

    // Overload for backward compatibility
    private JButton createStatusTabButton(String status) {
        return createStatusTabButton(status, status);
    }

    private JPanel createBookingTableSection() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                new EmptyBorder(10, 10, 10, 10)));

        // Table
        String[] columns = { "MaDat", "", "Tên khách hàng", "Số điện thoại", "SL", "Ngày đặt", "Giờ", "Số bàn",
                "Trạng thái", "MaBan" };
        modelBookings = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblBookings = new JTable(modelBookings);
        tblBookings.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblBookings.setRowHeight(50);
        tblBookings.setShowGrid(false);
        tblBookings.setIntercellSpacing(new Dimension(0, 0));
        tblBookings.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblBookings.getTableHeader().setBackground(new Color(249, 250, 251));

        // Hide ID columns
        tblBookings.getColumnModel().getColumn(0).setMinWidth(0);
        tblBookings.getColumnModel().getColumn(0).setMaxWidth(0);
        tblBookings.getColumnModel().getColumn(0).setWidth(0);

        tblBookings.getColumnModel().getColumn(9).setMinWidth(0);
        tblBookings.getColumnModel().getColumn(9).setMaxWidth(0);
        tblBookings.getColumnModel().getColumn(9).setWidth(0);

        // Set column widths
        tblBookings.getColumnModel().getColumn(1).setPreferredWidth(40); // Avatar
        tblBookings.getColumnModel().getColumn(2).setPreferredWidth(140); // Name
        tblBookings.getColumnModel().getColumn(3).setPreferredWidth(90); // Phone
        tblBookings.getColumnModel().getColumn(4).setPreferredWidth(40); // SL
        tblBookings.getColumnModel().getColumn(5).setPreferredWidth(90); // Date
        tblBookings.getColumnModel().getColumn(6).setPreferredWidth(60); // Time
        tblBookings.getColumnModel().getColumn(7).setPreferredWidth(70); // Table
        tblBookings.getColumnModel().getColumn(8).setPreferredWidth(120); // Status

        // --- IMPROVEMENT: Apply Custom Status Renderer ---
        tblBookings.getColumnModel().getColumn(8).setCellRenderer(new GUI.components.BookingStatusRenderer()); // Status
                                                                                                               // Column

        // Popup Menu Action
        tblBookings.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int r = tblBookings.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < tblBookings.getRowCount()) {
                        tblBookings.setRowSelectionInterval(r, r);
                        showPopupAction(e);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblBookings);
        scroll.setBorder(null);

        pnl.add(scroll, BorderLayout.CENTER);

        return pnl;
    }

    private void showPopupAction(MouseEvent e) {
        int row = tblBookings.getSelectedRow();
        String status = modelBookings.getValueAt(row, 8).toString(); // Col 8 is Status

        JPopupMenu popup = new JPopupMenu();

        if ("Chờ xác nhận".equals(status) || "Đã xác nhận".equals(status)) {
            JMenuItem itemCheckIn = new JMenuItem("✅ Nhận Bàn (Check-in)");
            itemCheckIn.addActionListener(ev -> checkInBookingFromTable(row));
            popup.add(itemCheckIn);

            JMenuItem itemCancel = new JMenuItem("❌ Hủy Đặt");
            itemCancel.addActionListener(ev -> cancelBookingFromTable(row));
            popup.add(itemCancel);
        }

        if (popup.getComponentCount() > 0)
            popup.show(e.getComponent(), e.getX(), e.getY());
    }

    private void openOrderUIFromTable(int row) {
        String maBan = modelBookings.getValueAt(row, 9).toString();
        String tenKhach = modelBookings.getValueAt(row, 2).toString();

        // Find or Create Invoice for this table
        int maHD = hoaDonDAO.getMaHDByBan(maBan);
        if (maHD == -1) {
            // Create first invoice
            // Need basic info. Use generic or find booking info again?
            // Since we have active booking, we can use it.
            int soKhach = Integer.parseInt(modelBookings.getValueAt(row, 4).toString());
            String sdt = modelBookings.getValueAt(row, 3).toString();

            HoaDon hd = new HoaDon(maBan, soKhach, sdt, "Khách đặt: " + tenKhach, null);
            maHD = hoaDonDAO.insert(hd);
            // Update table status just in case
            banDAO.updateTrangThai(maBan, "Có Khách");
        }

        if (maHD != -1) {
            ManHinhGoiMon screen = new ManHinhGoiMon(maHD, "Bàn " + maBan, false);
            screen.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Không thể tạo hóa đơn!");
        }
    }

    // ACTION HANDLERS
    private void checkInBookingFromTable(int row) {
        int maDat = Integer.parseInt(modelBookings.getValueAt(row, 0).toString());
        String maBan = modelBookings.getValueAt(row, 9).toString(); // Col 9 is MaBan
        String tenKhach = modelBookings.getValueAt(row, 2).toString();
        String sdt = modelBookings.getValueAt(row, 3).toString();

        // Create explicit Current Time
        java.util.Date now = new java.util.Date();

        // Re-fetch booking from DAO to check time constraints safely
        Entity.DatBan dbCheck = datBanDAO.getDatBanByID(maDat);
        if (dbCheck != null) {
            long diffMillis = now.getTime() - dbCheck.getThoiGianBatDau().getTime();
            long diffMinutes = diffMillis / (60 * 1000);

            // Allow check-in:
            // 1. If it's within 60 mins before start (early arrival)
            // 2. If it's within 60 mins after start (late arrival)
            // User request: "trước hoặc sau đó 30p"

            if (diffMinutes < -30) {
                JOptionPane.showMessageDialog(this, "Chưa đến giờ check-in! (Chỉ được check-in trước 30p)", "Cảnh báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            // Fail-safe: If we can't verify time, don't allow check-in
            JOptionPane.showMessageDialog(this, "Không thể xác thực thông tin đặt bàn (Lỗi dữ liệu). Vui lòng thử lại!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận KHÁCH ĐÃ ĐẾN (Check-in) - Bàn " + maBan + "?",
                "Check-in", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // 1. Create/Verify Customer
            KhachHangDAO khDAO = new KhachHangDAO();
            if (!khDAO.checkTonTai(sdt)) {
                // Try to create new customer
                boolean createdKhach = khDAO.themKhachMoi(sdt, tenKhach);
                if (!createdKhach) {
                    JOptionPane.showMessageDialog(this,
                            "Lỗi: Không thể tạo thông tin khách hàng mới!\nVui lòng kiểm tra lại SĐT và Tên.",
                            "Lỗi Check-in", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // 2. Create Invoice
            HoaDon hd = new HoaDon(maBan, Integer.parseInt(modelBookings.getValueAt(row, 4).toString()), sdt,
                    "Khách đặt: " + tenKhach, null);
            int maHD = hoaDonDAO.insert(hd);

            if (maHD == -1) {
                JOptionPane.showMessageDialog(this, "Lỗi: Không thể tạo hóa đơn! (Có thể do lỗi dữ liệu khách hàng)",
                        "Lỗi Check-in", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Update Statuses ONLY if Invoice created
            banDAO.updateTrangThai(maBan, "Có Khách");
            boolean updatedBooking = datBanDAO.capNhatTrangThai(maDat, "Đã nhận bàn");

            if (updatedBooking) {
                JOptionPane.showMessageDialog(this, "Check-in thành công! Hóa đơn #" + maHD + " đã được tạo.");
                loadBookings(); // Refresh List
                refreshAllFloors(); // Refresh Map
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật trạng thái đặt bàn!");
            }
        }
    }

    private void cancelBookingFromTable(int row) {
        int maDat = Integer.parseInt(modelBookings.getValueAt(row, 0).toString());
        String maBan = modelBookings.getValueAt(row, 9).toString(); // Col 9 is MaBan

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn HỦY đơn đặt này?", "Hủy Đặt",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (datBanDAO.capNhatTrangThai(maDat, "Đã hủy")) {
                // Check if need to reset table? No, because table is "Da Dat", but wait,
                // if datetime is now, table might be "Da Dat".
                // Logic complex. Simple: Just update Booking status.
                // Table status update is handled by Timer in Main or here?
                // For immediate effect:
                banDAO.updateTrangThai(maBan, "Trống");

                JOptionPane.showMessageDialog(this, "Đã hủy đơn đặt!");
                loadBookings();
                refreshAllFloors();
            }
        }
    }

    private JPanel createVisualFloorSection() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(249, 250, 251));

        // --- NEW: Map Availability Filter ---
        JPanel pnlMapFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlMapFilter.setBackground(new Color(240, 240, 240));
        pnlMapFilter.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel lblMapFilter = new JLabel("🔍 Kiểm tra bàn trống lúc:");
        lblMapFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JDateChooser dateMap = new JDateChooser(new Date());
        dateMap.setDateFormatString("dd/MM/yyyy");
        dateMap.setPreferredSize(new Dimension(110, 30));

        JComboBox<String> cboMapGio = new JComboBox<>();
        for (int h = 7; h <= 22; h++)
            cboMapGio.addItem(String.format("%02d", h));
        cboMapGio.setSelectedItem("18"); // Default 18h

        JComboBox<String> cboMapPhut = new JComboBox<>();
        cboMapPhut.addItem("00");
        cboMapPhut.addItem("30");

        JButton btnCheckMap = new JButton("Xem Trạng Thái");
        btnCheckMap.setBackground(new Color(59, 130, 246));
        btnCheckMap.setForeground(Color.WHITE);
        btnCheckMap.setFocusPainted(false);

        JButton btnResetMap = new JButton("Reset");
        btnResetMap.setFocusPainted(false);

        pnlMapFilter.add(lblMapFilter);
        pnlMapFilter.add(dateMap);
        pnlMapFilter.add(cboMapGio);
        pnlMapFilter.add(new JLabel(":"));
        pnlMapFilter.add(cboMapPhut);
        pnlMapFilter.add(btnCheckMap);
        pnlMapFilter.add(btnResetMap);

        pnl.add(pnlMapFilter, BorderLayout.NORTH);
        // ------------------------------------

        // Tabs: ALL + individual floors
        tabFloors = new JTabbedPane();
        tabFloors.setFont(new Font("Segoe UI", Font.BOLD, 13));

        addFloorTab("Tất cả", "ALL");
        addFloorTab("Tầng G", "KV01");
        addFloorTab("Tầng 1", "KV02");
        addFloorTab("VIP Room", "KV03");
        addFloorTab("Ngoài trời", "KV04");

        // --- SPLIT PANE ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabFloors, createDetailPanel());
        splitPane.setResizeWeight(0.85); // Map takes 85%
        splitPane.setDividerSize(5);
        pnl.add(splitPane, BorderLayout.CENTER);

        // --- Event Handling ---
        btnCheckMap.addActionListener(e -> {
            checkMapAvailability(dateMap.getDate(),
                    Integer.parseInt(cboMapGio.getSelectedItem().toString()),
                    Integer.parseInt(cboMapPhut.getSelectedItem().toString()));
        });

        btnResetMap.addActionListener(e -> {
            for (int i = 0; i < tabFloors.getTabCount(); i++) {
                Component c = tabFloors.getComponentAt(i);
                if (c instanceof TableFloorPanel) {
                    ((TableFloorPanel) c).restoreOriginalStatuses();
                }
            }
        });

        return pnl;
    }

    private void checkMapAvailability(Date date, int hour, int minute) {
        if (date == null)
            return;

        // 1. Calculate Target Time
        this.isFilterActive = true;
        this.filterDate = date;
        this.filterHour = hour;
        this.filterMinute = minute;

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date); // Sets YMD and Time from date object
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
        cal.set(java.util.Calendar.MINUTE, minute);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date targetTime = cal.getTime();

        System.out.println("DEBUG: Check Availability for " + targetTime);

        // 2. Get Bookings for that Date
        this.dailyBookings = datBanDAO.getDanhSachDatBan(date, date);
        ArrayList<DatBan> listBooking = this.dailyBookings;

        // 3. Identify Occupied Tables and Map to Booking
        java.util.Map<String, Entity.DatBan> bookingMap = new java.util.HashMap<>();

        for (DatBan db : listBooking) {
            // Skip canceled
            // Skip canceled or completed (History)
            if (db.getTrangThai().startsWith("Đã hủy") || db.getTrangThai().equals("Hoàn thành"))
                continue;

            // Check overlaps: Start <= Target < End
            // NORMALIZE TO MINUTES to avoid second-mismatch issues
            java.util.Calendar calB = java.util.Calendar.getInstance();
            calB.setTime(db.getThoiGianBatDau());
            calB.set(java.util.Calendar.SECOND, 0);
            calB.set(java.util.Calendar.MILLISECOND, 0);
            long startMillis = calB.getTimeInMillis();

            calB.setTime(db.getThoiGianKetThuc());
            calB.set(java.util.Calendar.SECOND, 0);
            calB.set(java.util.Calendar.MILLISECOND, 0);
            long endMillis = calB.getTimeInMillis();

            long targetMillis = targetTime.getTime();

            if (startMillis <= targetMillis && targetMillis < endMillis) {
                bookingMap.put(db.getMaBan(), db);
                System.out.println("DEBUG: Table " + db.getMaBan() + " OCCUPIED at " + targetTime);
            } else {
                // System.out.println("DEBUG: Table " + db.getMaBan() + " FREE at " + targetTime
                // + " (Start: " + db.getThoiGianBatDau() + ", End: " + db.getThoiGianKetThuc()
                // + ")");
            }
        }

        // 4. Update UI
        // Apply to Panels
        for (int i = 0; i < tabFloors.getTabCount(); i++) {
            Component c = tabFloors.getComponentAt(i);
            if (c instanceof TableFloorPanel) {
                TableFloorPanel tfp = (TableFloorPanel) c;
                tfp.updateTableStatuses(bookingMap);
                tfp.revalidate();
                tfp.repaint();
            }
        }
    }

    private void addFloorTab(String title, String zoneId) {
        TableFloorPanel pnl = new TableFloorPanel(zoneId);
        pnl.setTableCardListener(table -> {
            // Update Detail Panel on Click
            updateDetailPanel(table);
        });
        tabFloors.addTab(title, pnl);
    }

    private void refreshAllFloors() {
        for (int i = 0; i < tabFloors.getTabCount(); i++) {
            Component c = tabFloors.getComponentAt(i);
            if (c instanceof TableFloorPanel) {
                ((TableFloorPanel) c).refreshTables();
            }
        }
    }

    private void loadBookings() {
        // 1. Auto-maintenance
        datBanDAO.autoCancelOverdueBookings();
        datBanDAO.syncTableStatus();

        // 2. Load Data
        modelBookings.setRowCount(0);

        ArrayList<DatBan> bookings;

        // --- NEW: Toggle Logic ---
        if (chkEnableDateFilter != null && chkEnableDateFilter.isSelected()) {
            Date selectedDate = dateChooser.getDate();
            bookings = datBanDAO.getDanhSachDatBan(selectedDate, selectedDate);
        } else {
            // Fetch ALL Recent (Limit 100)
            bookings = datBanDAO.getDanhSachDatBanGanDay(100);

            // For Detail Panel consistency in "Today" view (default), maybe fetching Today
            // is better?
            // Or just use what we have. If we have recent bookings, filter by today might
            // be empty if we rely on "bookings" list.
            // BETTER: Explicitly load today's bookings for the Detail Panel if filter is
            // OFF.
            this.dailyBookings = datBanDAO.getDanhSachDatBan(new Date(), new Date());
        }

        if (chkEnableDateFilter != null && chkEnableDateFilter.isSelected()) {
            this.dailyBookings = bookings;
        }

        // 3. Update Notifications
        checkAlerts();
        // -------------------------

        for (DatBan booking : bookings) {
            // Filter by status if not "Tất cả"
            boolean matchStatus = false;
            if (selectedStatus.equals("Tất cả")) {
                matchStatus = true;
            } else if (selectedStatus.equals("Đã hủy")) {
                if (booking.getTrangThai().startsWith("Đã hủy"))
                    matchStatus = true;
            } else {
                if (booking.getTrangThai().equals(selectedStatus))
                    matchStatus = true;
            }

            if (!matchStatus)
                continue;
            // Filter by text search
            String search = txtSearch.getText().toLowerCase();
            if (!search.isEmpty() && !booking.getTenKhach().toLowerCase().contains(search)
                    && !booking.getSdt().contains(search)) {
                continue;
            }

            Object[] row = {
                    booking.getMaDat(),
                    getInitials(booking.getTenKhach()),
                    booking.getTenKhach(),
                    booking.getSdt(),
                    booking.getSoLuongKhach(),
                    new java.text.SimpleDateFormat("dd/MM/yyyy").format(booking.getThoiGianBatDau()), // Date
                    new java.text.SimpleDateFormat("HH:mm").format(booking.getThoiGianBatDau()), // Time
                    booking.getMaBan(),
                    booking.getTrangThai(),
                    booking.getMaBan()
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
        return new java.text.SimpleDateFormat("HH:mm").format(date);
    }

    private void showCreateBookingDialog(String preSelectedTable, Date date, int hour, int min) {
        Window win = SwingUtilities.getWindowAncestor(this);
        BookingFormDialog dialog = new BookingFormDialog((Frame) win, preSelectedTable);
        if (date != null && hour >= 0) {
            dialog.setPreFilledTime(date, hour, min);
        }
        dialog.setOnSuccessCallback(() -> {
            loadBookings();
            if (isFilterActive) {
                // Re-check map availability to refresh view
                checkMapAvailability(filterDate, filterHour, filterMinute);
            } else {
                refreshAllFloors();
            }
        });
        dialog.setVisible(true);
    }

    // Overload for backward compatibility / button click
    private void showCreateBookingDialog(String preSelectedTable) {
        showCreateBookingDialog(preSelectedTable, null, -1, -1);
    }

    // --- TABLE INTERACTIONS ---
    private void initTablePopupMenu() {
        popupTableAction = new JPopupMenu();
        JMenuItem itemBook = new JMenuItem("Đặt bàn này");
        popupTableAction.add(itemBook);
        itemBook.addActionListener(e -> {
        });
    }

    // ... (rest of code)

    @Override
    public void onTableCardClicked(Ban table) {
        // REFINEMENT: Smart Click Logic
        if (isFilterActive) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Đặt bàn " + table.getTenBan() + " vào lúc " +
                            String.format("%02d:%02d", filterHour, filterMinute) + " ngày " +
                            new java.text.SimpleDateFormat("dd/MM").format(filterDate) + "?",
                    "Đặt Bàn Theo Lịch",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                showCreateBookingDialog(table.getMaBan(), filterDate, filterHour, filterMinute);
            }
            return;
        }

        // Standard Logic (Real-time)
        String status = table.getTrangThai();
        if ("Trống".equals(status)) {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn muốn đặt bàn " + table.getTenBan() + "?", "Đặt Bàn",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                showCreateBookingDialog(table.getMaBan());
            }
        } else if ("Đã Đặt".equals(status)) {
            // Find booking info
            DatBan db = DatBanDAO.getDatBanGanNhat(table.getMaBan());
            if (db != null) {
                JOptionPane.showMessageDialog(this,
                        "Thông tin đặt bàn:\n" +
                                "Khách: " + db.getTenKhach() + "\n" +
                                "SĐT: " + db.getSdt() + "\n" +
                                "Giờ: " + formatDateTime(db.getThoiGianBatDau()));
            }
        } else if ("Có Khách".equals(status)) {
            JOptionPane.showMessageDialog(this, "Bàn đang có khách ngồi.");
        }
    }

    // --- NOTIFICATION SYSTEM ---
    private void checkAlerts() {
        if (btnNotify == null)
            return;

        ArrayList<Entity.DatBan> upcoming = datBanDAO.getUpcomingBookings(30); // 30 mins
        ArrayList<Entity.DatBan> overdue = datBanDAO.getOverdueBookings();

        int total = upcoming.size() + overdue.size();

        if (total > 0) {
            btnNotify.setText("🔔 " + total);
            btnNotify.setForeground(new Color(220, 38, 38)); // Red
            btnNotify.setBackground(new Color(254, 226, 226)); // Light Red
        } else {
            btnNotify.setText("🔔");
            btnNotify.setForeground(new Color(234, 179, 8)); // Gold
            btnNotify.setBackground(Color.WHITE);
        }
    }

    private void showNotificationDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thông báo đặt bàn", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // 1. Upcoming Panel
        JPanel pnlUpcoming = new JPanel(new BorderLayout());
        ArrayList<Entity.DatBan> upcoming = datBanDAO.getUpcomingBookings(30);
        if (upcoming.isEmpty()) {
            pnlUpcoming.add(new JLabel("Không có đặt bàn nào sắp đến giờ (30p)", SwingConstants.CENTER));
        } else {
            String[] cols = { "Mã", "Bàn", "Khách", "SĐT", "Giờ đến" };
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            for (Entity.DatBan db : upcoming) {
                model.addRow(new Object[] {
                        db.getMaDat(), db.getMaBan(), db.getTenKhach(), db.getSdt(),
                        new java.text.SimpleDateFormat("HH:mm").format(db.getThoiGianBatDau())
                });
            }
            JTable table = new JTable(model);
            table.setRowHeight(30);
            pnlUpcoming.add(new JScrollPane(table));
        }
        tabbedPane.addTab("Sắp đến (" + upcoming.size() + ")", pnlUpcoming);

        // 2. Overdue Panel
        JPanel pnlOverdue = new JPanel(new BorderLayout());
        ArrayList<Entity.DatBan> overdue = datBanDAO.getOverdueBookings();
        if (overdue.isEmpty()) {
            pnlOverdue.add(new JLabel("Không có đặt bàn nào quá giờ", SwingConstants.CENTER));
        } else {
            String[] cols = { "Mã", "Bàn", "Khách", "SĐT", "Giờ hẹn" };
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            for (Entity.DatBan db : overdue) {
                model.addRow(new Object[] {
                        db.getMaDat(), db.getMaBan(), db.getTenKhach(), db.getSdt(),
                        new java.text.SimpleDateFormat("HH:mm").format(db.getThoiGianBatDau())
                });
            }
            JTable table = new JTable(model);
            table.setRowHeight(30);

            // Add Cancel Action
            JButton btnCancelAll = new JButton("Hủy và Dọn tất cả bàn quá giờ");
            btnCancelAll.addActionListener(e -> {
                int count = datBanDAO.autoCancelOverdueBookings();
                JOptionPane.showMessageDialog(dialog, "Đã hủy " + count + " đơn đặt quá giờ.");
                dialog.dispose();
                loadBookings();
                refreshAllFloors();
                // --- NEW: Split View Logic ---
            });

            pnlOverdue.add(new JScrollPane(table), BorderLayout.CENTER);
            pnlOverdue.add(btnCancelAll, BorderLayout.SOUTH);
        }
        tabbedPane.addTab("Quá giờ (" + overdue.size() + ")", pnlOverdue);

        dialog.add(tabbedPane, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // --- NEW: Split View Logic ---

    private JPanel createDetailPanel() {
        pnlDetailContainer = new JPanel(new BorderLayout());
        pnlDetailContainer.setPreferredSize(new Dimension(300, 0));
        pnlDetailContainer.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(229, 231, 235)));
        pnlDetailContainer.setBackground(Color.WHITE);

        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(243, 244, 246));
        pnlHeader.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel lblTitle = new JLabel("THÔNG TIN BÀN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(31, 41, 55));
        pnlHeader.add(lblTitle, BorderLayout.CENTER);
        pnlDetailContainer.add(pnlHeader, BorderLayout.NORTH);

        // Content
        pnlDetailContent = new JPanel();
        pnlDetailContent.setLayout(new BoxLayout(pnlDetailContent, BoxLayout.Y_AXIS));
        pnlDetailContent.setBackground(Color.WHITE);
        pnlDetailContent.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Initial Empty State
        showEmptyDetailState();

        pnlDetailContainer.add(pnlDetailContent, BorderLayout.CENTER);
        return pnlDetailContainer;
    }

    private void showEmptyDetailState() {
        if (pnlDetailContent == null)
            return;
        pnlDetailContent.removeAll();
        pnlDetailContent.add(Box.createVerticalGlue());
        JLabel lblIcon = new JLabel("👈");
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlDetailContent.add(lblIcon);

        JLabel lblMsg = new JLabel("Chọn bàn để xem chi tiết");
        lblMsg.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblMsg.setForeground(Color.GRAY);
        lblMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlDetailContent.add(lblMsg);
        pnlDetailContent.add(Box.createVerticalGlue());
        pnlDetailContent.revalidate();
        pnlDetailContent.repaint();
    }

    private void updateDetailPanel(Entity.Ban table) {
        if (pnlDetailContent == null)
            return;
        pnlDetailContent.removeAll();

        // 1. Table Header
        JLabel lblName = new JLabel(table.getTenBan());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDetailContent.add(lblName);

        // Status Label (Visual only)
        /*
         * If filter is active, this status might be "Trống" even if DB says "Có Khách".
         * But table object passed here comes from the Card, which respects the filter.
         */
        // Status Label Logic
        String displayStatus = table.getTrangThai();

        // If filtering, we double-check availability for the specific time slot
        if (isFilterActive) {
            boolean isOccupiedInFilter = false;
            if (dailyBookings != null) {
                // Determine filter target time
                java.util.Calendar calF = java.util.Calendar.getInstance();
                calF.setTime(filterDate);
                calF.set(java.util.Calendar.HOUR_OF_DAY, filterHour);
                calF.set(java.util.Calendar.MINUTE, filterMinute);
                calF.set(java.util.Calendar.SECOND, 0);
                calF.set(java.util.Calendar.MILLISECOND, 0);
                long targetMillis = calF.getTimeInMillis();

                for (Entity.DatBan db : dailyBookings) {
                    // Check for this table and valid status
                    if (db.getMaBan().equals(table.getMaBan()) && !db.getTrangThai().startsWith("Đã hủy")) {
                        java.util.Calendar calB = java.util.Calendar.getInstance();
                        calB.setTime(db.getThoiGianBatDau());
                        calB.set(java.util.Calendar.SECOND, 0);
                        calB.set(java.util.Calendar.MILLISECOND, 0);
                        long start = calB.getTimeInMillis();

                        calB.setTime(db.getThoiGianKetThuc());
                        calB.set(java.util.Calendar.SECOND, 0);
                        calB.set(java.util.Calendar.MILLISECOND, 0);
                        long end = calB.getTimeInMillis();

                        // Check overlap
                        if (start <= targetMillis && targetMillis < end) {
                            isOccupiedInFilter = true;
                            break;
                        }
                    }
                }
            }

            if (!isOccupiedInFilter) {
                displayStatus = "Trống";
            }
        }

        JLabel lblStatus = new JLabel(displayStatus);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        if ("Trống".equals(displayStatus))
            lblStatus.setForeground(new Color(34, 197, 94));
        else if ("Có Khách".equals(displayStatus) || "Có khách".equals(displayStatus)) // Handle likely casing
            lblStatus.setForeground(new Color(239, 68, 68));
        else
            lblStatus.setForeground(new Color(245, 158, 11));

        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDetailContent.add(lblStatus);

        pnlDetailContent.add(Box.createVerticalStrut(20));

        // 2. Booking Schedule List
        JLabel lblSched = new JLabel("Lịch đặt "
                + (isFilterActive ? new java.text.SimpleDateFormat("dd/MM").format(filterDate) : "hôm nay") + ":");
        lblSched.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSched.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDetailContent.add(lblSched);
        pnlDetailContent.add(Box.createVerticalStrut(10));

        boolean hasBooking = false;
        if (dailyBookings != null) {
            for (Entity.DatBan db : dailyBookings) {
                if (db.getMaBan().equals(table.getMaBan()) && !db.getTrangThai().startsWith("Đã hủy")) {
                    hasBooking = true;
                    // Format: [HH:mm] - Name (Status)
                    String time = new java.text.SimpleDateFormat("HH:mm").format(db.getThoiGianBatDau());
                    String info = String.format("<html><b>%s</b> - %s<br/><i>%s</i></html>", time, db.getTenKhach(),
                            db.getTrangThai());

                    JPanel pItem = new JPanel(new BorderLayout());
                    pItem.setBackground(Color.WHITE);
                    pItem.setMaximumSize(new Dimension(300, 40));
                    pItem.setBorder(new EmptyBorder(5, 0, 5, 0));
                    pItem.setAlignmentX(Component.LEFT_ALIGNMENT);

                    JLabel l = new JLabel(info);
                    l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    pItem.add(l, BorderLayout.CENTER);

                    pnlDetailContent.add(pItem);
                }
            }
        }

        if (!hasBooking) {
            JLabel l = new JLabel("(Chưa có lịch đặt)");
            l.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            l.setForeground(Color.GRAY);
            pnlDetailContent.add(l);
        }

        pnlDetailContent.add(Box.createVerticalStrut(20));

        // 3. Actions
        // "Đặt Bàn Mới" - Always visible
        JButton btnBook = new JButton("Đặt Bàn Mới");
        btnBook.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBook.setBackground(new Color(34, 197, 94));
        btnBook.setForeground(Color.WHITE);
        btnBook.setFocusPainted(false);
        // Set maximum width
        btnBook.setMaximumSize(new Dimension(280, 35));

        btnBook.addActionListener(e -> {
            // Open dialog with pre-filled ID
            BookingFormDialog dialog = new BookingFormDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    table.getMaBan());

            if (isFilterActive) {
                dialog.setPreFilledTime(filterDate, filterHour, filterMinute);
            }

            dialog.setVisible(true);
            loadBookings();
            // Re-check availability to refresh data
            if (isFilterActive)
                checkMapAvailability(filterDate, filterHour, filterMinute);
            else
                refreshAllFloors();
        });
        pnlDetailContent.add(btnBook);

        // Show Check-in ONLY if status is "Đã Đặt" (Confirmed booking exists now)
        if ("Đã Đặt".equals(table.getTrangThai())) {
            pnlDetailContent.add(Box.createVerticalStrut(10));
            JButton btnCheckIn = new JButton("Check-in (Nhận bàn)");
            btnCheckIn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnCheckIn.setMaximumSize(new Dimension(280, 35));

            btnCheckIn.addActionListener(e -> {
                // Logic to find THE booking to check in
                if (dailyBookings != null) {
                    for (Entity.DatBan db : dailyBookings) {
                        if (db.getMaBan().equals(table.getMaBan()) && "Đã xác nhận".equals(db.getTrangThai())) {
                            performCheckIn(db);
                            return;
                        }
                    }
                }
                JOptionPane.showMessageDialog(this, "Không tìm thấy đơn đặt 'Đã xác nhận' nào để check-in.");
            });
            pnlDetailContent.add(btnCheckIn);
        }

        pnlDetailContent.add(Box.createVerticalGlue());
        pnlDetailContent.revalidate();
        pnlDetailContent.repaint();
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBackground(Color.WHITE);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(300, 25));
        JLabel l = new JLabel(label + " ");
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(l);
        p.add(v);
        return p;
    }

    private void performCheckIn(Entity.DatBan booking) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận KHÁCH ĐÃ ĐẾN (Check-in) - Bàn " + booking.getMaBan() + "?",
                "Check-in", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // 1. Create/Verify Customer
            DAO.KhachHangDAO khDAO = new DAO.KhachHangDAO();
            if (!khDAO.checkTonTai(booking.getSdt())) {
                boolean createdKhach = khDAO.themKhachMoi(booking.getSdt(), booking.getTenKhach());
                if (!createdKhach) {
                    JOptionPane.showMessageDialog(this,
                            "Lỗi: Không thể tạo thông tin khách hàng mới!\nVui lòng kiểm tra lại SĐT và Tên.",
                            "Lỗi Check-in", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // 2. Create Invoice
            HoaDon hd = new HoaDon(booking.getMaBan(), booking.getSoLuongKhach(), booking.getSdt(),
                    "Khách đặt: " + booking.getTenKhach(), null);
            int maHD = hoaDonDAO.insert(hd);

            if (maHD == -1) {
                JOptionPane.showMessageDialog(this, "Lỗi: Không thể tạo hóa đơn! (Có thể do lỗi dữ liệu khách hàng)",
                        "Lỗi Check-in", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Update Statuses ONLY if Invoice created
            banDAO.updateTrangThai(booking.getMaBan(), "Có Khách");
            boolean updatedBooking = datBanDAO.capNhatTrangThai(booking.getMaDat(), "Đã nhận bàn");

            if (updatedBooking) {
                JOptionPane.showMessageDialog(this, "Check-in thành công! Hóa đơn #" + maHD + " đã được tạo.");
                loadBookings(); // Refresh List
                refreshAllFloors(); // Refresh Map

                // Update detail if showing same table
                updateDetailPanel(new Entity.Ban(booking.getMaBan(), "Bàn " + booking.getMaBan(), "Có Khách", "",
                        booking.getSoLuongKhach()));
                // Note: The Ban object here is temporary, better to let refreshAllFloors handle
                // it or re-fetch.
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật trạng thái đặt bàn!");
            }
        }
    }
}
