package net.runelite.client.plugins.cupidbot.util.dialogues;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Rs2DialogueTest {
    @Test
    public void isContinuePromptTextAcceptsContinuePrompt() {
        assertTrue(Rs2Dialogue.isContinuePromptText("<col=ffffff>Click here to continue</col>"));
    }

    @Test
    public void isContinuePromptTextRejectsChatboxInputs() {
        assertFalse(Rs2Dialogue.isContinuePromptText("Enter amount:"));
        assertFalse(Rs2Dialogue.isContinuePromptText("Search"));
        assertFalse(Rs2Dialogue.isContinuePromptText("abyssal whip"));
    }

    @Test
    public void dialogueTextMatchesIgnoresFormattingTags() {
        assertTrue(Rs2Dialogue.dialogueTextMatches("<col=ff0000>[1] Yes please.</col>", "Yes please.", false));
        assertTrue(Rs2Dialogue.dialogueTextMatches("<col=ff0000>Yes please.</col>", "Yes please.", true));
    }

    @Test
    public void dialogueTextMatchesRejectsDifferentOption() {
        assertFalse(Rs2Dialogue.dialogueTextMatches("No thanks.", "Yes please.", false));
    }
}
