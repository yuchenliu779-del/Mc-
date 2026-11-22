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

    public ManagementScreen(ManagementMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 248;
        imageHeight = 250;
        inventoryLabelY = 1000;
    }

    private void clickServerButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < JOBS.length; i++) {
            final int job = i;
            addRenderableWidget(Button.builder(Component.translatable(JOBS[i]), button -> clickServerButton(job))
                    .bounds(leftPos + 15 + (i % 2) * 111, topPos + 44 + (i / 2) * 29, 103, 23).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("screen.maidmanagement.register"),
                button -> clickServerButton(4))
                .bounds(leftPos + 15, topPos + 189, 218, 22).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.maidmanagement.unregister"),
                button -> clickServerButton(5))
                .bounds(leftPos + 15, topPos + 216, 218, 22).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF27283A);
        graphics.fill(leftPos + 5, topPos + 5, leftPos + imageWidth - 5, topPos + 32, 0xFF444B69);
        graphics.fill(leftPos + 8, topPos + 111, leftPos + imageWidth - 8, topPos + 173, 0xFF353B50);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 14, 14, 0xFFFFFFFF, false);
        graphics.drawString(font, Component.translatable("screen.maidmanagement.selected"), 15, 112, 0xFFFFFFFF, false);
        graphics.drawString(font, Component.translatable(JOBS[Math.max(0, Math.min(3, menu.selectedJob()))]),
                15, 127, 0xFFFFDD88, false);
        graphics.drawString(font, Component.translatable("screen.maidmanagement.nearby", menu.nearbyMaidCount()),
                15, 145, 0xFFAAFFAA, false);
        graphics.drawString(font, Component.translatable("screen.maidmanagement.registered", menu.registeredCount()),
                15, 159, 0xFFFFFFFF, false);
        graphics.drawString(font, Component.translatable("screen.maidmanagement.job_count",
                menu.jobCount(menu.selectedJob())), 15, 174, 0xFFFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
