package io.github.kreiseljustus.asmputils.core;

import com.google.gson.*;
import io.github.kreiseljustus.asmputils.Asmputils;
import io.github.kreiseljustus.asmputils.config.Constants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static io.github.kreiseljustus.asmputils.Asmputils.*;


public class Utils {
    public static Gson s_Gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                    (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                    (json, typeOfT, context) -> {
                        String str = json.getAsString();
                        if (str.endsWith("Z")) {
                            return OffsetDateTime.parse(str).toLocalDateTime();
                        }
                        return LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS][.SSSSSS][.SSS]"));
                    })
            .create();

    /**
     * Returns true if currently playing on the ASMP Server
     */
    public static boolean onASMP() {
        MinecraftClient client = MinecraftClient.getInstance();

        ServerInfo server = client.getCurrentServerEntry();
        if(server == null) return false;
        return server.address.equals("asmp.cc");
    }

    /**
     * Prints to console and also tries to send the player a message
     * if debug mode is enabled in the mod options
     */
    public static void debug(String message, boolean verbose) {
        System.out.println("[ASMP Utils " + Constants.VERSION + "]: " + message);
        if(!Asmputils.s_Config.enableDebugMode || !Asmputils.s_Config.enable || Asmputils.s_Player == null) return;
        if(verbose && !s_Config.verboseLogging) {
            s_Player.sendMessage(Text.of("[ASMP Utils] Verbose message (see log)"), false);
            return;
        }
        Asmputils.s_Player.sendMessage(Text.of("[ASMP Utils] " + message), false);
    }

    public static void debug(String message) {
        debug(message, false);
    }

    /**
     * Uses reflection to check if a module is turned on
     * in the mod options <br>
     * Naming scheme in config should be "enable" + moduleName
     */
    public static boolean getModuleOn(String moduleName) {
        try {
            String fieldName = "enable" + moduleName;

            var field = Asmputils.s_Config.getClass().getDeclaredField(fieldName);

            return field.getBoolean(Asmputils.s_Config);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param dim 0->overworld, 1->the_nether, 2->the_end
     */
    public static String dimensionFromInt(int dim) {
        return switch (dim) {
            case 1 -> "the_nether";
            case 2 -> "the_end";
            default -> "overworld";
        };
    }

    /**
     * Creates a Keybinding with the category being asmputils
     * @param translationKey translation key without key.asmputils.
     * @param keycode GLFW key code
     */
    public static KeyBinding registerKeyBind(String translationKey, int keycode) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding("key.asmputils." + translationKey, keycode, "category.asmputils"));
    }

    public static void openLinkWithConfirm(String link) {
        tickDelay.schedule(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> {
                client.setScreen(new ConfirmLinkScreen(confirmed -> {
                    if (confirmed) {
                        Util.getOperatingSystem().open(link);
                    } else {
                        Utils.debug("User cancelled");
                    }
                    client.setScreen(null);
                }, link, true));
            });
        }, 50, TimeUnit.MILLISECONDS);
    }
}
