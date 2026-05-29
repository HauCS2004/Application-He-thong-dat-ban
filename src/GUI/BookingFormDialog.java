package GUI;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.toedter.calendar.JDateChooser;

import DAO.DatBanDAO;
import DAO.KhachHangDAO;
import DAO.BanDAO;
import Entity.DatBan;
import Entity.Ban;

/**
 * BookingFormDialog — Wizard đặt bàn theo phong cách nhà hàng hiện đại.
 * 
 * Flow: Bước 1 (Số khách + Ngày + Giờ) → Bước 2 (Chọn bàn trống)
 * → Bước 3 (Thông tin khách + Xác nhận)
 */
public class BookingFormDialog extends JDialog {

    private static final double UI_SCALE = 1.2;
    private static final int OPEN_HOUR = 9;
    private static final int CLOSE_HOUR = 22;
    private static final int EVENING_START_HOUR = 17;
    private static final int SLOT_INTERVAL_MINUTES = 30;

    // ── WIZARD STATE ─────────────────────────────────────────────────
    private int currentStep = 1;
    private static final int TOTAL_STEPS = 3;

    // ── STEP PANELS ──────────────────────────────────────────────────
    private JPanel pnlStepContent;
    private CardLayout cardStep;

    // ── STEP 1: Thời gian ────────────────────────────────────────────
    private JSpinner spinGuests;
    private JDateChooser dateChooser;
    private int selectedHour = -1;
    private int selectedMinute = -1;
    private JPanel pnlTimeSlots;
    private JComboBox<String> cboThoiLuong;

    // ── STEP 2: Chọn bàn (hỗ trợ nhiều bàn) ─────────────────────────
    private JPanel pnlTableGrid;
    private List<String> selectedTableIds = new ArrayList<>();
    private List<String> selectedTableNames = new ArrayList<>();
    private JLabel lblTableInfo;
    private JLabel lblCapacityStatus; // Hiển thị sức chứa đã chọn vs số khách
    private int maxSingleTableCapacity = 0; // Sức chứa tối đa của 1 bàn
    private JComboBox<String> cboZoneFilter; // Bộ lọc tầng/khu vực
    private JButton btnSuggest; // Nút gợi ý bàn thông minh

    // ── STEP 3: Thông tin khách ──────────────────────────────────────
    private JTextField txtSDT, txtTenKH, txtGhiChu;
    private JLabel lblCustomerStatus;

    // ── NAVIGATION ───────────────────────────────────────────────────
    private JButton btnPrev, btnNext;
    private JLabel[] stepLabels;
    private JPanel[] stepDots;
    private final List<JButton> timeSlotButtons = new ArrayList<>();

    // ── DAO ──────────────────────────────────────────────────────────
    private DatBanDAO datBanDAO;
    private KhachHangDAO khachHangDAO;
    private BanDAO banDAO;
    private Runnable onSuccessCallback;

    // ── COLORS ───────────────────────────────────────────────────────
    private static final Color ACCENT = new Color(59, 130, 246);
    private static final Color ACCENT_DARK = new Color(37, 99, 235);
    private static final Color ACCENT_LIGHT = new Color(239, 246, 255);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color SUCCESS_LIGHT = new Color(220, 252, 231);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color WARNING = new Color(245, 158, 11);
    private static final Color WARNING_LIGHT = new Color(254, 243, 199);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER = new Color(229, 231, 235);
    private static final Color BG_PAGE = new Color(249, 250, 251);

    private static int scaled(int value) {
        return (int) Math.round(value * UI_SCALE);
    }

    private static Font uiFont(int style, int size) {
        return new Font("Segoe UI", style, scaled(size));
    }

    private static Dimension uiSize(int width, int height) {
        return new Dimension(scaled(width), scaled(height));
    }

    private static EmptyBorder uiPadding(int top, int left, int bottom, int right) {
        return new EmptyBorder(scaled(top), scaled(left), scaled(bottom), scaled(right));
    }

    public BookingFormDialog(Frame parent, String preSelectedTableId) {
        super(parent, "Đặt Bàn", true);
        datBanDAO = new DatBanDAO();
        khachHangDAO = new KhachHangDAO();
        banDAO = new BanDAO();

        initUI();

        if (preSelectedTableId != null) {
            this.selectedTableIds.add(preSelectedTableId);
        }

        setSize(1020, 820);
        setMinimumSize(new Dimension(980, 780));
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    public void setPreFilledTime(Date date, int hour, int minute) {
        if (date != null) {
            dateChooser.setDate(date);
            selectedHour = hour;
            selectedMinute = minute;
            refreshTimeSlots();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // INIT UI
    // ═══════════════════════════════════════════════════════════════════
    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // ── TOP: Stepper ─────────────────────────────────────────────
        add(createStepper(), BorderLayout.NORTH);

        // ── CENTER: Step Content ─────────────────────────────────────
        cardStep = new CardLayout();
        pnlStepContent = new JPanel(cardStep);
        pnlStepContent.setBackground(Color.WHITE);

        pnlStepContent.add(createStep1(), "step1");
        pnlStepContent.add(createStep2(), "step2");
        pnlStepContent.add(createStep3(), "step3");

        add(pnlStepContent, BorderLayout.CENTER);

        // ── BOTTOM: Navigation ───────────────────────────────────────
        add(createNavigation(), BorderLayout.SOUTH);

        updateStepperUI();
    }

    // ═══════════════════════════════════════════════════════════════════
    // STEPPER (Progress Indicator)
    // ═══════════════════════════════════════════════════════════════════
    private JPanel createStepper() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                uiPadding(24, 48, 24, 48)));

        JPanel pnlSteps = new JPanel(new GridLayout(1, 3, 0, 0));
        pnlSteps.setOpaque(false);

        String[] titles = { "Thời gian", "Chọn bàn", "Thông tin" };
        String[] subtitles = { "Ngày, giờ & số khách", "Bàn phù hợp", "Xác nhận đặt bàn" };

        stepLabels = new JLabel[3];
        stepDots = new JPanel[3];

        for (int i = 0; i < 3; i++) {
            pnlSteps.add(createStepItem(i, titles[i], subtitles[i]));
        }

