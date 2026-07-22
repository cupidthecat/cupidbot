package net.runelite.client.plugins.cupidbot.questhelper.logic;

import net.runelite.api.Quest;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.cupidbot.questhelper.steps.QuestStep;
import net.runelite.client.plugins.cupidbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.cupidbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.cupidbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.cupidbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.cupidbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.cupidbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.cupidbot.util.player.Rs2Player;
import net.runelite.client.plugins.cupidbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.cupidbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.cupidbot.util.widget.Rs2Widget;

import java.util.List;

import static net.runelite.api.gameval.ItemID.BANANA;
import static net.runelite.api.gameval.ItemID.SPADE;
import static net.runelite.client.plugins.cupidbot.util.Global.sleep;
import static net.runelite.client.plugins.cupidbot.util.Global.sleepUntil;

/**
 * Pirate's Treasure quest custom logic
 */
public class PiratesTreasure extends BaseQuest {
    private static final String QUEST_NAME = "Pirate's Treasure";
    private static final String SYNC_STEP_TEXT = "Please open Pirate Treasure's Quest Journal to sync the current quest state.";

    public PiratesTreasure() {
    }

    @Override
    public boolean executeCustomLogic() {
        QuestStep questStep = getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep();
        if (questStep == null || questStep.getText() == null) return true;
        if (getQuestHelperPlugin().getSelectedQuest().getQuest().getId() == Quest.PIRATES_TREASURE.getId()) {
            if (questStep.getText().contains(SYNC_STEP_TEXT)) {
                return syncQuestJournal();
            }
            if (questStep.getText().contains("Right-click fill the rest of the crate with bananas, then talk to Luthas.")) {
                Rs2Walker.walkTo(2917, 3161, 0);
                sleep(2000);
                collectBananas();

                return true;
            }
            if (questStep.getText().contains("Talk to Luthas and tell him you finished filling the crate.")) {
                Rs2Walker.walkTo(2942, 3150, 0);
                if (!sleepUntil(() -> Rs2Npc.getNpc("Luthas") != null, 5000)) return false;
                if (!Rs2Npc.interact("Luthas", "Talk-to")) return false;
                return sleepUntil(Rs2Dialogue::isInDialogue, 5000);
            }

            if (questStep.getText().contains("Search the crate in the back room of the Port Sarim food shop. Make sure you're wearing your white apron.")) {
                if (Rs2Dialogue.isInDialogue()) {
                    Rs2Dialogue.clickContinue();
                }
                return true;
            }

            if (questStep.getText().contains("Dig in the middle of the cross in Falador Park, and kill the Gardener (level 4) who appears. Once killed, dig again.")) {
                if (!Rs2Inventory.contains(SPADE)) {
                    System.out.println("here2");
                    Rs2Walker.clearWalkingRoute("quest:pirates-treasure:detour-for-spade");
                    sleep(1200);
                    Rs2Walker.walkTo(2982, 3369, 0);
                    sleep(1200);
                    Rs2GroundItem.loot(SPADE);
                    sleep(1200);
                }

                //to give time to kill the npc (you can actually just run away and run back to dig and it works)
                if (Rs2Player.isInCombat()) {
                    sleepUntil(() -> !Rs2Player.isInCombat(), 30000);
                }
                return true;
            }

        }
        return true;
    }

    public static void collectBananas() {
        if (Rs2Inventory.count(BANANA) >= 10) {
            return;
        }

        pickBananasAt(new int[][]{
                {2917, 3161, 0},
                {2920, 3168, 0},
                {2909, 3163, 0}
        });

        if (Rs2Inventory.count(BANANA) >= 10) {
            Rs2Walker.walkTo(2942, 3150, 0);
            sleep(2000);
            Rs2GameObject.interact(2072, "Fill", 10);
            sleep(2000);
        }
    }

    private static void pickBananasAt(int[][] locations) {
        for (int[] location : locations) {
            Rs2Walker.walkTo(location[0], location[1], location[2]);
            sleep(2000);
            pickBananaTree();
        }
    }

    private static void pickBananaTree() {
        for (int i = 0; i < 4; i++) { // 4 picks total
            Rs2GameObject.interact("Banana Tree", "Pick");
            sleep(800, 1500); // Sleep after each click
        }
    }


    private boolean syncQuestJournal() {
        if (!Rs2Tab.switchToQuestTab()) return false;
        if (!sleepUntil(() -> Rs2Widget.isWidgetVisible(InterfaceID.Questlist.UNIVERSE), 3000)) return false;

        if (Rs2Widget.hasWidget("Search quest list")) {
            if (!openSearchWidget()) return false;
            if (!sleepUntil(() -> !Rs2Widget.hasWidget("Search quest list"), 1500)) return false;
        }
        if (!openSearchWidget()) return false;
        if (!sleepUntil(() -> Rs2Widget.hasWidget("Search quest list"), 1500)) return false;

        Rs2Keyboard.typeString("Pirate");
        if (!sleepUntil(() -> Rs2Widget.hasVisibleWidgetText(QUEST_NAME), 3000)) return false;
        if (!Rs2Widget.clickWidget(QUEST_NAME, true)) return false;
        if (!sleepUntil(() -> Rs2Widget.hasWidgetText(QUEST_NAME, InterfaceID.Questjournal.TITLE, true), 3000)) {
            return false;
        }

        boolean stateSynced = sleepUntil(() -> {
            QuestStep activeStep = getQuestHelperPlugin().getSelectedQuest().getCurrentStep().getActiveStep();
            return activeStep != null && !activeStep.getText().contains(SYNC_STEP_TEXT);
        }, 3000);
        Rs2Widget.clickWidget(InterfaceID.Questjournal.CLOSE);
        if (Rs2Widget.hasWidget("Search quest list")) {
            openSearchWidget();
            sleepUntil(() -> !Rs2Widget.hasWidget("Search quest list"), 1500);
        }
        return stateSynced;
    }

    private boolean openSearchWidget() {
        Widget parentWidget = Rs2Widget.getWidget(InterfaceID.Questlist.UNIVERSE);
        if (parentWidget == null) return false;
        Widget searchWidget = Rs2Widget.findWidget("Search", List.of(parentWidget), true);
        return searchWidget != null && Rs2Widget.clickWidget(searchWidget);
    }
}
