package io.github.kreiseljustus.asmputils.core.modules.commands.browser;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BrowserScreen extends HandledScreen<BrowserScreenHandler> {
    public BrowserScreen(BrowserScreenHandler handler, PlayerInventory inventory) {
        super(handler, inventory, Text.literal("Shop Browser"));
        this.backgroundHeight = 222;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context,mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context,mouseX,mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        Identifier texture = Identifier.ofVanilla("textures/gui/container/generic_54.png");
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight - 96, 256, 256);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y + this.backgroundHeight - 96, 0, 126, this.backgroundWidth, 96, 256, 256);
    }
}
