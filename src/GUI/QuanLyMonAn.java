package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import com.toedter.calendar.JDateChooser;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import DAO.BangGiaDAO;
import DAO.LoaiMonDAO;
import DAO.MonAnDAO;
import Entity.BangGia;
import Entity.LoaiMon;
import Entity.MonAn;
import UTILS.XImage;

public class QuanLyMonAn extends JPanel {

    // Khai báo biến
    private JTextField txtMa, txtTen, txtGia;
    private JComboBox<String> cboDVT;
    private JComboBox<LoaiMon> cboLoai; // Dùng cho form nhập
    private JLabel lblHinh;

    // --- PHẦN MỚI: CÔNG CỤ LỌC & TÌM KIẾM ---
    private JTextField txtTimKiem;
    private JButton btnTim;
    private JComboBox<LoaiMon> cboLocLoai; // Dùng để lọc ở giữa
    // ----------------------------------------

    private JPanel pnlDanhSach;

    private MonAnDAO dao = new MonAnDAO();
    private LoaiMonDAO daoLoai = new LoaiMonDAO();
    private BangGiaDAO bangGiaDAO = new BangGiaDAO();
    private String tenFileAnh = "default.png";

    Font fontLabel = new Font("Segoe UI", Font.BOLD, 16);
    Font fontInput = new Font("Segoe UI", Font.PLAIN, 16);

    // --- GĐ3: Bảng Giá Tab ---
    private JTable tblBangGia;
    private DefaultTableModel modelBangGia;
    private JComboBox<MonAn> cboBangGiaMon;
    private JTextField txtBangGiaDonGia;
    private JDateChooser txtBangGiaTuNgay, txtBangGiaDenNgay;
    private JSpinner txtBangGiaGioBatDau, txtBangGiaGioKetThuc;
    private JSpinner txtBangGiaUuTien;
    private JTextField txtBangGiaGhiChu;
    private int selectedMaGia = -1;

    public QuanLyMonAn() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ==> TABBED PANE CHÍNH <==
        JTabbedPane mainTabs = new JTabbedPane();
        mainTabs.setFont(new Font("Segoe UI", Font.BOLD, 14));

        mainTabs.addTab("Danh sách Món Ăn", createMonAnTab());
        mainTabs.addTab("Quản lý Bảng Giá", createBangGiaTab());

