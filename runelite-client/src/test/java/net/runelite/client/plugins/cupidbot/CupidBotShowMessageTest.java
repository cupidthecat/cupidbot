package net.runelite.client.plugins.cupidbot;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;

public class CupidBotShowMessageTest
{
	@Test
	public void showMessageDoesNotBlockScriptOrEventThreads() throws IOException
	{
		Path sourcePath = Path.of("src/main/java/net/runelite/client/plugins/cupidbot/CupidBot.java");
		String source = Files.readString(sourcePath, StandardCharsets.UTF_8);

		assertFalse("showMessage must not wait for modal dialog creation", source.contains("SwingUtilities.invokeAndWait"));
		assertFalse("showMessage must not open blocking confirm dialogs", source.contains("JOptionPane.showConfirmDialog"));
		assertFalse("showMessage must keep dialogs non-modal", source.contains("dialog.setModal(true)"));
	}
}
