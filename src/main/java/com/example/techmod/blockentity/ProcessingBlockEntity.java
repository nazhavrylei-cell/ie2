package com.example.techmod.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Машина, яка переробляє предмети: електропіч та дробарка.
 * Предмети кладуться кліком по блоку, готовий результат викидається вгору.
 */
public abstract class ProcessingBlockEntity extends MachineBlockEntity {
    private static final int MAX_INPUT_STACK = 64;

    protected ItemStack input = ItemStack.EMPTY;
    protected int progress;

    protected ProcessingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** @return результат переробки, або ItemStack.EMPTY, якщо предмет не підходить. */
    protected abstract ItemStack craft(Item item);

    protected abstract int energyPerTick();

    protected abstract int duration();

    protected abstract String title();

    @Override
    public InteractionResult onUse(Player player, ItemStack held) {
        if (held.isEmpty()) {
            int percent = progress * 100 / Math.max(1, duration());
            say(player, title() + ": енергія " + energy + "/" + getCapacity()
                    + ", у черзі " + input.getCount() + ", прогрес " + percent + "%");
            return InteractionResult.SUCCESS;
        }
        if (craft(held.getItem()).isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!input.isEmpty() && !ItemStack.isSameItemSameComponents(input, held)) {
            say(player, "Спочатку дочекайтесь, поки машина закінчить попередній предмет");
            return InteractionResult.SUCCESS;
        }
        int moved = Math.min(held.getCount(), MAX_INPUT_STACK - input.getCount());
        if (moved <= 0) {
            say(player, "Черга переповнена");
            return InteractionResult.SUCCESS;
        }
        if (input.isEmpty()) {
            input = held.copyWithCount(moved);
        } else {
            input.grow(moved);
        }
        if (!player.isCreative()) {
            held.shrink(moved);
        }
        setChanged();
        return InteractionResult.SUCCESS;
    }

    @Override
    public void dropContents(Level level, BlockPos pos) {
        if (!input.isEmpty()) {
            Block.popResource(level, pos, input);
            input = ItemStack.EMPTY;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ProcessingBlockEntity be) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }

        // Самі забираємо сировину зі скринь поруч або через предметний кабель
        if (server.getGameTime() % 8 == 0 && be.input.getCount() < MAX_INPUT_STACK) {
            be.pullInput(server);
        }

        if (be.input.isEmpty()) {
            be.progress = 0;
            return;
        }
        ItemStack result = be.craft(be.input.getItem());
        if (result.isEmpty() || be.energy < be.energyPerTick()) {
            return;
        }

        be.energy -= be.energyPerTick();
        be.progress++;
        if (be.progress >= be.duration()) {
            be.progress = 0;
            be.input.shrink(1);
            be.eject(server, result);
        }
        be.setChanged();
    }

    private void pullInput(ServerLevel level) {
        ItemStack taken = takeFromNetwork(level,
                stack -> !craft(stack.getItem()).isEmpty()
                        && (input.isEmpty() || ItemStack.isSameItemSameComponents(input, stack)),
                Math.min(8, MAX_INPUT_STACK - input.getCount()));
        if (taken.isEmpty()) {
            return;
        }
        if (input.isEmpty()) {
            input = taken;
        } else {
            input.grow(taken.getCount());
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("progress", progress);
        output.store("input", ItemStack.OPTIONAL_CODEC, input);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        progress = in.getIntOr("progress", 0);
        input = in.read("input", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }
}
