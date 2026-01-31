package GUI;

import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.toedter.calendar.JDateChooser;

import DAO.DatBanDAO;
import DAO.KhachHangDAO;
import DAO.BanDAO;
import Entity.DatBan;
import Entity.Ban;

public class BookingFormDialog extends JDialog {

    private JTextField txtTenKH, txtSDT, txtGhiChu;
    private JDateChooser dateChooser;
    private JComboBox<String> cboGio, cboPhut, cboThoiLuong;
    private JComboBox<ComboItem> cboBan; // Store Table ID and Name
    private JSpinner spinKhach;
    private JLabel lblStatus;

    // DAO
    private DatBanDAO datBanDAO;
    private KhachHangDAO khachHangDAO;
    private BanDAO banDAO;

    // Callback
    private Runnable onSuccessCallback;

    public BookingFormDialog(Frame parent, String preSelectedTableId) {
        super(parent, "Tạo Đặt Bàn Mới", true);

        datBanDAO = new DatBanDAO();
        khachHangDAO = new KhachHangDAO();
        banDAO = new BanDAO();

        initUI();

        if (preSelectedTableId != null) {
            setSelectedTable(preSelectedTableId);
        }

        pack();
        setLocationRelativeTo(parent);
    }

    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    public void setPreFilledTime(Date date, int hour, int minute) {
        if (date != null) {
            dateChooser.setDate(date);
            cboGio.setSelectedItem(String.format("%02d", hour));
            cboPhut.setSelectedItem(String.format("%02d", minute));

            // Highlight that these were auto-filled
            dateChooser.setBackground(new Color(254, 242, 242)); // Light red/yellow tint?
            cboGio.setBackground(new Color(254, 242, 242));
        }
    }

