package net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.util;

import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSmoothness;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.api.MouseMotionFactory;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.support.DefaultMouseMotionNature;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.support.DefaultNoiseProvider;
import net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.support.SinusoidalDeviationProvider;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FactoryTemplatesMouseSpeedTest
{
	@Test
	public void effectiveMouseBaseTimeUsesPresetWhenUnfatigued()
	{
		assertEquals(MouseSpeed.NORMAL.getBaseTimeMs(),
			FactoryTemplates.effectiveMouseBaseTimeMs(MouseSpeed.NORMAL, 1.0));
	}

	@Test
	public void effectiveMouseBaseTimeAppliesFatigueMultiplier()
	{
		assertEquals(221, FactoryTemplates.effectiveMouseBaseTimeMs(MouseSpeed.NORMAL, 1.30));
	}

	@Test
	public void effectiveMouseBaseTimeCapsAtSpeedMaximum()
	{
		assertEquals(MouseSpeed.NORMAL.getFatigueMaxBaseTimeMs(),
			FactoryTemplates.effectiveMouseBaseTimeMs(MouseSpeed.NORMAL, 3.0));
	}

	@Test
	public void effectiveMouseBaseTimeUsesDefaultForNullSpeed()
	{
		assertEquals(MouseSpeed.DEFAULT.getBaseTimeMs(),
			FactoryTemplates.effectiveMouseBaseTimeMs(null, 1.0));
	}

	@Test
	public void mouseSmoothnessPresetAdjustsMotionNatureWithoutChangingSpeedDuration() throws Exception
	{
		MouseMotionFactory standard = FactoryTemplates.createMouseSpeedMotionFactory(
			new DefaultMouseMotionNature(),
			MouseSpeed.NORMAL,
			MouseSpeed.NORMAL.getBaseTimeMs(),
			MouseSmoothness.STANDARD);
		MouseMotionFactory max = FactoryTemplates.createMouseSpeedMotionFactory(
			new DefaultMouseMotionNature(),
			MouseSpeed.NORMAL,
			MouseSpeed.NORMAL.getBaseTimeMs(),
			MouseSmoothness.MAX);

		assertEquals(MouseSmoothness.STANDARD.getTimeToStepsDivider(),
			standard.getNature().getTimeToStepsDivider(), 1e-9);
		assertEquals(MouseSmoothness.STANDARD.getMinSteps(), standard.getNature().getMinSteps());
		assertEquals(MouseSmoothness.STANDARD.getEffectFadeSteps(), standard.getNature().getEffectFadeSteps());
		assertEquals(MouseSmoothness.STANDARD.getDeviationSlopeDivider(), slopeDivider(standard), 1e-9);
		assertEquals(MouseSmoothness.STANDARD.getNoiseDivider(), noiseDivider(standard), 1e-9);

		assertEquals(MouseSmoothness.MAX.getTimeToStepsDivider(), max.getNature().getTimeToStepsDivider(), 1e-9);
		assertEquals(MouseSmoothness.MAX.getMinSteps(), max.getNature().getMinSteps());
		assertEquals(MouseSmoothness.MAX.getEffectFadeSteps(), max.getNature().getEffectFadeSteps());
		assertEquals(MouseSmoothness.MAX.getDeviationSlopeDivider(), slopeDivider(max), 1e-9);
		assertEquals(MouseSmoothness.MAX.getNoiseDivider(), noiseDivider(max), 1e-9);

		assertTrue(max.getNature().getMinSteps() > standard.getNature().getMinSteps());
		assertTrue(max.getNature().getEffectFadeSteps() > standard.getNature().getEffectFadeSteps());
	}

	private static double slopeDivider(MouseMotionFactory factory) throws Exception
	{
		Field field = SinusoidalDeviationProvider.class.getDeclaredField("slopeDivider");
		field.setAccessible(true);
		return (double) field.get(factory.getDeviationProvider());
	}

	private static double noiseDivider(MouseMotionFactory factory) throws Exception
	{
		Field field = DefaultNoiseProvider.class.getDeclaredField("noisinessDivider");
		field.setAccessible(true);
		return (double) field.get(factory.getNoiseProvider());
	}
}
