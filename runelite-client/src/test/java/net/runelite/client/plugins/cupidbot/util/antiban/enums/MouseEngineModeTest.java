package net.runelite.client.plugins.cupidbot.util.antiban.enums;

import net.runelite.client.plugins.cupidbot.util.antiban.Rs2AntibanSettings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class MouseEngineModeTest
{
	@Test
	public void defaultMouseEngineModeIsBalanced()
	{
		assertSame(MouseEngineMode.BALANCED, MouseEngineMode.DEFAULT);
	}

	@Test
	public void configParsingUsesDefaultForBlankOrInvalidValues()
	{
		assertSame(MouseEngineMode.DEFAULT, MouseEngineMode.fromConfigValue(null));
		assertSame(MouseEngineMode.DEFAULT, MouseEngineMode.fromConfigValue(""));
		assertSame(MouseEngineMode.DEFAULT, MouseEngineMode.fromConfigValue("missing"));
		assertSame(MouseEngineMode.PRECISE, MouseEngineMode.fromConfigValue("PRECISE"));
	}

	@Test
	public void resetRestoresDefaultMouseEngineMode()
	{
		Rs2AntibanSettings.mouseEngineMode = MouseEngineMode.QA_REPLAY;

		Rs2AntibanSettings.reset();

		assertSame(MouseEngineMode.DEFAULT, Rs2AntibanSettings.mouseEngineMode);
	}

	@Test
	public void displayNamesAreCompact()
	{
		assertEquals("Balanced", MouseEngineMode.BALANCED.getName());
		assertEquals("Precise", MouseEngineMode.PRECISE.getName());
		assertEquals("Relaxed", MouseEngineMode.RELAXED.getName());
		assertEquals("QA Replay", MouseEngineMode.QA_REPLAY.getName());
	}
}
