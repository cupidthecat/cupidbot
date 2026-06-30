package net.runelite.client.plugins.cupidbot.util.antiban.ui;

import org.junit.Test;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CardPanelTest
{
	@Test
	public void addedPanelsAreScrollableWhenCardIsShorterThanContent()
	{
		CardPanel cardPanel = new CardPanel();
		JPanel mousePanel = new JPanel();

		cardPanel.addPanel(mousePanel, "Mouse");

		assertEquals(1, cardPanel.getComponentCount());
		assertTrue(cardPanel.getComponent(0) instanceof JScrollPane);

		JScrollPane scrollPane = (JScrollPane) cardPanel.getComponent(0);
		assertSame(mousePanel, scrollPane.getViewport().getView());
		assertEquals(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, scrollPane.getHorizontalScrollBarPolicy());
		assertEquals(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, scrollPane.getVerticalScrollBarPolicy());
	}
}
