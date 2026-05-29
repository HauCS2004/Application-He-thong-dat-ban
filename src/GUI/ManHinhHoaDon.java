package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import com.toedter.calendar.JDateChooser;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import Entity.Ban;
import Entity.HoaDon;
import Entity.KhachHang;
import Entity.KhuyenMai;
import Entity.NhanVien;
import DAO.KhuyenMaiDAO;
import DAO.KhachHangDAO;
import DAO.NhanVienDAO;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import DAO.BanDAO;
import DAO.HoaDonDAO;


public class ManHinhHoaDon extends JPanel {

    private BanDAO banDAO = new BanDAO();
    private HoaDonDAO hdDAO = new HoaDonDAO();
    private KhuyenMaiDAO kmDAO = new KhuyenMaiDAO();
    private KhachHangDAO khDAO = new KhachHangDAO();
    private DAO.NhanVienDAO nvDAO = new DAO.NhanVienDAO();

    // TABS
    private JTabbedPane tabs;

    // --- TAB 1 COMPONENTS (PAYMENT) ---
    private JPanel pnlTableList;
    private JScrollPane scrollTables;
    private JTextField txtSearchActiveTable;
    private JPopupMenu popupTableSuggestions;
    private JList<TableSearchEntry> listTableSuggestions;
    private DefaultListModel<TableSearchEntry> modelTableSuggestions;
    private final ArrayList<TableSearchEntry> activeTableEntries = new ArrayList<>();
    private boolean suppressActiveTableSearchEvents = false;

    private JLabel lblTitleBan;
    private JLabel lblNhanVien;
    private JLabel lblMaHD;
    private JLabel lblNgayTao;
    private JLabel lblKhachHang;

    private JTable tblChiTiet;
    private DefaultTableModel modelChiTiet;

    private JLabel lblRankName; // New: Instead of Combo
    private JLabel lblTongTienHang;
    private JLabel lblTienGiam;
    private JTextField txtVoucher;
    private JLabel lblThanhTien;

    private JButton btnThanhToan;
    private JButton btnInHoaDon;

    // Thuế & Phí (đọc từ SystemConfig — không chỉnh sửa trực tiếp tại đây)
    private JLabel lblVATRate;      // Hiển thị % VAT
    private JLabel lblServiceRate;  // Hiển thị % phí PV
    private JLabel lblVATAmount;
    private JLabel lblPhiAmount;

    // Data Tab 1
    private String selectedMaBan = null;
    private int currentMaHD = -1;
    private double currentTongTienHang = 0;
    private Entity.KhachHang currentKhachHang = null; // New: Store current customer
    private Entity.KhuyenMai currentVoucher = null; // New: Store applied voucher

    // --- TAB 2 COMPONENTS (HISTORY) ---
    private JTable tblHistory;
    private DefaultTableModel modelHistory;
    private JDateChooser dateFrom;
    private JDateChooser dateTo;
    private JTextField txtSearchHistory;

    // API: Nhảy thẳng vào Tab "Lập Hóa Đơn" và tự động chọn bàn theo maBan
    public void selectActiveTable(String maBan) {
        // 1. Chuyển sang Tab "Lập Hóa Đơn" (index 1)
        tabs.setSelectedIndex(1);

        // 2. Load danh sách bàn trước
        loadTableList();

        if (txtSearchActiveTable != null) {
            setActiveTableSearchText(maBan);
            applyActiveTableSearch();
        }

        // 3. Tìm TableCard có maBan khớp rồi kích hoạt
        for (TableSearchEntry entry : activeTableEntries) {
            if (entry.table != null && entry.table.getMaBan().equalsIgnoreCase(maBan)) {
                highlightTableCard(entry.table.getMaBan());
                selectTable(entry.table.getMaBan());
                break;
            }
        }
    }

    public ManHinhHoaDon() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabs.addTab("Lịch Sử Hóa Đơn", GUI.utils.IconHelper.loadIcon("view/icons/order-history.png"),
                createHistoryTab());
        tabs.addTab("Lập Hóa Đơn", GUI.utils.IconHelper.loadIcon("view/icons/payment.png"), createPaymentTab());

        add(tabs, BorderLayout.CENTER);

