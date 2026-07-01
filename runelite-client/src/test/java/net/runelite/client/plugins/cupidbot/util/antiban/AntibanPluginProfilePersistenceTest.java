package net.runelite.client.plugins.cupidbot.util.antiban;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

	@Captor
	private ArgumentCaptor<String> savedSettings;

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
	public void loadProfileSettingsSeedsMissingProfileWithoutResettingCurrentSettings() throws Exception
	{
		when(configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY)).thenReturn(null);
		AntibanPlugin plugin = newPluginWithConfigManager();
		Rs2AntibanSettings.overwriteScriptSettings = true;
		Rs2AntibanSettings.usePlayStyle = true;
		Rs2AntibanSettings.simulateFatigue = true;
		Rs2AntibanSettings.profileSwitching = true;

		plugin.loadProfileSettings();

		assertTrue(Rs2AntibanSettings.overwriteScriptSettings);
		assertTrue(Rs2AntibanSettings.usePlayStyle);
		assertTrue(Rs2AntibanSettings.simulateFatigue);
		assertTrue(Rs2AntibanSettings.profileSwitching);
		assertTrue(Rs2Antiban.getActivityIntensity() != null);
		verify(configManager).setConfiguration(eq(CONFIG_GROUP), eq(CONFIG_KEY), savedSettings.capture());
		assertTrue(savedSettings.getValue().contains("\"overwriteScriptSettings\":true"));
		assertTrue(savedSettings.getValue().contains("\"usePlayStyle\":true"));
		assertTrue(savedSettings.getValue().contains("\"simulateFatigue\":true"));
		assertTrue(savedSettings.getValue().contains("\"profileSwitching\":true"));
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
