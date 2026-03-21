package GUI.utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * UIStyle — Factory tạo component với styling hiện đại, nhất quán toàn app.
 * Áp dụng rounded corners, hover effects, màu sắc chuẩn.
 */
public class UIStyle {

    // ─── BẢNG MÀU ────────────────────────────────────────────────────────────
    public static final Color PRIMARY      = new Color(59, 130, 246);   // Blue
    public static final Color PRIMARY_DARK = new Color(37, 99, 235);
    public static final Color SUCCESS      = new Color(34, 197, 94);    // Green
    public static final Color SUCCESS_DARK = new Color(22, 163, 74);
    public static final Color DANGER       = new Color(239, 68, 68);    // Red
    public static final Color DANGER_DARK  = new Color(220, 38, 38);
    public static final Color WARNING      = new Color(245, 158, 11);   // Amber
    public static final Color NEUTRAL      = new Color(107, 114, 128);  // Gray
    public static final Color NEUTRAL_DARK = new Color(75, 85, 99);
    public static final Color SURFACE      = Color.WHITE;
    public static final Color BORDER       = new Color(229, 231, 235);
    public static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    public static final Color TEXT_MUTED   = new Color(107, 114, 128);

    // ─── BUTTON FACTORY ──────────────────────────────────────────────────────

    public enum BtnType { PRIMARY, SUCCESS, DANGER, WARNING, NEUTRAL, OUTLINE }

    /**
     * Tạo JButton hiện đại với màu và hover effect.
     * @param text  Nhãn nút
     * @param type  Loại màu (PRIMARY, SUCCESS, ...)
     */
    public static JButton button(String type_str, String text) {
        BtnType type;
        try { type = BtnType.valueOf(type_str.toUpperCase()); }
        catch (Exception e) { type = BtnType.PRIMARY; }
        return button(type, text);
    }

    public static JButton button(BtnType type, String text) {
        Color base = colorFor(type);
        Color hover = darken(base);

        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getBackground();
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setContentAreaFilled(false); // Custom paint
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        if (type == BtnType.OUTLINE) {
            btn.setBackground(SURFACE);
            btn.setForeground(PRIMARY);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY, 1, true),
                    new EmptyBorder(7, 16, 7, 16)));
        } else {
            btn.setBackground(base);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        }

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(type == BtnType.OUTLINE ? new Color(239, 246, 255) : hover);
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(type == BtnType.OUTLINE ? SURFACE : base);
            }
        });

        return btn;
    }

    /** Button nhỏ hơn (cho toolbar, table actions) */
    public static JButton buttonSm(BtnType type, String text) {
        JButton btn = button(type, text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        return btn;
    }

    // ─── INPUT FACTORY ────────────────────────────────────────────────────────

    /**
     * Tạo JTextField với viền rounded, padding chuẩn.
     */
    public static JTextField textField(String placeholder, int cols) {
        JTextField tf = new JTextField(cols);
        styleTextField(tf);
        if (placeholder != null)
            tf.putClientProperty("JTextField.placeholderText", placeholder);
        return tf;
    }

    /** Apply styling lên một JTextField đã tồn tại */
    public static void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(7, 12, 7, 12)));
        tf.setBackground(SURFACE);
        tf.setForeground(TEXT_PRIMARY);
    }

    /** Apply styling lên JPasswordField */
    public static void stylePasswordField(JPasswordField pf) {
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(7, 12, 7, 12)));
        pf.setBackground(SURFACE);
        pf.setForeground(TEXT_PRIMARY);
    }

    /** Apply styling lên JComboBox */
    public static void styleComboBox(JComboBox<?> cb) {
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(SURFACE);
        cb.setBorder(BorderFactory.createLineBorder(BORDER, 1));
    }

    // ─── LABEL FACTORY ────────────────────────────────────────────────────────

    public static JLabel title(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel subtitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    public static JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    // ─── PANEL FACTORY ────────────────────────────────────────────────────────

    /** Card trắng với viền nhẹ và padding */
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(16, 16, 16, 16)));
        return p;
    }

    // ─── TABLE STYLING ────────────────────────────────────────────────────────

    public static void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(249, 250, 251));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private static Color colorFor(BtnType type) {
        return switch (type) {
            case SUCCESS -> SUCCESS;
            case DANGER  -> DANGER;
            case WARNING -> WARNING;
            case NEUTRAL -> NEUTRAL;
            default       -> PRIMARY; // PRIMARY, OUTLINE
        };
    }

    private static Color darken(Color c) {
        return switch (colorToType(c)) {
            case SUCCESS -> SUCCESS_DARK;
            case DANGER  -> DANGER_DARK;
            case NEUTRAL -> NEUTRAL_DARK;
            default       -> PRIMARY_DARK;
        };
    }

    private static BtnType colorToType(Color c) {
        if (c.equals(SUCCESS)) return BtnType.SUCCESS;
        if (c.equals(DANGER))  return BtnType.DANGER;
        if (c.equals(NEUTRAL)) return BtnType.NEUTRAL;
        return BtnType.PRIMARY;
    }
}
