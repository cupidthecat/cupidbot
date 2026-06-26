package net.runelite.client.plugins.cupidbot.util.antiban.ui;

import net.runelite.client.plugins.cupidbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MousePanelTest
{
	@Before
	public void setUp()
	{
		Rs2Antiban.resetAntibanSettings(true);
	}

	@After
	public void tearDown()
	{
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

	private static void setActivityIntensity(ActivityIntensity activityIntensity) throws Exception
	{
		Field field = Rs2Antiban.class.getDeclaredField("activityIntensity");
		field.setAccessible(true);
		field.set(null, activityIntensity);
	}
}
