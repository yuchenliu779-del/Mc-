package tw.yuchen.maidmanagement.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import tw.yuchen.maidmanagement.MaidManagement;

public final class ManagementMenu extends AbstractContainerMenu {
    private static final int SEARCH_RADIUS = 16;
    private final BlockPos position;
    private final Player player;
    private final ContainerData scanData;

    public ManagementMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, data.readBlockPos());
    }

    public ManagementMenu(int containerId, Inventory inventory, BlockPos position) {
        super(MaidManagement.MANAGEMENT_MENU.get(), containerId);
        this.position = position;
        this.player = inventory.player;
        this.scanData = new SimpleContainerData(1);
        addDataSlots(scanData);
        refreshNearbyCount();
    }

    private void refreshNearbyCount() {
        if (player.level().isClientSide) return;
        AABB area = new AABB(position).inflate(SEARCH_RADIUS);
        int count = 0;
        for (Entity entity : player.level().getEntities(player, area, e -> e.isAlive())) {
            ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
            if (key != null && key.getNamespace().equals("touhou_little_maid")
                    && key.getPath().equals("maid")) {
                count++;
            }
        }
        scanData.set(0, Math.min(count, 32767));
    }

    public int nearbyMaidCount() {
        return scanData.get(0);
    }

    @Override
    public void broadcastChanges() {
        refreshNearbyCount();
        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().isLoaded(position)
                && player.level().getBlockState(position).is(MaidManagement.MANAGEMENT_CENTER.get())
                && player.distanceToSqr(position.getX() + 0.5D, position.getY() + 0.5D,
                        position.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }
}
