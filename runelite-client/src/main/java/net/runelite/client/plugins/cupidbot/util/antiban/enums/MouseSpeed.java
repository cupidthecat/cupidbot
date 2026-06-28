package net.runelite.client.plugins.cupidbot.util.antiban.enums;

import lombok.Getter;

public enum MouseSpeed
{
	VERY_SLOW("1", 400, 500, 110, 4, 250),
	SLOW("2", 325, 425, 105, 4, 225),
	RELAXED("3", 260, 360, 105, 4, 200),
	STEADY("4", 210, 300, 100, 4, 180),
	NORMAL("5", 170, 240, 100, 4, 160),
	BRISK("6", 145, 210, 95, 3, 145),
	FAST("7", 125, 180, 95, 3, 130),
	VERY_FAST("8", 105, 150, 90, 3, 115),
	EXTREME("9", 90, 120, 90, 2, 100);

	public static final MouseSpeed DEFAULT = NORMAL;

	@Getter
	private final String name;
	@Getter
	private final int baseTimeMs;
	@Getter
	private final int fatigueMaxBaseTimeMs;
	@Getter
	private final int reactionTimeVariationMs;
	@Getter
	private final int overshoots;
	@Getter
	private final int minOvershootMovementMs;

	MouseSpeed(
		String name,
		int baseTimeMs,
		int fatigueMaxBaseTimeMs,
		int reactionTimeVariationMs,
		int overshoots,
		int minOvershootMovementMs)
	{
		this.name = name;
		this.baseTimeMs = baseTimeMs;
		this.fatigueMaxBaseTimeMs = fatigueMaxBaseTimeMs;
		this.reactionTimeVariationMs = reactionTimeVariationMs;
		this.overshoots = overshoots;
		this.minOvershootMovementMs = minOvershootMovementMs;
	}

	public int getSliderIndex()
	{
		return ordinal();
	}

	public static MouseSpeed fromSliderIndex(int index)
	{
		MouseSpeed[] speeds = values();
		if (index < 0)
		{
			return speeds[0];
		}
		if (index >= speeds.length)
		{
			return speeds[speeds.length - 1];
		}
		return speeds[index];
	}

	public static MouseSpeed fromConfigValue(String value)
	{
		if (value == null || value.isBlank())
		{
			return DEFAULT;
		}

		try
		{
			return valueOf(value.trim());
		}
		catch (IllegalArgumentException ex)
		{
			return DEFAULT;
		}
	}

	public static MouseSpeed fromActivityIntensity(ActivityIntensity intensity)
	{
		if (intensity == null)
		{
			return DEFAULT;
		}

		switch (intensity)
		{
			case VERY_LOW:
				return VERY_SLOW;
			case LOW:
				return SLOW;
			case MODERATE:
				return NORMAL;
			case HIGH:
				return FAST;
			case EXTREME:
				return EXTREME;
			default:
				return DEFAULT;
		}
	}

	public ActivityIntensity toActivityIntensity()
	{
		switch (this)
		{
			case VERY_SLOW:
				return ActivityIntensity.VERY_LOW;
			case SLOW:
			case RELAXED:
				return ActivityIntensity.LOW;
			case STEADY:
			case NORMAL:
			case BRISK:
				return ActivityIntensity.MODERATE;
			case FAST:
			case VERY_FAST:
				return ActivityIntensity.HIGH;
			case EXTREME:
			default:
				return ActivityIntensity.EXTREME;
		}
	}
}
