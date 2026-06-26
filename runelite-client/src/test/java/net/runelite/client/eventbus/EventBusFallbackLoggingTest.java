package net.runelite.client.eventbus;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class EventBusFallbackLoggingTest
{
	@Test
	public void lambdaConversionFallbackDoesNotWarnWithStackTrace() throws IOException
	{
		String source = Files.readString(Path.of("src/main/java/net/runelite/client/eventbus/EventBus.java"), StandardCharsets.UTF_8);

		assertTrue(
			"EventBus should handle expected LambdaMetafactory conversion failures separately",
			source.contains("catch (LambdaConversionException e)"));
		assertTrue(
			"Expected lambda fallback should log at debug level",
			source.contains("log.debug(\"Using reflection fallback for event subscriber method {}\", method, e);"));
		assertTrue(
			"Expected lambda fallback must be handled before the general warning path",
			source.indexOf("catch (LambdaConversionException e)") < source.indexOf("catch (Throwable e)"));
	}
}
