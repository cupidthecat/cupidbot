package net.runelite.client.plugins.cupidbot.util.antiban;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AntibanPluginProfilePersistenceTest
{
	private static final String CONFIG_GROUP = "CupidBotAntiban";
	private static final String CONFIG_KEY = "settings";

	@Mock
	private ConfigManager configManager;

	@After
	public void tearDown()
	{
		Rs2AntibanSettings.setProfileConfigManager(null);
		Rs2Antiban.resetAntibanSettings(true);
	}

	@Test
	public void loadProfileSettingsUsesInjectedConfigManager() throws Exception
	{
		when(configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY))
			.thenReturn("{\"naturalMouse\":false,\"mouseSpeed\":\"VERY_SLOW\"}");
		AntibanPlugin plugin = newPluginWithConfigManager();

		Rs2AntibanSettings.naturalMouse = true;
		Rs2AntibanSettings.mouseSpeed = MouseSpeed.DEFAULT;

		plugin.loadProfileSettings();

		verify(configManager).getConfiguration(eq(CONFIG_GROUP), eq(CONFIG_KEY));
		assertFalse(Rs2AntibanSettings.naturalMouse);
		assertSame(MouseSpeed.VERY_SLOW, Rs2AntibanSettings.mouseSpeed);
	}

	@Test
	public void loadProfileSettingsForcesResetAcrossProfiles() throws Exception
	{
		when(configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY)).thenReturn(null);
		AntibanPlugin plugin = newPluginWithConfigManager();
		Rs2AntibanSettings.overwriteScriptSettings = true;
		Rs2AntibanSettings.usePlayStyle = true;

		plugin.loadProfileSettings();

		assertFalse(Rs2AntibanSettings.overwriteScriptSettings);
		assertFalse(Rs2AntibanSettings.usePlayStyle);
		assertTrue(Rs2Antiban.getActivityIntensity() != null);
	}

	private AntibanPlugin newPluginWithConfigManager() throws Exception
	{
		AntibanPlugin plugin = new AntibanPlugin();
		Field field = AntibanPlugin.class.getDeclaredField("configManager");
		field.setAccessible(true);
		field.set(plugin, configManager);
		return plugin;
	}
}
