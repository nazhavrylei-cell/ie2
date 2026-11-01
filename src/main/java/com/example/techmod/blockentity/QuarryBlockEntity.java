package com.example.techmod.blockentity;

import com.example.techmod.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

/**
 * Кар'єр: копає область 15x15 під собою шар за шаром до самого низу світу.
 * Видобуті предмети викидаються у скриню або на землю над кар'єром.
 */
public class QuarryBlockEntity extends MachineBlockEntity {
    private static final int AREA = 15;
    private static final int HALF = AREA / 2;
    private static final int LAYER_SIZE = AREA * AREA;
    /** Енергія на один зламаний блок. */
    private static final int COST_PER_BLOCK = 150;
    /** Пауза між блоками в тіках. */
    private static final int INTERVAL = 10;
    /** Скільки порожніх клітинок можна пропустити за один тік. */
    private static final int MAX_SKIPS = 400;

    private int index;
    private boolean finished;

    public QuarryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUARRY, pos, state);
    }

    @Override
    public InteractionResult onUse(Player player, ItemStack held) {
        if (!held.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        String status = finished ? "роботу завершено" : "шар " + (index / LAYER_SIZE + 1);
        say(player, "Кар'єр: енергія " + energy + "/" + getCapacity() + ", " + status);
        return InteractionResult.SUCCESS;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, QuarryBlockEntity be) {
        if (!(level instanceof ServerLevel server) || be.finished) {
            return;
        }
        if (server.getGameTime() % INTERVAL != 0) {
            return;
        }

        for (int skips = 0; skips < MAX_SKIPS; skips++) {
            int layer = be.index / LAYER_SIZE;
            int y = pos.getY() - 1 - layer;
            if (y < server.getMinY()) {
                be.finished = true;
                be.setChanged();
                return;
            }

            int cell = be.index % LAYER_SIZE;
            BlockPos target = new BlockPos(
                    pos.getX() + (cell % AREA) - HALF,
                    y,
                    pos.getZ() + (cell / AREA) - HALF);

            if (!server.hasChunkAt(target)) {
                return; // чекаємо, поки чанк завантажиться
            }

            BlockState targetState = server.getBlockState(target);
            if (targetState.isAir()
                    || targetState.getDestroySpeed(server, target) < 0
                    || !targetState.getFluidState().isEmpty()) {
                be.index++;
                continue;
            }

            if (be.energy < COST_PER_BLOCK) {
                return; // не вистачає енергії
            }
            be.energy -= COST_PER_BLOCK;

            List<ItemStack> drops = Block.getDrops(targetState, server, target, server.getBlockEntity(target));
            server.destroyBlock(target, false);
            for (ItemStack drop : drops) {
                be.eject(server, drop);
            }

            be.index++;
            be.setChanged();
            return;
        }
        be.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("index", index);
        output.putBoolean("finished", finished);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        index = in.getIntOr("index", 0);
        finished = in.getBooleanOr("finished", false);
    }
}
