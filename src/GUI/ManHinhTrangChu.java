package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import DAO.ThongKeDAO;

public class ManHinhTrangChu extends JPanel {

    private ThongKeDAO tkDAO = new ThongKeDAO();

    // Label tham chiếu để cập nhật realtime
    private JLabel lblDoanhThu;
    private JLabel lblDonHang;
    private JLabel lblKhach;
    private JLabel lblBanMo;

    public ManHinhTrangChu() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(240, 240, 240));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- PHẦN 1: CÁC THẺ THỐNG KÊ (STAT CARDS) ---
        JPanel pnlStats = new JPanel(new GridLayout(1, 4, 30, 0));
        pnlStats.setBackground(new Color(240, 240, 240));
        pnlStats.setPreferredSize(new java.awt.Dimension(0, 180));

        // Card 1: Doanh thu (Màu Cam)
        JPanel card1 = createCard("Doanh Thu Ngày", "...", new Color(255, 159, 67));
        lblDoanhThu = findValueLabel(card1);
        pnlStats.add(card1);

        // Card 2: Hóa đơn (Màu Xanh Dương)
        JPanel card2 = createCard("Đơn Hàng Hôm Nay", "...", new Color(52, 152, 219));
        lblDonHang = findValueLabel(card2);
        pnlStats.add(card2);

        // Card 3: Khách hàng (Màu Tím)
        JPanel card3 = createCard("Khách Đang Phục Vụ", "...", new Color(155, 89, 182));
        lblKhach = findValueLabel(card3);
        pnlStats.add(card3);

        // Card 4: Bàn đang mở (Màu Xanh Lá)
        JPanel card4 = createCard("Bàn Đang Mở", "...", new Color(46, 204, 113));
        lblBanMo = findValueLabel(card4);
        pnlStats.add(card4);

        add(pnlStats, BorderLayout.NORTH);

        // --- PHẦN 2: HÌNH ẢNH TRUNG TÂM ---
        ImageIcon icon = UTILS.XImage.read("restaurant.jpg");
        final Image imgBot = (icon != null) ? icon.getImage() : null;

        JPanel pnlBackground = new JPanel() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                if (imgBot != null) {
                    g.drawImage(imgBot, 0, 0, this.getWidth(), this.getHeight(), this);
                }
            }
        };

        pnlBackground.setLayout(new java.awt.BorderLayout());
        pnlBackground.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 5));

        if (imgBot == null) {
            JLabel lblText = new JLabel("HỆ THỐNG QUẢN LÝ NHÀ HÀNG", javax.swing.SwingConstants.CENTER);
            lblText.setFont(new Font("Segoe UI", Font.BOLD, 30));
            lblText.setForeground(Color.GRAY);
            pnlBackground.add(lblText, BorderLayout.CENTER);
        }

        add(pnlBackground, BorderLayout.CENTER);

        // --- AUTO REFRESH khi panel được show ---
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshCards();
            }
        });
    }

    /** Cập nhật 4 thẻ thống kê từ DAO thật */
    public void refreshCards() {
        try {
            double doanhThu = tkDAO.getDoanhThuNgay(new java.util.Date());
            int soHoaDon = tkDAO.getSoHoaDonHomNay();
            int soKhach = tkDAO.getSoKhachHomNay();
            int soBan = tkDAO.getSoBanDangMo();

            if (lblDoanhThu != null)
                lblDoanhThu.setText(NumberFormat.getIntegerInstance().format(doanhThu) + " VNĐ");
            if (lblDonHang != null)
                lblDonHang.setText(soHoaDon + " Đơn");
            if (lblKhach != null)
                lblKhach.setText(soKhach + " Khách");
            if (lblBanMo != null)
                lblBanMo.setText(soBan + " Bàn");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- HÀM TẠO THẺ THỐNG KÊ ---
    private JPanel createCard(String title, String value, Color bgColor) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(bgColor);
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(255, 255, 255, 200));
        pnl.add(lblTitle, BorderLayout.NORTH);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(Color.WHITE);
        pnl.add(lblValue, BorderLayout.CENTER);

        return pnl;
    }

    /** Lấy JLabel giá trị (CENTER) trong card để cập nhật sau */
    private JLabel findValueLabel(JPanel card) {
        java.awt.Component center = ((BorderLayout) card.getLayout())
                .getLayoutComponent(BorderLayout.CENTER);
        return (center instanceof JLabel) ? (JLabel) center : null;
    }
}