package com.benji.netherman.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import com.benji.netherman.NetherExp; // Убедись, что импорт твоего главного класса совпадает

import java.util.Optional;

public class MegaJigsawStructure extends Structure {
    // Кодек без валидации ограничения размера в 128 блоков (подняли до 512)
    public static final Codec<MegaJigsawStructure> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter((structure) -> structure.startPool),
            ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter((structure) -> structure.startJigsawName),
            Codec.intRange(0, 30).fieldOf("size").forGetter((structure) -> structure.size),
            HeightProvider.CODEC.fieldOf("start_height").forGetter((structure) -> structure.startHeight),
            Codec.BOOL.fieldOf("use_expansion_hack").forGetter((structure) -> structure.useExpansionHack),
            Heightmap.Types.CODEC.optionalFieldOf("project_start_height").forGetter((structure) -> structure.projectStartHeight),
            Codec.intRange(1, 512).fieldOf("max_distance_from_center").forGetter((structure) -> structure.maxDistanceFromCenter)
    ).apply(instance, MegaJigsawStructure::new));

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartHeight;
    private final int maxDistanceFromCenter;

    public MegaJigsawStructure(Structure.StructureSettings settings, Holder<StructureTemplatePool> startPool, Optional<ResourceLocation> startJigsawName, int size, HeightProvider startHeight, boolean useExpansionHack, Optional<Heightmap.Types> projectStartHeight, int maxDistanceFromCenter) {
        super(settings);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.projectStartHeight = projectStartHeight;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        // 1. Вычисляем случайную стартовую высоту Y из тех, что мы задали в JSON
        int startY = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));

        // 2. Определяем точную координату X, Y, Z для старта генерации
        BlockPos blockpos = new BlockPos(context.chunkPos().getMinBlockX(), startY, context.chunkPos().getMinBlockZ());

        // 3. Вызываем правильный статический метод сборки пазлов JigsawPlacement
        return JigsawPlacement.addPieces(
                context,
                this.startPool,
                this.startJigsawName,
                this.size,
                blockpos,
                this.useExpansionHack,
                this.projectStartHeight,
                this.maxDistanceFromCenter
        );
    }

    @Override
    public StructureType<?> type() {
        // Ссылка на регистрацию типа структуры в твоем классе NetherExp
        return NetherExp.MEGA_JIGSAW_STRUCTURE.get();
    }
}