    private void setSelectedTable(String tableId) {
        for (int i = 0; i < cboBan.getItemCount(); i++) {
            ComboItem item = cboBan.getItemAt(i);
            if (item.getValue().equals(tableId)) {
                cboBan.setSelectedIndex(i);
                break;
            }
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        JPanel pnlMain = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlMain.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnlMain.setBackground(Color.WHITE);

        // LEFT: Customer Info
        JPanel pnlLeft = new JPanel(new GridLayout(5, 1, 10, 10));
        pnlLeft.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Thông tin khách hàng",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14)));
        pnlLeft.setBackground(Color.WHITE);

        txtSDT = createStyledTextField();
        txtSDT.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                checkKhachHang();
            }
        });

        txtTenKH = createStyledTextField();
        spinKhach = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));

        addFormRow(pnlLeft, "Số điện thoại:", txtSDT);
        addFormRow(pnlLeft, "Tên khách hàng:", txtTenKH);
        addFormRow(pnlLeft, "Số lượng khách:", spinKhach);

        // RIGHT: Booking Info
        JPanel pnlRight = new JPanel(new GridLayout(5, 1, 10, 10));
        pnlRight.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Thông tin đặt bàn",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14)));
        pnlRight.setBackground(Color.WHITE);

        // Date & Time
        dateChooser = new JDateChooser(new Date());
        dateChooser.setDateFormatString("dd/MM/yyyy");

        JPanel pnlTime = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlTime.setOpaque(false);

        cboGio = new JComboBox<>();
        for (int i = 7; i <= 22; i++)
            cboGio.addItem(String.format("%02d", i));

        cboPhut = new JComboBox<>();
        cboPhut.addItem("00");
        cboPhut.addItem("15");
        cboPhut.addItem("30");
        cboPhut.addItem("45");

        cboGio.setSelectedItem("18"); // Default
        cboPhut.setSelectedItem("00");

        pnlTime.add(cboGio);
        pnlTime.add(new JLabel(":"));
        pnlTime.add(cboPhut);

        // Duration
        cboThoiLuong = new JComboBox<>(new String[] { "1 giờ", "2 giờ", "3 giờ", "4 giờ" });
        cboThoiLuong.setSelectedIndex(1); // Default 2h

        // Table Selection
        cboBan = new JComboBox<>();
        loadTables(); // Load all tables initially or filter?

        addFormRow(pnlRight, "Ngày đặt:", dateChooser);
        addFormRow(pnlRight, "Giờ bắt đầu:", pnlTime);
        addFormRow(pnlRight, "Thời lượng:", cboThoiLuong);
        addFormRow(pnlRight, "Chọn bàn:", cboBan);

        // Additional Note
        txtGhiChu = createStyledTextField();
        JPanel pnlNote = new JPanel(new BorderLayout(5, 5));
        pnlNote.setBackground(Color.WHITE);
        pnlNote.setBorder(new EmptyBorder(10, 20, 0, 20));
        pnlNote.add(new JLabel("Ghi chú:"), BorderLayout.NORTH);
        pnlNote.add(txtGhiChu, BorderLayout.CENTER);

        pnlMain.add(pnlLeft);
        pnlMain.add(pnlRight);

        add(pnlMain, BorderLayout.CENTER);
        add(pnlNote, BorderLayout.NORTH);

        // BOTTOM: Actions
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setBorder(new EmptyBorder(15, 20, 15, 20));
        pnlBottom.setBackground(new Color(245, 245, 245));

        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.RED);
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlButtons.setOpaque(false);

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("Xác nhận đặt bàn");
        btnSave.setBackground(new Color(34, 197, 94));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.addActionListener(e -> saveBooking());

        pnlButtons.add(btnCancel);
        pnlButtons.add(btnSave);

        pnlBottom.add(lblStatus, BorderLayout.WEST);
        pnlBottom.add(pnlButtons, BorderLayout.EAST);

        add(pnlBottom, BorderLayout.SOUTH);
    }

    private void checkKhachHang() {
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty())
            return;

        String ten = khachHangDAO.getTenKhachHang(sdt);
        if (ten != null && !ten.isEmpty()) {
            txtTenKH.setText(ten);
            txtTenKH.setEditable(false);
            lblStatus.setText("✓ Khách hàng cũ: " + ten);
            lblStatus.setForeground(new Color(34, 197, 94));
        } else {
            txtTenKH.setEditable(true);
            lblStatus.setText("ℹ Khách hàng mới - Vui lòng nhập tên");
            lblStatus.setForeground(Color.BLUE);
        }
    }

    private void loadTables() {
        // Load simple list of tables
        // Ideally should filter by availability based on selected time
        // For now, load all tables and let backend validation handle conflicts
        ArrayList<Ban> listBan = banDAO.getAllBan();
        cboBan.removeAllItems();
        cboBan.addItem(new ComboItem("Chọn bàn...", ""));

        for (Ban b : listBan) {
            cboBan.addItem(
                    new ComboItem(b.getTenBan() + " (" + b.getSoGhe() + " chỗ) - " + b.getTrangThai(), b.getMaBan()));
        }
    }

    private void addFormRow(JPanel panel, String label, Component comp) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        panel.add(p);
    }

    private JTextField createStyledTextField() {
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(0, 30));
        return txt;
    }

    private void saveBooking() {
        // 1. Validate
        String ten = txtTenKH.getText().trim();
        String sdt = txtSDT.getText().trim();
        ComboItem selectedTable = (ComboItem) cboBan.getSelectedItem();

        if (ten.isEmpty() || sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên và SĐT khách!");
            return;
        }

        if (selectedTable == null || selectedTable.getValue().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn!");
            return;
        }

        // 2. Prepare Data
        Date date = dateChooser.getDate();
        if (date == null)
            return;

        Calendar calStart = Calendar.getInstance();
        calStart.setTime(date);
        calStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(cboGio.getSelectedItem().toString()));
        calStart.set(Calendar.MINUTE, Integer.parseInt(cboPhut.getSelectedItem().toString()));
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        // Validation: Check if time is in the past
        if (calStart.getTime().before(new Date())) {
            JOptionPane.showMessageDialog(this, "Thời gian không hợp lệ");
            return;
        }

        int durationHours = cboThoiLuong.getSelectedIndex() + 1;
        Calendar calEnd = (Calendar) calStart.clone();
        calEnd.add(Calendar.HOUR_OF_DAY, durationHours);

        String maBan = selectedTable.getValue();

        // 3. Save Customer if New
        if (!khachHangDAO.checkTonTai(sdt)) {
            boolean newCust = khachHangDAO.themKhachMoi(sdt, ten);
            if (!newCust) {
                System.err.println("Failed to auto-create customer: " + sdt);
            }
        }

        // 4. Check Conflict
        int conflicts = datBanDAO.kiemTraXungDot(maBan, calStart.getTime(), calEnd.getTime());
        if (conflicts > 0) {
            JOptionPane.showMessageDialog(this, "Bàn này đã có người đặt trong khung giờ " +
                    String.format("%02d:%02d", calStart.get(Calendar.HOUR_OF_DAY), calStart.get(Calendar.MINUTE)) +
                    " - " +
                    String.format("%02d:%02d", calEnd.get(Calendar.HOUR_OF_DAY), calEnd.get(Calendar.MINUTE)));
            return;
        }

        // 4. Insert
        DatBan db = new DatBan(
                maBan, ten, sdt,
                calStart.getTime(), calEnd.getTime(),
                (int) spinKhach.getValue(),
                0, // Coc
                txtGhiChu.getText());

        if (datBanDAO.insertDatBan(db)) {
            JOptionPane.showMessageDialog(this, "Đặt bàn thành công!");
            if (onSuccessCallback != null)
                onSuccessCallback.run();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu đặt bàn!");
        }
    }

    // Helper Class for ComboBox
    class ComboItem {
        private String label;
        private String value;

        public ComboItem(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
