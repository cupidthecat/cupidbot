package net.runelite.client.plugins.cupidbot.util.antiban;

import net.runelite.client.plugins.cupidbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.PlayStyle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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
	public void actionCooldownChanceAboveOneAlwaysTriggers()
	{
		Rs2AntibanSettings.usePlayStyle = true;
		Rs2AntibanSettings.actionCooldownChance = 1.5;
		Rs2Antiban.setActivity(Activity.GENERAL_COMBAT);

		Rs2Antiban.actionCooldown();

		assertTrue(Rs2AntibanSettings.actionCooldownActive);
		assertTrue(Rs2Antiban.getTIMEOUT() > 0);
	}

	@Test
	public void actionCooldownChanceAtOrBelowZeroNeverTriggers()
	{
		Rs2AntibanSettings.usePlayStyle = true;
		Rs2AntibanSettings.actionCooldownChance = -0.1;
		Rs2Antiban.setActivity(Activity.GENERAL_COMBAT);

		Rs2Antiban.actionCooldown();

		assertFalse(Rs2AntibanSettings.actionCooldownActive);
		assertEquals(0, Rs2Antiban.getTIMEOUT());
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

	@Test
	public void samePlayStyleIntensityChangeDoesNotResetAttentionTimer() throws Exception
	{
		Rs2Antiban.setActivity(Activity.GENERAL_COMBAT);
		PlayStyle playStyle = Rs2Antiban.getPlayStyle();
		Instant startTime = Instant.parse("2026-06-26T12:00:00Z");
		int attentionSpan = 900;
		setStartTime(playStyle, startTime);
		setAttentionSpan(playStyle, attentionSpan);

		Rs2Antiban.setActivityIntensity(ActivityIntensity.EXTREME);

		assertSame(ActivityIntensity.EXTREME, Rs2Antiban.getActivityIntensity());
		assertSame(playStyle, Rs2Antiban.getPlayStyle());
		assertEquals(startTime, getStartTime(playStyle));
		assertEquals(attentionSpan, getAttentionSpan(playStyle));
	}

	@Test
	public void playStyleSwitchUpdatesDynamicIntensityMouseSpeed()
	{
		Rs2AntibanSettings.dynamicActivity = true;
		Rs2AntibanSettings.dynamicIntensity = true;
		Rs2AntibanSettings.mouseSpeed = MouseSpeed.EXTREME;
		Rs2Antiban.setActivity(Activity.GENERAL_COMBAT);

		Rs2Antiban.setPlayStyle(PlayStyle.CAUTIOUS);

		assertSame(PlayStyle.CAUTIOUS, Rs2Antiban.getPlayStyle());
		assertSame(ActivityIntensity.LOW, Rs2Antiban.getActivityIntensity());
		assertSame(MouseSpeed.SLOW, Rs2AntibanSettings.getEffectiveMouseSpeed(Rs2Antiban.getActivityIntensity()));
	}

	@Test
	public void dynamicIntensitySwitchesPlayStyleWhenAttentionTimerExpires() throws Exception
	{
		Rs2AntibanSettings.usePlayStyle = true;
		Rs2AntibanSettings.simulateAttentionSpan = true;
		Rs2AntibanSettings.profileSwitching = false;
		Rs2AntibanSettings.dynamicIntensity = true;
		Rs2AntibanSettings.mouseSpeed = MouseSpeed.EXTREME;
		Rs2Antiban.setActivity(Activity.GENERAL_COMBAT);

		PlayStyle initialPlayStyle = Rs2Antiban.getPlayStyle();
		setStartTime(initialPlayStyle, Instant.now().minusSeconds(10));
		setAttentionSpan(initialPlayStyle, 1);

		assertTrue(Rs2Antiban.switchPlayStyleIfAttentionExpired());
		assertSame(PlayStyle.AGGRESSIVE, Rs2Antiban.getPlayStyle());
		assertSame(ActivityIntensity.MODERATE, Rs2Antiban.getActivityIntensity());
		assertSame(MouseSpeed.NORMAL, Rs2AntibanSettings.getEffectiveMouseSpeed(Rs2Antiban.getActivityIntensity()));
	}

	@Test
	public void dynamicActivityRefreshesCurrentActivityIntensity()
	{
		Rs2AntibanSettings.dynamicActivity = true;
		Rs2AntibanSettings.dynamicIntensity = true;
		Rs2AntibanSettings.mouseSpeed = MouseSpeed.EXTREME;
		Rs2Antiban.setActivity(Activity.GENERAL_FIREMAKING);
		Rs2Antiban.setActivityIntensity(ActivityIntensity.HIGH);

		Rs2Antiban.refreshDynamicActivity();

		assertSame(Activity.GENERAL_FIREMAKING, Rs2Antiban.getActivity());
		assertSame(ActivityIntensity.LOW, Rs2Antiban.getActivityIntensity());
		assertSame(PlayStyle.CAUTIOUS, Rs2Antiban.getPlayStyle());
		assertSame(MouseSpeed.SLOW, Rs2AntibanSettings.getEffectiveMouseSpeed(Rs2Antiban.getActivityIntensity()));
	}

	private static void setStartTime(PlayStyle playStyle, Instant startTime) throws Exception
	{
		Field field = PlayStyle.class.getDeclaredField("startTime");
		field.setAccessible(true);
		field.set(playStyle, startTime);
	}

	private static Instant getStartTime(PlayStyle playStyle) throws Exception
	{
		Field field = PlayStyle.class.getDeclaredField("startTime");
		field.setAccessible(true);
		return (Instant) field.get(playStyle);
	}

	private static void setAttentionSpan(PlayStyle playStyle, int attentionSpan) throws Exception
	{
		Field field = PlayStyle.class.getDeclaredField("attentionSpan");
		field.setAccessible(true);
		field.setInt(playStyle, attentionSpan);
	}

	private static int getAttentionSpan(PlayStyle playStyle) throws Exception
	{
		Field field = PlayStyle.class.getDeclaredField("attentionSpan");
		field.setAccessible(true);
		return field.getInt(playStyle);
	}
}
