package net.runelite.client.plugins.cupidbot.util.mouse.engine;

import net.runelite.api.Point;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseEngineMode;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSmoothness;
import net.runelite.client.plugins.cupidbot.util.antiban.enums.MouseSpeed;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MouseMovementPlanner
{
	private static final double LOG_2 = Math.log(2.0);

	public MouseMovementPlan plan(
		Point startPoint,
		MouseTarget target,
		MouseActionContext context,
		MouseEngineMode mode,
		MouseSpeed speed,
		MouseSmoothness smoothness,
		Long seed)
	{
		return plan(startPoint, target, context, mode, speed, smoothness, seed, MouseMovementTuning.fromSettings());
	}

	public MouseMovementPlan plan(
		Point startPoint,
		MouseTarget target,
		MouseActionContext context,
		MouseEngineMode mode,
		MouseSpeed speed,
		MouseSmoothness smoothness,
		Long seed,
		MouseMovementTuning tuning)
	{
		Point start = startPoint == null ? new Point(1, 1) : startPoint;
		MouseTarget safeTarget = target == null ? MouseTarget.point(start) : target;
		MouseActionContext safeContext = context == null ? MouseActionContext.GENERAL : context;
		MouseEngineMode safeMode = mode == null ? MouseEngineMode.DEFAULT : mode;
		MouseSpeed safeSpeed = speed == null ? MouseSpeed.DEFAULT : speed;
		MouseSmoothness safeSmoothness = smoothness == null ? MouseSmoothness.DEFAULT : smoothness;
		MouseMovementTuning safeTuning = tuning == null ? MouseMovementTuning.defaults() : tuning;
		long effectiveSeed = seed == null ? ThreadLocalRandom.current().nextLong() : seed;
		Random random = new Random(effectiveSeed);

		double distance = Math.hypot(
			safeTarget.getCenter().getX() - start.getX(),
			safeTarget.getCenter().getY() - start.getY());
		double targetWidth = safeTarget.getEffectiveWidth();
		int endpointErrorRadius = endpointErrorRadius(targetWidth, safeMode);
		boolean replay = safeMode == MouseEngineMode.QA_REPLAY || safeContext == MouseActionContext.QA_REPLAY;
		Point targetPoint = safeTarget.samplePoint(random, endpointErrorRadius, replay);
		if (replay)
		{
			safeTuning = safeTuning.withoutReplayDelays();
		}
		else
		{
			safeTuning = safeTuning.sampleTimings(random);
		}
		double targetPointDistance = Math.hypot(targetPoint.getX() - start.getX(), targetPoint.getY() - start.getY());
		int durationMs = durationMs(targetPointDistance, targetWidth, safeContext, safeMode, safeSpeed, safeSmoothness);
		int overshoots = overshootCount(targetPointDistance, targetWidth, safeContext, safeMode, safeSpeed, safeTuning);
		int corrections = correctionCount(targetPointDistance, targetWidth, overshoots, safeContext, safeMode, safeTuning);
		int factoryBaseTimeMs = Math.max(30, durationMs / 2);

		return new MouseMovementPlan(
			start,
			safeTarget,
			targetPoint,
			safeContext,
			safeMode,
			effectiveSeed,
			targetPointDistance,
			targetWidth,
			durationMs,
			replay ? 0 : endpointErrorRadius,
			overshoots,
			corrections,
			factoryBaseTimeMs,
			safeTuning);
	}

	private int durationMs(
		double distance,
		double targetWidth,
		MouseActionContext context,
		MouseEngineMode mode,
		MouseSpeed speed,
		MouseSmoothness smoothness)
	{
		double difficulty = Math.log(distance / Math.max(1.0, targetWidth) + 1.0) / LOG_2;
		double smoothnessCost = 1.0 + smoothness.getSliderIndex() * 0.025;
		double duration = speed.getBaseTimeMs()
			+ difficulty * speed.getBaseTimeMs() * 0.48
			+ distance * 0.08;
		duration *= context.getDurationMultiplier() * mode.getDurationMultiplier() * smoothnessCost;
		int max = Math.max(speed.getFatigueMaxBaseTimeMs() * 3, speed.getBaseTimeMs() + 100);
		return clamp((int) Math.round(duration), 45, max);
	}

	private int endpointErrorRadius(double targetWidth, MouseEngineMode mode)
	{
		if (mode == MouseEngineMode.QA_REPLAY)
		{
			return 0;
		}
		return clamp((int) Math.floor(targetWidth * 0.18 * mode.getEndpointErrorMultiplier()), 0, 14);
	}

	private int overshootCount(
		double distance,
		double targetWidth,
		MouseActionContext context,
		MouseEngineMode mode,
		MouseSpeed speed,
		MouseMovementTuning tuning)
	{
		if (!context.isOvershootAllowed()
			|| !mode.isOvershootAllowed()
			|| tuning.getOvershootPercent() <= 0
			|| distance < 80.0)
		{
			return 0;
		}
		double difficulty = Math.log(distance / Math.max(1.0, targetWidth) + 1.0) / LOG_2;
		if (difficulty < 3.0)
		{
			return 0;
		}
		int max = mode == MouseEngineMode.PRECISE ? 1 : speed.getOvershoots();
		int overshoots = (int) Math.ceil(difficulty / 3.5);
		overshoots = (int) Math.round(overshoots * tuning.getOvershootMultiplier());
		return clamp(overshoots, 0, max);
	}

	private int correctionCount(
		double distance,
		double targetWidth,
		int overshoots,
		MouseActionContext context,
		MouseEngineMode mode,
		MouseMovementTuning tuning)
	{
		if (mode == MouseEngineMode.QA_REPLAY
			|| context == MouseActionContext.QA_REPLAY
			|| tuning.getCorrectionPercent() <= 0)
		{
			return 0;
		}
		int corrections = overshoots;
		if (targetWidth <= 8.0 && distance > 50.0)
		{
			corrections++;
		}
		if (mode == MouseEngineMode.PRECISE && distance > 30.0)
		{
			corrections++;
		}
		corrections = (int) Math.round(corrections * tuning.getCorrectionMultiplier());
		return clamp(corrections, 0, 4);
	}

	private static int clamp(int value, int min, int max)
	{
		return Math.max(min, Math.min(max, value));
	}
}
