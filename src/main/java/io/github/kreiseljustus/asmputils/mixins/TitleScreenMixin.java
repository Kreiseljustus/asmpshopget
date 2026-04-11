package io.github.kreiseljustus.asmputils.mixins;

import io.github.kreiseljustus.asmputils.Asmputils;
import io.github.kreiseljustus.asmputils.core.ModrinthVersionManagement;
import io.github.kreiseljustus.asmputils.core.Utils;
import io.github.kreiseljustus.asmputils.core.screens.UpdateScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Unique
    private static boolean shownUpdateScreen = false;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if(ModrinthVersionManagement.updateAvailable && !shownUpdateScreen && Asmputils.s_Config.showUpdateScreen) {
            Utils.debug("Update available, showing update screen!");
            shownUpdateScreen = true;
            MinecraftClient client = MinecraftClient.getInstance();
            client.send(() -> client.setScreen(new UpdateScreen((Screen) (Object) this)));
        }
    }
}
