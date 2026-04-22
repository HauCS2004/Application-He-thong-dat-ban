package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import DAO.DatBanDAO;

import Entity.DatBan;

import com.toedter.calendar.JDateChooser;

/**
 * ManHinhDatBan - Module riêng cho quản lý đặt bàn
 * Tách khỏi QuanLyBan để tập trung vào booking workflow
 */
public class ManHinhDatBan extends JPanel {

    private JTabbedPane tabbedPane;

    // Tab 1: Lịch đặt
    private JDateChooser dateChooser;
    private JTable tblBookings;
    private DefaultTableModel modelBookings;
    private JButton btnViewDetail;

    // Tab 2: Tạo booking mới
    private JTextField txtTenKhach, txtSDT, txtSoKhach, txtGhiChu;
    private JDateChooser dateChooserNew;
    private JSpinner spinGioBD, spinPhutBD;
    private JComboBox<String> cboKhuVuc, cboBan;
    private JButton btnGoiYBan, btnXacNhan;

    // Tab 3: Tìm kiếm
    private JTextField txtSearchSDT;
    private JTable tblSearchResults;
    private DefaultTableModel modelSearch;
    private JButton btnSearch;

    private DatBanDAO datBanDAO;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

    public ManHinhDatBan() {
        datBanDAO = new DatBanDAO();

        initGUI();
        loadBookingToday();
    }

    private void initGUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("📋 QUẢN LÝ ĐẶT BÀN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(0, 123, 255));

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        add(pnlHeader, BorderLayout.NORTH);

        // Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        tabbedPane.addTab("📅 Lịch Đặt", createTabCalendar());
        tabbedPane.addTab("➕ Tạo Mới", createTabNew());
        tabbedPane.addTab("🔍 Tìm Kiếm", createTabSearch());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ===== TAB 1: LỊCH ĐẶT =====
    private JPanel createTabCalendar() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top: Date picker
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlTop.setBackground(Color.WHITE);

        JLabel lblDate = new JLabel("Chọn ngày:");
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        dateChooser = new JDateChooser();
        dateChooser.setDate(new Date());
        dateChooser.setPreferredSize(new Dimension(150, 35));
        dateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateChooser.addPropertyChangeListener("date", e -> loadBookingsByDate());

        btnViewDetail = new JButton(" Xem chi tiết");
        btnViewDetail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnViewDetail.addActionListener(e -> viewBookingDetail());

        pnlTop.add(lblDate);
        pnlTop.add(dateChooser);
        pnlTop.add(btnViewDetail);

        panel.add(pnlTop, BorderLayout.NORTH);

        // Center: Table
        String[] columns = { "Mã", "Bàn", "Khách hàng", "SĐT", "Giờ bắt đầu", "Giờ kết thúc", "Số khách",
                "Trạng thái" };
        modelBookings = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblBookings = new JTable(modelBookings);
        tblBookings.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblBookings.setRowHeight(30);
        tblBookings.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblBookings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(tblBookings);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ===== TAB 2: TẠO MỚI =====
    private JPanel createTabNew() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 5, 8, 5);

        int row = 0;

        // Tên khách
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Tên khách hàng:"), c);
        c.gridx = 1;
        txtTenKhach = new JTextField(20);
        txtTenKhach.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(txtTenKhach, c);

        // SĐT
        row++;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Số điện thoại:"), c);
        c.gridx = 1;
        txtSDT = new JTextField(20);
        txtSDT.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(txtSDT, c);

        // Ngày đặt
        row++;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Ngày đặt:"), c);
        c.gridx = 1;
        dateChooserNew = new JDateChooser();
        dateChooserNew.setDate(new Date());
        dateChooserNew.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateChooserNew.setPreferredSize(new Dimension(0, 30));
        panel.add(dateChooserNew, c);

        // Giờ bắt đầu
        row++;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Giờ bắt đầu:"), c);
        c.gridx = 1;

        JPanel pnlTime = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlTime.setBackground(Color.WHITE);

        SpinnerNumberModel hourModel = new SpinnerNumberModel(18, 0, 23, 1);
        spinGioBD = new JSpinner(hourModel);
        spinGioBD.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ((JSpinner.DefaultEditor) spinGioBD.getEditor()).getTextField().setColumns(2);

        SpinnerNumberModel minModel = new SpinnerNumberModel(0, 0, 59, 15);
        spinPhutBD = new JSpinner(minModel);
        spinPhutBD.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ((JSpinner.DefaultEditor) spinPhutBD.getEditor()).getTextField().setColumns(2);

