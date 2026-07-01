package net.runelite.client.plugins.cupidbot.util.mouse.engine;

public enum MouseTrajectoryStyle
{
	DIRECT(0.70, 0.55, 0.45),
	SMOOTH(0.78, 0.65, 0.55),
	BALANCED(1.00, 1.00, 1.00),
	CORRECTIVE(1.15, 1.10, 0.85),
	DRAG_STABLE(0.45, 0.30, 0.22),
	SCROLL_SMOOTH(0.55, 0.45, 0.35),
	QA_REPLAY(0.00, 0.00, 0.00);

	private final double curveMultiplier;
	private final double pathNoiseMultiplier;
	private final double microJitterMultiplier;

	MouseTrajectoryStyle(
		double curveMultiplier,
		double pathNoiseMultiplier,
		double microJitterMultiplier)
	{
		this.curveMultiplier = curveMultiplier;
		this.pathNoiseMultiplier = pathNoiseMultiplier;
		this.microJitterMultiplier = microJitterMultiplier;
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
}
