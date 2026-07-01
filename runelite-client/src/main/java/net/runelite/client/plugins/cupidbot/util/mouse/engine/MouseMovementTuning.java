package net.runelite.client.plugins.cupidbot.util.mouse.engine;

import net.runelite.client.plugins.cupidbot.util.antiban.Rs2AntibanSettings;

import java.util.Random;

public final class MouseMovementTuning
{
	public static final int MIN_TIMING_MS = 0;
	public static final int MAX_TIMING_MS = 500;
	public static final int DEFAULT_REACTION_DELAY_MS = 25;
	public static final int DEFAULT_REACTION_DELAY_MIN_MS = 10;
	public static final int DEFAULT_REACTION_DELAY_MAX_MS = 45;
	public static final int DEFAULT_SETTLE_DELAY_MS = 35;
	public static final int DEFAULT_SETTLE_DELAY_MIN_MS = 20;
	public static final int DEFAULT_SETTLE_DELAY_MAX_MS = 70;
	public static final int DEFAULT_BUTTON_HOLD_MS = 55;
	public static final int DEFAULT_BUTTON_HOLD_MIN_MS = 35;
	public static final int DEFAULT_BUTTON_HOLD_MAX_MS = 90;
	public static final int DEFAULT_PERCENT = 100;
	public static final int DEFAULT_ENDPOINT_ERROR_PERCENT = 100;
	public static final int DEFAULT_DRAG_STABILITY_PERCENT = 100;
	public static final int DEFAULT_SCROLL_BURST_PERCENT = 100;

	private final int reactionDelayMs;
	private final boolean reactionDelayRandom;
	private final int reactionDelayMinMs;
	private final int reactionDelayMaxMs;
	private final int settleDelayMs;
	private final boolean settleDelayRandom;
	private final int settleDelayMinMs;
	private final int settleDelayMaxMs;
	private final int buttonDownTimeMs;
	private final boolean buttonDownTimeRandom;
	private final int buttonDownTimeMinMs;
	private final int buttonDownTimeMaxMs;
	private final int curvePercent;
	private final int pathNoisePercent;
	private final int microJitterPercent;
	private final int overshootPercent;
	private final int correctionPercent;
	private final int endpointErrorPercent;
	private final int dragStabilityPercent;
	private final int scrollBurstPercent;

	public MouseMovementTuning(
		int reactionDelayMs,
		int settleDelayMs,
		int buttonDownTimeMs,
		int curvePercent,
		int pathNoisePercent,
		int microJitterPercent,
		int overshootPercent,
		int correctionPercent)
	{
		this(
			reactionDelayMs,
			settleDelayMs,
			buttonDownTimeMs,
			curvePercent,
			pathNoisePercent,
			microJitterPercent,
			overshootPercent,
			correctionPercent,
			DEFAULT_ENDPOINT_ERROR_PERCENT,
			DEFAULT_DRAG_STABILITY_PERCENT,
			DEFAULT_SCROLL_BURST_PERCENT);
	}

	public MouseMovementTuning(
		int reactionDelayMs,
		int settleDelayMs,
		int buttonDownTimeMs,
		int curvePercent,
		int pathNoisePercent,
		int microJitterPercent,
		int overshootPercent,
		int correctionPercent,
		int endpointErrorPercent,
		int dragStabilityPercent,
		int scrollBurstPercent)
	{
		this(
			reactionDelayMs,
			false,
			reactionDelayMs,
			reactionDelayMs,
			settleDelayMs,
			false,
			settleDelayMs,
			settleDelayMs,
			buttonDownTimeMs,
			false,
			buttonDownTimeMs,
			buttonDownTimeMs,
			curvePercent,
			pathNoisePercent,
			microJitterPercent,
			overshootPercent,
			correctionPercent,
			endpointErrorPercent,
			dragStabilityPercent,
			scrollBurstPercent);
	}

	public MouseMovementTuning(
		int reactionDelayMs,
		boolean reactionDelayRandom,
		int reactionDelayMinMs,
		int reactionDelayMaxMs,
		int settleDelayMs,
		boolean settleDelayRandom,
		int settleDelayMinMs,
		int settleDelayMaxMs,
		int buttonDownTimeMs,
		boolean buttonDownTimeRandom,
		int buttonDownTimeMinMs,
		int buttonDownTimeMaxMs,
		int curvePercent,
		int pathNoisePercent,
		int microJitterPercent,
		int overshootPercent,
		int correctionPercent)
	{
		this(
			reactionDelayMs,
			reactionDelayRandom,
			reactionDelayMinMs,
			reactionDelayMaxMs,
			settleDelayMs,
			settleDelayRandom,
			settleDelayMinMs,
			settleDelayMaxMs,
			buttonDownTimeMs,
			buttonDownTimeRandom,
			buttonDownTimeMinMs,
			buttonDownTimeMaxMs,
			curvePercent,
			pathNoisePercent,
			microJitterPercent,
			overshootPercent,
			correctionPercent,
			DEFAULT_ENDPOINT_ERROR_PERCENT,
			DEFAULT_DRAG_STABILITY_PERCENT,
			DEFAULT_SCROLL_BURST_PERCENT);
	}

