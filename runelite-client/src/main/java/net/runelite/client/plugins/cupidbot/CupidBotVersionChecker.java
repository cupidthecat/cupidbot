package net.runelite.client.plugins.cupidbot;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class CupidBotVersionChecker
{
	@Inject
	public CupidBotVersionChecker()
	{
	}

	public void checkForUpdate()
	{
		log.debug("CupidBot client update checks are disabled; launcher uses local jars.");
	}

	public void shutdown()
	{
	}
}
