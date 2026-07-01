package net.runelite.client.plugins.cupidbot.util.mouse.engine;

import net.runelite.api.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MouseMovementReport
{
	private static final MouseMovementReport EMPTY = new MouseMovementReport(
		null, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0, 0, 0);

	private final MouseMovementPlan plan;
	private final double pathLength;
	private final double directDistance;
	private final double pathEfficiency;
	private final double peakStepDistance;
	private final double finalError;
	private final double stepDistanceVariance;
	private final double jerkProxy;
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
		double stepDistanceVariance,
		double jerkProxy,
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
		this.stepDistanceVariance = stepDistanceVariance;
		this.jerkProxy = jerkProxy;
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
		List<Double> steps = new ArrayList<>();
		for (int i = 1; i < path.size(); i++)
		{
			double step = distance(path.get(i - 1), path.get(i));
			pathLength += step;
			peakStep = Math.max(peakStep, step);
			steps.add(step);
		}

		double direct = plan == null ? 0.0 : plan.getDistance();
		double efficiency = direct <= 0.0 ? 1.0 : pathLength / direct;
		double finalError = 0.0;
		if (plan != null && !path.isEmpty())
		{
			finalError = distance(path.get(path.size() - 1), plan.getTargetPoint());
		}
		double variance = stepDistanceVariance(steps, pathLength);
		double jerkProxy = jerkProxy(steps);
		return new MouseMovementReport(
			plan,
			pathLength,
			direct,
			efficiency,
			peakStep,
			finalError,
			variance,
			jerkProxy,
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

	private static double stepDistanceVariance(List<Double> steps, double pathLength)
	{
		if (steps.isEmpty())
		{
			return 0.0;
		}
		double average = pathLength / steps.size();
		double total = 0.0;
		for (double step : steps)
		{
			double delta = step - average;
			total += delta * delta;
		}
		return total / steps.size();
	}

	private static double jerkProxy(List<Double> steps)
	{
		if (steps.size() < 2)
		{
			return 0.0;
		}
		double total = 0.0;
		for (int i = 1; i < steps.size(); i++)
		{
			total += Math.abs(steps.get(i) - steps.get(i - 1));
		}
		return total / (steps.size() - 1);
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

	public double getStepDistanceVariance()
	{
		return stepDistanceVariance;
	}

	public double getJerkProxy()
	{
		return jerkProxy;
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
