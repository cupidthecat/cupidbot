package net.runelite.client.plugins.cupidbot.questhelper;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class QuestScriptTest {
    @Test
    public void conditionalDialogueChoiceRequiresExpectedPreviousLine() {
        assertTrue(QuestScript.matchesExpectedPreviousLine("What is the capital of Misthalin?", "capital of Misthalin"));
        assertFalse(QuestScript.matchesExpectedPreviousLine("What is two plus two?", "capital of Misthalin"));
        assertTrue(QuestScript.matchesExpectedPreviousLine("anything", null));
    }

    @Test
    public void actionSelectionUsesExplicitRightClickActionFromStepText() {
        String action = QuestScript.chooseActionFromStepText(
                List.of("Right-click fill the rest of the crate with bananas."),
                new String[]{"Search", "Fill", null}, null);

        assertEquals("Fill", action);
    }

    @Test
    public void actionSelectionDoesNotGuessBetweenMultipleActions() {
        assertNull(QuestScript.chooseActionFromStepText(
                List.of("Interact with the strange mechanism."),
                new String[]{"Search", "Open"}, null));
    }

    @Test
    public void actionSelectionAllowsOnlyUnambiguousFallback() {
        assertEquals("Read", QuestScript.chooseActionFromStepText(
                List.of("Inspect the note."), new String[]{null, "Read"}, null));
    }

    @Test
    public void itemCombinationDetectionRecognizesUseOnAndMixSteps() {
        assertTrue(QuestScript.isItemCombinationStep(List.of("Use the feather on the enchanted scroll.")));
        assertTrue(QuestScript.isItemCombinationStep(List.of("Mix the potion with the dust.")));
        assertFalse(QuestScript.isItemCombinationStep(List.of("Read the enchanted scroll.")));
    }
}
