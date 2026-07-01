package net.runelite.client.plugins.cupidbot.util.antiban.ui;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseEngineMode;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSmoothness;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.PlayStyle;
import net.runelite.client.ui.PluginPanel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.Dimension;
import java.lang.reflect.Field;

import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
	private static final int COMMON_ANTIBAN_CARD_WIDTH = PluginPanel.PANEL_WIDTH - (PluginPanel.BORDER_OFFSET * 2);

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
	public void updateValuesUsesPlayStyleSpeedWhenDynamicIntensityIsOn() throws Exception
	{
		Rs2AntibanSettings.dynamicIntensity = true;
		Rs2AntibanSettings.mouseSpeed = MouseSpeed.NORMAL;
		Rs2Antiban.setActivity(Activity.GENERAL_COMBAT);
		Rs2Antiban.setPlayStyle(PlayStyle.CAREFUL);

		MousePanel panel = new MousePanel();
		panel.updateValues();

		assertEquals(MouseSpeed.RELAXED.getSliderIndex(), getSlider(panel, "mouseSpeedSlider").getValue());
		assertEquals("Mouse Speed: Dynamic (" + MouseSpeed.RELAXED.getName() + ")", getLabel(panel, "mouseSpeedLabel").getText());
		assertSame(MouseSpeed.NORMAL, Rs2AntibanSettings.mouseSpeed);
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
	public void manualMouseEngineModeSelectionSavesProfile() throws Exception
	{
		ConfigManager configManager = mock(ConfigManager.class);
		setProfileConfigManager(configManager);
		MousePanel panel = new MousePanel();
		clearInvocations(configManager);

		getComboBox(panel, "mouseEngineModeComboBox").setSelectedItem(MouseEngineMode.PRECISE);

		assertSame(MouseEngineMode.PRECISE, Rs2AntibanSettings.mouseEngineMode);
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
	public void updateValuesRefreshesMouseEngineModeWithoutSavingProfile() throws Exception
	{
		ConfigManager configManager = mock(ConfigManager.class);
		setProfileConfigManager(configManager);
		MousePanel panel = new MousePanel();
		clearInvocations(configManager);

		Rs2AntibanSettings.mouseEngineMode = MouseEngineMode.QA_REPLAY;
		panel.updateValues();

		assertSame(MouseEngineMode.QA_REPLAY,
			getComboBox(panel, "mouseEngineModeComboBox").getSelectedItem());
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
	public void mousePanelKeepsSliderControlsInsideCommonCardWidth() throws Exception
	{
		MousePanel panel = new MousePanel();
		Dimension preferredSize = panel.getPreferredSize();

		assertTrue("Mouse panel preferred width must fit the visible Antiban mouse card. Preferred width was "
				+ preferredSize.width,
			preferredSize.width <= COMMON_ANTIBAN_CARD_WIDTH);
		assertSliderFitsCommonCard(panel, "moveMouseOffScreenChance");
		assertSliderFitsCommonCard(panel, "moveMouseRandomlyChance");
		assertSliderFitsCommonCard(panel, "mouseSpeedSlider");
		assertSliderFitsCommonCard(panel, "mouseSmoothnessSlider");
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

	@Test
	public void advancedMouseControlsAreCollapsedByDefault() throws Exception
	{
		MousePanel panel = new MousePanel();

		assertFalse(getCheckBox(panel, "advancedMouseControls").isSelected());
		assertFalse(getSlider(panel, "mouseReactionDelaySlider").isVisible());
		assertFalse(getSlider(panel, "mouseCorrectionScaleSlider").isVisible());
		assertTrue("Collapsed mouse panel should still fit the common card height",
			panel.getPreferredSize().height <= COMMON_ANTIBAN_CARD_HEIGHT);
	}

	@Test
	public void expandingAdvancedMouseControlsDoesNotResizeAntibanCardPanel() throws Exception
	{
		MousePanel panel = new MousePanel();
		JPanel peerPanel = new JPanel();
		peerPanel.setPreferredSize(new Dimension(COMMON_ANTIBAN_CARD_WIDTH, COMMON_ANTIBAN_CARD_HEIGHT));
		CardPanel cardPanel = new CardPanel();
		cardPanel.addPanel(panel, "Mouse");
		cardPanel.addPanel(peerPanel, "Peer");
		Dimension collapsedSize = cardPanel.getPreferredSize();

		getCheckBox(panel, "advancedMouseControls").doClick();

		assertEquals("Advanced mouse controls must not resize the shared antiban card container",
			collapsedSize, cardPanel.getPreferredSize());
	}

	@Test
	public void manualAdvancedMouseSelectionSavesProfile() throws Exception
	{
		ConfigManager configManager = mock(ConfigManager.class);
		setProfileConfigManager(configManager);
		MousePanel panel = new MousePanel();
		clearInvocations(configManager);

		getCheckBox(panel, "advancedMouseControls").doClick();
		getSlider(panel, "mouseReactionDelaySlider").setValue(60);
		getSlider(panel, "mouseCurveScaleSlider").setValue(150);
		getSlider(panel, "mouseCorrectionScaleSlider").setValue(50);

		assertEquals(60, Rs2AntibanSettings.mouseReactionDelayMs);
		assertEquals(150, Rs2AntibanSettings.mouseCurveScale);
		assertEquals(50, Rs2AntibanSettings.mouseCorrectionScale);
		verify(configManager, atLeastOnce()).setConfiguration(anyString(), anyString(), anyString());
	}

	@Test
	public void manualAdvancedTimingRandomSelectionSavesRanges() throws Exception
	{
		ConfigManager configManager = mock(ConfigManager.class);
		setProfileConfigManager(configManager);
		MousePanel panel = new MousePanel();
		clearInvocations(configManager);

		getCheckBox(panel, "advancedMouseControls").doClick();
		getCheckBox(panel, "mouseButtonHoldRandom").doClick();
		getSlider(panel, "mouseButtonHoldMinSlider").setValue(40);
		getSlider(panel, "mouseButtonHoldMaxSlider").setValue(90);

		assertTrue(Rs2AntibanSettings.mouseButtonHoldRandom);
		assertEquals(40, Rs2AntibanSettings.mouseButtonHoldMinMs);
		assertEquals(90, Rs2AntibanSettings.mouseButtonHoldMaxMs);
		verify(configManager, atLeastOnce()).setConfiguration(anyString(), anyString(), anyString());
	}

	@Test
	public void randomTimingShowsRangeControlsInsteadOfStaticSlider() throws Exception
	{
		MousePanel panel = new MousePanel();

		getCheckBox(panel, "advancedMouseControls").doClick();
		getCheckBox(panel, "mouseSettleDelayRandom").doClick();

		assertEquals("Settle (random)", getLabel(panel, "mouseSettleDelayLabel").getText());
		assertFalse(getSlider(panel, "mouseSettleDelaySlider").isVisible());
		assertTrue(getLabel(panel, "mouseSettleDelayMinLabel").isVisible());
		assertTrue(getSlider(panel, "mouseSettleDelayMinSlider").isVisible());
		assertTrue(getLabel(panel, "mouseSettleDelayMaxLabel").isVisible());
		assertTrue(getSlider(panel, "mouseSettleDelayMaxSlider").isVisible());
	}

	@Test
	public void defaultAdvancedTimingValuesAlignWithSliderTicks() throws Exception
	{
		MousePanel panel = new MousePanel();

		assertEquals(Rs2AntibanSettings.mouseReactionDelayMinMs,
			getSlider(panel, "mouseReactionDelayMinSlider").getValue());
		assertEquals(Rs2AntibanSettings.mouseReactionDelayMaxMs,
			getSlider(panel, "mouseReactionDelayMaxSlider").getValue());
		assertEquals(Rs2AntibanSettings.mouseSettleDelayMinMs,
			getSlider(panel, "mouseSettleDelayMinSlider").getValue());
		assertEquals(Rs2AntibanSettings.mouseSettleDelayMaxMs,
			getSlider(panel, "mouseSettleDelayMaxSlider").getValue());
		assertEquals(Rs2AntibanSettings.mouseButtonHoldMinMs,
			getSlider(panel, "mouseButtonHoldMinSlider").getValue());
		assertEquals(Rs2AntibanSettings.mouseButtonHoldMaxMs,
			getSlider(panel, "mouseButtonHoldMaxSlider").getValue());
		assertSliderValueIsTickAligned(panel, "mouseReactionDelayMinSlider");
		assertSliderValueIsTickAligned(panel, "mouseReactionDelayMaxSlider");
		assertSliderValueIsTickAligned(panel, "mouseSettleDelayMinSlider");
		assertSliderValueIsTickAligned(panel, "mouseSettleDelayMaxSlider");
		assertSliderValueIsTickAligned(panel, "mouseButtonHoldMinSlider");
		assertSliderValueIsTickAligned(panel, "mouseButtonHoldMaxSlider");
	}

	@Test
	public void updateValuesDoesNotFightActiveAdvancedTimingRangeDrag() throws Exception
	{
		MousePanel panel = new MousePanel();
		getCheckBox(panel, "advancedMouseControls").doClick();
		getCheckBox(panel, "mouseSettleDelayRandom").doClick();
		JSlider minSlider = getSlider(panel, "mouseSettleDelayMinSlider");
		JSlider maxSlider = getSlider(panel, "mouseSettleDelayMaxSlider");

		minSlider.getModel().setValueIsAdjusting(true);
		minSlider.setValue(90);
		maxSlider.setValue(120);
		Rs2AntibanSettings.mouseSettleDelayMinMs = 20;
		Rs2AntibanSettings.mouseSettleDelayMaxMs = 70;

		panel.updateValues();

		assertEquals(90, minSlider.getValue());
		assertEquals(120, maxSlider.getValue());
		assertEquals("Settle Min (ms): 90", getLabel(panel, "mouseSettleDelayMinLabel").getText());
		assertEquals("Settle Max (ms): 120", getLabel(panel, "mouseSettleDelayMaxLabel").getText());
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

	private static JCheckBox getCheckBox(MousePanel panel, String name) throws Exception
	{
		Field field = MousePanel.class.getDeclaredField(name);
		field.setAccessible(true);
		return (JCheckBox) field.get(panel);
	}

	private static void assertSliderFitsCommonCard(MousePanel panel, String name) throws Exception
	{
		JSlider slider = getSlider(panel, name);
		int availableRowWidth = COMMON_ANTIBAN_CARD_WIDTH - 10;

		assertTrue(name + " preferred width must fit inside a mouse-card row. Preferred width was "
				+ slider.getPreferredSize().width,
			slider.getPreferredSize().width <= availableRowWidth);
	}

	private static void assertSliderValueIsTickAligned(MousePanel panel, String name) throws Exception
	{
		JSlider slider = getSlider(panel, name);
		int spacing = slider.getMinorTickSpacing();
		assertEquals(name + " default value should sit on a tick to prevent snap jitter",
			0, slider.getValue() % spacing);
	}

	private static JComboBox<?> getComboBox(MousePanel panel, String name) throws Exception
	{
		Field field = MousePanel.class.getDeclaredField(name);
		field.setAccessible(true);
		return (JComboBox<?>) field.get(panel);
	}
}