        pnl.add(pnlSteps, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createStepItem(int index, String title, String subtitle) {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setOpaque(false);
        pnl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Dot + Number
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = Math.min(getWidth(), getHeight());
                g2.setColor(getBackground());
                g2.fillOval(0, 0, size, size);
                g2.setColor(getForeground());
                g2.setFont(uiFont(Font.BOLD, 14));
                String num = String.valueOf(index + 1);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(num, (size - fm.stringWidth(num)) / 2,
                        (size + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        dot.setPreferredSize(uiSize(42, 42));
        dot.setMaximumSize(uiSize(42, 42));
        dot.setOpaque(false);
        dot.setAlignmentX(Component.CENTER_ALIGNMENT);
        stepDots[index] = dot;

        // Title
        JLabel lbl = new JLabel(title);
        lbl.setFont(uiFont(Font.BOLD, 13));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        stepLabels[index] = lbl;

        // Subtitle
        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(uiFont(Font.PLAIN, 11));
        lblSub.setForeground(TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnl.add(Box.createVerticalStrut(scaled(2)));
        pnl.add(dot);
        pnl.add(Box.createVerticalStrut(scaled(8)));
        pnl.add(lbl);
        pnl.add(lblSub);

        return pnl;
    }

    private void updateStepperUI() {
        for (int i = 0; i < 3; i++) {
            if (i + 1 < currentStep) {
                // Completed
                stepDots[i].setBackground(SUCCESS);
                stepDots[i].setForeground(Color.WHITE);
                stepLabels[i].setForeground(SUCCESS);
            } else if (i + 1 == currentStep) {
                // Active
                stepDots[i].setBackground(ACCENT);
                stepDots[i].setForeground(Color.WHITE);
                stepLabels[i].setForeground(ACCENT);
            } else {
                // Future
                stepDots[i].setBackground(new Color(229, 231, 235));
                stepDots[i].setForeground(TEXT_MUTED);
                stepLabels[i].setForeground(TEXT_MUTED);
            }
            stepDots[i].repaint();
        }

        // Update buttons
        btnPrev.setVisible(currentStep > 1);
        if (currentStep == TOTAL_STEPS) {
            btnNext.setText("Xác nhận đặt bàn");
            btnNext.setBackground(SUCCESS);
        } else {
            btnNext.setText("Tiếp theo  →");
            btnNext.setBackground(ACCENT);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // STEP 1: Thời gian + Số khách
    // ═══════════════════════════════════════════════════════════════════
    private JPanel createStep1() {
        JPanel pnl = new JPanel(new BorderLayout(0, 0));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(uiPadding(28, 48, 12, 48));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // ── Số khách ─────────────────────────────────────────────────
        JPanel pnlControlsCard = new JPanel(new GridBagLayout());
        pnlControlsCard.setOpaque(true);
        pnlControlsCard.setBackground(BG_PAGE);
        pnlControlsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlControlsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, scaled(170)));
        pnlControlsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                uiPadding(18, 22, 18, 22)));

        JLabel lblGuests = createStepFieldLabel("Số khách");

        spinGuests = new JSpinner(new SpinnerNumberModel(2, 1, 500, 1));
        spinGuests.setFont(uiFont(Font.BOLD, 16));
        spinGuests.setPreferredSize(uiSize(110, 52));
        JComponent editor = spinGuests.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField txtSpinner = ((JSpinner.DefaultEditor) editor).getTextField();
            txtSpinner.setHorizontalAlignment(JTextField.CENTER);
            txtSpinner.setFont(uiFont(Font.BOLD, 16));
            txtSpinner.setBorder(BorderFactory.createEmptyBorder());
        }

        JLabel lblDuration = createStepFieldLabel("Thời lượng");
        cboThoiLuong = new JComboBox<>(new String[] { "1 giờ", "1.5 giờ", "2 giờ", "3 giờ", "4 giờ" });
        cboThoiLuong.setSelectedIndex(2);
        cboThoiLuong.setFont(uiFont(Font.PLAIN, 14));
        cboThoiLuong.setPreferredSize(uiSize(160, 52));
        cboThoiLuong.addActionListener(e -> {
            normalizeSelectedTimeSlot();
            refreshTimeSlots();
        });

        JLabel lblDate = createStepFieldLabel("Chọn ngày");
        dateChooser = new JDateChooser(new Date());
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setPreferredSize(uiSize(220, 52));
        dateChooser.setFont(uiFont(Font.PLAIN, 14));
        dateChooser.setMinSelectableDate(new Date());
        if (dateChooser.getDateEditor().getUiComponent() instanceof JTextField) {
            JTextField txtDate = (JTextField) dateChooser.getDateEditor().getUiComponent();
            txtDate.setFont(uiFont(Font.PLAIN, 14));
            txtDate.setBorder(BorderFactory.createEmptyBorder(0, scaled(10), 0, 0));
        }
        JButton btnCalendar = dateChooser.getCalendarButton();
        if (btnCalendar != null) {
            btnCalendar.setPreferredSize(uiSize(36, 36));
            btnCalendar.setFocusable(false);
        }

        // Khi đổi ngày → cập nhật lại trạng thái enabled/disabled của các khung giờ
        dateChooser.getDateEditor().addPropertyChangeListener("date", evt -> {
            normalizeSelectedTimeSlot();
            // Reset giờ đã chọn nếu nó là giờ trong quá khứ so với ngày mới
            if (selectedHour >= 0) {
                Calendar now = Calendar.getInstance();
                Calendar selDate = Calendar.getInstance();
                if (dateChooser.getDate() != null) {
                    selDate.setTime(dateChooser.getDate());
                }
                selDate.set(Calendar.HOUR_OF_DAY, selectedHour);
                selDate.set(Calendar.MINUTE, selectedMinute);
                selDate.set(Calendar.SECOND, 0);
                boolean selectedIsPast = selDate.before(now) && isSameDay(now, selDate);
                if (selectedIsPast) {
                    selectedHour = -1;
                    selectedMinute = -1;
                }
            }
            refreshTimeSlots();
        });

        // Quick date buttons
        JPanel pnlQuickDates = new JPanel(new FlowLayout(FlowLayout.LEFT, scaled(10), 0));
        pnlQuickDates.setOpaque(false);
        pnlQuickDates.add(createQuickDateBtn("Hôm nay", 0));
        pnlQuickDates.add(createQuickDateBtn("Ngày mai", 1));
        pnlQuickDates.add(createQuickDateBtn("+2 ngày", 2));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, scaled(16), scaled(16));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        pnlControlsCard.add(lblGuests, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.25;
        pnlControlsCard.add(spinGuests, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        pnlControlsCard.add(lblDuration, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, scaled(16), 0);
        pnlControlsCard.add(cboThoiLuong, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 0, scaled(16));
        pnlControlsCard.add(lblDate, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        pnlControlsCard.add(dateChooser, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        gbc.insets = new Insets(0, 0, 0, 0);
        pnlControlsCard.add(pnlQuickDates, gbc);

        content.add(pnlControlsCard);
        content.add(Box.createVerticalStrut(scaled(26)));

        // ── Khung giờ ────────────────────────────────────────────────
        JLabel lblTime = new JLabel("Chọn giờ");
        lblTime.setFont(uiFont(Font.BOLD, 15));
        lblTime.setForeground(TEXT_PRIMARY);
        lblTime.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblTime);
        content.add(Box.createVerticalStrut(scaled(14)));

        timeSlotButtons.clear();

        JPanel pnlTimeAll = new JPanel();
        pnlTimeAll.setLayout(new BoxLayout(pnlTimeAll, BoxLayout.Y_AXIS));
        pnlTimeAll.setOpaque(false);
        pnlTimeAll.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlTimeAll.add(createTimePeriodSection(
                "Buổi trưa",
                "09:00 - 16:30",
                buildTimeSlots(OPEN_HOUR, 0, EVENING_START_HOUR - 1, 30),
                6));
        pnlTimeAll.add(Box.createVerticalStrut(scaled(18)));
        pnlTimeAll.add(createTimePeriodSection(
                "Buổi tối",
                "17:00 - 22:00",
                buildTimeSlots(EVENING_START_HOUR, 0, CLOSE_HOUR, 0),
                6));

        pnlTimeSlots = pnlTimeAll;

        JScrollPane scrollTime = new JScrollPane(pnlTimeAll);
        scrollTime.setBorder(null);
        scrollTime.setOpaque(false);
        scrollTime.getViewport().setOpaque(false);
        scrollTime.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollTime.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollTime.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollTime.getVerticalScrollBar().setUnitIncrement(scaled(18));
        scrollTime.setPreferredSize(new Dimension(0, scaled(370)));

        content.add(scrollTime);

        pnl.add(content, BorderLayout.CENTER);
        return pnl;
    }

    private JLabel createStepFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(uiFont(Font.BOLD, 15));
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    private JPanel createTimePeriodSection(String title, String subtitle, List<int[]> slots, int columns) {
        JPanel section = new JPanel(new BorderLayout(0, scaled(12)));
        section.setOpaque(true);
        section.setBackground(BG_PAGE);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                uiPadding(16, 18, 18, 18)));

        JPanel pnlHeader = new JPanel(new BorderLayout(0, scaled(2)));
        pnlHeader.setOpaque(false);

        JLabel lblSection = new JLabel(title);
        lblSection.setFont(uiFont(Font.BOLD, 13));
        lblSection.setForeground(TEXT_MUTED);
        pnlHeader.add(lblSection, BorderLayout.NORTH);

        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(uiFont(Font.PLAIN, 11));
        lblSubtitle.setForeground(new Color(148, 163, 184));
        pnlHeader.add(lblSubtitle, BorderLayout.SOUTH);

        section.add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlSlots = new JPanel(new GridLayout(0, Math.max(1, columns), scaled(12), scaled(12)));
        pnlSlots.setOpaque(false);
        pnlSlots.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int[] slot : slots) {
            pnlSlots.add(createTimeSlotBtn(slot[0], slot[1]));
        }

        section.add(pnlSlots, BorderLayout.CENTER);
        return section;
    }

