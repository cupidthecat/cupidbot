package net.runelite.client.plugins.cupidbot.ui;

import java.awt.Dimension;
import javax.swing.JLabel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CupidBotPluginListItemTest
{
	@Test
	public void wrappedPluginNamesReserveEnoughRowHeight()
	{
		JLabel label = CupidBotPluginListItem.createNameLabel(
			"Extremely Long CupidBot Plugin Name That Clearly Wraps Across Lines",
			"Used only to exercise wrapped title sizing"
		);
		Dimension labelSize = label.getPreferredSize();

		assertTrue("test fixture should produce a wrapped label",
			labelSize.height > CupidBotPluginListItem.MIN_LIST_ITEM_HEIGHT);

		int rowHeight = CupidBotPluginListItem.preferredItemHeight(label);
		assertTrue("row height must fit the wrapped title plus padding",
			rowHeight >= labelSize.height + CupidBotPluginListItem.LIST_ITEM_VERTICAL_PADDING);
	}

	@Test
	public void shortPluginNamesKeepCompactRowHeight()
	{
		JLabel label = CupidBotPluginListItem.createNameLabel("Woodcutting", "");

		assertEquals(CupidBotPluginListItem.MIN_LIST_ITEM_HEIGHT,
			CupidBotPluginListItem.preferredItemHeight(label));
	}

	@Test
	public void htmlPluginNamePrefixesRenderAsMarkup()
	{
		JLabel label = CupidBotPluginListItem.createNameLabel(
			"<html>[<font color=#000000>00</font>] Bird Hunter",
			"Automates bird hunting"
		);

		assertTrue(label.getText().contains("<font color=#000000>00</font>"));
		assertFalse(label.getText().contains("&lt;html&gt;"));
		assertFalse(label.getText().contains("&lt;font"));
		assertFalse(label.getToolTipText().contains("&lt;font"));
	}
}
