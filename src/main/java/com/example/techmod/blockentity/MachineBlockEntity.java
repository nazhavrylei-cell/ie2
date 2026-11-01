package com.example.techmod.blockentity;

import com.example.techmod.network.ItemNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.function.Predicate;

/**
 * Основа всіх машин: має буфер енергії, вміє зберігати його та викидати готові предмети
 * (у скриню/лійку зверху або просто на землю над машиною).
 */
public abstract class MachineBlockEntity extends BlockEntity {
    public static final int DEFAULT_CAPACITY = 4000;

    protected int energy;

    protected MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public int getCapacity() {
        return DEFAULT_CAPACITY;
    }

    public int getEnergy() {
        return energy;
    }

    /** Чи приймає машина енергію з кабелів (генератор - ні). */
    public boolean acceptsEnergy() {
        return true;
    }

    /** @return скільки енергії реально прийнято */
    public int receiveEnergy(int amount) {
        int moved = Math.max(0, Math.min(amount, getCapacity() - energy));
        if (moved > 0) {
            energy += moved;
            setChanged();
        }
        return moved;
    }

    /** @return скільки енергії реально забрано */
    public int extractEnergy(int amount) {
        int moved = Math.max(0, Math.min(amount, energy));
        energy -= moved;
        return moved;
    }

    /** Клік по машині. Порожня рука показує стан. */
    public abstract InteractionResult onUse(Player player, ItemStack held);

    /** Викликається при ламанні блока: викидає вміст машини. */
    public void dropContents(Level level, BlockPos pos) {
    }

    protected void say(Player player, String text) {
        player.displayClientMessage(Component.literal(text), true);
    }

    /**
     * Віддає готовий предмет у контейнери поруч або у скрині, під'єднані предметним кабелем.
     * Якщо місця немає, предмет падає над машиною.
     */
    protected void eject(ServerLevel level, ItemStack stack) {
        ItemStack remaining = stack;
        for (Container container : ItemNetwork.findContainers(level, getBlockPos())) {
            remaining = HopperBlockEntity.addItem(null, container, remaining, null);
            if (remaining.isEmpty()) {
                return;
            }
        }
        Block.popResource(level, getBlockPos().above(), remaining);
    }

    /**
     * Забирає до {@code max} предметів, що підходять під фільтр, з контейнерів поруч
     * або з контейнерів, під'єднаних предметним кабелем.
     */
    protected ItemStack takeFromNetwork(ServerLevel level, Predicate<ItemStack> filter, int max) {
        if (max <= 0) {
            return ItemStack.EMPTY;
        }
        for (Container container : ItemNetwork.findContainers(level, getBlockPos())) {
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack inSlot = container.getItem(slot);
                if (inSlot.isEmpty() || !filter.test(inSlot)) {
                    continue;
                }
                ItemStack taken = container.removeItem(slot, Math.min(max, inSlot.getCount()));
                container.setChanged();
                return taken;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("energy", energy);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energy = input.getIntOr("energy", 0);
    }
}
