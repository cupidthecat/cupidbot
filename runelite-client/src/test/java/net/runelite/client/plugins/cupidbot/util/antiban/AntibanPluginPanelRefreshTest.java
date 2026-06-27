package net.runelite.client.plugins.cupidbot.util.antiban;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class AntibanPluginPanelRefreshTest
{
	@Test
	public void playStyleSwitchRequestsImmediatePanelRefresh() throws IOException
	{
		String source = readSource("src/main/java/net/runelite/client/plugins/cupidbot/util/antiban/AntibanPlugin.java")
			.replace("\r\n", "\n");

		assertTrue(source.contains("private MasterPanel masterPanel;"));
		assertTrue(source.contains("private void refreshPanel()"));
		assertTrue(source.contains("if (Rs2Antiban.switchPlayStyleIfAttentionExpired()) {\n                refreshPanel();\n            }"));
	}

	private static String readSource(String path) throws IOException
	{
		return Files.readString(Path.of(path), StandardCharsets.UTF_8);
	}
}
