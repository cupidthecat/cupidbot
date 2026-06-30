package net.runelite.client.plugins.cupidbot.util.antiban.ui;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSmoothness;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.PlayStyle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.Dimension;
import java.lang.reflect.Field;

import javax.swing.JLabel;
import javax.swing.JSlider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class MousePanelTest
{
	private static final int COMMON_ANTIBAN_CARD_HEIGHT = 410;

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
	public void updateValuesDoesNotDisableDynamicIntensity() throws Exception
	{
		Rs2AntibanSettings.dynamicIntensity = true;
		Rs2AntibanSettings.mouseSpeed = MouseSpeed.VERY_SLOW;
		setActivityIntensity(ActivityIntensity.EXTREME);

		MousePanel panel = new MousePanel();
		panel.updateValues();

		assertTrue(Rs2AntibanSettings.dynamicIntensity);
		assertSame(MouseSpeed.VERY_SLOW, Rs2AntibanSettings.mouseSpeed);
	}

	@Test
	public void automaticIntensityChangesRefreshDisplayedMouseSpeed() throws Exception
	{
		Rs2AntibanSettings.dynamicIntensity = true;
		Rs2AntibanSettings.mouseSpeed = MouseSpeed.EXTREME;
		setActivityIntensity(ActivityIntensity.EXTREME);

		MousePanel panel = new MousePanel();
		Rs2Antiban.setActivityIntensity(ActivityIntensity.LOW);
		panel.updateValues();

		assertTrue(Rs2AntibanSettings.dynamicIntensity);
		assertEquals(MouseSpeed.SLOW.getSliderIndex(), getSlider(panel, "mouseSpeedSlider").getValue());
		assertEquals("Mouse Speed: Dynamic (" + MouseSpeed.SLOW.getName() + ")", getLabel(panel, "mouseSpeedLabel").getText());
		assertSame(MouseSpeed.EXTREME, Rs2AntibanSettings.mouseSpeed);
	}

	@Test
	public void manualMouseSpeedSelectionUpdatesActivityIntensityAndPlayStyle() throws Exception
	{
		Rs2Antiban.setActivity(Activity.GENERAL_COMBAT);
		Rs2AntibanSettings.dynamicIntensity = true;

		MousePanel panel = new MousePanel();
		getSlider(panel, "mouseSpeedSlider").setValue(MouseSpeed.SLOW.getSliderIndex());

		assertFalse(Rs2AntibanSettings.dynamicIntensity);
		assertSame(MouseSpeed.SLOW, Rs2AntibanSettings.mouseSpeed);
		assertSame(ActivityIntensity.LOW, Rs2Antiban.getActivityIntensity());
		assertSame(PlayStyle.CAUTIOUS, Rs2Antiban.getPlayStyle());
		assertEquals("Mouse Speed: " + MouseSpeed.SLOW.getName(), getLabel(panel, "mouseSpeedLabel").getText());
	}

	@Test
	public void updateValuesRefreshesOffscreenChanceLabelWhenValueDidNotChange() throws Exception
	{
		Rs2AntibanSettings.moveMouseOffScreenChance = 0.1;
		MousePanel panel = new MousePanel();
		JLabel label = getLabel(panel, "moveMouseOffScreenChanceLabel");

		label.setText("stale");
		panel.updateValues();

		assertEquals("Move Mouse Off Screen (%): 10", label.getText());
	}

	@Test
	public void manualMouseSmoothnessSelectionSavesProfile() throws Exception
	{
		ConfigManager configManager = mock(ConfigManager.class);
		setProfileConfigManager(configManager);
		MousePanel panel = new MousePanel();
		clearInvocations(configManager);

		getSlider(panel, "mouseSmoothnessSlider").setValue(MouseSmoothness.MAX.getSliderIndex());

		assertSame(MouseSmoothness.MAX, Rs2AntibanSettings.mouseSmoothness);
		assertEquals("Mouse Smoothness: " + MouseSmoothness.MAX.getName(),
			getLabel(panel, "mouseSmoothnessLabel").getText());
		verify(configManager, atLeastOnce()).setConfiguration(anyString(), anyString(), anyString());
	}

	@Test
	public void updateValuesRefreshesMouseSmoothnessWithoutSavingProfile() throws Exception
	{
		ConfigManager configManager = mock(ConfigManager.class);
		setProfileConfigManager(configManager);
		MousePanel panel = new MousePanel();
		clearInvocations(configManager);

		Rs2AntibanSettings.mouseSmoothness = MouseSmoothness.HIGH;
		panel.updateValues();

		assertEquals(MouseSmoothness.HIGH.getSliderIndex(), getSlider(panel, "mouseSmoothnessSlider").getValue());
		assertEquals("Mouse Smoothness: " + MouseSmoothness.HIGH.getName(),
			getLabel(panel, "mouseSmoothnessLabel").getText());
		verify(configManager, never()).setConfiguration(anyString(), anyString(), anyString());
	}

	@Test
	public void mousePanelKeepsSmoothnessControlsInsideCommonCardHeight()
	{
		MousePanel panel = new MousePanel();

		Dimension preferredSize = panel.getPreferredSize();

		assertTrue("Mouse panel preferred height must fit the visible Antiban mouse card. Preferred height was "
				+ preferredSize.height,
			preferredSize.height <= COMMON_ANTIBAN_CARD_HEIGHT);
	}

	@Test
	public void updateValuesDoesNotSaveProfileDuringProgrammaticRefresh() throws Exception
	{
		ConfigManager configManager = mock(ConfigManager.class);
		setProfileConfigManager(configManager);
		MousePanel panel = new MousePanel();
		clearInvocations(configManager);

		Rs2AntibanSettings.moveMouseOffScreenChance = 0.25;
		Rs2AntibanSettings.moveMouseRandomlyChance = 0.35;

		panel.updateValues();

		verify(configManager, never()).setConfiguration(anyString(), anyString(), anyString());
	}

	private static void setActivityIntensity(ActivityIntensity activityIntensity) throws Exception
	{
		Field field = Rs2Antiban.class.getDeclaredField("activityIntensity");
		field.setAccessible(true);
		field.set(null, activityIntensity);
	}

	private static void setProfileConfigManager(ConfigManager configManager) throws Exception
	{
		Field field = Rs2AntibanSettings.class.getDeclaredField("profileConfigManager");
		field.setAccessible(true);
		field.set(null, configManager);
	}

	private static JLabel getLabel(MousePanel panel, String name) throws Exception
	{
		Field field = MousePanel.class.getDeclaredField(name);
		field.setAccessible(true);
		return (JLabel) field.get(panel);
	}

	private static JSlider getSlider(MousePanel panel, String name) throws Exception
	{
		Field field = MousePanel.class.getDeclaredField(name);
		field.setAccessible(true);
		return (JSlider) field.get(panel);
	}
}