        add(mainTabs, BorderLayout.CENTER);
    }

    // ==================== TAB 1: DANH SÁCH MÓN ĂN ====================
    private JPanel createMonAnTab() {
        JPanel tabPanel = new JPanel(new BorderLayout(10, 10));
        tabPanel.setBackground(Color.WHITE);
        tabPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // ================= PHẦN 1: FORM NHẬP LIỆU (TOP) =================
        JPanel pnlTop = new JPanel(new BorderLayout(10, 0));
        pnlTop.setBackground(Color.WHITE);
        pnlTop.setPreferredSize(new Dimension(0, 300));
        pnlTop.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "THÔNG TIN MÓN ĂN", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 18), Color.BLUE));

        // A. Nhập liệu (Trái)
        JPanel pnlInputs = new JPanel(new GridLayout(4, 2, 15, 15));
        pnlInputs.setBackground(Color.WHITE);
        pnlInputs.setBorder(new EmptyBorder(10, 20, 10, 20));

        pnlInputs.add(createLabel("Mã món:"));
        txtMa = createTextField();
        pnlInputs.add(txtMa);

        pnlInputs.add(createLabel("Tên món:"));
        txtTen = createTextField();
        pnlInputs.add(txtTen);

        pnlInputs.add(createLabel("Đơn giá:"));
        txtGia = createTextField();
        pnlInputs.add(txtGia);

        pnlInputs.add(createLabel("ĐVT & Loại:"));
        JPanel pnlSub = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlSub.setBackground(Color.WHITE);

        cboDVT = new JComboBox<>(new String[] { "Dĩa", "Tô", "Lon", "Chai", "Ly", "Nồi", "Phần", "Kg" });
        cboDVT.setFont(fontInput);
        cboDVT.setBackground(Color.WHITE);

        cboLoai = new JComboBox<>();
        cboLoai.setFont(fontInput);
        cboLoai.setBackground(Color.WHITE);

        pnlSub.add(cboDVT);
        pnlSub.add(cboLoai);
        pnlInputs.add(pnlSub);

        pnlTop.add(pnlInputs, BorderLayout.CENTER);

        // B. Ảnh (Phải)
        JPanel pnlImage = new JPanel(new BorderLayout());
        pnlImage.setBackground(Color.WHITE);
        pnlImage.setBorder(new EmptyBorder(10, 10, 10, 30));

        lblHinh = new JLabel("CHỌN ẢNH");
        lblHinh.setPreferredSize(new Dimension(200, 200));
        lblHinh.setBorder(new LineBorder(Color.GRAY, 1));
        lblHinh.setHorizontalAlignment(JLabel.CENTER);
        lblHinh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblHinh.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                chonAnh();
            }
        });

        pnlImage.add(lblHinh, BorderLayout.CENTER);
        pnlTop.add(pnlImage, BorderLayout.EAST);

        // C. Nút bấm chức năng (Dưới cùng của Top)
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        pnlButtons.setBackground(Color.WHITE);

        JButton btnThem = createButton("THÊM");
        JButton btnSua = createButton("SỬA");
        JButton btnXoa = createButton("XÓA");
        JButton btnMoi = createButton("LÀM MỚI");

        pnlButtons.add(btnThem);
        pnlButtons.add(btnSua);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnMoi);

        pnlTop.add(pnlButtons, BorderLayout.SOUTH);
        tabPanel.add(pnlTop, BorderLayout.NORTH);

        // ================= PHẦN 2: THANH CÔNG CỤ & DANH SÁCH (CENTER) =================
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 10));
        pnlCenter.setBackground(Color.WHITE);

        // >> THANH TÌM KIẾM & LỌC <<
        JPanel pnlCongCu = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlCongCu.setBackground(new Color(240, 248, 255));
        pnlCongCu.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        pnlCongCu.add(createLabel("Tìm tên:"));
        txtTimKiem = new JTextField(15);
        txtTimKiem.setFont(fontInput);
        pnlCongCu.add(txtTimKiem);

        btnTim = new JButton("Tìm");
        btnTim.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTim.setBackground(new Color(46, 204, 113));
        btnTim.setForeground(Color.WHITE);
        pnlCongCu.add(btnTim);

        pnlCongCu.add(new JLabel("   |   Lọc theo loại:"));
        cboLocLoai = new JComboBox<>();
        cboLocLoai.setFont(fontInput);
        cboLocLoai.setBackground(Color.WHITE);
        cboLocLoai.setPreferredSize(new Dimension(150, 30));
        pnlCongCu.add(cboLocLoai);

        pnlCenter.add(pnlCongCu, BorderLayout.NORTH);

        // >> DANH SÁCH GRID <<
        pnlDanhSach = new JPanel(new GridLayout(0, 4, 20, 20));
        pnlDanhSach.setBackground(new Color(245, 245, 245));

        JScrollPane scroll = new JScrollPane(pnlDanhSach);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(BorderFactory.createTitledBorder("DANH SÁCH MÓN ĂN"));

        pnlCenter.add(scroll, BorderLayout.CENTER);
        tabPanel.add(pnlCenter, BorderLayout.CENTER);

        // ================= KHỞI TẠO DỮ LIỆU =================
        loadComboboxLoai();
        loadComboboxLoc();
        loadDataGrid();

        // ================= SỰ KIỆN NÚT BẤM CƠ BẢN =================
        btnThem.addActionListener(e -> {
            if (dao.insert(getForm())) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                loadDataGrid();
                clearForm();
            } else
                JOptionPane.showMessageDialog(this, "Lỗi thêm!");
        });

        btnSua.addActionListener(e -> {
            if (dao.update(getForm())) {
                JOptionPane.showMessageDialog(this, "Sửa thành công!");
                loadDataGrid();
                clearForm();
            }
        });

        btnXoa.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Xóa món này?") == 0) {
                String maMon = txtMa.getText();
                if (dao.delete(maMon)) {
                    XImage.delete(tenFileAnh);
                    loadDataGrid();
                    clearForm();
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                }
            }
        });

        btnMoi.addActionListener(e -> clearForm());

        // ================= SỰ KIỆN TÌM KIẾM & LỌC =================
        btnTim.addActionListener(e -> xuLyTimKiem());
        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                xuLyTimKiem();
            }
        });
        cboLocLoai.addActionListener(e -> xuLyTimKiem());

        return tabPanel;
    }

    // ==================== TAB 2: QUẢN LÝ BẢNG GIÁ (GĐ3) ====================
    private JPanel createBangGiaTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBackground(Color.WHITE);
        tab.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- FORM NHẬP ---
        JPanel pnlForm = new JPanel(new GridLayout(4, 4, 10, 10));
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "THÊM / SỬA MỨC GIÁ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 16), new Color(124, 58, 237)));

        // Row 1
        pnlForm.add(createLabel("Món ăn (nhập lọc):"));
        JPanel pnlMon = new JPanel(new BorderLayout(5, 0)); // Thêm khoảng cách ngang 5px
        JTextField txtLocMon = createTextField();
        txtLocMon.setPreferredSize(new java.awt.Dimension(80, 0)); // Cố định chiều rộng cho ô text
        cboBangGiaMon = new JComboBox<>();
        cboBangGiaMon.setFont(fontInput);
        pnlMon.add(txtLocMon, BorderLayout.WEST); // Đặt nằm ngang hàng
        pnlMon.add(cboBangGiaMon, BorderLayout.CENTER);
        pnlForm.add(pnlMon);

        txtLocMon.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String keyword = txtLocMon.getText().toLowerCase();
                cboBangGiaMon.removeAllItems();
                for (MonAn m : dao.getAll()) {
                    if (m.getTenMon().toLowerCase().contains(keyword)) {
                        cboBangGiaMon.addItem(m);
                    }
                }
            }
        });

        cboBangGiaMon.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                MonAn selectedMon = (MonAn) cboBangGiaMon.getSelectedItem();
                if (selectedMon != null && txtBangGiaDonGia.getText().trim().isEmpty()) {
                    txtBangGiaDonGia.setText(new DecimalFormat("#.##").format(selectedMon.getDonGia()).replace(",", ""));
                }
            }
        });

        pnlForm.add(createLabel("Đơn giá:"));
        txtBangGiaDonGia = createTextField();
        pnlForm.add(txtBangGiaDonGia);

        // Row 2
        pnlForm.add(createLabel("Từ ngày:"));
        txtBangGiaTuNgay = new JDateChooser();
        txtBangGiaTuNgay.setDateFormatString("yyyy-MM-dd");
        pnlForm.add(txtBangGiaTuNgay);

        pnlForm.add(createLabel("Đến ngày:"));
        txtBangGiaDenNgay = new JDateChooser();
        txtBangGiaDenNgay.setDateFormatString("yyyy-MM-dd");
        pnlForm.add(txtBangGiaDenNgay);

        // Row 3
        pnlForm.add(createLabel("Giờ bắt đầu (HH:mm):"));
        txtBangGiaGioBatDau = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditorBD = new JSpinner.DateEditor(txtBangGiaGioBatDau, "HH:mm");
        txtBangGiaGioBatDau.setEditor(timeEditorBD);
        pnlForm.add(txtBangGiaGioBatDau);

        pnlForm.add(createLabel("Giờ kết thúc (HH:mm):"));
        txtBangGiaGioKetThuc = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditorKT = new JSpinner.DateEditor(txtBangGiaGioKetThuc, "HH:mm");
        txtBangGiaGioKetThuc.setEditor(timeEditorKT);
        pnlForm.add(txtBangGiaGioKetThuc);

        // Row 4
        pnlForm.add(createLabel("Ưu tiên (0-10):"));
        txtBangGiaUuTien = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        pnlForm.add(txtBangGiaUuTien);

        pnlForm.add(createLabel("Ghi chú:"));
        txtBangGiaGhiChu = createTextField();
        pnlForm.add(txtBangGiaGhiChu);

        tab.add(pnlForm, BorderLayout.NORTH);

        // --- BUTTONS ---
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        pnlBtns.setBackground(Color.WHITE);

        JButton btnThemGia = createButton("THÊM GIÁ");
        btnThemGia.setBackground(new Color(22, 163, 74));
        JButton btnSuaGia = createButton("SỬA GIÁ");
        btnSuaGia.setBackground(new Color(234, 88, 12));
        JButton btnXoaGia = createButton("XÓA GIÁ");
        btnXoaGia.setBackground(new Color(220, 38, 38));
        JButton btnLamMoiGia = createButton("LÀM MỚI");

        pnlBtns.add(btnThemGia);
        pnlBtns.add(btnSuaGia);
        pnlBtns.add(btnXoaGia);
        pnlBtns.add(btnLamMoiGia);

        // --- TABLE ---
        String[] headers = { "Mã Giá", "Mã Món", "Tên Món", "Đơn Giá", "Từ Ngày", "Đến Ngày",
                "Giờ BĐ", "Giờ KT", "Ưu Tiên", "Ghi Chú" };
        modelBangGia = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tblBangGia = new JTable(modelBangGia);
        tblBangGia.setRowHeight(30);
        tblBangGia.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblBangGia.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        tblBangGia.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblBangGia.getSelectedRow();
                if (row >= 0) fillBangGiaForm(row);
            }
        });

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.add(pnlBtns, BorderLayout.NORTH);
        pnlCenter.add(new JScrollPane(tblBangGia), BorderLayout.CENTER);
        tab.add(pnlCenter, BorderLayout.CENTER);

        // Load Data
        loadCboBangGiaMon();
        loadBangGiaData();

        // Events
        btnThemGia.addActionListener(e -> {
            BangGia bg = getBangGiaForm();
            if (bg == null) return;
            if (bangGiaDAO.insert(bg)) {
                JOptionPane.showMessageDialog(this, "Thêm mức giá thành công!");
                loadBangGiaData();
                loadDataGrid(); // Refresh cards to show new price
                clearBangGiaForm();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi thêm mức giá!");
            }
        });

        btnSuaGia.addActionListener(e -> {
            if (selectedMaGia == -1) {
                JOptionPane.showMessageDialog(this, "Chọn mức giá cần sửa!");
                return;
            }
            BangGia bg = getBangGiaForm();
            if (bg == null) return;
            bg.setMaGia(selectedMaGia);
            if (bangGiaDAO.update(bg)) {
                JOptionPane.showMessageDialog(this, "Sửa mức giá thành công!");
                loadBangGiaData();
                loadDataGrid(); // Refresh cards to show new price
                clearBangGiaForm();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi sửa mức giá!");
            }
        });

        btnXoaGia.addActionListener(e -> {
            if (selectedMaGia == -1) {
                JOptionPane.showMessageDialog(this, "Chọn mức giá cần xóa!");
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Xóa mức giá này?") == 0) {
                if (bangGiaDAO.delete(selectedMaGia)) {
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    loadBangGiaData();
                    loadDataGrid(); // Refresh cards to show reset price
                    clearBangGiaForm();
                }
            }
        });

        btnLamMoiGia.addActionListener(e -> clearBangGiaForm());

        return tab;
    }

    // --- Bảng Giá helpers ---
    private void loadCboBangGiaMon() {
        cboBangGiaMon.removeAllItems();
        for (MonAn m : dao.getAll()) {
            cboBangGiaMon.addItem(m);
        }
    }

    private void loadBangGiaData() {
        modelBangGia.setRowCount(0);
        DecimalFormat df = new DecimalFormat("#,###");
        for (BangGia bg : bangGiaDAO.getAll()) {
            // Try to get TenMon from combo
            String tenMon = "";
            for (int i = 0; i < cboBangGiaMon.getItemCount(); i++) {
                MonAn m = cboBangGiaMon.getItemAt(i);
                if (m.getMaMon().equals(bg.getMaMon())) {
                    tenMon = m.getTenMon();
                    break;
                }
            }
            modelBangGia.addRow(new Object[] {
                    bg.getMaGia(), bg.getMaMon(), tenMon,
                    df.format(bg.getDonGia()),
                    bg.getTuNgay() != null ? bg.getTuNgay() : "",
                    bg.getDenNgay() != null ? bg.getDenNgay() : "",
                    bg.getGioBatDau() != null ? bg.getGioBatDau() : "",
                    bg.getGioKetThuc() != null ? bg.getGioKetThuc() : "",
                    bg.getUuTien(),
                    bg.getGhiChu() != null ? bg.getGhiChu() : ""
            });
        }
    }

    private BangGia getBangGiaForm() {
        MonAn selectedMon = (MonAn) cboBangGiaMon.getSelectedItem();
        if (selectedMon == null) {
            JOptionPane.showMessageDialog(this, "Chọn món ăn!");
            return null;
        }
        double donGia;
        try {
            donGia = Double.parseDouble(txtBangGiaDonGia.getText().replace(",", "").replace(".", ""));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Đơn giá không hợp lệ!");
            return null;
        }
        String tuNgay = null;
        if (txtBangGiaTuNgay.getDate() != null) {
            tuNgay = new java.text.SimpleDateFormat("yyyy-MM-dd").format(txtBangGiaTuNgay.getDate());
        }
        String denNgay = null;
        if (txtBangGiaDenNgay.getDate() != null) {
            denNgay = new java.text.SimpleDateFormat("yyyy-MM-dd").format(txtBangGiaDenNgay.getDate());
        }
        String gioBD = new java.text.SimpleDateFormat("HH:mm:00").format(txtBangGiaGioBatDau.getValue());
        String gioKT = new java.text.SimpleDateFormat("HH:mm:00").format(txtBangGiaGioKetThuc.getValue());
        int uuTien = (int) txtBangGiaUuTien.getValue();
        String ghiChu = txtBangGiaGhiChu.getText().trim().isEmpty() ? null : txtBangGiaGhiChu.getText().trim();

        return new BangGia(selectedMon.getMaMon(), donGia, tuNgay, denNgay, gioBD, gioKT, uuTien, ghiChu);
    }

    private void fillBangGiaForm(int row) {
        selectedMaGia = Integer.parseInt(modelBangGia.getValueAt(row, 0).toString());
        String maMon = modelBangGia.getValueAt(row, 1).toString();
        for (int i = 0; i < cboBangGiaMon.getItemCount(); i++) {
            if (cboBangGiaMon.getItemAt(i).getMaMon().equals(maMon)) {
                cboBangGiaMon.setSelectedIndex(i);
                break;
            }
        }
        txtBangGiaDonGia.setText(modelBangGia.getValueAt(row, 3).toString().replace(",", ""));
        try {
            String tuNgayStr = modelBangGia.getValueAt(row, 4).toString();
            txtBangGiaTuNgay.setDate(tuNgayStr.isEmpty() ? null : new java.text.SimpleDateFormat("yyyy-MM-dd").parse(tuNgayStr));
            String denNgayStr = modelBangGia.getValueAt(row, 5).toString();
            txtBangGiaDenNgay.setDate(denNgayStr.isEmpty() ? null : new java.text.SimpleDateFormat("yyyy-MM-dd").parse(denNgayStr));
        } catch (Exception e) {}
        String gioBD = modelBangGia.getValueAt(row, 6).toString();
        try { txtBangGiaGioBatDau.setValue(new java.text.SimpleDateFormat("HH:mm").parse(gioBD)); } catch (Exception e){}
        String gioKT = modelBangGia.getValueAt(row, 7).toString();
        try { txtBangGiaGioKetThuc.setValue(new java.text.SimpleDateFormat("HH:mm").parse(gioKT)); } catch (Exception e){}
        try { txtBangGiaUuTien.setValue(Integer.parseInt(modelBangGia.getValueAt(row, 8).toString())); } catch (Exception e) {}
        txtBangGiaGhiChu.setText(modelBangGia.getValueAt(row, 9).toString());
    }

    private void clearBangGiaForm() {
        selectedMaGia = -1;
        txtBangGiaDonGia.setText("");
        txtBangGiaTuNgay.setDate(null);
        txtBangGiaDenNgay.setDate(null);
        try { txtBangGiaGioBatDau.setValue(new java.text.SimpleDateFormat("HH:mm").parse("00:00")); } catch (Exception e){}
        try { txtBangGiaGioKetThuc.setValue(new java.text.SimpleDateFormat("HH:mm").parse("00:00")); } catch (Exception e){}
        txtBangGiaUuTien.setValue(0);
        txtBangGiaGhiChu.setText("");
    }

    // ================== HÀM XỬ LÝ GRID VIEW ==================

    void xuLyTimKiem() {
        String keyword = txtTimKiem.getText();
        String maLoai = "";

        if (cboLocLoai.getSelectedItem() != null) {
            LoaiMon lm = (LoaiMon) cboLocLoai.getSelectedItem();
            if (lm.getMaLoai() != null) {
                maLoai = lm.getMaLoai();
            }
        }

        ArrayList<MonAn> list = dao.timKiem(keyword, maLoai);

        pnlDanhSach.removeAll();
        for (MonAn m : list) {
            ItemMonAn item = new ItemMonAn(m);
            pnlDanhSach.add(item);
        }
        pnlDanhSach.revalidate();
        pnlDanhSach.repaint();
    }

    void loadDataGrid() {
        txtTimKiem.setText("");
        if (cboLocLoai.getItemCount() > 0)
            cboLocLoai.setSelectedIndex(0);
        xuLyTimKiem();
    }

    // Class con: ItemMonAn
    public class ItemMonAn extends JPanel {
        private MonAn monAn;

        public ItemMonAn(MonAn m) {
            this.monAn = m;
            setPreferredSize(new Dimension(220, 280));
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel lblImg = new JLabel();
            lblImg.setHorizontalAlignment(JLabel.CENTER);
            ImageIcon icon = XImage.read(m.getHinhAnh());
            if (icon != null) {
                Image img = icon.getImage().getScaledInstance(200, 160, Image.SCALE_SMOOTH);
                lblImg.setIcon(new ImageIcon(img));
            } else
                lblImg.setText("No Image");
            add(lblImg, BorderLayout.CENTER);

            JPanel pnlInfo = new JPanel(new GridLayout(2, 1));
            pnlInfo.setBackground(new Color(250, 250, 250));
            pnlInfo.setPreferredSize(new Dimension(220, 80));
            pnlInfo.setBorder(new EmptyBorder(5, 10, 5, 10));

            JLabel lblTen = new JLabel(m.getTenMon());
            lblTen.setFont(new Font("Segoe UI", Font.BOLD, 16));

            // [GĐ3] Show dynamic price if available
            double displayPrice = m.getDonGia();
            try {
                double dynamicPrice = bangGiaDAO.getGiaHienTai(m.getMaMon());
                if (dynamicPrice > 0) displayPrice = dynamicPrice;
            } catch (Exception e) { /* fallback to default */ }

            DecimalFormat df = new DecimalFormat("#,### VNĐ");
            JLabel lblGia = new JLabel(df.format(displayPrice));
            lblGia.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblGia.setForeground(new Color(200, 50, 50));

            pnlInfo.add(lblTen);
            pnlInfo.add(lblGia);
            add(pnlInfo, BorderLayout.SOUTH);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215), 2));
                }

                public void mouseExited(MouseEvent e) {
                    setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
                }

                public void mouseClicked(MouseEvent e) {
                    fillForm(monAn);
                }
            });
        }
    }

    // ================== CÁC HÀM HỖ TRỢ KHÁC ==================

    void loadComboboxLoai() {
        cboLoai.removeAllItems();
        for (LoaiMon lm : daoLoai.getAllLoai())
            cboLoai.addItem(lm);
    }

    void loadComboboxLoc() {
        cboLocLoai.removeAllItems();
        cboLocLoai.addItem(new LoaiMon("", "--- Tất cả ---"));
        for (LoaiMon lm : daoLoai.getAllLoai()) {
            cboLocLoai.addItem(lm);
        }
    }

    void fillForm(MonAn m) {
        txtMa.setText(m.getMaMon());
        txtTen.setText(m.getTenMon());
        txtGia.setText(String.valueOf((int) m.getDonGia()));
        cboDVT.setSelectedItem(m.getDonViTinh());
        for (int i = 0; i < cboLoai.getItemCount(); i++) {
            LoaiMon lm = cboLoai.getItemAt(i);
            if (lm.getMaLoai().equals(m.getMaLoai())) {
                cboLoai.setSelectedIndex(i);
                break;
            }
        }
        tenFileAnh = m.getHinhAnh();
        ImageIcon icon = XImage.read(tenFileAnh);
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(lblHinh.getWidth(), lblHinh.getHeight(), Image.SCALE_SMOOTH);
            lblHinh.setIcon(new ImageIcon(img));
            lblHinh.setText("");
        }
    }

    MonAn getForm() {
        String ma = txtMa.getText();
        String ten = txtTen.getText();
        String dvt = cboDVT.getSelectedItem().toString();
        double gia = 0;
        try {
            gia = Double.parseDouble(txtGia.getText());
        } catch (Exception e) {
        }
        String maLoai = ((LoaiMon) cboLoai.getSelectedItem()).getMaLoai();
        return new MonAn(ma, ten, dvt, gia, tenFileAnh, maLoai);
    }

    void clearForm() {
        txtMa.setText("");
        txtTen.setText("");
        txtGia.setText("");
        lblHinh.setIcon(null);
        lblHinh.setText("CHỌN ẢNH");
        tenFileAnh = "default.png";
    }

    void chonAnh() {
        JFileChooser ch = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Ảnh", "png", "jpg");
        ch.setFileFilter(filter);
        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File src = ch.getSelectedFile();
            XImage.save(src);
            tenFileAnh = src.getName();
            ImageIcon icon = XImage.read(tenFileAnh);
            Image img = icon.getImage().getScaledInstance(lblHinh.getWidth(), lblHinh.getHeight(), Image.SCALE_SMOOTH);
            lblHinh.setIcon(new ImageIcon(img));
            lblHinh.setText("");
        }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(fontLabel);
        return lbl;
    }

    private JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setFont(fontInput);
        return txt;
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(0, 102, 204));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(120, 40));
        return btn;
    }
}