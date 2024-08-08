package net.rene.custommapimages.item;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.rene.custommapimages.CustomMapImages;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item CUSTOM_MAP = registerItem("custom_map", new Item(new Item.Settings()));
    private static void addItemsToIngredientTabItemGroup(FabricItemGroupEntries entries) {
        entries.add(CUSTOM_MAP);
    }
    private static Item registerItem(String name, Item item) {

        return Registry.register(Registries.ITEM, Identifier.of(CustomMapImages.MOD_ID, name), item);
    }
    public static void registerModItems() {
        CustomMapImages.LOGGER.info("Registering Mod items for " + CustomMapImages.MOD_ID);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItems::addItemsToIngredientTabItemGroup);
    }
}
