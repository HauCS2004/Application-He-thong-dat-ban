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

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import DAO.BangGiaDAO;
import DAO.LoaiMonDAO;
import DAO.MonAnDAO;
import Entity.BangGia;
import Entity.ChiTietBangGia;
import Entity.LoaiMon;
import Entity.MonAn;
import GUI.utils.UIStyle;
import UTILS.XImage;

public class QuanLyMonAn extends JPanel {

    // --- Tab 1: Món Ăn Variables ---
    private JTextField txtTimKiem;
    private JButton btnTim;
    private JComboBox<LoaiMon> cboLocLoai;
    private JPanel pnlDanhSach;
    private MonAn selectedMonAn; 

    // --- Tab 2: Bảng Giá Variables ---
    private JTable tblBangGia, tblChiTiet;
    private DefaultTableModel modelBangGia, modelChiTiet;
    private int selectedMaBG = -1;

    // --- DAO & Config ---
    private MonAnDAO dao = new MonAnDAO();
    private LoaiMonDAO daoLoai = new LoaiMonDAO();
    private BangGiaDAO bangGiaDAO = new BangGiaDAO();

    Font fontLabel = new Font("Segoe UI", Font.BOLD, 16);
    Font fontInput = new Font("Segoe UI", Font.PLAIN, 16);

    public QuanLyMonAn() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

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

        // Toolbar
        JPanel pnlToolbar = new JPanel(new BorderLayout(10, 10));
        pnlToolbar.setBackground(new Color(243, 244, 246));
        pnlToolbar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search & Filter (West)
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlSearch.setBackground(new Color(243, 244, 246));

        pnlSearch.add(createLabel("Tìm tên:"));
        txtTimKiem = UIStyle.textField("", 15);
        pnlSearch.add(txtTimKiem);

        btnTim = UIStyle.buttonSm(UIStyle.BtnType.PRIMARY, "Tìm");
        pnlSearch.add(btnTim);

        pnlSearch.add(new JLabel("   |   Lọc theo loại:"));
        cboLocLoai = new JComboBox<>();
        UIStyle.styleComboBox(cboLocLoai);
        cboLocLoai.setPreferredSize(new Dimension(150, 35));
        pnlSearch.add(cboLocLoai);

        // Actions (East)
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setBackground(new Color(243, 244, 246));

        JButton btnThem = UIStyle.button(UIStyle.BtnType.SUCCESS, "THÊM");
        JButton btnSua = UIStyle.button(UIStyle.BtnType.WARNING, "SỬA");
        JButton btnXoa = UIStyle.button(UIStyle.BtnType.DANGER, "XÓA");

        pnlActions.add(btnThem);
        pnlActions.add(btnSua);
        pnlActions.add(btnXoa);

        pnlToolbar.add(pnlSearch, BorderLayout.WEST);
        pnlToolbar.add(pnlActions, BorderLayout.EAST);
        tabPanel.add(pnlToolbar, BorderLayout.NORTH);

        // Center Grid
        pnlDanhSach = new JPanel(new GridLayout(0, 4, 15, 15));
        pnlDanhSach.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(pnlDanhSach);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        tabPanel.add(scroll, BorderLayout.CENTER);

        // Data & Events
        loadComboboxLoc();
        loadDataGrid();

        btnThem.addActionListener(e -> {
            MonAnDialog dialog = new MonAnDialog(this, null, dao, daoLoai);
            dialog.setVisible(true);
            if (dialog.isSaved()) loadDataGrid();
        });

