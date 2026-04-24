package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import DAO.NhanVienDAO;
import Entity.NhanVien;

/**
 * ChiTietNhanVienDialog — Popup hồ sơ nhân viên chi tiết, hiện đại.
 * Lấy cảm hứng từ các hệ thống CRM/POS hàng đầu.
 * Hiển thị: thông tin cá nhân, chức vụ, số hóa đơn phục vụ,
 * tổng doanh thu, và lịch sử hóa đơn gần nhất.
 */
public class ChiTietNhanVienDialog extends JDialog {

    // ─── COLOR PALETTE ─────────────────────────────────────────────────
    private static final Color BG_MAIN = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY_COLOR = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    // Role colors
    private static final Color ROLE_MANAGER = new Color(99, 102, 241);      // Indigo
    private static final Color ROLE_MANAGER_BG = new Color(238, 242, 255);
    private static final Color ROLE_STAFF = new Color(16, 185, 129);        // Emerald
    private static final Color ROLE_STAFF_BG = new Color(236, 253, 245);

    // Status colors
    private static final Color STATUS_ACTIVE = new Color(34, 197, 94);
    private static final Color STATUS_ACTIVE_BG = new Color(240, 253, 244);
    private static final Color STATUS_INACTIVE = new Color(239, 68, 68);
    private static final Color STATUS_INACTIVE_BG = new Color(254, 242, 242);

    // Stat card colors
    private static final Color STAT_BLUE = new Color(59, 130, 246);
    private static final Color STAT_GREEN = new Color(16, 185, 129);
    private static final Color STAT_PURPLE = new Color(139, 92, 246);
    private static final Color STAT_AMBER = new Color(245, 158, 11);

    private NhanVienDAO dao;
    private NhanVien nv;
    private DecimalFormat moneyFmt = new DecimalFormat("#,##0");
    private SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private SimpleDateFormat dateOnlyFmt = new SimpleDateFormat("dd/MM/yyyy");

