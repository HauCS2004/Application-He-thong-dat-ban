package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import DAO.LoaiMonDAO;
import DAO.MonAnDAO;
import Entity.LoaiMon;
import Entity.MonAn;
import GUI.utils.UIStyle;
import UTILS.XImage;

public class MonAnDialog extends JDialog {

    private JTextField txtMa, txtTen;
    private JComboBox<String> cboDVT;
    private JComboBox<LoaiMon> cboLoai;
    private JLabel lblHinh;
    private String tenFileAnh = "default.png";

    private boolean saved = false;
    private boolean isEdit = false;
    private MonAnDAO dao;
    private LoaiMonDAO loaiDao;

    public MonAnDialog(JPanel parent, MonAn monAn, MonAnDAO dao, LoaiMonDAO loaiDao) {
        super((JFrame) SwingUtilities.getWindowAncestor(parent), true);
        this.dao = dao;
        this.loaiDao = loaiDao;
        this.isEdit = (monAn != null);

        setTitle(isEdit ? "SỬA MÓN ĂN" : "THÊM MÓN ĂN");
        setSize(750, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- HEADER ---
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 0, 5, 0));
        pnlHeader.add(UIStyle.title(isEdit ? "CHỈNH SỬA MÓN ĂN" : "THÊM MÓN ĂN MỚI"));
        add(pnlHeader, BorderLayout.NORTH);

        // --- CENTER ---
        JPanel pnlCenter = new JPanel(new BorderLayout(15, 0));
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.setBorder(new EmptyBorder(10, 20, 10, 20));

        // LEFT: Inputs
        JPanel pnlInputs = new JPanel(new GridBagLayout());
        pnlInputs.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        int row = 0;
        
        txtMa = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Mã món:", txtMa);

        txtTen = UIStyle.textField("", 20);
        addRow(pnlInputs, gbc, row++, "Tên món:", txtTen);


        cboDVT = new JComboBox<>(new String[] { "Dĩa", "Tô", "Lon", "Chai", "Ly", "Nồi", "Phần", "Kg" });
        UIStyle.styleComboBox(cboDVT);
        addRow(pnlInputs, gbc, row++, "ĐVT:", cboDVT);

        cboLoai = new JComboBox<>();
        UIStyle.styleComboBox(cboLoai);
        loadComboboxLoai();
        addRow(pnlInputs, gbc, row++, "Loại món:", cboLoai);

        pnlCenter.add(pnlInputs, BorderLayout.CENTER);

        // RIGHT: Image
        JPanel pnlImageWrap = new JPanel(new BorderLayout());
        pnlImageWrap.setBackground(Color.WHITE);
        pnlImageWrap.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        lblHinh = new JLabel("CHỌN ẢNH (200x200)");
        lblHinh.setPreferredSize(new Dimension(220, 220));
        lblHinh.setBorder(new LineBorder(new Color(200, 200, 200), 2));
        lblHinh.setHorizontalAlignment(JLabel.CENTER);
        lblHinh.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        lblHinh.setForeground(new Color(150, 150, 150));
        lblHinh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblHinh.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                chonAnh();
            }
        });
        
        JPanel pnlImgFrame = UIStyle.card();
        pnlImgFrame.setLayout(new BorderLayout());
        pnlImgFrame.add(lblHinh, BorderLayout.CENTER);
        pnlImageWrap.add(pnlImgFrame, BorderLayout.NORTH);

        pnlCenter.add(pnlImageWrap, BorderLayout.EAST);
        add(pnlCenter, BorderLayout.CENTER);

        // --- BUTTONS ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlButtons.setBackground(Color.WHITE);

        JButton btnLuu = UIStyle.button(UIStyle.BtnType.SUCCESS, "LƯU DỮ LIỆU");
        btnLuu.setPreferredSize(new java.awt.Dimension(120, 35));
        JButton btnHuy = UIStyle.button(UIStyle.BtnType.NEUTRAL, "HỦY");
        btnHuy.setPreferredSize(new java.awt.Dimension(100, 35));

        pnlButtons.add(btnHuy);
        pnlButtons.add(btnLuu);
        add(pnlButtons, BorderLayout.SOUTH);

        // --- BIND DATA IF EDIT ---
        if (isEdit) {
            txtMa.setText(monAn.getMaMon());
            txtMa.setEditable(false);
            txtMa.setBackground(new Color(240, 240, 240));
            txtTen.setText(monAn.getTenMon());

            cboDVT.setSelectedItem(monAn.getDonViTinh());
            
            for (int i = 0; i < cboLoai.getItemCount(); i++) {
                if (cboLoai.getItemAt(i).getMaLoai().equals(monAn.getMaLoai())) {
                    cboLoai.setSelectedIndex(i);
                    break;
                }
            }

            tenFileAnh = monAn.getHinhAnh();
            updateImageLabel();
        }

        // --- EVENT LISTENERS ---
        btnLuu.addActionListener(e -> save());
        btnHuy.addActionListener(e -> dispose());
    }

    private void addRow(JPanel parent, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = UIStyle.label(label);
        lbl.setPreferredSize(new java.awt.Dimension(90, 30));
        parent.add(lbl, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1;
        parent.add(comp, gbc);
    }

    private void loadComboboxLoai() {
        cboLoai.removeAllItems();
        for (LoaiMon lm : loaiDao.getAllLoai()) {
            cboLoai.addItem(lm);
        }
    }

    private void chonAnh() {
        JFileChooser ch = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Ảnh", "png", "jpg");
        ch.setFileFilter(filter);
        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File src = ch.getSelectedFile();
            XImage.save(src);
            tenFileAnh = src.getName();
            updateImageLabel();
        }
    }

    private void updateImageLabel() {
        ImageIcon icon = XImage.read(tenFileAnh);
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
            lblHinh.setIcon(new ImageIcon(img));
            lblHinh.setText("");
        } else {
            lblHinh.setIcon(null);
            lblHinh.setText("Lỗi Ảnh");
        }
    }

    private void save() {
        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        String dvt = cboDVT.getSelectedItem().toString();
        LoaiMon loaiSelected = (LoaiMon) cboLoai.getSelectedItem();
        
        if (ma.isEmpty() || ten.isEmpty() || loaiSelected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ Mã, Tên và Loại món!");
            return;
        }

        String maLoai = loaiSelected.getMaLoai();
        MonAn newMon = new MonAn(ma, ten, dvt, tenFileAnh, maLoai);

        if (isEdit) {
            if (dao.update(newMon)) {
                JOptionPane.showMessageDialog(this, "Sửa món ăn thành công!");
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi cập nhật!");
            }
        } else {
            if (dao.insert(newMon)) {
                JOptionPane.showMessageDialog(this, "Thêm món ăn thành công!");
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi thêm mới (Trùng mã?)!");
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
