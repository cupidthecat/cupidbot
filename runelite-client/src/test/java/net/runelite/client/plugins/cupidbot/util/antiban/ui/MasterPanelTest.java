package net.runelite.client.plugins.cupidbot.util.antiban.ui;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MasterPanelTest
{
	@Test
	public void loadSettingsRefreshesDynamicAntibanValuesWithoutLoginGate() throws IOException
	{
		String source = readSource("src/main/java/net/runelite/client/plugins/cupidbot/util/antiban/ui/MasterPanel.java");

		assertTrue(source.contains("mousePanel.updateValues();"));
		assertTrue(source.contains("playStyleLabel.setText("));
		assertFalse("Play-style and mouse-speed UI refresh must not be skipped by login state",
			source.contains("if (!CupidBot.isLoggedIn())"));
	}

	private static String readSource(String path) throws IOException
	{
		return Files.readString(Path.of(path), StandardCharsets.UTF_8);
	}
}
