package net.runelite.client.externalplugins;

import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class PluginHubVersionConfigurationTest
{
	@Test
	public void pluginHubVersionIsDecoupledFromSnapshotClientVersion() throws IOException
	{
		Properties rootProperties = readProperties("../gradle.properties");
		String projectVersion = rootProperties.getProperty("project.build.version");
		String pluginHubVersion = rootProperties.getProperty("runelite.pluginhub.version");

		assertNotNull(pluginHubVersion);
		assertFalse(pluginHubVersion.endsWith("-SNAPSHOT"));
		assertNotEquals(projectVersion, pluginHubVersion);

		Properties resourceTemplate = readProperties("src/main/resources/net/runelite/client/runelite.properties");
		assertEquals("${runelite.pluginhub.version}", resourceTemplate.getProperty("runelite.pluginhub.version"));
	}

	private static Properties readProperties(String path) throws IOException
	{
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8))
		{
			properties.load(reader);
		}
		return properties;
	}
}
