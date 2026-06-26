package net.runelite.client.plugins.cupidbot.util.reflection;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;

public class Rs2ReflectionLoggingTest
{
	@Test
	public void invokeMenuDoesNotWriteInvokeDiagnosticsToStdout() throws Exception
	{
		String source = Files.readString(Path.of(
			"src",
			"main",
			"java",
			"net",
			"runelite",
			"client",
			"plugins",
			"cupidbot",
			"util",
			"reflection",
			"Rs2Reflection.java"
		));

		assertFalse(source.contains("[INVOKE] =>"));
	}
}
