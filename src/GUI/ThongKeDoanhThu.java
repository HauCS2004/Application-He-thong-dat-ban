package GUI;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Color;

public class ThongKeDoanhThu extends JPanel {
    public ThongKeDoanhThu() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        JLabel lbl = new JLabel("MÀN HÌNH THỐNG KÊ DOANH THU (Đang phát triển)", JLabel.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 20));
        add(lbl, BorderLayout.CENTER);
    }
}
