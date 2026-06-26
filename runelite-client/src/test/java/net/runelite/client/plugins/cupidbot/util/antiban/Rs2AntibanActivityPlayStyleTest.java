package net.runelite.client.plugins.cupidbot.util.antiban;

import net.runelite.client.plugins.cupidbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.PlayStyle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class Rs2AntibanActivityPlayStyleTest
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
	public void attentionSpanUsesActivityIntensityPlayStyle()
	{
		Rs2AntibanSettings.simulateAttentionSpan = true;

		Rs2Antiban.setActivity(Activity.GENERAL_WOODCUTTING);

		assertSame(ActivityIntensity.LOW, Rs2Antiban.getActivityIntensity());
		assertSame(PlayStyle.CAUTIOUS, Rs2Antiban.getPlayStyle());
		assertEquals(17, Rs2Antiban.getPlayStyle().getPrimaryTickInterval());
		assertEquals(30, Rs2Antiban.getPlayStyle().getSecondaryTickInterval());
	}

	@Test
	public void repeatedSameActivityDoesNotOverwriteSwitchedPlayStyle()
	{
		Rs2AntibanSettings.simulateAttentionSpan = true;
		Rs2AntibanSettings.profileSwitching = true;

		Rs2Antiban.setActivity(Activity.GENERAL_COMBAT);
		Rs2Antiban.setPlayStyle(PlayStyle.AGGRESSIVE);

		Rs2Antiban.setActivity(Activity.GENERAL_COMBAT);

		assertSame(ActivityIntensity.HIGH, Rs2Antiban.getActivityIntensity());
		assertSame(PlayStyle.AGGRESSIVE, Rs2Antiban.getPlayStyle());
	}

	@Test
	public void changedActivityIntensityUpdatesPlayStyle()
	{
		Rs2Antiban.setActivity(Activity.GENERAL_COMBAT);

		Rs2Antiban.setActivityIntensity(ActivityIntensity.LOW);

		assertSame(ActivityIntensity.LOW, Rs2Antiban.getActivityIntensity());
		assertSame(PlayStyle.CAUTIOUS, Rs2Antiban.getPlayStyle());
	}
}
