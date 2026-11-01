package com.example.techmod.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

/** Предметний кабель: з'єднує машини зі скринями. Сама по собі нічого не робить. */
public class ItemCableBlock extends Block {
    public static final MapCodec<ItemCableBlock> CODEC = simpleCodec(ItemCableBlock::new);

    public ItemCableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
