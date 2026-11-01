package com.example.techmod.energy;

import com.example.techmod.block.CableBlock;
import com.example.techmod.blockentity.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Проста енергомережа без власних блок-ентіті у кабелів: генератор обходить
 * сусідні кабелі вшир і віддає енергію всім машинам, до яких дістався.
 */
public final class EnergyNetwork {
    private static final int MAX_BLOCKS = 512;

    private EnergyNetwork() {
    }

    /**
     * Віддає енергію джерела всім машинам, під'єднаним до нього безпосередньо або через кабелі.
     *
     * @param maxPerTarget максимум енергії на одну машину за виклик
     */
    public static void push(ServerLevel level, BlockPos origin, MachineBlockEntity source, int maxPerTarget) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        List<MachineBlockEntity> targets = new ArrayList<>();

        visited.add(origin);
        queue.add(origin);

        int steps = 0;
        while (!queue.isEmpty() && steps++ < MAX_BLOCKS) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (!visited.add(next) || !level.hasChunkAt(next)) {
                    continue;
                }
                if (level.getBlockState(next).getBlock() instanceof CableBlock) {
                    queue.add(next);
                } else if (level.getBlockEntity(next) instanceof MachineBlockEntity machine
                        && machine != source && machine.acceptsEnergy()) {
                    targets.add(machine);
                }
            }
        }

        for (MachineBlockEntity machine : targets) {
            if (source.getEnergy() <= 0) {
                break;
            }
            int moved = machine.receiveEnergy(Math.min(maxPerTarget, source.getEnergy()));
            source.extractEnergy(moved);
        }
    }
}
