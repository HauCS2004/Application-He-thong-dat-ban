package GUI;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import GUI.components.TableFloorPanel;
import GUI.components.TableCard;
import Entity.Ban;

/**
 * QuanLyBan - Visual Table Management Screen
 * NEW: Using TableCard components with color-coded status
 */
public class QuanLyBanVisual extends JPanel {

    private JTabbedPane tabFloors;
    private TableFloorPanel currentFloorPanel;
    private JPanel pnlDetails;
    private JLabel lblSelectedTable;
    private JLabel lblStatus;
    private JLabel lblInfo;
    private JLabel lblZone; // Add zone label
    private JButton btnAction1, btnAction2, btnRefresh;
    private Ban selectedTable;

    public QuanLyBanVisual() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(249, 250, 251));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top: Header with title and refresh button
        JPanel pnlHeader = createHeader();
        add(pnlHeader, BorderLayout.NORTH);

        // Center: Tabbed floor panels
        tabFloors = new JTabbedPane();
        tabFloors.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Add floor tabs
        addFloorTab("Tầng G", "KV01");
        addFloorTab("Tầng 1", "KV02");
        addFloorTab("VIP Room", "KV03");
        addFloorTab("Ngoài trời", "KV04");

        add(tabFloors, BorderLayout.CENTER);

        // Right: Details panel
        pnlDetails = createDetailsPanel();
        add(pnlDetails, BorderLayout.EAST);
    }

    private JPanel createHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblTitle = new JLabel("📍 Quản Lý Bàn");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(31, 41, 55));

        btnRefresh = new JButton("🔄 Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> refreshCurrentFloor());

        pnl.add(lblTitle, BorderLayout.WEST);
        pnl.add(btnRefresh, BorderLayout.EAST);

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

        btnAction1 = createActionButton("✏ Chỉnh Sửa", new Color(59, 130, 246));
        btnAction2 = createActionButton("🗑 Xóa Bàn", new Color(239, 68, 68));

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

        // Deselect all cards in current floor
        int selectedTab = tabFloors.getSelectedIndex();
        TableFloorPanel panel = (TableFloorPanel) tabFloors.getComponentAt(selectedTab);
        panel.deselectAllCards();

        // Update details panel with table info
        lblSelectedTable.setText(table.getTenBan());
        lblStatus.setText("Trạng thái: " + table.getTrangThai());
        lblInfo.setText("Sức chứa: " + table.getSoGhe() + " người");
        lblZone.setText("Khu vực: " + table.getMaKV()); // Fixed: getMaKV() not getMaKhuVuc()

        // Show CRUD buttons
        btnAction1.setVisible(true);
        btnAction2.setVisible(true);
    }

    private void handleEditTable() {
        if (selectedTable != null) {
            JOptionPane.showMessageDialog(this,
                    "Chức năng Chỉnh Sửa bàn " + selectedTable.getTenBan() + "\n(Sẽ được implement)",
                    "Chỉnh Sửa Bàn",
                    JOptionPane.INFORMATION_MESSAGE);
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
                JOptionPane.showMessageDialog(this,
                        "Đã xóa bàn " + selectedTable.getTenBan() + " (demo)",
                        "Thành Công",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void refreshCurrentFloor() {
        int selectedTab = tabFloors.getSelectedIndex();
        if (selectedTab >= 0) {
            TableFloorPanel panel = (TableFloorPanel) tabFloors.getComponentAt(selectedTab);
            panel.refreshTables();
            JOptionPane.showMessageDialog(this, "Đã làm mới!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
