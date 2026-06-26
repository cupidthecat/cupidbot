package net.runelite.client.plugins.cupidbot.util.antiban.ui;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2AntibanSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class CooldownPanelTest
{
	@Before
	public void setUp()
	{
		Rs2Antiban.resetAntibanSettings(true);
	}

	@After
	public void tearDown() throws Exception
	{
		setProfileConfigManager(null);
		Rs2Antiban.resetAntibanSettings(true);
	}

	@Test
	public void updateValuesDoesNotSaveProfileDuringProgrammaticRefresh() throws Exception
	{
		ConfigManager configManager = mock(ConfigManager.class);
		setProfileConfigManager(configManager);
		CooldownPanel panel = new CooldownPanel();

		Rs2AntibanSettings.actionCooldownChance = 0.35;

		panel.updateValues();

		verify(configManager, never()).setConfiguration(anyString(), anyString(), anyString());
	}

	private static void setProfileConfigManager(ConfigManager configManager) throws Exception
	{
		Field field = Rs2AntibanSettings.class.getDeclaredField("profileConfigManager");
		field.setAccessible(true);
		field.set(null, configManager);
	}
}
