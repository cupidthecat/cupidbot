package net.runelite.client.plugins.cupidbot.util.antiban.enums;

import lombok.Getter;

public enum MouseSmoothness
{
	STANDARD("1", 8.0, 10, 15, 10.0, 2.0),
	SMOOTH("2", 7.0, 14, 18, 11.0, 2.2),
	BALANCED("3", 6.0, 18, 22, 12.0, 2.4),
	HIGH("4", 5.0, 24, 28, 13.0, 2.7),
	MAX("5", 4.5, 32, 34, 14.0, 3.0);

	public static final MouseSmoothness DEFAULT = BALANCED;

	@Getter
	private final String name;
	@Getter
	private final double timeToStepsDivider;
	@Getter
	private final int minSteps;
	@Getter
	private final int effectFadeSteps;
	@Getter
	private final double deviationSlopeDivider;
	@Getter
	private final double noiseDivider;

	MouseSmoothness(
		String name,
		double timeToStepsDivider,
		int minSteps,
		int effectFadeSteps,
		double deviationSlopeDivider,
		double noiseDivider)
	{
		this.name = name;
		this.timeToStepsDivider = timeToStepsDivider;
		this.minSteps = minSteps;
		this.effectFadeSteps = effectFadeSteps;
		this.deviationSlopeDivider = deviationSlopeDivider;
		this.noiseDivider = noiseDivider;
	}

	public int getSliderIndex()
	{
		return ordinal();
	}

	public static MouseSmoothness fromSliderIndex(int index)
	{
		MouseSmoothness[] values = values();
		if (index < 0)
		{
			return values[0];
		}
		if (index >= values.length)
		{
			return values[values.length - 1];
		}
		return values[index];
	}

	public static MouseSmoothness fromConfigValue(String value)
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
}
