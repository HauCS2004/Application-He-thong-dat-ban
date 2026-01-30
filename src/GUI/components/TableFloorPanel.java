package GUI.components;

import Entity.Ban;
import DAO.BanDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * TableFloorPanel - Container managing table layout by floor/zone
 * Displays tables in a grid with status-based filtering
 */
public class TableFloorPanel extends JPanel {

    private String khuVuc; // Floor/Zone ID
    private ArrayList<TableCard> tableCards;
    private JPanel pnlTableGrid;
    private TableCard.TableCardListener cardListener;
    private BanDAO banDAO;

    public TableFloorPanel(String khuVuc) {
        this.khuVuc = khuVuc;
        this.tableCards = new ArrayList<>();
        this.banDAO = new BanDAO();

        initUI();
        loadTables();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(249, 250, 251)); // Light gray background
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header with zone name and stats
        JPanel pnlHeader = createHeader();
        add(pnlHeader, BorderLayout.NORTH);

        // Table grid with scroll
        // Use custom ResponsivePanel to force FlowLayout to wrap
        pnlTableGrid = new ResponsiveGridPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        pnlTableGrid.setBackground(new Color(249, 250, 251));

        JScrollPane scroll = new JScrollPane(pnlTableGrid);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(249, 250, 251));

        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);

        // Zone name
        JLabel lblZone = new JLabel(getZoneName(khuVuc));
        lblZone.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblZone.setForeground(new Color(31, 41, 55));

        // Status legend
        JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlLegend.setOpaque(false);

        pnlLegend.add(createLegendItem("Trống", new Color(34, 197, 94)));
        pnlLegend.add(createLegendItem("Có khách", new Color(239, 68, 68)));
        pnlLegend.add(createLegendItem("Đã đặt", new Color(250, 204, 21)));
        pnlLegend.add(createLegendItem("Đang dọn", new Color(156, 163, 175)));

        pnl.add(lblZone, BorderLayout.WEST);
        pnl.add(pnlLegend, BorderLayout.EAST);

        return pnl;
    }

    private JPanel createLegendItem(String text, Color color) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnl.setOpaque(false);

        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(12, 12));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(107, 114, 128));

        pnl.add(colorBox);
        pnl.add(lbl);

        return pnl;
    }

    private String getZoneName(String khuVuc) {
        Map<String, String> zoneNames = new HashMap<>();
        zoneNames.put("KV01", "Tầng G");
        zoneNames.put("KV02", "Tầng 1");
        zoneNames.put("KV03", "VIP Room");
        zoneNames.put("KV04", "Ngoài trời");

        return zoneNames.getOrDefault(khuVuc, khuVuc);
    }

    public void loadTables() {
        // Clear existing cards
        pnlTableGrid.removeAll();
        tableCards.clear();

        // Load tables from DAO
        ArrayList<Ban> tables;
        if ("ALL".equals(khuVuc)) {
            tables = banDAO.getAllBan();
        } else {
            tables = banDAO.getBanTheoKhuVuc(khuVuc);
        }

        for (Ban table : tables) {
            TableCard card = new TableCard(table);

            if (cardListener != null) {
                card.setTableCardListener(cardListener);
            }

            tableCards.add(card);
            pnlTableGrid.add(card);
        }

        pnlTableGrid.revalidate();
        pnlTableGrid.repaint();
    }

    public void setTableCardListener(TableCard.TableCardListener listener) {
        // Wrapper to enforce Single Selection
        TableCard.TableCardListener wrapper = new TableCard.TableCardListener() {
            @Override
            public void onTableCardClicked(Ban table) {
                // 1. Deselect ALL cards first
                deselectAllCards();

                // 2. Select ONLY the clicked card (Visual update)
                for (TableCard card : tableCards) {
                    if (card.getTable().getMaBan().equals(table.getMaBan())) {
                        card.setSelected(true);
                        break;
                    }
                }

                // 3. Propagate event to original listener
                if (listener != null) {
                    listener.onTableCardClicked(table);
                }
            }
        };

        this.cardListener = wrapper;

        // Apply wrapper to existing cards
        for (TableCard card : tableCards) {
            card.setTableCardListener(wrapper);
        }
    }

    public void refreshTables() {
        loadTables();
    }

    public void deselectAllCards() {
        for (TableCard card : tableCards) {
            card.setSelected(false);
        }
    }

    public ArrayList<TableCard> getTableCards() {
        return tableCards;
    }

    // --- NEW: Update statuses for availability check ---
    // --- NEW: Update statuses for availability check ---
    // --- NEW: Update statuses for availability check ---
    public void updateTableStatuses(java.util.Map<String, Entity.DatBan> bookingMap) {
        pnlTableGrid.removeAll(); // Reset Grid

        for (TableCard card : tableCards) {
            String id = card.getTable().getMaBan();

            if (bookingMap.containsKey(id)) {
                // Table is OCCUPIED in this time slot -> HIDE IT
                card.setVisible(false);
            } else {
                // Table is AVAILABLE -> SHOW IT
                card.setTemporaryStatus("Trống");
                card.setVisible(true);
            }
            // Ensure card is added (ResponsiveGridPanel handles visibility)
            pnlTableGrid.add(card);
        }
        pnlTableGrid.revalidate();
        pnlTableGrid.repaint();
    }

    public void restoreOriginalStatuses() {
        pnlTableGrid.removeAll(); // Reset Grid
        for (TableCard card : tableCards) {
            card.restoreStatus();
            card.setVisible(true);
            pnlTableGrid.add(card);
        }
        pnlTableGrid.revalidate();
        pnlTableGrid.repaint();
    }

    /**
     * Updates the internal Ban objects with fresh data from DB
     */
    public void refreshTableData(java.util.List<Ban> freshTables) {
        for (TableCard card : tableCards) {
            String myId = card.getTable().getMaBan();
            for (Ban fresh : freshTables) {
                if (fresh.getMaBan().equals(myId)) {
                    // Update the card's table entity
                    card.setTable(fresh);
                    // Refresh visual immediately
                    card.restoreStatus();
                    break;
                }
            }
        }
        revalidate();
        repaint();
    }

    // --- Helper Class for Responsive wrapping ---
    private class ResponsiveGridPanel extends JPanel implements Scrollable {
        public ResponsiveGridPanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredSize() {
            // If inside a scroll pane, calculate height based on available width
            if (getParent() instanceof JViewport) {
                int targetWidth = getParent().getWidth();
                if (targetWidth <= 0)
                    return super.getPreferredSize();

                int nmembers = getComponentCount();
                int x = 0, y = 0;
                int rowHeight = 0;
                int maxRowWidth = 0;
                int startX = 0;

                FlowLayout fl = (FlowLayout) getLayout();
                int hgap = fl.getHgap();
                int vgap = fl.getVgap();
                Insets insets = getInsets();

                x = insets.left + hgap;
                y = insets.top + vgap;

                for (int i = 0; i < nmembers; i++) {
                    Component m = getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = m.getPreferredSize();
                        m.setSize(d.width, d.height);

                        // Check if wraps
                        if ((x == insets.left + hgap) || ((x + d.width) <= (targetWidth - insets.right))) {
                            // Fits in current row
                            if (x > insets.left + hgap)
                                x += hgap;
                            x += d.width;
                            rowHeight = Math.max(rowHeight, d.height);
                        } else {
                            // Start new row
                            x = insets.left + hgap + d.width;
                            y += vgap + rowHeight;
                            rowHeight = d.height;
                        }
                    }
                }
                y += rowHeight + vgap + insets.bottom;
                return new Dimension(targetWidth, y);
            }
            return super.getPreferredSize();
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 20; // Scroll speed
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 100;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true; // Force width to match viewport -> FlowLayout wraps
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
