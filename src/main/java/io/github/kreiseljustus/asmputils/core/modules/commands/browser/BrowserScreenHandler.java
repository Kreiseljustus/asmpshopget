package io.github.kreiseljustus.asmputils.core.modules.commands.browser;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BrowserScreenHandler extends GenericContainerScreenHandler {

    private final Consumer<Integer> onSlotClick;
    private final Inventory inv;

    public BrowserScreenHandler(int syncId, PlayerInventory playerInventory,
                                Inventory inventory, Consumer<Integer> onSlotClick) {
        super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, inventory, 6);
        this.inv = inventory;
        this.onSlotClick = onSlotClick;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex >= 0 && slotIndex < inv.size()) {
            onSlotClick.accept(slotIndex);
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) { return true; }
}