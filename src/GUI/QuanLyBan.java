package GUI;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import DAO.BanDAO;
import DAO.DatBanDAO;
import DAO.HoaDonDAO;
import Entity.Ban;
import Entity.DatBan;
import Entity.HoaDon;
import DAO.KhachHangDAO;

public class QuanLyBan extends JPanel {
    // --- COMPONENT ---
    private JTabbedPane tabKhuVuc;
    private JLabel lblTenBan, lblTrangThai;
    private JButton btnDatBan, btnGoiMon, btnThanhToan, btnChuyenBan, btnGhepBan, btnGanKhach;
    private JPanel pnlRight;
    // Khai báo biến
    private JTable tblOrder;
    private javax.swing.table.DefaultTableModel modelOrder;
    private JLabel lblTongTienTam; // Để hiện tổng tiền bên dưới
    // --- DATA ---
    private BanDAO banDAO = new BanDAO();
    private HoaDonDAO hdDAO = new HoaDonDAO();
    private Ban banDangChon = null;
    private Timer timerUpdate;

    public QuanLyBan() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        // 1. LEFT: SƠ ĐỒ BÀN
        tabKhuVuc = new JTabbedPane();
        tabKhuVuc.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loadTabs();
        add(tabKhuVuc, BorderLayout.CENTER);

        // 2. RIGHT: CONTROL PANEL
        setupRightPanel();
        add(pnlRight, BorderLayout.EAST);

        // 3. Xử lý sự kiện
        initEvents();

