package GUI.components;

import Entity.Ban;
import DAO.BanDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

/**
 * TableFloorPanelAll - Shows ALL tables from all zones
 */
public class TableFloorPanelAll extends JPanel {

    private ArrayList<TableCard> tableCards;
    private JPanel pnlTableGrid;
    private TableCard.TableCardListener cardListener;
    private BanDAO banDAO;

    public TableFloorPanelAll() {
        this.tableCards = new ArrayList<>();
        this.banDAO = new BanDAO();

        initUI();
        loadAllTables();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(249, 250, 251));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header
        JPanel pnlHeader = createHeader();
        add(pnlHeader, BorderLayout.NORTH);

        // Table grid
        pnlTableGrid = new JPanel();
        pnlTableGrid.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 12));
        pnlTableGrid.setBackground(new Color(249, 250, 251));

        JScrollPane scroll = new JScrollPane(pnlTableGrid);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(249, 250, 251));

        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);

        JLabel lblZone = new JLabel("Tất cả các bàn");
        lblZone.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblZone.setForeground(new Color(31, 41, 55));

        // Status legend
        JPanel pnlLegend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlLegend.setOpaque(false);

        pnlLegend.add(createLegendItem("Trống", new Color(34, 197, 94)));
        pnlLegend.add(createLegendItem("Có khách", new Color(239, 68, 68)));
        pnlLegend.add(createLegendItem("Đã đặt", new Color(250, 204, 21)));

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

    public void loadAllTables() {
        pnlTableGrid.removeAll();
        tableCards.clear();

        // Load tables from ALL zones
        String[] zones = { "KV01", "KV02", "KV03", "KV04" };

        for (String zone : zones) {
            ArrayList<Ban> tables = banDAO.getBanTheoKhuVuc(zone);

            for (Ban table : tables) {
                TableCard card = new TableCard(table);

                if (cardListener != null) {
                    card.setTableCardListener(cardListener);
                }

                tableCards.add(card);
                pnlTableGrid.add(card);
            }
        }

        pnlTableGrid.revalidate();
        pnlTableGrid.repaint();
    }

    public void setTableCardListener(TableCard.TableCardListener listener) {
        this.cardListener = listener;

        for (TableCard card : tableCards) {
            card.setTableCardListener(listener);
        }
    }

    public void refreshTables() {
        loadAllTables();
    }

    public void deselectAllCards() {
        for (TableCard card : tableCards) {
            card.setSelected(false);
        }
    }
}