        // Events
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadTableList();
                loadHistoryData();
            }
        });

        // Tab Change Event
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1) { // 1 là Lập Hóa Đơn
                loadTableList();
            } else {
                loadHistoryData();
            }
        });
    }

    // =========================================================================
    // TAB 1: PAYMENT (LẬP HÓA ĐƠN)
    // =========================================================================
    private JPanel createPaymentTab() {
        JPanel pnlRoot = new JPanel(new BorderLayout());

        // Split Pane: Left (Tables 30%) - Right (Invoice 70%)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createRightPanel());
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0.3);
        pnlRoot.add(splitPane, BorderLayout.CENTER);

        return pnlRoot;
    }

    private JPanel createLeftPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(243, 244, 246));
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pnlHeaderWrap = new JPanel(new BorderLayout(0, 10));
        pnlHeaderWrap.setOpaque(false);

        JLabel lblHeader = new JLabel("BÀN ĐANG PHỤC VỤ (" + connectDB.SessionManager.getDisplayName() + ")");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHeader.setForeground(new Color(31, 41, 55));
        pnlHeaderWrap.add(lblHeader, BorderLayout.NORTH);

        JPanel pnlSearch = new JPanel(new BorderLayout(8, 0));
        pnlSearch.setOpaque(false);

        txtSearchActiveTable = new JTextField();
        txtSearchActiveTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearchActiveTable.putClientProperty("JTextField.placeholderText",
                "Tìm bàn, mã HĐ, khách, SĐT, khu, bàn ghép...");
        txtSearchActiveTable.setPreferredSize(new Dimension(0, 38));

        JButton btnClearSearch = new JButton("×");
        btnClearSearch.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnClearSearch.setFocusable(false);
        btnClearSearch.setMargin(new Insets(0, 0, 0, 0));
        btnClearSearch.setPreferredSize(new Dimension(40, 38));
        btnClearSearch.addActionListener(e -> {
            setActiveTableSearchText("");
            applyActiveTableSearch();
            txtSearchActiveTable.requestFocusInWindow();
        });

        pnlSearch.add(txtSearchActiveTable, BorderLayout.CENTER);
        pnlSearch.add(btnClearSearch, BorderLayout.EAST);
        pnlHeaderWrap.add(pnlSearch, BorderLayout.SOUTH);
        pnl.add(pnlHeaderWrap, BorderLayout.NORTH);

        // Use FlowLayout for visual cards
        pnlTableList = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        pnlTableList.setBackground(new Color(243, 244, 246));
        pnlTableList.setPreferredSize(new Dimension(320, 1000)); // Initial height

        scrollTables = new JScrollPane(pnlTableList);
        scrollTables.setBorder(null);
        scrollTables.getVerticalScrollBar().setUnitIncrement(16);

        // Auto-resize logic
        scrollTables.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateTableListPreferredSize();
                pnlTableList.revalidate();
            }
        });

        initActiveTableSuggestionPopup();
        setupActiveTableSearch();

        pnl.add(scrollTables, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel createRightPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(20, 30, 20, 30));

        // 1. Header Info (Receipt Style)
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(229, 231, 235)));

        // Restaurant Name (đọc từ SystemConfig)
        JLabel lblResName = new JLabel(GUI.utils.SystemConfig.getResName());
        lblResName.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblResName.setForeground(new Color(185, 28, 28));
        lblResName.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblResName);

        // Address
        JLabel lblAddress = new JLabel("ĐC: " + GUI.utils.SystemConfig.getResAddress());
        lblAddress.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAddress.setForeground(Color.GRAY);
        lblAddress.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblAddress);

        pnlHeader.add(Box.createVerticalStrut(10));

        // Title
        JLabel lblTitle = new JLabel("HÓA ĐƠN THANH TOÁN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.BLACK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblTitle);

        pnlHeader.add(Box.createVerticalStrut(20));

        // Info Grid
        JPanel pnlInfo = new JPanel(new GridLayout(3, 2, 10, 5));
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setBorder(new EmptyBorder(0, 20, 10, 20));

        lblMaHD = new JLabel("Mã HĐ: ---");
        lblMaHD.setFont(new Font("Segoe UI", Font.BOLD, 14));

        lblNgayTao = new JLabel("Ngày: ---");
        lblNgayTao.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        lblTitleBan = new JLabel("Bàn: ---");
        lblTitleBan.setFont(new Font("Segoe UI", Font.BOLD, 14));

        lblNhanVien = new JLabel("Thu ngân: ---");
        lblNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        lblKhachHang = new JLabel("Khách hàng: ---");
        lblKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        pnlInfo.add(lblMaHD);
        pnlInfo.add(lblNgayTao);
        pnlInfo.add(lblTitleBan);
        pnlInfo.add(lblNhanVien);
        pnlInfo.add(lblKhachHang);

        pnlHeader.add(pnlInfo);

        pnl.add(pnlHeader, BorderLayout.NORTH);

        // 2. Items Table
        String[] headers = { "Tên Món", "Số Lượng", "Đơn Giá", "Thành Tiền" };
        modelChiTiet = new DefaultTableModel(headers, 0);
        tblChiTiet = new JTable(modelChiTiet);
        tblChiTiet.setRowHeight(30);
        tblChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblChiTiet.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        pnl.add(new JScrollPane(tblChiTiet), BorderLayout.CENTER);

        // 3. Payment Section (Footer)
        JPanel pnlFooter = new JPanel(new BorderLayout(10, 10));
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Calculation Area
        JPanel pnlCalc = new JPanel(new GridLayout(6, 2, 10, 8)); // [GĐ4] 6 Rows
        pnlCalc.setBackground(Color.WHITE);

        // ROW 1: Tổng tiền hàng
        pnlCalc.add(new JLabel("Tổng tiền hàng:", SwingConstants.RIGHT));
        lblTongTienHang = new JLabel("0 VNĐ", SwingConstants.RIGHT);
        lblTongTienHang.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlCalc.add(lblTongTienHang);

        // ROW 2: VAT (đọc từ config, không cho chỉnh sửa)
        double vatPct = GUI.utils.SystemConfig.getVAT();
        lblVATRate = new JLabel(String.format("+ VAT: %.1f%%", vatPct), SwingConstants.RIGHT);
        lblVATRate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblVATRate.setForeground(new Color(75, 85, 99));
        pnlCalc.add(lblVATRate);

        lblVATAmount = new JLabel("+ 0 VNĐ", SwingConstants.RIGHT);
        lblVATAmount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblVATAmount.setForeground(new Color(75, 85, 99));
        pnlCalc.add(lblVATAmount);

        // ROW 3: Phí phục vụ (đọc từ config)
        double svcPct = GUI.utils.SystemConfig.getServiceFee();
        lblServiceRate = new JLabel(String.format("+ Phí PV: %.1f%%", svcPct), SwingConstants.RIGHT);
        lblServiceRate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblServiceRate.setForeground(new Color(75, 85, 99));
        pnlCalc.add(lblServiceRate);

        lblPhiAmount = new JLabel("+ 0 VNĐ", SwingConstants.RIGHT);
        lblPhiAmount.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPhiAmount.setForeground(new Color(75, 85, 99));
        pnlCalc.add(lblPhiAmount);

        // ROW 4: Khuyến mãi thành viên
        pnlCalc.add(new JLabel("Giảm giá thành viên:", SwingConstants.RIGHT));

        JPanel pnlMemberDiscount = new JPanel(new BorderLayout(5, 0));
        pnlMemberDiscount.setBackground(Color.WHITE);

        lblRankName = new JLabel("Khách lẻ (0%)");
        lblRankName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRankName.setForeground(new Color(31, 41, 55));

        lblTienGiam = new JLabel("- 0 VNĐ", SwingConstants.RIGHT);
        lblTienGiam.setForeground(new Color(22, 163, 74));
        lblTienGiam.setFont(new Font("Segoe UI", Font.ITALIC, 13));

        pnlMemberDiscount.add(lblRankName, BorderLayout.CENTER);
        pnlMemberDiscount.add(lblTienGiam, BorderLayout.EAST);

        pnlCalc.add(pnlMemberDiscount);

        // ROW 5: Mã Voucher
        pnlCalc.add(new JLabel("Mã Voucher:", SwingConstants.RIGHT));

        JPanel pnlVoucher = new JPanel(new BorderLayout(5, 0));
        pnlVoucher.setBackground(Color.WHITE);

        txtVoucher = new JTextField();
        txtVoucher.putClientProperty("JTextField.placeholderText", "Nhập mã...");

        JButton btnCheckVoucher = GUI.utils.UIStyle.buttonSm(GUI.utils.UIStyle.BtnType.PRIMARY, "Áp dụng");
        btnCheckVoucher.addActionListener(e -> applyVoucher());

        pnlVoucher.add(txtVoucher, BorderLayout.CENTER);
        pnlVoucher.add(btnCheckVoucher, BorderLayout.EAST);

        pnlCalc.add(pnlVoucher);

        // ROW 6: Tổng thanh toán
        JLabel lblTotalTitle = new JLabel("TỔNG THANH TOÁN:", SwingConstants.RIGHT);
        lblTotalTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalTitle.setForeground(new Color(220, 38, 38));

        lblThanhTien = new JLabel("0 VNĐ", SwingConstants.RIGHT);
        lblThanhTien.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblThanhTien.setForeground(new Color(220, 38, 38));

        pnlCalc.add(lblTotalTitle);
        pnlCalc.add(lblThanhTien);

        pnlFooter.add(pnlCalc, BorderLayout.NORTH);

        // Buttons
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlButtons.setBackground(Color.WHITE);

        btnInHoaDon = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.NEUTRAL, "In Hóa Đơn");
        btnInHoaDon.setPreferredSize(new Dimension(140, 40));
        btnInHoaDon.addActionListener(e -> showPrintPreview());

        JButton btnThanhToanQR = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.PRIMARY, "THANH TOÁN QR");
        btnThanhToanQR.setPreferredSize(new Dimension(180, 40));
        btnThanhToanQR.addActionListener(e -> showQRCodeDialog());

        btnThanhToan = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.SUCCESS, "THANH TOÁN & ĐÓNG BÀN");
        btnThanhToan.setPreferredSize(new Dimension(280, 40));
        btnThanhToan.addActionListener(e -> processPayment());

        pnlButtons.add(btnInHoaDon);
        pnlButtons.add(btnThanhToanQR);
        pnlButtons.add(btnThanhToan);

        pnlFooter.add(pnlButtons, BorderLayout.SOUTH);

        pnl.add(pnlFooter, BorderLayout.SOUTH);

        return pnl;
    }

    // =========================================================================
    // TAB 2: HISTORY (LỊCH SỬ HÓA ĐƠN)
    // =========================================================================
    private JPanel createHistoryTab() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. Filter Panel
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlFilter.setBackground(new Color(243, 244, 246));
        pnlFilter.setBorder(BorderFactory.createTitledBorder("Bộ Lọc"));

        dateFrom = new JDateChooser(); // Bỏ Date() ban đầu
        dateFrom.setDateFormatString("dd/MM/yyyy");
        dateFrom.setPreferredSize(new Dimension(130, 30));

        dateTo = new JDateChooser(); // Bỏ Date() ban đầu
        dateTo.setDateFormatString("dd/MM/yyyy");
        dateTo.setPreferredSize(new Dimension(130, 30));

        txtSearchHistory = new JTextField(20);
        txtSearchHistory.setPreferredSize(new Dimension(200, 30));
        txtSearchHistory.putClientProperty("JTextField.placeholderText", "SĐT Khách hoặc Mã HĐ...");

        JButton btnSearch = GUI.utils.UIStyle.buttonSm(GUI.utils.UIStyle.BtnType.PRIMARY, "Tìm Kiếm");
        btnSearch.addActionListener(e -> loadHistoryData());

        pnlFilter.add(new JLabel("Từ ngày:"));
        pnlFilter.add(dateFrom);
        pnlFilter.add(new JLabel("Đến ngày:"));
        pnlFilter.add(dateTo);
        pnlFilter.add(new JLabel("Tìm kiếm:"));
        pnlFilter.add(txtSearchHistory);
        pnlFilter.add(btnSearch);

        pnl.add(pnlFilter, BorderLayout.NORTH);

        // 2. Table
        String[] columns = { "Mã HĐ", "Bàn", "Ngày Tạo", "Tổng Tiền", "Khách Hàng", "Thu Ngân" };
        modelHistory = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblHistory = new JTable(modelHistory);
        tblHistory.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tblHistory.getSelectedRow();
                    if (row != -1) {
                        int maHD = Integer.parseInt(tblHistory.getValueAt(row, 0).toString());
                        showInvoiceDetailDialog(maHD);
                    }
                }
            }
        });
        tblHistory.setRowHeight(30);
        tblHistory.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblHistory.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Căn phải cột tiền
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tblHistory.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        JScrollPane scroll = new JScrollPane(tblHistory);
        pnl.add(scroll, BorderLayout.CENTER);

        return pnl;
    }

    private void initActiveTableSuggestionPopup() {
        modelTableSuggestions = new DefaultListModel<>();
        listTableSuggestions = new JList<>(modelTableSuggestions);
        listTableSuggestions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listTableSuggestions.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listTableSuggestions.setFixedCellHeight(44);
        listTableSuggestions.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TableSearchEntry) {
                    TableSearchEntry entry = (TableSearchEntry) value;
                    label.setText(entry.toSuggestionHtml());
                    label.setBorder(new EmptyBorder(6, 10, 6, 10));
                }
                return label;
            }
        });
        listTableSuggestions.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TableSearchEntry selectedEntry = listTableSuggestions.getSelectedValue();
                if (selectedEntry != null) {
                    selectSearchEntry(selectedEntry);
                }
            }
        });

        popupTableSuggestions = new JPopupMenu();
        popupTableSuggestions.setFocusable(false);
        popupTableSuggestions.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)));

        JScrollPane scrollSuggestion = new JScrollPane(listTableSuggestions);
        scrollSuggestion.setBorder(null);
        scrollSuggestion.setPreferredSize(new Dimension(0, 220));
        popupTableSuggestions.add(scrollSuggestion);
    }

    private void setupActiveTableSearch() {
        if (txtSearchActiveTable == null) {
            return;
        }

        txtSearchActiveTable.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleSearchChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleSearchChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleSearchChanged();
            }

            private void handleSearchChanged() {
                if (!suppressActiveTableSearchEvents) {
                    applyActiveTableSearch();
                }
            }
        });

        txtSearchActiveTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (popupTableSuggestions != null && popupTableSuggestions.isVisible()
                        && modelTableSuggestions.getSize() > 0) {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        int nextIndex = Math.min(listTableSuggestions.getSelectedIndex() + 1,
                                modelTableSuggestions.getSize() - 1);
                        listTableSuggestions.setSelectedIndex(Math.max(0, nextIndex));
                        listTableSuggestions.ensureIndexIsVisible(listTableSuggestions.getSelectedIndex());
                        e.consume();
                        return;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        int nextIndex = listTableSuggestions.getSelectedIndex() <= 0 ? 0
                                : listTableSuggestions.getSelectedIndex() - 1;
                        listTableSuggestions.setSelectedIndex(nextIndex);
                        listTableSuggestions.ensureIndexIsVisible(nextIndex);
                        e.consume();
                        return;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        TableSearchEntry selectedEntry = listTableSuggestions.getSelectedValue();
                        if (selectedEntry == null && modelTableSuggestions.getSize() > 0) {
                            selectedEntry = modelTableSuggestions.getElementAt(0);
                        }
                        if (selectedEntry != null) {
                            selectSearchEntry(selectedEntry);
                            e.consume();
                            return;
                        }
                    }
                }

                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    hideTableSuggestionPopup();
                }
            }
        });

        txtSearchActiveTable.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (!txtSearchActiveTable.isFocusOwner() && !listTableSuggestions.isFocusOwner()) {
                        hideTableSuggestionPopup();
                    }
                });
            }
        });
    }

    private void applyActiveTableSearch() {
        if (pnlTableList == null) {
            return;
        }

        java.util.List<TableSearchEntry> matches = findMatchingTableEntries(
                txtSearchActiveTable != null ? txtSearchActiveTable.getText() : "");

        pnlTableList.removeAll();
        for (TableSearchEntry entry : matches) {
            pnlTableList.add(entry.card);
        }

        updateTableListPreferredSize();
        pnlTableList.revalidate();
        pnlTableList.repaint();

        if (selectedMaBan != null) {
            highlightTableCard(selectedMaBan);
        }

        updateTableSuggestionPopup(matches);
    }

    private java.util.List<TableSearchEntry> findMatchingTableEntries(String query) {
        ArrayList<TableSearchEntry> matches = new ArrayList<>();
        String normalizedQuery = normalizeSearchText(query);
        if (normalizedQuery.isBlank()) {
            matches.addAll(activeTableEntries);
            return matches;
        }

        String[] tokens = normalizedQuery.split("\\s+");
        for (TableSearchEntry entry : activeTableEntries) {
            boolean match = true;
            for (String token : tokens) {
                if (!entry.searchableText.contains(token)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                matches.add(entry);
            }
        }
        return matches;
    }

    private void updateTableSuggestionPopup(java.util.List<TableSearchEntry> matches) {
        if (popupTableSuggestions == null || txtSearchActiveTable == null) {
            return;
        }

        modelTableSuggestions.clear();
        String query = txtSearchActiveTable.getText() != null ? txtSearchActiveTable.getText().trim() : "";
        if (query.isEmpty() || !txtSearchActiveTable.isFocusOwner()) {
            hideTableSuggestionPopup();
            return;
        }

        int limit = Math.min(matches.size(), 6);
        for (int i = 0; i < limit; i++) {
            modelTableSuggestions.addElement(matches.get(i));
        }

        if (modelTableSuggestions.isEmpty()) {
            hideTableSuggestionPopup();
            return;
        }

        listTableSuggestions.setSelectedIndex(0);
        popupTableSuggestions.setPopupSize(txtSearchActiveTable.getWidth(), 220);
        popupTableSuggestions.show(txtSearchActiveTable, 0, txtSearchActiveTable.getHeight());
    }

    private void hideTableSuggestionPopup() {
        if (popupTableSuggestions != null) {
            popupTableSuggestions.setVisible(false);
        }
    }

    private void setActiveTableSearchText(String text) {
        suppressActiveTableSearchEvents = true;
        txtSearchActiveTable.setText(text);
        suppressActiveTableSearchEvents = false;
    }

    private void selectSearchEntry(TableSearchEntry entry) {
        if (entry == null) {
            return;
        }
        hideTableSuggestionPopup();
        setActiveTableSearchText(entry.table.getMaBan());
        applyActiveTableSearch();
        highlightTableCard(entry.table.getMaBan());
        selectTable(entry.table.getMaBan());
    }

    private void highlightTableCard(String maBan) {
        resetCardSelection();
        for (TableSearchEntry entry : activeTableEntries) {
            if (entry.table != null && entry.table.getMaBan().equalsIgnoreCase(maBan)) {
                entry.card.setSelected(true);
                entry.card.repaint();
                break;
            }
        }
    }

    private void updateTableListPreferredSize() {
        int rowHeight = 160;
        int cols = 2;
        if (scrollTables != null && scrollTables.getWidth() > 0) {
            cols = scrollTables.getWidth() / 155;
        }
        if (cols < 1) {
            cols = 1;
        }
        int rows = (int) Math.ceil((double) Math.max(1, pnlTableList.getComponentCount()) / cols);
        pnlTableList.setPreferredSize(new Dimension(pnlTableList.getWidth(), rows * rowHeight + 50));
    }

    private ResolvedInvoiceInfo resolveOpenInvoiceInfo(String maBan) {
        int directMaHD = hdDAO.getMaHDByBan(maBan);
        if (directMaHD != -1) {
            return new ResolvedInvoiceInfo(maBan, directMaHD, hdDAO.getThongTinHoaDon(directMaHD));
        }

        try {
            java.sql.Connection con = connectDB.ConnectDB.getConnection();
            String sql = "SELECT TOP 1 db.MaDat FROM DatBan db " +
                    "INNER JOIN ChiTietDatBan ctdb ON db.MaDat = ctdb.MaDat " +
                    "WHERE ctdb.MaBan = ? AND db.TrangThai = N'Đã nhận bàn' " +
                    "ORDER BY db.ThoiGianBatDau DESC";
            java.sql.PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, maBan);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Entity.DatBan booking = new DAO.DatBanDAO().getDatBanByID(rs.getInt("MaDat"));
                if (booking != null) {
                    for (String relatedTable : booking.getDanhSachBan()) {
                        int relatedMaHD = hdDAO.getMaHDByBan(relatedTable);
                        if (relatedMaHD != -1) {
                            return new ResolvedInvoiceInfo(
                                    relatedTable,
                                    relatedMaHD,
                                    hdDAO.getThongTinHoaDon(relatedMaHD));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResolvedInvoiceInfo(maBan, -1, null);
    }

    private TableSearchEntry buildTableSearchEntry(Ban table) {
        GUI.components.TableCard card = new GUI.components.TableCard(table);
        card.setPreferredSize(new Dimension(140, 140));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                highlightTableCard(table.getMaBan());
                selectTable(table.getMaBan());
            }
        });

        ResolvedInvoiceInfo invoiceInfo = resolveOpenInvoiceInfo(table.getMaBan());
        int maHD = invoiceInfo.maHD;
        HoaDon hd = invoiceInfo.hoaDon;
        String sdtKhach = hd != null && hd.getSdtKhach() != null ? hd.getSdtKhach().trim() : "";
        String tenKhach = extractCustomerNameForSearch(hd, sdtKhach);
        String multiTableDisplay = hd != null ? getMultiTableDisplay(hd) : table.getMaBan();
        String searchableText = normalizeSearchText(String.join(" ",
                safe(table.getMaBan()),
                safe(table.getTenBan()),
                safe(table.getMaKV()),
                table.getSoGhe() + " chỗ",
                "hoa don " + (maHD == -1 ? "" : maHD),
                maHD == -1 ? "" : "#" + maHD,
                safe(tenKhach),
                safe(sdtKhach),
                safe(multiTableDisplay),
                hd != null ? safe(hd.getGhiChu()) : "",
                safe(table.getTrangThai())));

        return new TableSearchEntry(table, card, maHD, tenKhach, sdtKhach, multiTableDisplay, searchableText);
    }

    private String extractCustomerNameForSearch(HoaDon hd, String sdtKhach) {
        if (sdtKhach != null && !sdtKhach.isBlank()) {
            KhachHang kh = khDAO.getBySDT(sdtKhach);
            if (kh != null && kh.getTenKhach() != null && !kh.getTenKhach().isBlank()) {
                return kh.getTenKhach();
            }
        }

        if (hd != null && hd.getGhiChu() != null) {
            String ghiChu = hd.getGhiChu().trim();
            String prefix = "Khách đặt:";
            if (ghiChu.startsWith(prefix)) {
                return ghiChu.substring(prefix.length()).trim();
            }
        }

        return "Khách lẻ";
    }

    private String normalizeSearchText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase()
                .trim();
        return normalized.replaceAll("\\s+", " ");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    // =========================================================================
    // LOGIC METHODS
    // =========================================================================

    private void loadTableList() {
        pnlTableList.removeAll();
        activeTableEntries.clear();
        ArrayList<Ban> listBan = banDAO.getAllBan();

        for (Ban b : listBan) {
            if ("Có Khách".equalsIgnoreCase(b.getTrangThai())) {
                activeTableEntries.add(buildTableSearchEntry(b));
            }
        }

        applyActiveTableSearch();
    }

    private void resetCardSelection() {
        for (TableSearchEntry entry : activeTableEntries) {
            entry.card.setSelected(false);
            entry.card.repaint();
        }
    }

    private void selectTable(String maBan) {
        ResolvedInvoiceInfo invoiceInfo = resolveOpenInvoiceInfo(maBan);
        this.selectedMaBan = invoiceInfo.billingMaBan;
        this.currentMaHD = invoiceInfo.maHD;

        if (currentMaHD == -1) {
            JOptionPane.showMessageDialog(this, "Bàn này chưa có hóa đơn (Chưa gọi món)!");
            resetInvoiceUI();
            return;
        }
        loadInvoiceDetails(currentMaHD);
    }

    private void loadInvoiceDetails(int maHD) {
        HoaDon hd = hdDAO.getThongTinHoaDon(maHD);
        if (hd == null)
            return;

        String baseBan;
        if (hd.getGhiChu() != null && hd.getGhiChu().contains("| Ghép:")) {
            // Format mới: "Bàn: T1-06 | Ghép: T1-01, T1-02"
            baseBan = hd.getGhiChu().trim();
        } else if (hd.getGhiChu() != null && hd.getGhiChu().contains("[Ghép")) {
            // Format cũ
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\[Ghép từ bàn (.*?)\\]");
            java.util.regex.Matcher m = p.matcher(hd.getGhiChu());
            java.util.ArrayList<String> ml = new java.util.ArrayList<>();
            while (m.find()) ml.add(m.group(1));
            baseBan = "Bàn: " + hd.getMaBan() + (ml.isEmpty() ? "" : " | Ghép: " + String.join(", ", ml));
        } else {
            // Kiểm tra nếu bàn thuộc booking nhiều bàn → hiện tất cả
            String allBanStr = hd.getMaBan();
            try {
                java.sql.Connection con = connectDB.ConnectDB.getConnection();
                String sql = "SELECT TOP 1 db.MaDat FROM DatBan db " +
                        "INNER JOIN ChiTietDatBan ctdb ON db.MaDat = ctdb.MaDat " +
                        "WHERE ctdb.MaBan = ? AND db.TrangThai IN (N'Đã nhận bàn', N'Đã hoàn tất') " +
                        "ORDER BY db.ThoiGianBatDau DESC";
                java.sql.PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, hd.getMaBan());
                java.sql.ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Entity.DatBan booking = new DAO.DatBanDAO().getDatBanByID(rs.getInt("MaDat"));
                    if (booking != null && booking.getDanhSachBan().size() > 1) {
                        allBanStr = String.join(", ", booking.getDanhSachBan());
                    }
                }
            } catch (Exception ex) { /* ignore */ }
            baseBan = "Bàn: " + allBanStr;
        }
        lblTitleBan.setText("<html>" + baseBan + "</html>");
        lblMaHD.setText("Mã HĐ: #" + hd.getMaHD());
        lblNgayTao.setText("Ngày: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(hd.getNgayTao()));

        String sdt = hd.getSdtKhach();
        String displayKhach = "Khách lẻ";
        this.currentKhachHang = null; // Reset

        if (sdt != null && !sdt.isEmpty()) {
            this.currentKhachHang = khDAO.getBySDT(sdt); // Fetch full info
            if (this.currentKhachHang != null) {
                displayKhach = this.currentKhachHang.getTenKhach() + " (" + sdt + ")";
            } else {
                displayKhach = sdt;
            }
        }
        lblKhachHang.setText("Khách hàng: " + displayKhach);
        lblNhanVien.setText("Thu ngân: " + connectDB.SessionManager.getDisplayName());

        modelChiTiet.setRowCount(0);
        ArrayList<String[]> details = hdDAO.getChiTietHoaDon(maHD);
        double total = 0;
        for (String[] row : details) {
            String ten = row[0];
            double sl = Double.parseDouble(row[1]);
            double gia = Double.parseDouble(row[2]);
            double thanhTien = Double.parseDouble(row[3]);
            modelChiTiet.addRow(new Object[] { ten, (int) sl, formatMoney(gia), formatMoney(thanhTien) });
            total += thanhTien;
        }

        currentTongTienHang = total;

        updateFinalTotal();
    }

    private void updateFinalTotal() {
        double currentDiscountVIP = 0;
        double currentDiscountVoucher = 0;

        // Đọc VAT và phí từ SystemConfig
        double vatPercent = GUI.utils.SystemConfig.getVAT();
        double phiPercent = GUI.utils.SystemConfig.getServiceFee();
        double vatAmount  = currentTongTienHang * vatPercent / 100.0;
        double phiAmount  = currentTongTienHang * phiPercent / 100.0;

        // Cập nhật label tỷ lệ phòng hợp với config mới nhất
        lblVATRate.setText(String.format("+ VAT: %.1f%%", vatPercent));
        lblServiceRate.setText(String.format("+ Phí PV: %.1f%%", phiPercent));

        lblVATAmount.setText("+ " + formatMoney(vatAmount) + " VNĐ");
        lblPhiAmount.setText("+ " + formatMoney(phiAmount) + " VNĐ");

        // 1. VIP Discount
        if (currentKhachHang != null) {
            int percent = currentKhachHang.getPhanTramGiam();
            currentDiscountVIP = currentTongTienHang * percent / 100.0;

            String rank = currentKhachHang.getHangThanhVien();
            if (rank == null)
                rank = "Thành viên";
            lblRankName.setText(rank + " (" + percent + "%) - Giảm: " + formatMoney(currentDiscountVIP));
        } else {
            lblRankName.setText("Khách lẻ (0%)");
        }

        // 2. Voucher Discount
        if (currentVoucher != null) {
            currentDiscountVoucher = currentVoucher.tinhGiamGia(currentTongTienHang);
        }

        double totalDiscount = currentDiscountVIP + currentDiscountVoucher;

        if (currentVoucher != null) {
            lblTienGiam.setText("<html>VIP: -" + formatMoney(currentDiscountVIP) + "<br>Voucher: -"
                    + formatMoney(currentDiscountVoucher) + "</html>");
        } else {
            lblTienGiam.setText("- " + formatMoney(currentDiscountVIP) + " VNĐ");
        }

        // [GĐ4] Final = TongTien + VAT + PhiPV - GiamGia
        double finalTotal = currentTongTienHang + vatAmount + phiAmount - totalDiscount;
        if (finalTotal < 0)
            finalTotal = 0;

        lblTongTienHang.setText(formatMoney(currentTongTienHang) + " VNĐ");
        lblThanhTien.setText(formatMoney(finalTotal) + " VNĐ");
    }

    private void applyVoucher() {
        String code = txtVoucher.getText().trim();
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã Voucher!");
            return;
        }

        KhuyenMai km = kmDAO.getByMaKM(code);
        if (km == null) {
            JOptionPane.showMessageDialog(this, "Mã Voucher không tồn tại!");
            currentVoucher = null;
            updateFinalTotal();
            return;
        }

        // Validation
        if (!km.isHoatDong()) {
            JOptionPane.showMessageDialog(this, "Mã Voucher đã hết hạn hoặc ngưng hoạt động!");
            return;
        }

        if (currentTongTienHang < km.getDieuKienToiThieu()) {
            JOptionPane.showMessageDialog(this,
                    "Đơn hàng chưa đạt giá trị tối thiểu: " + formatMoney(km.getDieuKienToiThieu()));
            return;
        }

        // VIP Constraint
        if (km.getHangVIPApDung() != null && !km.getHangVIPApDung().isEmpty()) {
            if (currentKhachHang == null || !km.apDungChoHang(currentKhachHang.getHangThanhVien())) {
                JOptionPane.showMessageDialog(this,
                        "Voucher chỉ áp dụng cho hạng thành viên: " + km.getHangVIPApDung());
                return;
            }
        }

        // Success
        this.currentVoucher = km;
        JOptionPane.showMessageDialog(this, "Áp dụng Voucher thành công: " + km.getTenKM());
        updateFinalTotal();
    }

    private void showQRCodeDialog() {
        if (currentMaHD == -1) {
            JOptionPane.showMessageDialog(this, "Chưa chọn hóa đơn để thanh toán!");
            return;
        }

        try {
            // Bank info từ SystemConfig
            double amount = parseMoney(lblThanhTien.getText());
            String addInfo = "THANH TOAN HD " + currentMaHD;
            String url = GUI.utils.SystemConfig.buildQrUrl(amount, addInfo);

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Quét Mã Thanh Toán QR", true);
            dialog.setSize(400, 550);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            // Image Label
            JLabel lblQR = new JLabel("Đang tải mã QR...", SwingConstants.CENTER);
            lblQR.setFont(new Font("Segoe UI", Font.ITALIC, 14));

            // Load Image Async
            new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                    java.net.URL qrUrl = java.net.URI.create(url).toURL();
                    java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(qrUrl);
                    return new ImageIcon(image.getScaledInstance(350, 350, java.awt.Image.SCALE_SMOOTH));
                }

                @Override
                protected void done() {
                    try {
                        lblQR.setText("");
                        lblQR.setIcon(get());
                    } catch (Exception ex) {
                        lblQR.setText("Lỗi tải mã QR");
                        ex.printStackTrace();
                    }
                }
            }.execute();

            dialog.add(lblQR, BorderLayout.CENTER);

            // Button Panel
            JPanel pnlAction = new JPanel(new GridLayout(2, 1, 5, 5));
            pnlAction.setBorder(new EmptyBorder(10, 10, 10, 10));

            JLabel lblInfo = new JLabel(
                    "<html><center>Số tiền: <font color='red'>" + formatMoney(amount) + " VNĐ</font><br>"
                            + "Nội dung: " + addInfo + "</center></html>",
                    SwingConstants.CENTER);
            lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));

            JButton btnConfirm = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.SUCCESS,
                    "XÁC NHẬN ĐÃ THANH TOÁN XONG");
            btnConfirm.setPreferredSize(new Dimension(280, 40));
            btnConfirm.addActionListener(e -> {
                dialog.dispose();
                processPayment(); // Call existing payment logic
            });

            pnlAction.add(lblInfo);
            pnlAction.add(btnConfirm);

            dialog.add(pnlAction, BorderLayout.SOUTH);
            dialog.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tạo mã QR: " + e.getMessage());
        }
    }

    private void processPayment() {
        if (currentMaHD == -1) {
            JOptionPane.showMessageDialog(this, "Chưa chọn hóa đơn để thanh toán!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận thanh toán cho bàn " + selectedMaBan + "?\nTổng tiền: " + lblThanhTien.getText(),
                "Xác nhận thanh toán", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            // [GĐ4] Pass VAT/fee from config
            double vatVal = GUI.utils.SystemConfig.getVAT();
            double phiVal = GUI.utils.SystemConfig.getServiceFee();
            String maKM = (currentVoucher != null) ? txtVoucher.getText().trim() : null;
            String sdtKH = (currentKhachHang != null) ? currentKhachHang.getSoDienThoai() : null;
            boolean success = hdDAO.thanhToan(currentMaHD, maKM, sdtKH, vatVal, phiVal);
            if (success) {
                // 1. Tìm tất cả bàn trong booking TRƯỚC khi thay đổi trạng thái
                java.util.List<String> allBanToReset = new java.util.ArrayList<>();
                allBanToReset.add(selectedMaBan);
                try {
                    java.sql.Connection con = connectDB.ConnectDB.getConnection();
                    String sql = "SELECT TOP 1 db.MaDat FROM DatBan db " +
                            "INNER JOIN ChiTietDatBan ctdb ON db.MaDat = ctdb.MaDat " +
                            "WHERE ctdb.MaBan = ? AND db.TrangThai IN (N'Đã nhận bàn', N'Đã hoàn tất')";
                    java.sql.PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, selectedMaBan);
                    java.sql.ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        Entity.DatBan booking = new DAO.DatBanDAO().getDatBanByID(rs.getInt("MaDat"));
                        if (booking != null) {
                            allBanToReset = booking.getDanhSachBan();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                // 2. Reset TẤT CẢ bàn về Trống cùng lúc (không delay)
                for (String mb : allBanToReset) {
                    banDAO.updateTrangThai(mb, "Trống");
                }
                banDAO.huyGopBan(selectedMaBan);
                new DAO.DatBanDAO().completeBookingOfTable(selectedMaBan);

                // 3. UI refresh SAU khi tất cả DB đã xong
                JOptionPane.showMessageDialog(this, "Thanh toán thành công!");
                loadTableList();
                resetInvoiceUI();

                // Switch to History Tab to show recent payment?
                // tabs.setSelectedIndex(1);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thanh toán!");
            }
        }
    }

    private void resetInvoiceUI() {
        lblTitleBan.setText("Bàn: ---");
        lblMaHD.setText("Mã HĐ: ---");
        lblNgayTao.setText("Ngày: ---");
        lblKhachHang.setText("Khách hàng: ---");
        lblNhanVien.setText("Thu ngân: ---");
        modelChiTiet.setRowCount(0);
        lblTongTienHang.setText("0 VNĐ");
        lblThanhTien.setText("0 VNĐ");
        // New fields
        lblRankName.setText("Khách lẻ (0%)");
        lblTienGiam.setText("- 0 VNĐ");
        txtVoucher.setText("");

        this.currentMaHD = -1;
        this.selectedMaBan = null;
        this.currentTongTienHang = 0;
        this.currentKhachHang = null;
        this.currentVoucher = null;
        // Refresh labels tỷ lệ thuế/phí (có thể đã được thay đổi từ Settings)
        if (lblVATRate != null)
            lblVATRate.setText(String.format("+ VAT: %.1f%%", GUI.utils.SystemConfig.getVAT()));
        if (lblServiceRate != null)
            lblServiceRate.setText(String.format("+ Phí PV: %.1f%%", GUI.utils.SystemConfig.getServiceFee()));
    }

    private void loadHistoryData() {
        modelHistory.setRowCount(0);
        Date from = dateFrom.getDate();
        Date to = dateTo.getDate();
        String search = txtSearchHistory.getText().trim();

        // (if dateFrom/To is null, they will be ignored in DAO now)
        ArrayList<HoaDon> list = hdDAO.getLichSuHoaDon(from, to, search);
        for (HoaDon hd : list) {
            String tenKhach = hd.getSdtKhach();
            if (tenKhach != null && !tenKhach.isEmpty()) {
                String name = khDAO.getTenKhachHang(tenKhach);
                if (name != null)
                    tenKhach = name + " (" + tenKhach + ")";
            } else {
                tenKhach = "Vãng lai";
            }

            String banDisplayValue = getMultiTableDisplay(hd);

            modelHistory.addRow(new Object[] {
                    hd.getMaHD(),
                    banDisplayValue,
                    new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(hd.getNgayTao()),
                    formatMoney(hd.getThanhTien()) + " VNĐ", // [Bug 3] Sử dụng Thành Tiền (đã gồm VAT/Phí) thay vì Tổng
                                                             // Tiền
                    tenKhach,
                    hd.getMaNV()
            });
        }
    }

    /**
     * Lấy chuỗi hiển thị tất cả bàn cho hóa đơn (hỗ trợ ghép bàn + đặt nhiều bàn).
     */
    private String getMultiTableDisplay(HoaDon hd) {
        // 1. Ghép bàn (format mới)
        if (hd.getGhiChu() != null && hd.getGhiChu().contains("| Ghép:")) {
            return hd.getGhiChu().replaceFirst("^Bàn:\\s*", "").replace("Bàn: Bàn", "Bàn");
        }
        // 2. Ghép bàn (format cũ)
        if (hd.getGhiChu() != null && hd.getGhiChu().contains("[Ghép")) {
            return hd.getMaBan() + " " + hd.getGhiChu();
        }
        // 3. Kiểm tra booking nhiều bàn qua ChiTietDatBan
        try {
            java.sql.Connection con = connectDB.ConnectDB.getConnection();
            String sql = "SELECT TOP 1 db.MaDat FROM DatBan db " +
                    "INNER JOIN ChiTietDatBan ctdb ON db.MaDat = ctdb.MaDat " +
                    "WHERE ctdb.MaBan = ? AND db.TrangThai IN (N'Đã nhận bàn', N'Đã hoàn tất') " +
                    "ORDER BY db.ThoiGianBatDau DESC";
            java.sql.PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, hd.getMaBan());
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Entity.DatBan booking = new DAO.DatBanDAO().getDatBanByID(rs.getInt("MaDat"));
                if (booking != null && booking.getDanhSachBan().size() > 1) {
                    return String.join(", ", booking.getDanhSachBan());
                }
            }
        } catch (Exception ex) { /* ignore */ }
        return hd.getMaBan();
    }

    private String formatMoney(double amount) {
        return java.text.NumberFormat.getIntegerInstance().format(amount);
    }

    private double parseMoney(String text) {
        try {
            String s = text.replace(" VNĐ", "").replace(".", "").replace(",", "");
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private void showInvoiceDetailDialog(int maHD) {
        HoaDon hd = hdDAO.getThongTinHoaDon(maHD);
        if (hd == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi Tiết Hóa Đơn #" + maHD, true);
        dialog.setSize(825, 750);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JEditorPane previewPane = new JEditorPane();
        previewPane.setContentType("text/html");
        previewPane.setEditable(false);
        previewPane.setMargin(new java.awt.Insets(10, 20, 10, 20));

        String banDisplayPrint = getMultiTableDisplay(hd);
        
        String tenNV = "---";
        if (hd.getMaNV() != null) {
            NhanVien nv = nvDAO.getByMaNV(hd.getMaNV());
            tenNV = (nv != null) ? nv.getTenNV() : hd.getMaNV();
        }

        String sdt = hd.getSdtKhach();
        String tenKhach = "Khách vãng lai";
        if (sdt != null && !sdt.isEmpty()) {
            String name = khDAO.getTenKhachHang(sdt);
            tenKhach = (name != null) ? name + " (" + sdt + ")" : sdt;
        }

        double vatAmount = hd.getTongTien() * hd.getPhanTramVAT() / 100.0;
        double phiAmount = hd.getTongTien() * hd.getPhiPhucVu() / 100.0;

        // Build HTML
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: Arial, sans-serif; font-size: 13px; color: #333;'>");
        
        html.append("<div style='text-align: center; margin-bottom: 20px;'>");
        html.append("<h1 style='color: #b91c1c; margin: 0; font-size: 26px;'>").append(GUI.utils.SystemConfig.getResName()).append("</h1>");
        html.append("<p style='margin: 5px 0 0 0; color: #666;'>ĐC: ").append(GUI.utils.SystemConfig.getResAddress()).append("</p>");
        html.append("<p style='margin: 5px 0 0 0; color: #666;'>Hotline: ").append(GUI.utils.SystemConfig.getResPhone()).append("</p>");
        html.append("</div>");
        
        html.append("<hr style='border: 1px dashed #ccc;'/>");
        html.append("<h2 style='text-align: center; font-size: 20px; color: #000;'>HÓA ĐƠN THANH TOÁN (LỊCH SỬ)</h2>");
        
        html.append("<table width='100%' style='margin-bottom: 10px;'>");
        html.append("<tr><td width='50%'><b>Mã HĐ:</b> #").append(hd.getMaHD()).append("</td>");
        html.append("<td width='50%' align='right'><b>Ngày:</b> ").append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(hd.getNgayTao())).append("</td></tr>");
        
        html.append("<tr><td colspan='2' style='padding-top: 5px;'><b>Bàn:</b> ").append(banDisplayPrint).append("</td></tr>");
        
        html.append("<tr><td style='padding-top: 5px;'><b>Khách hàng:</b> ").append(tenKhach).append("</td>");
        html.append("<td align='right' style='padding-top: 5px;'><b>Thu ngân:</b> ").append(tenNV).append("</td></tr>");
        html.append("</table>");
        
        html.append("<hr style='border: 1px double #999;'/>");
        
        html.append("<table width='100%' style='border-collapse: collapse; margin-top: 10px; margin-bottom: 10px;'>");
        html.append("<tr style='border-bottom: 1px solid #000;'>");
        html.append("<th align='left' style='padding-bottom: 5px;'>Món Ăn</th>");
        html.append("<th align='center' style='padding-bottom: 5px;'>SL</th>");
        html.append("<th align='right' style='padding-bottom: 5px;'>Đơn Giá</th>");
        html.append("<th align='right' style='padding-bottom: 5px;'>Thành Tiền</th>");
        html.append("</tr>");
        
        ArrayList<String[]> details = hdDAO.getChiTietHoaDon(maHD);
        for (String[] row : details) {
            html.append("<tr>");
            html.append("<td style='padding: 5px 0;'>").append(row[0]).append("</td>");
            html.append("<td align='center' style='padding: 5px 0;'>").append((int) Double.parseDouble(row[1])).append("</td>");
            html.append("<td align='right' style='padding: 5px 0;'>").append(formatMoney(Double.parseDouble(row[2]))).append("</td>");
            html.append("<td align='right' style='padding: 5px 0;'>").append(formatMoney(Double.parseDouble(row[3]))).append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        
        html.append("<hr style='border: 1px solid #000;'/>");
        
        html.append("<table width='100%' style='margin-top: 10px;'>");
        html.append("<tr><td align='right' width='60%'>Tiền hàng:</td><td align='right'>").append(formatMoney(hd.getTongTien())).append(" VNĐ</td></tr>");
        html.append("<tr><td align='right'>Giảm giá:</td><td align='right'>- ").append(formatMoney(hd.getTienGiamGia())).append(" VNĐ</td></tr>");
        html.append("<tr><td align='right'>VAT (" + hd.getPhanTramVAT() + "%):</td><td align='right'>+ ").append(formatMoney(vatAmount)).append(" VNĐ</td></tr>");
        html.append("<tr><td align='right'>Phí phục vụ (" + hd.getPhiPhucVu() + "%):</td><td align='right'>+ ").append(formatMoney(phiAmount)).append(" VNĐ</td></tr>");
        html.append("<tr><td align='right' style='padding-top: 10px;'><b style='font-size: 16px;'>TỔNG THANH TOÁN:</b></td>");
        html.append("<td align='right' style='padding-top: 10px;'><b style='font-size: 18px; color: #b91c1c;'>").append(formatMoney(hd.getThanhTien())).append(" VNĐ</b></td></tr>");
        html.append("</table>");

        html.append("</body></html>");

        previewPane.setText(html.toString());
        previewPane.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(previewPane);
        scroll.setBorder(null);
        dialog.add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        pnlBot.setBackground(Color.WHITE);
        pnlBot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));
        
        JButton btnClose = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.NEUTRAL, "Đóng");
        btnClose.setPreferredSize(new Dimension(150, 40));
        btnClose.addActionListener(e -> dialog.dispose());
        
        pnlBot.add(btnClose);
        dialog.add(pnlBot, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showPrintPreview() {
        if (currentMaHD == -1) {
            JOptionPane.showMessageDialog(this, "Chưa chọn hóa đơn để in!");
            return;
        }

        JDialog previewDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Xem Trước Hóa Đơn", true);
        previewDialog.setSize(550, 750);
        previewDialog.setLocationRelativeTo(this);
        previewDialog.setLayout(new BorderLayout());

        JEditorPane previewPane = new JEditorPane();
        previewPane.setContentType("text/html");
        previewPane.setEditable(false);
        previewPane.setMargin(new java.awt.Insets(10, 20, 10, 20));

        // Build HTML content for preview
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: Arial, sans-serif; font-size: 13px; color: #333;'>");
        
        // Header
        html.append("<div style='text-align: center; margin-bottom: 20px;'>");
        html.append("<h1 style='color: #b91c1c; margin: 0; font-size: 26px;'>").append(GUI.utils.SystemConfig.getResName()).append("</h1>");
        html.append("<p style='margin: 5px 0 0 0; color: #666;'>ĐC: ").append(GUI.utils.SystemConfig.getResAddress()).append("</p>");
        html.append("<p style='margin: 5px 0 0 0; color: #666;'>Hotline: ").append(GUI.utils.SystemConfig.getResPhone()).append("</p>");
        html.append("</div>");
        
        html.append("<hr style='border: 1px dashed #ccc;'/>");
        html.append("<h2 style='text-align: center; font-size: 20px; color: #000;'>HÓA ĐƠN THANH TOÁN</h2>");
        
        // Info
        html.append("<table width='100%' style='margin-bottom: 10px;'>");
        html.append("<tr><td width='50%'><b>Mã HĐ:</b> #").append(currentMaHD).append("</td>");
        html.append("<td width='50%' align='right'><b>Bàn:</b> ").append(lblTitleBan.getText().replaceAll("<[^>]*>", "").replace("Bàn: ", "")).append("</td></tr>");
        html.append("<tr><td><b>K.Hàng:</b> ").append(lblKhachHang.getText().replace("Khách hàng: ", "")).append("</td>");
        html.append("<td align='right'><b>Thu ngân:</b> ").append(lblNhanVien.getText().replace("Thu ngân: ", "")).append("</td></tr>");
        html.append("<tr><td colspan='2'><b>Ngày:</b> ").append(lblNgayTao.getText().replace("Ngày: ", "")).append("</td></tr>");
        html.append("</table>");
        
        html.append("<hr style='border: 1px double #999;'/>");
        
        // Details Table
        html.append("<table width='100%' style='border-collapse: collapse; margin-top: 10px; margin-bottom: 10px;'>");
        html.append("<tr style='border-bottom: 1px solid #000;'>");
        html.append("<th align='left' style='padding-bottom: 5px;'>Món Ăn</th>");
        html.append("<th align='center' style='padding-bottom: 5px;'>SL</th>");
        html.append("<th align='right' style='padding-bottom: 5px;'>Đơn Giá</th>");
        html.append("<th align='right' style='padding-bottom: 5px;'>Thành Tiền</th>");
        html.append("</tr>");
        
        for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
            html.append("<tr>");
            html.append("<td style='padding: 5px 0;'>").append(modelChiTiet.getValueAt(i, 0)).append("</td>");
            html.append("<td align='center' style='padding: 5px 0;'>").append(modelChiTiet.getValueAt(i, 1)).append("</td>");
            html.append("<td align='right' style='padding: 5px 0;'>").append(modelChiTiet.getValueAt(i, 2)).append("</td>");
            html.append("<td align='right' style='padding: 5px 0;'>").append(modelChiTiet.getValueAt(i, 3)).append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");
        
        html.append("<hr style='border: 1px solid #000;'/>");
        
        // Summary
        html.append("<table width='100%' style='margin-top: 10px;'>");
        html.append("<tr><td align='right' width='60%'>Tiền hàng:</td><td align='right'>").append(lblTongTienHang.getText()).append("</td></tr>");
        html.append("<tr><td align='right'>Giảm giá:</td><td align='right'>").append(lblTienGiam.getText().replace("<html>", "").replace("<br>", " + ").replace("</html>", "")).append("</td></tr>");
        html.append("<tr><td align='right'>VAT:</td><td align='right'>").append(lblVATAmount.getText()).append("</td></tr>");
        html.append("<tr><td align='right'>Phí phục vụ:</td><td align='right'>").append(lblPhiAmount.getText()).append("</td></tr>");
        html.append("<tr><td align='right' style='padding-top: 10px;'><b style='font-size: 16px;'>TỔNG THANH TOÁN:</b></td>");
        html.append("<td align='right' style='padding-top: 10px;'><b style='font-size: 18px; color: #b91c1c;'>").append(lblThanhTien.getText()).append("</b></td></tr>");
        html.append("</table>");

        html.append("<p style='text-align: center; margin-top: 40px; font-style: italic; color: #666;'>Cảm ơn quý khách và hẹn gặp lại!</p>");
        html.append("</body></html>");

        previewPane.setText(html.toString());
        previewPane.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(previewPane);
        scroll.setBorder(null);
        previewDialog.add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        pnlBot.setBackground(Color.WHITE);
        pnlBot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));
        
        JButton btnHuy = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.NEUTRAL, "Hủy");
        btnHuy.setPreferredSize(new Dimension(120, 40));
        btnHuy.addActionListener(e -> previewDialog.dispose());
        
        JButton btnIn = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.SUCCESS, "Xuất PDF");
        btnIn.setPreferredSize(new Dimension(180, 40));
        btnIn.addActionListener(e -> {
            previewDialog.dispose();
            exportToPDF();
        });

        pnlBot.add(btnHuy);
        pnlBot.add(btnIn);
        previewDialog.add(pnlBot, BorderLayout.SOUTH);

        previewDialog.setVisible(true);
    }

    private void exportToPDF() {
        if (currentMaHD == -1) return;

        try {
            String fileName = "HoaDon_" + currentMaHD + ".pdf";
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Setup fonts
            BaseFont baseFont = BaseFont.createFont("C:/Windows/Fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            com.itextpdf.text.Font fTitle = new com.itextpdf.text.Font(baseFont, 24, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(baseFont, 14, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fNormal = new com.itextpdf.text.Font(baseFont, 12, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Font fSmall = new com.itextpdf.text.Font(baseFont, 10, com.itextpdf.text.Font.ITALIC);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(baseFont, 12, com.itextpdf.text.Font.BOLD);

            // Restaurant Header
            Paragraph title = new Paragraph("NHÀ HÀNG HẬU", fTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph address = new Paragraph("ĐC: 12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM\nHotline: 0123.456.789\n", fNormal);
            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);
            
            // Dotted line
            com.itextpdf.text.pdf.draw.DottedLineSeparator separator = new com.itextpdf.text.pdf.draw.DottedLineSeparator();
            separator.setPercentage(100);
            document.add(new com.itextpdf.text.Chunk(separator));

            Paragraph invoiceTitle = new Paragraph("\nHÓA ĐƠN THANH TOÁN\n", fHeader);
            invoiceTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(invoiceTitle);
            document.add(new Paragraph("\n"));

            // Info Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.getDefaultCell().setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            
            infoTable.addCell(new Phrase("Mã HĐ: #" + currentMaHD, fNormal));
            PdfPCell cBan = new PdfPCell(new Phrase("Bàn: " + selectedMaBan, fNormal));
            cBan.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            cBan.setHorizontalAlignment(Element.ALIGN_RIGHT);
            infoTable.addCell(cBan);
            
            infoTable.addCell(new Phrase("Khách: " + lblKhachHang.getText().replace("Khách hàng: ", ""), fNormal));
            PdfPCell cThuNgan = new PdfPCell(new Phrase("Thu ngân: " + lblNhanVien.getText().replace("Thu ngân: ", ""), fNormal));
            cThuNgan.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            cThuNgan.setHorizontalAlignment(Element.ALIGN_RIGHT);
            infoTable.addCell(cThuNgan);
            
            PdfPCell cNgay = new PdfPCell(new Phrase("Ngày: " + lblNgayTao.getText().replace("Ngày: ", ""), fNormal));
            cNgay.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            cNgay.setColspan(2);
            infoTable.addCell(cNgay);
            
            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // Details Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1.5f, 2.5f, 3f});

            // Headers
            String[] headersArr = {"Món Ăn", "SL", "Đơn Giá", "Thành Tiền"};
            for (int i = 0; i < headersArr.length; i++) {
                PdfPCell hCell = new PdfPCell(new Phrase(headersArr[i], fBold));
                hCell.setPaddingBottom(8);
                hCell.setBorderWidthLeft(0);
                hCell.setBorderWidthRight(0);
                hCell.setBorderWidthTop(1);
                hCell.setBorderWidthBottom(1);
                if (i == 0) hCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                else if (i == 1) hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                else hCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(hCell);
            }

            // Data rows
            for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
                PdfPCell c1 = new PdfPCell(new Phrase(modelChiTiet.getValueAt(i, 0).toString(), fNormal));
                c1.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                c1.setPaddingTop(5);
                c1.setPaddingBottom(5);
                
                PdfPCell c2 = new PdfPCell(new Phrase(modelChiTiet.getValueAt(i, 1).toString(), fNormal));
                c2.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c2.setPaddingTop(5);
                
                PdfPCell c3 = new PdfPCell(new Phrase(modelChiTiet.getValueAt(i, 2).toString(), fNormal));
                c3.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c3.setPaddingTop(5);
                
                PdfPCell c4 = new PdfPCell(new Phrase(modelChiTiet.getValueAt(i, 3).toString(), fNormal));
                c4.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                c4.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c4.setPaddingTop(5);

                table.addCell(c1);
                table.addCell(c2);
                table.addCell(c3);
                table.addCell(c4);
            }

            document.add(table);
            document.add(new com.itextpdf.text.Chunk(separator));
            document.add(new Paragraph("\n"));

            // Summary Table
            PdfPTable sumTable = new PdfPTable(2);
            sumTable.setWidthPercentage(100);
            sumTable.setWidths(new float[]{7f, 3f});
            sumTable.getDefaultCell().setBorder(com.itextpdf.text.Rectangle.NO_BORDER);

            sumTable.addCell(new Phrase("Tiền hàng:", fNormal));
            PdfPCell r1 = new PdfPCell(new Phrase(lblTongTienHang.getText(), fNormal));
            r1.setBorder(com.itextpdf.text.Rectangle.NO_BORDER); r1.setHorizontalAlignment(Element.ALIGN_RIGHT); sumTable.addCell(r1);

            sumTable.addCell(new Phrase("Giảm giá:", fNormal));
            String giamGiaClean = lblTienGiam.getText().replace("<html>", "").replace("<br>", " + ").replace("</html>", "");
            PdfPCell r2 = new PdfPCell(new Phrase(giamGiaClean, fNormal));
            r2.setBorder(com.itextpdf.text.Rectangle.NO_BORDER); r2.setHorizontalAlignment(Element.ALIGN_RIGHT); sumTable.addCell(r2);

            sumTable.addCell(new Phrase("VAT:", fNormal));
            PdfPCell r3 = new PdfPCell(new Phrase(lblVATAmount.getText(), fNormal));
            r3.setBorder(com.itextpdf.text.Rectangle.NO_BORDER); r3.setHorizontalAlignment(Element.ALIGN_RIGHT); sumTable.addCell(r3);

            sumTable.addCell(new Phrase("Phí phục vụ:", fNormal));
            PdfPCell r4 = new PdfPCell(new Phrase(lblPhiAmount.getText(), fNormal));
            r4.setBorder(com.itextpdf.text.Rectangle.NO_BORDER); r4.setHorizontalAlignment(Element.ALIGN_RIGHT); sumTable.addCell(r4);
            
            // Total Row
            PdfPCell tLabel = new PdfPCell(new Phrase("\nTỔNG THANH TOÁN:", fBold));
            tLabel.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            sumTable.addCell(tLabel);

            PdfPCell tAmount = new PdfPCell(new Phrase("\n" + lblThanhTien.getText(), fHeader));
            tAmount.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            tAmount.setHorizontalAlignment(Element.ALIGN_RIGHT);
            sumTable.addCell(tAmount);

            document.add(sumTable);
            
            Paragraph footer = new Paragraph("\nCảm ơn quý khách và hẹn gặp lại!", fSmall);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            JOptionPane.showMessageDialog(this, "Đã xuất PDF thành công: " + fileName);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi xuất PDF: " + e.getMessage());
        }
    }

    private static class ResolvedInvoiceInfo {
        private final String billingMaBan;
        private final int maHD;
        private final HoaDon hoaDon;

        private ResolvedInvoiceInfo(String billingMaBan, int maHD, HoaDon hoaDon) {
            this.billingMaBan = billingMaBan;
            this.maHD = maHD;
            this.hoaDon = hoaDon;
        }
    }

    private static class TableSearchEntry {
        private final Ban table;
        private final GUI.components.TableCard card;
        private final int maHD;
        private final String tenKhach;
        private final String sdtKhach;
        private final String relatedTables;
        private final String searchableText;

        private TableSearchEntry(Ban table, GUI.components.TableCard card, int maHD, String tenKhach,
                String sdtKhach, String relatedTables, String searchableText) {
            this.table = table;
            this.card = card;
            this.maHD = maHD;
            this.tenKhach = tenKhach;
            this.sdtKhach = sdtKhach;
            this.relatedTables = relatedTables;
            this.searchableText = searchableText;
        }

        private String toSuggestionHtml() {
            String hdLabel = maHD == -1 ? "Chưa có HĐ" : "HĐ #" + maHD;
            String customerLabel = (tenKhach == null || tenKhach.isBlank()) ? "Khách lẻ" : tenKhach;
            String phoneLabel = (sdtKhach == null || sdtKhach.isBlank()) ? "" : " · " + sdtKhach;
            String relatedLabel = (relatedTables == null || relatedTables.isBlank()
                    || relatedTables.equalsIgnoreCase(table.getMaBan()))
                            ? ""
                            : " · " + relatedTables;

            return "<html><b>" + table.getMaBan() + "</b> · " + hdLabel
                    + "<br><span style='color:#6b7280'>" + customerLabel
                    + phoneLabel
                    + " · " + table.getSoGhe() + " chỗ"
                    + relatedLabel
                    + "</span></html>";
        }
    }
}