    public ChiTietNhanVienDialog(JPanel parent, String maNV, NhanVienDAO dao) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), true);
        this.dao = dao;
        this.nv = dao.getByMaNV(maNV);

        if (nv == null) {
            JOptionPane.showMessageDialog(parent, "Không tìm thấy thông tin nhân viên!");
            dispose();
            return;
        }

        setTitle("Hồ sơ nhân viên — " + nv.getTenNV());
        setSize(720, 700);
        setLocationRelativeTo(parent);
        setResizable(false);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 720, 700, 20, 20));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_MAIN);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(0, 0, 0, 0)));
        setContentPane(mainPanel);

        // ─── TITLE BAR ──────────────────────────────────────────────
        mainPanel.add(createTitleBar(), BorderLayout.NORTH);

        // ─── SCROLLABLE CONTENT ─────────────────────────────────────
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_MAIN);
        content.setBorder(new EmptyBorder(0, 24, 24, 24));

        content.add(createProfileHeader());
        content.add(Box.createVerticalStrut(20));
        content.add(createStatsCards());
        content.add(Box.createVerticalStrut(20));
        content.add(createInfoSection());
        content.add(Box.createVerticalStrut(20));
        content.add(createRecentTransactions());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(BG_MAIN);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    // ═════════════════════════════════════════════════════════════════════
    // TITLE BAR (draggable, with close button)
    // ═════════════════════════════════════════════════════════════════════
    private JPanel createTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(CARD_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(12, 20, 12, 20)));

        JLabel title = new JLabel("HỒ SƠ NHÂN VIÊN");
        title.setIcon(getIcon("src/view/icons/user.png", 24, 24));
        title.setIconTextGap(10);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_PRIMARY_COLOR);
        bar.add(title, BorderLayout.WEST);

        JButton btnClose = new JButton("✕");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnClose.setForeground(TEXT_SECONDARY);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        btnClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnClose.setForeground(new Color(239, 68, 68));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnClose.setForeground(TEXT_SECONDARY);
            }
        });
        bar.add(btnClose, BorderLayout.EAST);

        // Drag support
        final Point[] dragPoint = { null };
        bar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragPoint[0] = e.getPoint();
            }

            public void mouseReleased(MouseEvent e) {
                dragPoint[0] = null;
            }
        });
        bar.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragPoint[0] != null) {
                    Point loc = getLocation();
                    setLocation(loc.x + e.getX() - dragPoint[0].x,
                            loc.y + e.getY() - dragPoint[0].y);
                }
            }
        });

        return bar;
    }

    // ═════════════════════════════════════════════════════════════════════
    // PROFILE HEADER (Avatar + Name + Role Badge + Contact Info)
    // ═════════════════════════════════════════════════════════════════════
    private JPanel createProfileHeader() {
        String vaiTro = (nv.getTaiKhoan() != null) ? nv.getTaiKhoan().getVaiTro() : "Nhân viên";
        boolean isManager = "Quản lý".equals(vaiTro);
        Color roleColor = isManager ? ROLE_MANAGER : ROLE_STAFF;
        Color roleBg = isManager ? ROLE_MANAGER_BG : ROLE_STAFF_BG;

        String trangThai = nv.getTrangThai() != null ? nv.getTrangThai() : "Đang làm việc";
        boolean isActive = "Đang làm việc".equals(trangThai);

        JPanel card = createCard();
        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(),
                new EmptyBorder(24, 24, 24, 24)));

        // --- LEFT: Avatar ---
        final Color avatarColor = roleColor;
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int size = 80;
                int x = (getWidth() - size) / 2;
                int y = 5;

                // Avatar circle with gradient
                GradientPaint gp = new GradientPaint(x, y, avatarColor, x + size, y + size, avatarColor.darker());
                g2.setPaint(gp);
                g2.fill(new Ellipse2D.Double(x, y, size, size));

                // Initials
                String initials = getInitials(nv.getTenNV());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (size - fm.stringWidth(initials)) / 2;
                int textY = y + (size + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initials, textX, textY);

                // Role icon at bottom-right of avatar
                String icon = isManager ? "👔" : "🧑‍💼";
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                g2.drawString(icon, x + size - 20, y + size + 2);

                g2.dispose();
            }
        };
        avatarPanel.setPreferredSize(new Dimension(100, 100));
        avatarPanel.setOpaque(false);
        card.add(avatarPanel, BorderLayout.WEST);

        // --- CENTER: Info ---
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        // Name
        JLabel lblName = new JLabel(nv.getTenNV());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblName.setForeground(TEXT_PRIMARY_COLOR);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(lblName);
        infoPanel.add(Box.createVerticalStrut(6));

        // Role badge
        String roleIcon = isManager ? "👔" : "🧑‍💼";
        JLabel lblRole = new JLabel("  " + roleIcon + "  " + vaiTro.toUpperCase() + "  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(roleBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRole.setForeground(roleColor);
        lblRole.setOpaque(false);
        lblRole.setBorder(new EmptyBorder(4, 8, 4, 8));
        lblRole.setMaximumSize(new Dimension(200, 28));
        lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(lblRole);
        infoPanel.add(Box.createVerticalStrut(10));

        // Contact info
        JPanel contactPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        contactPanel.setOpaque(false);
        contactPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (nv.getSoDienThoai() != null && !nv.getSoDienThoai().isEmpty()) {
            contactPanel.add(createInfoChip("📱", nv.getSoDienThoai()));
        }
        if (nv.getEmail() != null && !nv.getEmail().isEmpty()) {
            contactPanel.add(createInfoChip("📧", nv.getEmail()));
        }
        contactPanel.add(createInfoChip("🆔", nv.getMaNV()));

        infoPanel.add(contactPanel);
        card.add(infoPanel, BorderLayout.CENTER);

        // --- RIGHT: Status badge ---
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setOpaque(false);

        Color statusColor = isActive ? STATUS_ACTIVE : STATUS_INACTIVE;
        Color statusBg = isActive ? STATUS_ACTIVE_BG : STATUS_INACTIVE_BG;
        String statusIcon = isActive ? "✅" : "⛔";

        JLabel lblStatus = new JLabel(statusIcon + " " + trangThai) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(statusBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus.setForeground(statusColor);
        lblStatus.setOpaque(false);
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatus.setBorder(new EmptyBorder(8, 14, 8, 14));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusPanel.add(lblStatus);

        card.add(statusPanel, BorderLayout.EAST);

        return card;
    }

    // ═════════════════════════════════════════════════════════════════════
    // STATS CARDS (3 cards: Invoices, Revenue, Last Active)
    // ═════════════════════════════════════════════════════════════════════
    private JPanel createStatsCards() {
        Object[] stats = dao.getThongTinChiTiet(nv.getMaNV());
        int soHoaDon = (int) stats[0];
        double tongDoanhThu = (double) stats[1];
        java.sql.Timestamp lanLamCuoi = (java.sql.Timestamp) stats[2];

        JPanel wrapper = new JPanel(new GridLayout(1, 3, 14, 0));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        wrapper.add(createStatCard("📋", "Hóa đơn phục vụ",
                String.valueOf(soHoaDon), "hóa đơn", STAT_BLUE));
        wrapper.add(createStatCard("💰", "Tổng doanh thu",
                moneyFmt.format(tongDoanhThu), "VNĐ", STAT_GREEN));

        String lanCuoiStr = lanLamCuoi != null ? dateFmt.format(lanLamCuoi) : "Chưa có";
        wrapper.add(createStatCard("🕐", "Lần phục vụ cuối",
                lanCuoiStr, "", STAT_PURPLE));

        return wrapper;
    }

    private JPanel createStatCard(String icon, String title, String value, String unit, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                // Left accent stripe
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(14, 16, 14, 14)));

        // Icon + Title
        JLabel lblTitle = new JLabel(icon + "  " + title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTitle.setForeground(TEXT_SECONDARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(8));

        // Value - truncate if too long
        String displayValue = value;
        if (displayValue.length() > 14) {
            displayValue = displayValue.substring(0, 12) + "..";
        }
        JLabel lblValue = new JLabel(displayValue);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblValue.setForeground(TEXT_PRIMARY_COLOR);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblValue.setToolTipText(value);
        card.add(lblValue);

        if (!unit.isEmpty()) {
            JLabel lblUnit = new JLabel(unit);
            lblUnit.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblUnit.setForeground(TEXT_MUTED);
            lblUnit.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(lblUnit);
        }

        return card;
    }

    // ═════════════════════════════════════════════════════════════════════
    // INFO SECTION (Personal details)
    // ═════════════════════════════════════════════════════════════════════
    private JPanel createInfoSection() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(),
                new EmptyBorder(20, 24, 20, 24)));

        // Section title
        JLabel lblSection = new JLabel("📝  THÔNG TIN CÁ NHÂN");
        lblSection.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSection.setForeground(TEXT_PRIMARY_COLOR);
        lblSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblSection);
        card.add(Box.createVerticalStrut(16));

        // Info grid
        JPanel grid = new JPanel(new GridLayout(0, 2, 16, 12));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        addInfoRow(grid, "Mã nhân viên", nv.getMaNV());
        addInfoRow(grid, "Giới tính", nv.getGioiTinh() != null ? nv.getGioiTinh() : "Nam");
        addInfoRow(grid, "CCCD/CMND", nv.getCccd() != null && !nv.getCccd().isEmpty() ? nv.getCccd() : "—");
        addInfoRow(grid, "Số điện thoại", nv.getSoDienThoai() != null ? nv.getSoDienThoai() : "—");
        addInfoRow(grid, "Email", nv.getEmail() != null ? nv.getEmail() : "—");
        addInfoRow(grid, "Ngày vào làm",
                nv.getNgayVaoLam() != null ? dateOnlyFmt.format(nv.getNgayVaoLam()) : "—");

        card.add(grid);

        return card;
    }

    private void addInfoRow(JPanel grid, String label, String value) {
        // Label
        JPanel rowPanel = new JPanel(new BorderLayout(8, 0));
        rowPanel.setOpaque(false);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLabel.setForeground(TEXT_SECONDARY);
        rowPanel.add(lblLabel, BorderLayout.WEST);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblValue.setForeground(TEXT_PRIMARY_COLOR);
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);
        rowPanel.add(lblValue, BorderLayout.EAST);

        grid.add(rowPanel);
    }

    // ═════════════════════════════════════════════════════════════════════
    // RECENT TRANSACTIONS TABLE
    // ═════════════════════════════════════════════════════════════════════
    private JPanel createRecentTransactions() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(),
                new EmptyBorder(20, 24, 20, 24)));

        // Section title
        JLabel lblSection = new JLabel("📋  HÓA ĐƠN PHỤC VỤ GẦN ĐÂY");
        lblSection.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSection.setForeground(TEXT_PRIMARY_COLOR);
        lblSection.setBorder(new EmptyBorder(0, 0, 14, 0));
        card.add(lblSection, BorderLayout.NORTH);

        // Table
        String[] headers = { "Mã HĐ", "Thời gian", "Bàn", "Khách hàng", "Thành tiền (VNĐ)" };
        DefaultTableModel tblModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        ArrayList<Object[]> hoaDonList = dao.getHoaDonPhucVu(nv.getMaNV());
        for (Object[] row : hoaDonList) {
            java.sql.Timestamp ts = (java.sql.Timestamp) row[1];
            tblModel.addRow(new Object[] {
                    "#" + row[0],
                    ts != null ? dateFmt.format(ts) : "",
                    row[3] != null ? row[3] : "—",
                    row[4] != null ? row[4] : "—",
                    moneyFmt.format((double) row[2])
            });
        }

        JTable table = new JTable(tblModel);
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(TEXT_PRIMARY_COLOR);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(249, 250, 251));
        header.setForeground(TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(0, 36));

        // Alternate row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));

                // Right-align money column
                if (col == 4) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return c;
            }
        });

        // If no data
        if (hoaDonList.isEmpty()) {
            JPanel emptyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            emptyPanel.setBackground(CARD_BG);
            emptyPanel.setBorder(new EmptyBorder(30, 0, 30, 0));
            JLabel lblEmpty = new JLabel("📭  Chưa có hóa đơn nào");
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lblEmpty.setForeground(TEXT_MUTED);
            emptyPanel.add(lblEmpty);
            card.add(emptyPanel, BorderLayout.CENTER);
        } else {
            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            scroll.getViewport().setBackground(CARD_BG);
            int tableHeight = Math.min(hoaDonList.size() * 36 + 40, 220);
            scroll.setPreferredSize(new Dimension(0, tableHeight));
            card.add(scroll, BorderLayout.CENTER);
        }

        return card;
    }

    // ═════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═════════════════════════════════════════════════════════════════════

    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1000));
        return card;
    }

    private JLabel createInfoChip(String icon, String text) {
        JLabel lbl = new JLabel(icon + "  " + text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty())
            return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return ("" + parts[0].charAt(0)).toUpperCase();
    }

    private ImageIcon getIcon(String path, int width, int height) {
        try {
            ImageIcon imageIcon = new ImageIcon(path);
            Image image = imageIcon.getImage();
            Image newimg = image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
            return new ImageIcon(newimg);
        } catch (Exception e) {
            return null;
        }
    }
}
