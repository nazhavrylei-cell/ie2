package com.example.techmod;

import com.example.techmod.block.CableBlock;
import com.example.techmod.block.CrusherBlock;
import com.example.techmod.block.ElectricFurnaceBlock;
import com.example.techmod.block.GeneratorBlock;
import com.example.techmod.block.ItemCableBlock;
import com.example.techmod.block.QuarryBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public final class ModBlocks {
    public static final CableBlock CABLE = createBlock("cable", CableBlock::new,
            BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.COPPER).noOcclusion());

    public static final ItemCableBlock ITEM_CABLE = createBlock("item_cable", ItemCableBlock::new,
            BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.METAL).noOcclusion());

    public static final GeneratorBlock GENERATOR = createBlock("generator", GeneratorBlock::new, machineProperties());
    public static final ElectricFurnaceBlock ELECTRIC_FURNACE = createBlock("electric_furnace", ElectricFurnaceBlock::new, machineProperties());
    public static final CrusherBlock CRUSHER = createBlock("crusher", CrusherBlock::new, machineProperties());
    public static final QuarryBlock QUARRY = createBlock("quarry", QuarryBlock::new, machineProperties());

    private ModBlocks() {
    }

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    private static <T extends Block> T createBlock(String name, Function<BlockBehaviour.Properties, T> factory,
                                                   BlockBehaviour.Properties properties) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, TechMod.id(name));
        T block = factory.apply(properties.setId(blockKey));

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, TechMod.id(name));
        BlockItem item = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    public static void initialize() {
    }
}
