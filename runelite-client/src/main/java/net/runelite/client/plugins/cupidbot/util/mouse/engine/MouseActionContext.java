package net.runelite.client.plugins.cupidbot.util.mouse.engine;

public enum MouseActionContext
{
	GENERAL(1.00, true),
	MENU(0.92, true),
	INVENTORY(0.96, true),
	WORLD_OBJECT(1.05, true),
	ACTOR(1.02, true),
	DRAG(1.16, false),
	SCROLL(0.82, false),
	QA_REPLAY(1.00, false);

	private final double durationMultiplier;
	private final boolean overshootAllowed;

	MouseActionContext(double durationMultiplier, boolean overshootAllowed)
	{
		this.durationMultiplier = durationMultiplier;
		this.overshootAllowed = overshootAllowed;
	}

	public double getDurationMultiplier()
	{
		return durationMultiplier;
	}

	public boolean isOvershootAllowed()
	{
		return overshootAllowed;
	}
}
