package com.example.techmod.blockentity;

import com.example.techmod.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/** Дробарка: перетворює руду на подвоєну кількість сировини. */
public class CrusherBlockEntity extends ProcessingBlockEntity {
    private record Output(Item item, int count) {
    }

    private static final Map<Item, Output> RECIPES = new HashMap<>();

    static {
        RECIPES.put(Items.IRON_ORE, new Output(Items.RAW_IRON, 2));
        RECIPES.put(Items.DEEPSLATE_IRON_ORE, new Output(Items.RAW_IRON, 2));
        RECIPES.put(Items.GOLD_ORE, new Output(Items.RAW_GOLD, 2));
        RECIPES.put(Items.DEEPSLATE_GOLD_ORE, new Output(Items.RAW_GOLD, 2));
        RECIPES.put(Items.COPPER_ORE, new Output(Items.RAW_COPPER, 6));
        RECIPES.put(Items.DEEPSLATE_COPPER_ORE, new Output(Items.RAW_COPPER, 6));
        RECIPES.put(Items.COAL_ORE, new Output(Items.COAL, 2));
        RECIPES.put(Items.DEEPSLATE_COAL_ORE, new Output(Items.COAL, 2));
        RECIPES.put(Items.REDSTONE_ORE, new Output(Items.REDSTONE, 8));
        RECIPES.put(Items.DEEPSLATE_REDSTONE_ORE, new Output(Items.REDSTONE, 8));
        RECIPES.put(Items.LAPIS_ORE, new Output(Items.LAPIS_LAZULI, 12));
        RECIPES.put(Items.DEEPSLATE_LAPIS_ORE, new Output(Items.LAPIS_LAZULI, 12));
        RECIPES.put(Items.DIAMOND_ORE, new Output(Items.DIAMOND, 2));
        RECIPES.put(Items.DEEPSLATE_DIAMOND_ORE, new Output(Items.DIAMOND, 2));
        RECIPES.put(Items.EMERALD_ORE, new Output(Items.EMERALD, 2));
        RECIPES.put(Items.DEEPSLATE_EMERALD_ORE, new Output(Items.EMERALD, 2));
        RECIPES.put(Items.COBBLESTONE, new Output(Items.GRAVEL, 1));
    }

    public CrusherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUSHER, pos, state);
    }

    @Override
    protected ItemStack craft(Item item) {
        Output output = RECIPES.get(item);
        return output == null ? ItemStack.EMPTY : new ItemStack(output.item(), output.count());
    }

    @Override
    protected int energyPerTick() {
        return 15;
    }

    @Override
    protected int duration() {
        return 100;
    }

    @Override
    protected String title() {
        return "Дробарка";
    }
}
