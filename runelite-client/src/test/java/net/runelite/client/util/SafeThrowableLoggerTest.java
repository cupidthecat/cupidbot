package net.runelite.client.util;

import org.junit.Test;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class SafeThrowableLoggerTest
{
	@Test
	public void logUncaughtLogsOnlySummaryThroughLogger()
	{
		Logger logger = mock(Logger.class);
		RuntimeException throwable = new RuntimeException("boom");
		ByteArrayOutputStream err = new ByteArrayOutputStream();

		SafeThrowableLogger.logUncaught(logger, "Uncaught exception:", throwable, printStream(err));

		verify(logger).error("Uncaught exception: java.lang.RuntimeException: boom");
		verify(logger, never()).error(anyString(), any(Throwable.class));
		assertTrue(err.toString(StandardCharsets.UTF_8).contains("java.lang.RuntimeException: boom"));
	}

	@Test
	public void logUncaughtFallsBackToStderrWhenLoggerFails()
	{
		Logger logger = mock(Logger.class);
		NoClassDefFoundError loggerFailure = new NoClassDefFoundError("ch/qos/logback/classic/spi/ThrowableProxy");
		RuntimeException throwable = new RuntimeException("original");
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		doThrow(loggerFailure).when(logger).error(eq("Uncaught exception: java.lang.RuntimeException: original"));

		SafeThrowableLogger.logUncaught(logger, "Uncaught exception:", throwable, printStream(err));

		String output = err.toString(StandardCharsets.UTF_8);
		assertTrue(output.contains("Uncaught exception: java.lang.RuntimeException: original"));
		assertTrue(output.contains("Failed to log throwable safely: java.lang.NoClassDefFoundError: ch/qos/logback/classic/spi/ThrowableProxy"));
		assertTrue(output.contains("java.lang.RuntimeException: original"));
	}

	private static PrintStream printStream(ByteArrayOutputStream out)
	{
		return new PrintStream(out, true, StandardCharsets.UTF_8);
	}
}
