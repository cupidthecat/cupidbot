package net.runelite.client.plugins.cupidbot.util.mouse.engine;

public enum MouseActionContext
{
	GENERAL(1.00, true, 1.00, 1.00, 1.00, 1.00, 0.55),
	MENU(0.92, true, 0.85, 0.82, 0.72, 0.62, 0.72),
	INVENTORY(0.96, true, 0.82, 0.90, 0.78, 0.68, 0.68),
	WORLD_OBJECT(1.05, true, 1.00, 1.10, 1.00, 0.90, 0.55),
	ACTOR(1.02, true, 1.00, 1.00, 0.95, 0.85, 0.55),
	DRAG(1.16, false, 0.65, 0.50, 0.35, 0.25, 0.72),
	SCROLL(0.82, false, 0.75, 0.55, 0.45, 0.35, 0.70),
	QA_REPLAY(1.00, false, 0.00, 0.00, 0.00, 0.00, 1.00);

	private final double durationMultiplier;
	private final boolean overshootAllowed;
	private final double endpointErrorMultiplier;
	private final double curveMultiplier;
	private final double pathNoiseMultiplier;
	private final double microJitterMultiplier;
	private final double targetCenterBias;

	MouseActionContext(
		double durationMultiplier,
		boolean overshootAllowed,
		double endpointErrorMultiplier,
		double curveMultiplier,
		double pathNoiseMultiplier,
		double microJitterMultiplier,
		double targetCenterBias)
	{
		this.durationMultiplier = durationMultiplier;
		this.overshootAllowed = overshootAllowed;
		this.endpointErrorMultiplier = endpointErrorMultiplier;
		this.curveMultiplier = curveMultiplier;
		this.pathNoiseMultiplier = pathNoiseMultiplier;
		this.microJitterMultiplier = microJitterMultiplier;
		this.targetCenterBias = targetCenterBias;
	}

	public double getDurationMultiplier()
	{
		return durationMultiplier;
	}

	public boolean isOvershootAllowed()
	{
		return overshootAllowed;
	}

	public double getEndpointErrorMultiplier()
	{
		return endpointErrorMultiplier;
	}

	public double getCurveMultiplier()
	{
		return curveMultiplier;
	}

	public double getPathNoiseMultiplier()
	{
		return pathNoiseMultiplier;
	}

	public double getMicroJitterMultiplier()
	{
		return microJitterMultiplier;
	}

	public double getTargetCenterBias()
	{
		return targetCenterBias;
	}
}
