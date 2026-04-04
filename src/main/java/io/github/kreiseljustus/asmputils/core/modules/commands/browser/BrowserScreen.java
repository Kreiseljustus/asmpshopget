package io.github.kreiseljustus.asmputils.core.modules.commands.browser;

import io.github.kreiseljustus.asmputils.core.Utils;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BrowserScreen extends HandledScreen<BrowserScreenHandler> {

    private TextFieldWidget searchField;
    private boolean searchActive = false;
    private Runnable onSearchConfirm;

    public BrowserScreen(BrowserScreenHandler handler, PlayerInventory inventory) {
        super(handler, inventory, Text.literal("Shop Browser"));
        this.backgroundHeight = 222;
        this.playerInventoryTitleY = 129;
        this.titleX = 8;
        this.titleY = 6;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        searchField = new TextFieldWidget(this.textRenderer, x + 8, y + this.backgroundHeight + 4, this.backgroundWidth - 16, 18, Text.literal("Search..."));
        searchField.setMaxLength(64);
        searchField.setVisible(false);
        searchField.setFocused(false);

        this.addSelectableChild(searchField);
    }

    public void activateSearch(String currentSearch, Runnable onConfirm) {
        this.searchActive = true;
        this.onSearchConfirm = onConfirm;
        searchField.setVisible(true);
        searchField.setFocused(true);
        searchField.setText(currentSearch != null ? currentSearch : "");
        this.setFocused(searchField);
    }

    public void deactivateSearch() {
        this.searchActive = false;
        searchField.setVisible(false);
        searchField.setFocused(false);
    }

    public String getSearchText() {
        return searchField.getText();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchActive && searchField.isFocused()) {
            //Enter (+numpad)
            if (keyCode == 257 || keyCode == 335) {
                if (onSearchConfirm != null) onSearchConfirm.run();
                return true;
            }
            //ESC
            if (keyCode == 256) {
                deactivateSearch();
                return true;
            }
            return searchField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchActive && searchField.isFocused()) {
            return searchField.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        if (searchActive) {
            searchField.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        Identifier texture = Identifier.ofVanilla("textures/gui/container/generic_54.png");
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, 0, 0, this.backgroundWidth, 125, 256, 256);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y + 125, 0, 126, this.backgroundWidth, 96, 256, 256);
    }
}