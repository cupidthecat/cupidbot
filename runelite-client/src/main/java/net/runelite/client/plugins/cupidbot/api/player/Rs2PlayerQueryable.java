package net.runelite.client.plugins.cupidbot.api.player;

import net.runelite.client.plugins.cupidbot.CupidBot;
import net.runelite.client.plugins.cupidbot.api.AbstractEntityQueryable;
import net.runelite.client.plugins.cupidbot.api.IEntityQueryable;
import net.runelite.client.plugins.cupidbot.api.player.models.Rs2PlayerModel;

import java.util.stream.Stream;

public final class Rs2PlayerQueryable extends AbstractEntityQueryable<Rs2PlayerQueryable, Rs2PlayerModel>
        implements IEntityQueryable<Rs2PlayerQueryable, Rs2PlayerModel> {

    public Rs2PlayerQueryable() {
        super();
    }

    @Override
    protected Stream<Rs2PlayerModel> initialSource() {
        return CupidBot.getRs2PlayerCache().getStream();
    }
}
