package net.runelite.client.plugins.cupidbot;

import javax.swing.SwingUtilities;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Local splash tip provider. CupidBot does not fetch splash text from a service.
 */
public class RandomFactClient
{
	private static final String LOCAL_TIP = "CupidBot loads local plugins from ~/.runelite/cupidbot-plugins.";

	public static String getRandomFact()
	{
		return LOCAL_TIP;
	}

	public static void getRandomFactAsync(Consumer<String> callback)
	{
		CompletableFuture
			.supplyAsync(RandomFactClient::getRandomFact)
			.thenAccept(fact -> SwingUtilities.invokeLater(() -> callback.accept(fact)));
	}
}
