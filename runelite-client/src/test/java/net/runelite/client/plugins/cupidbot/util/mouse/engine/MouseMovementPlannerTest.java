package net.runelite.client.plugins.cupidbot.util.mouse.engine;

import net.runelite.api.Point;
import net.runelite.client.plugins.cupidbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseEngineMode;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSmoothness;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;
import org.junit.After;
import org.junit.Test;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MouseMovementPlannerTest
{
	@After
	public void tearDown()
	{
		Rs2AntibanSettings.reset();
	}

	@Test
	public void smallerTargetsTakeLongerAndUseTighterEndpointError()
	{
		MouseMovementPlanner planner = new MouseMovementPlanner();
		Point start = new Point(10, 10);

		MouseMovementPlan small = planner.plan(
			start,
			MouseTarget.rectangle(new Rectangle(300, 100, 4, 4)),
			MouseActionContext.INVENTORY,
			MouseEngineMode.BALANCED,
			MouseSpeed.NORMAL,
			MouseSmoothness.DEFAULT,
			123L);
		MouseMovementPlan large = planner.plan(
			start,
			MouseTarget.rectangle(new Rectangle(300, 100, 80, 40)),
			MouseActionContext.INVENTORY,
			MouseEngineMode.BALANCED,
			MouseSpeed.NORMAL,
			MouseSmoothness.DEFAULT,
			123L);

		assertTrue("small targets should slow acquisition", small.getDurationMs() > large.getDurationMs());
		assertTrue("small targets should allow less endpoint error",
			small.getEndpointErrorRadius() < large.getEndpointErrorRadius());
		assertTrue(small.getTarget().contains(small.getTargetPoint()));
		assertTrue(large.getTarget().contains(large.getTargetPoint()));
	}

	@Test
	public void seededPlanningRepeatsTargetPointDurationAndCorrections()
	{
		MouseMovementPlanner planner = new MouseMovementPlanner();
		MouseTarget target = MouseTarget.rectangle(new Rectangle(200, 150, 60, 30));
		Point start = new Point(25, 40);

		MouseMovementPlan first = planner.plan(
			start, target, MouseActionContext.WORLD_OBJECT, MouseEngineMode.RELAXED,
			MouseSpeed.BRISK, MouseSmoothness.HIGH, 987654321L);
		MouseMovementPlan second = planner.plan(
			start, target, MouseActionContext.WORLD_OBJECT, MouseEngineMode.RELAXED,
			MouseSpeed.BRISK, MouseSmoothness.HIGH, 987654321L);

		assertEquals(first.getTargetPoint(), second.getTargetPoint());
		assertEquals(first.getDurationMs(), second.getDurationMs());
		assertEquals(first.getCorrectionCount(), second.getCorrectionCount());
		assertEquals(first.getOvershootCount(), second.getOvershootCount());
		assertEquals(first.getSeed(), second.getSeed());
	}

	@Test
	public void qaReplayUsesExactCenterAndDisablesErrorAndOvershoot()
	{
		MouseMovementPlanner planner = new MouseMovementPlanner();
		MouseTarget target = MouseTarget.rectangle(new Rectangle(100, 50, 40, 20));
		MouseMovementTuning tuning = new MouseMovementTuning(70, 80, 90, 150, 130, 120, 110, 100);

		MouseMovementPlan plan = planner.plan(
			new Point(0, 0),
			target,
			MouseActionContext.QA_REPLAY,
			MouseEngineMode.QA_REPLAY,
			MouseSpeed.NORMAL,
			MouseSmoothness.DEFAULT,
			99L,
			tuning);

		assertEquals(target.getCenter(), plan.getTargetPoint());
		assertEquals(0, plan.getEndpointErrorRadius());
		assertEquals(0, plan.getOvershootCount());
		assertEquals(0, plan.getCorrectionCount());
		assertEquals(0, plan.getReactionDelayMs());
		assertEquals(0, plan.getSettleDelayMs());
		assertEquals(0, plan.getButtonDownTimeMs());
	}

	@Test
	public void tuningControlsTimingTrajectoryTextureAndCorrectionScales()
	{
		MouseMovementPlanner planner = new MouseMovementPlanner();
		Point start = new Point(5, 5);
		MouseTarget target = MouseTarget.rectangle(new Rectangle(500, 240, 4, 4));
		MouseMovementTuning detailed = new MouseMovementTuning(60, 45, 75, 150, 130, 70, 200, 50);
		MouseMovementTuning disabledMistakes = new MouseMovementTuning(60, 45, 75, 150, 130, 70, 0, 0);

		MouseMovementPlan detailedPlan = planner.plan(
			start, target, MouseActionContext.WORLD_OBJECT, MouseEngineMode.RELAXED,
			MouseSpeed.NORMAL, MouseSmoothness.DEFAULT, 123L, detailed);
		MouseMovementPlan repeatedPlan = planner.plan(
			start, target, MouseActionContext.WORLD_OBJECT, MouseEngineMode.RELAXED,
			MouseSpeed.NORMAL, MouseSmoothness.DEFAULT, 123L, detailed);
		MouseMovementPlan disabledMistakePlan = planner.plan(
			start, target, MouseActionContext.WORLD_OBJECT, MouseEngineMode.RELAXED,
			MouseSpeed.NORMAL, MouseSmoothness.DEFAULT, 123L, disabledMistakes);

		assertEquals(detailedPlan.getTargetPoint(), repeatedPlan.getTargetPoint());
		assertEquals(60, detailedPlan.getReactionDelayMs());
		assertEquals(45, detailedPlan.getSettleDelayMs());
		assertEquals(75, detailedPlan.getButtonDownTimeMs());
		assertEquals(150, detailedPlan.getCurvePercent());
		assertEquals(130, detailedPlan.getPathNoisePercent());
		assertEquals(70, detailedPlan.getMicroJitterPercent());
		assertTrue("overshoot scale should allow overshoots on difficult targets",
			detailedPlan.getOvershootCount() > 0);
		assertTrue("correction scale should retain at least one correction for a small difficult target",
			detailedPlan.getCorrectionCount() > 0);
		assertEquals(0, disabledMistakePlan.getOvershootCount());
		assertEquals(0, disabledMistakePlan.getCorrectionCount());
	}

	@Test
	public void contextAndDifficultyChooseTrajectoryStyleAndTexture()
	{
		MouseMovementPlanner planner = new MouseMovementPlanner();
		Point start = new Point(5, 5);

		MouseMovementPlan dragPlan = planner.plan(
			start,
			MouseTarget.rectangle(new Rectangle(300, 240, 30, 30)),
			MouseActionContext.DRAG,
			MouseEngineMode.BALANCED,
			MouseSpeed.NORMAL,
			MouseSmoothness.DEFAULT,
			123L);
		MouseMovementPlan scrollPlan = planner.plan(
			start,
			MouseTarget.rectangle(new Rectangle(300, 240, 30, 30)),
			MouseActionContext.SCROLL,
			MouseEngineMode.BALANCED,
			MouseSpeed.NORMAL,
			MouseSmoothness.DEFAULT,
			123L);
		MouseMovementPlan difficultPlan = planner.plan(
			start,
			MouseTarget.rectangle(new Rectangle(600, 420, 4, 4)),
			MouseActionContext.WORLD_OBJECT,
			MouseEngineMode.RELAXED,
			MouseSpeed.NORMAL,
			MouseSmoothness.DEFAULT,
			123L);

		assertEquals(MouseTrajectoryStyle.DRAG_STABLE, dragPlan.getTrajectoryStyle());
		assertEquals(MouseTrajectoryStyle.SCROLL_SMOOTH, scrollPlan.getTrajectoryStyle());
		assertEquals(MouseTrajectoryStyle.CORRECTIVE, difficultPlan.getTrajectoryStyle());
		assertEquals(0, dragPlan.getOvershootCount());
		assertEquals(0, scrollPlan.getOvershootCount());
		assertTrue("drag movement should reduce low-frequency path drift",
			dragPlan.getEffectivePathNoiseMultiplier() < difficultPlan.getEffectivePathNoiseMultiplier());
		assertTrue("drag movement should reduce micro jitter",
			dragPlan.getEffectiveMicroJitterMultiplier() < difficultPlan.getEffectiveMicroJitterMultiplier());
		assertTrue("difficult target should expose a useful Fitts-style difficulty",
			difficultPlan.getDifficultyIndex() > dragPlan.getDifficultyIndex());
	}

	@Test
	public void endpointErrorScaleCanTightenTargetAcquisition()
	{
		MouseMovementPlanner planner = new MouseMovementPlanner();
		Point start = new Point(5, 5);
		MouseTarget target = MouseTarget.rectangle(new Rectangle(300, 240, 80, 60));

		Rs2AntibanSettings.mouseEndpointErrorScale = 100;
		MouseMovementPlan normalPlan = planner.plan(
			start,
			target,
			MouseActionContext.MENU,
			MouseEngineMode.BALANCED,
			MouseSpeed.NORMAL,
			MouseSmoothness.DEFAULT,
			123L);
		Rs2AntibanSettings.mouseEndpointErrorScale = 0;
		MouseMovementPlan tightPlan = planner.plan(
			start,
			target,
			MouseActionContext.MENU,
			MouseEngineMode.BALANCED,
			MouseSpeed.NORMAL,
			MouseSmoothness.DEFAULT,
			123L);

		assertTrue(normalPlan.getEndpointErrorRadius() > 0);
		assertEquals(0, tightPlan.getEndpointErrorRadius());
	}

	@Test
	public void targetPointSamplingIsCenterBiasedForLargeTargets()
	{
		MouseMovementPlanner planner = new MouseMovementPlanner();
		Point start = new Point(10, 10);
		MouseTarget target = MouseTarget.rectangle(new Rectangle(200, 200, 100, 100));
		double totalDistanceFromCenter = 0.0;

		for (long seed = 1; seed <= 80; seed++)
		{
			MouseMovementPlan plan = planner.plan(
				start,
				target,
				MouseActionContext.MENU,
				MouseEngineMode.BALANCED,
				MouseSpeed.NORMAL,
				MouseSmoothness.DEFAULT,
				seed);
			Point targetPoint = plan.getTargetPoint();
			assertTrue(target.contains(targetPoint));
			totalDistanceFromCenter += Math.hypot(
				targetPoint.getX() - target.getCenter().getX(),
				targetPoint.getY() - target.getCenter().getY());
		}

		assertTrue("target sampling should prefer the middle of large clickboxes",
			totalDistanceFromCenter / 80.0 < 24.0);
	}

	@Test
	public void randomTimingSettingsAreSampledPerPlanWithinConfiguredRanges()
	{
		Rs2AntibanSettings.mouseReactionDelayRandom = true;
		Rs2AntibanSettings.mouseReactionDelayMinMs = 10;
		Rs2AntibanSettings.mouseReactionDelayMaxMs = 25;
		Rs2AntibanSettings.mouseSettleDelayRandom = true;
		Rs2AntibanSettings.mouseSettleDelayMinMs = 30;
		Rs2AntibanSettings.mouseSettleDelayMaxMs = 55;
		Rs2AntibanSettings.mouseButtonHoldRandom = true;
		Rs2AntibanSettings.mouseButtonHoldMinMs = 60;
		Rs2AntibanSettings.mouseButtonHoldMaxMs = 95;

		MouseMovementPlanner planner = new MouseMovementPlanner();
		MouseTarget target = MouseTarget.rectangle(new Rectangle(200, 150, 60, 30));
		Point start = new Point(25, 40);
		Set<Integer> reactionDelays = new HashSet<>();
		Set<Integer> settleDelays = new HashSet<>();
		Set<Integer> buttonHoldDelays = new HashSet<>();

		for (long seed = 1; seed <= 30; seed++)
		{
			MouseMovementPlan plan = planner.plan(
				start,
				target,
				MouseActionContext.WORLD_OBJECT,
				MouseEngineMode.BALANCED,
				MouseSpeed.NORMAL,
				MouseSmoothness.DEFAULT,
				seed);

			assertTrue(plan.getReactionDelayMs() >= 10 && plan.getReactionDelayMs() <= 25);
			assertTrue(plan.getSettleDelayMs() >= 30 && plan.getSettleDelayMs() <= 55);
			assertTrue(plan.getButtonDownTimeMs() >= 60 && plan.getButtonDownTimeMs() <= 95);
			reactionDelays.add(plan.getReactionDelayMs());
			settleDelays.add(plan.getSettleDelayMs());
			buttonHoldDelays.add(plan.getButtonDownTimeMs());
		}

		assertTrue("reaction timing should vary across plans", reactionDelays.size() > 1);
		assertTrue("settle timing should vary across plans", settleDelays.size() > 1);
		assertTrue("button hold timing should vary across plans", buttonHoldDelays.size() > 1);
	}

	@Test
	public void settleRandomRangeDoesNotRandomizeStaticReactionOrButtonHold()
	{
		Rs2AntibanSettings.mouseReactionDelayMs = 30;
		Rs2AntibanSettings.mouseReactionDelayRandom = false;
		Rs2AntibanSettings.mouseSettleDelayMs = 35;
		Rs2AntibanSettings.mouseSettleDelayRandom = true;
		Rs2AntibanSettings.mouseSettleDelayMinMs = 90;
		Rs2AntibanSettings.mouseSettleDelayMaxMs = 330;
		Rs2AntibanSettings.mouseButtonHoldMs = 55;
		Rs2AntibanSettings.mouseButtonHoldRandom = false;

		MouseMovementPlanner planner = new MouseMovementPlanner();
		MouseTarget target = MouseTarget.rectangle(new Rectangle(200, 150, 60, 30));
		Point start = new Point(25, 40);
		Set<Integer> reactionDelays = new HashSet<>();
		Set<Integer> settleDelays = new HashSet<>();
		Set<Integer> buttonHoldDelays = new HashSet<>();

		for (long seed = 1; seed <= 30; seed++)
		{
			MouseMovementPlan plan = planner.plan(
				start,
				target,
				MouseActionContext.WORLD_OBJECT,
				MouseEngineMode.BALANCED,
				MouseSpeed.NORMAL,
				MouseSmoothness.DEFAULT,
				seed);

			assertEquals(30, plan.getReactionDelayMs());
			assertTrue("settle delay " + plan.getSettleDelayMs() + " should use configured range",
				plan.getSettleDelayMs() >= 90 && plan.getSettleDelayMs() <= 330);
			assertEquals(55, plan.getButtonDownTimeMs());
			reactionDelays.add(plan.getReactionDelayMs());
			settleDelays.add(plan.getSettleDelayMs());
			buttonHoldDelays.add(plan.getButtonDownTimeMs());
		}

		assertEquals(1, reactionDelays.size());
		assertTrue("settle timing should vary across plans", settleDelays.size() > 1);
		assertEquals(1, buttonHoldDelays.size());
	}

	@Test
	public void reportCalculatesPathMetricsFromObservedPoints()
	{
		MouseMovementPlan plan = new MouseMovementPlan(
			new Point(0, 0),
			MouseTarget.point(new Point(6, 8)),
			new Point(6, 8),
			MouseActionContext.GENERAL,
			MouseEngineMode.BALANCED,
			42L,
			10.0,
			10.0,
			120,
			1,
			0,
			0,
			MouseSpeed.NORMAL.getBaseTimeMs());

		MouseMovementReport report = MouseMovementReport.fromPath(
			plan,
			Arrays.asList(new Point(0, 0), new Point(3, 4), new Point(6, 8)));

		assertEquals(10.0, report.getPathLength(), 1e-9);
		assertEquals(10.0, report.getDirectDistance(), 1e-9);
		assertEquals(1.0, report.getPathEfficiency(), 1e-9);
		assertEquals(5.0, report.getPeakStepDistance(), 1e-9);
		assertEquals(0.0, report.getFinalError(), 1e-9);
		assertEquals(3, report.getStepCount());
		assertEquals(120, report.getPlannedDurationMs());
		assertEquals(MouseMovementTuning.DEFAULT_REACTION_DELAY_MS, report.getPlannedReactionDelayMs());
		assertEquals(MouseMovementTuning.DEFAULT_SETTLE_DELAY_MS, report.getPlannedSettleDelayMs());
		assertEquals(MouseMovementTuning.DEFAULT_BUTTON_HOLD_MS, report.getPlannedButtonDownTimeMs());
	}

	@Test
	public void reportCalculatesStepVarianceAndJerkProxy()
	{
		MouseMovementPlan plan = new MouseMovementPlan(
			new Point(0, 0),
			MouseTarget.point(new Point(9, 0)),
			new Point(9, 0),
			MouseActionContext.GENERAL,
			MouseEngineMode.BALANCED,
			42L,
			9.0,
			10.0,
			120,
			1,
			0,
			0,
			MouseSpeed.NORMAL.getBaseTimeMs());

		MouseMovementReport report = MouseMovementReport.fromPath(
			plan,
			Arrays.asList(new Point(0, 0), new Point(5, 0), new Point(5, 0), new Point(9, 0)));

		assertTrue(report.getStepDistanceVariance() > 0.0);
		assertTrue(report.getJerkProxy() > 0.0);
	}
}
