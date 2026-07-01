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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
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
		Rs2AntibanSettings.mouseReactionDelayMs = 60;
		Rs2AntibanSettings.mouseReactionDelayRandom = true;
		Rs2AntibanSettings.mouseReactionDelayMinMs = 15;
		Rs2AntibanSettings.mouseReactionDelayMaxMs = 90;
		Rs2AntibanSettings.mouseSettleDelayMs = 45;
		Rs2AntibanSettings.mouseSettleDelayRandom = true;
		Rs2AntibanSettings.mouseSettleDelayMinMs = 25;
		Rs2AntibanSettings.mouseSettleDelayMaxMs = 110;
		Rs2AntibanSettings.mouseButtonHoldMs = 75;
		Rs2AntibanSettings.mouseButtonHoldRandom = true;
		Rs2AntibanSettings.mouseButtonHoldMinMs = 30;
		Rs2AntibanSettings.mouseButtonHoldMaxMs = 140;
		Rs2AntibanSettings.mouseCurveScale = 150;
		Rs2AntibanSettings.mousePathNoiseScale = 130;
		Rs2AntibanSettings.mouseMicroJitterScale = 70;
		Rs2AntibanSettings.mouseOvershootScale = 200;
		Rs2AntibanSettings.mouseCorrectionScale = 50;
		Rs2AntibanSettings.mouseEndpointErrorScale = 80;
		Rs2AntibanSettings.mouseDragStabilityScale = 150;
		Rs2AntibanSettings.mouseScrollBurstScale = 120;

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
		assertEquals(60, Rs2AntibanSettings.mouseReactionDelayMs);
		assertTrue(Rs2AntibanSettings.mouseReactionDelayRandom);
		assertEquals(15, Rs2AntibanSettings.mouseReactionDelayMinMs);
		assertEquals(90, Rs2AntibanSettings.mouseReactionDelayMaxMs);
		assertEquals(45, Rs2AntibanSettings.mouseSettleDelayMs);
		assertTrue(Rs2AntibanSettings.mouseSettleDelayRandom);
		assertEquals(25, Rs2AntibanSettings.mouseSettleDelayMinMs);
		assertEquals(110, Rs2AntibanSettings.mouseSettleDelayMaxMs);
		assertEquals(75, Rs2AntibanSettings.mouseButtonHoldMs);
		assertTrue(Rs2AntibanSettings.mouseButtonHoldRandom);
		assertEquals(30, Rs2AntibanSettings.mouseButtonHoldMinMs);
		assertEquals(140, Rs2AntibanSettings.mouseButtonHoldMaxMs);
		assertEquals(150, Rs2AntibanSettings.mouseCurveScale);
		assertEquals(130, Rs2AntibanSettings.mousePathNoiseScale);
		assertEquals(70, Rs2AntibanSettings.mouseMicroJitterScale);
		assertEquals(200, Rs2AntibanSettings.mouseOvershootScale);
		assertEquals(50, Rs2AntibanSettings.mouseCorrectionScale);
		assertEquals(80, Rs2AntibanSettings.mouseEndpointErrorScale);
		assertEquals(150, Rs2AntibanSettings.mouseDragStabilityScale);
		assertEquals(120, Rs2AntibanSettings.mouseScrollBurstScale);
	}

	@Test
	public void saveToProfileFlushesConfigImmediately()
	{
		Rs2AntibanSettings.saveToProfile();

		InOrder orderedConfigSave = inOrder(configManager);
		orderedConfigSave.verify(configManager).setConfiguration(eq(CONFIG_GROUP), eq(CONFIG_KEY), savedSettings.capture());
		orderedConfigSave.verify(configManager).sendConfig();
	}

	@Test
	public void missingProfileSettingsPreserveCurrentValues()
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

		boolean loaded = Rs2AntibanSettings.loadFromProfile();

		assertFalse(loaded);
		assertFalse(Rs2AntibanSettings.antibanEnabled);
		assertTrue(Rs2AntibanSettings.usePlayStyle);
		assertTrue(Rs2AntibanSettings.overwriteScriptSettings);
		assertTrue(Rs2AntibanSettings.takeMicroBreaks);
		assertEquals(9, Rs2AntibanSettings.microBreakDurationLow);
		assertEquals(29, Rs2AntibanSettings.microBreakDurationHigh);
		assertSame(MouseSpeed.VERY_SLOW, Rs2AntibanSettings.mouseSpeed);
		assertSame(MouseSmoothness.MAX, Rs2AntibanSettings.mouseSmoothness);
		assertSame(MouseEngineMode.QA_REPLAY, Rs2AntibanSettings.mouseEngineMode);
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
	public void loadProfileSettingsClampsAdvancedMouseValues()
	{
		when(configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY)).thenReturn(
			"{\"mouseReactionDelayMs\":-1,"
				+ "\"mouseReactionDelayRandom\":true,"
				+ "\"mouseReactionDelayMinMs\":120,"
				+ "\"mouseReactionDelayMaxMs\":40,"
				+ "\"mouseSettleDelayMs\":900,"
				+ "\"mouseSettleDelayRandom\":true,"
				+ "\"mouseSettleDelayMinMs\":-20,"
				+ "\"mouseSettleDelayMaxMs\":900,"
				+ "\"mouseButtonHoldMs\":501,"
				+ "\"mouseButtonHoldRandom\":true,"
				+ "\"mouseButtonHoldMinMs\":200,"
				+ "\"mouseButtonHoldMaxMs\":150,"
				+ "\"mouseCurveScale\":-20,"
				+ "\"mousePathNoiseScale\":220,"
				+ "\"mouseMicroJitterScale\":90,"
				+ "\"mouseOvershootScale\":250,"
				+ "\"mouseCorrectionScale\":-10,"
				+ "\"mouseEndpointErrorScale\":250,"
				+ "\"mouseDragStabilityScale\":-10,"
				+ "\"mouseScrollBurstScale\":300}");

		Rs2AntibanSettings.loadFromProfile();

		assertEquals(0, Rs2AntibanSettings.mouseReactionDelayMs);
		assertTrue(Rs2AntibanSettings.mouseReactionDelayRandom);
		assertEquals(40, Rs2AntibanSettings.mouseReactionDelayMinMs);
		assertEquals(120, Rs2AntibanSettings.mouseReactionDelayMaxMs);
		assertEquals(500, Rs2AntibanSettings.mouseSettleDelayMs);
		assertTrue(Rs2AntibanSettings.mouseSettleDelayRandom);
		assertEquals(0, Rs2AntibanSettings.mouseSettleDelayMinMs);
		assertEquals(500, Rs2AntibanSettings.mouseSettleDelayMaxMs);
		assertEquals(500, Rs2AntibanSettings.mouseButtonHoldMs);
		assertTrue(Rs2AntibanSettings.mouseButtonHoldRandom);
		assertEquals(150, Rs2AntibanSettings.mouseButtonHoldMinMs);
		assertEquals(200, Rs2AntibanSettings.mouseButtonHoldMaxMs);
		assertEquals(0, Rs2AntibanSettings.mouseCurveScale);
		assertEquals(200, Rs2AntibanSettings.mousePathNoiseScale);
		assertEquals(90, Rs2AntibanSettings.mouseMicroJitterScale);
		assertEquals(200, Rs2AntibanSettings.mouseOvershootScale);
		assertEquals(0, Rs2AntibanSettings.mouseCorrectionScale);
		assertEquals(200, Rs2AntibanSettings.mouseEndpointErrorScale);
		assertEquals(0, Rs2AntibanSettings.mouseDragStabilityScale);
		assertEquals(200, Rs2AntibanSettings.mouseScrollBurstScale);
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

	@Test
	public void capturedSettingsRestoreAfterScriptTemplateMutatesRuntimeSettings()
	{
		Rs2AntibanSettings.usePlayStyle = true;
		Rs2AntibanSettings.randomIntervals = true;
		Rs2AntibanSettings.simulateFatigue = true;
		Rs2AntibanSettings.profileSwitching = true;
		Rs2AntibanSettings.dynamicIntensity = true;
		Rs2AntibanSettings.dynamicActivity = true;
		Rs2AntibanSettings.moveMouseOffScreen = true;
		Rs2AntibanSettings.moveMouseRandomly = true;
		Rs2AntibanSettings.mouseSmoothness = MouseSmoothness.MAX;
		Rs2AntibanSettings.mouseReactionDelayRandom = true;
		Rs2AntibanSettings.mouseReactionDelayMinMs = 90;
		Rs2AntibanSettings.mouseReactionDelayMaxMs = 330;
		Rs2AntibanSettings.mouseSettleDelayRandom = true;
		Rs2AntibanSettings.mouseSettleDelayMinMs = 90;
		Rs2AntibanSettings.mouseSettleDelayMaxMs = 330;
		Rs2AntibanSettings.actionCooldownActive = true;
		Rs2AntibanSettings.microBreakActive = true;

		Rs2AntibanSettings.SettingsSnapshot snapshot = Rs2AntibanSettings.captureSettings();

		Rs2Antiban.resetAntibanSettings(true);
		Rs2AntibanSettings.mouseSmoothness = MouseSmoothness.DEFAULT;
		Rs2AntibanSettings.mouseReactionDelayMinMs = 30;
		Rs2AntibanSettings.mouseReactionDelayMaxMs = 40;

		Rs2AntibanSettings.restoreSettings(snapshot);

		assertTrue(Rs2AntibanSettings.usePlayStyle);
		assertTrue(Rs2AntibanSettings.randomIntervals);
		assertTrue(Rs2AntibanSettings.simulateFatigue);
		assertTrue(Rs2AntibanSettings.profileSwitching);
		assertTrue(Rs2AntibanSettings.dynamicIntensity);
		assertTrue(Rs2AntibanSettings.dynamicActivity);
		assertTrue(Rs2AntibanSettings.moveMouseOffScreen);
		assertTrue(Rs2AntibanSettings.moveMouseRandomly);
		assertSame(MouseSmoothness.MAX, Rs2AntibanSettings.mouseSmoothness);
		assertTrue(Rs2AntibanSettings.mouseReactionDelayRandom);
		assertEquals(90, Rs2AntibanSettings.mouseReactionDelayMinMs);
		assertEquals(330, Rs2AntibanSettings.mouseReactionDelayMaxMs);
		assertTrue(Rs2AntibanSettings.mouseSettleDelayRandom);
		assertEquals(90, Rs2AntibanSettings.mouseSettleDelayMinMs);
		assertEquals(330, Rs2AntibanSettings.mouseSettleDelayMaxMs);
		assertFalse(Rs2AntibanSettings.actionCooldownActive);
		assertFalse(Rs2AntibanSettings.microBreakActive);
	}

	@Test
	public void nonForcedAntibanResetPreservesUserMouseTuning()
	{
		Rs2AntibanSettings.mouseSpeed = MouseSpeed.VERY_SLOW;
		Rs2AntibanSettings.mouseSmoothness = MouseSmoothness.MAX;
		Rs2AntibanSettings.mouseEngineMode = MouseEngineMode.PRECISE;
		Rs2AntibanSettings.mouseReactionDelayMs = 65;
		Rs2AntibanSettings.mouseReactionDelayRandom = true;
		Rs2AntibanSettings.mouseReactionDelayMinMs = 90;
		Rs2AntibanSettings.mouseReactionDelayMaxMs = 330;
		Rs2AntibanSettings.mouseSettleDelayMs = 45;
		Rs2AntibanSettings.mouseSettleDelayRandom = true;
		Rs2AntibanSettings.mouseSettleDelayMinMs = 95;
		Rs2AntibanSettings.mouseSettleDelayMaxMs = 335;
		Rs2AntibanSettings.mouseButtonHoldMs = 80;
		Rs2AntibanSettings.mouseButtonHoldRandom = true;
		Rs2AntibanSettings.mouseButtonHoldMinMs = 40;
		Rs2AntibanSettings.mouseButtonHoldMaxMs = 120;
		Rs2AntibanSettings.mouseCurveScale = 150;
		Rs2AntibanSettings.mousePathNoiseScale = 130;
		Rs2AntibanSettings.mouseMicroJitterScale = 70;
		Rs2AntibanSettings.mouseOvershootScale = 160;
		Rs2AntibanSettings.mouseCorrectionScale = 60;
		Rs2AntibanSettings.mouseEndpointErrorScale = 80;
		Rs2AntibanSettings.mouseDragStabilityScale = 150;
		Rs2AntibanSettings.mouseScrollBurstScale = 120;

		Rs2Antiban.resetAntibanSettings();

		assertSame(MouseSpeed.VERY_SLOW, Rs2AntibanSettings.mouseSpeed);
		assertSame(MouseSmoothness.MAX, Rs2AntibanSettings.mouseSmoothness);
		assertSame(MouseEngineMode.PRECISE, Rs2AntibanSettings.mouseEngineMode);
		assertEquals(65, Rs2AntibanSettings.mouseReactionDelayMs);
		assertTrue(Rs2AntibanSettings.mouseReactionDelayRandom);
		assertEquals(90, Rs2AntibanSettings.mouseReactionDelayMinMs);
		assertEquals(330, Rs2AntibanSettings.mouseReactionDelayMaxMs);
		assertEquals(45, Rs2AntibanSettings.mouseSettleDelayMs);
		assertTrue(Rs2AntibanSettings.mouseSettleDelayRandom);
		assertEquals(95, Rs2AntibanSettings.mouseSettleDelayMinMs);
		assertEquals(335, Rs2AntibanSettings.mouseSettleDelayMaxMs);
		assertEquals(80, Rs2AntibanSettings.mouseButtonHoldMs);
		assertTrue(Rs2AntibanSettings.mouseButtonHoldRandom);
		assertEquals(40, Rs2AntibanSettings.mouseButtonHoldMinMs);
		assertEquals(120, Rs2AntibanSettings.mouseButtonHoldMaxMs);
		assertEquals(150, Rs2AntibanSettings.mouseCurveScale);
		assertEquals(130, Rs2AntibanSettings.mousePathNoiseScale);
		assertEquals(70, Rs2AntibanSettings.mouseMicroJitterScale);
		assertEquals(160, Rs2AntibanSettings.mouseOvershootScale);
		assertEquals(60, Rs2AntibanSettings.mouseCorrectionScale);
		assertEquals(80, Rs2AntibanSettings.mouseEndpointErrorScale);
		assertEquals(150, Rs2AntibanSettings.mouseDragStabilityScale);
		assertEquals(120, Rs2AntibanSettings.mouseScrollBurstScale);
		assertFalse(Rs2AntibanSettings.actionCooldownActive);
		assertFalse(Rs2AntibanSettings.microBreakActive);
	}
}
