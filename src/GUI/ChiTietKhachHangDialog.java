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

import DAO.KhachHangDAO;
import Entity.KhachHang;

/**
 * ChiTietKhachHangDialog — Popup hồ sơ khách hàng chi tiết, hiện đại.
 * Lấy cảm hứng từ các hệ thống CRM/POS hàng đầu (Toast, Square, Lightspeed).
 * Hiển thị: thông tin cá nhân, hạng VIP, điểm, số lần ăn, tổng chi tiêu,
 * ưu đãi, món yêu thích, và lịch sử giao dịch gần nhất.
 */
public class ChiTietKhachHangDialog extends JDialog {

    // ─── COLOR PALETTE ─────────────────────────────────────────────────
    private static final Color BG_MAIN = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY_COLOR = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);

    // Tier colors
    private static final Color BRONZE = new Color(205, 127, 50);
    private static final Color BRONZE_BG = new Color(254, 243, 231);
    private static final Color SILVER = new Color(156, 163, 175);
    private static final Color SILVER_BG = new Color(243, 244, 246);
    private static final Color GOLD = new Color(234, 179, 8);
    private static final Color GOLD_BG = new Color(254, 252, 232);
    private static final Color DIAMOND = new Color(99, 102, 241);
    private static final Color DIAMOND_BG = new Color(238, 242, 255);

    // Stat card colors
    private static final Color STAT_BLUE = new Color(59, 130, 246);
    private static final Color STAT_GREEN = new Color(16, 185, 129);
    private static final Color STAT_PURPLE = new Color(139, 92, 246);
    private static final Color STAT_AMBER = new Color(245, 158, 11);

    private KhachHangDAO dao;
    private KhachHang kh;
    private DecimalFormat moneyFmt = new DecimalFormat("#,##0");
    private SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public ChiTietKhachHangDialog(JPanel parent, String sdt, KhachHangDAO dao) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), true);
        this.dao = dao;
        this.kh = dao.getBySDT(sdt);

        if (kh == null) {
            JOptionPane.showMessageDialog(parent, "Không tìm thấy thông tin khách hàng!");
            dispose();
            return;
        }

        setTitle("Hồ sơ khách hàng — " + kh.getTenKhach());
        setSize(720, 760);
        setLocationRelativeTo(parent);
        setResizable(false);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 720, 760, 20, 20));

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
        content.add(createProgressSection());
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

        // JLabel title = new JLabel("👤 HỒ SƠ KHÁCH HÀNG");
        JLabel title = new JLabel("HỒ SƠ KHÁCH HÀNG");
        // Giả sử bạn để ảnh tại src/assets/icons/customer.png
        title.setIcon(getIcon("src/view/icons/user.png", 24, 24));
        title.setIconTextGap(10); // Tạo khoảng cách giữa ảnh và chữ

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
    // PROFILE HEADER (Avatar + Name + Rank Badge + Contact Info)
    // ═════════════════════════════════════════════════════════════════════
    private JPanel createProfileHeader() {
        String hang = kh.getHangThanhVien();
        Color tierColor = getTierColor(hang);
        Color tierBg = getTierBgColor(hang);

        JPanel card = createCard();
        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(),
                new EmptyBorder(24, 24, 24, 24)));

        // --- LEFT: Avatar ---
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
                GradientPaint gp = new GradientPaint(x, y, tierColor, x + size, y + size, tierColor.darker());
                g2.setPaint(gp);
                g2.fill(new Ellipse2D.Double(x, y, size, size));

                // Initials
                String initials = getInitials(kh.getTenKhach());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (size - fm.stringWidth(initials)) / 2;
                int textY = y + (size + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initials, textX, textY);

                // Tier badge icon at bottom-right of avatar
                String icon = getTierIcon(hang);
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
        JLabel lblName = new JLabel(kh.getTenKhach());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblName.setForeground(TEXT_PRIMARY_COLOR);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(lblName);
        infoPanel.add(Box.createVerticalStrut(6));

        // Tier badge
        JLabel lblTier = new JLabel("  " + getTierIcon(hang) + "  " + hang.toUpperCase() + "  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tierBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblTier.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTier.setForeground(tierColor);
        lblTier.setOpaque(false);
        lblTier.setBorder(new EmptyBorder(4, 8, 4, 8));
        lblTier.setMaximumSize(new Dimension(200, 28));
        lblTier.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(lblTier);
        infoPanel.add(Box.createVerticalStrut(10));

        // Contact info
        JPanel contactPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        contactPanel.setOpaque(false);
        contactPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contactPanel.add(createInfoChip("📱", kh.getSoDienThoai()));
        if (kh.getEmail() != null && !kh.getEmail().isEmpty()) {
            contactPanel.add(createInfoChip("📧", kh.getEmail()));
        }
        if (kh.getNgaySinh() != null) {
            contactPanel.add(createInfoChip("🎂", new SimpleDateFormat("dd/MM/yyyy").format(kh.getNgaySinh())));
        }

        infoPanel.add(contactPanel);
        card.add(infoPanel, BorderLayout.CENTER);

        // --- RIGHT: Discount badge ---
        JPanel discountPanel = new JPanel();
        discountPanel.setLayout(new BoxLayout(discountPanel, BoxLayout.Y_AXIS));
        discountPanel.setOpaque(false);

        int pctGiam = kh.getPhanTramGiam();
        if (pctGiam > 0) {
            JLabel lblDiscount = new JLabel("-" + pctGiam + "%") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    GradientPaint gp = new GradientPaint(0, 0, new Color(239, 68, 68), getWidth(), getHeight(),
                            new Color(220, 38, 38));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblDiscount.setFont(new Font("Segoe UI", Font.BOLD, 20));
            lblDiscount.setForeground(Color.WHITE);
            lblDiscount.setOpaque(false);
            lblDiscount.setHorizontalAlignment(SwingConstants.CENTER);
            lblDiscount.setBorder(new EmptyBorder(10, 18, 10, 18));
            lblDiscount.setAlignmentX(Component.CENTER_ALIGNMENT);
            discountPanel.add(lblDiscount);

            JLabel lblDiscountNote = new JLabel("Ưu đãi");
            lblDiscountNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblDiscountNote.setForeground(TEXT_MUTED);
            lblDiscountNote.setAlignmentX(Component.CENTER_ALIGNMENT);
            discountPanel.add(Box.createVerticalStrut(4));
            discountPanel.add(lblDiscountNote);
        }
        card.add(discountPanel, BorderLayout.EAST);

        return card;
    }

    // ═════════════════════════════════════════════════════════════════════
    // STATS CARDS (4 cards: Points, Visits, Total Spent, Discount)
    // ═════════════════════════════════════════════════════════════════════
    private JPanel createStatsCards() {
        Object[] stats = dao.getThongTinChiTiet(kh.getSoDienThoai());
        int soLanAn = (int) stats[0];
        double tongChiTieu = (double) stats[1];
        java.sql.Timestamp lanAnCuoi = (java.sql.Timestamp) stats[2];
        String monYeuThich = (String) stats[3];

        JPanel wrapper = new JPanel(new GridLayout(1, 4, 14, 0));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        wrapper.add(createStatCard("⭐", "Điểm tích lũy",
                String.valueOf(kh.getDiemTichLuy()), "điểm", STAT_BLUE));
        wrapper.add(createStatCard("🍽️", "Số lần ghé",
                String.valueOf(soLanAn), "lần", STAT_GREEN));
        wrapper.add(createStatCard("💰", "Tổng chi tiêu",
                moneyFmt.format(tongChiTieu), "VNĐ", STAT_PURPLE));

        String lanCuoiStr = lanAnCuoi != null ? dateFmt.format(lanAnCuoi) : "Chưa có";
        wrapper.add(createStatCard("❤️", "Món yêu thích",
                monYeuThich, "", STAT_AMBER));

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

        // Value
        // Truncate if too long
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
    // PROGRESS SECTION (Progress bar toward next rank + last visit)
    // ═════════════════════════════════════════════════════════════════════
    private JPanel createProgressSection() {
        Object[] stats = dao.getThongTinChiTiet(kh.getSoDienThoai());
        java.sql.Timestamp lanAnCuoi = (java.sql.Timestamp) stats[2];

        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                card.getBorder(),
                new EmptyBorder(20, 24, 20, 24)));

        // Section title
        JLabel lblSection = new JLabel("TIẾN TRÌNH THĂNG HẠNG");
        // Giả sử bạn để ảnh tại src/assets/icons/customer.png
        lblSection.setIcon(getIcon("src/assets/icons/cross.png", 24, 24));
        lblSection.setIconTextGap(10); // Tạo khoảng cách giữa ảnh và chữ

        lblSection.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSection.setForeground(TEXT_PRIMARY_COLOR);
        lblSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblSection);
        card.add(Box.createVerticalStrut(16));

        // Determine tier thresholds
        int diem = kh.getDiemTichLuy();
        String currentTier = kh.getHangThanhVien();
        String nextTier;
        int currentThreshold, nextThreshold;

        if (diem >= 1000) {
            nextTier = "Đã đạt hạng cao nhất!";
            currentThreshold = 1000;
            nextThreshold = 1000;
        } else if (diem >= 500) {
            nextTier = "Kim cương";
            currentThreshold = 500;
            nextThreshold = 1000;
        } else if (diem >= 200) {
            nextTier = "Vàng";
            currentThreshold = 200;
            nextThreshold = 500;
        } else {
            nextTier = "Bạc";
            currentThreshold = 0;
            nextThreshold = 200;
        }

        double progress = (nextThreshold == currentThreshold) ? 1.0
                : (double) (diem - currentThreshold) / (nextThreshold - currentThreshold);

        // Tier progress visualization
        JPanel tiersRow = new JPanel(new GridLayout(1, 4, 0, 0));
        tiersRow.setOpaque(false);
        tiersRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        tiersRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        String[] tiers = { "Đồng", "Bạc", "Vàng", "Kim cương" };
        int[] thresholds = { 0, 200, 500, 1000 };
        for (int i = 0; i < tiers.length; i++) {
            JLabel lbl = new JLabel(getTierIcon(tiers[i]) + " " + tiers[i],
                    SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI",
                    tiers[i].equals(currentTier) ? Font.BOLD : Font.PLAIN, 11));
            lbl.setForeground(tiers[i].equals(currentTier) ? getTierColor(tiers[i]) : TEXT_MUTED);
            tiersRow.add(lbl);
        }
        card.add(tiersRow);
        card.add(Box.createVerticalStrut(8));

        // Progress bar
        Color tierColor = getTierColor(currentTier);
        JPanel progressBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int h = getHeight();
                int w = getWidth();

                // Background track
                g2.setColor(new Color(226, 232, 240));
                g2.fillRoundRect(0, 0, w, h, h, h);

                // Filled portion
                int filledW = (int) (w * progress);
                if (filledW > 0) {
                    GradientPaint gp = new GradientPaint(0, 0, tierColor,
                            filledW, 0, tierColor.brighter());
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, filledW, h, h, h);

                    // Glow
                    g2.setColor(new Color(tierColor.getRed(), tierColor.getGreen(),
                            tierColor.getBlue(), 60));
                    g2.fillRoundRect(0, 0, filledW, h, h, h);
                }

                g2.dispose();
            }
        };
        progressBar.setPreferredSize(new Dimension(0, 12));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(progressBar);
        card.add(Box.createVerticalStrut(8));

        // Progress text
        String progressText;
        if (diem >= 1000) {
            progressText = "🎉 Chúc mừng! Bạn đã đạt hạng " + currentTier + " — hạng cao nhất!";
        } else {
            int diemCon = nextThreshold - diem;
            progressText = "Còn " + diemCon + " điểm nữa để lên hạng " + nextTier +
                    "  (" + diem + "/" + nextThreshold + ")";
        }
        JLabel lblProgress = new JLabel(progressText);
        lblProgress.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblProgress.setForeground(TEXT_SECONDARY);
        lblProgress.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblProgress);

        // Last visit info
        card.add(Box.createVerticalStrut(14));
        JPanel lastVisitRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lastVisitRow.setOpaque(false);
        lastVisitRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblLastVisit = new JLabel("🕐  Lần ghé gần nhất: ");
        lblLastVisit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLastVisit.setForeground(TEXT_SECONDARY);
        lastVisitRow.add(lblLastVisit);

        String lastVisitStr = lanAnCuoi != null ? dateFmt.format(lanAnCuoi) : "Chưa có giao dịch";
        JLabel lblLastVisitVal = new JLabel(lastVisitStr);
        lblLastVisitVal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLastVisitVal.setForeground(TEXT_PRIMARY_COLOR);
        lastVisitRow.add(lblLastVisitVal);

        card.add(lastVisitRow);

        return card;
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
        JLabel lblSection = new JLabel("📋  LỊCH SỬ GIAO DỊCH GẦN ĐÂY");
        lblSection.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSection.setForeground(TEXT_PRIMARY_COLOR);
        lblSection.setBorder(new EmptyBorder(0, 0, 14, 0));
        card.add(lblSection, BorderLayout.NORTH);

        // Table
        String[] headers = { "Mã HĐ", "Thời gian", "Bàn", "Thành tiền (VNĐ)" };
        DefaultTableModel tblModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        ArrayList<Object[]> hoaDonList = dao.getHoaDonGanDay(kh.getSoDienThoai());
        for (Object[] row : hoaDonList) {
            java.sql.Timestamp ts = (java.sql.Timestamp) row[1];
            tblModel.addRow(new Object[] {
                    "#" + row[0],
                    ts != null ? dateFmt.format(ts) : "",
                    row[3] != null ? row[3] : "—",
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
                if (col == 3) {
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
            JLabel lblEmpty = new JLabel("📭  Chưa có giao dịch nào");
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

    private Color getTierColor(String tier) {
        return switch (tier) {
            case "Kim cương" -> DIAMOND;
            case "Vàng" -> GOLD;
            case "Bạc" -> SILVER;
            default -> BRONZE;
        };
    }

    private Color getTierBgColor(String tier) {
        return switch (tier) {
            case "Kim cương" -> DIAMOND_BG;
            case "Vàng" -> GOLD_BG;
            case "Bạc" -> SILVER_BG;
            default -> BRONZE_BG;
        };
    }

    private String getTierIcon(String tier) {
        return switch (tier) {
            case "Kim cương" -> "💎";
            case "Vàng" -> "🥇";
            case "Bạc" -> "🥈";
            default -> "🥉";
        };
    }

    private ImageIcon getIcon(String path, int width, int height) {
        try {
            ImageIcon imageIcon = new ImageIcon(path); // Load từ đường dẫn
            Image image = imageIcon.getImage();
            Image newimg = image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH); // Bo tròn và làm mượt
            return new ImageIcon(newimg);
        } catch (Exception e) {
            return null;
        }
    }

}
