package tw.yuchen.maidmanagement.menu;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;
import tw.yuchen.maidmanagement.MaidManagement;
import tw.yuchen.maidmanagement.data.MaidRegistryData;

public final class ManagementMenu extends AbstractContainerMenu {
    private static final int SEARCH_RADIUS = 16;
    private final BlockPos position;
    private final Player player;
    private final ContainerData data = new SimpleContainerData(3);

    public ManagementMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, buf.readBlockPos());
    }

    public ManagementMenu(int id, Inventory inventory, BlockPos position) {
        super(MaidManagement.MANAGEMENT_MENU.get(), id);
        this.position = position;
        this.player = inventory.player;
        addDataSlots(data);
        updateData();
    }

    private boolean isMaid(Entity entity) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return id != null && id.getNamespace().equals("touhou_little_maid")
                && id.getPath().equals("maid") && entity.isAlive();
    }

    private List<Entity> nearbyMaids() {
        return player.level().getEntities(player, new AABB(position).inflate(SEARCH_RADIUS), this::isMaid);
    }

    private void updateData() {
        if (!(player.level() instanceof ServerLevel level)) return;
        data.set(0, Math.min(nearbyMaids().size(), 32767));
        data.set(1, Math.min(MaidRegistryData.get(level).count(player.getUUID()), 32767));
    }

    public int nearbyMaidCount() { return data.get(0); }
    public int registeredCount() { return data.get(1); }
    public int selectedJob() { return data.get(2); }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (!(player.level() instanceof ServerLevel level) || !stillValid(player)) return false;
        if (buttonId >= 0 && buttonId < 4) {
            data.set(2, buttonId);
            broadcastChanges();
            return true;
        }
        if (buttonId != 4) return false;
        List<Entity> nearby = nearbyMaids();
        if (nearby.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.maidmanagement.no_maid"), false);
            return true;
        }
        Entity nearest = nearby.stream().min((a, b) -> Double.compare(
                a.distanceToSqr(player), b.distanceToSqr(player))).orElse(null);
        if (nearest == null) return true;
        // The registry is an addon-owned record only; it does not change the maid's native AI.
        boolean ok = MaidRegistryData.get(level).register(nearest.getUUID(), player.getUUID(), data.get(2));
        player.displayClientMessage(Component.translatable(ok
                ? "message.maidmanagement.registered" : "message.maidmanagement.claimed"), false);
        updateData();
        broadcastChanges();
        return true;
    }

    @Override
    public void broadcastChanges() {
        updateData();
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
