package com.example.techmod.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

public class CableBlock extends Block {
    public static final MapCodec<CableBlock> CODEC = simpleCodec(CableBlock::new);

    public CableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
