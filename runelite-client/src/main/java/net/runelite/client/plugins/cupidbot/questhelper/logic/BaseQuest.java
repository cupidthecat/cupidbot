package net.runelite.client.plugins.cupidbot.questhelper.logic;

import net.runelite.client.plugins.cupidbot.CupidBot;
import net.runelite.client.plugins.cupidbot.questhelper.QuestHelperPlugin;

public abstract class BaseQuest implements IQuest {

    protected QuestHelperPlugin getQuestHelperPlugin() {
        return (QuestHelperPlugin) CupidBot.getPluginManager().getPlugins().stream().filter(x -> x instanceof QuestHelperPlugin).findFirst().orElse(null);
    }
}
