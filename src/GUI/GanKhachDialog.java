package GUI;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import DAO.KhachHangDAO;
import Entity.KhachHang;
import GUI.utils.UIStyle;

public class GanKhachDialog extends JDialog {

    private JTextField txtSDT;
    private JTextField txtTen;
    private JButton btnLuu, btnHuy;
    private boolean isSaved = false;
    private String savedSDT = "";
    private String savedTen = "";
    private boolean isSelecting = false;

    private List<KhachHang> cacheKhachHang;
    private JPopupMenu popupSDT;
    private JPopupMenu popupTen;

    public GanKhachDialog(Component parent, String curSdt, String curTen) {
        super(SwingUtilities.getWindowAncestor(parent), "Gán Khách Hàng", ModalityType.APPLICATION_MODAL);
        
        setSize(400, 250);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Load Cache
        cacheKhachHang = new KhachHangDAO().getAll("Đang hoạt động");

        // Header
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(10, 0, 0, 0));
        pnlHeader.add(UIStyle.title("GÁN KHÁCH HÀNG"));
        add(pnlHeader, BorderLayout.NORTH);

        // Body
        JPanel pnlBody = new JPanel(new GridBagLayout());
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);

        // SDT
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        pnlBody.add(UIStyle.label("SĐT:"), gbc);
        
        txtSDT = UIStyle.textField("", 20);
        txtSDT.setText(curSdt);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        pnlBody.add(txtSDT, gbc);

        // Ten
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        pnlBody.add(UIStyle.label("Tên khách:"), gbc);

        txtTen = UIStyle.textField("", 20);
        txtTen.setText(curTen);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        pnlBody.add(txtTen, gbc);

        add(pnlBody, BorderLayout.CENTER);

        // Footer
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlFooter.setBackground(Color.WHITE);

        btnHuy = UIStyle.button(UIStyle.BtnType.NEUTRAL, "Hủy");
        btnLuu = UIStyle.button(UIStyle.BtnType.SUCCESS, "Xác nhận");

        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> save());

        pnlFooter.add(btnHuy);
        pnlFooter.add(btnLuu);
        add(pnlFooter, BorderLayout.SOUTH);

        // Autocomplete
        popupSDT = new JPopupMenu();
        popupTen = new JPopupMenu();

        setupAutocomplete(txtSDT, popupSDT, true);
        setupAutocomplete(txtTen, popupTen, false);
    }

    private void setupAutocomplete(JTextField textField, JPopupMenu popup, boolean isSDT) {
        popup.setFocusable(false);
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { showSuggestions(); }
            @Override
            public void removeUpdate(DocumentEvent e) { showSuggestions(); }
            @Override
            public void changedUpdate(DocumentEvent e) { showSuggestions(); }

            private void showSuggestions() {
                if (isSelecting) return;
                SwingUtilities.invokeLater(() -> {
                    String input = textField.getText().trim().toLowerCase();
                    if (input.isEmpty()) {
                        popup.setVisible(false);
                        return;
                    }

                    popup.removeAll();
                    int count = 0;
                    for (KhachHang kh : cacheKhachHang) {
                        boolean match = isSDT ? kh.getSoDienThoai().startsWith(input) 
                                              : kh.getTenKhach().toLowerCase().contains(input);
                        if (match) {
                            JMenuItem item = new JMenuItem(kh.getSoDienThoai() + " - " + kh.getTenKhach());
                            item.addActionListener(ev -> {
                                isSelecting = true;
                                txtSDT.setText(kh.getSoDienThoai());
                                txtTen.setText(kh.getTenKhach());
                                popup.setVisible(false);
                                isSelecting = false;
                            });
                            popup.add(item);
                            count++;
                            if (count >= 5) break; 
                        }
                    }

                    if (count > 0 && textField.isFocusOwner()) {
                        popup.pack();
                        if (!popup.isVisible()) {
                            popup.show(textField, 0, textField.getHeight());
                        }
                    } else {
                        popup.setVisible(false);
                    }
                });
            }
        });
    }

    private void save() {
        String sdt = txtSDT.getText().trim();
        String ten = txtTen.getText().trim();

        if (sdt.isEmpty() || ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ SĐT và tên khách hàng!");
            return;
        }

        KhachHangDAO dao = new KhachHangDAO();
        if (!dao.checkTonTai(sdt)) {
            int confirm = JOptionPane.showConfirmDialog(this, "Khách hàng mới! Bạn có muốn tạo mới?", "Khách Mới", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (!dao.themKhachMoi(sdt, ten)) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi tạo khách hàng!");
                    return;
                }
            } else return;
        } else {
            KhachHang curKh = dao.getBySDT(sdt);
            if (curKh != null && !curKh.getTenKhach().equalsIgnoreCase(ten)) {
                curKh.setTenKhach(ten);
                dao.update(curKh); 
            }
        }

        this.savedSDT = sdt;
        this.savedTen = ten;
        this.isSaved = true;
        dispose();
    }

    public boolean isSaved() { return isSaved; }
    public String getSDT() { return savedSDT; }
    public String getTenKhach() { return savedTen; }
}
