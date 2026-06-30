package net.runelite.client.plugins.cupidbot.util.antiban.enums;

import lombok.Getter;

public enum MouseEngineMode
{
	BALANCED("Balanced", 1.00, 1.00, true),
	PRECISE("Precise", 1.15, 0.55, true),
	RELAXED("Relaxed", 1.25, 1.35, true),
	QA_REPLAY("QA Replay", 1.00, 0.00, false);

	public static final MouseEngineMode DEFAULT = BALANCED;

	@Getter
	private final String name;
	@Getter
	private final double durationMultiplier;
	@Getter
	private final double endpointErrorMultiplier;
	@Getter
	private final boolean overshootAllowed;

	MouseEngineMode(
		String name,
		double durationMultiplier,
		double endpointErrorMultiplier,
		boolean overshootAllowed)
	{
		this.name = name;
		this.durationMultiplier = durationMultiplier;
		this.endpointErrorMultiplier = endpointErrorMultiplier;
		this.overshootAllowed = overshootAllowed;
	}

	public static MouseEngineMode fromConfigValue(String value)
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

	@Override
	public String toString()
	{
		return name;
	}
}