        pnlTime.add(spinGioBD);
        pnlTime.add(new JLabel(":"));
        pnlTime.add(spinPhutBD);
        panel.add(pnlTime, c);

        // Số khách
        row++;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Số khách:"), c);
        c.gridx = 1;
        txtSoKhach = new JTextField(20);
        txtSoKhach.setText("2");
        txtSoKhach.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(txtSoKhach, c);

        // Khu vực
        row++;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Khu vực:"), c);
        c.gridx = 1;
        cboKhuVuc = new JComboBox<>(new String[] { "Tất cả", "VIP", "Thường", "Ngoài trời" });
        cboKhuVuc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(cboKhuVuc, c);

        // Gợi ý bàn
        row++;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Chọn bàn:"), c);
        c.gridx = 1;

        JPanel pnlBan = new JPanel(new BorderLayout(5, 0));
        pnlBan.setBackground(Color.WHITE);

        cboBan = new JComboBox<>();
        cboBan.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        btnGoiYBan = new JButton("💡 Gợi ý");
        btnGoiYBan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnGoiYBan.addActionListener(e -> goiYBan());

        pnlBan.add(cboBan, BorderLayout.CENTER);
        pnlBan.add(btnGoiYBan, BorderLayout.EAST);
        panel.add(pnlBan, c);

