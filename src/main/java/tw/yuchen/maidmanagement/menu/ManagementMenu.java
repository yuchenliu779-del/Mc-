package tw.yuchen.maidmanagement.menu;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import java.util.UUID;
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
    private final ContainerData data = new SimpleContainerData(7);

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
        MaidRegistryData registry = MaidRegistryData.get(level);
        data.set(1, Math.min(registry.count(player.getUUID()), 32767));
        for (int job = 0; job < 4; job++)
            data.set(3 + job, Math.min(registry.countByJob(player.getUUID(), job), 32767));
    }

    public int nearbyMaidCount() { return data.get(0); }
    public int registeredCount() { return data.get(1); }
    public int selectedJob() { return data.get(2); }
    public int jobCount(int job) { return data.get(3 + Math.max(0, Math.min(3, job))); }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (!(player.level() instanceof ServerLevel level) || !stillValid(player)) return false;
        if (buttonId >= 0 && buttonId < 4) {
            data.set(2, buttonId);
            broadcastChanges();
            return true;
        }
        if (buttonId != 4 && buttonId != 5) return false;
        List<Entity> nearby = nearbyMaids();
        if (nearby.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.maidmanagement.no_maid"), false);
            return true;
        }
        // Only a maid owned by this player can be registered or unregistered.
        Entity nearest = nearby.stream().filter(e -> e instanceof OwnableEntity ownable
                && player.getUUID().equals(ownable.getOwnerUUID()))
                .min((a, b) -> Double.compare(
                a.distanceToSqr(player), b.distanceToSqr(player))).orElse(null);
        if (nearest == null) {
            player.displayClientMessage(Component.translatable("message.maidmanagement.not_owner"), false);
            return true;
        }
        // The registry is an addon-owned record only; it does not change the maid's native AI.
        boolean ok = buttonId == 4
                ? MaidRegistryData.get(level).register(nearest.getUUID(), player.getUUID(), data.get(2))
                : MaidRegistryData.get(level).unregister(nearest.getUUID(), player.getUUID());
        String message = buttonId == 4
                ? (ok ? "message.maidmanagement.registered" : "message.maidmanagement.claimed")
                : (ok ? "message.maidmanagement.unregistered" : "message.maidmanagement.not_registered");
        player.displayClientMessage(Component.translatable(message), false);
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
