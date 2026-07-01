package net.runelite.client.plugins.cupidbot.util.mouse.engine;

import net.runelite.api.Point;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseEngineMode;

public final class MouseMovementPlan
{
	private final Point startPoint;
	private final MouseTarget target;
	private final Point targetPoint;
	private final MouseActionContext context;
	private final MouseEngineMode mode;
	private final long seed;
	private final double distance;
	private final double targetWidth;
	private final int durationMs;
	private final int endpointErrorRadius;
	private final int overshootCount;
	private final int correctionCount;
	private final int factoryBaseTimeMs;
	private final MouseMovementTuning tuning;
	private final double difficultyIndex;
	private final MouseTrajectoryStyle trajectoryStyle;
	private final double effectiveCurveMultiplier;
	private final double effectivePathNoiseMultiplier;
	private final double effectiveMicroJitterMultiplier;

	public MouseMovementPlan(
		Point startPoint,
		MouseTarget target,
		Point targetPoint,
		MouseActionContext context,
		MouseEngineMode mode,
		long seed,
		double distance,
		double targetWidth,
		int durationMs,
		int endpointErrorRadius,
		int overshootCount,
		int correctionCount,
		int factoryBaseTimeMs)
	{
		this(
			startPoint,
			target,
			targetPoint,
			context,
			mode,
			seed,
			distance,
			targetWidth,
			durationMs,
			endpointErrorRadius,
			overshootCount,
			correctionCount,
			factoryBaseTimeMs,
			MouseMovementTuning.defaults());
	}

	public MouseMovementPlan(
		Point startPoint,
		MouseTarget target,
		Point targetPoint,
		MouseActionContext context,
		MouseEngineMode mode,
		long seed,
		double distance,
		double targetWidth,
		int durationMs,
		int endpointErrorRadius,
		int overshootCount,
		int correctionCount,
		int factoryBaseTimeMs,
		MouseMovementTuning tuning)
	{
		this(
			startPoint,
			target,
			targetPoint,
			context,
			mode,
			seed,
			distance,
			targetWidth,
			durationMs,
			endpointErrorRadius,
			overshootCount,
			correctionCount,
			factoryBaseTimeMs,
			tuning,
			difficultyIndex(distance, targetWidth),
			MouseTrajectoryStyle.BALANCED,
			(tuning == null ? MouseMovementTuning.defaults() : tuning).getCurveMultiplier(),
			(tuning == null ? MouseMovementTuning.defaults() : tuning).getPathNoiseMultiplier(),
			(tuning == null ? MouseMovementTuning.defaults() : tuning).getMicroJitterMultiplier());
	}

	public MouseMovementPlan(
		Point startPoint,
		MouseTarget target,
		Point targetPoint,
		MouseActionContext context,
		MouseEngineMode mode,
		long seed,
		double distance,
		double targetWidth,
		int durationMs,
		int endpointErrorRadius,
		int overshootCount,
		int correctionCount,
		int factoryBaseTimeMs,
		MouseMovementTuning tuning,
		double difficultyIndex,
		MouseTrajectoryStyle trajectoryStyle,
		double effectiveCurveMultiplier,
		double effectivePathNoiseMultiplier,
		double effectiveMicroJitterMultiplier)
	{
		this.startPoint = startPoint;
		this.target = target;
		this.targetPoint = targetPoint;
		this.context = context;
		this.mode = mode;
		this.seed = seed;
		this.distance = distance;
		this.targetWidth = targetWidth;
		this.durationMs = durationMs;
		this.endpointErrorRadius = endpointErrorRadius;
		this.overshootCount = overshootCount;
		this.correctionCount = correctionCount;
		this.factoryBaseTimeMs = factoryBaseTimeMs;
		this.tuning = tuning == null ? MouseMovementTuning.defaults() : tuning;
		this.difficultyIndex = difficultyIndex;
		this.trajectoryStyle = trajectoryStyle == null ? MouseTrajectoryStyle.BALANCED : trajectoryStyle;
		this.effectiveCurveMultiplier = effectiveCurveMultiplier;
		this.effectivePathNoiseMultiplier = effectivePathNoiseMultiplier;
		this.effectiveMicroJitterMultiplier = effectiveMicroJitterMultiplier;
	}

	public Point getStartPoint()
	{
		return startPoint;
	}

	public MouseTarget getTarget()
	{
		return target;
	}

	public Point getTargetPoint()
	{
		return targetPoint;
	}

	public MouseActionContext getContext()
	{
		return context;
	}

	public MouseEngineMode getMode()
	{
		return mode;
	}

	public long getSeed()
	{
		return seed;
	}

	public double getDistance()
	{
		return distance;
	}

	public double getTargetWidth()
	{
		return targetWidth;
	}

	public int getDurationMs()
	{
		return durationMs;
	}

	public int getEndpointErrorRadius()
	{
		return endpointErrorRadius;
	}

	public int getOvershootCount()
	{
		return overshootCount;
	}

	public int getCorrectionCount()
	{
		return correctionCount;
	}

	public int getFactoryBaseTimeMs()
	{
		return factoryBaseTimeMs;
	}

	public MouseMovementTuning getTuning()
	{
		return tuning;
	}

	public double getDifficultyIndex()
	{
		return difficultyIndex;
	}

	public MouseTrajectoryStyle getTrajectoryStyle()
	{
		return trajectoryStyle;
	}

	public double getEffectiveCurveMultiplier()
	{
		return effectiveCurveMultiplier;
	}

	public double getEffectivePathNoiseMultiplier()
	{
		return effectivePathNoiseMultiplier;
	}

	public double getEffectiveMicroJitterMultiplier()
	{
		return effectiveMicroJitterMultiplier;
	}

	public int getReactionDelayMs()
	{
		return tuning.getReactionDelayMs();
	}

	public int getSettleDelayMs()
	{
		return tuning.getSettleDelayMs();
	}

	public int getButtonDownTimeMs()
	{
		return tuning.getButtonDownTimeMs();
	}

	public int getCurvePercent()
	{
		return tuning.getCurvePercent();
	}

	public int getPathNoisePercent()
	{
		return tuning.getPathNoisePercent();
	}

	public int getMicroJitterPercent()
	{
		return tuning.getMicroJitterPercent();
	}

	public int getOvershootPercent()
	{
		return tuning.getOvershootPercent();
	}

	public int getCorrectionPercent()
	{
		return tuning.getCorrectionPercent();
	}

	public int getEndpointErrorPercent()
	{
		return tuning.getEndpointErrorPercent();
	}

	public int getDragStabilityPercent()
	{
		return tuning.getDragStabilityPercent();
	}

	public int getScrollBurstPercent()
	{
		return tuning.getScrollBurstPercent();
	}

	private static double difficultyIndex(double distance, double targetWidth)
	{
		return Math.log(distance / Math.max(1.0, targetWidth) + 1.0) / Math.log(2.0);
	}
}