        // 4. Timer cập nhật (60s)
        timerUpdate = new Timer(60000, e -> {
            banDAO.capNhatTrangThaiDatBan();
            if (banDangChon == null)
                reloadTableData();
        });
        timerUpdate.start();
    }

    private void setupRightPanel() {
        pnlRight = new JPanel(new BorderLayout());
        pnlRight.setPreferredSize(new Dimension(350, 0));
        pnlRight.setBackground(new Color(245, 245, 245));
        pnlRight.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Info
        JPanel pnlInfo = new JPanel(new GridLayout(4, 1, 10, 10));
        pnlInfo.setBorder(new TitledBorder("THÔNG TIN BÀN"));
        lblTenBan = new JLabel("Chưa chọn", SwingConstants.CENTER);
        lblTenBan.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTenBan.setForeground(Color.BLUE);
        lblTrangThai = new JLabel("...", SwingConstants.CENTER);
        lblTrangThai.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        pnlInfo.add(lblTenBan);
        pnlInfo.add(lblTrangThai);
        pnlRight.add(pnlInfo, BorderLayout.NORTH);
        // B. DANH SÁCH MÓN ĂN (SỬA LẠI PHẦN NÀY)
        JPanel pnlList = new JPanel(new BorderLayout());
        pnlList.setBackground(Color.WHITE);
        pnlList.setBorder(BorderFactory.createTitledBorder("DANH SÁCH MÓN ĐANG GỌI"));

        // -> Tạo Bảng
        String[] headers = { "Tên món", "SL", "Đ.Giá", "T.Tiền" };
        modelOrder = new javax.swing.table.DefaultTableModel(headers, 0);
        tblOrder = new JTable(modelOrder);
        tblOrder.setRowHeight(25);
        tblOrder.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Chỉnh độ rộng cột cho đẹp
        tblOrder.getColumnModel().getColumn(0).setPreferredWidth(130); // Tên món dài
        tblOrder.getColumnModel().getColumn(1).setPreferredWidth(30); // SL ngắn

        JScrollPane sc = new JScrollPane(tblOrder);
        pnlList.add(sc, BorderLayout.CENTER);

        // -> Tạo dòng Tổng tiền tạm tính
        lblTongTienTam = new JLabel("Tổng: 0 VNĐ", SwingConstants.RIGHT);
        lblTongTienTam.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTongTienTam.setForeground(Color.RED);
        lblTongTienTam.setBorder(new EmptyBorder(10, 0, 10, 10));
        pnlList.add(lblTongTienTam, BorderLayout.SOUTH);

        pnlRight.add(pnlList, BorderLayout.CENTER);
        // Buttons
        JPanel pnlAction = new JPanel(new GridLayout(3, 2, 10, 10));
        pnlAction.setPreferredSize(new Dimension(0, 180));

        btnDatBan = createButton("ĐẶT BÀN", new Color(241, 196, 15));
        btnGoiMon = createButton("GỌI MÓN", new Color(46, 204, 113));
        btnThanhToan = createButton("THANH TOÁN", new Color(231, 76, 60));
        btnChuyenBan = createButton("CHUYỂN BÀN", new Color(52, 152, 219));
        btnGhepBan = createButton("GHÉP BÀN", new Color(155, 89, 182)); // Tím
        btnGanKhach = createButton("GÁN KHÁCH", new Color(0, 150, 136)); // Teal

        pnlAction.add(btnGoiMon);
        pnlAction.add(btnThanhToan);
        pnlAction.add(btnChuyenBan);
        pnlAction.add(btnGhepBan);
        pnlAction.add(btnDatBan);
        pnlAction.add(btnGanKhach);

        pnlRight.add(pnlAction, BorderLayout.SOUTH);

    }

    private void initEvents() {
        // GỌI MÓN (Mở bàn / Check-in / Thêm món)
        btnGoiMon.addActionListener(e -> {
            if (banDangChon == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn!");
                return;
            }

            String trangThai = banDangChon.getTrangThai();

            // TRƯỜNG HỢP 1: BÀN TRỐNG -> KHÁCH VÃNG LAI
            if (trangThai.equals("Trống")) {
                moBanChoKhachVangLai();
            }

            // TRƯỜNG HỢP 2: BÀN ĐÃ ĐẶT -> CHECK-IN (NHẬN BÀN)
            else if (trangThai.equals("Đã Đặt")) {
                checkInKhachDat();
            }

            // TRƯỜNG HỢP 3: ĐANG CÓ KHÁCH -> GỌI THÊM MÓN
            else if (trangThai.equals("Có Khách")) {
                int maHD = hdDAO.getMaHDByBan(banDangChon.getMaBan());
                if (maHD != -1) {
                    new ManHinhGoiMon(maHD, banDangChon.getTenBan(), false).setVisible(true);
                }
            }

            // TRƯỜNG HỢP 4: BÀN ĐANG GỘP -> CHẶN
            else if (trangThai.equals("Đang Gộp")) {
                JOptionPane.showMessageDialog(this,
                        "Bàn này đang gộp vào bàn " + banDangChon.getMaBanGop() + ". Hãy thao tác bên bàn chính!");
            }
        });

        // ĐẶT BÀN
        btnDatBan.addActionListener(e -> {
            if (banDangChon == null)
                return;
            if (!banDangChon.getTrangThai().equals("Trống")) {
                JOptionPane.showMessageDialog(this, "Chỉ đặt được bàn Trống!");
                return;
            }
            showDialogDatBan();
        });

        // GÁN KHÁCH
        btnGanKhach.addActionListener(e -> {
            if (banDangChon == null || !banDangChon.getTrangThai().equals("Có Khách")) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn đang phục vụ (Có Khách)!");
                return;
            }
            assignCustomer(banDangChon.getMaBan());
        });

        // CHUYỂN BÀN
        btnChuyenBan.addActionListener(e -> {
            if (banDangChon == null || !banDangChon.getTrangThai().equals("Có Khách"))
                return;

            ArrayList<Ban> listTrong = banDAO.getBanTheoTrangThai("Trống");
            if (listTrong.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Hết bàn trống!");
                return;
            }

            Ban banDich = (Ban) JOptionPane.showInputDialog(this, "Chuyển sang bàn nào?", "Chuyển Bàn",
                    JOptionPane.QUESTION_MESSAGE, null, listTrong.toArray(), listTrong.get(0));

            if (banDich != null) {
                if (banDAO.chuyenBan(banDangChon.getMaBan(), banDich.getMaBan())) {
                    JOptionPane.showMessageDialog(this, "Chuyển thành công!");
                    reloadTableData();
                }
            }
        });

        // GHÉP BÀN (Nhiều -> 1)
        btnGhepBan.addActionListener(e -> {
            if (banDangChon == null || !banDangChon.getTrangThai().equals("Có Khách")) {
                JOptionPane.showMessageDialog(this, "Chọn bàn ĐÍCH (Có khách) để gộp vào!");
                return;
            }

            ArrayList<Ban> listCoKhach = banDAO.getBanCoKhach();
            listCoKhach.removeIf(b -> b.getMaBan().equals(banDangChon.getMaBan())); // Bỏ bàn hiện tại

            if (listCoKhach.isEmpty())
                return;

            JList<Ban> listJList = new JList<>(listCoKhach.toArray(new Ban[0]));
            listJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            int res = JOptionPane.showConfirmDialog(this, new JScrollPane(listJList),
                    "Chọn các bàn nguồn (Giữ Ctrl để chọn nhiều):", JOptionPane.OK_CANCEL_OPTION);

            if (res == JOptionPane.OK_OPTION) {
                List<Ban> selectedBans = listJList.getSelectedValuesList();
                if (selectedBans.isEmpty())
                    return;

                ArrayList<String> listMaNguon = new ArrayList<>();
                int tongKhach = hdDAO.getSoLuongKhach(banDangChon.getMaBan());

                for (Ban b : selectedBans) {
                    tongKhach += hdDAO.getSoLuongKhach(b.getMaBan());
                    listMaNguon.add(b.getMaBan());
                }

                // Check Sức Chứa khi gộp
                int maxGhep = getGioiHanGhep(banDangChon.getSoGhe());
                if (tongKhach > maxGhep) {
                    JOptionPane.showMessageDialog(this, "Tổng khách " + tongKhach + " vượt quá giới hạn " + maxGhep);
                    return;
                }

                if (banDAO.ghepNhieuBan(banDangChon.getMaBan(), listMaNguon)) {
                    JOptionPane.showMessageDialog(this, "Gộp bàn thành công!");
                    reloadTableData();
                }
            }
        });

        // THANH TOÁN
        btnThanhToan.addActionListener(e -> {
            if (banDangChon != null && banDangChon.getTrangThai().equals("Có Khách")) {

                // 1. Lấy thông tin hóa đơn hiện tại
                int maHD = hdDAO.getMaHDByBan(banDangChon.getMaBan());
                HoaDon hdInfo = hdDAO.getThongTinHoaDon(maHD); // Lấy SĐT và Tên đã lưu lúc check-in

                String sdt = (hdInfo != null) ? hdInfo.getSdtKhach() : "";
                String ten = (hdInfo != null) ? hdInfo.getGhiChu() : "";

                // 2. Nếu hóa đơn chưa có SĐT (Khách vãng lai), hỏi xem có muốn nhập để tích
                // điểm không?
                if (sdt == null || sdt.isEmpty()) {
                    String nhapSDT = JOptionPane.showInputDialog(this,
                            "Khách vãng lai. Nhập SĐT để tích điểm (Để trống nếu không cần):");
                    if (nhapSDT != null && !nhapSDT.trim().isEmpty()) {
                        sdt = nhapSDT;
                        // Nếu là SĐT mới -> Hỏi tên để lưu
                        KhachHangDAO khDAO = new KhachHangDAO();
                        if (!khDAO.checkTonTai(sdt)) {
                            String nhapTen = JOptionPane.showInputDialog(this, "Khách hàng mới! Vui lòng nhập tên:");
                            ten = (nhapTen != null) ? nhapTen : "Khách Mới";
                            khDAO.themKhachMoi(sdt, ten); // [QUAN TRỌNG] Insert vào bảng KhachHang ngay
                        }
                        // Cập nhật ngược lại SĐT vào Hóa Đơn để lưu vết
                        hdDAO.updateSdtKhach(maHD, sdt);
                    }
                }

                // 3. Tính tiền & Giảm giá VIP
                double tongTienHang = hdDAO.getTongTienTamTinh(maHD);
                int phanTramGiam = new DAO.KhachHangDAO().getPhanTramGiam(sdt);
                double tienGiam = tongTienHang * phanTramGiam / 100;
                double tongThanhToan = tongTienHang - tienGiam;

                // 4. Hiển thị xác nhận
                java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
                String msg = "<html><h3>THANH TOÁN: " + banDangChon.getTenBan() + "</h3>" +
                        "Khách: " + (ten == null ? "Vãng lai" : ten) + "<br>" +
                        "Tổng tiền: " + df.format(tongTienHang) + "<br>" +
                        "Giảm giá VIP (" + phanTramGiam + "%): -" + df.format(tienGiam) + "<br>" +
                        "<h2 style='color:red'>THỰC THU: " + df.format(tongThanhToan) + " VNĐ</h2></html>";

                if (JOptionPane.showConfirmDialog(this, msg, "Xác nhận",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

                    // 5. Thanh toán & Tích điểm
                    hdDAO.thanhToan(maHD, tongThanhToan);

                    if (sdt != null && !sdt.isEmpty()) {
                        System.out.println("DEBUG: Goi ham tichDiem cho SDT: " + sdt + ", Tong tien: " + tongThanhToan);
                        new DAO.KhachHangDAO().tichDiem(sdt, tongThanhToan); // [QUAN TRỌNG] Cộng điểm
                    }

                    // 6. Dọn bàn
                    banDAO.updateTrangThai(banDangChon.getMaBan(), "Trống");
                    banDAO.huyGopBan(banDangChon.getMaBan());
                    reloadTableData();
                    JOptionPane.showMessageDialog(this,
                            "Đã thanh toán! " + (sdt != null ? "Đã cộng điểm cho khách." : ""));
                }
            }
        });
    }

    // --- SUPPORT METHODS ---
    private void loadTabs() {
        tabKhuVuc.removeAll();
        tabKhuVuc.addTab("Tầng 1", createPanelBan("KV01"));
        tabKhuVuc.addTab("Tầng 2", createPanelBan("KV02"));
        tabKhuVuc.addTab("Khu VIP", createPanelBan("KV03"));
    }

    private JPanel createPanelBan(String maKV) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        pnl.setBackground(Color.WHITE);

        ArrayList<Ban> listBan = banDAO.getBanTheoKhuVuc(maKV);
        for (Ban b : listBan) {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(140, 140));

            Color bg;
            String status = b.getTrangThai();
            String subInfo = b.getSoGhe() + " chỗ";

            // LOGIC MÀU SẮC MỚI
            if (status.equals("Có Khách")) {
                bg = new Color(231, 76, 60); // Đỏ
            } else if (status.equals("Đã Đặt")) {
                bg = new Color(241, 196, 15); // Vàng
            } else if (status.equals("Đang Gộp")) {
                bg = new Color(108, 92, 231); // Tím
                subInfo = "Gộp -> " + b.getMaBanGop();
            } else {
                bg = new Color(46, 204, 113); // Xanh
            }

            btn.setText("<html><center><h3>" + b.getTenBan() + "</h3>" +
                    "<p style='font-size:10px'>🪑 " + subInfo + "</p><br><b>" + status + "</b></center></html>");
            btn.setBackground(bg);
            btn.setForeground(Color.WHITE);

            btn.addActionListener(e -> {
                this.banDangChon = b;
                updateRightPanel();
                loadDSMonChoBan();
            });
            pnl.add(btn);
        }
        return pnl;
    }

    private void updateRightPanel() {
        if (banDangChon != null) {
            lblTenBan.setText(banDangChon.getTenBan());
            lblTrangThai.setText(banDangChon.getTrangThai());

            Color c = Color.BLACK;
            if (banDangChon.getTrangThai().equals("Có Khách"))
                c = Color.RED;
            else if (banDangChon.getTrangThai().equals("Đang Gộp"))
                c = new Color(108, 92, 231);
            else if (banDangChon.getTrangThai().equals("Trống"))
                c = new Color(0, 150, 0);
            lblTrangThai.setForeground(c);
        }
    }

    private void reloadTableData() {
        int index = tabKhuVuc.getSelectedIndex();
        loadTabs();
        tabKhuVuc.setSelectedIndex(index);
        updateRightPanel();
        loadDSMonChoBan();
    }

    private boolean checkSucChua(Ban b, int khach) {
        if (khach > b.getSoGhe()) {
            JOptionPane.showMessageDialog(this, "Quá tải! Bàn " + b.getSoGhe() + " chỗ không đủ.", "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private int getGioiHanGhep(int soGhe) {
        if (soGhe <= 2)
            return 6;
        if (soGhe <= 4)
            return 12;
        return soGhe * 3;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        return btn;
    }

    // --- FORM ĐẶT BÀN FULL ---
    private void showDialogDatBan() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Đặt Bàn: " + banDangChon.getTenBan());
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setModal(true);
        dialog.setLayout(new GridLayout(6, 1, 10, 10));

        JPanel pnlKhach = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlKhach.setBorder(new TitledBorder("Thông tin khách hàng"));
        JTextField txtTenKH = new JTextField();
        JTextField txtSDT = new JTextField();
        pnlKhach.add(new JLabel("Tên khách:"));
        pnlKhach.add(txtTenKH);
        pnlKhach.add(new JLabel("Số điện thoại:"));
        pnlKhach.add(txtSDT);
        dialog.add(pnlKhach);

        JPanel pnlTime = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlTime.setBorder(new TitledBorder("Thời gian đặt"));
        com.toedter.calendar.JDateChooser txtNgay = new com.toedter.calendar.JDateChooser();
        txtNgay.setDateFormatString("dd/MM/yyyy");
        txtNgay.setDate(new java.util.Date());

        JComboBox<String> cboGio = new JComboBox<>();
        for (int h = 8; h <= 22; h++) {
            cboGio.addItem(String.format("%02d:00", h));
            cboGio.addItem(String.format("%02d:30", h));
        }
        cboGio.setSelectedItem("18:00");
        pnlTime.add(new JLabel("Chọn ngày:"));
        pnlTime.add(txtNgay);
        pnlTime.add(new JLabel("Khung giờ:"));
        pnlTime.add(cboGio);
        dialog.add(pnlTime);

        JPanel pnlMore = new JPanel(new GridLayout(2, 2, 10, 10));
        JSpinner spinNguoi = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
        JTextField txtGhiChu = new JTextField();
        pnlMore.add(new JLabel("Số lượng khách:"));
        pnlMore.add(spinNguoi);
        pnlMore.add(new JLabel("Ghi chú thêm:"));
        pnlMore.add(txtGhiChu);
        dialog.add(pnlMore);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnLuu = new JButton("XÁC NHẬN ĐẶT");
        JButton btnHuy = new JButton("Hủy Bỏ");
        pnlBtn.add(btnLuu);
        pnlBtn.add(btnHuy);
        dialog.add(pnlBtn);

        btnLuu.addActionListener(e -> {
            String ten = txtTenKH.getText();
            String sdt = txtSDT.getText();
            if (ten.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Nhập tên!");
                return;
            }

            try {
                java.util.Date dateChon = txtNgay.getDate();
                if (dateChon == null)
                    return;
                String[] parts = cboGio.getSelectedItem().toString().split(":");
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(dateChon);
                cal.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                cal.set(java.util.Calendar.MINUTE, Integer.parseInt(parts[1]));

                DatBanDAO dbDAO = new DatBanDAO();
                // Tạo thời gian kết thúc (2 tiếng sau thời gian bắt đầu)
                java.util.Calendar calEnd = (java.util.Calendar) cal.clone();
                calEnd.add(java.util.Calendar.HOUR, 2);

                DatBan db = new DatBan(
                        banDangChon.getMaBan(), ten, sdt,
                        cal.getTime(), calEnd.getTime(), // Thời gian bắt đầu và kết thúc
                        (int) spinNguoi.getValue(),
                        0, // Tiền cọc = 0
                        txtGhiChu.getText());

                if (dbDAO.insertDatBan(db)) {
                    JOptionPane.showMessageDialog(dialog, "Đặt bàn thành công!");
                    dialog.dispose();
                    reloadTableData();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        btnHuy.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    // --- HÀM XỬ LÝ CHECK-IN (KHÁCH ĐẶT ĐẾN) ---
    private void checkInKhachDat() {
        // 1. Lấy thông tin người đặt
        DatBan db = DatBanDAO.getDatBanGanNhat(banDangChon.getMaBan());

        String thongtin = "Có khách đặt bàn này nhưng không tìm thấy dữ liệu!";
        int soKhach = 2;
        String tenKhach = "Khách đặt";
        String sdtKhach = ""; // Biến lưu SĐT

        if (db != null) {
            thongtin = "Thông tin đặt bàn:\n" +
                    "- Khách hàng: " + db.getTenKhach() + "\n" +
                    "- SĐT: " + db.getSdt() + "\n" +
                    "- Số người dự kiến: " + db.getSoLuongKhach() + "\n\n" +
                    "Khách đã đến và muốn nhận bàn?";
            soKhach = db.getSoLuongKhach();
            tenKhach = db.getTenKhach();
            sdtKhach = db.getSdt();
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin đặt bàn!");
            return;
        }

        // 2. Hỏi xác nhận Check-in
        int confirm = JOptionPane.showConfirmDialog(this, thongtin, "Check-in Bàn Đặt", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (!checkSucChua(banDangChon, soKhach))
                return;

            // Kiểm tra khách hàng, nếu chưa có thì thêm mới
            DAO.KhachHangDAO khDAO = new DAO.KhachHangDAO();
            if (sdtKhach != null && !sdtKhach.isEmpty()) {
                if (!khDAO.checkTonTai(sdtKhach)) {
                    khDAO.themKhachMoi(sdtKhach, tenKhach);
                }
            }

            // Tạo hóa đơn
            String maNV = connectDB.SessionManager.getCurrentUser() != null
                    ? connectDB.SessionManager.getCurrentUser().getMaNV()
                    : null;
            HoaDon hd = new HoaDon(banDangChon.getMaBan(), soKhach, sdtKhach, "Khách đặt: " + tenKhach, maNV);

            int maHD = hdDAO.insert(hd);
            if (maHD != -1) {
                banDAO.updateTrangThai(banDangChon.getMaBan(), "Có Khách");

                // (Tùy chọn) Cập nhật trạng thái đơn đặt thành "Hoàn tất" để không hiện lại lần
                // sau
                // datBanDAO.updateTrangThai(db.getMaDat(), "Hoàn tất");

                reloadTableData();
                new ManHinhGoiMon(maHD, banDangChon.getTenBan(), false).setVisible(true);
            }
        }
    }

    // --- HÀM XỬ LÝ KHÁCH VÃNG LAI ---
    private void moBanChoKhachVangLai() {
        String input = JOptionPane.showInputDialog(this, "Mở bàn mới cho khách vãng lai.\nNhập số lượng khách:", "1");
        if (input == null)
            return;

        int soKhach = 1;
        try {
            soKhach = Integer.parseInt(input);
        } catch (Exception ex) {
            return;
        }

        if (!checkSucChua(banDangChon, soKhach))
            return;

        String maNV = connectDB.SessionManager.getCurrentUser() != null
                ? connectDB.SessionManager.getCurrentUser().getMaNV()
                : null;

        if (maNV == null) {
            JOptionPane.showMessageDialog(this,
                    "CẢNH BÁO: Hệ thống không nhận diện được nhân viên đang đăng nhập!\nHóa đơn sẽ được lưu nhưng không có tên thu ngân.",
                    "Lỗi Session", JOptionPane.WARNING_MESSAGE);
        }

        HoaDon hd = new HoaDon(banDangChon.getMaBan(), soKhach, null, "Khách vãng lai", maNV);
        int maHD = hdDAO.insert(hd);

        if (maHD != -1) {
            banDAO.updateTrangThai(banDangChon.getMaBan(), "Có Khách");
            reloadTableData();
            new ManHinhGoiMon(maHD, banDangChon.getTenBan(), false).setVisible(true);
        }
    }

    private void loadDSMonChoBan() {
        // Xóa dữ liệu cũ trên bảng
        modelOrder.setRowCount(0);
        lblTongTienTam.setText("Tổng: 0 VNĐ");

        if (banDangChon == null || banDangChon.getTrangThai().equals("Trống")) {
            return; // Bàn trống thì không có gì để hiện
        }

        // Lấy mã hóa đơn đang hoạt động
        int maHD = hdDAO.getMaHDByBan(banDangChon.getMaBan());
        if (maHD == -1)
            return;

        // Lấy danh sách món từ DAO
        ArrayList<String[]> listMon = hdDAO.getChiTietHoaDon(maHD);
        double tongTien = 0;

        for (String[] row : listMon) {
            modelOrder.addRow(row);

            // Cộng dồn tiền (Xử lý chuỗi "100,000" về số double để cộng)
            try {
                String tienStr = row[3].replace(",", "").replace(".", ""); // Bỏ dấu phẩy
                tongTien += Double.parseDouble(tienStr);
            } catch (Exception e) {
            }
        }

        // Cập nhật Label tổng tiền
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        lblTongTienTam.setText("Tổng: " + df.format(tongTien) + " VNĐ");
    }

    private void assignCustomer(String maBan) {
        int maHD = hdDAO.getMaHDByBan(maBan);
        if (maHD == -1) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn!");
            return;
        }

        // Show current info
        String curName = "Vãng lai";
        String curSDT = "";
        HoaDon hd = hdDAO.getThongTinHoaDon(maHD);
        if (hd != null && hd.getSdtKhach() != null) {
            curSDT = hd.getSdtKhach();
            String n = new KhachHangDAO().getTenKhachHang(curSDT);
            if (n != null)
                curName = n;
        }

        String message = "Khách hiện tại: " + curName + (curSDT.isEmpty() ? "" : " (" + curSDT + ")") +
                "\n\nNhập số điện thoại khách hàng mới:";

        String sdt = JOptionPane.showInputDialog(this, message, "Gán Khách Hàng", JOptionPane.QUESTION_MESSAGE);
        if (sdt == null || sdt.trim().isEmpty())
            return;

        sdt = sdt.trim();
        KhachHangDAO khDAO = new KhachHangDAO();
        String ten = khDAO.getTenKhachHang(sdt);

        if (ten == null) {
            // New Customer
            int confirm = JOptionPane.showConfirmDialog(this, "Khách hàng mới! Bạn có muốn tạo mới?", "Khách Mới",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String newName = JOptionPane.showInputDialog(this, "Nhập tên khách hàng:", "Tạo Khách Mới",
                        JOptionPane.PLAIN_MESSAGE);
                if (newName != null && !newName.trim().isEmpty()) {
                    if (khDAO.themKhachMoi(sdt, newName.trim())) {
                        ten = newName.trim();
                        JOptionPane.showMessageDialog(this, "Đã tạo khách hàng mới!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Lỗi khi tạo khách hàng!");
                        return;
                    }
                } else {
                    return;
                }
            } else {
                return;
            }
        }

        // Update Invoice
        if (hdDAO.updateSdtKhach(maHD, sdt)) {
            JOptionPane.showMessageDialog(this, "Đã cập nhật: " + ten + " (" + sdt + ")");
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật hóa đơn!");
        }
    }
}
