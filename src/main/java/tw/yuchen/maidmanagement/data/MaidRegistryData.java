package tw.yuchen.maidmanagement.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class MaidRegistryData extends SavedData {
    private static final String KEY = "maidmanagement_registry";
    private final Map<UUID, Entry> entries = new HashMap<>();

    public record Entry(UUID owner, int job) {}

    public static MaidRegistryData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(MaidRegistryData::load, MaidRegistryData::new, KEY);
    }

    private static MaidRegistryData load(CompoundTag tag) {
        MaidRegistryData data = new MaidRegistryData();
        ListTag list = tag.getList("Maids", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.hasUUID("Maid") && entry.hasUUID("Owner")) {
                int job = entry.getInt("Job");
                if (job >= 0 && job <= 3) {
                    data.entries.put(entry.getUUID("Maid"), new Entry(entry.getUUID("Owner"), job));
                }
            }
        }
        return data;
    }

    public boolean register(UUID maid, UUID owner, int job) {
        Entry previous = entries.get(maid);
        if (previous != null && !previous.owner().equals(owner)) return false;
        entries.put(maid, new Entry(owner, job));
        setDirty();
        return true;
    }

    public int count(UUID owner) {
        int count = 0;
        for (Entry entry : entries.values()) if (entry.owner().equals(owner)) count++;
        return count;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        entries.forEach((maid, entry) -> {
            CompoundTag record = new CompoundTag();
            record.putUUID("Maid", maid);
            record.putUUID("Owner", entry.owner());
            record.putInt("Job", entry.job());
            list.add(record);
        });
        tag.put("Maids", list);
        return tag;
    }
}
