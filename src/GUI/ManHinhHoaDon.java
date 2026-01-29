package GUI;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import DAO.BanDAO;
import DAO.HoaDonDAO;
import DAO.KhuyenMaiDAO;
import Entity.Ban;
import Entity.HoaDon;
import Entity.KhuyenMai;

public class ManHinhHoaDon extends JPanel {

    private BanDAO banDAO = new BanDAO();
    private HoaDonDAO hdDAO = new HoaDonDAO();
    private KhuyenMaiDAO kmDAO = new KhuyenMaiDAO();

    // UI Components - Left (Table List)
    private JPanel pnlTableList;
    private JScrollPane scrollTables;

    // UI Components - Right (Invoice)
    private JLabel lblTitleBan;
    private JLabel lblNhanVien;
    private JLabel lblMaHD;
    private JLabel lblNgayTao;
    private JLabel lblKhachHang;

    private JTable tblChiTiet;
    private DefaultTableModel modelChiTiet;

    private JComboBox<KhuyenMai> cboKhuyenMai;
    private JLabel lblTongTienHang;
    private JLabel lblGiamGia;
    private JLabel lblThanhTien;

    private JButton btnThanhToan;
    private JButton btnInHoaDon;

    // Data
    private String selectedMaBan = null;
    private int currentMaHD = -1;
    private double currentTongTienHang = 0;

    public ManHinhHoaDon() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Split Pane: Left (Tables 30%) - Right (Invoice 70%)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createRightPanel());
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0.3);
        add(splitPane, BorderLayout.CENTER);

        loadTableList();

        // Auto-refresh
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadTableList();
            }
        });
    }

    // --- LEFT PANEL: LIST OF ACTIVE TABLES ---
    private JPanel createLeftPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(243, 244, 246));
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblHeader = new JLabel("BÀN ĐANG PHỤC VỤ");
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

    private void loadTableList() {
        pnlTableList.removeAll();
        ArrayList<Ban> listBan = banDAO.getAllBan();

        for (Ban b : listBan) {
            // Only show tables with status "Có Khách"
            if ("Có Khách".equalsIgnoreCase(b.getTrangThai())) {

                // Use TableCard component
                GUI.components.TableCard card = new GUI.components.TableCard(b);
                card.setPreferredSize(new Dimension(140, 140)); // Smaller for list
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));

                // Custom mouse listener for selection
                card.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Visual feedback
                        resetCardSelection();
                        card.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
                        selectTable(b.getMaBan());
                    }
                });

                pnlTableList.add(card);
            }
        }

        // Adjust height
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
                ((GUI.components.TableCard) c).setBorder(null); // Reset border using standard logic if possible, or
                                                                // just remove custom border
                // Ideally TableCard has a setSelected(boolean) but checking raw border for now
                ((GUI.components.TableCard) c).repaint();
            }
        }
    }

    // --- RIGHT PANEL: INVOICE DETAILS ---
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
        JPanel pnlCalc = new JPanel(new GridLayout(3, 2, 10, 10));
        pnlCalc.setBackground(Color.WHITE);

        pnlCalc.add(new JLabel("Tổng tiền hàng:", SwingConstants.RIGHT));
        lblTongTienHang = new JLabel("0 VNĐ", SwingConstants.RIGHT);
        lblTongTienHang.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlCalc.add(lblTongTienHang);

        pnlCalc.add(new JLabel("Khuyến Mãi:", SwingConstants.RIGHT));
        cboKhuyenMai = new JComboBox<>();
        cboKhuyenMai.addItem(null); // Default no promo

        // Load promotions
        ArrayList<KhuyenMai> promos = kmDAO.getKhuyenMaiDangHoatDong();
        for (KhuyenMai km : promos)
            cboKhuyenMai.addItem(km);

        cboKhuyenMai.addActionListener(e -> updateFinalTotal());
        pnlCalc.add(cboKhuyenMai); // Better if right aligned or wrapper? okay for grid

        JLabel lblTotalTitle = new JLabel("THANH TOÁN:", SwingConstants.RIGHT);
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

    // --- LOGIC ---

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

        // Fetch Customer Name
        String sdt = hd.getSdtKhach();
        String displayKhach = "Khách lẻ";
        if (sdt != null && !sdt.isEmpty()) {
            String tenKhach = new DAO.KhachHangDAO().getTenKhachHang(sdt);
            if (tenKhach != null) {
                displayKhach = tenKhach + " (" + sdt + ")";
            } else {
                displayKhach = sdt;
            }
        }
        lblKhachHang.setText("Khách hàng: " + displayKhach);
        lblNhanVien.setText("Thu ngân: " + connectDB.SessionManager.getDisplayName());

        // Load items
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
        double discount = 0;
        KhuyenMai selectedKM = (KhuyenMai) cboKhuyenMai.getSelectedItem();

        if (selectedKM != null) {
            discount = selectedKM.tinhGiamGia(currentTongTienHang);
        }

        double finalTotal = currentTongTienHang - discount;
        if (finalTotal < 0)
            finalTotal = 0;

        lblTongTienHang.setText(formatMoney(currentTongTienHang) + " VNĐ");
        // lblGiamGia.setText can be added if UI has it, currently just calculating
        lblThanhTien.setText(formatMoney(finalTotal) + " VNĐ");
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

            // 1. Update Invoice Status
            boolean success = hdDAO.thanhToan(currentMaHD, finalTotal);
            if (success) {
                // 2. Update Table Status to Empty
                banDAO.updateTrangThai(selectedMaBan, "Trống");

                JOptionPane.showMessageDialog(this, "Thanh toán thành công!");
                loadTableList(); // Refresh left list
                resetInvoiceUI();
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
        this.currentMaHD = -1;
        this.selectedMaBan = null;
        this.currentTongTienHang = 0;
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
}
