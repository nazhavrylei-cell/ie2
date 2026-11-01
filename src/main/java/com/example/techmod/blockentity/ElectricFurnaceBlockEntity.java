package com.example.techmod.blockentity;

import com.example.techmod.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/** Електропіч: плавить предмети зі списку нижче швидше за звичайну піч. */
public class ElectricFurnaceBlockEntity extends ProcessingBlockEntity {
    private static final Map<Item, Item> SMELTING = new HashMap<>();

    static {
        // руди та сировина
        SMELTING.put(Items.RAW_IRON, Items.IRON_INGOT);
        SMELTING.put(Items.RAW_GOLD, Items.GOLD_INGOT);
        SMELTING.put(Items.RAW_COPPER, Items.COPPER_INGOT);
        SMELTING.put(Items.IRON_ORE, Items.IRON_INGOT);
        SMELTING.put(Items.DEEPSLATE_IRON_ORE, Items.IRON_INGOT);
        SMELTING.put(Items.GOLD_ORE, Items.GOLD_INGOT);
        SMELTING.put(Items.DEEPSLATE_GOLD_ORE, Items.GOLD_INGOT);
        SMELTING.put(Items.COPPER_ORE, Items.COPPER_INGOT);
        SMELTING.put(Items.DEEPSLATE_COPPER_ORE, Items.COPPER_INGOT);
        // будівельні матеріали
        SMELTING.put(Items.SAND, Items.GLASS);
        SMELTING.put(Items.COBBLESTONE, Items.STONE);
        SMELTING.put(Items.CLAY_BALL, Items.BRICK);
        SMELTING.put(Items.NETHERRACK, Items.NETHER_BRICK);
        SMELTING.put(Items.OAK_LOG, Items.CHARCOAL);
        // їжа
        SMELTING.put(Items.BEEF, Items.COOKED_BEEF);
        SMELTING.put(Items.PORKCHOP, Items.COOKED_PORKCHOP);
        SMELTING.put(Items.CHICKEN, Items.COOKED_CHICKEN);
        SMELTING.put(Items.POTATO, Items.BAKED_POTATO);
    }

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_FURNACE, pos, state);
    }

    @Override
    protected ItemStack craft(Item item) {
        Item result = SMELTING.get(item);
        return result == null ? ItemStack.EMPTY : new ItemStack(result);
    }

    @Override
    protected int energyPerTick() {
        return 10;
    }

    @Override
    protected int duration() {
        return 80;
    }

    @Override
    protected String title() {
        return "Електропіч";
    }
}
