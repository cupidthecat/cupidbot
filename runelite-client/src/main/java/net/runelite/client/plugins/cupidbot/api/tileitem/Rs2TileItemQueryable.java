package net.runelite.client.plugins.cupidbot.api.tileitem;

import net.runelite.client.plugins.cupidbot.CupidBot;
import net.runelite.client.plugins.cupidbot.api.AbstractEntityQueryable;
import net.runelite.client.plugins.cupidbot.api.IEntityQueryable;
import net.runelite.client.plugins.cupidbot.api.tileitem.models.Rs2TileItemModel;

import java.util.stream.Stream;

public final class Rs2TileItemQueryable extends AbstractEntityQueryable<Rs2TileItemQueryable, Rs2TileItemModel>
        implements IEntityQueryable<Rs2TileItemQueryable, Rs2TileItemModel> {

    public Rs2TileItemQueryable() {
        super();
    }

    @Override
    protected Stream<Rs2TileItemModel> initialSource() {
        return CupidBot.getRs2TileItemCache().getStream();
    }
}
