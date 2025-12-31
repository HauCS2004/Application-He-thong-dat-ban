package GUI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import connectDB.ConnectDB;
// import GUI.QuanLyMonAn; // Bỏ comment dòng này khi bạn đã có file QuanLyMonAn

public class TrangChu extends JFrame {

    private JPanel pnlContent;
    private CardLayout cardLayout;
    
    // Danh sách các mục menu để xử lý hiệu ứng click
    private ArrayList<JLabel> listMenu = new ArrayList<>();

    // MÀU SẮC CHỦ ĐẠO (Giống hình mẫu)
    private Color colorActive = new Color(0, 123, 255); // Màu xanh dương (khi chọn)
    private Color colorNormal = new Color(0, 0, 0);     // Màu đen (bình thường)
    private Color colorBg = Color.WHITE;                // Nền trắng

    public TrangChu() {
       
        ConnectDB.getInstance().connect();
        initGUI();
    }

    private void initGUI() {
        setTitle("QUẢN LÝ NHÀ HÀNG");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // --- 1. THANH MENU (NAVIGATION BAR) ---
        JPanel pnlMenu = new JPanel();
        pnlMenu.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 15)); // Căn trái, khoảng cách giữa các chữ là 30px
        pnlMenu.setBackground(colorBg);
        pnlMenu.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220))); // Kẻ 1 đường mờ ở dưới đáy menu

        // Tạo các mục menu (Text, Tên thẻ card)
        JLabel lblHome = createMenuItem("Trang Chủ", "home");
        JLabel lblMonAn = createMenuItem("Món Ăn", "mon_an");
        JLabel lblBan = createMenuItem("Quản Lý Bàn", "ban");
        JLabel lblGoiMon = createMenuItem("Gọi Món", "goi_mon");
        JLabel lblKhachHang = createMenuItem("Khách Hàng", "khach_hang");
        JLabel lblThoat = createMenuItem("Thoát", "exit");

        // Add vào thanh menu
        pnlMenu.add(lblHome);
        pnlMenu.add(lblMonAn);
        pnlMenu.add(lblBan);
        pnlMenu.add(lblGoiMon);
        pnlMenu.add(lblKhachHang);
        pnlMenu.add(lblThoat);

        add(pnlMenu, BorderLayout.NORTH); // Đặt lên đầu

        // --- 2. PHẦN NỘI DUNG (CONTENT) ---
        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);
        pnlContent.setBackground(new Color(245, 245, 250)); // Màu nền xám cực nhạt cho nội dung đỡ chói

        // >> Tạo các Panel nội dung <<
//        JPanel pnlTrangChu = new JPanel();
//        pnlTrangChu.add(new JLabel("MÀN HÌNH DASHBOARD / TRANG CHỦ"));
        ManHinhTrangChu pnlTrangChu = new ManHinhTrangChu();
        
        // Màn hình Món ăn (Class bạn đã làm)
        QuanLyMonAn pnlQuanLyMonAn = new QuanLyMonAn(); 
//        JPanel pnlQuanLyMonAn = new JPanel(); // (Placeholder)
//        pnlQuanLyMonAn.add(new JLabel("CODE QUẢN LÝ MÓN ĂN Ở ĐÂY"));

        JPanel pnlBan = new JPanel();
        pnlBan.add(new JLabel("MÀN HÌNH SƠ ĐỒ BÀN"));

        JPanel pnlGoiMon = new JPanel();
        pnlGoiMon.add(new JLabel("MÀN HÌNH GỌI MÓN (ORDER)"));

//        JPanel pnlKhachHang = new JPanel();
//        pnlKhachHang.add(new JLabel("MÀN HÌNH KHÁCH HÀNG"));
        QuanLyKhachHang pnlKhachHang = new QuanLyKhachHang();
        // Add vào CardLayout
        pnlContent.add(pnlTrangChu, "home");
        pnlContent.add(pnlQuanLyMonAn, "mon_an");
        pnlContent.add(pnlBan, "ban");
        pnlContent.add(pnlGoiMon, "goi_mon");
        pnlContent.add(pnlKhachHang, "khach_hang");

        add(pnlContent, BorderLayout.CENTER);

        // Mặc định chọn Trang chủ đầu tiên
        setSelectedMenu(lblHome);
    }

    /**
     * Hàm tạo một mục Menu chuẩn đẹp
     */
    private JLabel createMenuItem(String text, String cardName) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18)); // Chữ to, đậm (Size 18)
        lbl.setForeground(colorNormal);
        lbl.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Di chuột vào thành bàn tay
        lbl.setBorder(new EmptyBorder(5, 0, 5, 0)); // Padding nhẹ

        // Thêm vào danh sách quản lý để sau này reset màu
        listMenu.add(lbl);

        // Sự kiện Click
        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(cardName.equals("exit")) {
                    System.exit(0);
                } else {
                    // 1. Chuyển màn hình
                    cardLayout.show(pnlContent, cardName);
                    // 2. Đổi màu menu active
                    setSelectedMenu(lbl);
                }
            }
            
            // Hiệu ứng Hover (Di chuột vào đổi màu nhẹ)
            @Override
            public void mouseEntered(MouseEvent e) {
                 if(lbl.getForeground() != colorActive) {
                     lbl.setForeground(new Color(100, 100, 100)); // Màu xám khi hover
                 }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if(lbl.getForeground() != colorActive) {
                    lbl.setForeground(colorNormal);
                }
            }
        });

        return lbl;
    }

    /**
     * Hàm xử lý hiệu ứng "Đang chọn" (Gạch chân màu xanh)
     */
    private void setSelectedMenu(JLabel selectedLbl) {
        // 1. Reset tất cả các menu khác về bình thường (Màu đen, không gạch chân)
        for (JLabel lbl : listMenu) {
            lbl.setForeground(colorNormal);
            lbl.setBorder(new EmptyBorder(5, 0, 5, 0)); // Bỏ viền
        }

        // 2. Set style cho menu được chọn (Màu xanh, Gạch chân đậm)
        selectedLbl.setForeground(colorActive);
        
        // Tạo Border phía dưới (MatteBorder: top, left, bottom, right)
        // Số 4 là độ dày của gạch chân
        selectedLbl.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, colorActive));
    }

    public static void main(String[] args) {
        new TrangChu().setVisible(true);
    }
}