        btnSua.addActionListener(e -> {
            if (selectedMonAn == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn món ăn cần sửa!");
                return;
            }
            MonAnDialog dialog = new MonAnDialog(this, selectedMonAn, dao, daoLoai);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                selectedMonAn = null;
                loadDataGrid();
            }
        });

        btnXoa.addActionListener(e -> {
            if (selectedMonAn == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn món ăn cần xóa!");
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Thu hồi và xóa món " + selectedMonAn.getTenMon() + "?") == 0) {
                if (dao.delete(selectedMonAn.getMaMon())) {
                    XImage.delete(selectedMonAn.getHinhAnh());
                    selectedMonAn = null;
                    loadDataGrid();
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi xóa món (Món ăn có thể đang được sử dụng)!");
                }
            }
        });

        btnTim.addActionListener(e -> xuLyTimKiem());
        txtTimKiem.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { xuLyTimKiem(); }
        });
        cboLocLoai.addActionListener(e -> xuLyTimKiem());

        return tabPanel;
    }

    // ==================== TAB 2: QUẢN LÝ BẢNG GIÁ ====================
    private JPanel createBangGiaTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 10));
        tab.setBackground(Color.WHITE);
        tab.setBorder(new EmptyBorder(10, 10, 10, 10));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(10);

        // ============ VÙNG MASTER: CHIẾN DỊCH GIÁ ============
        JPanel pnlMaster = new JPanel(new BorderLayout(5, 10));
        pnlMaster.setBackground(Color.WHITE);
        pnlMaster.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "CHIẾN DỊCH GIÁ (MASTER)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 15), UIStyle.PRIMARY));

        JPanel pnlMasterTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlMasterTop.setBackground(Color.WHITE);
        JButton btnThemBG = UIStyle.buttonSm(UIStyle.BtnType.SUCCESS, "THÊM CHIẾN DỊCH");
        JButton btnSuaBG = UIStyle.buttonSm(UIStyle.BtnType.WARNING, "SỬA CHIẾN DỊCH");
        JButton btnXoaBG = UIStyle.buttonSm(UIStyle.BtnType.DANGER, "XÓA CHIẾN DỊCH");
        pnlMasterTop.add(btnThemBG);
        pnlMasterTop.add(btnSuaBG);
        pnlMasterTop.add(btnXoaBG);
        pnlMaster.add(pnlMasterTop, BorderLayout.NORTH);

        String[] headerBG = {"Mã CD", "Tên CD", "Loại", "Từ Ngày", "Đến Ngày", "Giờ BĐ", "Giờ KT", "Ưu Tiên", "Trạng Thái", "Ghi Chú"};
        modelBangGia = new DefaultTableModel(headerBG, 0) { public boolean isCellEditable(int row, int column) { return false; } };
        tblBangGia = new JTable(modelBangGia);
        UIStyle.styleTable(tblBangGia);
        pnlMaster.add(new JScrollPane(tblBangGia), BorderLayout.CENTER);

        // ============ VÙNG DETAIL: CHI TIẾT MÓN ĂN ============
        JPanel pnlDetail = new JPanel(new BorderLayout(5, 10));
        pnlDetail.setBackground(Color.WHITE);
        pnlDetail.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "MÓN ĂN ÁP DỤNG TRONG CHIẾN DỊCH", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 15), UIStyle.PRIMARY));

        JPanel pnlDetailTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlDetailTop.setBackground(Color.WHITE);
        JButton btnThemMon = UIStyle.buttonSm(UIStyle.BtnType.SUCCESS, "THÊM MÓN VÀO CD");
        JButton btnXoaMon = UIStyle.buttonSm(UIStyle.BtnType.DANGER, "XÓA MÓN KHỎI CD");
        pnlDetailTop.add(btnThemMon);
        pnlDetailTop.add(btnXoaMon);
        pnlDetail.add(pnlDetailTop, BorderLayout.NORTH);

        String[] headerCT = {"Mã Món", "Tên Món", "Đơn Giá Khuyến Mãi", "Ghi Chú"};
        modelChiTiet = new DefaultTableModel(headerCT, 0) { public boolean isCellEditable(int row, int column) { return false; } };
        tblChiTiet = new JTable(modelChiTiet);
        UIStyle.styleTable(tblChiTiet);
        pnlDetail.add(new JScrollPane(tblChiTiet), BorderLayout.CENTER);

        splitPane.setTopComponent(pnlMaster);
        splitPane.setBottomComponent(pnlDetail);
        tab.add(splitPane, BorderLayout.CENTER);

        // Events
        loadBangGiaData();

        tblBangGia.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblBangGia.getSelectedRow() != -1) {
                selectedMaBG = Integer.parseInt(tblBangGia.getValueAt(tblBangGia.getSelectedRow(), 0).toString());
                loadChiTietData(selectedMaBG);
            }
        });

        // ACTIONS
        btnThemBG.addActionListener(e -> {
            BangGiaDialog dialog = new BangGiaDialog(this, null, bangGiaDAO);
            dialog.setVisible(true);
            if (dialog.isSaved()) loadBangGiaData();
        });

        btnSuaBG.addActionListener(e -> {
            if (selectedMaBG == -1) {
                JOptionPane.showMessageDialog(this, "Chọn chiến dịch cần sửa!");
                return;
            }
            BangGia bg = bangGiaDAO.getAll().stream().filter(b -> b.getMaBG() == selectedMaBG).findFirst().orElse(null);
            if (bg != null) {
                BangGiaDialog dialog = new BangGiaDialog(this, bg, bangGiaDAO);
                dialog.setVisible(true);
                if (dialog.isSaved()) loadBangGiaData();
            }
        });

        btnXoaBG.addActionListener(e -> {
            if (selectedMaBG == -1) {
                JOptionPane.showMessageDialog(this, "Chọn chiến dịch cần xóa!");
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Xóa chiến dịch này?") == 0) {
                if (bangGiaDAO.delete(selectedMaBG)) {
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    loadBangGiaData();
                    modelChiTiet.setRowCount(0);
                    selectedMaBG = -1;
                } else JOptionPane.showMessageDialog(this, "Không thể xóa. Cần xóa hết món ăn thuộc chiến dịch này trước.");
            }
        });

        btnThemMon.addActionListener(e -> {
            if (selectedMaBG == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn Chiến Dịch Giá ở bảng trên trước!");
                return;
            }
            ChiTietBangGiaDialog dialog = new ChiTietBangGiaDialog(this, selectedMaBG, bangGiaDAO);
            dialog.setVisible(true);
            if (dialog.isSaved()) loadChiTietData(selectedMaBG);
        });

        btnXoaMon.addActionListener(e -> {
            int row = tblChiTiet.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Chọn món trong bảng chi tiết để xóa!");
                return;
            }
            String maMon = tblChiTiet.getValueAt(row, 0).toString();
            if (bangGiaDAO.deleteChiTiet(selectedMaBG, maMon)) {
                loadChiTietData(selectedMaBG);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi xóa món!");
            }
        });

        return tab;
    }

    // --- Helpers Tab 1 ---
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(fontLabel);
        return lbl;
    }

    void xuLyTimKiem() {
        String keyword = txtTimKiem.getText();
        String maLoai = "";
        if (cboLocLoai.getSelectedItem() != null) {
            LoaiMon lm = (LoaiMon) cboLocLoai.getSelectedItem();
            if (lm.getMaLoai() != null) maLoai = lm.getMaLoai();
        }

        ArrayList<MonAn> list = dao.timKiem(keyword, maLoai);
        pnlDanhSach.removeAll();
        for (MonAn m : list) {
            pnlDanhSach.add(new ItemMonAn(m));
        }
        pnlDanhSach.revalidate();
        pnlDanhSach.repaint();
    }

    void loadDataGrid() {
        txtTimKiem.setText("");
        if (cboLocLoai.getItemCount() > 0) cboLocLoai.setSelectedIndex(0);
        xuLyTimKiem();
    }

    void loadComboboxLoc() {
        cboLocLoai.removeAllItems();
        cboLocLoai.addItem(new LoaiMon("", "--- Tất cả ---"));
        for (LoaiMon lm : daoLoai.getAllLoai()) cboLocLoai.addItem(lm);
    }

    // --- Helpers Tab 2 ---
    private void loadBangGiaData() {
        modelBangGia.setRowCount(0);
        for (BangGia bg : bangGiaDAO.getAll()) {
            modelBangGia.addRow(new Object[] {
                    bg.getMaBG(), bg.getTenBG(), bg.getLoaiBG(),
                    bg.getNgayBatDau() != null ? bg.getNgayBatDau() : "",
                    bg.getNgayKetThuc() != null ? bg.getNgayKetThuc() : "",
                    bg.getGioBatDau() != null ? bg.getGioBatDau() : "",
                    bg.getGioKetThuc() != null ? bg.getGioKetThuc() : "",
                    bg.getUuTien(), bg.getTrangThai(), bg.getGhiChu() != null ? bg.getGhiChu() : ""
            });
        }
    }

    private void loadChiTietData(int maBG) {
        modelChiTiet.setRowCount(0);
        DecimalFormat df = new DecimalFormat("#,###");
        ArrayList<ChiTietBangGia> list = bangGiaDAO.getChiTietByMaBG(maBG);
        for (ChiTietBangGia ct : list) {
            MonAn m = dao.getByMaMon(ct.getMaMon());
            String tenMon = m != null ? m.getTenMon() : "...";
            modelChiTiet.addRow(new Object[] {
                ct.getMaMon(), tenMon, df.format(ct.getDonGia()), ct.getGhiChu() != null ? ct.getGhiChu() : ""
            });
        }
    }

    // --- Inner Class Card Món Ăn ---
    public class ItemMonAn extends JPanel {
        private MonAn monAn;
        public ItemMonAn(MonAn m) {
            this.monAn = m;
            setPreferredSize(new Dimension(220, 260));
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel lblImg = new JLabel();
            lblImg.setHorizontalAlignment(JLabel.CENTER);
            ImageIcon icon = XImage.read(m.getHinhAnh());
            if (icon != null && icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(180, 160, Image.SCALE_SMOOTH);
                lblImg.setIcon(new ImageIcon(img));
            } else lblImg.setText("No Image");
            add(lblImg, BorderLayout.CENTER);

            JPanel pnlInfo = new JPanel(new GridLayout(2, 1));
            pnlInfo.setBackground(new Color(250, 250, 250));
            pnlInfo.setBorder(new EmptyBorder(5, 10, 5, 10));

            JLabel lblTen = new JLabel(m.getTenMon());
            lblTen.setFont(new Font("Segoe UI", Font.BOLD, 15));

            double displayPrice = m.getDonGia();
            try {
                double dynamicPrice = bangGiaDAO.getGiaHienTai(m.getMaMon());
                if (dynamicPrice > 0) displayPrice = dynamicPrice;
            } catch (Exception e) {}

            DecimalFormat df = new DecimalFormat("#,### VNĐ");
            JLabel lblGia = new JLabel(df.format(displayPrice));
            lblGia.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblGia.setForeground(UIStyle.DANGER_DARK);

            pnlInfo.add(lblTen);
            pnlInfo.add(lblGia);
            add(pnlInfo, BorderLayout.SOUTH);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (selectedMonAn != monAn) setBorder(BorderFactory.createLineBorder(UIStyle.PRIMARY, 2));
                }
                public void mouseExited(MouseEvent e) {
                    if (selectedMonAn != monAn) setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
                }
                public void mouseClicked(MouseEvent e) {
                    selectedMonAn = monAn;
                    for (java.awt.Component c : pnlDanhSach.getComponents()) {
                        if (c instanceof ItemMonAn) ((ItemMonAn) c).setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
                    }
                    setBorder(BorderFactory.createLineBorder(UIStyle.DANGER, 3));
                }
            });
        }
    }
}