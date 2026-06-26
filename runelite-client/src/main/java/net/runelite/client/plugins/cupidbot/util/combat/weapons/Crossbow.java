package net.runelite.client.plugins.cupidbot.util.combat.weapons;

import java.util.List;

public class Crossbow extends Weapon
{
	public Crossbow(int id)
	{
		super(id);
		range = 7;
	}

	public Crossbow(List<Integer> weaponData)
	{
		super(weaponData);
	}
}
