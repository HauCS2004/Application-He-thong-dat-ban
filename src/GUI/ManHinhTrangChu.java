package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import UTILS.XImage; // Nhớ import cái này để lấy ảnh

public class ManHinhTrangChu extends JPanel {

    public ManHinhTrangChu() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(240, 240, 240)); // Màu nền xám nhẹ
        setBorder(new EmptyBorder(30, 30, 30, 30)); // Căn lề 4 phía

        // --- PHẦN 1: CÁC THẺ THỐNG KÊ (STAT CARDS) ---
        JPanel pnlStats = new JPanel(new GridLayout(1, 4, 30, 0)); // 1 dòng, 4 cột, cách nhau 30px
        pnlStats.setBackground(new Color(240, 240, 240));
        pnlStats.setPreferredSize(new java.awt.Dimension(0, 180)); // Chiều cao cố định 180px

        // Tạo 4 thẻ màu sắc
        // Card 1: Doanh thu (Màu Cam)
        pnlStats.add(createCard("Doanh Thu Ngày", "5.250.000 VNĐ", "icon_money.png", new Color(255, 159, 67)));
        
        // Card 2: Hóa đơn (Màu Xanh Dương)
        pnlStats.add(createCard("Đơn Hàng Mới", "24 Đơn", "icon_bill.png", new Color(52, 152, 219)));
        
        // Card 3: Khách hàng (Màu Tím)
        pnlStats.add(createCard("Khách Hàng", "18 Khách", "icon_user.png", new Color(155, 89, 182)));
        
        // Card 4: Bàn đang mở (Màu Xanh Lá)
        pnlStats.add(createCard("Bàn Đang Mở", "6/20 Bàn", "icon_table.png", new Color(46, 204, 113)));

        add(pnlStats, BorderLayout.NORTH);

// --- PHẦN 2: HÌNH ẢNH TRUNG TÂM (CENTER) ---
        
        // 1. Lấy ảnh gốc (Không cần resize trước)
        ImageIcon icon = UTILS.XImage.read("bg_restaurant.jpg"); 
        final Image imgBot = (icon != null) ? icon.getImage() : null;

        // 2. Tạo một Panel đặc biệt tự vẽ hình nền
        JPanel pnlBackground = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                // Nếu có ảnh thì vẽ ảnh full kích thước panel
                if (imgBot != null) {
                    // Vẽ từ tọa độ (0,0) đến (chiều rộng, chiều cao) hiện tại của Panel
                    g.drawImage(imgBot, 0, 0, this.getWidth(), this.getHeight(), this);
                }
            }
        };
        
        // Cấu hình panel nền
        pnlBackground.setLayout(new java.awt.BorderLayout()); // Để sau này add gì vào giữa cũng được
        pnlBackground.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 5)); // Viền khung tranh

        // Nếu không có ảnh thì hiện chữ (dự phòng)
        if (imgBot == null) {
            JLabel lblText = new JLabel("HỆ THỐNG QUẢN LÝ NHÀ HÀNG", javax.swing.SwingConstants.CENTER);
            lblText.setFont(new Font("Segoe UI", Font.BOLD, 30));
            lblText.setForeground(Color.GRAY);
            pnlBackground.add(lblText, BorderLayout.CENTER);
        }

        add(pnlBackground, BorderLayout.CENTER);
    }

    // --- HÀM TẠO THẺ THỐNG KÊ ĐẸP ---
    private JPanel createCard(String title, String value, String iconName, Color bgColor) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(bgColor);
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20)); // Padding bên trong thẻ
        
        // Tiêu đề nhỏ ở trên
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(255, 255, 255, 200)); // Màu trắng hơi mờ
        pnl.add(lblTitle, BorderLayout.NORTH);

        // Giá trị to ở giữa
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 32)); // Chữ số thật to
        lblValue.setForeground(Color.WHITE);
        pnl.add(lblValue, BorderLayout.CENTER);
        
        // Icon nằm bên phải (nếu có)
        // JLabel lblIcon = new JLabel(XImage.read(iconName));
        // pnl.add(lblIcon, BorderLayout.EAST);

        return pnl;
    }
}