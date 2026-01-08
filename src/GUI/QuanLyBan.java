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

public class QuanLyBan extends JPanel {
    
    // --- COMPONENT ---
    private JTabbedPane tabKhuVuc;
    private JLabel lblTenBan, lblTrangThai;
    private JButton btnDatBan, btnGoiMon, btnThanhToan, btnChuyenBan, btnGhepBan;
    private JPanel pnlRight;
    
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
        
        // 3. EVENTS
        initEvents();

        // 4. TIMER (60s update 1 lần)
        timerUpdate = new Timer(60000, e -> {
            banDAO.capNhatTrangThaiDatBan(); 
            if(banDangChon == null) reloadTableData();
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
        pnlInfo.add(lblTenBan); pnlInfo.add(lblTrangThai);
        pnlRight.add(pnlInfo, BorderLayout.NORTH);
        
        // Buttons
        JPanel pnlAction = new JPanel(new GridLayout(3, 2, 10, 10));
        pnlAction.setPreferredSize(new Dimension(0, 180));
        
        btnDatBan = createButton("ĐẶT BÀN", new Color(241, 196, 15));
        btnGoiMon = createButton("GỌI MÓN", new Color(46, 204, 113));
        btnThanhToan = createButton("THANH TOÁN", new Color(231, 76, 60));
        btnChuyenBan = createButton("CHUYỂN BÀN", new Color(52, 152, 219));
        btnGhepBan = createButton("GHÉP BÀN", new Color(155, 89, 182)); // Tím
        
        pnlAction.add(btnGoiMon); pnlAction.add(btnThanhToan);
        pnlAction.add(btnChuyenBan); pnlAction.add(btnGhepBan);
        pnlAction.add(btnDatBan); 

        pnlRight.add(pnlAction, BorderLayout.SOUTH);
    }

    private void initEvents() {
        // GỌI MÓN (Mở bàn / Check-in / Thêm món)
        btnGoiMon.addActionListener(e -> {
            if (banDangChon == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn bàn!"); return;
            }
            // Logic Mở bàn mới hoặc Nhận bàn đặt
            if (banDangChon.getTrangThai().equals("Trống") || banDangChon.getTrangThai().equals("Đã Đặt")) {
                if(banDangChon.getTrangThai().equals("Đang Gộp")) {
                    JOptionPane.showMessageDialog(this, "Bàn này đang gộp vào " + banDangChon.getMaBanGop() + ". Hãy thao tác bên đó!");
                    return;
                }
                
                String msg = banDangChon.getTrangThai().equals("Đã Đặt") ? "Khách đặt đã đến? Nhận bàn?" : "Mở bàn mới?";
                if (JOptionPane.showConfirmDialog(this, msg, "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    
                    String input = JOptionPane.showInputDialog(this, "Nhập số lượng khách:", "1");
                    if (input == null) return;
                    int soKhach = 1;
                    try { soKhach = Integer.parseInt(input); } catch(Exception ex){}

                    if (!checkSucChua(banDangChon, soKhach)) return;

                    HoaDon hd = new HoaDon(banDangChon.getMaBan(), soKhach, null, "");
                    int maHD = hdDAO.insert(hd);

                    if (maHD != -1) {
                        banDAO.updateTrangThai(banDangChon.getMaBan(), "Có Khách");
                        reloadTableData();
                        new ManHinhGoiMon(maHD, banDangChon.getTenBan()).setVisible(true);
                    }
                }
            } 
            // Logic Gọi thêm
            else if (banDangChon.getTrangThai().equals("Có Khách")) {
                int maHD = hdDAO.getMaHDByBan(banDangChon.getMaBan());
                if (maHD != -1) new ManHinhGoiMon(maHD, banDangChon.getTenBan()).setVisible(true);
            }
        });

        // ĐẶT BÀN
        btnDatBan.addActionListener(e -> {
            if (banDangChon == null) return;
            if (!banDangChon.getTrangThai().equals("Trống")) {
                JOptionPane.showMessageDialog(this, "Chỉ đặt được bàn Trống!"); return;
            }
            showDialogDatBan();
        });

        // CHUYỂN BÀN
        btnChuyenBan.addActionListener(e -> {
            if (banDangChon == null || !banDangChon.getTrangThai().equals("Có Khách")) return;
            
            ArrayList<Ban> listTrong = banDAO.getBanTheoTrangThai("Trống");
            if (listTrong.isEmpty()) { JOptionPane.showMessageDialog(this, "Hết bàn trống!"); return; }

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
                JOptionPane.showMessageDialog(this, "Chọn bàn ĐÍCH (Có khách) để gộp vào!"); return;
            }

            ArrayList<Ban> listCoKhach = banDAO.getBanCoKhach();
            listCoKhach.removeIf(b -> b.getMaBan().equals(banDangChon.getMaBan())); // Bỏ bàn hiện tại

            if (listCoKhach.isEmpty()) return;

            JList<Ban> listJList = new JList<>(listCoKhach.toArray(new Ban[0]));
            listJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            
            int res = JOptionPane.showConfirmDialog(this, new JScrollPane(listJList), 
                    "Chọn các bàn nguồn (Giữ Ctrl để chọn nhiều):", JOptionPane.OK_CANCEL_OPTION);

            if (res == JOptionPane.OK_OPTION) {
                List<Ban> selectedBans = listJList.getSelectedValuesList();
                if (selectedBans.isEmpty()) return;

                ArrayList<String> listMaNguon = new ArrayList<>();
                int tongKhach = hdDAO.getSoLuongKhach(banDangChon.getMaBan());
                
                for (Ban b : selectedBans) {
                    tongKhach += hdDAO.getSoLuongKhach(b.getMaBan());
                    listMaNguon.add(b.getMaBan());
                }
                
                // Check Sức Chứa khi gộp
                int maxGhep = getGioiHanGhep(banDangChon.getSoGhe());
                if(tongKhach > maxGhep) {
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
                 int chon = JOptionPane.showConfirmDialog(this, "Thanh toán bàn " + banDangChon.getTenBan() + "?");
                 if(chon == JOptionPane.YES_OPTION) {
                     int maHD = hdDAO.getMaHDByBan(banDangChon.getMaBan());
                     hdDAO.thanhToan(maHD, 0); // Set tạm 0 đồng
                     
                     // Trả bàn chính về Trống
                     banDAO.updateTrangThai(banDangChon.getMaBan(), "Trống");
                     // Giải phóng các bàn phụ
                     banDAO.huyGopBan(banDangChon.getMaBan());
                     
                     reloadTableData();
                     JOptionPane.showMessageDialog(this, "Đã thanh toán và giải phóng bàn!");
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
            });
            pnl.add(btn);
        }
        return pnl;
    }

    private void updateRightPanel() {
        if(banDangChon != null) {
            lblTenBan.setText(banDangChon.getTenBan());
            lblTrangThai.setText(banDangChon.getTrangThai());
            
            Color c = Color.BLACK;
            if(banDangChon.getTrangThai().equals("Có Khách")) c = Color.RED;
            else if(banDangChon.getTrangThai().equals("Đang Gộp")) c = new Color(108, 92, 231);
            else if(banDangChon.getTrangThai().equals("Trống")) c = new Color(0, 150, 0);
            lblTrangThai.setForeground(c);
        }
    }

    private void reloadTableData() {
        int index = tabKhuVuc.getSelectedIndex();
        loadTabs();
        tabKhuVuc.setSelectedIndex(index);
        updateRightPanel();
    }
    
    private boolean checkSucChua(Ban b, int khach) {
        if (khach > b.getSoGhe()) {
            JOptionPane.showMessageDialog(this, "Quá tải! Bàn " + b.getSoGhe() + " chỗ không đủ.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    
    private int getGioiHanGhep(int soGhe) {
        if (soGhe <= 2) return 6;
        if (soGhe <= 4) return 12;
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
        pnlKhach.add(new JLabel("Tên khách:")); pnlKhach.add(txtTenKH);
        pnlKhach.add(new JLabel("Số điện thoại:")); pnlKhach.add(txtSDT);
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
        pnlTime.add(new JLabel("Chọn ngày:")); pnlTime.add(txtNgay);
        pnlTime.add(new JLabel("Khung giờ:")); pnlTime.add(cboGio);
        dialog.add(pnlTime);

        JPanel pnlMore = new JPanel(new GridLayout(2, 2, 10, 10));
        JSpinner spinNguoi = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
        JTextField txtGhiChu = new JTextField();
        pnlMore.add(new JLabel("Số lượng khách:")); pnlMore.add(spinNguoi);
        pnlMore.add(new JLabel("Ghi chú thêm:")); pnlMore.add(txtGhiChu);
        dialog.add(pnlMore);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnLuu = new JButton("XÁC NHẬN ĐẶT");
        JButton btnHuy = new JButton("Hủy Bỏ");
        pnlBtn.add(btnLuu); pnlBtn.add(btnHuy);
        dialog.add(pnlBtn);

        btnLuu.addActionListener(e -> {
            String ten = txtTenKH.getText();
            String sdt = txtSDT.getText();
            if(ten.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Nhập tên!"); return; }

            try {
                java.util.Date dateChon = txtNgay.getDate(); 
                if(dateChon==null) return;
                String[] parts = cboGio.getSelectedItem().toString().split(":");
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(dateChon);
                cal.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                cal.set(java.util.Calendar.MINUTE, Integer.parseInt(parts[1]));
                
                DatBanDAO dbDAO = new DatBanDAO();
                DatBan db = new DatBan(banDangChon.getMaBan(), ten, sdt, cal.getTime(), (int)spinNguoi.getValue(), txtGhiChu.getText());
                
                if(dbDAO.insertDatBan(db)) {
                    JOptionPane.showMessageDialog(dialog, "Đặt bàn thành công!");
                    dialog.dispose();
                    reloadTableData();
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        btnHuy.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }
}
