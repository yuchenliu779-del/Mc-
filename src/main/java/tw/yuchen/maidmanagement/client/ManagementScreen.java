package tw.yuchen.maidmanagement.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import tw.yuchen.maidmanagement.menu.ManagementMenu;

public final class ManagementScreen extends AbstractContainerScreen<ManagementMenu> {
    private static final String[] JOBS = {
            "screen.maidmanagement.lumberjack",
            "screen.maidmanagement.miner",
            "screen.maidmanagement.farmer",
            "screen.maidmanagement.fighter"
    };
    private int selected = 0;

    public ManagementScreen(ManagementMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 248;
        this.imageHeight = 192;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        int x = leftPos + 15;
        int y = topPos + 44;
        for (int i = 0; i < JOBS.length; i++) {
            final int index = i;
            addRenderableWidget(Button.builder(Component.translatable(JOBS[i]), button -> selected = index)
                    .bounds(x + (i % 2) * 111, y + (i / 2) * 29, 103, 23).build());
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF27283A);
        graphics.fill(leftPos + 5, topPos + 5, leftPos + imageWidth - 5, topPos + 32, 0xFF444B69);
        graphics.fill(leftPos + 8, topPos + 111, leftPos + imageWidth - 8, topPos + 181, 0xFF353B50);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 14, 14, 0xFFFFFFFF, false);
        graphics.drawString(font, Component.translatable("screen.maidmanagement.selected"), 15, 112, 0xFFFFFFFF, false);
        graphics.drawString(font, Component.translatable(JOBS[selected]), 15, 128, 0xFFFFDD88, false);
        graphics.drawString(font, Component.translatable("screen.maidmanagement.nearby", menu.nearbyMaidCount()),
                15, 146, 0xFFAAFFAA, false);
        graphics.drawString(font, Component.translatable("screen.maidmanagement.scan_only"),
                15, 166, 0xFFCCCCCC, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
