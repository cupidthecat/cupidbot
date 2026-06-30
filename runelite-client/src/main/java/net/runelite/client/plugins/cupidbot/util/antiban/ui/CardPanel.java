package net.runelite.client.plugins.cupidbot.util.antiban.ui;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;

public class CardPanel extends JPanel {
    public CardPanel() {
        setLayout(new CardLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 2, 2, 2, ColorScheme.DARKER_GRAY_COLOR.darker()),
                BorderFactory.createEmptyBorder(0, 0, 5, 0)));
        setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
    }

    public void addPanel(JPanel panel, String name) {
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, name);
    }

    public void showPanel(String name) {
        CardLayout cardLayout = (CardLayout) getLayout();
        cardLayout.show(this, name);
    }
}
