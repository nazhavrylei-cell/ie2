package com.example.techmod.blockentity;

import com.example.techmod.ModBlockEntities;
import com.example.techmod.energy.EnergyNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashMap;
import java.util.Map;

public class GeneratorBlockEntity extends MachineBlockEntity {
    /** Скільки енергії генератор виробляє за тік. */
    private static final int PRODUCTION = 20;
    private static final int MAX_FUEL_STACK = 64;
    private static final Map<Item, Integer> FUEL = new HashMap<>();

    static {
        FUEL.put(Items.COAL, 1600);
        FUEL.put(Items.CHARCOAL, 1600);
        FUEL.put(Items.COAL_BLOCK, 16000);
        FUEL.put(Items.BLAZE_ROD, 2400);
        FUEL.put(Items.DRIED_KELP_BLOCK, 4000);
    }

    private ItemStack fuel = ItemStack.EMPTY;
    private int burnTime;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GENERATOR, pos, state);
    }

    @Override
    public boolean acceptsEnergy() {
        return false;
    }

    @Override
    public InteractionResult onUse(Player player, ItemStack held) {
        if (held.isEmpty()) {
            say(player, "Генератор: енергія " + energy + "/" + getCapacity()
                    + ", паливо " + fuel.getCount() + (burnTime > 0 ? " (працює)" : ""));
            return InteractionResult.SUCCESS;
        }
        if (!FUEL.containsKey(held.getItem())) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!fuel.isEmpty() && !ItemStack.isSameItemSameComponents(fuel, held)) {
            say(player, "У генераторі вже інше паливо");
            return InteractionResult.SUCCESS;
        }
        int moved = Math.min(held.getCount(), MAX_FUEL_STACK - fuel.getCount());
        if (moved <= 0) {
            say(player, "У генераторі повно палива");
            return InteractionResult.SUCCESS;
        }
        if (fuel.isEmpty()) {
            fuel = held.copyWithCount(moved);
        } else {
            fuel.grow(moved);
        }
        if (!player.isCreative()) {
            held.shrink(moved);
        }
        setChanged();
        return InteractionResult.SUCCESS;
    }

    @Override
    public void dropContents(Level level, BlockPos pos) {
        if (!fuel.isEmpty()) {
            Block.popResource(level, pos, fuel);
            fuel = ItemStack.EMPTY;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GeneratorBlockEntity be) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }

        boolean changed = false;

        // Самі забираємо паливо зі скринь поруч або через предметний кабель
        if (server.getGameTime() % 8 == 0 && be.fuel.getCount() < MAX_FUEL_STACK) {
            ItemStack taken = be.takeFromNetwork(server,
                    stack -> FUEL.containsKey(stack.getItem())
                            && (be.fuel.isEmpty() || ItemStack.isSameItemSameComponents(be.fuel, stack)),
                    Math.min(8, MAX_FUEL_STACK - be.fuel.getCount()));
            if (!taken.isEmpty()) {
                if (be.fuel.isEmpty()) {
                    be.fuel = taken;
                } else {
                    be.fuel.grow(taken.getCount());
                }
                changed = true;
            }
        }

        // Беремо нове паливо, лише якщо є куди складати енергію
        if (be.burnTime <= 0 && !be.fuel.isEmpty() && be.energy + PRODUCTION <= be.getCapacity()) {
            Integer time = FUEL.get(be.fuel.getItem());
            if (time != null) {
                be.fuel.shrink(1);
                be.burnTime = time;
                changed = true;
            }
        }

        if (be.burnTime > 0 && be.energy + PRODUCTION <= be.getCapacity()) {
            be.burnTime--;
            be.energy += PRODUCTION;
            changed = true;
        }

        // Роздаємо енергію машинам кожні 4 тіки
        if (be.energy > 0 && server.getGameTime() % 4 == 0) {
            EnergyNetwork.push(server, pos, be, 200);
            changed = true;
        }

        if (changed) {
            be.setChanged();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("burn_time", burnTime);
        output.store("fuel", ItemStack.OPTIONAL_CODEC, fuel);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        burnTime = input.getIntOr("burn_time", 0);
        fuel = input.read("fuel", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }
}
