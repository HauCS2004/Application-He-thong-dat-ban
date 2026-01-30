package GUI;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import GUI.components.TableFloorPanel;
import Entity.Ban;

/**
 * QuanLyBan - Visual Table Management Screen
 * NEW: Using TableCard components with color-coded status
 */
public class QuanLyBanVisual extends JPanel {

    private JTabbedPane tabFloors;
    private JPanel pnlDetails;
    private JLabel lblSelectedTable;
    private JLabel lblStatus;
    private JLabel lblInfo;
    private JLabel lblZone; // Add zone label
    private JButton btnAction1, btnAction2, btnRefresh;
    private Ban selectedTable;

    // DAO
    private DAO.BanDAO banDAO = new DAO.BanDAO();

    public QuanLyBanVisual() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(249, 250, 251));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top: Header với tiêu đề và nút làm mới
        JPanel pnlHeader = createHeader();
        add(pnlHeader, BorderLayout.NORTH);

        // Center: Tab khu vực bàn (Tầng)
        tabFloors = new JTabbedPane();
        tabFloors.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Thêm các tab tầng
        addFloorTab("Tầng G", "KV01");
        addFloorTab("Tầng 1", "KV02");
        addFloorTab("VIP Room", "KV03");
        addFloorTab("Ngoài trời", "KV04");

        add(tabFloors, BorderLayout.CENTER);

        // Right: Panel chi tiết
        pnlDetails = createDetailsPanel();
        add(pnlDetails, BorderLayout.EAST);
    }

    private JPanel createHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblTitle = new JLabel("Quản Lý Bàn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(31, 41, 55));

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtns.setOpaque(false);

        JButton btnAdd = new JButton("Thêm Bàn");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.setBackground(new Color(16, 185, 129)); // Green
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setIcon(GUI.utils.IconHelper.loadIcon("view/icons/add.png")); // New Icon
        btnAdd.addActionListener(e -> handleAddTable());

        btnRefresh = new JButton("Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setIcon(GUI.utils.IconHelper.loadIcon("view/icons/refresh.png")); // New Icon
        btnRefresh.addActionListener(e -> refreshCurrentFloor());

        pnlBtns.add(btnAdd);
        pnlBtns.add(btnRefresh);

        pnl.add(lblTitle, BorderLayout.WEST);
        pnl.add(pnlBtns, BorderLayout.EAST);

        return pnl;
    }

    private void addFloorTab(String tabName, String khuVuc) {
        TableFloorPanel floorPanel = new TableFloorPanel(khuVuc);

        // Set listener for table clicks
        floorPanel.setTableCardListener(table -> {
            handleTableSelected(table);
        });

        tabFloors.addTab(tabName, floorPanel);
    }

    private JPanel createDetailsPanel() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setPreferredSize(new Dimension(280, 0));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                new EmptyBorder(20, 20, 20, 20)));

        // Title
        JLabel lblTitle = new JLabel("Thông Tin Bàn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(31, 41, 55));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.add(lblTitle);
        pnl.add(Box.createVerticalStrut(20));

        // Selected table info
        lblSelectedTable = new JLabel("Chưa chọn bàn");
        lblSelectedTable.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSelectedTable.setForeground(new Color(59, 130, 246));
        lblSelectedTable.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnl.add(lblSelectedTable);
        pnl.add(Box.createVerticalStrut(15));

        // Info rows
        lblStatus = new JLabel("Trạng thái: ---");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(new Color(31, 41, 55));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblInfo = new JLabel("Sức chứa: ---");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblInfo.setForeground(new Color(31, 41, 55));
        lblInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblZone = new JLabel("Khu vực: ---");
        lblZone.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblZone.setForeground(new Color(31, 41, 55));
        lblZone.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnl.add(lblStatus);
        pnl.add(Box.createVerticalStrut(8));
        pnl.add(lblInfo);
        pnl.add(Box.createVerticalStrut(8));
        pnl.add(lblZone);
        pnl.add(Box.createVerticalStrut(25));

        // CRUD Actions
        JPanel pnlActions = new JPanel();
        pnlActions.setLayout(new BoxLayout(pnlActions, BoxLayout.Y_AXIS));
        pnlActions.setOpaque(false);
        pnlActions.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnAction1 = createActionButton("Chỉnh Sửa", new Color(59, 130, 246));
        btnAction1.setIcon(GUI.utils.IconHelper.loadIcon("view/icons/edit.png"));

        btnAction2 = createActionButton("Xóa Bàn", new Color(239, 68, 68));
        btnAction2.setIcon(GUI.utils.IconHelper.loadIcon("view/icons/delete.png"));

        btnAction1.setVisible(false);
        btnAction2.setVisible(false);

        btnAction1.addActionListener(e -> handleEditTable());
        btnAction2.addActionListener(e -> handleDeleteTable());

        pnlActions.add(btnAction1);
        pnlActions.add(Box.createVerticalStrut(10));
        pnlActions.add(btnAction2);

        pnl.add(pnlActions);
        pnl.add(Box.createVerticalGlue());

        return pnl;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void handleTableSelected(Ban table) {
        this.selectedTable = table;

        // Deselect all cards in current floor (Visual only)
        // Note: Ideally refresh logic handles this, but here we just update Right Panel

        // Update details panel with table info
        lblSelectedTable.setText(table.getTenBan());
        lblStatus.setText("Trạng thái: " + table.getTrangThai());
        lblInfo.setText("Sức chứa: " + table.getSoGhe() + " người");
        lblZone.setText("Khu vực: " + table.getMaKV());

        // Show CRUD buttons
        btnAction1.setVisible(true);
        btnAction2.setVisible(true);
    }

    // CRUD Handlers
    private void handleAddTable() {
        showTableDialog(null);
    }

    private void handleEditTable() {
        if (selectedTable != null) {
            showTableDialog(selectedTable);
        }
    }

    private void handleDeleteTable() {
        if (selectedTable != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa bàn " + selectedTable.getTenBan() + "?",
                    "Xác Nhận Xóa",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                if (banDAO.delete(selectedTable.getMaBan())) {
                    JOptionPane.showMessageDialog(this, "Đã xóa bàn thành công!");
                    refreshCurrentFloor();
                    selectedTable = null;
                    btnAction1.setVisible(false);
                    btnAction2.setVisible(false);
                    lblSelectedTable.setText("Chưa chọn bàn");
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa! (Có thể bàn đang có hóa đơn)");
                }
            }
        }
    }

    // Helper Dialog
    private void showTableDialog(Ban ban) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                ban == null ? "Thêm Bàn Mới" : "Sửa Bàn", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel pnlCenter = new JPanel(new GridLayout(4, 2, 10, 10));
        pnlCenter.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtMa = new JTextField(ban != null ? ban.getMaBan() : "");
        JTextField txtTen = new JTextField(ban != null ? ban.getTenBan() : "");
        JTextField txtGhe = new JTextField(ban != null ? String.valueOf(ban.getSoGhe()) : "4");
        String[] khuVucs = { "KV01", "KV02", "KV03", "KV04" };
        JComboBox<String> cboKV = new JComboBox<>(khuVucs);
        if (ban != null)
            cboKV.setSelectedItem(ban.getMaKV());

        pnlCenter.add(new JLabel("Mã Bàn:"));
        pnlCenter.add(txtMa);
        pnlCenter.add(new JLabel("Tên Bàn:"));
        pnlCenter.add(txtTen);
        pnlCenter.add(new JLabel("Khu Vực:"));
        pnlCenter.add(cboKV);
        pnlCenter.add(new JLabel("Số Ghế:"));
        pnlCenter.add(txtGhe);

        if (ban != null)
            txtMa.setEditable(false);

        JButton btnSave = new JButton("Lưu");
        btnSave.addActionListener(e -> {
            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            String kv = cboKV.getSelectedItem().toString();
            int ghe = 4;
            try {
                ghe = Integer.parseInt(txtGhe.getText());
            } catch (Exception ex) {
            }

            if (ma.isEmpty() || ten.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đủ thông tin!");
                return;
            }

            Ban newBan = new Ban(ma, ten, "Trống", kv, ghe, null);
            boolean result;
            if (ban == null) {
                result = banDAO.insert(newBan);
            } else {
                result = banDAO.updateInfo(newBan);
            }

            if (result) {
                JOptionPane.showMessageDialog(dialog, "Lưu thành công!");
                dialog.dispose();
                refreshCurrentFloor();
            } else {
                JOptionPane.showMessageDialog(dialog, "Lỗi khi lưu (Trùng mã?)!");
            }
        });

        JPanel pnlBottom = new JPanel();
        pnlBottom.add(btnSave);

        dialog.add(pnlCenter, BorderLayout.CENTER);
        dialog.add(pnlBottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void refreshCurrentFloor() {
        int selectedTab = tabFloors.getSelectedIndex();
        if (selectedTab >= 0) {
            TableFloorPanel panel = (TableFloorPanel) tabFloors.getComponentAt(selectedTab);
            panel.refreshTables();
        }
    }
}
