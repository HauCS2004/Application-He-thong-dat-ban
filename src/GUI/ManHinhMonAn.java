package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
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

public class ManHinhMonAn extends JPanel {

    // --- Tab 1: Món Ăn Variables ---
    private JTextField txtTimKiem;
    private JButton btnTim;
    private JComboBox<LoaiMon> cboLocLoai;
    private JComboBox<String> cboLocTrangThai;  // Filter theo trạng thái
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

    public ManHinhMonAn() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane mainTabs = new JTabbedPane();
        mainTabs.setFont(new Font("Segoe UI", Font.BOLD, 14));

        mainTabs.addTab("Danh sách Món Ăn", createMonAnTab());
        mainTabs.addTab("Quản lý Bảng Giá", createBangGiaTab());

        mainTabs.addChangeListener(e -> {
            if (mainTabs.getSelectedIndex() == 0) loadDataGrid();
            else if (mainTabs.getSelectedIndex() == 1) loadBangGiaData();
        });

        add(mainTabs, BorderLayout.CENTER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadDataGrid();
                loadBangGiaData();
            }
        });
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
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlSearch.setBackground(new Color(243, 244, 246));

        pnlSearch.add(createLabel("Tìm tên:"));
        txtTimKiem = UIStyle.textField("", 12);
        pnlSearch.add(txtTimKiem);

        btnTim = UIStyle.buttonSm(UIStyle.BtnType.PRIMARY, "Tìm");
        pnlSearch.add(btnTim);

        pnlSearch.add(new JLabel("   Loại:"));
        cboLocLoai = new JComboBox<>();
        UIStyle.styleComboBox(cboLocLoai);
        cboLocLoai.setPreferredSize(new Dimension(140, 32));
        pnlSearch.add(cboLocLoai);

        pnlSearch.add(new JLabel("   Trạng thái:"));
        cboLocTrangThai = new JComboBox<>(new String[]{"Tất cả", MonAn.CON_MON, MonAn.HET_MON, MonAn.NGUNG_BAN});
        cboLocTrangThai.setPreferredSize(new Dimension(130, 32));
        pnlSearch.add(cboLocTrangThai);

        // Actions (East)
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlActions.setBackground(new Color(243, 244, 246));

        JButton btnThem  = UIStyle.button(UIStyle.BtnType.SUCCESS, "THÊM");
        JButton btnSua   = UIStyle.button(UIStyle.BtnType.WARNING, "SỬA");
        JButton btnDoiTT = UIStyle.button(UIStyle.BtnType.PRIMARY, "ĐỔI TRẠNG THÁI");
        JButton btnXoa   = UIStyle.button(UIStyle.BtnType.DANGER,  "XÓA");

        pnlActions.add(btnThem);
        pnlActions.add(btnSua);
        pnlActions.add(btnDoiTT);
        pnlActions.add(btnXoa);

        pnlToolbar.add(pnlSearch, BorderLayout.WEST);
        pnlToolbar.add(pnlActions, BorderLayout.EAST);
        tabPanel.add(pnlToolbar, BorderLayout.NORTH);

        // Center Grid
        pnlDanhSach = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
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
            if (dialog.isSaved()) { selectedMonAn = null; loadDataGrid(); }
        });

        // Đổi trạng thái — dialog hiển thị rõ tên món
        btnDoiTT.addActionListener(e -> {
            if (selectedMonAn == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn món ăn cần đổi trạng thái!");
                return;
            }

            // Label tên món (nổi bật, căn giữa)
            JLabel lblMonTen = new JLabel(selectedMonAn.getTenMon(), JLabel.CENTER);
            lblMonTen.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblMonTen.setForeground(new Color(17, 24, 39));

            // Trạng thái hiện tại (căn giữa)
            JLabel lblCurrent = new JLabel("Trạng thái hiện tại: " + selectedMonAn.getTrangThai(), JLabel.CENTER);
            lblCurrent.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            lblCurrent.setForeground(ManHinhMonAn.statusColor(selectedMonAn.getTrangThai()));

            // Label hướng dẫn
            JLabel lblPrompt = new JLabel("Chọn trạng thái mới:");
            lblPrompt.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            // ComboBox
            JComboBox<String> cbStatus = new JComboBox<>(MonAn.ALL_STATUSES);
            cbStatus.setSelectedItem(selectedMonAn.getTrangThai());
            cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            // Layout: GridBagLayout — mọi hàng đều kéo full width
            JPanel dlgPanel = new JPanel(new java.awt.GridBagLayout());
            dlgPanel.setBorder(new EmptyBorder(12, 20, 12, 20));
            java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
            gbc.gridx = 0; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0; gbc.insets = new java.awt.Insets(0, 0, 0, 0);

            gbc.gridy = 0;
            dlgPanel.add(lblMonTen, gbc);

            gbc.gridy = 1; gbc.insets = new java.awt.Insets(2, 0, 0, 0);
            dlgPanel.add(lblCurrent, gbc);

            gbc.gridy = 2; gbc.insets = new java.awt.Insets(10, 0, 10, 0);
            dlgPanel.add(new JSeparator(), gbc);

            gbc.gridy = 3; gbc.insets = new java.awt.Insets(0, 0, 4, 0);
            dlgPanel.add(lblPrompt, gbc);

            gbc.gridy = 4; gbc.insets = new java.awt.Insets(0, 0, 0, 0);
            dlgPanel.add(cbStatus, gbc);

            int result = JOptionPane.showConfirmDialog(
                    this, dlgPanel,
                    "Đổi trạng thái món ăn",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String newStatus = (String) cbStatus.getSelectedItem();
                if (dao.updateTrangThai(selectedMonAn.getMaMon(), newStatus)) {
                    JOptionPane.showMessageDialog(this,
                            "Đã cập nhật: " + selectedMonAn.getTenMon() + " → " + newStatus);
                    selectedMonAn = null;
                    loadDataGrid();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi cập nhật trạng thái!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Xóa thật — chỉ khi chưa có trong hóa đơn
        btnXoa.addActionListener(e -> {
            if (selectedMonAn == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn món ăn cần xóa!");
                return;
            }
            if (dao.isUsedInInvoice(selectedMonAn.getMaMon())) {
                JOptionPane.showMessageDialog(this,
                        "Không thể xóa!\nMón này đã xuất hiện trong hóa đơn.\n\nHãy dùng nút 'ĐỔI TRẠNG THÁI' → 'Ngừng bán' thay thế.",
                        "Không thể xóa", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Xóa vĩnh viễn món: " + selectedMonAn.getTenMon() + "?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                if (dao.delete(selectedMonAn.getMaMon())) {
                    XImage.delete(selectedMonAn.getHinhAnh());
                    selectedMonAn = null;
                    loadDataGrid();
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi xóa món!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnTim.addActionListener(e -> xuLyTimKiem());
        txtTimKiem.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { xuLyTimKiem(); }
        });
        cboLocLoai.addActionListener(e -> xuLyTimKiem());
        cboLocTrangThai.addActionListener(e -> xuLyTimKiem());

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

        JPanel pnlMaster = new JPanel(new BorderLayout(5, 10));
        pnlMaster.setBackground(Color.WHITE);
        pnlMaster.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "BẢNG GIÁ (MASTER)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 15), UIStyle.PRIMARY));

        JPanel pnlMasterTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlMasterTop.setBackground(Color.WHITE);
        JButton btnThemBG = UIStyle.button(UIStyle.BtnType.SUCCESS, "THÊM BẢNG GIÁ");
        JButton btnSuaBG  = UIStyle.button(UIStyle.BtnType.WARNING, "SỬA BẢNG GIÁ");
        JButton btnXoaBG  = UIStyle.button(UIStyle.BtnType.DANGER,  "XÓA BẢNG GIÁ");
        pnlMasterTop.add(btnThemBG); pnlMasterTop.add(btnSuaBG); pnlMasterTop.add(btnXoaBG);
        pnlMaster.add(pnlMasterTop, BorderLayout.NORTH);

        String[] headerBG = {"Mã CD","Tên CD","Loại","Từ Ngày","Đến Ngày","Giờ BĐ","Giờ KT","Ưu Tiên","Trạng Thái","Ghi Chú"};
        modelBangGia = new DefaultTableModel(headerBG, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblBangGia = new JTable(modelBangGia);
        UIStyle.styleTable(tblBangGia);
        pnlMaster.add(new JScrollPane(tblBangGia), BorderLayout.CENTER);

        JPanel pnlDetail = new JPanel(new BorderLayout(5, 10));
        pnlDetail.setBackground(Color.WHITE);
        pnlDetail.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "MÓN ĂN TRONG BẢNG GIÁ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 15), UIStyle.PRIMARY));

        JPanel pnlDetailTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlDetailTop.setBackground(Color.WHITE);
        JButton btnThemMon = UIStyle.button(UIStyle.BtnType.SUCCESS, "THÊM MÓN VÀO BG");
        JButton btnSuaMon  = UIStyle.button(UIStyle.BtnType.WARNING, "SỬA MÓN");
        JButton btnXoaMon  = UIStyle.button(UIStyle.BtnType.DANGER,  "XÓA MÓN KHỎI BG");
        pnlDetailTop.add(btnThemMon); pnlDetailTop.add(btnSuaMon); pnlDetailTop.add(btnXoaMon);
        pnlDetail.add(pnlDetailTop, BorderLayout.NORTH);

        String[] headerCT = {"Mã Món","Tên Món","Đơn Giá","Ghi Chú"};
        modelChiTiet = new DefaultTableModel(headerCT, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblChiTiet = new JTable(modelChiTiet);
        UIStyle.styleTable(tblChiTiet);
        pnlDetail.add(new JScrollPane(tblChiTiet), BorderLayout.CENTER);

        splitPane.setTopComponent(pnlMaster);
        splitPane.setBottomComponent(pnlDetail);
        tab.add(splitPane, BorderLayout.CENTER);

        loadBangGiaData();

        tblBangGia.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblBangGia.getSelectedRow() != -1) {
                selectedMaBG = Integer.parseInt(tblBangGia.getValueAt(tblBangGia.getSelectedRow(), 0).toString());
                loadChiTietData(selectedMaBG);
            }
        });

        btnThemBG.addActionListener(e -> {
            BangGiaDialog dialog = new BangGiaDialog(this, null, bangGiaDAO);
            dialog.setVisible(true);
            if (dialog.isSaved()) loadBangGiaData();
        });
        btnSuaBG.addActionListener(e -> {
            if (selectedMaBG == -1) { JOptionPane.showMessageDialog(this, "Chọn chiến dịch cần sửa!"); return; }
            BangGia bg = bangGiaDAO.getAll().stream().filter(b -> b.getMaBG() == selectedMaBG).findFirst().orElse(null);
            if (bg != null) {
                BangGiaDialog dialog = new BangGiaDialog(this, bg, bangGiaDAO);
                dialog.setVisible(true);
                if (dialog.isSaved()) loadBangGiaData();
            }
        });
        btnXoaBG.addActionListener(e -> {
            if (selectedMaBG == -1) { JOptionPane.showMessageDialog(this, "Chọn chiến dịch cần xóa!"); return; }
            if (JOptionPane.showConfirmDialog(this, "Xóa chiến dịch này?") == 0) {
                if (bangGiaDAO.delete(selectedMaBG)) {
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                    loadBangGiaData(); modelChiTiet.setRowCount(0); selectedMaBG = -1;
                } else JOptionPane.showMessageDialog(this, "Không thể xóa. Cần xóa hết món ăn thuộc chiến dịch này trước.");
            }
        });
        btnThemMon.addActionListener(e -> {
            if (selectedMaBG == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn Chiến Dịch Giá ở bảng trên trước!"); return; }
            ChiTietBangGiaDialog dialog = new ChiTietBangGiaDialog(this, selectedMaBG, bangGiaDAO, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) loadChiTietData(selectedMaBG);
        });
        btnSuaMon.addActionListener(e -> {
            int row = tblChiTiet.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn món ăn trong chiến dịch cần sửa!"); return; }
            String maMon = tblChiTiet.getValueAt(row, 0).toString();
            double donGia = 0;
            try { donGia = Double.parseDouble(tblChiTiet.getValueAt(row, 2).toString().replace(",", "")); } catch(Exception ex) {}
            String ghiChu = tblChiTiet.getValueAt(row, 3) != null ? tblChiTiet.getValueAt(row, 3).toString() : "";
            ChiTietBangGia ctEdit = new ChiTietBangGia(selectedMaBG, maMon, donGia, ghiChu);
            ChiTietBangGiaDialog dialog = new ChiTietBangGiaDialog(this, selectedMaBG, bangGiaDAO, ctEdit);
            dialog.setVisible(true);
            if (dialog.isSaved()) loadChiTietData(selectedMaBG);
        });
        btnXoaMon.addActionListener(e -> {
            int row = tblChiTiet.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn món trong bảng chi tiết để xóa!"); return; }
            String maMon = tblChiTiet.getValueAt(row, 0).toString();
            if (bangGiaDAO.deleteChiTiet(selectedMaBG, maMon)) {
                loadChiTietData(selectedMaBG);
            } else JOptionPane.showMessageDialog(this, "Lỗi xóa món!");
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
        String trangThai = "";
        if (cboLocTrangThai.getSelectedItem() != null && !cboLocTrangThai.getSelectedItem().equals("Tất cả")) {
            trangThai = cboLocTrangThai.getSelectedItem().toString();
        }

        ArrayList<MonAn> list = dao.timKiem(keyword, maLoai, trangThai);
        pnlDanhSach.removeAll();
        for (MonAn m : list) {
            pnlDanhSach.add(new ItemMonAn(m));
        }

        int cardW = 235;
        int cardH = 300; // tăng thêm cho badge trạng thái
        int panelW = pnlDanhSach.getWidth();
        int cols = panelW > 0 ? Math.max(1, panelW / cardW) : 4;
        int rows = (int) Math.ceil((double) list.size() / cols);
        pnlDanhSach.setPreferredSize(new java.awt.Dimension(panelW, Math.max(rows * cardH + 20, cardH)));

        pnlDanhSach.revalidate();
        pnlDanhSach.repaint();
    }

    void loadDataGrid() {
        txtTimKiem.setText("");
        if (cboLocLoai.getItemCount() > 0) cboLocLoai.setSelectedIndex(0);
        if (cboLocTrangThai.getItemCount() > 0) cboLocTrangThai.setSelectedIndex(0);
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
            modelBangGia.addRow(new Object[]{
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
        for (ChiTietBangGia ct : bangGiaDAO.getChiTietByMaBG(maBG)) {
            MonAn m = dao.getByMaMon(ct.getMaMon());
            String tenMon = m != null ? m.getTenMon() : "...";
            modelChiTiet.addRow(new Object[]{
                    ct.getMaMon(), tenMon, df.format(ct.getDonGia()),
                    ct.getGhiChu() != null ? ct.getGhiChu() : ""
            });
        }
    }

    // ── Màu trạng thái ────────────────────────────────────────────────────────
    static Color statusColor(String trangThai) {
        if (MonAn.CON_MON.equals(trangThai))   return new Color(22, 163, 74);   // xanh lá
        if (MonAn.HET_MON.equals(trangThai))   return new Color(220, 38, 38);   // đỏ
        if (MonAn.NGUNG_BAN.equals(trangThai)) return new Color(107, 114, 128); // xám
        return new Color(75, 85, 99);
    }

    // --- Inner Class Card Món Ăn ---
    public class ItemMonAn extends JPanel {
        private MonAn monAn;

        public ItemMonAn(MonAn m) {
            this.monAn = m;
            setPreferredSize(new Dimension(220, 285));
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Dim nếu ngừng bán
            boolean isNgungBan = MonAn.NGUNG_BAN.equals(m.getTrangThai());
            if (isNgungBan) setBackground(new Color(248, 248, 248));

            // Ảnh
            JLabel lblImg = new JLabel();
            lblImg.setHorizontalAlignment(JLabel.CENTER);
            ImageIcon icon = XImage.read(m.getHinhAnh());
            if (icon != null && icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(180, 130, Image.SCALE_SMOOTH);
                lblImg.setIcon(new ImageIcon(img));
                if (isNgungBan) {
                    // Làm mờ ảnh bằng alpha tint
                    lblImg.setOpaque(true);
                    lblImg.setBackground(new Color(200, 200, 200, 80));
                }
            } else {
                lblImg.setText("No Image");
                lblImg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblImg.setForeground(Color.GRAY);
                lblImg.setPreferredSize(new Dimension(180, 130));
                lblImg.setOpaque(true);
                lblImg.setBackground(new Color(243, 244, 246));
            }
            add(lblImg, BorderLayout.CENTER);

            // Info bottom
            JPanel pnlInfo = new JPanel(new GridLayout(3, 1, 0, 2));
            pnlInfo.setBackground(isNgungBan ? new Color(248, 248, 248) : new Color(250, 250, 250));
            pnlInfo.setBorder(new EmptyBorder(5, 10, 5, 10));

            JLabel lblTen = new JLabel(m.getTenMon());
            lblTen.setFont(new Font("Segoe UI", Font.BOLD, 14));
            if (isNgungBan) lblTen.setForeground(Color.GRAY);

            double displayPrice = bangGiaDAO.getGiaHienTai(m.getMaMon());
            DecimalFormat df = new DecimalFormat("#,### VNĐ");
            JLabel lblGia;
            if (displayPrice > 0) {
                lblGia = new JLabel(df.format(displayPrice));
                lblGia.setForeground(isNgungBan ? Color.GRAY : UIStyle.DANGER_DARK);
            } else {
                lblGia = new JLabel("Chưa có giá");
                lblGia.setForeground(new Color(150, 150, 150));
            }
            lblGia.setFont(new Font("Segoe UI", Font.BOLD, 13));

            // Badge trạng thái
            JLabel lblStatus = new JLabel("  " + m.getTrangThai() + "  ");
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblStatus.setForeground(Color.WHITE);
            lblStatus.setOpaque(true);
            lblStatus.setBackground(statusColor(m.getTrangThai()));

            pnlInfo.add(lblTen);
            pnlInfo.add(lblGia);
            pnlInfo.add(lblStatus);
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
                        if (c instanceof ItemMonAn)
                            ((ItemMonAn) c).setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
                    }
                    setBorder(BorderFactory.createLineBorder(UIStyle.DANGER, 3));
                }
            });
        }
    }
}