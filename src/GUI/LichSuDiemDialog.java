package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import DAO.KhachHangDAO;
import GUI.utils.UIStyle;

public class LichSuDiemDialog extends JDialog {

    private JTable table;
    private DefaultTableModel model;
    private KhachHangDAO dao;
    private String sdtKhach;

    public LichSuDiemDialog(JPanel parent, String sdtKhach, String tenKhach, KhachHangDAO dao) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), true);
        this.sdtKhach = sdtKhach;
        this.dao = dao;

        setTitle("LỊCH SỬ CỘNG ĐIỂM - " + tenKhach);
        setSize(550, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 0, 10, 0));
        pnlHeader.add(UIStyle.title("LỊCH SỬ CỘNG ĐIỂM: " + tenKhach));
        add(pnlHeader, BorderLayout.NORTH);

        // --- TABLE ---
        String[] headers = { "Mã HĐ", "Thời gian", "Tổng tiền (VNĐ)", "Điểm cộng" };
        model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(240, 240, 240));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scroll.getViewport().setBackground(Color.WHITE);
        
        JPanel pnlTable = new JPanel(new BorderLayout());
        pnlTable.setBackground(Color.WHITE);
        pnlTable.setBorder(new EmptyBorder(0, 15, 10, 15));
        pnlTable.add(scroll, BorderLayout.CENTER);
        add(pnlTable, BorderLayout.CENTER);

        // --- BOTTOM ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlButtons.setBackground(Color.WHITE);

        JButton btnDong = UIStyle.button(UIStyle.BtnType.NEUTRAL, "ĐÓNG");
        btnDong.setPreferredSize(new java.awt.Dimension(100, 35));
        btnDong.addActionListener(e -> dispose());
        pnlButtons.add(btnDong);

        add(pnlButtons, BorderLayout.SOUTH);

        // --- LOAD DATA ---
        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        ArrayList<Object[]> list = dao.getLichSuTichDiem(sdtKhach);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Object[] row : list) {
            java.sql.Timestamp ts = (java.sql.Timestamp) row[1];
            String thoiGian = (ts != null) ? sdf.format(ts) : "";
            double thanhTien = (double) row[2];
            int diemCong = (int) row[3];
            
            model.addRow(new Object[]{
                row[0],
                thoiGian,
                String.format("%,.0f", thanhTien),
                "+" + diemCong
            });
        }
    }
}
