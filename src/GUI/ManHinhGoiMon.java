package GUI;

import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import DAO.HoaDonDAO;
import DAO.MonAnDAO;
import DAO.LoaiMonDAO;
import DAO.ChiTietHoaDonDAO;
import DAO.BangGiaDAO;
import Entity.MonAn;
import Entity.LoaiMon;
import Entity.ChiTietHoaDon;

public class ManHinhGoiMon extends JFrame {

    private int maHD;
    private String tenBan;
    private boolean isReadOnly;

    private HoaDonDAO hdDAO = new HoaDonDAO();
    private MonAnDAO monAnDAO = new MonAnDAO();
    private LoaiMonDAO loaiMonDAO = new LoaiMonDAO();
    private ChiTietHoaDonDAO cthdDAO = new ChiTietHoaDonDAO();
    private BangGiaDAO bangGiaDAO = new BangGiaDAO();

    // UI Components
    private JPanel pnlMenuCards;
    private JScrollPane scrollMenu;

    private JTable tblOrder;
    private DefaultTableModel modelOrder;
    private JComboBox<String> cboLoaiMon;
    private JTextField txtSearch;
    private JLabel lblTongTien;

    // Data cache
    private ArrayList<MonAn> listMonAn;
    private ArrayList<LoaiMon> listLoaiMon;

    public ManHinhGoiMon(int maHD, String tenBan, boolean isReadOnly) {
        this.maHD = maHD;
        this.tenBan = tenBan;
        this.isReadOnly = isReadOnly;

        // Load referenced data first
        listMonAn = monAnDAO.getAllForOrder(); // Chỉ CON_MON + HET_MON (bỏ NGUNG_BAN)
        listLoaiMon = loaiMonDAO.getAllLoai();

        initGUI();
        loadMenuCards();
        loadOrderData();
    }

    private void initGUI() {
        setTitle("Gọi Món - Bàn: " + tenBan);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- HEADER ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(31, 41, 55));
        pnlHeader.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("MENU GỌI MÓN - " + tenBan.toUpperCase());
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        pnlHeader.add(lblTitle, BorderLayout.WEST);

        add(pnlHeader, BorderLayout.NORTH);

        add(pnlHeader, BorderLayout.NORTH);

