package net.runelite.client.plugins.cupidbot.util.events;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.cupidbot.BlockingEvent;
import net.runelite.client.plugins.cupidbot.BlockingEventPriority;
import net.runelite.client.plugins.cupidbot.CupidBot;
import net.runelite.client.plugins.cupidbot.CupidBotConfig;
import net.runelite.client.plugins.cupidbot.util.settings.Rs2Settings;

public class HideRoofsEvent implements BlockingEvent
{
	@Override
	public boolean validate()
	{
		return isConfigEnabled() && CupidBot.isLoggedIn() && !Rs2Settings.isHideRoofsEnabled();
	}

	@Override
	public boolean execute()
	{
		if (!isConfigEnabled())
		{
			return true;
		}
		return Rs2Settings.hideRoofs();
	}

	private boolean isConfigEnabled()
	{
		ConfigManager configManager = CupidBot.getConfigManager();
		if (configManager == null)
		{
			return true;
		}

		CupidBotConfig config = configManager.getConfig(CupidBotConfig.class);
		return config == null || config.hideRoofs();
	}

	@Override
	public BlockingEventPriority priority()
	{
		return BlockingEventPriority.HIGH;
	}
}
