package net.runelite.client.plugins.cupidbot.util.mouse.engine;

import net.runelite.api.Point;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseEngineMode;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSmoothness;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;
import org.junit.Test;

import java.awt.Rectangle;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MouseMovementPlannerTest
{
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

		MouseMovementPlan plan = planner.plan(
			new Point(0, 0),
			target,
			MouseActionContext.QA_REPLAY,
			MouseEngineMode.QA_REPLAY,
			MouseSpeed.NORMAL,
			MouseSmoothness.DEFAULT,
			99L);

		assertEquals(target.getCenter(), plan.getTargetPoint());
		assertEquals(0, plan.getEndpointErrorRadius());
		assertEquals(0, plan.getOvershootCount());
		assertEquals(0, plan.getCorrectionCount());
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
	}
}
