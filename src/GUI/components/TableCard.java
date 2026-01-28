package GUI.components;

import Entity.Ban;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * TableCard - Visual representation using PNG images
 * Shows booking info overlay for reserved tables
 */
public class TableCard extends JPanel {

    private Ban table;
    private boolean selected = false;
    private TableCardListener listener;

    private JLabel lblTableImage;
    private JLabel lblName;
    private JLabel lblInfo1;
    private JLabel lblInfo2;
    private JLabel lblInfo3;

    public TableCard(Ban table) {
        this.table = table;
        initUI();
        addInteractions();
    }

    private void initUI() {
        setLayout(new BorderLayout(5, 5));
        setPreferredSize(new Dimension(140, 160)); // Increased from 120x140
        setMaximumSize(new Dimension(140, 160)); // Prevent stretching beyond this
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 2));

        // Top: Table name
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        pnlTop.setBorder(new EmptyBorder(8, 8, 5, 8));

        lblName = new JLabel(table.getTenBan());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(new Color(31, 41, 55));
        pnlTop.add(lblName, BorderLayout.WEST);

        // Center: PNG Image + Info overlay
        JPanel pnlCenter = new JPanel();
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(new EmptyBorder(0, 8, 5, 8));

        // PNG Table Image
        lblTableImage = new JLabel();
        lblTableImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTableImage.setHorizontalAlignment(JLabel.CENTER);
        loadTableImage();
        pnlCenter.add(lblTableImage);
        pnlCenter.add(Box.createVerticalStrut(5));

        // Info labels (will show different content based on status)
        lblInfo1 = new JLabel("", JLabel.CENTER);
        lblInfo1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblInfo1.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblInfo2 = new JLabel("", JLabel.CENTER);
        lblInfo2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblInfo2.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblInfo3 = new JLabel("", JLabel.CENTER);
        lblInfo3.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblInfo3.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlCenter.add(lblInfo1);
        pnlCenter.add(lblInfo2);
        pnlCenter.add(lblInfo3);

        // Bottom: Capacity
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        pnlBottom.setOpaque(false);
        pnlBottom.setBorder(new EmptyBorder(0, 8, 8, 8));

        JLabel lblCapacity = new JLabel(table.getSoGhe() + " chỗ");
        lblCapacity.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCapacity.setForeground(new Color(107, 114, 128));
        pnlBottom.add(lblCapacity);

        add(pnlTop, BorderLayout.NORTH);
        add(pnlCenter, BorderLayout.CENTER);
        add(pnlBottom, BorderLayout.SOUTH);

        updateInfoDisplay();
    }

    private void loadTableImage() {
        String imagePath = "view/icons/tables/";
        String imageFile = getImageFileName();

        try {
            ImageIcon icon = new ImageIcon(imagePath + imageFile);
            if (icon.getIconWidth() > 0) {
                Image scaled = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                lblTableImage.setIcon(new ImageIcon(scaled));
            } else {
                // Fallback if image not found
                lblTableImage.setText("🪑");
                lblTableImage.setFont(new Font("Segoe UI", Font.PLAIN, 36));
            }
        } catch (Exception e) {
            // Fallback
            lblTableImage.setText("🪑");
            lblTableImage.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        }
    }

    private String getImageFileName() {
        String status = table.getTrangThai();

        switch (status) {
            case "Trống":
                return "table_green.png";
            case "Có Khách":
                return "table_orange.png";
            case "Đã Đặt":
                return "table_yellow.png";
            case "Đang Gộp":
                return "table_orange.png"; // Same as occupied
            default:
                return "table_gray.png";
        }
    }

    private void updateInfoDisplay() {
        String status = table.getTrangThai();

        switch (status) {
            case "Trống":
                lblInfo1.setText("TRỐNG");
                lblInfo1.setForeground(new Color(34, 197, 94));
                lblInfo2.setText("");
                lblInfo3.setText("");
                break;

            case "Có Khách":
                lblInfo1.setText("CÓ KHÁCH");
                lblInfo1.setForeground(new Color(239, 68, 68));
                lblInfo2.setText(""); // Can add bill total if available
                lblInfo3.setText("");
                break;

            case "Đã Đặt":
                // TODO: Get booking info from database
                lblInfo1.setText("📅 19:30"); // Booking time
                lblInfo1.setForeground(new Color(245, 158, 11));
                lblInfo2.setText("👤 Nguyễn A"); // Customer name
                lblInfo2.setForeground(new Color(107, 114, 128));
                lblInfo3.setText("☎ 090..."); // Phone
                lblInfo3.setForeground(new Color(107, 114, 128));
                break;

            case "Đang Gộp":
                lblInfo1.setText("ĐANG GỘP");
                lblInfo1.setForeground(new Color(249, 115, 22));
                lblInfo2.setText("");
                lblInfo3.setText("");
                break;

            default:
                lblInfo1.setText(status.toUpperCase());
                lblInfo1.setForeground(new Color(107, 114, 128));
                lblInfo2.setText("");
                lblInfo3.setText("");
        }
    }

    private void addInteractions() {
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!selected) {
                    setBorder(BorderFactory.createLineBorder(new Color(59, 130, 246), 2));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!selected) {
                    setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 2));
                }
            }
        });
    }

    private void handleClick() {
        setSelected(!selected);
        if (listener != null) {
            listener.onTableCardClicked(table);
        }
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            setBorder(BorderFactory.createLineBorder(new Color(59, 130, 246), 3));
        } else {
            setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 2));
        }
    }

    public void setTableCardListener(TableCardListener listener) {
        this.listener = listener;
    }

    public Ban getTable() {
        return table;
    }

    public void updateStatus() {
        loadTableImage();
        updateInfoDisplay();
        repaint();
    }

    public void setBookingInfo(String time, String customerName, String phone) {
        if ("Đã Đặt".equals(table.getTrangThai())) {
            lblInfo1.setText("📅 " + time);
            lblInfo2.setText("👤 " + customerName);
            lblInfo3.setText("☎ " + phone);
        }
    }

    // Listener interface
    public interface TableCardListener {
        void onTableCardClicked(Ban table);
    }
}
