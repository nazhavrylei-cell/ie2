package com.example.techmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;

public class TechMod implements ModInitializer {
    public static final String MOD_ID = "techmod";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModBlockEntities.initialize();

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(group -> {
            group.accept(ModBlocks.CABLE.asItem());
            group.accept(ModBlocks.ITEM_CABLE.asItem());
            group.accept(ModBlocks.GENERATOR.asItem());
            group.accept(ModBlocks.ELECTRIC_FURNACE.asItem());
            group.accept(ModBlocks.CRUSHER.asItem());
            group.accept(ModBlocks.QUARRY.asItem());
        });
    }
}
