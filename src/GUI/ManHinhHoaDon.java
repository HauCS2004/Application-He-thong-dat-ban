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

import DAO.BanDAO;
import DAO.HoaDonDAO;
import DAO.KhuyenMaiDAO;
import DAO.KhachHangDAO;
import Entity.Ban;
import Entity.HoaDon;
import Entity.KhuyenMai;
import Entity.NhanVien;

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

    // API: Switch to active table
    public void selectActiveTable(String maBan) {
        tabs.setSelectedIndex(0); // Switch to Payment Tab
        loadTableList(); // specific load might be needed
        // Ideally loop through pnlTableList buttons to find and simulate click
        // Or refactor loadTableList to auto-select if selectedMaBan is set
        for (Component c : pnlTableList.getComponents()) {
            if (c instanceof JButton) {
                JButton btn = (JButton) c;
                if (btn.getText().contains(maBan)) { // Simplistic check (Text usually contains Ma or Ten)
                    btn.doClick();
                    break;
                }
            }
        }
    }

    public ManHinhHoaDon() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabs.addTab("Lập Hóa Đơn", GUI.utils.IconHelper.loadIcon("view/icons/payment.png"), createPaymentTab());
        tabs.addTab("Lịch Sử Hóa Đơn", GUI.utils.IconHelper.loadIcon("view/icons/order-history.png"),
                createHistoryTab());

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
            if (tabs.getSelectedIndex() == 0) {
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

        JLabel lblHeader = new JLabel("BÀN ĐANG PHỤC VỤ (" + connectDB.SessionManager.getDisplayName() + ")");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHeader.setForeground(new Color(31, 41, 55));
        lblHeader.setBorder(new EmptyBorder(0, 0, 10, 0));
        pnl.add(lblHeader, BorderLayout.NORTH);

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
                pnlTableList.revalidate();
            }
        });

        pnl.add(scrollTables, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Làm mới danh sách");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.addActionListener(e -> loadTableList());
        pnl.add(btnRefresh, BorderLayout.SOUTH);

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

        // Restaurant Name
        JLabel lblResName = new JLabel("NHÀ HÀNG HẬU");
        lblResName.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblResName.setForeground(new Color(185, 28, 28)); // Dark Red
        lblResName.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblResName);

        // Address
        JLabel lblAddress = new JLabel("ĐC: 12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM");
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
        JPanel pnlCalc = new JPanel(new GridLayout(4, 2, 10, 10)); // 4 Rows
        pnlCalc.setBackground(Color.WHITE);

        // ROW 1: Tổng tiền hàng
        pnlCalc.add(new JLabel("Tổng tiền hàng:", SwingConstants.RIGHT));
        lblTongTienHang = new JLabel("0 VNĐ", SwingConstants.RIGHT);
        lblTongTienHang.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlCalc.add(lblTongTienHang);

        // ROW 2: Khuyến mãi thành viên
        // Left: Label
        pnlCalc.add(new JLabel("Giảm giá thành viên:", SwingConstants.RIGHT));

        // Right: Panel containing Rank Name + Amount Label
        JPanel pnlMemberDiscount = new JPanel(new BorderLayout(5, 0));
        pnlMemberDiscount.setBackground(Color.WHITE);

        lblRankName = new JLabel("Khách lẻ (0%)");
        lblRankName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRankName.setForeground(new Color(31, 41, 55));

        lblTienGiam = new JLabel("- 0 VNĐ", SwingConstants.RIGHT);
        lblTienGiam.setForeground(new Color(22, 163, 74)); // Green
        lblTienGiam.setFont(new Font("Segoe UI", Font.ITALIC, 13));

        pnlMemberDiscount.add(lblRankName, BorderLayout.CENTER);
        pnlMemberDiscount.add(lblTienGiam, BorderLayout.EAST);

        pnlCalc.add(pnlMemberDiscount);

        // ROW 3: Mã Voucher (Placeholder)
        pnlCalc.add(new JLabel("Mã Voucher:", SwingConstants.RIGHT));

        JPanel pnlVoucher = new JPanel(new BorderLayout(5, 0));
        pnlVoucher.setBackground(Color.WHITE);

        txtVoucher = new JTextField();
        txtVoucher.putClientProperty("JTextField.placeholderText", "Nhập mã...");

        JButton btnCheckVoucher = new JButton("Áp dụng");
        btnCheckVoucher.setBackground(new Color(243, 244, 246));
        btnCheckVoucher.addActionListener(e -> applyVoucher());

        pnlVoucher.add(txtVoucher, BorderLayout.CENTER);
        pnlVoucher.add(btnCheckVoucher, BorderLayout.EAST);

        pnlCalc.add(pnlVoucher);

        // ROW 4: Tổng thanh toán
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

        btnInHoaDon = new JButton("In Hóa Đơn");
        btnInHoaDon.setPreferredSize(new Dimension(120, 40));

        btnThanhToan = new JButton("THANH TOÁN & ĐÓNG BÀN");
        btnThanhToan.setBackground(new Color(16, 185, 129));
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnThanhToan.setPreferredSize(new Dimension(250, 40));
        btnThanhToan.addActionListener(e -> processPayment());

        pnlButtons.add(btnInHoaDon);
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

        dateFrom = new JDateChooser(new Date());
        dateFrom.setDateFormatString("dd/MM/yyyy");
        dateFrom.setPreferredSize(new Dimension(130, 30));

        dateTo = new JDateChooser(new Date());
        dateTo.setDateFormatString("dd/MM/yyyy");
        dateTo.setPreferredSize(new Dimension(130, 30));

        txtSearchHistory = new JTextField(20);
        txtSearchHistory.setPreferredSize(new Dimension(200, 30));
        txtSearchHistory.putClientProperty("JTextField.placeholderText", "SĐT Khách hoặc Mã HĐ...");

        JButton btnSearch = new JButton("Tìm Kiếm");
        btnSearch.setBackground(new Color(52, 152, 219));
        btnSearch.setForeground(Color.WHITE);
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

    // =========================================================================
    // LOGIC METHODS
    // =========================================================================

    private void loadTableList() {
        pnlTableList.removeAll();
        ArrayList<Ban> listBan = banDAO.getAllBan();

        for (Ban b : listBan) {
            if ("Có Khách".equalsIgnoreCase(b.getTrangThai())) {
                GUI.components.TableCard card = new GUI.components.TableCard(b);
                card.setPreferredSize(new Dimension(140, 140));
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                card.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        resetCardSelection();
                        card.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
                        selectTable(b.getMaBan());
                    }
                });
                pnlTableList.add(card);
            }
        }

        int rowHeight = 160;
        int cols = 2;
        if (scrollTables.getWidth() > 0)
            cols = scrollTables.getWidth() / 155;
        if (cols < 1)
            cols = 1;
        int rows = (int) Math.ceil((double) pnlTableList.getComponentCount() / cols);
        pnlTableList.setPreferredSize(new Dimension(pnlTableList.getWidth(), rows * rowHeight + 50));

        pnlTableList.revalidate();
        pnlTableList.repaint();
    }

    private void resetCardSelection() {
        for (Component c : pnlTableList.getComponents()) {
            if (c instanceof GUI.components.TableCard) {
                ((GUI.components.TableCard) c).setBorder(null);
                ((GUI.components.TableCard) c).repaint();
            }
        }
    }

    private void selectTable(String maBan) {
        this.selectedMaBan = maBan;
        this.currentMaHD = hdDAO.getMaHDByBan(maBan);

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

        lblTitleBan.setText("Bàn: " + hd.getMaBan());
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

        // Update Label Text to show details if voucher applied
        if (currentVoucher != null) {
            lblTienGiam.setText("<html>VIP: -" + formatMoney(currentDiscountVIP) + "<br>Voucher: -"
                    + formatMoney(currentDiscountVoucher) + "</html>");
        } else {
            lblTienGiam.setText("- " + formatMoney(currentDiscountVIP) + " VNĐ");
        }

        double finalTotal = currentTongTienHang - totalDiscount;
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

        KhuyenMai km = kmDAO.getByCode(code);
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

    private void processPayment() {
        if (currentMaHD == -1) {
            JOptionPane.showMessageDialog(this, "Chưa chọn hóa đơn để thanh toán!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận thanh toán cho bàn " + selectedMaBan + "?\nTổng tiền: " + lblThanhTien.getText(),
                "Xác nhận thanh toán", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            double finalTotal = parseMoney(lblThanhTien.getText());
            boolean success = hdDAO.thanhToan(currentMaHD, finalTotal);
            if (success) {
                // Tích điểm cho khách hàng
                HoaDon hd = hdDAO.getThongTinHoaDon(currentMaHD);
                if (hd != null && hd.getSdtKhach() != null && !hd.getSdtKhach().isEmpty()) {
                    System.out.println(
                            "DEBUG: ManHinhHoaDon Tich Diem - SDT: " + hd.getSdtKhach() + " Total: " + finalTotal);
                    khDAO.tichDiem(hd.getSdtKhach(), finalTotal);
                }

                banDAO.updateTrangThai(selectedMaBan, "Trống");
                new DAO.DatBanDAO().completeBookingOfTable(selectedMaBan);
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
    }

    private void loadHistoryData() {
        modelHistory.setRowCount(0);
        Date from = dateFrom.getDate();
        Date to = dateTo.getDate();
        String search = txtSearchHistory.getText().trim();

        if (from == null || to == null)
            return;

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

            modelHistory.addRow(new Object[] {
                    hd.getMaHD(),
                    "Bàn " + hd.getMaBan(),
                    new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(hd.getNgayTao()),
                    formatMoney(hd.getTongTien()) + " VNĐ",
                    tenKhach,
                    hd.getMaNV()
            });
        }
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
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi Tiết Hóa Đơn #" + maHD, true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        HoaDon hd = hdDAO.getThongTinHoaDon(maHD);
        if (hd == null)
            return;

        // Header Info
        JPanel pnlInfo = new JPanel(new GridLayout(4, 2, 10, 10));
        pnlInfo.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlInfo.setBackground(Color.WHITE);

        pnlInfo.add(new JLabel("Mã Hóa Đơn: " + hd.getMaHD()));
        pnlInfo.add(
                new JLabel("Ngày tạo: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(hd.getNgayTao())));
        pnlInfo.add(new JLabel("Bàn: " + hd.getMaBan()));

        String tenNV = "---";
        if (hd.getMaNV() != null) {
            NhanVien nv = nvDAO.getByMaNV(hd.getMaNV());
            if (nv != null)
                tenNV = nv.getTenNV();
            else
                tenNV = hd.getMaNV();
        }
        pnlInfo.add(new JLabel("Thu ngân: " + tenNV));

        String sdt = hd.getSdtKhach();
        String tenKhach = "Khách lẻ";
        if (sdt != null && !sdt.isEmpty()) {
            String name = khDAO.getTenKhachHang(sdt);
            if (name != null)
                tenKhach = name + " (" + sdt + ")";
            else
                tenKhach = sdt;
        }
        pnlInfo.add(new JLabel("Khách hàng: " + tenKhach));
        pnlInfo.add(new JLabel("Tổng tiền: " + formatMoney(hd.getTongTien()) + " VNĐ"));

        dialog.add(pnlInfo, BorderLayout.NORTH);

        // Details Table
        String[] headers = { "Món Ăn", "Số Lượng", "Đơn Giá", "Thành Tiền" };
        DefaultTableModel model = new DefaultTableModel(headers, 0);
        JTable table = new JTable(model);
        table.setRowHeight(25);

        ArrayList<String[]> details = hdDAO.getChiTietHoaDon(maHD);
        for (String[] row : details) {
            String ten = row[0];
            double sl = Double.parseDouble(row[1]);
            double gia = Double.parseDouble(row[2]);
            double thanhTien = Double.parseDouble(row[3]);
            model.addRow(new Object[] { ten, (int) sl, formatMoney(gia), formatMoney(thanhTien) });
        }

        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        // Close Button
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel pnlBot = new JPanel();
        pnlBot.add(btnClose);
        dialog.add(pnlBot, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