    private List<int[]> buildTimeSlots(int startHour, int startMinute, int endHour, int endMinute) {
        List<int[]> slots = new ArrayList<>();
        int startTotalMinutes = startHour * 60 + startMinute;
        int endTotalMinutes = endHour * 60 + endMinute;
        for (int totalMinutes = startTotalMinutes; totalMinutes <= endTotalMinutes;
                totalMinutes += SLOT_INTERVAL_MINUTES) {
            slots.add(new int[] { totalMinutes / 60, totalMinutes % 60 });
        }
        return slots;
    }

    private JButton createQuickDateBtn(String text, int daysFromNow) {
        JButton btn = new JButton(text);
        btn.setFont(uiFont(Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(243, 244, 246));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBorder(uiPadding(8, 14, 8, 14));
        btn.setPreferredSize(uiSize(110, 40));

        btn.addActionListener(e -> {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, daysFromNow);
            dateChooser.setDate(cal.getTime());
        });

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(ACCENT_LIGHT);
                btn.setForeground(ACCENT);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(243, 244, 246));
                btn.setForeground(TEXT_PRIMARY);
            }
        });

        return btn;
    }

    private JButton createTimeSlotBtn(int hour, int minute) {
        String timeStr = String.format("%02d:%02d", hour, minute);
        JButton btn = new JButton(timeStr) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), scaled(12), scaled(12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(uiFont(Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(uiSize(104, 50));
        btn.putClientProperty("hour", hour);
        btn.putClientProperty("minute", minute);
        timeSlotButtons.add(btn);
        applyTimeSlotState(btn, hour, minute, hour == selectedHour && minute == selectedMinute);

        btn.addActionListener(e -> {
            selectedHour = hour;
            selectedMinute = minute;
            refreshTimeSlots();
        });

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled() && !(selectedHour == hour && selectedMinute == minute)) {
                    btn.setBackground(ACCENT_LIGHT);
                    btn.setForeground(ACCENT);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) {
                    boolean sel = (hour == selectedHour && minute == selectedMinute);
                    updateTimeSlotStyle(btn, hour, minute, sel);
                }
            }
        });

        return btn;
    }

    private void updateTimeSlotStyle(JButton btn, int hour, int minute, boolean selected) {
        if (selected) {
            btn.setBackground(ACCENT);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(TEXT_PRIMARY);
        }
    }

    private void applyTimeSlotState(JButton btn, int hour, int minute, boolean isSelected) {
        if (isPastTimeSlot(hour, minute)) {
            btn.setEnabled(false);
            btn.setBackground(new Color(243, 244, 246));
            btn.setForeground(new Color(209, 213, 219));
            btn.setToolTipText("Khung giờ này đã qua.");
            return;
        }

        if (exceedsClosingTime(hour, minute)) {
            btn.setEnabled(false);
            btn.setBackground(new Color(243, 244, 246));
            btn.setForeground(new Color(209, 213, 219));
            btn.setToolTipText("Khung giờ này kết thúc sau 22:00.");
            return;
        }

        btn.setEnabled(true);
        btn.setToolTipText(null);
        updateTimeSlotStyle(btn, hour, minute, isSelected);
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private Calendar buildSlotCalendar(Date baseDate, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        if (baseDate != null) {
            cal.setTime(baseDate);
        }
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    private boolean isPastTimeSlot(int hour, int minute) {
        if (dateChooser == null || dateChooser.getDate() == null) {
            return false;
        }
        Calendar now = Calendar.getInstance();
        Calendar slotCal = buildSlotCalendar(dateChooser.getDate(), hour, minute);
        return isSameDay(now, slotCal) && slotCal.before(now);
    }

    private boolean exceedsClosingTime(int hour, int minute) {
        if (dateChooser == null || dateChooser.getDate() == null) {
            return false;
        }
        Calendar endCal = buildSlotCalendar(dateChooser.getDate(), hour, minute);
        endCal.add(Calendar.MINUTE, getDurationMinutes());
        Calendar closeCal = buildSlotCalendar(dateChooser.getDate(), CLOSE_HOUR, 0);
        return endCal.after(closeCal);
    }

    private boolean isTimeSlotSelectable(int hour, int minute) {
        return !isPastTimeSlot(hour, minute) && !exceedsClosingTime(hour, minute);
    }

    private void normalizeSelectedTimeSlot() {
        if (selectedHour >= 0 && !isTimeSlotSelectable(selectedHour, selectedMinute)) {
            selectedHour = -1;
            selectedMinute = -1;
        }
    }

    private void refreshTimeSlots() {
        if (pnlTimeSlots == null)
            return;
        normalizeSelectedTimeSlot();
        for (JButton btn : timeSlotButtons) {
            int h = (Integer) btn.getClientProperty("hour");
            int m = (Integer) btn.getClientProperty("minute");
            boolean isSelected = (h == selectedHour && m == selectedMinute);
            applyTimeSlotState(btn, h, m, isSelected);
        }
        pnlTimeSlots.revalidate();
        pnlTimeSlots.repaint();
    }

    // ═══════════════════════════════════════════════════════════════════
    // STEP 2: Chọn bàn
    // ═══════════════════════════════════════════════════════════════════
    private JPanel createStep2() {
        JPanel pnl = new JPanel(new BorderLayout(0, scaled(10)));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(uiPadding(20, 48, 12, 48));

        // ── TOP SECTION: Header + Filters ────────────────────────────
        JPanel pnlTop = new JPanel();
        pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));
        pnlTop.setOpaque(false);

        // Header info
        lblTableInfo = new JLabel();
        lblTableInfo.setFont(uiFont(Font.PLAIN, 13));
        lblTableInfo.setForeground(TEXT_MUTED);

        JPanel pnlHeader = new JPanel(new BorderLayout(0, scaled(4)));
        pnlHeader.setOpaque(false);
        pnlHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblTitle = new JLabel("Chọn bàn phù hợp");
        lblTitle.setFont(uiFont(Font.BOLD, 16));
        lblTitle.setForeground(TEXT_PRIMARY);
        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(lblTableInfo, BorderLayout.CENTER);

        // Capacity status indicator
        lblCapacityStatus = new JLabel(" ");
        lblCapacityStatus.setFont(uiFont(Font.BOLD, 13));
        lblCapacityStatus.setBorder(uiPadding(4, 0, 4, 0));
        pnlHeader.add(lblCapacityStatus, BorderLayout.SOUTH);

        pnlTop.add(pnlHeader);

        // ── BỘ LỌC ──────────────────────────────────────────────────
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, scaled(10), scaled(4)));
        pnlFilter.setOpaque(false);
        pnlFilter.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlFilter.setBorder(uiPadding(4, 0, 4, 0));

        // Zone filter
        JLabel lblZone = new JLabel("Khu vực:");
        lblZone.setFont(uiFont(Font.BOLD, 12));
        lblZone.setForeground(TEXT_MUTED);
        pnlFilter.add(lblZone);

        cboZoneFilter = new JComboBox<>(new String[] {
                "Tất cả", "Tầng G", "Tầng 1", "VIP Room", "Ngoài trời"
        });
        cboZoneFilter.setFont(uiFont(Font.PLAIN, 12));
        cboZoneFilter.setPreferredSize(uiSize(128, 36));
        cboZoneFilter.addActionListener(e -> loadAvailableTables());
        pnlFilter.add(cboZoneFilter);

        pnlFilter.add(Box.createHorizontalStrut(scaled(10)));

        // Suggestion button
        btnSuggest = new JButton("Gợi ý bàn") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(245, 158, 11), getWidth(), 0,
                        new Color(251, 191, 36));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSuggest.setFont(uiFont(Font.BOLD, 12));
        btnSuggest.setForeground(Color.WHITE);
        btnSuggest.setFocusPainted(false);
        btnSuggest.setContentAreaFilled(false);
        btnSuggest.setBorderPainted(false);
        btnSuggest.setOpaque(false);
        btnSuggest.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSuggest.setPreferredSize(uiSize(140, 36));
        btnSuggest.setToolTipText("Tự động chọn tổ hợp bàn phù hợp nhất");
        btnSuggest.addActionListener(e -> suggestTables());
        pnlFilter.add(btnSuggest);

        // Reset selection button
        JButton btnReset = new JButton("Bỏ chọn");
        btnReset.setFont(uiFont(Font.PLAIN, 11));
        btnReset.setForeground(DANGER);
        btnReset.setBackground(new Color(254, 242, 242));
        btnReset.setFocusPainted(false);
        btnReset.setBorder(uiPadding(5, 10, 5, 10));
        btnReset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReset.setPreferredSize(uiSize(100, 36));
        btnReset.addActionListener(e -> {
            selectedTableIds.clear();
            selectedTableNames.clear();
            refreshTableSelection();
        });
        pnlFilter.add(btnReset);

        pnlTop.add(pnlFilter);

        pnl.add(pnlTop, BorderLayout.NORTH);

        // ── TABLE GRID ───────────────────────────────────────────────
        pnlTableGrid = new JPanel(new GridLayout(0, 3, scaled(14), scaled(14)));
        pnlTableGrid.setOpaque(false);

        JScrollPane scroll = new JScrollPane(pnlTableGrid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        pnl.add(scroll, BorderLayout.CENTER);

        // ── LEGEND ───────────────────────────────────────────────────
        JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.LEFT, scaled(15), scaled(4)));
        pnlLegend.setOpaque(false);
        pnlLegend.add(createLegendItem(ACCENT_LIGHT, ACCENT, "Đang chọn"));
        pnlLegend.add(createLegendItem(SUCCESS_LIGHT, SUCCESS, "Trống"));
        pnlLegend.add(createLegendItem(WARNING_LIGHT, WARNING, "Ít chỗ"));
        pnlLegend.add(createLegendItem(new Color(254, 226, 226), DANGER, "Đã đặt"));
        pnlLegend.add(createLegendItem(new Color(243, 244, 246), TEXT_MUTED, "Có khách"));
        pnl.add(pnlLegend, BorderLayout.SOUTH);

        return pnl;
    }

    private JPanel createLegendItem(Color bg, Color fg, String text) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, scaled(4), 0));
        pnl.setOpaque(false);

        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, scaled(16), scaled(16), scaled(6), scaled(6));
                g2.setColor(fg);
                g2.fillOval(scaled(4), scaled(4), scaled(8), scaled(8));
                g2.dispose();
            }
        };
        dot.setPreferredSize(uiSize(16, 16));
        dot.setOpaque(false);

        JLabel lbl = new JLabel(text);
        lbl.setFont(uiFont(Font.PLAIN, 11));
        lbl.setForeground(TEXT_MUTED);

        pnl.add(dot);
        pnl.add(lbl);
        return pnl;
    }

    private void loadAvailableTables() {
        pnlTableGrid.removeAll();

        int soKhach = (int) spinGuests.getValue();
        Date date = dateChooser.getDate();
        if (date == null || selectedHour < 0)
            return;

        Calendar calStart = Calendar.getInstance();
        calStart.setTime(date);
        calStart.set(Calendar.HOUR_OF_DAY, selectedHour);
        calStart.set(Calendar.MINUTE, selectedMinute);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        double durationHours = getDurationHours();
        Calendar calEnd = (Calendar) calStart.clone();
        calEnd.add(Calendar.MINUTE, (int) (durationHours * 60));

        String dateStr = new java.text.SimpleDateFormat("dd/MM/yyyy").format(date);
        String timeStr = String.format("%02d:%02d", selectedHour, selectedMinute);
        lblTableInfo.setText(String.format(
                "<html>Ngày: %s  •  Giờ: %s  •  Số khách: %d  •  Thời lượng: %s</html>",
                dateStr, timeStr, soKhach, cboThoiLuong.getSelectedItem()));

        // Load all tables
        ArrayList<Ban> allTables = banDAO.getAllBan();

        // Áp dụng bộ lọc khu vực
        String selectedZoneId = getSelectedZoneId();
        if (selectedZoneId != null) {
            allTables.removeIf(t -> !selectedZoneId.equals(t.getMaKV()));
        }

        // Get bookings that conflict with this time
        ArrayList<DatBan> conflicts = datBanDAO.getDanhSachDatBan(calStart.getTime(), calEnd.getTime());
        java.util.Set<String> bookedTableIds = new java.util.HashSet<>();
        for (DatBan db : conflicts) {
            String st = db.getTrangThai().toLowerCase();
            if (!st.startsWith("đã hủy") && !st.contains("hoàn thành") && !st.contains("hoàn tất")) {
                // Check actual time overlap
                long targetStart = calStart.getTimeInMillis();
                long targetEnd = calEnd.getTimeInMillis();
                long bookStart = db.getThoiGianBatDau().getTime();
                long bookEnd = db.getThoiGianKetThuc().getTime();

                if (targetStart < bookEnd && targetEnd > bookStart) {
                    for (String maBan : db.getDanhSachBan()) {
                        bookedTableIds.add(maBan);
                    }
                }
            }
        }

        // Tính sức chứa tối đa của 1 bàn TRỐNG (chỉ tính bàn available)
        maxSingleTableCapacity = 0;
        for (Ban table : allTables) {
            boolean isBooked = bookedTableIds.contains(table.getMaBan());
            boolean isOccupied = "Có Khách".equals(table.getTrangThai()) || "Đang Gộp".equals(table.getTrangThai());
            if (!isBooked && !isOccupied) {
                if (table.getSoGhe() > maxSingleTableCapacity) {
                    maxSingleTableCapacity = table.getSoGhe();
                }
            }
        }

        // Sort: available first, then by capacity match
        allTables.sort((a, b) -> {
            boolean aBooked = bookedTableIds.contains(a.getMaBan())
                    || "Có Khách".equals(a.getTrangThai()) || "Đang Gộp".equals(a.getTrangThai());
            boolean bBooked = bookedTableIds.contains(b.getMaBan())
                    || "Có Khách".equals(b.getTrangThai()) || "Đang Gộp".equals(b.getTrangThai());

            if (aBooked != bBooked)
                return aBooked ? 1 : -1;

            // Sort available tables by best capacity match
            int aDiff = Math.abs(a.getSoGhe() - soKhach);
            int bDiff = Math.abs(b.getSoGhe() - soKhach);
            return Integer.compare(aDiff, bDiff);
        });

        int availableCount = 0;
        for (Ban table : allTables) {
            boolean isBooked = bookedTableIds.contains(table.getMaBan());
            boolean isOccupied = "Có Khách".equals(table.getTrangThai()) || "Đang Gộp".equals(table.getTrangThai());
            boolean isAvailable = !isBooked && !isOccupied;
            boolean isFit = table.getSoGhe() >= soKhach;

            if (isAvailable)
                availableCount++;

            pnlTableGrid.add(createTableCard(table, isAvailable, isFit, isBooked));
        }

        // Kiểm tra xem có cần chọn nhiều bàn không
        boolean needMultiTable = soKhach > maxSingleTableCapacity;

        // Update info
        String multiHint = needMultiTable
                ? String.format("  •  <b style='color:#f59e0b'>Cần chọn nhiều bàn (%d > %d/bàn)</b>", soKhach,
                        maxSingleTableCapacity)
                : "";
        lblTableInfo.setText(String.format(
                "<html>Ngày: %s  •  Giờ: %s  •  Số khách: %d  •  Thời lượng: %s  •  <b style='color:#22c55e'>%d bàn trống</b>%s</html>",
                dateStr, timeStr, soKhach, cboThoiLuong.getSelectedItem(), availableCount, multiHint));


        // Update capacity status
        updateCapacityStatus(allTables);

        pnlTableGrid.revalidate();
        pnlTableGrid.repaint();
    }

    /**
     * Cập nhật label hiển thị sức chứa tổng cộng vs số khách cần.
     */
    private void updateCapacityStatus(ArrayList<Ban> allTables) {
        if (lblCapacityStatus == null)
            return;
        int soKhach = (int) spinGuests.getValue();
        int tongSucChua = getTotalSelectedCapacity(allTables);
        int soBanChon = selectedTableIds.size();

        if (soBanChon == 0) {
            lblCapacityStatus.setText("Chưa chọn bàn nào");
            lblCapacityStatus.setForeground(TEXT_MUTED);
            return;
        }

        String tableListStr = String.join(", ", selectedTableNames);

        if (tongSucChua >= soKhach) {
            // Đủ chỗ
            lblCapacityStatus.setText(String.format(
                    "<html>Đã chọn <b>%d bàn</b> (%s) — Sức chứa: <b style='color:#22c55e'>%d/%d khách</b></html>",
                    soBanChon, tableListStr, tongSucChua, soKhach));
            lblCapacityStatus.setForeground(SUCCESS);
        } else {
            // Chưa đủ chỗ — cần chọn thêm
            int conThieu = soKhach - tongSucChua;
            lblCapacityStatus.setText(String.format(
                    "<html>Đã chọn <b>%d bàn</b> (%s) — Sức chứa: <b style='color:#ef4444'>%d/%d khách</b> — <b style='color:#f59e0b'>Cần thêm %d chỗ!</b></html>",
                    soBanChon, tableListStr, tongSucChua, soKhach, conThieu));
            lblCapacityStatus.setForeground(WARNING);
        }
    }

    /**
     * Tính tổng sức chứa các bàn đang chọn.
     */
    private int getTotalSelectedCapacity(ArrayList<Ban> allTables) {
        int total = 0;
        for (Ban table : allTables) {
            if (selectedTableIds.contains(table.getMaBan())) {
                total += table.getSoGhe();
            }
        }
        return total;
    }

    /**
     * Kiểm tra xem có cần chọn nhiều bàn không (số khách > sức chứa tối đa 1 bàn).
     */
    private boolean isMultiTableMode() {
        int soKhach = (int) spinGuests.getValue();
        return soKhach > maxSingleTableCapacity && maxSingleTableCapacity > 0;
    }

    private JPanel createTableCard(Ban table, boolean available, boolean fits, boolean booked) {
        boolean isSelected = selectedTableIds.contains(table.getMaBan());
        boolean multiMode = isMultiTableMode();

        JPanel card = new JPanel(new BorderLayout(scaled(8), scaled(4))) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), scaled(14), scaled(14)));
                // Border
                boolean sel = selectedTableIds.contains(table.getMaBan());
                if (sel) {
                    g2.setColor(ACCENT);
                    g2.setStroke(new BasicStroke(2.5f));
                } else {
                    g2.setColor(BORDER);
                    g2.setStroke(new BasicStroke(1f));
                }
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, scaled(14), scaled(14)));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(uiPadding(12, 14, 12, 14));
        card.setPreferredSize(new Dimension(0, scaled(104)));

        // Background color
        if (isSelected) {
            card.setBackground(ACCENT_LIGHT);
        } else if (!available) {
            card.setBackground(booked ? new Color(254, 242, 242) : new Color(243, 244, 246));
        } else if (!fits && !multiMode) {
            // Nếu ở chế độ multi-table, không đánh dấu "ít chỗ" vì sẽ cộng dồn
            card.setBackground(WARNING_LIGHT);
        } else {
            card.setBackground(SUCCESS_LIGHT);
        }

        // Table icon + name
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblIcon = new JLabel(table.getTenBan());
        lblIcon.setFont(uiFont(Font.BOLD, 14));
        lblIcon.setForeground(isSelected ? ACCENT_DARK : TEXT_PRIMARY);

        // Zone label
        String zoneName = getZoneName(table.getMaKV());
        JLabel lblZone = new JLabel(zoneName);
        lblZone.setFont(uiFont(Font.PLAIN, 10));
        lblZone.setForeground(TEXT_MUTED);

        pnlTop.add(lblIcon, BorderLayout.WEST);
        pnlTop.add(lblZone, BorderLayout.EAST);

        // Capacity + Status
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setOpaque(false);

        JLabel lblCap = new JLabel(table.getSoGhe() + " chỗ ngồi");
        lblCap.setFont(uiFont(Font.PLAIN, 12));
        lblCap.setForeground(TEXT_MUTED);

        String statusText;
        Color statusColor;
        if (!available) {
            statusText = booked ? "Đã đặt" : "Có khách";
            statusColor = DANGER;
        } else if (isSelected) {
            statusText = "Đã chọn";
            statusColor = ACCENT;
        } else if (fits || multiMode) {
            statusText = multiMode ? (table.getSoGhe() + " chỗ") : "Phù hợp";
            statusColor = SUCCESS;
        } else {
            statusText = "Ít chỗ";
            statusColor = WARNING;
        }
        JLabel lblStatus = new JLabel(statusText);
        lblStatus.setFont(uiFont(Font.BOLD, 11));
        lblStatus.setForeground(statusColor);

        pnlBottom.add(lblCap, BorderLayout.WEST);
        pnlBottom.add(lblStatus, BorderLayout.EAST);

        card.add(pnlTop, BorderLayout.NORTH);
        card.add(pnlBottom, BorderLayout.SOUTH);

        // Click interaction
        if (available) {
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Luôn cho phép chọn/bỏ chọn nhiều bàn (toggle)
                    if (selectedTableIds.contains(table.getMaBan())) {
                        selectedTableIds.remove(table.getMaBan());
                        selectedTableNames.remove(table.getTenBan());
                    } else {
                        selectedTableIds.add(table.getMaBan());
                        selectedTableNames.add(table.getTenBan());
                    }
                    refreshTableSelection();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!selectedTableIds.contains(table.getMaBan())) {
                        card.setBackground(new Color(219, 234, 254));
                        card.repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!selectedTableIds.contains(table.getMaBan())) {
                        if (fits || multiMode)
                            card.setBackground(SUCCESS_LIGHT);
                        else
                            card.setBackground(WARNING_LIGHT);
                        card.repaint();
                    }
                }
            });
        }

        // Selected indicator
        if (isSelected) {
            JLabel lblCheck = new JLabel(selectedTableIds.size() > 1
                    ? "Chọn [" + (selectedTableIds.indexOf(table.getMaBan()) + 1) + "]"
                    : "Đã chọn");
            lblCheck.setFont(uiFont(Font.BOLD, 16));
            lblCheck.setForeground(ACCENT);
            lblCheck.setHorizontalAlignment(SwingConstants.CENTER);
            card.add(lblCheck, BorderLayout.CENTER);
        }

        return card;
    }

    private void refreshTableSelection() {
        // Rebuild the table grid with updated selection
        loadAvailableTables();
    }

    /**
     * Trả về mã khu vực (KV01, KV02, ...) tương ứng với lựa chọn trong
     * cboZoneFilter.
     * Trả về null nếu chọn "Tất cả".
     */
    private String getSelectedZoneId() {
        if (cboZoneFilter == null)
            return null;
        int idx = cboZoneFilter.getSelectedIndex();
        switch (idx) {
            case 1:
                return "KV01"; // Tầng G
            case 2:
                return "KV02"; // Tầng 1
            case 3:
                return "KV03"; // VIP
            case 4:
                return "KV04"; // Ngoài trời
            default:
                return null; // Tất cả
        }
    }

    /**
     * Gợi ý tổ hợp bàn tối ưu cho số khách.
     * Thuật toán: ưu tiên 1 bàn phù hợp nhất, nếu không đủ thì
     * chọn tổ hợp bàn trống có tổng sức chứa ≥ số khách với ít bàn nhất.
     */
    private void suggestTables() {
        Date date = dateChooser.getDate();
        if (date == null || selectedHour < 0) {
            showError("Vui lòng chọn ngày và giờ trước!");
            return;
        }

        int soKhach = (int) spinGuests.getValue();

        // Lấy danh sách bàn trống trong khung giờ này
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(date);
        calStart.set(Calendar.HOUR_OF_DAY, selectedHour);
        calStart.set(Calendar.MINUTE, selectedMinute);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        double durationHours = getDurationHours();
        Calendar calEnd = (Calendar) calStart.clone();
        calEnd.add(Calendar.MINUTE, (int) (durationHours * 60));

        ArrayList<Ban> allTables = banDAO.getAllBan();

        // Áp dụng bộ lọc khu vực
        String selectedZoneId = getSelectedZoneId();
        if (selectedZoneId != null) {
            allTables.removeIf(t -> !selectedZoneId.equals(t.getMaKV()));
        }

        // Xác định bàn đã bị đặt
        ArrayList<DatBan> conflicts = datBanDAO.getDanhSachDatBan(calStart.getTime(), calEnd.getTime());
        java.util.Set<String> bookedIds = new java.util.HashSet<>();
        for (DatBan db : conflicts) {
            String st = db.getTrangThai().toLowerCase();
            if (!st.startsWith("đã hủy") && !st.contains("hoàn thành") && !st.contains("hoàn tất")) {
                long tS = calStart.getTimeInMillis(), tE = calEnd.getTimeInMillis();
                long bS = db.getThoiGianBatDau().getTime(), bE = db.getThoiGianKetThuc().getTime();
                if (tS < bE && tE > bS) {
                    for (String mb : db.getDanhSachBan())
                        bookedIds.add(mb);
                }
            }
        }

        // Lọc bàn trống
        ArrayList<Ban> available = new ArrayList<>();
        for (Ban t : allTables) {
            boolean isBooked = bookedIds.contains(t.getMaBan());
            boolean isOccupied = "Có Khách".equals(t.getTrangThai()) || "Đang Gộp".equals(t.getTrangThai());
            if (!isBooked && !isOccupied)
                available.add(t);
        }

        if (available.isEmpty()) {
            showError("Không có bàn trống nào trong khung giờ này!");
            return;
        }

        // Thuật toán: tìm tổ hợp tối ưu
        // 1. Thử tìm 1 bàn vừa đủ (sức chứa >= soKhach, nhỏ nhất có thể)
        Ban bestSingle = null;
        for (Ban t : available) {
            if (t.getSoGhe() >= soKhach) {
                if (bestSingle == null || t.getSoGhe() < bestSingle.getSoGhe()) {
                    bestSingle = t;
                }
            }
        }

        selectedTableIds.clear();
        selectedTableNames.clear();

        if (bestSingle != null) {
            // Tìm được 1 bàn phù hợp
            selectedTableIds.add(bestSingle.getMaBan());
            selectedTableNames.add(bestSingle.getTenBan());
        } else {
            // 2. Cần ghép nhiều bàn — sắp xếp giảm dần sức chứa
            available.sort((a, b) -> Integer.compare(b.getSoGhe(), a.getSoGhe()));
            int remaining = soKhach;
            for (Ban t : available) {
                if (remaining <= 0)
                    break;
                selectedTableIds.add(t.getMaBan());
                selectedTableNames.add(t.getTenBan());
                remaining -= t.getSoGhe();
            }
            if (remaining > 0) {
                showError(String.format("Không đủ bàn trống! Còn thiếu %d chỗ ngồi.", remaining));
                selectedTableIds.clear();
                selectedTableNames.clear();
            }
        }

        refreshTableSelection();
    }

    private String getZoneName(String maKV) {
        if (maKV == null)
            return "";
        switch (maKV) {
            case "KV01":
                return "Tầng G";
            case "KV02":
                return "Tầng 1";
            case "KV03":
                return "VIP";
            case "KV04":
                return "Ngoài trời";
            default:
                return maKV;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // STEP 3: Thông tin khách + Xác nhận
    // ═══════════════════════════════════════════════════════════════════
    private JPanel createStep3() {
        JPanel pnl = new JPanel(new BorderLayout(0, scaled(15)));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(uiPadding(28, 48, 12, 48));

        // ── Summary Card ─────────────────────────────────────────────
        JPanel pnlSummary = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(239, 246, 255), getWidth(), getHeight(),
                        new Color(224, 242, 254));
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), scaled(16), scaled(16)));
                g2.setColor(new Color(186, 230, 253));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, scaled(16), scaled(16)));
                g2.dispose();
            }
        };
        pnlSummary.setLayout(new FlowLayout(FlowLayout.LEFT, scaled(25), scaled(12)));
        pnlSummary.setOpaque(false);
        pnlSummary.setPreferredSize(new Dimension(0, scaled(76)));

        pnl.add(pnlSummary, BorderLayout.NORTH);

        // ── Customer Form ────────────────────────────────────────────
        JPanel pnlForm = new JPanel();
        pnlForm.setLayout(new BoxLayout(pnlForm, BoxLayout.Y_AXIS));
        pnlForm.setOpaque(false);

        JLabel lblTitle = new JLabel("Thông tin khách hàng");
        lblTitle.setFont(uiFont(Font.BOLD, 16));
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblTitle);
        pnlForm.add(Box.createVerticalStrut(scaled(15)));

        // Phone
        pnlForm.add(createFormLabel("Số điện thoại *"));
        txtSDT = new JTextField();
        txtSDT.setFont(uiFont(Font.PLAIN, 14));
        txtSDT.setMaximumSize(new Dimension(Integer.MAX_VALUE, scaled(50)));
        txtSDT.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                uiPadding(8, 12, 8, 12)));
        txtSDT.putClientProperty("JTextField.placeholderText", "Nhập số điện thoại...");
        txtSDT.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                checkKhachHang();
            }
        });
        pnlForm.add(txtSDT);
        pnlForm.add(Box.createVerticalStrut(scaled(10)));

        // Customer status
        lblCustomerStatus = new JLabel(" ");
        lblCustomerStatus.setFont(uiFont(Font.ITALIC, 12));
        lblCustomerStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlForm.add(lblCustomerStatus);
        pnlForm.add(Box.createVerticalStrut(scaled(5)));

        // Name
        pnlForm.add(createFormLabel("Tên khách hàng *"));
        txtTenKH = new JTextField();
        txtTenKH.setFont(uiFont(Font.PLAIN, 14));
        txtTenKH.setMaximumSize(new Dimension(Integer.MAX_VALUE, scaled(50)));
        txtTenKH.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                uiPadding(8, 12, 8, 12)));
        txtTenKH.putClientProperty("JTextField.placeholderText", "Nhập tên khách hàng...");
        pnlForm.add(txtTenKH);
        pnlForm.add(Box.createVerticalStrut(scaled(12)));

        // Notes
        pnlForm.add(createFormLabel("Ghi chú (tùy chọn)"));
        txtGhiChu = new JTextField();
        txtGhiChu.setFont(uiFont(Font.PLAIN, 14));
        txtGhiChu.setMaximumSize(new Dimension(Integer.MAX_VALUE, scaled(50)));
        txtGhiChu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                uiPadding(8, 12, 8, 12)));
        txtGhiChu.putClientProperty("JTextField.placeholderText", "VD: Kỷ niệm sinh nhật, ghế trẻ em...");
        pnlForm.add(txtGhiChu);

        pnl.add(pnlForm, BorderLayout.CENTER);

        return pnl;
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(uiFont(Font.BOLD, 13));
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(uiPadding(0, 0, 5, 0));
        return lbl;
    }

    private void updateSummaryPanel() {
        // Find the summary panel in step 3
        JPanel step3 = (JPanel) pnlStepContent.getComponent(2);
        JPanel pnlSummary = (JPanel) step3.getComponent(0);
        pnlSummary.removeAll();

        String dateStr = dateChooser.getDate() != null
                ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(dateChooser.getDate())
                : "...";
        String timeStr = selectedHour >= 0
                ? String.format("%02d:%02d", selectedHour, selectedMinute)
                : "...";
        String tableNamesStr = !selectedTableNames.isEmpty()
                ? String.join(", ", selectedTableNames)
                : "...";

        pnlSummary.add(createSummaryBadge("Ngày:", dateStr));
        pnlSummary.add(createSummaryBadge("Giờ:", timeStr));
        pnlSummary.add(createSummaryBadge("Khách:", spinGuests.getValue() + " khách"));
        pnlSummary.add(createSummaryBadge("Bàn:", tableNamesStr));
        pnlSummary.add(createSummaryBadge("Thời lượng:", cboThoiLuong.getSelectedItem().toString()));

        pnlSummary.revalidate();
        pnlSummary.repaint();
    }

    private JLabel createSummaryBadge(String icon, String text) {
        JLabel lbl = new JLabel(icon + " " + text);
        lbl.setFont(uiFont(Font.BOLD, 13));
        lbl.setForeground(ACCENT_DARK);
        return lbl;
    }

    private void checkKhachHang() {
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty())
            return;

        String ten = khachHangDAO.getTenKhachHang(sdt);
        if (ten != null && !ten.isEmpty()) {
            txtTenKH.setText(ten);
            txtTenKH.setEditable(false);
            txtTenKH.setBackground(new Color(243, 244, 246));
            lblCustomerStatus.setText("Khách hàng thân thiết: " + ten);
            lblCustomerStatus.setForeground(SUCCESS);
        } else {
            txtTenKH.setEditable(true);
            txtTenKH.setBackground(Color.WHITE);
            lblCustomerStatus.setText("Khách mới — vui lòng nhập tên");
            lblCustomerStatus.setForeground(ACCENT);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════════════
    private JPanel createNavigation() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(249, 250, 251));
        pnl.setBorder(uiPadding(14, 48, 14, 48));

        // Prev button
        btnPrev = new JButton("←  Quay lại");
        btnPrev.setFont(uiFont(Font.BOLD, 13));
        btnPrev.setFocusPainted(false);
        btnPrev.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnPrev.setBackground(Color.WHITE);
        btnPrev.setForeground(TEXT_PRIMARY);
        btnPrev.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                uiPadding(10, 20, 10, 20)));
        btnPrev.setVisible(false);
        btnPrev.addActionListener(e -> prevStep());

        // Cancel button
        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(uiFont(Font.PLAIN, 13));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setForeground(TEXT_MUTED);
        btnCancel.setBorder(uiPadding(10, 20, 10, 20));
        btnCancel.addActionListener(e -> dispose());

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, scaled(8), 0));
        pnlLeft.setOpaque(false);
        pnlLeft.add(btnCancel);
        pnlLeft.add(btnPrev);

        // Next button
        btnNext = new JButton("Tiếp theo  →") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), scaled(10), scaled(10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnNext.setFont(uiFont(Font.BOLD, 14));
        btnNext.setFocusPainted(false);
        btnNext.setContentAreaFilled(false);
        btnNext.setBorderPainted(false);
        btnNext.setOpaque(false);
        btnNext.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNext.setBackground(ACCENT);
        btnNext.setForeground(Color.WHITE);
        btnNext.setBorder(uiPadding(10, 28, 10, 28));
        btnNext.setPreferredSize(uiSize(220, 52));
        btnNext.addActionListener(e -> nextStep());

        // Hover effect
        btnNext.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (currentStep == TOTAL_STEPS) {
                    btnNext.setBackground(new Color(22, 163, 74));
                } else {
                    btnNext.setBackground(ACCENT_DARK);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (currentStep == TOTAL_STEPS) {
                    btnNext.setBackground(SUCCESS);
                } else {
                    btnNext.setBackground(ACCENT);
                }
            }
        });

        pnl.add(pnlLeft, BorderLayout.WEST);
        pnl.add(btnNext, BorderLayout.EAST);

        return pnl;
    }

    private void nextStep() {
        if (currentStep == 1) {
            // Validate Step 1
            if (dateChooser.getDate() == null) {
                showError("Vui lòng chọn ngày đặt bàn!");
                return;
            }
            if (selectedHour < 0) {
                showError("Vui lòng chọn khung giờ!");
                return;
            }
            // Validate past time
            Calendar calCheck = Calendar.getInstance();
            Calendar calSlot = Calendar.getInstance();
            calSlot.setTime(dateChooser.getDate());
            calSlot.set(Calendar.HOUR_OF_DAY, selectedHour);
            calSlot.set(Calendar.MINUTE, selectedMinute);
            calSlot.set(Calendar.SECOND, 0);
            if (calSlot.before(calCheck)) {
                showError("Thời gian đặt không thể trong quá khứ!");
                return;
            }

            currentStep = 2;
            selectedTableIds.clear(); // Reset table selection
            selectedTableNames.clear();
            loadAvailableTables();

        } else if (currentStep == 2) {
            // Validate Step 2
            if (selectedTableIds.isEmpty()) {
                showError("Vui lòng chọn ít nhất một bàn!");
                return;
            }

            // Validate tổng sức chứa >= số khách
            int soKhach = (int) spinGuests.getValue();
            ArrayList<Ban> allTables = banDAO.getAllBan();
            int tongSucChua = getTotalSelectedCapacity(allTables);

            if (tongSucChua < soKhach) {
                showError(String.format(
                        "Tổng sức chứa các bàn đã chọn (%d chỗ) không đủ cho %d khách!\n" +
                                "Vui lòng chọn thêm bàn hoặc giảm số khách.",
                        tongSucChua, soKhach));
                return;
            }

            currentStep = 3;
            updateSummaryPanel();

        } else if (currentStep == 3) {
            // Submit!
            saveBooking();
            return;
        }

        cardStep.show(pnlStepContent, "step" + currentStep);
        updateStepperUI();
    }

    private void prevStep() {
        if (currentStep > 1) {
            if (currentStep == 2) {
                // Reset table selection khi quay lại step 1
                selectedTableIds.clear();
                selectedTableNames.clear();
            }
            currentStep--;
            cardStep.show(pnlStepContent, "step" + currentStep);
            updateStepperUI();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SAVE BOOKING
    // ═══════════════════════════════════════════════════════════════════
    private void saveBooking() {
        // 1. Validate
        String ten = txtTenKH.getText().trim();
        String sdt = txtSDT.getText().trim();

        if (ten.isEmpty() || sdt.isEmpty()) {
            showError("Vui lòng nhập tên và số điện thoại khách!");
            return;
        }

        if (!sdt.matches("^0\\d{9}$")) {
            showError("Số điện thoại không hợp lệ! (10 chữ số, bắt đầu bằng 0)");
            return;
        }

        // 2. Prepare dates
        Date date = dateChooser.getDate();
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(date);
        calStart.set(Calendar.HOUR_OF_DAY, selectedHour);
        calStart.set(Calendar.MINUTE, selectedMinute);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        double durationHours = getDurationHours();
        Calendar calEnd = (Calendar) calStart.clone();
        calEnd.add(Calendar.MINUTE, (int) (durationHours * 60));

        // 3. Double-check conflict cho tất cả bàn đã chọn
        for (String tableId : selectedTableIds) {
            int conflicts = datBanDAO.kiemTraXungDot(tableId, calStart.getTime(), calEnd.getTime());
            if (conflicts > 0) {
                showError("Bàn " + tableId + " đã có người đặt trong khung giờ " +
                        String.format("%02d:%02d", calStart.get(Calendar.HOUR_OF_DAY), calStart.get(Calendar.MINUTE)) +
                        " - " +
                        String.format("%02d:%02d", calEnd.get(Calendar.HOUR_OF_DAY), calEnd.get(Calendar.MINUTE)));
                return;
            }
        }

        // 4. Save Customer
        if (!khachHangDAO.checkTonTai(sdt)) {
            boolean created = khachHangDAO.themKhachMoi(sdt, ten);
            if (!created) {
                System.err.println("Warning: Failed to auto-create customer: " + sdt);
            }
        }

        // 5. Insert Booking (hỗ trợ nhiều bàn)
        DatBan db = new DatBan(
                new ArrayList<>(selectedTableIds), ten, sdt,
                calStart.getTime(), calEnd.getTime(),
                (int) spinGuests.getValue(),
                0.0, // Tiền cọc
                txtGhiChu.getText().trim());

        if (datBanDAO.insertDatBan(db)) {
            showSuccessDialog();
            if (onSuccessCallback != null)
                onSuccessCallback.run();
            dispose();
        } else {
            showError("Lỗi khi lưu đặt bàn! Vui lòng thử lại.");
        }
    }

    private double getDurationHours() {
        String sel = cboThoiLuong.getSelectedItem().toString();
        if (sel.contains("1.5"))
            return 1.5;
        if (sel.contains("1 "))
            return 1;
        if (sel.contains("2"))
            return 2;
        if (sel.contains("3"))
            return 3;
        if (sel.contains("4"))
            return 4;
        return 2;
    }

    private int getDurationMinutes() {
        return (int) Math.round(getDurationHours() * 60);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.WARNING_MESSAGE);
    }

    private void showSuccessDialog() {
        String dateStr = new java.text.SimpleDateFormat("dd/MM/yyyy").format(dateChooser.getDate());
        String timeStr = String.format("%02d:%02d", selectedHour, selectedMinute);
        String tableNamesStr = String.join(", ", selectedTableNames);

        String msg = String.format(
                "<html><div style='text-align:center; padding:10px;'>" +
                        "<h2 style='color:#22c55e;'>Đặt bàn thành công!</h2>" +
                        "<p style='font-size:12px;'>Bàn: <b>%s</b></p>" +
                        "<p style='font-size:12px;'>Ngày: <b>%s</b> — Giờ: <b>%s</b></p>" +
                        "<p style='font-size:12px;'>Số khách: <b>%s</b></p>" +
                        "<p style='font-size:12px;'>Khách: <b>%s</b> — SĐT: <b>%s</b></p>" +
                        "</div></html>",
                tableNamesStr, dateStr, timeStr,
                spinGuests.getValue(), txtTenKH.getText().trim(), txtSDT.getText().trim());

        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.PLAIN_MESSAGE);
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPER CLASS
    // ═══════════════════════════════════════════════════════════════════
    static class ComboItem {
        private String label;
        private String value;

        public ComboItem(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
