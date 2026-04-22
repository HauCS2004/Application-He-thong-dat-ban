package GUI;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import GUI.components.TableFloorPanel;
import Entity.Ban;

/**
 * QuanLyBan - Visual Table Management Screen
 * Calls BanDialog for CRUD operations
 */
public class ManHinhQLBanVisual extends JPanel {

    private JTabbedPane tabFloors;
    private JPanel pnlDetails;
    private JLabel lblSelectedTable;
    private JLabel lblStatus;
    private JLabel lblInfo;
    private JLabel lblZone; // Add zone label
    private JButton btnAction1, btnAction2;
    private Ban selectedTable;

    // DAO
    private DAO.BanDAO banDAO = new DAO.BanDAO();

    public ManHinhQLBanVisual() {
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

        // Tự động làm mới khi chuyển sang tab khu vực khác
        tabFloors.addChangeListener(e -> refreshCurrentFloor());

        // Tự động làm mới khi chuyển màn hình (menu sidebar)
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent e) {
                for (int i = 0; i < tabFloors.getTabCount(); i++) {
                    Component c = tabFloors.getComponentAt(i);
                    if (c instanceof TableFloorPanel) {
                        ((TableFloorPanel) c).refreshTables();
                    }
                }
            }
        });
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

        JButton btnAdd = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.SUCCESS, "THÊM BÀN");
        btnAdd.setIcon(GUI.utils.IconHelper.loadIcon("view/icons/add.png"));
        btnAdd.addActionListener(e -> handleAddTable());

        pnlBtns.add(btnAdd);

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

        btnAction1 = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.WARNING, "Chỉnh Sửa");
        btnAction1.setIcon(GUI.utils.IconHelper.loadIcon("view/icons/edit.png"));
        // Đã bỏ setMaximumSize

        btnAction2 = GUI.utils.UIStyle.button(GUI.utils.UIStyle.BtnType.DANGER, "Xóa Bàn");
        btnAction2.setIcon(GUI.utils.IconHelper.loadIcon("view/icons/delete.png"));
        // Đã bỏ setMaximumSize

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

    private void handleTableSelected(Ban table) {
        this.selectedTable = table;

        lblSelectedTable.setText(table.getTenBan());
        lblStatus.setText("Trạng thái: " + table.getTrangThai());
        lblInfo.setText("Sức chứa: " + table.getSoGhe() + " người");
        lblZone.setText("Khu vực: " + table.getMaKV());

        btnAction1.setVisible(true);
        btnAction2.setVisible(true);
    }

    private void handleAddTable() {
        BanDialog dialog = new BanDialog(this, null, banDAO);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshCurrentFloor();
        }
    }

    private void handleEditTable() {
        if (selectedTable != null) {
            BanDialog dialog = new BanDialog(this, selectedTable, banDAO);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshCurrentFloor();
            }
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

    private void refreshCurrentFloor() {
        int selectedTab = tabFloors.getSelectedIndex();
        if (selectedTab >= 0) {
            TableFloorPanel panel = (TableFloorPanel) tabFloors.getComponentAt(selectedTab);
            panel.refreshTables();
        }
    }
}