	public MouseMovementTuning(
		int reactionDelayMs,
		boolean reactionDelayRandom,
		int reactionDelayMinMs,
		int reactionDelayMaxMs,
		int settleDelayMs,
		boolean settleDelayRandom,
		int settleDelayMinMs,
		int settleDelayMaxMs,
		int buttonDownTimeMs,
		boolean buttonDownTimeRandom,
		int buttonDownTimeMinMs,
		int buttonDownTimeMaxMs,
		int curvePercent,
		int pathNoisePercent,
		int microJitterPercent,
		int overshootPercent,
		int correctionPercent,
		int endpointErrorPercent,
		int dragStabilityPercent,
		int scrollBurstPercent)
	{
		this.reactionDelayMs = clampTimingMs(reactionDelayMs);
		this.reactionDelayRandom = reactionDelayRandom;
		this.reactionDelayMinMs = normalizeTimingMinMs(reactionDelayMinMs, reactionDelayMaxMs);
		this.reactionDelayMaxMs = normalizeTimingMaxMs(reactionDelayMinMs, reactionDelayMaxMs);
		this.settleDelayMs = clampTimingMs(settleDelayMs);
		this.settleDelayRandom = settleDelayRandom;
		this.settleDelayMinMs = normalizeTimingMinMs(settleDelayMinMs, settleDelayMaxMs);
		this.settleDelayMaxMs = normalizeTimingMaxMs(settleDelayMinMs, settleDelayMaxMs);
		this.buttonDownTimeMs = clampTimingMs(buttonDownTimeMs);
		this.buttonDownTimeRandom = buttonDownTimeRandom;
		this.buttonDownTimeMinMs = normalizeTimingMinMs(buttonDownTimeMinMs, buttonDownTimeMaxMs);
		this.buttonDownTimeMaxMs = normalizeTimingMaxMs(buttonDownTimeMinMs, buttonDownTimeMaxMs);
		this.curvePercent = clampPercent(curvePercent);
		this.pathNoisePercent = clampPercent(pathNoisePercent);
		this.microJitterPercent = clampPercent(microJitterPercent);
		this.overshootPercent = clampPercent(overshootPercent);
		this.correctionPercent = clampPercent(correctionPercent);
		this.endpointErrorPercent = clampPercent(endpointErrorPercent);
		this.dragStabilityPercent = clampPercent(dragStabilityPercent);
		this.scrollBurstPercent = clampPercent(scrollBurstPercent);
	}

	public static MouseMovementTuning defaults()
	{
		return new MouseMovementTuning(
			DEFAULT_REACTION_DELAY_MS,
			DEFAULT_SETTLE_DELAY_MS,
			DEFAULT_BUTTON_HOLD_MS,
			DEFAULT_PERCENT,
			DEFAULT_PERCENT,
			DEFAULT_PERCENT,
			DEFAULT_PERCENT,
			DEFAULT_PERCENT,
			DEFAULT_ENDPOINT_ERROR_PERCENT,
			DEFAULT_DRAG_STABILITY_PERCENT,
			DEFAULT_SCROLL_BURST_PERCENT);
	}

	public static MouseMovementTuning fromSettings()
	{
		return new MouseMovementTuning(
			Rs2AntibanSettings.mouseReactionDelayMs,
			Rs2AntibanSettings.mouseReactionDelayRandom,
			Rs2AntibanSettings.mouseReactionDelayMinMs,
			Rs2AntibanSettings.mouseReactionDelayMaxMs,
			Rs2AntibanSettings.mouseSettleDelayMs,
			Rs2AntibanSettings.mouseSettleDelayRandom,
			Rs2AntibanSettings.mouseSettleDelayMinMs,
			Rs2AntibanSettings.mouseSettleDelayMaxMs,
			Rs2AntibanSettings.mouseButtonHoldMs,
			Rs2AntibanSettings.mouseButtonHoldRandom,
			Rs2AntibanSettings.mouseButtonHoldMinMs,
			Rs2AntibanSettings.mouseButtonHoldMaxMs,
			Rs2AntibanSettings.mouseCurveScale,
			Rs2AntibanSettings.mousePathNoiseScale,
			Rs2AntibanSettings.mouseMicroJitterScale,
			Rs2AntibanSettings.mouseOvershootScale,
			Rs2AntibanSettings.mouseCorrectionScale,
			Rs2AntibanSettings.mouseEndpointErrorScale,
			Rs2AntibanSettings.mouseDragStabilityScale,
			Rs2AntibanSettings.mouseScrollBurstScale);
	}

