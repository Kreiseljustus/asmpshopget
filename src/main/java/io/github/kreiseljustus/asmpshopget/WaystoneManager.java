package io.github.kreiseljustus.asmpshopget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedList;

public class WaystoneManager {

    public static LinkedList<WaystoneDataHolder> s_CachedWaystones = new LinkedList<>();
    private static LinkedList<String> checkedInventories = new LinkedList<>();

    public static void addWaystone(WaystoneDataHolder waystone) {
        if(!s_CachedWaystones.contains(waystone)) {
            s_CachedWaystones.add(waystone);
        }
    }

    public static boolean isWaystoneUI(ScreenHandler handler) {
        int waystoneGuiSlots = 6*9;
        if(handler.slots.size() < waystoneGuiSlots + 36 ) return false; //36 is player inv size
        Utils.debug("The screen has the correct size");

        ItemStack head = handler.getSlot(5).getStack();
        if(!(head.getItem() == Items.PLAYER_HEAD)) return false;

        Utils.debug("The screen has a head in the right position");

        if(!(handler.getSlot(49).getStack().getItem() == Items.SPYGLASS)) return false;
        Utils.debug("The screen has a spyglass in the right slot");

        return true;
    }

    public static String getWaystoneOwner(ScreenHandler handler) {
        ItemStack head = handler.getSlot(5).getStack();
        return head.getCustomName() == null ? "Unknown" : head.getCustomName().getString();
    }

    public static void waystoneTick(MinecraftClient client) {

        if(!Asmpshopget.s_Config.trackWaystones) return;

        if(client.currentScreen instanceof HandledScreen<?> screen) {

            Utils.debug("Theres a screen open");

            if(checkedInventories.contains(screen.getTitle().toString())) return;

            ScreenHandler handler = screen.getScreenHandler();

            if(isWaystoneUI(handler)) {
                Utils.debug("The screen is a waystone");
                String owner = getWaystoneOwner(handler);
                String name = screen.getTitle().getString();

                BlockHitResult hit = (client.crosshairTarget instanceof BlockHitResult br) ? br : null;
                BlockPos pos = (hit != null) ? hit.getBlockPos() : BlockPos.ORIGIN;

                Utils.debug("Waystone detected! Owner: " + owner + ", Name: " + name + ", Pos: " + pos);

                int[] position = {pos.getX(), pos.getY(), pos.getZ()};
                addWaystone(new WaystoneDataHolder(owner,name, position));

                checkedInventories.add(screen.getTitle().getString());
            }
        }
    }
}
