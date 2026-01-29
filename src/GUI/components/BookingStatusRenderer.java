package GUI.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class BookingStatusRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Reset style
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        String status = (String) value;

        if ("Đã nhận bàn".equals(status) || "Hoàn tất".equals(status)) {
            label.setBackground(new Color(220, 252, 231)); // Light Green
            label.setForeground(new Color(22, 163, 74)); // Dark Green
            label.setText("✔ " + status);
        } else if ("Đã xác nhận".equals(status)) {
            label.setBackground(new Color(219, 234, 254)); // Light Blue
            label.setForeground(new Color(37, 99, 235)); // Dark Blue
            label.setText("ℹ " + status);
        } else if ("Chờ xác nhận".equals(status)) {
            label.setBackground(new Color(254, 243, 199)); // Light Orange
            label.setForeground(new Color(217, 119, 6)); // Dark Orange
            label.setText("⏳ " + status);
        } else if ("Đã hủy".equals(status)) {
            label.setBackground(new Color(254, 226, 226)); // Light Red
            label.setForeground(new Color(220, 38, 38)); // Dark Red
            label.setText("✕ " + status);
        } else {
            label.setBackground(Color.WHITE);
            label.setForeground(Color.BLACK);
        }

        if (isSelected) {
            label.setBackground(table.getSelectionBackground());
            label.setForeground(table.getSelectionForeground());
        }

        return label;
    }
}
