package GUI;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.toedter.calendar.JDateChooser;

import GUI.components.TableFloorPanel;
import GUI.components.TableCard;
import Entity.Ban;
import Entity.HoaDon;
import DAO.BanDAO;
import DAO.HoaDonDAO;
import DAO.KhachHangDAO;
// reuse DatBanDAO/Entity if needed, or just use Ban logic

public class ManHinhPhucVu extends JPanel implements TableCard.TableCardListener {

    private JTabbedPane tabFloors;
    private BanDAO banDAO;
    private HoaDonDAO hoaDonDAO;

    public ManHinhPhucVu() {
        this.banDAO = new BanDAO();
        this.hoaDonDAO = new HoaDonDAO();

        initUI();

        // Auto-refresh
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshAllFloors();
            }
        });
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(249, 250, 251));

        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel lblTitle = new JLabel("PHỤC VỤ KHÁCH HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(31, 41, 55));

        JLabel lblSubtitle = new JLabel("Chọn bàn để gọi món hoặc bắt đầu phục vụ");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(107, 114, 128));

        JPanel pnlTitle = new JPanel(new GridLayout(2, 1));
        pnlTitle.setOpaque(false);
        pnlTitle.add(lblTitle);
        pnlTitle.add(lblSubtitle);

        JButton btnReload = new JButton("Làm mới trạng thái");
        btnReload.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnReload.addActionListener(e -> refreshAllFloors());

        pnlHeader.add(pnlTitle, BorderLayout.WEST);
        pnlHeader.add(btnReload, BorderLayout.EAST);

        add(pnlHeader, BorderLayout.NORTH);

        // Floors
        tabFloors = new JTabbedPane();
        tabFloors.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabFloors.setBackground(Color.WHITE);

        addFloorTab("Tất cả", "ALL");
        addFloorTab("Tầng G", "KV01");
        addFloorTab("Tầng 1", "KV02");
        addFloorTab("VIP Room", "KV03");
        addFloorTab("Ngoài trời", "KV04");

        add(tabFloors, BorderLayout.CENTER);
    }

    private void addFloorTab(String title, String zoneId) {
        TableFloorPanel pnl = new TableFloorPanel(zoneId);
        pnl.setTableCardListener(this);
        tabFloors.addTab(title, pnl);
    }

    private void refreshAllFloors() {
        for (int i = 0; i < tabFloors.getTabCount(); i++) {
            Component c = tabFloors.getComponentAt(i);
            if (c instanceof TableFloorPanel) {
                ((TableFloorPanel) c).refreshTables();
            }
        }
    }

    @Override
    public void onTableCardClicked(Ban table) {
        String status = table.getTrangThai();

        if ("Trống".equals(status)) {
            // Logic: Khách vào bàn -> Tạo hóa đơn -> Mở gọi món
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bàn " + table.getTenBan() + " còn trống. Bắt đầu phục vụ khách mới?",
                    "Khách Mới", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // START SERVICE
                // Auto create Invoice
                HoaDon hd = new HoaDon(table.getMaBan(), 1, null, "Khách vãng lai", null);
                int maHD = hoaDonDAO.insert(hd);

                if (maHD != -1) {
                    banDAO.updateTrangThai(table.getMaBan(), "Có Khách");
                    refreshAllFloors();
                    openOrdering(maHD, table.getTenBan());
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi tạo hóa đơn!");
                }
            }
        } else if ("Có Khách".equals(status) || "Đã nhận bàn".equals(status)) {
            // Already occupied -> Open Ordering
            // Find invoice
            int maHD = hoaDonDAO.getMaHDByBan(table.getMaBan());
            if (maHD != -1) {
                openOrdering(maHD, table.getTenBan());
            } else {
                // Stuck Table Case
                Object[] options = { "Tạo Hóa Đơn Mới", "Reset Bàn Về Trống", "Hủy" };
                int choice = JOptionPane.showOptionDialog(this,
                        "Bàn " + table.getMaBan()
                                + " đang có khách nhưng không tìm thấy hóa đơn (Lỗi dữ liệu).\nBạn muốn xử lý thế nào?",
                        "Lỗi Dữ Liệu Bàn",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.ERROR_MESSAGE,
                        null,
                        options,
                        options[0]);

                if (choice == 0) {
                    // Create new invoice
                    HoaDon hd = new HoaDon(table.getMaBan(), 1, null, "Khách vãng lai (Recovered)", null);
                    int newMaHD = hoaDonDAO.insert(hd);
                    if (newMaHD != -1) {
                        openOrdering(newMaHD, table.getTenBan());
                    }
                } else if (choice == 1) {
                    // Reset to Empty
                    banDAO.updateTrangThai(table.getMaBan(), "Trống");
                    refreshAllFloors();
                    JOptionPane.showMessageDialog(this, "Đã reset bàn về trạng thái Trống.");
                }
            }
        } else if ("Đã Đặt".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "Bàn này ĐÃ ĐẶT trước. Vui lòng vào mục 'Đặt Bàn' để Check-in trước khi gọi món.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openOrdering(int maHD, String tenBan) {
        ManHinhGoiMon screen = new ManHinhGoiMon(maHD, "Phục vụ: " + tenBan, false);
        screen.setVisible(true);
        // Add listener to refresh when closed? Not strictly needed as DB updates.
        // But maybe refresh status if they check out?
        // For now simple open.
    }
}
