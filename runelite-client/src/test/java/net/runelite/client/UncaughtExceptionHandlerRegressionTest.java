package net.runelite.client;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UncaughtExceptionHandlerRegressionTest
{
	@Test
	public void runeliteUncaughtHandlerDoesNotPassThrowableToLogback() throws IOException
	{
		assertUsesSafeThrowableLogger("src/main/java/net/runelite/client/RuneLite.java");
	}

	@Test
	public void runeliteDebugUncaughtHandlerDoesNotPassThrowableToLogback() throws IOException
	{
		assertUsesSafeThrowableLogger("src/main/java/net/runelite/client/RuneLiteDebug.java");
	}

	private static void assertUsesSafeThrowableLogger(String path) throws IOException
	{
		String source = Files.readString(Path.of(path), StandardCharsets.UTF_8);

		assertFalse(
			"Uncaught handlers must not pass throwables directly to Logback",
			source.contains("log.error(\"Uncaught exception:\", throwable);"));
		assertTrue(
			"Uncaught handlers must use the safe throwable logger",
			source.contains("SafeThrowableLogger.logUncaught(log, \"Uncaught exception:\", throwable);"));
	}
}
