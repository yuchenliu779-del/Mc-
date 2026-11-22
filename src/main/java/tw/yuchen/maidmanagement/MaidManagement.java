package tw.yuchen.maidmanagement;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tw.yuchen.maidmanagement.block.ManagementCenterBlock;
import tw.yuchen.maidmanagement.menu.ManagementMenu;

@Mod(MaidManagement.MOD_ID)
public final class MaidManagement {
    public static final String MOD_ID = "maidmanagement";
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);

    public static final RegistryObject<Block> MANAGEMENT_CENTER = BLOCKS.register(
            "management_center",
            () -> new ManagementCenterBlock(BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE).strength(3.0F))
    );
    public static final RegistryObject<Item> MANAGEMENT_CENTER_ITEM = ITEMS.register(
            "management_center", () -> new BlockItem(MANAGEMENT_CENTER.get(), new Item.Properties())
    );
    public static final RegistryObject<MenuType<ManagementMenu>> MANAGEMENT_MENU = MENUS.register(
            "management_menu", () -> IForgeMenuType.create(ManagementMenu::new)
    );

    public MaidManagement() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        MENUS.register(bus);
        bus.addListener(this::addCreativeTabItems);
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(MANAGEMENT_CENTER_ITEM);
        }
    }
}
