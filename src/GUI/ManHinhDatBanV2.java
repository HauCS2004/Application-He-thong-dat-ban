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
 * ManHinhDatBanV2 - Màn hình Đặt bàn (Giao diện mới)
 * Tính năng: Tìm kiếm, Bộ lọc, Danh sách đặt bàn, Sơ đồ bàn trực quan
 */
public class ManHinhDatBanV2 extends JPanel implements TableCard.TableCardListener {

    private JTextField txtSearch;
    private JDateChooser dateChooser; // Optional date filter
    private JButton btnNotify; // Notification button
    private JComboBox<String> cboTimeFilter;
    private JTable tblBookings;
    private DefaultTableModel modelBookings;
    private JTabbedPane tabFloors;
    // private JPanel pnlFloorTabs;
    // private JPanel pnlTables;
    private String selectedStatus = "Tất cả";
    private JPopupMenu popupTableAction;

    // Giao diện bộ lọc bản đồ
    private JDateChooser dateMap;
    private JComboBox<String> cboMapGio, cboMapPhut;
    private JButton btnCheckMap;

    // Panel chi tiết
    private JPanel pnlDetailContainer;
    private JPanel pnlDetailContent;
    private java.util.ArrayList<Entity.DatBan> dailyBookings;

    private DatBanDAO datBanDAO;
    private BanDAO banDAO;
    private HoaDonDAO hoaDonDAO;

    // --- TRẠNG THÁI BỘ LỌC ---
    private boolean isFilterActive = false;
    private Date filterDate;
    private int filterHour;
    private int filterMinute;

    public ManHinhDatBanV2() {
        this.datBanDAO = new DatBanDAO();
        this.banDAO = new BanDAO();
        this.hoaDonDAO = new HoaDonDAO();

        initBtnNotify(); // Khởi tạo chuông trước để có thể đặt vào TopBar
        initUI();
        initTablePopupMenu();
        loadBookings();
    }

    /**
     * Khởi tạo nút chuông thông báo (được đặt ở TopBar bên ngoài).
     */
    private void initBtnNotify() {
        btnNotify = new JButton(GUI.utils.IconHelper.loadIcon("view/icons/bell.png"));
        btnNotify.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNotify.setBackground(Color.WHITE);
        btnNotify.setFocusPainted(false);
        btnNotify.setBorder(null);
        btnNotify.setPreferredSize(new Dimension(44, 36));
        btnNotify.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNotify.setToolTipText("Thông báo đặt bàn sắp đến");
        btnNotify.addActionListener(e -> showNotificationDialog());
    }

    /** Trả về nút chuông để MainLayout đặt lên TopBar. */
    public JButton getNotifyButton() {
        return btnNotify;
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(249, 250, 251));

        // Create Main Tabbed Pane
        JTabbedPane mainTabs = new JTabbedPane();
        mainTabs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainTabs.setBackground(Color.WHITE);

        // TAB 1: Sơ Đồ Bàn
        JPanel pnlMapTab = new JPanel(new BorderLayout());
        pnlMapTab.setBackground(new Color(249, 250, 251));
        pnlMapTab.add(createVisualFloorSection(), BorderLayout.CENTER);

        mainTabs.addTab("Sơ Đồ Bàn", GUI.utils.IconHelper.loadIcon("view/icons/table.png"), pnlMapTab);

        // TAB 2: Danh Sách Đặt Bàn
        JPanel pnlListTab = new JPanel(new BorderLayout(0, 15));
        pnlListTab.setBackground(new Color(249, 250, 251));
        pnlListTab.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header cho Danh sách
        pnlListTab.add(createListHeaderSection(), BorderLayout.NORTH);
        pnlListTab.add(createBookingTableSection(), BorderLayout.CENTER);

        mainTabs.addTab("Danh Sách Đặt Bàn", GUI.utils.IconHelper.loadIcon("view/icons/list.png"), pnlListTab);

        add(mainTabs, BorderLayout.CENTER);

        // Tự động làm mới khi chuyển giữa hai tab chính
        mainTabs.addChangeListener(e -> {
            if (mainTabs.getSelectedIndex() == 1) {
                // Tab Danh Sách: load lại booking list
                loadBookings();
            } else {
                // Tab Sơ Đồ Bàn: khôi phục trạng thái bàn thực tế
                restoreAllOriginalStatuses();
            }
        });

        // Tự động làm mới khi chuyển tab/menu
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadBookings();

