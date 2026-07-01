package net.runelite.client.plugins.cupidbot.util.antiban.enums;

import net.runelite.client.plugins.cupidbot.util.antiban.Rs2AntibanSettings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MouseSpeedTest
{
	@Test
	public void sliderIndexesCoverEveryPresetInOrder()
	{
		MouseSpeed[] speeds = MouseSpeed.values();

		assertEquals(9, speeds.length);
		for (int i = 0; i < speeds.length; i++)
		{
			assertEquals(i, speeds[i].getSliderIndex());
			assertSame(speeds[i], MouseSpeed.fromSliderIndex(i));
		}
		assertSame(MouseSpeed.VERY_SLOW, MouseSpeed.fromSliderIndex(-1));
		assertSame(MouseSpeed.EXTREME, MouseSpeed.fromSliderIndex(100));
	}

	@Test
	public void displayNamesAreNumericPresets()
	{
		MouseSpeed[] speeds = MouseSpeed.values();

		for (int i = 0; i < speeds.length; i++)
		{
			assertEquals(String.valueOf(i + 1), speeds[i].getName());
		}
	}

	@Test
	public void baseMovementTimeGetsFasterForEachPreset()
	{
		MouseSpeed previous = null;
		for (MouseSpeed speed : MouseSpeed.values())
		{
			assertTrue(speed.getFatigueMaxBaseTimeMs() >= speed.getBaseTimeMs());
			if (previous != null)
			{
				assertTrue(
					speed.getName() + " should be faster than " + previous.getName(),
					speed.getBaseTimeMs() < previous.getBaseTimeMs());
			}
			previous = speed;
		}
	}

	@Test
	public void legacyActivityIntensityMapsToNearestMouseSpeed()
	{
		assertSame(MouseSpeed.VERY_SLOW, MouseSpeed.fromActivityIntensity(ActivityIntensity.VERY_LOW));
		assertSame(MouseSpeed.SLOW, MouseSpeed.fromActivityIntensity(ActivityIntensity.LOW));
		assertSame(MouseSpeed.NORMAL, MouseSpeed.fromActivityIntensity(ActivityIntensity.MODERATE));
		assertSame(MouseSpeed.FAST, MouseSpeed.fromActivityIntensity(ActivityIntensity.HIGH));
		assertSame(MouseSpeed.EXTREME, MouseSpeed.fromActivityIntensity(ActivityIntensity.EXTREME));
		assertSame(MouseSpeed.DEFAULT, MouseSpeed.fromActivityIntensity(null));
	}

	@Test
	public void defaultMouseSpeedIsMiddlePreset()
	{
		assertSame(MouseSpeed.NORMAL, MouseSpeed.DEFAULT);
	}

	@Test
	public void resetRestoresDefaultMouseSpeed()
	{
		Rs2AntibanSettings.mouseSpeed = MouseSpeed.VERY_SLOW;

		Rs2AntibanSettings.reset();

		assertSame(MouseSpeed.DEFAULT, Rs2AntibanSettings.mouseSpeed);
	}

	@Test
	public void effectiveMouseSpeedUsesConfiguredSpeedWhenDynamicIntensityIsOff()
	{
		Rs2AntibanSettings.dynamicIntensity = false;
		Rs2AntibanSettings.mouseSpeed = MouseSpeed.STEADY;

		assertSame(MouseSpeed.STEADY, Rs2AntibanSettings.getEffectiveMouseSpeed(ActivityIntensity.EXTREME));
	}

	@Test
	public void effectiveMouseSpeedUsesActivityIntensityWhenDynamicIntensityIsOn()
	{
		Rs2AntibanSettings.dynamicIntensity = true;
		Rs2AntibanSettings.mouseSpeed = MouseSpeed.EXTREME;

		assertSame(MouseSpeed.SLOW, Rs2AntibanSettings.getEffectiveMouseSpeed(ActivityIntensity.LOW));
		assertSame(MouseSpeed.EXTREME, Rs2AntibanSettings.getEffectiveMouseSpeed(ActivityIntensity.EXTREME));
	}

	@Test
	public void dynamicIntensityCanExceedConfiguredMouseSpeed()
	{
		Rs2AntibanSettings.dynamicIntensity = true;
		Rs2AntibanSettings.mouseSpeed = MouseSpeed.SLOW;

		assertSame(MouseSpeed.FAST, Rs2AntibanSettings.getEffectiveMouseSpeed(ActivityIntensity.HIGH));
		assertSame(MouseSpeed.EXTREME, Rs2AntibanSettings.getEffectiveMouseSpeed(ActivityIntensity.EXTREME));
	}
}
