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

public class TrangChu extends JFrame {

    private JPanel pnlContent;
    private CardLayout cardLayout;
    
    // Danh sách các mục menu để xử lý hiệu ứng click
    private ArrayList<JLabel> listMenu = new ArrayList<>();

    // MÀU SẮC CHỦ ĐẠO
    private Color colorActive = new Color(0, 123, 255); // Màu xanh dương (khi chọn)
    private Color colorNormal = new Color(0, 0, 0);     // Màu đen (bình thường)
    private Color colorBg = Color.WHITE;                // Nền trắng

    // Khai báo các màn hình con để có thể truy cập nếu cần reload
    private ManHinhTrangChu pnlTrangChu;
    private QuanLyMonAn pnlQuanLyMonAn;
    private QuanLyBan pnlBan;
    private ManHinhHoaDon pnlHoaDon;
    private QuanLyKhachHang pnlKhachHang;
    private ThongKeDoanhThu pnlThongKe; // <--- KHAI BÁO MỚI

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
        pnlMenu.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 15)); 
        pnlMenu.setBackground(colorBg);
        pnlMenu.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        // Tạo các mục menu (Text, Tên thẻ card)
        JLabel lblHome = createMenuItem("Trang Chủ", "home");
        JLabel lblMonAn = createMenuItem("Món Ăn", "mon_an");
        JLabel lblBan = createMenuItem("Quản Lý Bàn", "ban");
        JLabel lblGoiMon = createMenuItem("Hóa Đơn", "hoa_don");
        JLabel lblKhachHang = createMenuItem("Khách Hàng", "khach_hang");
        
        // [THÊM MỚI] Menu Thống kê
        JLabel lblThongKe = createMenuItem("Thống Kê", "thong_ke"); 
        
        JLabel lblThoat = createMenuItem("Thoát", "exit");

        // Add vào thanh menu
        pnlMenu.add(lblHome);
        pnlMenu.add(lblMonAn);
        pnlMenu.add(lblBan);
        pnlMenu.add(lblGoiMon);
        pnlMenu.add(lblKhachHang);
        pnlMenu.add(lblThongKe); // <--- Add nút Thống kê vào
        pnlMenu.add(lblThoat);

        add(pnlMenu, BorderLayout.NORTH);

        // --- 2. PHẦN NỘI DUNG (CONTENT) ---
        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);
        pnlContent.setBackground(new Color(245, 245, 250));

        // >> Khởi tạo các Panel nội dung <<
        pnlTrangChu = new ManHinhTrangChu();
        pnlQuanLyMonAn = new QuanLyMonAn(); 
        pnlBan = new QuanLyBan();
        pnlHoaDon = new ManHinhHoaDon();
        pnlKhachHang = new QuanLyKhachHang();
        
        // [THÊM MỚI] Khởi tạo màn hình Thống kê
        pnlThongKe = new ThongKeDoanhThu(); 

        // Add vào CardLayout
        pnlContent.add(pnlTrangChu, "home");
        pnlContent.add(pnlQuanLyMonAn, "mon_an");
        pnlContent.add(pnlBan, "ban");
        pnlContent.add(pnlHoaDon, "hoa_don");
        pnlContent.add(pnlKhachHang, "khach_hang");
        
        // [THÊM MỚI] Add vào layout
        pnlContent.add(pnlThongKe, "thong_ke"); 

        add(pnlContent, BorderLayout.CENTER);

        // Mặc định chọn Trang chủ đầu tiên
        setSelectedMenu(lblHome);
    }

    /**
     * Hàm tạo một mục Menu chuẩn đẹp
     */
    private JLabel createMenuItem(String text, String cardName) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(colorNormal);
        lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lbl.setBorder(new EmptyBorder(5, 0, 5, 0));

        listMenu.add(lbl);

        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(cardName.equals("exit")) {
                    System.exit(0);
                } else {
                    cardLayout.show(pnlContent, cardName);
                    setSelectedMenu(lbl);
                    
                    // [TÙY CHỌN] Nếu muốn bấm vào tab Thống Kê thì tự động làm mới dữ liệu
                    /*
                    if (cardName.equals("thong_ke")) {
                        // Cần mở modifier của hàm thongKe() trong ThongKeDatBan thành public để gọi được
                        // pnlThongKe.thongKe(); 
                    }
                    */
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                 if(lbl.getForeground() != colorActive) {
                     lbl.setForeground(new Color(100, 100, 100));
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

    private void setSelectedMenu(JLabel selectedLbl) {
        for (JLabel lbl : listMenu) {
            lbl.setForeground(colorNormal);
            lbl.setBorder(new EmptyBorder(5, 0, 5, 0));
        }

        selectedLbl.setForeground(colorActive);
        selectedLbl.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, colorActive));
    }

    public static void main(String[] args) {
        new TrangChu().setVisible(true);
    }
}