                // Refresh Map
                if (isFilterActive && dateMap != null && dateMap.getDate() != null) {
                    checkMapAvailability(dateMap.getDate(),
                            Integer.parseInt(cboMapGio.getSelectedItem().toString()),
                            Integer.parseInt(cboMapPhut.getSelectedItem().toString()));
                } else {
                    // Just reload table statuses from DB to be safe
                    ArrayList<Ban> currentTables = banDAO.getAllBan();
                    for (int i = 0; i < tabFloors.getTabCount(); i++) {
                        Component c = tabFloors.getComponentAt(i);
                        if (c instanceof TableFloorPanel) {
                            ((TableFloorPanel) c).refreshTableData(currentTables);
                        }
                    }
                }
            }
        });
    }

    private JPanel createListHeaderSection() {
        JPanel pnl = new JPanel(new BorderLayout(10, 15));
        pnl.setOpaque(false);

        // Tiêu đề + Nút tạo
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

        // btnNotify đã được chuyển lên TopBar toàn cục (xem MainLayout)
        JButton btnCreate = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.SUCCESS, "THÊM ĐẶT BÀN MỚI");
        btnCreate.setIcon(
                GUI.utils.IconHelper.resize(GUI.utils.IconHelper.loadIcon("view/icons/add_datban.png"), 20, 20));
        btnCreate.setPreferredSize(new Dimension(190, 40));
        btnCreate.addActionListener(e -> {
            showCreateBookingDialog(null);
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
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên hoặc SĐT...");
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
        pnlFilters.add(new JLabel(" Giờ:"));
        pnlFilters.add(cboTimeFilter);

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

    private JPanel createBookingTableSection() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                new EmptyBorder(10, 10, 10, 10)));

        // Table
        String[] columns = { "MaDat", "", "Tên khách hàng", "Số điện thoại", "SL",
                "Ngày đặt", "Giờ", "Số bàn", "Trạng thái", "MaBan" };
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

        // Luôn hiển thị "Xem chi tiết"
        JMenuItem itemDetail = new JMenuItem(" Xem chi tiết");
        itemDetail.setFont(new Font("Segoe UI", Font.BOLD, 13));
        itemDetail.addActionListener(ev -> showBookingDetailDialog(row));
        popup.add(itemDetail);

        if ("Chờ xác nhận".equals(status) || "Đã xác nhận".equals(status)) {
            popup.addSeparator();

            JMenuItem itemCheckIn = new JMenuItem("Nhận Bàn (Check-in)");
            itemCheckIn.addActionListener(ev -> checkInBookingFromTable(row));
            popup.add(itemCheckIn);

            JMenuItem itemCancel = new JMenuItem("Hủy Đặt");
            itemCancel.addActionListener(ev -> cancelBookingFromTable(row));
            popup.add(itemCancel);
        }

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * Hiển thị dialog chi tiết thông tin đặt bàn.
     */
    private void showBookingDetailDialog(int row) {
        int maDat = Integer.parseInt(modelBookings.getValueAt(row, 0).toString());
        DatBan booking = datBanDAO.getDatBanByID(maDat);
        if (booking == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin đặt bàn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết đặt bàn #" + maDat,
                true);
        dialog.setSize(520, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // ── Header ──────────────────────────────────────────────────
        JPanel pnlHeader = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(59, 130, 246), getWidth(), 0,
                        new Color(37, 99, 235));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        pnlHeader.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel lblHeaderTitle = new JLabel(" Đặt bàn #" + maDat);
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeaderTitle.setForeground(Color.WHITE);

        // Status badge
        String trangThai = booking.getTrangThai();
        Color statusBg, statusFg;
        if (trangThai.startsWith("Đã hủy")) {
            statusBg = new Color(254, 226, 226);
            statusFg = new Color(239, 68, 68);
        } else if ("Đã nhận bàn".equals(trangThai)) {
            statusBg = new Color(220, 252, 231);
            statusFg = new Color(34, 197, 94);
        } else if (trangThai.contains("hoàn") || trangThai.contains("Hoàn")) {
            statusBg = new Color(219, 234, 254);
            statusFg = new Color(59, 130, 246);
        } else {
            statusBg = new Color(254, 243, 199);
            statusFg = new Color(245, 158, 11);
        }
        JLabel lblStatus = new JLabel(" " + trangThai + " ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(statusBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(statusFg);
        lblStatus.setOpaque(false);
        lblStatus.setBorder(new EmptyBorder(4, 10, 4, 10));

        pnlHeader.add(lblHeaderTitle, BorderLayout.WEST);
        pnlHeader.add(lblStatus, BorderLayout.EAST);

        dialog.add(pnlHeader, BorderLayout.NORTH);

        // ── Content ─────────────────────────────────────────────────
        JPanel pnlContent = new JPanel();
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(new EmptyBorder(20, 28, 20, 28));

        // Thông tin khách
        addDetailSection(pnlContent, " Thông tin khách hàng");
        addDetailRow(pnlContent, "Tên khách:", booking.getTenKhach());
        addDetailRow(pnlContent, "Số điện thoại:", booking.getSdt());
        addDetailRow(pnlContent, "Số lượng khách:", booking.getSoLuongKhach() + " người");
        pnlContent.add(Box.createVerticalStrut(16));

        // Thông tin thời gian
        addDetailSection(pnlContent, " Thời gian");
        java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("dd/MM/yyyy");
        java.text.SimpleDateFormat sdfTime = new java.text.SimpleDateFormat("HH:mm");
        addDetailRow(pnlContent, "Ngày đặt:", sdfDate.format(booking.getThoiGianBatDau()));
        addDetailRow(pnlContent, "Giờ bắt đầu:", sdfTime.format(booking.getThoiGianBatDau()));
        addDetailRow(pnlContent, "Giờ kết thúc:", sdfTime.format(booking.getThoiGianKetThuc()));

        // Tính thời lượng
        long durationMinutes = (booking.getThoiGianKetThuc().getTime() - booking.getThoiGianBatDau().getTime()) / 60000;
        addDetailRow(pnlContent, "Thời lượng:",
                durationMinutes / 60 + " giờ " + (durationMinutes % 60 > 0 ? durationMinutes % 60 + " phút" : ""));
        pnlContent.add(Box.createVerticalStrut(16));

        // Thông tin bàn
        addDetailSection(pnlContent, " Thông tin bàn");
        java.util.List<String> dsBan = booking.getDanhSachBan();
        if (dsBan != null && !dsBan.isEmpty()) {
            // Lấy thông tin chi tiết từng bàn
            int totalCapacity = 0;
            StringBuilder banInfo = new StringBuilder();
            for (String maBan : dsBan) {
                ArrayList<Ban> allBan = banDAO.getAllBan();
                for (Ban b : allBan) {
                    if (b.getMaBan().equals(maBan)) {
                        totalCapacity += b.getSoGhe();
                        if (banInfo.length() > 0)
                            banInfo.append(", ");
                        banInfo.append(b.getTenBan()).append(" (").append(b.getSoGhe()).append(" chỗ, ")
                                .append(getZoneNameStatic(b.getMaKV())).append(")");
                        break;
                    }
                }
            }
            addDetailRow(pnlContent, "Số bàn:", dsBan.size() + " bàn");
            addDetailRow(pnlContent, "Chi tiết:", banInfo.toString());
            addDetailRow(pnlContent, "Tổng sức chứa:", totalCapacity + " chỗ");
        } else {
            addDetailRow(pnlContent, "Bàn:", "(Không có thông tin)");
        }
        pnlContent.add(Box.createVerticalStrut(16));

        // Thông tin bổ sung
        addDetailSection(pnlContent, " Thông tin bổ sung");
        addDetailRow(pnlContent, "Tiền cọc:", String.format("%,.0f VNĐ", booking.getTienCoc()));
        addDetailRow(pnlContent, "Ghi chú:",
                booking.getGhiChu() != null && !booking.getGhiChu().isEmpty() ? booking.getGhiChu() : "(Không có)");
        if (booking.getNgayTao() != null) {
            addDetailRow(pnlContent, "Ngày tạo:",
                    new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy").format(booking.getNgayTao()));
        }

        JScrollPane scrollContent = new JScrollPane(pnlContent);
        scrollContent.setBorder(null);
        scrollContent.getVerticalScrollBar().setUnitIncrement(12);
        dialog.add(scrollContent, BorderLayout.CENTER);

        // ── Footer ──────────────────────────────────────────────────
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlFooter.setBackground(new Color(249, 250, 251));
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnClose.setBackground(new Color(107, 114, 128));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setPreferredSize(new Dimension(100, 36));
        btnClose.addActionListener(ev -> dialog.dispose());
        pnlFooter.add(btnClose);

        dialog.add(pnlFooter, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addDetailSection(JPanel parent, String title) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(31, 41, 55));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        parent.add(lbl);
    }

    private void addDetailRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setBorder(new EmptyBorder(2, 12, 2, 0));

        JLabel lblKey = new JLabel(label);
        lblKey.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblKey.setForeground(new Color(107, 114, 128));
        lblKey.setPreferredSize(new Dimension(130, 22));

        JLabel lblVal = new JLabel("<html>" + (value != null ? value : "") + "</html>");
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblVal.setForeground(new Color(31, 41, 55));

        row.add(lblKey, BorderLayout.WEST);
        row.add(lblVal, BorderLayout.CENTER);
        parent.add(row);
    }

    private String getZoneNameStatic(String maKV) {
        if (maKV == null)
            return "";
        switch (maKV) {
            case "KV01":
                return "Tầng G";
            case "KV02":
                return "Tầng 1";
            case "KV03":
                return "VIP";
            case "KV04":
                return "Ngoài trời";
            default:
                return maKV;
        }
    }

    private void openOrderUIFromTable(int row) {
        String maBan = modelBookings.getValueAt(row, 9).toString();
        String tenKhach = modelBookings.getValueAt(row, 2).toString();

        // Find or Create Invoice for this table
        int maHD = hoaDonDAO.getMaHDByBan(maBan);
        if (maHD == -1) {
            // Tạo hóa đơn mới
            // Cần thông tin cơ bản. Vì đã có thông tin đặt bàn, ta có thể sử dụng luôn.
            int soKhach = Integer.parseInt(modelBookings.getValueAt(row, 4).toString());
            String sdt = modelBookings.getValueAt(row, 3).toString();

            String maNV = connectDB.SessionManager.getCurrentUser() != null
                    ? connectDB.SessionManager.getCurrentUser().getMaNV()
                    : null;
            HoaDon hd = new HoaDon(maBan, soKhach, sdt, "Khách đặt: " + tenKhach, maNV);
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
            String maNV = connectDB.SessionManager.getCurrentUser() != null
                    ? connectDB.SessionManager.getCurrentUser().getMaNV()
                    : null;
            HoaDon hd = new HoaDon(maBan, Integer.parseInt(modelBookings.getValueAt(row, 4).toString()), sdt,
                    "Khách đặt: " + tenKhach, maNV);
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

        // --- NEW: Split Filter Design ---
        JPanel pnlFilterContainer = new JPanel(new BorderLayout());
        pnlFilterContainer.setBackground(new Color(240, 240, 240));
        pnlFilterContainer.setBorder(new EmptyBorder(5, 10, 5, 10));

        // ROW 1: Date Overview (Simple)
        JPanel pnlDateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlDateRow.setOpaque(false);

        JLabel lblDate = new JLabel("Ngày xem:");
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 12));

        dateMap = new JDateChooser(new Date());
        dateMap.setDateFormatString("dd/MM/yyyy");
        dateMap.setPreferredSize(new Dimension(130, 30));

        JButton btnViewDate = new JButton("Xem Lịch Ngày");
        btnViewDate.setBackground(new Color(34, 197, 94));
        btnViewDate.setForeground(Color.WHITE);
        btnViewDate.setFocusPainted(false);

        JCheckBox chkAdvanced = new JCheckBox("Tìm  bàn trống theo thời gian");
        chkAdvanced.setOpaque(false);
        chkAdvanced.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        pnlDateRow.add(lblDate);
        pnlDateRow.add(dateMap);
        pnlDateRow.add(btnViewDate);
        pnlDateRow.add(Box.createHorizontalStrut(5));

        // Refresh Button (F5)
        JButton btnRefresh = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.NEUTRAL, "Reset bộ lọc");
        btnRefresh.setToolTipText("Quay về thời gian thực (F5)");
        pnlDateRow.add(btnRefresh);

        pnlDateRow.add(Box.createHorizontalStrut(20));
        pnlDateRow.add(chkAdvanced);

        // ROW 2: Time Filter (Hidden by default)
        JPanel pnlTimeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlTimeRow.setOpaque(false);
        pnlTimeRow.setVisible(false); // Hidden initially

        JLabel lblTime = new JLabel("Kiểm tra trống lúc:");
        lblTime.setFont(new Font("Segoe UI", Font.BOLD, 12));

        cboMapGio = new JComboBox<>();
        for (int h = 7; h <= 22; h++)
            cboMapGio.addItem(String.format("%02d", h));
        cboMapGio.setSelectedItem("18");

        cboMapPhut = new JComboBox<>();
        cboMapPhut.addItem("00");
        cboMapPhut.addItem("30");

        btnCheckMap = new JButton("Kiểm tra");
        btnCheckMap.setBackground(new Color(59, 130, 246));
        btnCheckMap.setForeground(Color.WHITE);
        btnCheckMap.setFocusPainted(false);

        pnlTimeRow.add(lblTime);
        pnlTimeRow.add(cboMapGio);
        pnlTimeRow.add(new JLabel(":"));
        pnlTimeRow.add(cboMapPhut);
        pnlTimeRow.add(btnCheckMap);

        // Assemble Filter
        pnlFilterContainer.add(pnlDateRow, BorderLayout.NORTH);
        pnlFilterContainer.add(pnlTimeRow, BorderLayout.CENTER);

        pnl.add(pnlFilterContainer, BorderLayout.NORTH);

        // Tabs & Split Pane
        tabFloors = new JTabbedPane();
        tabFloors.setFont(new Font("Segoe UI", Font.BOLD, 13));

        addFloorTab("Tất cả", "ALL");
        addFloorTab("Tầng G", "KV01");
        addFloorTab("Tầng 1", "KV02");
        addFloorTab("VIP Room", "KV03");
        addFloorTab("Ngoài trời", "KV04");

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabFloors, createDetailPanel());
        splitPane.setResizeWeight(0.85);
        splitPane.setDividerSize(5);
        pnl.add(splitPane, BorderLayout.CENTER);

        // --- Logic ---

        // Toggle Advanced Mode
        chkAdvanced.addActionListener(e -> {
            boolean advanced = chkAdvanced.isSelected();
            pnlTimeRow.setVisible(advanced);
            btnViewDate.setEnabled(!advanced); // Disable Date Button if Advanced is on? Or keep both?
            // User request: "trước hoặc sau đó 30p"

            // If Advanced OFF -> "Date Mode" (Overview)
            // If Advanced ON -> "Time Mode" (Strict)
            if (!advanced) {
                // Revert to Date Overview immediately?
                checkMapOverview(dateMap.getDate());
            }
        });

        // "Xem Lịch Ngày" Action
        btnViewDate.addActionListener(e -> {
            checkMapOverview(dateMap.getDate());
        });

        // "Kiểm Tra" Action (Time)
        btnCheckMap.addActionListener(e -> {
            checkMapAvailability(dateMap.getDate(),
                    Integer.parseInt(cboMapGio.getSelectedItem().toString()),
                    Integer.parseInt(cboMapPhut.getSelectedItem().toString()));
        });

        // "Refresh" Action
        ActionListener refreshAction = e -> {
            // Reset Inputs
            dateMap.setDate(new Date());
            chkAdvanced.setSelected(false);
            pnlTimeRow.setVisible(false);
            btnViewDate.setEnabled(true);

            // Reset Logic
            isFilterActive = false;
            filterDate = null;
            filterHour = -1;

            // Reload Real-time
            loadBookings(); // Loads recent/today

            // Restore Real-time Statuses
            restoreAllOriginalStatuses();
        };

        btnRefresh.addActionListener(refreshAction);

        // Add F5 Global Shortcut
        InputMap inputMap = pnl.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = pnl.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "refresh_map");
        actionMap.put("refresh_map", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshAction.actionPerformed(e);
            }
        });

        return pnl;
    }

    /**
     * Mode 1: Date Overview
     * Shows ALL bookings for the day. Tables with ANY valid booking are marked
     * Yellow (Đã Đặt).
     */
    private void checkMapOverview(Date date) {
        if (date == null)
            return;

        this.isFilterActive = true;
        this.filterDate = date;
        // Reset Time filters to avoid confusion
        this.filterHour = -1;

        // 1. Load Bookings for Date (Fix: Full Day Range)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date start = cal.getTime();

        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        Date end = cal.getTime();

        this.dailyBookings = datBanDAO.getDanhSachDatBan(start, end);

        // 2. Refresh Panels (Clear old statuses)
        restoreAllOriginalStatuses();

        // Check if viewing Today
        java.util.Calendar calNow = java.util.Calendar.getInstance();
        java.util.Calendar calView = java.util.Calendar.getInstance();
        calView.setTime(date);
        boolean isToday = (calNow.get(java.util.Calendar.YEAR) == calView.get(java.util.Calendar.YEAR) &&
                calNow.get(java.util.Calendar.DAY_OF_YEAR) == calView.get(java.util.Calendar.DAY_OF_YEAR));

        // 3. Map Tables
        java.util.Map<String, Entity.DatBan> bookingMap = new java.util.HashMap<>();

        for (DatBan db : this.dailyBookings) {
            String status = db.getTrangThai().toLowerCase();
            if (status.startsWith("đã hủy") || status.contains("hoàn thành") || status.contains("hoàn tất")
                    || status.contains("thanh toán"))
                continue;

            // In Overview Mode, we map the booking to the table if it exists.
            bookingMap.put(db.getMaBan(), db);
        }

        // 4. Update UI
        for (int i = 0; i < tabFloors.getTabCount(); i++) {
            Component c = tabFloors.getComponentAt(i);
            if (c instanceof TableFloorPanel) {
                ((TableFloorPanel) c).updateOverviewMode(bookingMap, isToday);
            }
        }
    }

    private void restoreAllOriginalStatuses() {
        ArrayList<Ban> currentTables = banDAO.getAllBan();
        for (int i = 0; i < tabFloors.getTabCount(); i++) {
            Component c = tabFloors.getComponentAt(i);
            if (c instanceof TableFloorPanel) {
                ((TableFloorPanel) c).refreshTableData(currentTables);
            }
        }
    }

    /**
     * Mode 2: Strict Time Availability
     */
    private void checkMapAvailability(Date date, int hour, int minute) {
        if (date == null)
            return;

        this.isFilterActive = true;
        this.filterDate = date;
        this.filterHour = hour;
        this.filterMinute = minute;

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
        cal.set(java.util.Calendar.MINUTE, minute);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date targetTime = cal.getTime();

        // Fix: Query Full Day for Filtering
        java.util.Calendar calRange = java.util.Calendar.getInstance();
        calRange.setTime(date);
        calRange.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calRange.set(java.util.Calendar.MINUTE, 0);
        calRange.set(java.util.Calendar.SECOND, 0);
        Date start = calRange.getTime();

        calRange.set(java.util.Calendar.HOUR_OF_DAY, 23);
        calRange.set(java.util.Calendar.MINUTE, 59);
        calRange.set(java.util.Calendar.SECOND, 59);
        Date end = calRange.getTime();

        this.dailyBookings = datBanDAO.getDanhSachDatBan(start, end);
        restoreAllOriginalStatuses();

        java.util.Map<String, Entity.DatBan> bookingMap = new java.util.HashMap<>();

        for (DatBan db : this.dailyBookings) {
            String status = db.getTrangThai() != null ? db.getTrangThai().trim() : "";
            if (status.toLowerCase().startsWith("đã hủy")
                    || status.toLowerCase().contains("hoàn thành")
                    || status.toLowerCase().contains("hoàn tất")
                    || status.toLowerCase().contains("thanh toán"))
                continue;

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
            }
        }

        // 4. Update UI
        for (int i = 0; i < tabFloors.getTabCount(); i++) {
            Component c = tabFloors.getComponentAt(i);
            if (c instanceof TableFloorPanel) {
                ((TableFloorPanel) c).updateTableStatuses(bookingMap);
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

        // 2. Load Data for List Tab
        modelBookings.setRowCount(0);

        ArrayList<DatBan> bookings;

        // Logic for List Tab Filter
        if (chkEnableDateFilter != null && chkEnableDateFilter.isSelected()) {
            Date selectedDate = dateChooser.getDate();
            bookings = datBanDAO.getDanhSachDatBan(selectedDate, selectedDate);
        } else {
            bookings = datBanDAO.getDanhSachDatBanGanDay(100);
        }

        // 3. Update Daily Bookings for Map Tab (Corrected Logic)
        // 3. Update Daily Bookings for Map Tab (Corrected Logic)
        Date targetDateForMap = (isFilterActive && filterDate != null) ? filterDate : new Date();

        java.util.Calendar calMap = java.util.Calendar.getInstance();
        calMap.setTime(targetDateForMap);
        calMap.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calMap.set(java.util.Calendar.MINUTE, 0);
        calMap.set(java.util.Calendar.SECOND, 0);
        calMap.set(java.util.Calendar.MILLISECOND, 0);
        Date startMap = calMap.getTime();

        calMap.set(java.util.Calendar.HOUR_OF_DAY, 23);
        calMap.set(java.util.Calendar.MINUTE, 59);
        calMap.set(java.util.Calendar.SECOND, 59);
        Date endMap = calMap.getTime();

        this.dailyBookings = datBanDAO.getDanhSachDatBan(startMap, endMap);

        if (chkEnableDateFilter != null && chkEnableDateFilter.isSelected()) {
            // If List filter is active, sync dailyBookings to it?
            // Maybe better to keep them separate, but for simplicity let's stick to Map
            // Priority
            // If user is focused on Map, isFilterActive should be true.
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

    private void showCreateBookingDialog(String preSelectedTable) {
        Window win = SwingUtilities.getWindowAncestor(this);
        BookingFormDialog dialog = new BookingFormDialog((Frame) win, preSelectedTable);
        // Pre-fill nếu đang ở chế độ filter
        if (isFilterActive && filterDate != null && filterHour >= 0) {
            dialog.setPreFilledTime(filterDate, filterHour, filterMinute);
        }
        dialog.setOnSuccessCallback(() -> {
            loadBookings();
            if (isFilterActive) {
                checkMapAvailability(filterDate, filterHour, filterMinute);
            } else {
                refreshAllFloors();
            }
        });
        dialog.setVisible(true);
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
        String status = table.getTrangThai();

        if ("Trống".equals(status)) {
            // Mở wizard đặt bàn hiện đại — pre-select bàn này
            showCreateBookingDialog(table.getMaBan());
        } else if ("Đã Đặt".equals(status)) {
            // Hiển thị thông tin booking hiện tại
            DatBan db = DatBanDAO.getDatBanGanNhat(table.getMaBan());
            if (db != null) {
                String info = String.format(
                        "<html><div style='padding:8px;'>" +
                                "<h3>Thông tin đặt bàn — %s</h3>" +
                                "<p><b>Khách:</b> %s</p>" +
                                "<p><b>SĐT:</b> %s</p>" +
                                "<p><b>Giờ:</b> %s — %s</p>" +
                                "<p><b>Số khách:</b> %d</p>" +
                                "<p><b>Trạng thái:</b> %s</p>" +
                                "</div></html>",
                        table.getTenBan(), db.getTenKhach(), db.getSdt(),
                        formatDateTime(db.getThoiGianBatDau()),
                        formatDateTime(db.getThoiGianKetThuc()),
                        db.getSoLuongKhach(), db.getTrangThai());
                JOptionPane.showMessageDialog(this, info, "Thông tin đặt bàn", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if ("Có Khách".equals(status) || "Có khách".equals(status)) {
            JOptionPane.showMessageDialog(this, "Bàn " + table.getTenBan() + " đang có khách ngồi.");
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
            // Warning State
            btnNotify.setIcon(GUI.utils.IconHelper.loadIcon("view/icons/bell_warning.png"));
            btnNotify.setText(String.valueOf(total)); // Show count
            btnNotify.setForeground(new Color(220, 38, 38)); // Red Text
            btnNotify.setBackground(new Color(254, 226, 226)); // Light Red BG
        } else {
            // Normal State
            btnNotify.setIcon(GUI.utils.IconHelper.loadIcon("view/icons/bell.png"));
            btnNotify.setText(""); // Hide count
            btnNotify.setForeground(new Color(234, 179, 8)); // Restore default (though not used for icon)
            btnNotify.setBackground(Color.WHITE);
        }
    }

    private void showNotificationDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thông báo đặt bàn", true);
        dialog.setSize(650, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // --- 1. Tab Sắp đến ---
        JPanel pnlUpcoming = new JPanel(new BorderLayout());
        ArrayList<Entity.DatBan> upcoming = datBanDAO.getUpcomingBookings(30);
        if (upcoming.isEmpty()) {
            JLabel lbl = new JLabel("Không có đặt bàn nào sắp đến giờ (~30p)", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            pnlUpcoming.add(lbl);
        } else {
            String[] cols = { "Mã Đặt", "Bàn", "Khách", "SĐT", "Giờ đến" };
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            for (Entity.DatBan db : upcoming) {
                model.addRow(new Object[] {
                        db.getMaDat(), db.getMaBan(), db.getTenKhach(), db.getSdt(),
                        new java.text.SimpleDateFormat("HH:mm dd/MM").format(db.getThoiGianBatDau())
                });
            }
            JTable table = new JTable(model);
            table.setRowHeight(30);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            pnlUpcoming.add(new JScrollPane(table));
        }
        tabbedPane.addTab("Sắp đến (" + upcoming.size() + ")", pnlUpcoming);

        // --- 2. Tab Quá Giờ --- (READ-ONLY từ getDatBanQuaGio(), không auto-cancel)
        JPanel pnlOverdue = new JPanel(new BorderLayout(0, 5));
        pnlOverdue.setBorder(new EmptyBorder(8, 8, 8, 8));
        // Lấy danh sách quá giờ 1-30p, không hủy gì cả
        ArrayList<Entity.DatBan> overdue = datBanDAO.getDatBanQuaGio();

        String[] colsOv = { "Mã Đặt", "Bàn", "Khách", "SĐT", "Giờ hẹn", "Trễ (phút)" };
        DefaultTableModel modelOv = new DefaultTableModel(colsOv, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        if (overdue.isEmpty()) {
            JLabel lbl = new JLabel("Không có đặt bàn nào quá giờ trong 30 phút vừa rồi", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            pnlOverdue.add(lbl, BorderLayout.CENTER);
        } else {
            long now = System.currentTimeMillis();
            for (Entity.DatBan db : overdue) {
                long trezMillis = now - db.getThoiGianBatDau().getTime();
                int trezPhut = (int) (trezMillis / 60_000);
                modelOv.addRow(new Object[] {
                        db.getMaDat(), db.getMaBan(), db.getTenKhach(), db.getSdt(),
                        new java.text.SimpleDateFormat("HH:mm dd/MM").format(db.getThoiGianBatDau()),
                        trezPhut
                });
            }

            JTable tblOv = new JTable(modelOv);
            tblOv.setRowHeight(30);
            tblOv.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tblOv.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            // Tô màu cột "Trễ" theo mức độ
            tblOv.getColumnModel().getColumn(5).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                public java.awt.Component getTableCellRendererComponent(JTable t, Object v,
                        boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    if (v instanceof Integer) {
                        int min = (Integer) v;
                        setBackground(min <= 15 ? new Color(254, 249, 195) // vàng nhạt
                                : min <= 25 ? new Color(254, 215, 170) // cam
                                        : new Color(254, 202, 202)); // đỏ
                        setText(min + " phút");
                        setHorizontalAlignment(SwingConstants.CENTER);
                    }
                    return this;
                }
            });

            pnlOverdue.add(new JScrollPane(tblOv), BorderLayout.CENTER);

            // --- Nút hành động ---
            JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            pnlBtns.setOpaque(false);

            // Nút hủy từng booking được chọn
            JButton btnHuyChon = new JButton("❌ Hủy booking được chọn");
            btnHuyChon.setBackground(new Color(239, 68, 68));
            btnHuyChon.setForeground(Color.WHITE);
            btnHuyChon.setFocusPainted(false);
            btnHuyChon.addActionListener(ev -> {
                int row = tblOv.getSelectedRow();
                if (row < 0) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng chọn một đư bàn trong danh sách!",
                            "Chưa chọn", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int maDat = (Integer) modelOv.getValueAt(row, 0);
                String tenKhach = (String) modelOv.getValueAt(row, 2);
                String sdt = (String) modelOv.getValueAt(row, 3);

                // Hỏi lý do hủy
                String lyDo = JOptionPane.showInputDialog(dialog,
                        "Lý do hủy cho khách " + tenKhach + " (" + sdt + "):",
                        "Hủy đặt bàn theo yêu cầu");
                if (lyDo == null)
                    return; // Bấm Cancel
                if (lyDo.isBlank())
                    lyDo = "Hủy theo yêu cầu khách (NV xử lý)";

                boolean ok = datBanDAO.huyDatBanManual(maDat, lyDo);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "✔ Đã hủy và ghi lý do: " + lyDo);
                    modelOv.removeRow(row); // Xóa khỏi UI luôn
                    loadBookings();
                    refreshAllFloors();
                    checkAlerts(); // Cập nhật badge
                } else {
                    JOptionPane.showMessageDialog(dialog, "Lỗi khi hủy! Vui lòng thử lại.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });

            // Nút hủy tất cả (chỉ hủy booking > 30p)
            JButton btnHuyTatCa = new JButton("🗑 Hủy tất cả quá 30 phút");
            btnHuyTatCa.setBackground(new Color(107, 114, 128));
            btnHuyTatCa.setForeground(Color.WHITE);
            btnHuyTatCa.setFocusPainted(false);
            btnHuyTatCa.setToolTipText("Chỉ hủy các booking đã quá 30 phút (không phải danh sách này)");
            btnHuyTatCa.addActionListener(ev -> {
                int confirm = JOptionPane.showConfirmDialog(dialog,
                        "Hủy tất cả đặt bàn đã QUÁ 30 PHÚT?\n(Danh sách này đang hiện booking trễ 1-30p — những cái >30p sẽ bị hủy)",
                        "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    int count = datBanDAO.autoCancelOverdueBookings();
                    JOptionPane.showMessageDialog(dialog, "Đã tự động hủy " + count + " đơn quá 30 phút.");
                    dialog.dispose();
                    loadBookings();
                    refreshAllFloors();
                    checkAlerts();
                }
            });

            pnlBtns.add(btnHuyChon);
            pnlBtns.add(btnHuyTatCa);
            pnlOverdue.add(pnlBtns, BorderLayout.SOUTH);
        }

        tabbedPane.addTab("Quá giờ (" + overdue.size() + ")", pnlOverdue);

        dialog.add(tabbedPane, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // --- NEW: Split View Logic ---

    // Panel chứa nút đặt bàn nổi bật ở phần dưới
    private JPanel pnlBookingAction;
    private Ban currentDetailTable; // Lưu bàn đang xem chi tiết

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

        // Content (scrollable)
        pnlDetailContent = new JPanel();
        pnlDetailContent.setLayout(new BoxLayout(pnlDetailContent, BoxLayout.Y_AXIS));
        pnlDetailContent.setBackground(Color.WHITE);
        pnlDetailContent.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane scrollDetail = new JScrollPane(pnlDetailContent);
        scrollDetail.setBorder(null);
        scrollDetail.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollDetail.getVerticalScrollBar().setUnitIncrement(10);
        pnlDetailContainer.add(scrollDetail, BorderLayout.CENTER);

        // ── PHẦN NỔI BẬT: Nút Đặt Bàn (Fixed Bottom Section) ──
        pnlBookingAction = createBookingActionPanel();
        pnlDetailContainer.add(pnlBookingAction, BorderLayout.SOUTH);

        // Initial Empty State
        showEmptyDetailState();

        return pnlDetailContainer;
    }

    /**
     * Tạo panel nút đặt bàn nổi bật ở phần dưới, luôn hiển thị.
     */
    private JPanel createBookingActionPanel() {
        JPanel pnl = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient nền từ xanh nhạt sang xanh đậm
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(236, 253, 245),
                        0, getHeight(), new Color(209, 250, 229));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Đường kẻ trên (shadow effect)
                g2.setColor(new Color(167, 243, 208));
                g2.drawLine(0, 0, getWidth(), 0);
                g2.dispose();
            }
        };
        pnl.setBorder(new EmptyBorder(16, 15, 16, 15));
        pnl.setOpaque(false);

        // Nút đặt bàn chính — to, nổi bật
        JButton btnBook = new JButton("  ĐẶT BÀN MỚI") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient nút từ xanh lá đậm sang xanh lá sáng
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(22, 163, 74),
                        0, getHeight(), new Color(34, 197, 94));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnBook.setIcon(
                GUI.utils.IconHelper.resize(GUI.utils.IconHelper.loadIcon("view/icons/add_datban.png"), 22, 22));
        btnBook.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnBook.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBook.setForeground(Color.WHITE);
        btnBook.setFocusPainted(false);
        btnBook.setContentAreaFilled(false);
        btnBook.setBorderPainted(false);
        btnBook.setOpaque(false);
        btnBook.setPreferredSize(new Dimension(0, 48));

        // Hover effect
        btnBook.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnBook.setFont(new Font("Segoe UI", Font.BOLD, 16));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnBook.setFont(new Font("Segoe UI", Font.BOLD, 15));
            }
        });

        btnBook.addActionListener(e -> {
            String maBan = (currentDetailTable != null) ? currentDetailTable.getMaBan() : null;
            showCreateBookingDialog(maBan);
        });

        pnl.add(btnBook, BorderLayout.CENTER);

        // Label gợi ý
        JLabel lblHint = new JLabel("Nhấn để tạo đặt bàn mới", SwingConstants.CENTER);
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(new Color(22, 163, 74));
        lblHint.setBorder(new EmptyBorder(6, 0, 0, 0));
        pnl.add(lblHint, BorderLayout.SOUTH);

        return pnl;
    }

    private void showEmptyDetailState() {
        if (pnlDetailContent == null)
            return;
        pnlDetailContent.removeAll();
        pnlDetailContent.add(Box.createVerticalGlue());
        JLabel lblIcon = new JLabel();
        lblIcon.setIcon(GUI.utils.IconHelper.loadIcon("view/icons/table.png"));
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
        currentDetailTable = table; // Lưu bàn đang xem

        // 1. Table Header
        String baseName = table.getTenBan();
        String mergeInfo = "";

        // Status Label Logic
        String displayStatus = table.getTrangThai();

        // Kiểm tra thông tin gộp bàn nếu đang Có Khách
        if ("Có Khách".equals(table.getTrangThai()) || "Có khách".equals(table.getTrangThai())) {
            int maHD = hoaDonDAO.getMaHDByBan(table.getMaBan());
            if (maHD != -1) {
                Entity.HoaDon hd = hoaDonDAO.getThongTinHoaDon(maHD);
                if (hd != null && hd.getGhiChu() != null && hd.getGhiChu().contains("[Ghép")) {
                    java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\[Ghép từ bàn (.*?)\\]");
                    java.util.regex.Matcher m = p.matcher(hd.getGhiChu());
                    java.util.ArrayList<String> mergedTables = new java.util.ArrayList<>();
                    while (m.find()) {
                        mergedTables.add(m.group(1));
                    }
                    if (!mergedTables.isEmpty()) {
                        mergeInfo = " (Đang gộp với bàn " + String.join(", ", mergedTables) + ")";
                    }
                }
            }
        }

        JLabel lblName = new JLabel("<html>" + baseName + mergeInfo + "</html>");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDetailContent.add(lblName);

        // Hiển thị sức chứa
        JLabel lblCapacity = new JLabel("💺 Sức chứa: " + table.getSoGhe() + " khách");
        lblCapacity.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCapacity.setForeground(new Color(107, 114, 128));
        lblCapacity.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDetailContent.add(lblCapacity);
        pnlDetailContent.add(Box.createVerticalStrut(5));

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
        else if ("Có Khách".equals(displayStatus) || "Có khách".equals(displayStatus))
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
                // Filter: Same Table AND Active Status (Not Canceled, Not Completed)
                String st = db.getTrangThai().toLowerCase();
                if (db.getMaBan().equals(table.getMaBan())
                        && !st.startsWith("đã hủy")
                        && !st.contains("hoàn tất")
                        && !st.contains("hoàn thành")) {

                    hasBooking = true;
                    // Format: [HH:mm] - Name (Status)
                    String time = new java.text.SimpleDateFormat("HH:mm").format(db.getThoiGianBatDau());
                    String info = String.format("<html><b>%s</b> - %s<br/><i>%s</i></html>", time, db.getTenKhach(),
                            db.getTrangThai());

                    JPanel pItem = new JPanel(new BorderLayout());
                    pItem.setBackground(Color.WHITE);
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

        // Nút đặt bàn đã được chuyển xuống phần bottom cố định (pnlBookingAction)
        // Không cần thêm nút ở đây nữa

        pnlDetailContent.add(Box.createVerticalGlue());
        pnlDetailContent.revalidate();
        pnlDetailContent.repaint();
    }
}
