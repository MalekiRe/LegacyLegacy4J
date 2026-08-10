package wily.legacy125.client.screen;

import net.minecraft.src.InventoryBasic;
import net.minecraft.src.Slot;
import org.junit.jupiter.api.Test;
import wily.legacy125.input.PadButton;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SlotNavigatorTest {
    @Test
    void navigatesIrregularModContainerGeometry() {
        InventoryBasic inventory = new InventoryBasic("modded", 4);
        List slots = new ArrayList();
        slots.add(new Slot(inventory, 0, 8, 8));
        slots.add(new Slot(inventory, 1, 40, 8));
        slots.add(new Slot(inventory, 2, 8, 50));
        slots.add(new Slot(inventory, 3, 58, 42));

        assertEquals(1, SlotNavigator.neighbor(slots, 0, PadButton.DPAD_RIGHT));
        assertEquals(2, SlotNavigator.neighbor(slots, 0, PadButton.DPAD_DOWN));
        assertEquals(3, SlotNavigator.neighbor(slots, 1, PadButton.DPAD_DOWN));
        assertEquals(0, SlotNavigator.neighbor(slots, 2, PadButton.DPAD_UP));
    }

    @Test
    void ignoresSlotsMovedOffScreenByAnAdapter() {
        InventoryBasic inventory = new InventoryBasic("modded", 2);
        List slots = new ArrayList();
        slots.add(new Slot(inventory, 0, -1000, -1000));
        slots.add(new Slot(inventory, 1, 12, 12));

        assertEquals(1, SlotNavigator.firstVisible(slots));
    }
}
