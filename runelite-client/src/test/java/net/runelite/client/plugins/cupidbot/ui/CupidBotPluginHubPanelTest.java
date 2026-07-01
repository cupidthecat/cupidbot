package net.runelite.client.plugins.cupidbot.ui;

import javax.swing.JLabel;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CupidBotPluginHubPanelTest
{
	@Test
	public void hubPluginNamesWrapHtmlPrefixesLikeInstalledTab()
	{
		JLabel label = CupidBotPluginHubPanel.createPluginNameLabel(
			"<html>[<font color=#000000>00</font>] Bird Hunter"
		);

		assertTrue(label.getText().startsWith("<html><div style='width:"));
		assertTrue(label.getText().contains("<font color=#000000>00</font>"));
		assertFalse(label.getText().contains("&lt;html&gt;"));
		assertFalse(label.getText().contains("&lt;font"));
		assertTrue(label.getToolTipText().contains("<font color=#000000>00</font>"));
	}
}
