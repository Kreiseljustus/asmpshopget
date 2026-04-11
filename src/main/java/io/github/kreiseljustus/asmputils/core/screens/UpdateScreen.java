package io.github.kreiseljustus.asmputils.core.screens;

import io.github.kreiseljustus.asmputils.config.Constants;
import io.github.kreiseljustus.asmputils.core.ModrinthVersionManagement;
import io.github.kreiseljustus.asmputils.core.Utils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

public class UpdateScreen extends Screen {
    private final Screen parent;

    public UpdateScreen(Screen parent) {
        super(Text.literal("Update Available"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fillGradient(0, 0, width, height, 0xFF4F4F51, 0xFF6E7481);
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Update Now"), button -> {
            downloadAndInstall();
        }).dimensions(width / 2 - 102, height / 2 + 10, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Later"), button -> {
            client.setScreen(parent);
        }).dimensions(width / 2 + 2, height / 2 + 10, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("ASMP Utils " + ModrinthVersionManagement.latestVersion + " is available!")
                        .formatted(Formatting.YELLOW),
                width / 2, height / 2 - 60, 0xFFFFFFFF);

        String[] lines = cleanChangelog(ModrinthVersionManagement.changeLog);
        int lineY = height / 2 - 45;
        for (String line : lines) {
            if (line.isBlank()) { lineY += 4; continue; }
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(line), width / 2, lineY, 0xFFAAAAAA);
            lineY += 10;
        }

        context.drawTextWithShadow(textRenderer,
                Text.literal("You are running " + Constants.VERSION).formatted(Formatting.GRAY),
                1, height - 10, 0xFFFFFFFF);
    }

    private String[] cleanChangelog(String raw) {
        if (raw == null) return new String[]{"No changelog available."};
        return raw
                .replace("<br>", "")
                .replace("# ", "")
                .replace("## ", "")
                .replaceAll("\\[([^]]+)]\\([^)]+\\)", "$1")
                .replaceAll("\\*\\*([^*]+)\\*\\*", "$1")
                .replaceAll("\\*([^*]+)\\*", "$1")
                .replaceAll("`([^`]+)`", "$1")
                .lines()
                .map(String::trim)
                .toArray(String[]::new);
    }

    private void downloadAndInstall() {
        Utils.debug("Downloading and installing update...");
        clearChildren();
        addDrawableChild(ButtonWidget.builder(Text.literal("Downloading..."), b -> {})
                .dimensions(width / 2 - 50, height / 2 + 10, 100, 20).build());

        CompletableFuture.runAsync(() -> {
            try {
                Path modsFolder = FabricLoader.getInstance().getGameDir().resolve("mods");
                Path newJar = modsFolder.resolve(ModrinthVersionManagement.downloadFilename + ".tmp");
                Path finalJar = modsFolder.resolve(ModrinthVersionManagement.downloadFilename);

                Path oldJar = Path.of(FabricLoader.getInstance()
                        .getModContainer("asmputils").get()
                        .getOrigin()
                        .getPaths().get(0)
                        .toUri());

                try (InputStream in = new URI(ModrinthVersionManagement.downloadUrl).toURL().openStream()) {
                    Files.copy(in, newJar, StandardCopyOption.REPLACE_EXISTING);
                }
                Utils.debug("Downloaded new jar to: " + newJar);

                Path script = modsFolder.resolve("asmputils_update.ps1");
                String ps =
                        "param()\n" +
                                "Set-StrictMode -Version Latest\n" +
                                "$ErrorActionPreference = 'Stop'\n" +
                                "\n" +
                                "$logFile = '" + modsFolder.toAbsolutePath().toString().replace("'", "''") + "\\asmputils_update - delete me if you want.log'\n" +
                                "function Log($msg) { \"$(Get-Date -Format 'HH:mm:ss') $msg\" | Out-File $logFile -Append }\n" +
                                "\n" +
                                "Log 'Script started'\n" +
                                "\n" +
                                "$gamepid = " + ProcessHandle.current().pid() + "\n" +
                                "$old = '" + oldJar.toAbsolutePath().toString().replace("'", "''") + "'\n" +
                                "$tmp = '" + newJar.toAbsolutePath().toString().replace("'", "''") + "'\n" +
                                "$new = '" + finalJar.toAbsolutePath().toString().replace("'", "''") + "'\n" +
                                "$scriptPath = $MyInvocation.MyCommand.Path\n" +
                                "\n" +
                                "Log \"PID: $gamepid\"\n" +
                                "Log \"Old: $old\"\n" +
                                "Log \"Tmp: $tmp\"\n" +
                                "Log \"New: $new\"\n" +
                                "\n" +
                                "$modsFolder = Split-Path $old -Parent\n" +
                                "if ((Split-Path $tmp -Parent) -ne $modsFolder) { Log 'FAIL: tmp not in mods folder'; exit 1 }\n" +
                                "if ((Split-Path $new -Parent) -ne $modsFolder) { Log 'FAIL: new not in mods folder'; exit 1 }\n" +
                                "if ((Split-Path $scriptPath -Parent) -ne $modsFolder) { Log 'FAIL: script not in mods folder'; exit 1 }\n" +
                                "\n" +
                                "if (-not $old.EndsWith('.jar')) { Log 'FAIL: old does not end with .jar'; exit 1 }\n" +
                                "if (-not $tmp.EndsWith('.tmp')) { Log 'FAIL: tmp does not end with .tmp'; exit 1 }\n" +
                                "if (-not $new.EndsWith('.jar')) { Log 'FAIL: new does not end with .jar'; exit 1 }\n" +
                                "\n" +
                                "Log 'Waiting for game process to exit...'\n" +
                                "$waited = 0\n" +
                                "while ((Get-Process -Id $gamepid -ErrorAction SilentlyContinue) -and $waited -lt 300) {\n" +
                                "    Start-Sleep 1\n" +
                                "    $waited++\n" +
                                "}\n" +
                                "if ($waited -ge 300) { Log 'FAIL: timed out waiting for game'; exit 1 }\n" +
                                "Log \"Game exited after $waited seconds\"\n" +
                                "\n" +
                                "if (-not (Test-Path $tmp)) { Log 'FAIL: tmp file not found'; exit 1 }\n" +
                                "if ((Get-Item $tmp).Length -eq 0) { Log 'FAIL: tmp file is empty'; Remove-Item $tmp -Force; exit 1 }\n" +
                                "Log \"Tmp file size: $((Get-Item $tmp).Length) bytes\"\n" +
                                "\n" +
                                "Log 'Deleting old jar...'\n" +
                                "Remove-Item $old -Force -ErrorAction SilentlyContinue\n" +
                                "Log 'Moving tmp to new jar...'\n" +
                                "Move-Item $tmp $new -Force\n" +
                                "Log 'Update complete!'\n" +
                                "\n" +
                                "Remove-Item $scriptPath -Force -ErrorAction SilentlyContinue\n";

                Files.writeString(script, ps);

                new ProcessBuilder(
                        "powershell.exe",
                        "-NonInteractive",
                        "-WindowStyle", "Hidden",
                        "-ExecutionPolicy", "Bypass",
                        "-File", script.toAbsolutePath().toString()
                ).start();

                Utils.debug("Update script launched");

                client.execute(() -> client.setScreen(new ConfirmScreen(
                        confirmed -> { if (confirmed) client.scheduleStop(); else client.setScreen(parent); },
                        Text.literal("Update downloaded!").formatted(Formatting.GREEN),
                        Text.literal("Restart now to apply the update?")
                )));

            } catch (Exception e) {
                e.printStackTrace();
                client.execute(() -> client.setScreen(new ConfirmScreen(
                        confirmed -> client.setScreen(parent),
                        Text.literal("Download failed!").formatted(Formatting.RED),
                        Text.literal("Please update manually on Modrinth.")
                )));
            }
        });
    }
}