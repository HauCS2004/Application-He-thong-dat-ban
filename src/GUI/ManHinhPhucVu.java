package GUI;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;

import GUI.components.TableFloorPanel;
import GUI.components.TableCard;
import Entity.Ban;
import Entity.DatBan;
import Entity.HoaDon;
import DAO.BanDAO;
import DAO.HoaDonDAO;
import DAO.DatBanDAO;

public class ManHinhPhucVu extends JPanel implements TableCard.TableCardListener {

    private JTabbedPane tabFloors;
    private JPanel pnlSideBar;
    private JTabbedPane tabSide;

    // Side Panel Components
    private JPanel pnlTableDetail;
    private JLabel lblDetailName;
    private JLabel lblDetailStatus;
    private JPanel pnlActionButtons;
    private JLabel lblWarning; // For warnings

    private JPanel pnlTodayList;
    private JTable tblTodayBookings;
    private DefaultTableModel modelToday;
    private ArrayList<DatBan> todayBookingsList = new ArrayList<>(); // Cache for list check-in

    private BanDAO banDAO;
    private HoaDonDAO hoaDonDAO;
    private DatBanDAO datBanDAO;

    // Currently selected table
    private Ban selectedTable;

    public ManHinhPhucVu() {
        this.banDAO = new BanDAO();
        this.hoaDonDAO = new HoaDonDAO();
        this.datBanDAO = new DatBanDAO(); // Initialize

        initUI();

        // Auto-refresh
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshAllFloors();
                loadTodayBookings();
            }
        });
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(249, 250, 251));

        // --- HEADER ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel lblTitle = new JLabel("PHỤC VỤ KHÁCH HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(31, 41, 55));

        JLabel lblSubtitle = new JLabel("Chọn bàn để xem chi tiết hoặc bắt đầu phục vụ");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(107, 114, 128));

        JPanel pnlTitle = new JPanel(new GridLayout(2, 1));
        pnlTitle.setOpaque(false);
        pnlTitle.add(lblTitle);
        pnlTitle.add(lblSubtitle);

        JButton btnReload = new JButton("Làm mới trạng thái");
        btnReload.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnReload.addActionListener(e -> {
            refreshAllFloors();
            loadTodayBookings();
        });

        pnlHeader.add(pnlTitle, BorderLayout.WEST);
        pnlHeader.add(btnReload, BorderLayout.EAST);

        add(pnlHeader, BorderLayout.NORTH);

        // --- MAIN CONTENT ---
        // Floors
        tabFloors = new JTabbedPane();
        tabFloors.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabFloors.setBackground(Color.WHITE);

        addFloorTab("Tất cả", "ALL");
        addFloorTab("Tầng G", "KV01");
        addFloorTab("Tầng 1", "KV02");
        addFloorTab("VIP Room", "KV03");
        addFloorTab("Ngoài trời", "KV04");

        add(tabFloors, BorderLayout.CENTER);

        // Side Bar
        createSidePanel();
        add(pnlSideBar, BorderLayout.EAST);
    }

    private void createSidePanel() {
        pnlSideBar = new JPanel(new BorderLayout());
        pnlSideBar.setPreferredSize(new Dimension(360, 0));
        pnlSideBar.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlSideBar.setBackground(new Color(249, 250, 251)); // Neutral bg

        tabSide = new JTabbedPane();
        tabSide.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // --- TAB 1: CHI TIẾT BÀN ---
        pnlTableDetail = new JPanel(new BorderLayout());
        pnlTableDetail.setBackground(Color.WHITE);
        pnlTableDetail.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pnlInfo = new JPanel(new GridLayout(0, 1, 8, 8));
        pnlInfo.setOpaque(false);

        lblDetailName = new JLabel("Chưa chọn bàn");
        lblDetailName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblDetailName.setForeground(new Color(31, 41, 55));

        lblDetailStatus = new JLabel("Trạng thái: ---");
        lblDetailStatus.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        pnlInfo.add(lblDetailName);
        pnlInfo.add(lblDetailStatus);

        // Warning Label
        lblWarning = new JLabel("");
        lblWarning.setForeground(new Color(220, 38, 38)); // Red-600
        lblWarning.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlInfo.add(lblWarning);

        // Action Buttons Container - FIXED LAYOUT
        pnlActionButtons = new JPanel();
        pnlActionButtons.setLayout(new BoxLayout(pnlActionButtons, BoxLayout.Y_AXIS));
        pnlActionButtons.setOpaque(false);
        pnlActionButtons.setBorder(new EmptyBorder(20, 0, 0, 0));

        pnlTableDetail.add(pnlInfo, BorderLayout.NORTH);
        pnlTableDetail.add(pnlActionButtons, BorderLayout.CENTER);

        // --- TAB 2: LỊCH HÔM NAY ---
        pnlTodayList = new JPanel(new BorderLayout());
        pnlTodayList.setBackground(Color.WHITE);

        String[] headers = { "Giờ", "Bàn", "Khách", "SL" };
        modelToday = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblTodayBookings = new JTable(modelToday);
        tblTodayBookings.setRowHeight(30);
        tblTodayBookings.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblTodayBookings.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Resize cols
        tblTodayBookings.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblTodayBookings.getColumnModel().getColumn(1).setPreferredWidth(50);
        tblTodayBookings.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblTodayBookings.getColumnModel().getColumn(3).setPreferredWidth(40);

        pnlTodayList.add(new JScrollPane(tblTodayBookings), BorderLayout.CENTER);

        JButton btnCheckInList = new JButton("NHẬN BÀN (Check-in)");
        btnCheckInList.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCheckInList.setBackground(new Color(234, 88, 12)); // Orange
        btnCheckInList.setForeground(Color.WHITE);
        btnCheckInList.setPreferredSize(new Dimension(0, 45));
        btnCheckInList.setFocusPainted(false);
        btnCheckInList.addActionListener(e -> checkInFromList());

        pnlTodayList.add(btnCheckInList, BorderLayout.SOUTH);

        tabSide.addTab("Chi Tiết Bàn", pnlTableDetail);
        tabSide.addTab("Lịch Hôm Nay", pnlTodayList);

        pnlSideBar.add(tabSide, BorderLayout.CENTER);
    }

    private void addFloorTab(String title, String zoneId) {
        TableFloorPanel pnl = new TableFloorPanel(zoneId);
        pnl.setTableCardListener(this);
        tabFloors.addTab(title, pnl);
    }

    private void refreshAllFloors() {
        for (int i = 0; i < tabFloors.getTabCount(); i++) {
            Component c = tabFloors.getComponentAt(i);
            if (c instanceof TableFloorPanel) {
                ((TableFloorPanel) c).refreshTables();
            }
        }
        if (selectedTable != null) {
            // Update the selected table status if changed
            // We need to re-fetch to get latest status, but effectively we just refresh
            // display
            // A lightweight way is to iterate and find the updated Ban object,
            // but user interaction will likely click again.
            // For now, let's just re-render with existing object (status might be stale if
            // we don't fetch)
            // Ideally we should fetch new status:
            Ban updated = banDAO.getAllBan().stream().filter(b -> b.getMaBan().equals(selectedTable.getMaBan()))
                    .findFirst().orElse(selectedTable);
            this.selectedTable = updated;
            updateSidePanel(selectedTable);
        }
    }

    private void loadTodayBookings() {
        modelToday.setRowCount(0);
        todayBookingsList.clear();

        java.util.Calendar cal = java.util.Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        Date start = cal.getTime();

        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        Date end = cal.getTime();

        ArrayList<DatBan> list = datBanDAO.getDanhSachDatBan(start, end);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        for (DatBan db : list) {
            // FILTER: If table selected, only show bookings for that table
            if (selectedTable != null && !db.getMaBan().equals(selectedTable.getMaBan())) {
                continue;
            }

            // FILTER: Don't show Cancelled (Though DAO now filters it, good to double check
            // or if user refreshes)
            if ("Đã hủy".equalsIgnoreCase(db.getTrangThai())) {
                continue;
            }

            todayBookingsList.add(db);
            modelToday.addRow(new Object[] {
                    sdf.format(db.getThoiGianBatDau()),
                    db.getMaBan(),
                    db.getTenKhach(),
                    db.getSoLuongKhach()
            });
        }
    }

    // --- ACTIONS ---

    private void checkInFromList() {
        int row = tblTodayBookings.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn đặt bàn trong danh sách để nhận.");
            return;
        }

        DatBan db = todayBookingsList.get(row);

        // RE-FETCH Current Status to prevent Check-in on Cancelled Booking
        try {
            // Quick query to check status
            java.sql.Connection con = connectDB.ConnectDB.getConnection();
            java.sql.PreparedStatement ps = con.prepareStatement("SELECT TrangThai FROM DatBan WHERE MaDat = ?");
            ps.setInt(1, db.getMaDat());
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String currentStatus = rs.getString("TrangThai");
                if ("Đã hủy".equals(currentStatus)) {
                    JOptionPane.showMessageDialog(this, "Đơn đặt này đã bị HỦY! Không thể check-in.");
                    loadTodayBookings(); // Refresh list
                    return;
                }
                if ("Đã nhận bàn".equals(currentStatus)) {
                    JOptionPane.showMessageDialog(this, "Đơn đặt này đã được check-in rồi!");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Ban table = new BanDAO().getAllBan().stream()
                .filter(b -> b.getMaBan().equals(db.getMaBan()))
                .findFirst()
                .orElse(null);

        if (table == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin bàn " + db.getMaBan());
            return;
        }

        if ("Có Khách".equals(table.getTrangThai())) {
            JOptionPane.showMessageDialog(this, "Bàn này đang có khách! Không thể check-in.");
            return;
        }

        processCheckInObj(db, table);
    }

    @Override
    public void onTableCardClicked(Ban table) {
        this.selectedTable = table;
        updateSidePanel(table);
        tabSide.setSelectedIndex(0);
    }

    private void updateSidePanel(Ban table) {
        lblDetailName.setText("BÀN " + table.getTenBan());
        lblDetailStatus.setText(table.getTrangThai());

        if ("Trống".equals(table.getTrangThai()))
            lblDetailStatus.setForeground(new Color(22, 163, 74));
        else if ("Có Khách".equals(table.getTrangThai()))
            lblDetailStatus.setForeground(new Color(220, 38, 38));
        else
            lblDetailStatus.setForeground(new Color(202, 138, 4));

        pnlActionButtons.removeAll();
        lblWarning.setText("");

        String status = table.getTrangThai();
        JButton actionBtn = null;

        if ("Trống".equals(status)) {
            // Button: Phục vụ khách vãng lai
            actionBtn = createStyledButton("PHỤC VỤ KHÁCH VÃNG LAI", new Color(22, 163, 74));

            // WARNING
            DatBan upcoming = datBanDAO.getDatBanSapToi(table.getMaBan());
            if (upcoming != null) {
                long diff = upcoming.getThoiGianBatDau().getTime() - new Date().getTime();
                long diffMinutes = diff / (60 * 1000);

                if (diffMinutes > 0 && diffMinutes < 180) {
                    lblWarning.setText("<html>⚠️ CẢNH BÁO:<br>Có lịch đặt lúc "
                            + new SimpleDateFormat("HH:mm").format(upcoming.getThoiGianBatDau())
                            + "<br>Khách: " + upcoming.getTenKhach() + "</html>");

                    actionBtn.setBackground(new Color(234, 179, 8)); // Yellow/Orange
                    actionBtn.setText("<html>PHỤC VỤ (CÓ RỦI RO)</html>");
                }
            }
            actionBtn.addActionListener(e -> processWalkIn(table));
            pnlActionButtons.add(actionBtn);

        } else if ("Đã Đặt".equals(status)) {
            // Button: Check-in
            actionBtn = createStyledButton("NHẬN BÀN (Check-in)", new Color(234, 88, 12)); // Orange

            DatBan db = DatBanDAO.getDatBanGanNhat(table.getMaBan());
            if (db != null) {
                lblWarning.setText("<html>Khách đặt: " + db.getTenKhach() + "<br>SĐT: " + db.getSdt() +
                        "<br>Giờ: " + new SimpleDateFormat("HH:mm").format(db.getThoiGianBatDau()) + "</html>");
                lblWarning.setForeground(new Color(75, 85, 99));
            }
            actionBtn.addActionListener(e -> processCheckIn(table));
            pnlActionButtons.add(actionBtn);

        } else if ("Có Khách".equals(status) || "Đã nhận bàn".equals(status)) {
            // Button: Gọi Món
            actionBtn = createStyledButton("GỌI MÓN", new Color(37, 99, 235)); // Blue
            actionBtn.addActionListener(e -> processOpenOrder(table));

            // Button: Xem Hóa Đơn (Integrated)
            JButton btnPay = createStyledButton("XEM HÓA ĐƠN", new Color(75, 85, 99)); // Gray
            btnPay.addActionListener(e -> {
                // NAVIGATION LOGIC
                Window w = SwingUtilities.getWindowAncestor(this);
                if (w instanceof TrangChu) {
                    TrangChu tc = (TrangChu) w;
                    tc.showScreen("hoa_don");
                    if (tc.getPnlHoaDon() != null) {
                        try {
                            tc.getPnlHoaDon().selectActiveTable(table.getMaBan()); // Auto-select table
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy màn hình chính!");
                }
            });

            pnlActionButtons.add(actionBtn);
            pnlActionButtons.add(Box.createVerticalStrut(10)); // Gap
            pnlActionButtons.add(btnPay);
        }

        pnlActionButtons.revalidate();
        pnlActionButtons.repaint();
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); // FIXED HEIGHT SMALLER
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }

    private void processWalkIn(Ban table) {
        if (!lblWarning.getText().isEmpty() && lblWarning.getText().contains("CẢNH BÁO")) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Bàn này có lịch đặt sắp tới!\n" + lblWarning.getText().replaceAll("<[^>]*>", " ")
                            + "\nBạn có chắc chắn muốn nhận khách vãng lai?",
                    "Cảnh Báo Xung Đột",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION)
                return;
        }

        String input = JOptionPane.showInputDialog(this,
                "Nhập số lượng khách cho bàn " + table.getTenBan() + ":",
                "Mở Bàn Mới",
                JOptionPane.QUESTION_MESSAGE);

        if (input != null && !input.isEmpty()) {
            try {
                int soLuongKhach = Integer.parseInt(input);
                if (soLuongKhach <= 0) {
                    JOptionPane.showMessageDialog(this, "Số lượng khách phải lớn hơn 0!");
                    return;
                }

                String maNV = connectDB.SessionManager.getCurrentUser() != null
                        ? connectDB.SessionManager.getCurrentUser().getMaNV()
                        : null;
                if (maNV == null) {
                    JOptionPane.showMessageDialog(this,
                            "CẢNH BÁO: Không xác định được nhân viên! Hóa đơn sẽ không có tên thu ngân.",
                            "Lỗi Session", JOptionPane.WARNING_MESSAGE);
                }

                HoaDon hd = new HoaDon(table.getMaBan(), soLuongKhach, null, "Khách vãng lai", maNV);
                int maHD = hoaDonDAO.insert(hd);

                if (maHD != -1) {
                    banDAO.updateTrangThai(table.getMaBan(), "Có Khách");
                    refreshAllFloors();
                    openOrdering(maHD, table.getTenBan());
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi tạo hóa đơn!");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!");
            }
        }
    }

    private void processCheckIn(Ban table) {
        DatBan db = DatBanDAO.getDatBanGanNhat(table.getMaBan());
        if (db == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin đặt bàn khả dụng!");
            return;
        }
        processCheckInObj(db, table);
    }

    private void processCheckInObj(DatBan db, Ban table) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Xác nhận nhận bàn cho khách: " + db.getTenKhach() + "\nSĐT: " + db.getSdt() + "\nSố khách: "
                        + db.getSoLuongKhach(),
                "Check-in Đặt Bàn",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            if (datBanDAO.capNhatTrangThai(db.getMaDat(), "Đã nhận bàn")) {
                banDAO.updateTrangThai(table.getMaBan(), "Có Khách");

                // Pass Name (as Note) and Phone to Invoice
                String maNV = connectDB.SessionManager.getCurrentUser() != null
                        ? connectDB.SessionManager.getCurrentUser().getMaNV()
                        : null;
                HoaDon hd = new HoaDon(table.getMaBan(), db.getSoLuongKhach(), db.getSdt(), db.getTenKhach(), maNV);
                int maHD = hoaDonDAO.insert(hd);

                refreshAllFloors();
                loadTodayBookings();

                if (maHD != -1)
                    openOrdering(maHD, table.getTenBan());
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi cập nhật trạng thái đặt bàn!");
            }
        }
    }

    private void processOpenOrder(Ban table) {
        int maHD = hoaDonDAO.getMaHDByBan(table.getMaBan());
        if (maHD != -1) {
            openOrdering(maHD, table.getTenBan());
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn!");
        }
    }

    private void openOrdering(int maHD, String tenBan) {
        ManHinhGoiMon screen = new ManHinhGoiMon(maHD, "Phục vụ: " + tenBan, false);
        screen.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                refreshAllFloors();
            }
        });
        screen.setVisible(true);
    }
}
