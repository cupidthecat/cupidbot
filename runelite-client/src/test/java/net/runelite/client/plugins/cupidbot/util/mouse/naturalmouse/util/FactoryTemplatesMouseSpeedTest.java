package net.runelite.client.plugins.cupidbot.util.mouse.naturalmouse.util;

import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
