package net.runelite.client.plugins.cupidbot.util.mouse.engine;

import net.runelite.api.Point;

import java.util.Collections;
import java.util.List;

public final class MouseMovementReport
{
	private static final MouseMovementReport EMPTY = new MouseMovementReport(
		null, 0.0, 0.0, 1.0, 0.0, 0.0, 0, 0, 0, 0, 0);

	private final MouseMovementPlan plan;
	private final double pathLength;
	private final double directDistance;
	private final double pathEfficiency;
	private final double peakStepDistance;
	private final double finalError;
	private final int stepCount;
	private final int plannedDurationMs;
	private final int plannedReactionDelayMs;
	private final int plannedSettleDelayMs;
	private final int plannedButtonDownTimeMs;

	private MouseMovementReport(
		MouseMovementPlan plan,
		double pathLength,
		double directDistance,
		double pathEfficiency,
		double peakStepDistance,
		double finalError,
		int stepCount,
		int plannedDurationMs,
		int plannedReactionDelayMs,
		int plannedSettleDelayMs,
		int plannedButtonDownTimeMs)
	{
		this.plan = plan;
		this.pathLength = pathLength;
		this.directDistance = directDistance;
		this.pathEfficiency = pathEfficiency;
		this.peakStepDistance = peakStepDistance;
		this.finalError = finalError;
		this.stepCount = stepCount;
		this.plannedDurationMs = plannedDurationMs;
		this.plannedReactionDelayMs = plannedReactionDelayMs;
		this.plannedSettleDelayMs = plannedSettleDelayMs;
		this.plannedButtonDownTimeMs = plannedButtonDownTimeMs;
	}

	public static MouseMovementReport empty()
	{
		return EMPTY;
	}

	public static MouseMovementReport fromPath(MouseMovementPlan plan, List<Point> observedPath)
	{
		List<Point> path = observedPath == null ? Collections.emptyList() : observedPath;
		double pathLength = 0.0;
		double peakStep = 0.0;
		for (int i = 1; i < path.size(); i++)
		{
			double step = distance(path.get(i - 1), path.get(i));
			pathLength += step;
			peakStep = Math.max(peakStep, step);
		}

		double direct = plan == null ? 0.0 : plan.getDistance();
		double efficiency = direct <= 0.0 ? 1.0 : pathLength / direct;
		double finalError = 0.0;
		if (plan != null && !path.isEmpty())
		{
			finalError = distance(path.get(path.size() - 1), plan.getTargetPoint());
		}
		return new MouseMovementReport(
			plan,
			pathLength,
			direct,
			efficiency,
			peakStep,
			finalError,
			path.size(),
			plan == null ? 0 : plan.getDurationMs(),
			plan == null ? 0 : plan.getReactionDelayMs(),
			plan == null ? 0 : plan.getSettleDelayMs(),
			plan == null ? 0 : plan.getButtonDownTimeMs());
	}

	private static double distance(Point a, Point b)
	{
		return Math.hypot(a.getX() - b.getX(), a.getY() - b.getY());
	}

	public MouseMovementPlan getPlan()
	{
		return plan;
	}

	public double getPathLength()
	{
		return pathLength;
	}

	public double getDirectDistance()
	{
		return directDistance;
	}

	public double getPathEfficiency()
	{
		return pathEfficiency;
	}

	public double getPeakStepDistance()
	{
		return peakStepDistance;
	}

	public double getFinalError()
	{
		return finalError;
	}

	public int getStepCount()
	{
		return stepCount;
	}

	public int getPlannedDurationMs()
	{
		return plannedDurationMs;
	}

	public int getPlannedReactionDelayMs()
	{
		return plannedReactionDelayMs;
	}

	public int getPlannedSettleDelayMs()
	{
		return plannedSettleDelayMs;
	}

	public int getPlannedButtonDownTimeMs()
	{
		return plannedButtonDownTimeMs;
	}
}
