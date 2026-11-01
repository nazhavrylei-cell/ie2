package com.example.techmod.network;

import com.example.techmod.block.ItemCableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Предметна мережа: шукає всі контейнери (скрині, бочки, лійки), які стоять впритул
 * до машини або до предметних кабелів, під'єднаних до неї.
 */
public final class ItemNetwork {
    private static final int MAX_BLOCKS = 512;

    private ItemNetwork() {
    }

    public static List<Container> findContainers(ServerLevel level, BlockPos origin) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        List<Container> result = new ArrayList<>();

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
                if (level.getBlockState(next).getBlock() instanceof ItemCableBlock) {
                    queue.add(next);
                } else if (level.getBlockEntity(next) instanceof Container container) {
                    result.add(container);
                }
            }
        }
        return result;
    }
}
