package com.example.techmod;

import com.example.techmod.blockentity.CrusherBlockEntity;
import com.example.techmod.blockentity.ElectricFurnaceBlockEntity;
import com.example.techmod.blockentity.GeneratorBlockEntity;
import com.example.techmod.blockentity.QuarryBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static final BlockEntityType<GeneratorBlockEntity> GENERATOR =
            register("generator", GeneratorBlockEntity::new, ModBlocks.GENERATOR);
    public static final BlockEntityType<ElectricFurnaceBlockEntity> ELECTRIC_FURNACE =
            register("electric_furnace", ElectricFurnaceBlockEntity::new, ModBlocks.ELECTRIC_FURNACE);
    public static final BlockEntityType<CrusherBlockEntity> CRUSHER =
            register("crusher", CrusherBlockEntity::new, ModBlocks.CRUSHER);
    public static final BlockEntityType<QuarryBlockEntity> QUARRY =
            register("quarry", QuarryBlockEntity::new, ModBlocks.QUARRY);

    private ModBlockEntities() {
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> factory,
            Block... blocks) {
        Identifier id = TechMod.id(name);
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id,
                FabricBlockEntityTypeBuilder.<T>create(factory, blocks).build());
    }

    public static void initialize() {
    }
}