        // --- CONTENT SPLIT PANE ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createVisualMenuPanel(), createOrderPanel());
        splitPane.setDividerLocation(750); // Menu wider
        splitPane.setResizeWeight(0.7);
        add(splitPane, BorderLayout.CENTER);
    }

    // --- VISUAL MENU PANEL ---
    private JPanel createVisualMenuPanel() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnl.setBackground(Color.WHITE);

        // Filter Section
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlFilter.setOpaque(false);

        // Search
        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(200, 35));
        txtSearch.putClientProperty("JTextField.placeholderText", "🔍 Tên món ăn...");
        txtSearch.addActionListener(e -> filterMenuCards());

        // Category
        cboLoaiMon = new JComboBox<>();
        cboLoaiMon.setPreferredSize(new Dimension(150, 35));
        cboLoaiMon.addItem("Tất cả loại");
        for (LoaiMon lm : listLoaiMon) {
            cboLoaiMon.addItem(lm.getTenLoai());
        }
        cboLoaiMon.addActionListener(e -> filterMenuCards());

        JButton btnSearch = GUI.utils.UIStyle.buttonSm(GUI.utils.UIStyle.BtnType.PRIMARY, "Tìm Kiếm");
        btnSearch.addActionListener(e -> filterMenuCards());

        pnlFilter.add(txtSearch);
        pnlFilter.add(cboLoaiMon);
        pnlFilter.add(btnSearch);

        pnl.add(pnlFilter, BorderLayout.NORTH);

        // Menu Cards Container
        // Use FlowLayout to prevent stretching.
        pnlMenuCards = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        pnlMenuCards.setBackground(Color.WHITE);
        pnlMenuCards.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Hack for FlowLayout in ScrollPane: setPreferredSize height dynamically
        pnlMenuCards.setPreferredSize(new Dimension(700, 2000));

        scrollMenu = new JScrollPane(pnlMenuCards);
        scrollMenu.getVerticalScrollBar().setUnitIncrement(16);
        scrollMenu.setBorder(null);

        // Resize listener to adjust height based on width
        scrollMenu.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                pnlMenuCards.revalidate();
            }
        });

        pnl.add(scrollMenu, BorderLayout.CENTER);

        return pnl;
    }

    // --- LOAD MENU CARDS ---
    private void loadMenuCards() {
        filterMenuCards();
    }

    private void filterMenuCards() {
        pnlMenuCards.removeAll(); // Clear existing

        String keyword = txtSearch.getText().toLowerCase();
        String selectedLoai = (String) cboLoaiMon.getSelectedItem();
        String mapMaLoai = null;

        if (!"Tất cả loại".equals(selectedLoai)) {
            for (LoaiMon lm : listLoaiMon) {
                if (lm.getTenLoai().equals(selectedLoai)) {
                    mapMaLoai = lm.getMaLoai();
                    break;
                }
            }
        }

        for (MonAn m : listMonAn) {
            // Bỏ NGUNG_BAN (không hiển thị)
            if (Entity.MonAn.NGUNG_BAN.equals(m.getTrangThai()))
                continue;

            boolean matchName = m.getTenMon().toLowerCase().contains(keyword);
            boolean matchType = (mapMaLoai == null) || m.getMaLoai().equals(mapMaLoai);

            if (matchName && matchType) {
                JPanel card = createMenuItemCard(m);
                pnlMenuCards.add(card);
            }
        }

        // Adjust Height logic roughly for FlowLayout
        int cards = pnlMenuCards.getComponentCount();
        int rowHeight = 300;
        int cols = 3;
        if (scrollMenu != null && scrollMenu.getWidth() > 0)
            cols = scrollMenu.getWidth() / 240;
        if (cols < 1)
            cols = 1;
        int rows = (int) Math.ceil((double) cards / cols);
        pnlMenuCards.setPreferredSize(new Dimension(pnlMenuCards.getWidth(), rows * rowHeight + 100));

        pnlMenuCards.revalidate();
        pnlMenuCards.repaint();
    }

    // --- CREATE SINGLE ITEM CARD ---
    private JPanel createMenuItemCard(MonAn m) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                new EmptyBorder(10, 10, 10, 10)));
        // Strict size for grid-like effect in FlowLayout
        card.setPreferredSize(new Dimension(200, 280));

        // 1. IMAGE (Top)
        JLabel lblImage = new JLabel();
        lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblImage.setPreferredSize(new Dimension(180, 140));
        lblImage.setBackground(new Color(243, 244, 246));
        lblImage.setOpaque(true);

        // Load Image
        ImageIcon icon = null;
        if (m.getHinhAnh() != null && !m.getHinhAnh().isEmpty()) {
            // Use IconHelper to load (supports src/ fallback)
            icon = GUI.utils.IconHelper.loadIcon("view/image/" + m.getHinhAnh());

            if (icon != null) {
                // Resize
                Image img = icon.getImage().getScaledInstance(180, 140, Image.SCALE_SMOOTH);
                icon = new ImageIcon(img);
            }
        }

        if (icon != null) {
            lblImage.setIcon(icon);
        } else {
            lblImage.setText("IMAGE");
            lblImage.setFont(new Font("Segoe UI", Font.BOLD, 18));
        }
        card.add(lblImage, BorderLayout.NORTH);

        // 2. INFO (Center)
        JPanel pnlInfo = new JPanel(new GridLayout(2, 1));
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setBorder(new EmptyBorder(5, 0, 5, 0));

        JLabel lblName = new JLabel(m.getTenMon());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setHorizontalAlignment(SwingConstants.CENTER);

        // Lấy giá hiện tại từ bảng giá
        double displayPrice = 0;
        try {
            displayPrice = bangGiaDAO.getGiaHienTai(m.getMaMon());
        } catch (Exception ex) {
            /* giữ giá = 0 */ }
        // Xác định trạng thái: HET_MON thì hiển thị nhưng disable
        boolean isHetMon = Entity.MonAn.HET_MON.equals(m.getTrangThai());

        // Nếu hết món: phủ layer đỏ nhạt lên card
        if (isHetMon) {
            card.setBackground(new Color(254, 242, 242));
        }

        final double cardPrice = displayPrice;
        final boolean hasPrice = displayPrice > 0;

        JLabel lblPrice;
        if (hasPrice) {
            lblPrice = new JLabel(formatMoney(displayPrice) + " / " + m.getDonViTinh());
            lblPrice.setForeground(isHetMon ? Color.GRAY : new Color(220, 38, 38));
        } else {
            lblPrice = new JLabel("Chưa có giá");
            lblPrice.setForeground(new Color(150, 150, 150));
        }

        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPrice.setHorizontalAlignment(SwingConstants.CENTER);

        pnlInfo.add(lblName);
        pnlInfo.add(lblPrice);

        // Báo HET_MON
        if (isHetMon) {
            JLabel lblHet = new JLabel(" Hết món ");
            lblHet.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblHet.setForeground(Color.WHITE);
            lblHet.setOpaque(true);
            lblHet.setBackground(new Color(220, 38, 38));
            lblHet.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            pnlInfo.setLayout(new java.awt.GridLayout(3, 1));
            pnlInfo.add(lblHet);
        }

        card.add(pnlInfo, BorderLayout.CENTER);

        // 3. ACTIONS (Bottom) - Spinner + Add Button
        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        pnlAction.setBackground(Color.WHITE);

        JSpinner spnQty = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        spnQty.setPreferredSize(new Dimension(60, 30));

        JButton btnAdd = GUI.utils.UIStyle.buttonSm(GUI.utils.UIStyle.BtnType.SUCCESS, "THÊM");
        btnAdd.setPreferredSize(new Dimension(80, 30));

        if (isReadOnly || !hasPrice || isHetMon) {
            spnQty.setEnabled(false);
            btnAdd.setEnabled(false);
            if (isHetMon)
                btnAdd.setText("Hết Món");
        }

        btnAdd.addActionListener(e -> {
            int qty = (int) spnQty.getValue();
            addSingleItemToOrder(m, qty, cardPrice);
        });

        pnlAction.add(spnQty);
        pnlAction.add(btnAdd);

        card.add(pnlAction, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createOrderPanel() {
        JPanel pnl = new JPanel(new BorderLayout(10, 10));
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnl.setBackground(new Color(249, 250, 251));

        JLabel lblOrderTitle = new JLabel("Danh sách món đã gọi");
        lblOrderTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblOrderTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        pnl.add(lblOrderTitle, BorderLayout.NORTH);

        // Order Table
        // Headers: [Tên, -, SL, +, Đơn Giá, Thành Tiền, Xóa, MaMon]
        String[] headers = { "Tên Món", "-", "SL", "+", "Đơn Giá", "Thành Tiền", "Xóa", "MaMon" };
        modelOrder = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Chỉ cho phép sửa ô SL (cột 2) khi không phải ReadOnly
                return column == 2 && !isReadOnly;
            }
        };

        tblOrder = new JTable(modelOrder);
        tblOrder.setRowHeight(40);
        tblOrder.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblOrder.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Hide MaMon (index 7)
        tblOrder.getColumnModel().getColumn(7).setMinWidth(0);
        tblOrder.getColumnModel().getColumn(7).setMaxWidth(0);
        tblOrder.getColumnModel().getColumn(7).setWidth(0);

        // Column Widths
        tblOrder.getColumnModel().getColumn(1).setPreferredWidth(30); // -
        tblOrder.getColumnModel().getColumn(1).setMaxWidth(30);
        tblOrder.getColumnModel().getColumn(2).setPreferredWidth(55); // SL
        tblOrder.getColumnModel().getColumn(2).setMaxWidth(55);
        tblOrder.getColumnModel().getColumn(3).setPreferredWidth(30); // +
        tblOrder.getColumnModel().getColumn(3).setMaxWidth(30);
        tblOrder.getColumnModel().getColumn(6).setPreferredWidth(40); // Xóa
        tblOrder.getColumnModel().getColumn(6).setMaxWidth(40);

        // Center Alignment for SL
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblOrder.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        // Custom cell editor cho cột SL: cho phép nhập số trực tiếp
        if (!isReadOnly) {
            JTextField tfEditor = new JTextField();
            tfEditor.setHorizontalAlignment(JTextField.CENTER);
            tfEditor.setFont(new Font("Segoe UI", Font.BOLD, 14));
            tfEditor.setBorder(BorderFactory.createLineBorder(new Color(99, 102, 241), 2));
            DefaultCellEditor slEditor = new DefaultCellEditor(tfEditor) {
                @Override
                public boolean stopCellEditing() {
                    String val = tfEditor.getText().trim();
                    try {
                        int newSL = Integer.parseInt(val);
                        if (newSL <= 0) {
                            // Hỏi xóa
                            int editingRow = tblOrder.getEditingRow();
                            cancelCellEditing();
                            if (editingRow >= 0) {
                                int confirm = javax.swing.JOptionPane.showConfirmDialog(
                                        tblOrder, "Số lượng = 0. Xóa món này?", "Xác nhận",
                                        javax.swing.JOptionPane.YES_NO_OPTION);
                                if (confirm == javax.swing.JOptionPane.YES_OPTION)
                                    deleteSingleItem(editingRow);
                            }
                            return true;
                        }
                        int editingRow = tblOrder.getEditingRow();
                        boolean ok = super.stopCellEditing();
                        if (ok && editingRow >= 0)
                            changeQuantityTo(editingRow, newSL);
                        return ok;
                    } catch (NumberFormatException ex) {
                        tfEditor.setBackground(new Color(255, 220, 220));
                        return false; // không close editor nếu nhập chữ
                    }
                }
            };
            slEditor.setClickCountToStart(1); // 1 click là vào edit mode
            tblOrder.getColumnModel().getColumn(2).setCellEditor(slEditor);
        }

        // Button Renderer (-, +, X)
        DefaultTableCellRenderer btnRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(value.toString());
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));

                if (column == 6) { // Xóa
                    // lbl.setForeground(Color.RED);
                    ImageIcon icon = GUI.utils.IconHelper.loadIcon("view/icons/delete.png");
                    if (icon != null) {
                        // Resize if needed
                        Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                        lbl.setIcon(new ImageIcon(img));
                    } else {
                        lbl.setText("X"); // Fallback
                        lbl.setForeground(Color.RED);
                    }
                } else if (column == 1 || column == 3) { // - or +
                    lbl.setForeground(new Color(31, 41, 55));
                    lbl.setBackground(new Color(229, 231, 235));
                    lbl.setOpaque(true);
                    // Optional: add border/padding for button look
                    lbl.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                }
                return lbl;
            }
        };

        tblOrder.getColumnModel().getColumn(1).setCellRenderer(btnRenderer);
        tblOrder.getColumnModel().getColumn(3).setCellRenderer(btnRenderer);
        tblOrder.getColumnModel().getColumn(6).setCellRenderer(btnRenderer);

        // Click Logic
        tblOrder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblOrder.rowAtPoint(e.getPoint());
                int col = tblOrder.columnAtPoint(e.getPoint());
                if (row >= 0 && !isReadOnly) {
                    // 6: Delete
                    if (col == 6) {
                        deleteSingleItem(row);
                    }
                    // 1: Decrease
                    else if (col == 1) {
                        changeQuantity(row, -1);
                    }
                    // 3: Increase
                    else if (col == 3) {
                        changeQuantity(row, 1);
                    }
                }
            }
        });

        pnl.add(new JScrollPane(tblOrder), BorderLayout.CENTER);

        // Footer (Customer + Total + Actions)
        // Footer (Total + Actions)
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setOpaque(false);
        pnlFooter.setBorder(new EmptyBorder(10, 0, 0, 0));

        // 2. Total
        JPanel pnlTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlTotal.setOpaque(false);

        lblTongTien = new JLabel("Tổng tiền: 0 VNĐ");
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTongTien.setForeground(new Color(220, 38, 38));

        pnlTotal.add(lblTongTien);

        // 3. Actions
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlActions.setOpaque(false);

        JButton btnClearAll = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.DANGER, "Xóa Hết");
        btnClearAll.addActionListener(e -> clearAllOrder());

        JButton btnConfirm = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.SUCCESS, "Xác Nhận");
        btnConfirm.setPreferredSize(new Dimension(100, 35));

        // This button acts as "Close" or "Finish Ordering"
        btnConfirm.addActionListener(e -> dispose());

        pnlActions.add(btnClearAll);
        pnlActions.add(btnConfirm);

        pnlFooter.add(pnlTotal);
        pnlFooter.add(pnlActions);

        pnl.add(pnlFooter, BorderLayout.SOUTH);

        return pnl;
    }

    // --- LOGIC ---

    private void addSingleItemToOrder(MonAn m, int qty, double dynamicPrice) {
        if (qty <= 0)
            return;

        ensureInvoiceExists();
        if (maHD == -1) {
            JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy hóa đơn!");
            return;
        }

        // Sử dụng giá từ bảng giá (không fallback về MonAn.getDonGia)
        double priceToUse = dynamicPrice;

        // Check if item exists
        ChiTietHoaDon exists = cthdDAO.getChiTiet(maHD, m.getMaMon());
        if (exists != null) {
            // Update quantity AND PRICE
            int newSL = exists.getSoLuong() + qty;
            cthdDAO.capNhatSoLuongVaGia(maHD, m.getMaMon(), newSL, priceToUse);
        } else {
            // Insert with dynamic price
            cthdDAO.themMon(maHD, m.getMaMon(), qty, priceToUse);
        }
        loadOrderData(); // Refresh Right Panel
    }

    private void loadOrderData() {
        if (maHD == -1)
            return;

        modelOrder.setRowCount(0);
        ArrayList<String[]> details = hdDAO.getChiTietHoaDon(maHD);
        double total = 0;
        for (String[] row : details) {
            // Query DB returns: [Ten, SL, DonGia, ThanhTien, GhiChu, MaMon]
            String tenMon = row[0];
            int sl = Integer.parseInt(row[1]);
            double donGia = Double.parseDouble(row[2]);
            double thanhTien = Double.parseDouble(row[3]);
            String maMon = row[5];

            // New Columns: [Tên, -, SL, +, Giá, ThànhTiền, Xóa, MaMon]
            modelOrder.addRow(new Object[] {
                    tenMon,
                    "-",
                    sl,
                    "+",
                    formatMoney(donGia),
                    formatMoney(thanhTien),
                    "", // Icon renderer
                    maMon
            });
            total += thanhTien;
        }
        lblTongTien.setText("Tổng tiền: " + formatMoney(total) + " VNĐ");
    }

    private void changeQuantity(int row, int delta) {
        String maMon = (String) modelOrder.getValueAt(row, 7);
        int currentSL = (int) modelOrder.getValueAt(row, 2);
        int newSL = currentSL + delta;

        if (newSL <= 0) {
            int confirm = JOptionPane.showConfirmDialog(this, "Số lượng về 0. Xóa món này?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                cthdDAO.xoaMon(maHD, maMon);
                loadOrderData();
            }
        } else {
            cthdDAO.capNhatSoLuong(maHD, maMon, newSL);
            loadOrderData();
        }
    }

    // Đặt số lượng trực tiếp (dùng khi nhập số từ cell editor)
    private void changeQuantityTo(int row, int newSL) {
        String maMon = (String) modelOrder.getValueAt(row, 7);
        cthdDAO.capNhatSoLuong(maHD, maMon, newSL);
        loadOrderData();
    }

    private void deleteSingleItem(int row) {
        String maMon = (String) modelOrder.getValueAt(row, 7); // Index 7
        String tenMon = (String) modelOrder.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Xóa " + tenMon + "?", "Xóa Món", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            cthdDAO.xoaMon(maHD, maMon);
            loadOrderData();
        }
    }

    private void clearAllOrder() {
        if (modelOrder.getRowCount() == 0)
            return;

        int confirm = JOptionPane.showConfirmDialog(this, "Xóa TOÀN BỘ món đã gọi?", "Xóa Hết",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int rowCount = modelOrder.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                String maMon = (String) modelOrder.getValueAt(i, 7); // Index 7
                cthdDAO.xoaMon(maHD, maMon);
            }
            loadOrderData();
        }
    }

    private void ensureInvoiceExists() {
        if (maHD != -1)
            return;
    }

    private String formatMoney(double amount) {
        return java.text.NumberFormat.getIntegerInstance().format(amount);
    }
}