        // Ghi chú
        row++;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel("Ghi chú:"), c);
        c.gridx = 1;
        txtGhiChu = new JTextField(20);
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(txtGhiChu, c);

        // Button
        row++;
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        c.insets = new Insets(20, 5, 8, 5);

        btnXacNhan = new JButton(" XÁC NHẬN ĐẶT BÀN");
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnXacNhan.setBackground(new Color(46, 204, 113));
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setPreferredSize(new Dimension(0, 45));
        btnXacNhan.setFocusPainted(false);
        btnXacNhan.addActionListener(e -> handleCreateBooking());

        panel.add(btnXacNhan, c);

        return panel;
    }

    // ===== TAB 3: TÌM KIẾM =====
    private JPanel createTabSearch() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top: Search
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlTop.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("Số điện thoại:");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        txtSearchSDT = new JTextField(15);
        txtSearchSDT.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearchSDT.setPreferredSize(new Dimension(0, 35));

        btnSearch = new JButton("🔍 Tìm kiếm");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.setBackground(new Color(0, 123, 255));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.addActionListener(e -> searchBooking());

        pnlTop.add(lblSearch);
        pnlTop.add(txtSearchSDT);
        pnlTop.add(btnSearch);

        panel.add(pnlTop, BorderLayout.NORTH);

        // Center: Results
        String[] columns = { "Mã", "Bàn", "Tên khách", "Ngày đặt", "Giờ", "Trạng thái" };
        modelSearch = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblSearchResults = new JTable(modelSearch);
        tblSearchResults.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblSearchResults.setRowHeight(30);
        tblSearchResults.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scroll = new JScrollPane(tblSearchResults);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // ===== LOGIC =====

    private void loadBookingToday() {
        dateChooser.setDate(new Date());
        loadBookingsByDate();
    }

    private void loadBookingsByDate() {
        modelBookings.setRowCount(0);

        Date selectedDate = dateChooser.getDate();
        if (selectedDate == null)
            return;

        // Lấy tất cả bookings từ ngày đầu tháng đến cuối tháng
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        Date startDate = cal.getTime();

        cal.add(Calendar.MONTH, 1);
        Date endDate = cal.getTime();

        ArrayList<DatBan> list = datBanDAO.getDanhSachDatBan(startDate, endDate);

        for (DatBan db : list) {
            // Filter by date
            if (sdfDate.format(db.getThoiGianBatDau()).equals(sdfDate.format(selectedDate))) {
                modelBookings.addRow(new Object[] {
                        db.getMaDat(),
                        db.getMaBan(),
                        db.getTenKhach(),
                        db.getSdt(),
                        sdf.format(db.getThoiGianBatDau()),
                        sdf.format(db.getThoiGianKetThuc()),
                        db.getSoLuongKhach(),
                        db.getTrangThai()
                });
            }
        }
    }

    private void viewBookingDetail() {
        int row = tblBookings.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn booking!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DatBan db = DatBanDAO.getDatBanGanNhat(tblBookings.getValueAt(row, 3).toString());

        if (db != null) {
            String info = String.format(
                    "Mã đặt: %d\nBàn: %s\nKhách hàng: %s\nSĐT: %s\nThời gian: %s - %s\nSố khách: %d\nTrạng thái: %s\nGhi chú: %s",
                    db.getMaDat(), db.getMaBan(), db.getTenKhach(), db.getSdt(),
                    sdf.format(db.getThoiGianBatDau()), sdf.format(db.getThoiGianKetThuc()),
                    db.getSoLuongKhach(), db.getTrangThai(), db.getGhiChu());

            JOptionPane.showMessageDialog(this, info, "Chi tiết đặt bàn", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void goiYBan() {
        try {
            int soKhach = Integer.parseInt(txtSoKhach.getText());
            Date ngay = dateChooserNew.getDate();
            int gio = (int) spinGioBD.getValue();
            int phut = (int) spinPhutBD.getValue();

            Calendar cal = Calendar.getInstance();
            cal.setTime(ngay);
            cal.set(Calendar.HOUR_OF_DAY, gio);
            cal.set(Calendar.MINUTE, phut);
            Date thoiGianBD = cal.getTime();

            cal.add(Calendar.HOUR, 2);
            Date thoiGianKT = cal.getTime();

            String maKV = cboKhuVuc.getSelectedItem().toString();
            if (maKV.equals("Tất cả"))
                maKV = null;

            ArrayList<String> dsBan = datBanDAO.goiYBan(soKhach, thoiGianBD, thoiGianKT, maKV);

            cboBan.removeAllItems();
            if (dsBan.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có bàn trống phù hợp!", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                for (String ban : dsBan) {
                    cboBan.addItem(ban);
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleCreateBooking() {
        try {
            // Validation
            if (txtTenKhach.getText().trim().isEmpty() || txtSDT.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (cboBan.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn hoặc gợi ý bàn!", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create booking
            String maBan = cboBan.getSelectedItem().toString();
            Date ngay = dateChooserNew.getDate();
            int gio = (int) spinGioBD.getValue();
            int phut = (int) spinPhutBD.getValue();

            Calendar cal = Calendar.getInstance();
            cal.setTime(ngay);
            cal.set(Calendar.HOUR_OF_DAY, gio);
            cal.set(Calendar.MINUTE, phut);
            Date thoiGianBD = cal.getTime();

            cal.add(Calendar.HOUR, 2);
            Date thoiGianKT = cal.getTime();

            DatBan db = new DatBan(
                    maBan,
                    txtTenKhach.getText().trim(),
                    txtSDT.getText().trim(),
                    thoiGianBD,
                    thoiGianKT,
                    Integer.parseInt(txtSoKhach.getText()),
                    0, // Tiền cọc
                    txtGhiChu.getText().trim());

            if (datBanDAO.insertDatBan(db)) {
                JOptionPane.showMessageDialog(this, " Đặt bàn thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadBookingsByDate(); // Refresh
                tabbedPane.setSelectedIndex(0); // Switch to calendar tab
            } else {
                JOptionPane.showMessageDialog(this, " Đặt bàn thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void searchBooking() {
        modelSearch.setRowCount(0);
        String sdt = txtSearchSDT.getText().trim();

        if (sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập SĐT!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get all bookings (last 30 days)
        Calendar cal = Calendar.getInstance();
        Date endDate = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date startDate = cal.getTime();

        ArrayList<DatBan> list = datBanDAO.getDanhSachDatBan(startDate, endDate);
        int count = 0;

        for (DatBan db : list) {
            if (db.getSdt().contains(sdt)) {
                modelSearch.addRow(new Object[] {
                        db.getMaDat(),
                        db.getMaBan(),
                        db.getTenKhach(),
                        sdfDate.format(db.getThoiGianBatDau()),
                        new SimpleDateFormat("HH:mm").format(db.getThoiGianBatDau()),
                        db.getTrangThai()
                });
                count++;
            }
        }

        if (count == 0) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy booking nào!", "Kết quả",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearForm() {
        txtTenKhach.setText("");
        txtSDT.setText("");
        txtSoKhach.setText("2");
        txtGhiChu.setText("");
        dateChooserNew.setDate(new Date());
        spinGioBD.setValue(18);
        spinPhutBD.setValue(0);
        cboBan.removeAllItems();
    }
}
