package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import DAO.HoaDonDAO;
import DAO.MonAnDAO;
import Entity.MonAn;

/**
 * ManHinhGoiMon - Placeholder class to resolve compilation errors.
 * This should be fully implemented based on business requirements.
 */
public class ManHinhGoiMon extends JFrame {

    private int maHD;
    private String tenBan;
    private boolean isReadOnly;
    private HoaDonDAO hdDAO = new HoaDonDAO();
    private MonAnDAO monAnDAO = new MonAnDAO();
    private JTable tblMenu;
    private DefaultTableModel modelMenu;

    public ManHinhGoiMon(int maHD, String tenBan, boolean isReadOnly) {
        this.maHD = maHD;
        this.tenBan = tenBan;
        this.isReadOnly = isReadOnly;

        initGUI();
    }

    private void initGUI() {
        setTitle("Gọi món - " + tenBan);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JPanel pnlHeader = new JPanel();
        pnlHeader.setBackground(new Color(0, 123, 255));
        JLabel lblTitle = new JLabel("MENU GỌI MÓN - " + tenBan.toUpperCase());
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // Content - Dummy list món ăn
        String[] headers = { "Mã", "Tên món", "Đơn giá", "ĐVT" };
        modelMenu = new DefaultTableModel(headers, 0);
        tblMenu = new JTable(modelMenu);
        add(new JScrollPane(tblMenu), BorderLayout.CENTER);

        // Load data dummy or from DAO
        loadData();

        // Footer actions
        JPanel pnlFooter = new JPanel();
        JButton btnAdd = new JButton("Thêm món");
        JButton btnClose = new JButton("Đóng");

        btnAdd.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng đang được cập nhật!"));
        btnClose.addActionListener(e -> dispose());

        pnlFooter.add(btnAdd);
        pnlFooter.add(btnClose);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private void loadData() {
        // Mockup loading
        try {
            ArrayList<MonAn> list = monAnDAO.getAll(); // Fixed method name
            for (MonAn m : list) {
                modelMenu.addRow(new Object[] { m.getMaMon(), m.getTenMon(), m.getDonGia(), m.getDonViTinh() });
            }
        } catch (Exception e) {
            // Fallback
        }
    }
}
