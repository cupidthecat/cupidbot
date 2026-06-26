package net.runelite.client.plugins.cupidbot;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProfileSwitchThreadingRegressionTest
{
	@Test
	public void inventorySetupsFastRemoveAllDoesNotPumpNestedEdtEvents() throws IOException
	{
		String source = readSource("src/main/java/net/runelite/client/plugins/cupidbot/inventorysetups/InventorySetupUtilities.java");

		assertFalse(
			"Inventory setups redraw must not spin a nested event loop during recursive component removal",
			source.contains("SwingUtil.pumpPendingEvents()"));
	}

	@Test
	public void worldHopperSidebarHighlightIsUpdatedOnEdt() throws IOException
	{
		String source = readSource("src/main/java/net/runelite/client/plugins/worldhopper/WorldHopperPlugin.java");

		assertTrue(
			"World hopper must defer sidebar row recolouring to the Swing EDT",
			source.contains("SwingUtilities.invokeLater(() -> panel.switchCurrentHighlight(newWorld, previousWorld));"));
	}

	private static String readSource(String path) throws IOException
	{
		return Files.readString(Path.of(path), StandardCharsets.UTF_8);
	}
}
