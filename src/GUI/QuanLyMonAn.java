package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import DAO.LoaiMonDAO;
import DAO.MonAnDAO;
import Entity.LoaiMon;
import Entity.MonAn;
import UTILS.XImage;

public class QuanLyMonAn extends JPanel {

    // Khai báo biến
    private JTextField txtMa, txtTen, txtGia;
    private JComboBox<String> cboDVT;
    private JComboBox<LoaiMon> cboLoai; // Dùng cho form nhập
    private JLabel lblHinh;
    
    // --- PHẦN MỚI: CÔNG CỤ LỌC & TÌM KIẾM ---
    private JTextField txtTimKiem;
    private JButton btnTim;
    private JComboBox<LoaiMon> cboLocLoai; // Dùng để lọc ở giữa
    // ----------------------------------------

    private JPanel pnlDanhSach; 
    
    private MonAnDAO dao = new MonAnDAO();
    private LoaiMonDAO daoLoai = new LoaiMonDAO();
    private String tenFileAnh = "default.png";
    
    Font fontLabel = new Font("Segoe UI", Font.BOLD, 16);
    Font fontInput = new Font("Segoe UI", Font.PLAIN, 16);

    public QuanLyMonAn() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ================= PHẦN 1: FORM NHẬP LIỆU (TOP) =================
        JPanel pnlTop = new JPanel(new BorderLayout(10, 0));
        pnlTop.setBackground(Color.WHITE);
        pnlTop.setPreferredSize(new Dimension(0, 300));
        pnlTop.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                "THÔNG TIN MÓN ĂN", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, 
                new Font("Segoe UI", Font.BOLD, 18), Color.BLUE));

        // A. Nhập liệu (Trái)
        JPanel pnlInputs = new JPanel(new GridLayout(4, 2, 15, 15));
        pnlInputs.setBackground(Color.WHITE);
        pnlInputs.setBorder(new EmptyBorder(10, 20, 10, 20));

        pnlInputs.add(createLabel("Mã món:"));
        txtMa = createTextField(); pnlInputs.add(txtMa);

        pnlInputs.add(createLabel("Tên món:"));
        txtTen = createTextField(); pnlInputs.add(txtTen);

        pnlInputs.add(createLabel("Đơn giá:"));
        txtGia = createTextField(); pnlInputs.add(txtGia);

        pnlInputs.add(createLabel("ĐVT & Loại:"));
        JPanel pnlSub = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlSub.setBackground(Color.WHITE);
        
        cboDVT = new JComboBox<>(new String[]{"Dĩa", "Tô", "Lon", "Chai", "Ly", "Nồi", "Phần", "Kg"});
        cboDVT.setFont(fontInput); cboDVT.setBackground(Color.WHITE);
        
        cboLoai = new JComboBox<>();
        cboLoai.setFont(fontInput); cboLoai.setBackground(Color.WHITE);
        
        pnlSub.add(cboDVT); pnlSub.add(cboLoai);
        pnlInputs.add(pnlSub);
        
        pnlTop.add(pnlInputs, BorderLayout.CENTER);

        // B. Ảnh (Phải)
        JPanel pnlImage = new JPanel(new BorderLayout());
        pnlImage.setBackground(Color.WHITE);
        pnlImage.setBorder(new EmptyBorder(10, 10, 10, 30));
        
        lblHinh = new JLabel("CHỌN ẢNH");
        lblHinh.setPreferredSize(new Dimension(200, 200));
        lblHinh.setBorder(new LineBorder(Color.GRAY, 1));
        lblHinh.setHorizontalAlignment(JLabel.CENTER);
        lblHinh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblHinh.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { chonAnh(); }
        });
        
        pnlImage.add(lblHinh, BorderLayout.CENTER);
        pnlTop.add(pnlImage, BorderLayout.EAST);

        // C. Nút bấm chức năng (Dưới cùng của Top)
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        pnlButtons.setBackground(Color.WHITE);
        
        JButton btnThem = createButton("THÊM");
        JButton btnSua = createButton("SỬA");
        JButton btnXoa = createButton("XÓA");
        JButton btnMoi = createButton("LÀM MỚI");
        
        pnlButtons.add(btnThem); pnlButtons.add(btnSua);
        pnlButtons.add(btnXoa); pnlButtons.add(btnMoi);
        
        pnlTop.add(pnlButtons, BorderLayout.SOUTH);
        add(pnlTop, BorderLayout.NORTH);

        // ================= PHẦN 2: THANH CÔNG CỤ & DANH SÁCH (CENTER) =================
        
        // Tạo một Panel chứa cả Thanh tìm kiếm và Grid để add vào CENTER
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 10));
        pnlCenter.setBackground(Color.WHITE);

        // >> THANH TÌM KIẾM & LỌC <<
        JPanel pnlCongCu = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlCongCu.setBackground(new Color(240, 248, 255)); // Màu nền xanh nhạt cho nổi bật
        pnlCongCu.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        // 1. Ô tìm kiếm
        pnlCongCu.add(createLabel("Tìm tên:"));
        txtTimKiem = new JTextField(15);
        txtTimKiem.setFont(fontInput);
        pnlCongCu.add(txtTimKiem);
        
        // 2. Nút tìm
        btnTim = new JButton("Tìm");
        btnTim.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTim.setBackground(new Color(46, 204, 113)); // Màu xanh lá
        btnTim.setForeground(Color.WHITE);
        pnlCongCu.add(btnTim);

        // 3. ComboBox Lọc Loại
        pnlCongCu.add(new JLabel("   |   Lọc theo loại:")); // Dấu gạch đứng phân cách
        cboLocLoai = new JComboBox<>();
        cboLocLoai.setFont(fontInput);
        cboLocLoai.setBackground(Color.WHITE);
        cboLocLoai.setPreferredSize(new Dimension(150, 30));
        pnlCongCu.add(cboLocLoai);

        pnlCenter.add(pnlCongCu, BorderLayout.NORTH); // Add thanh công cụ lên đầu phần Center

        // >> DANH SÁCH GRID <<
        pnlDanhSach = new JPanel(new GridLayout(0, 4, 20, 20)); 
        pnlDanhSach.setBackground(new Color(245, 245, 245));
        
        JScrollPane scroll = new JScrollPane(pnlDanhSach);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(BorderFactory.createTitledBorder("DANH SÁCH MÓN ĂN"));
        
        pnlCenter.add(scroll, BorderLayout.CENTER);
        
        // Add toàn bộ phần Center vào giao diện chính
        add(pnlCenter, BorderLayout.CENTER);

        // ================= KHỞI TẠO DỮ LIỆU =================
        loadComboboxLoai(); // Load cho form nhập
        loadComboboxLoc();  // Load cho thanh lọc
        loadDataGrid();     // Load toàn bộ món ăn

        // ================= SỰ KIỆN NÚT BẤM CƠ BẢN =================
        btnThem.addActionListener(e -> {
            if(dao.insert(getForm())) {
                JOptionPane.showMessageDialog(this, "Thêm thành công!");
                loadDataGrid(); clearForm();
            } else JOptionPane.showMessageDialog(this, "Lỗi thêm!");
        });

        btnSua.addActionListener(e -> {
            if(dao.update(getForm())) {
                JOptionPane.showMessageDialog(this, "Sửa thành công!");
                loadDataGrid(); clearForm();
            }
        });

        btnXoa.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this, "Xóa món này?") == 0) {
                if(dao.delete(txtMa.getText())) {
                    loadDataGrid(); clearForm();
                }
            }
        });
        
        btnMoi.addActionListener(e -> clearForm());

        // ================= SỰ KIỆN TÌM KIẾM & LỌC (QUAN TRỌNG) =================
        
        // 1. Sự kiện bấm nút Tìm
        btnTim.addActionListener(e -> xuLyTimKiem());
        
        // 2. Sự kiện gõ phím trong ô tìm kiếm (Gõ đến đâu tìm đến đó)
        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                xuLyTimKiem();
            }
        });
        
        // 3. Sự kiện chọn ComboBox Lọc
        cboLocLoai.addActionListener(e -> xuLyTimKiem());
    }

    // ================== HÀM XỬ LÝ GRID VIEW ==================

    // Hàm load dữ liệu (có áp dụng tìm kiếm)
    void xuLyTimKiem() {
        String keyword = txtTimKiem.getText();
        String maLoai = "";
        
        // Lấy mã loại từ ComboBox Lọc
        if (cboLocLoai.getSelectedItem() != null) {
            LoaiMon lm = (LoaiMon) cboLocLoai.getSelectedItem();
            // Nếu mã là rỗng (mục "Tất cả") thì để chuỗi rỗng
            if(lm.getMaLoai() != null) {
                maLoai = lm.getMaLoai();
            }
        }
        
        // Gọi DAO tìm kiếm
        ArrayList<MonAn> list = dao.timKiem(keyword, maLoai);
        
        // Vẽ lại giao diện
        pnlDanhSach.removeAll();
        for (MonAn m : list) {
            ItemMonAn item = new ItemMonAn(m);
            pnlDanhSach.add(item);
        }
        pnlDanhSach.revalidate();
        pnlDanhSach.repaint();
    }
    
    // Hàm load mặc định (lấy tất cả)
    void loadDataGrid() {
        // Reset thanh tìm kiếm
        txtTimKiem.setText("");
        if(cboLocLoai.getItemCount() > 0) cboLocLoai.setSelectedIndex(0);
        
        // Gọi hàm tìm kiếm với tham số rỗng -> tương đương getAll
        xuLyTimKiem(); 
    }

    // Class con: ItemMonAn (Giữ nguyên như bài trước)
    public class ItemMonAn extends JPanel {
        private MonAn monAn;
        public ItemMonAn(MonAn m) {
            this.monAn = m;
            setPreferredSize(new Dimension(220, 280));
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel lblImg = new JLabel();
            lblImg.setHorizontalAlignment(JLabel.CENTER);
            ImageIcon icon = XImage.read(m.getHinhAnh());
            if(icon != null) {
                Image img = icon.getImage().getScaledInstance(200, 160, Image.SCALE_SMOOTH);
                lblImg.setIcon(new ImageIcon(img));
            } else lblImg.setText("No Image");
            add(lblImg, BorderLayout.CENTER);

            JPanel pnlInfo = new JPanel(new GridLayout(2, 1));
            pnlInfo.setBackground(new Color(250, 250, 250));
            pnlInfo.setPreferredSize(new Dimension(220, 80));
            pnlInfo.setBorder(new EmptyBorder(5, 10, 5, 10));

            JLabel lblTen = new JLabel(m.getTenMon());
            lblTen.setFont(new Font("Segoe UI", Font.BOLD, 16));
            DecimalFormat df = new DecimalFormat("#,### VNĐ");
            JLabel lblGia = new JLabel(df.format(m.getDonGia()));
            lblGia.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblGia.setForeground(new Color(200, 50, 50));
            
            pnlInfo.add(lblTen); pnlInfo.add(lblGia);
            add(pnlInfo, BorderLayout.SOUTH);

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setBorder(BorderFactory.createLineBorder(new Color(0, 120, 215), 2)); }
                public void mouseExited(MouseEvent e) { setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1)); }
                public void mouseClicked(MouseEvent e) { fillForm(monAn); }
            });
        }
    }

    // ================== CÁC HÀM HỖ TRỢ KHÁC ==================
    
    // Load CBO cho Form Nhập
    void loadComboboxLoai() {
        cboLoai.removeAllItems();
        for (LoaiMon lm : daoLoai.getAllLoai()) cboLoai.addItem(lm);
    }
    
    // Load CBO cho Thanh Lọc (Thêm mục "Tất cả")
    void loadComboboxLoc() {
        cboLocLoai.removeAllItems();
        // Thêm mục mặc định
        cboLocLoai.addItem(new LoaiMon("", "--- Tất cả ---"));
        
        for (LoaiMon lm : daoLoai.getAllLoai()) {
            cboLocLoai.addItem(lm);
        }
    }
    
    void fillForm(MonAn m) {
        txtMa.setText(m.getMaMon());
        txtTen.setText(m.getTenMon());
        txtGia.setText(String.valueOf((int)m.getDonGia()));
        cboDVT.setSelectedItem(m.getDonViTinh());
        for(int i=0; i<cboLoai.getItemCount(); i++) {
            LoaiMon lm = cboLoai.getItemAt(i);
            if(lm.getMaLoai().equals(m.getMaLoai())) {
                cboLoai.setSelectedIndex(i); break;
            }
        }
        tenFileAnh = m.getHinhAnh();
        ImageIcon icon = XImage.read(tenFileAnh);
        if(icon != null) {
            Image img = icon.getImage().getScaledInstance(lblHinh.getWidth(), lblHinh.getHeight(), Image.SCALE_SMOOTH);
            lblHinh.setIcon(new ImageIcon(img)); lblHinh.setText("");
        }
    }
    
    MonAn getForm() {
        String ma = txtMa.getText();
        String ten = txtTen.getText();
        String dvt = cboDVT.getSelectedItem().toString();
        double gia = 0;
        try { gia = Double.parseDouble(txtGia.getText()); } catch(Exception e){}
        String maLoai = ((LoaiMon)cboLoai.getSelectedItem()).getMaLoai();
        return new MonAn(ma, ten, dvt, gia, tenFileAnh, maLoai);
    }
    
    void clearForm() {
        txtMa.setText(""); txtTen.setText(""); txtGia.setText("");
        lblHinh.setIcon(null); lblHinh.setText("CHỌN ẢNH");
        tenFileAnh = "default.png";
    }
    
    void chonAnh() {
        JFileChooser ch = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Ảnh", "png", "jpg");
        ch.setFileFilter(filter);
        if(ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File src = ch.getSelectedFile();
            XImage.save(src);
            tenFileAnh = src.getName();
            ImageIcon icon = XImage.read(tenFileAnh);
            Image img = icon.getImage().getScaledInstance(lblHinh.getWidth(), lblHinh.getHeight(), Image.SCALE_SMOOTH);
            lblHinh.setIcon(new ImageIcon(img)); lblHinh.setText("");
        }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(fontLabel);
        return lbl;
    }
    private JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setFont(fontInput);
        return txt;
    }
    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(0, 102, 204));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(120, 40));
        return btn;
    }
}