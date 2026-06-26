package net.runelite.client.plugins.cupidbot;

import com.google.common.base.Strings;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.cupidbot.util.player.Rs2Player;
import net.runelite.client.plugins.cupidbot.util.tile.Rs2Tile;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.awt.*;
import java.util.Map;

public class CupidBotOverlay extends OverlayPanel {
    CupidBotPlugin plugin;
    CupidBotConfig config;

    @Inject
    CupidBotOverlay(CupidBotPlugin plugin, CupidBotConfig config) {
        super(plugin);
        setPosition(OverlayPosition.TOP_RIGHT);
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.setPreferredSize(new Dimension(200, 300));

        for (Map.Entry<WorldPoint, Integer> dangerousTile : Rs2Tile.getDangerousGraphicsObjectTiles().entrySet()) {
            drawTile(graphics, dangerousTile.getKey(), Color.RED, dangerousTile.getValue().toString());
        }

        return super.render(graphics);
    }

    private void drawTile(Graphics2D graphics, WorldPoint point, Color color, @Nullable String label) {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();

        if (point.distanceTo(playerLocation) >= 32) {
            return;
        }

        LocalPoint lp;
        if (CupidBot.getClient().getTopLevelWorldView().getScene().isInstance()) {
            WorldPoint worldPoint = WorldPoint.toLocalInstance(CupidBot.getClient().getTopLevelWorldView().getScene(), point).stream().findFirst().get();
            lp = LocalPoint.fromWorld(CupidBot.getClient().getTopLevelWorldView(), worldPoint);
        } else {
            lp = LocalPoint.fromWorld(CupidBot.getClient().getTopLevelWorldView(), point);
        }

        if (lp == null) {
            return;
        }


        Polygon poly = Perspective.getCanvasTilePoly(CupidBot.getClient(), lp);
        if (poly != null) {
            OverlayUtil.renderPolygon(graphics, poly, color, new Color(0, 0, 0, 50), new BasicStroke(2f));
        }

        if (!Strings.isNullOrEmpty(label)) {
            Point canvasTextLocation = Perspective.getCanvasTextLocation(CupidBot.getClient(), graphics, lp, label, 0);
            if (canvasTextLocation != null) {
                OverlayUtil.renderTextLocation(graphics, canvasTextLocation, label, color);
            }
        }
    }
}

