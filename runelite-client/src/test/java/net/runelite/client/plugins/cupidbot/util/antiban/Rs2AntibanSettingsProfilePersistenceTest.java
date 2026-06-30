package net.runelite.client.plugins.cupidbot.util.antiban;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseEngineMode;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSmoothness;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class Rs2AntibanSettingsProfilePersistenceTest
{
	private static final String CONFIG_GROUP = "CupidBotAntiban";
	private static final String CONFIG_KEY = "settings";

	@Mock
	private ConfigManager configManager;

	@Captor
	private ArgumentCaptor<String> savedSettings;

	@Before
	public void setUp()
	{
		Rs2AntibanSettings.reset();
		Rs2AntibanSettings.setProfileConfigManager(configManager);
	}

	@After
	public void tearDown()
	{
		Rs2AntibanSettings.setProfileConfigManager(null);
		Rs2AntibanSettings.reset();
	}

	@Test
	public void saveAndLoadRoundTripsThroughProfileConfigManager()
	{
		Rs2AntibanSettings.antibanEnabled = false;
		Rs2AntibanSettings.usePlayStyle = true;
		Rs2AntibanSettings.naturalMouse = false;
		Rs2AntibanSettings.overwriteScriptSettings = true;
		Rs2AntibanSettings.takeMicroBreaks = true;
		Rs2AntibanSettings.microBreakDurationLow = 7;
		Rs2AntibanSettings.microBreakDurationHigh = 21;
		Rs2AntibanSettings.actionCooldownChance = 0.35;
		Rs2AntibanSettings.mouseSpeed = MouseSpeed.VERY_SLOW;
		Rs2AntibanSettings.mouseSmoothness = MouseSmoothness.MAX;
		Rs2AntibanSettings.mouseEngineMode = MouseEngineMode.PRECISE;

		Rs2AntibanSettings.saveToProfile();

		verify(configManager).setConfiguration(eq(CONFIG_GROUP), eq(CONFIG_KEY), savedSettings.capture());

		Rs2AntibanSettings.reset();
		when(configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY)).thenReturn(savedSettings.getValue());

		Rs2AntibanSettings.loadFromProfile();

		assertFalse(Rs2AntibanSettings.antibanEnabled);
		assertTrue(Rs2AntibanSettings.usePlayStyle);
		assertFalse(Rs2AntibanSettings.naturalMouse);
		assertTrue(Rs2AntibanSettings.overwriteScriptSettings);
		assertTrue(Rs2AntibanSettings.takeMicroBreaks);
		assertEquals(7, Rs2AntibanSettings.microBreakDurationLow);
		assertEquals(21, Rs2AntibanSettings.microBreakDurationHigh);
		assertEquals(0.35, Rs2AntibanSettings.actionCooldownChance, 1e-9);
		assertSame(MouseSpeed.VERY_SLOW, Rs2AntibanSettings.mouseSpeed);
		assertSame(MouseSmoothness.MAX, Rs2AntibanSettings.mouseSmoothness);
		assertSame(MouseEngineMode.PRECISE, Rs2AntibanSettings.mouseEngineMode);
	}

	@Test
	public void missingProfileSettingsResetPersistedValuesToDefaults()
	{
		Rs2AntibanSettings.antibanEnabled = false;
		Rs2AntibanSettings.usePlayStyle = true;
		Rs2AntibanSettings.overwriteScriptSettings = true;
		Rs2AntibanSettings.takeMicroBreaks = true;
		Rs2AntibanSettings.microBreakDurationLow = 9;
		Rs2AntibanSettings.microBreakDurationHigh = 29;
		Rs2AntibanSettings.mouseSpeed = MouseSpeed.VERY_SLOW;
		Rs2AntibanSettings.mouseSmoothness = MouseSmoothness.MAX;
		Rs2AntibanSettings.mouseEngineMode = MouseEngineMode.QA_REPLAY;
		when(configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY)).thenReturn(null);

		Rs2AntibanSettings.loadFromProfile();

		assertTrue(Rs2AntibanSettings.antibanEnabled);
		assertFalse(Rs2AntibanSettings.usePlayStyle);
		assertFalse(Rs2AntibanSettings.overwriteScriptSettings);
		assertFalse(Rs2AntibanSettings.takeMicroBreaks);
		assertEquals(AntibanPlugin.MICRO_BREAK_DURATION_LOW_DEFAULT, Rs2AntibanSettings.microBreakDurationLow);
		assertEquals(AntibanPlugin.MICRO_BREAK_DURATION_HIGH_DEFAULT, Rs2AntibanSettings.microBreakDurationHigh);
		assertSame(MouseSpeed.DEFAULT, Rs2AntibanSettings.mouseSpeed);
		assertSame(MouseSmoothness.DEFAULT, Rs2AntibanSettings.mouseSmoothness);
		assertSame(MouseEngineMode.DEFAULT, Rs2AntibanSettings.mouseEngineMode);
	}

	@Test
	public void partialProfileSettingsResetBeforeApplyingSavedFields()
	{
		Rs2AntibanSettings.takeMicroBreaks = true;
		Rs2AntibanSettings.overwriteScriptSettings = true;
		Rs2AntibanSettings.mouseSpeed = MouseSpeed.VERY_SLOW;
		Rs2AntibanSettings.mouseSmoothness = MouseSmoothness.MAX;
		Rs2AntibanSettings.mouseEngineMode = MouseEngineMode.QA_REPLAY;
		when(configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY)).thenReturn("{\"naturalMouse\":false}");

		Rs2AntibanSettings.loadFromProfile();

		assertFalse(Rs2AntibanSettings.naturalMouse);
		assertFalse(Rs2AntibanSettings.takeMicroBreaks);
		assertFalse(Rs2AntibanSettings.overwriteScriptSettings);
		assertSame(MouseSpeed.DEFAULT, Rs2AntibanSettings.mouseSpeed);
		assertSame(MouseSmoothness.DEFAULT, Rs2AntibanSettings.mouseSmoothness);
		assertSame(MouseEngineMode.DEFAULT, Rs2AntibanSettings.mouseEngineMode);
	}

	@Test
	public void loadProfileSettingsUsesDefaultForInvalidMouseSmoothness()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY)).thenReturn(
			"{\"mouseSmoothness\":\"NOT_REAL\",\"mouseEngineMode\":\"NOT_REAL\"}");

		Rs2AntibanSettings.loadFromProfile();

		assertSame(MouseSmoothness.DEFAULT, Rs2AntibanSettings.mouseSmoothness);
		assertSame(MouseEngineMode.DEFAULT, Rs2AntibanSettings.mouseEngineMode);
	}

	@Test
	public void loadProfileSettingsClampsChanceValues()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY)).thenReturn(
			"{\"actionCooldownChance\":1.5,"
				+ "\"microBreakChance\":-0.25,"
				+ "\"moveMouseRandomlyChance\":2.0,"
				+ "\"moveMouseOffScreenChance\":0.35}");

		Rs2AntibanSettings.loadFromProfile();

		assertEquals(1.0, Rs2AntibanSettings.actionCooldownChance, 1e-9);
		assertEquals(0.0, Rs2AntibanSettings.microBreakChance, 1e-9);
		assertEquals(1.0, Rs2AntibanSettings.moveMouseRandomlyChance, 1e-9);
		assertEquals(0.35, Rs2AntibanSettings.moveMouseOffScreenChance, 1e-9);
	}

	@Test
	public void normalizeProbabilityHandlesInvalidValues()
	{
		assertEquals(0.0, Rs2AntibanSettings.normalizeProbability(Double.NaN), 1e-9);
		assertEquals(0.0, Rs2AntibanSettings.normalizeProbability(Double.NEGATIVE_INFINITY), 1e-9);
		assertEquals(1.0, Rs2AntibanSettings.normalizeProbability(Double.POSITIVE_INFINITY), 1e-9);
		assertEquals(0.0, Rs2AntibanSettings.normalizeProbability(-0.01), 1e-9);
		assertEquals(0.42, Rs2AntibanSettings.normalizeProbability(0.42), 1e-9);
		assertEquals(1.0, Rs2AntibanSettings.normalizeProbability(1.01), 1e-9);
	}
}