	public MouseMovementTuning withoutReplayDelays()
	{
		return new MouseMovementTuning(
			0,
			0,
			0,
			curvePercent,
			pathNoisePercent,
			microJitterPercent,
			0,
			0,
			0,
			dragStabilityPercent,
			scrollBurstPercent);
	}

	public MouseMovementTuning sampleTimings(Random random)
	{
		Random safeRandom = random == null ? new Random() : random;
		return new MouseMovementTuning(
			sampleTimingMs(safeRandom, reactionDelayRandom, reactionDelayMs, reactionDelayMinMs, reactionDelayMaxMs),
			sampleTimingMs(safeRandom, settleDelayRandom, settleDelayMs, settleDelayMinMs, settleDelayMaxMs),
			sampleTimingMs(safeRandom, buttonDownTimeRandom, buttonDownTimeMs, buttonDownTimeMinMs, buttonDownTimeMaxMs),
			curvePercent,
			pathNoisePercent,
			microJitterPercent,
			overshootPercent,
			correctionPercent,
			endpointErrorPercent,
			dragStabilityPercent,
			scrollBurstPercent);
	}

	public int getReactionDelayMs()
	{
		return reactionDelayMs;
	}

	public boolean isReactionDelayRandom()
	{
		return reactionDelayRandom;
	}

	public int getReactionDelayMinMs()
	{
		return reactionDelayMinMs;
	}

	public int getReactionDelayMaxMs()
	{
		return reactionDelayMaxMs;
	}

	public int getSettleDelayMs()
	{
		return settleDelayMs;
	}

	public boolean isSettleDelayRandom()
	{
		return settleDelayRandom;
	}

	public int getSettleDelayMinMs()
	{
		return settleDelayMinMs;
	}

	public int getSettleDelayMaxMs()
	{
		return settleDelayMaxMs;
	}

	public int getButtonDownTimeMs()
	{
		return buttonDownTimeMs;
	}

	public boolean isButtonDownTimeRandom()
	{
		return buttonDownTimeRandom;
	}

	public int getButtonDownTimeMinMs()
	{
		return buttonDownTimeMinMs;
	}

	public int getButtonDownTimeMaxMs()
	{
		return buttonDownTimeMaxMs;
	}

	public int getCurvePercent()
	{
		return curvePercent;
	}

	public int getPathNoisePercent()
	{
		return pathNoisePercent;
	}

	public int getMicroJitterPercent()
	{
		return microJitterPercent;
	}

	public int getOvershootPercent()
	{
		return overshootPercent;
	}

	public int getCorrectionPercent()
	{
		return correctionPercent;
	}

	public int getEndpointErrorPercent()
	{
		return endpointErrorPercent;
	}

	public int getDragStabilityPercent()
	{
		return dragStabilityPercent;
	}

	public int getScrollBurstPercent()
	{
		return scrollBurstPercent;
	}

	public double getCurveMultiplier()
	{
		return toMultiplier(curvePercent);
	}

	public double getPathNoiseMultiplier()
	{
		return toMultiplier(pathNoisePercent);
	}

	public double getMicroJitterMultiplier()
	{
		return toMultiplier(microJitterPercent);
	}

	public double getOvershootMultiplier()
	{
		return toMultiplier(overshootPercent);
	}

	public double getCorrectionMultiplier()
	{
		return toMultiplier(correctionPercent);
	}

	public double getEndpointErrorMultiplier()
	{
		return toMultiplier(endpointErrorPercent);
	}

	public double getDragStabilityMultiplier()
	{
		return toMultiplier(dragStabilityPercent);
	}

	public double getScrollBurstMultiplier()
	{
		return toMultiplier(scrollBurstPercent);
	}

	public static int clampTimingMs(int value)
	{
		return clamp(value, MIN_TIMING_MS, MAX_TIMING_MS);
	}

	public static int normalizeTimingMinMs(int minValue, int maxValue)
	{
		return Math.min(clampTimingMs(minValue), clampTimingMs(maxValue));
	}

	public static int normalizeTimingMaxMs(int minValue, int maxValue)
	{
		return Math.max(clampTimingMs(minValue), clampTimingMs(maxValue));
	}

	public static int clampPercent(int value)
	{
		return clamp(value, 0, 200);
	}

	private static double toMultiplier(int percent)
	{
		return percent / 100.0;
	}

	private static int sampleTimingMs(Random random, boolean randomEnabled, int staticValue, int minValue, int maxValue)
	{
		if (!randomEnabled)
		{
			return staticValue;
		}

		int min = normalizeTimingMinMs(minValue, maxValue);
		int max = normalizeTimingMaxMs(minValue, maxValue);
		if (min == max)
		{
			return min;
		}
		return min + random.nextInt(max - min + 1);
	}

	private static int clamp(int value, int min, int max)
	{
		return Math.max(min, Math.min(max, value));
	}
}
