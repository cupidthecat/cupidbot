package net.runelite.client.plugins.cupidbot.util.inventory;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Rs2InventorySelectionTest {
    @Test
    public void inventoryItemSelectionRequiresAnItemWidget() {
        assertTrue(Rs2Inventory.isInventoryItemSelection(true, 995));
        assertFalse(Rs2Inventory.isInventoryItemSelection(false, 995));
    }

    @Test
    public void inventoryItemSelectionRejectsSelectedSpellWidget() {
        assertFalse(Rs2Inventory.isInventoryItemSelection(true, -1));
    }
}
