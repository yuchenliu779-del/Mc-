package tw.yuchen.maidmanagement.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import tw.yuchen.maidmanagement.MaidManagement;

public final class ManagementMenu extends AbstractContainerMenu {
    private final BlockPos position;

    public ManagementMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, data.readBlockPos());
    }

    public ManagementMenu(int containerId, Inventory inventory, BlockPos position) {
        super(MaidManagement.MANAGEMENT_MENU.get(), containerId);
        this.position = position;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!player.level().isLoaded(position)) return false;
        Block block = player.level().getBlockState(position).getBlock();
        return block == MaidManagement.MANAGEMENT_CENTER.get()
                && player.distanceToSqr(position.getX() + 0.5D, position.getY() + 0.5D,
                        position.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int slot) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
