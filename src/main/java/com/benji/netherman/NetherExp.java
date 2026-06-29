package com.benji.netherman;

import com.benji.netherman.block.*;
import com.benji.netherman.block.entity.*;
import com.benji.netherman.config.AzazelConfig;
import com.benji.netherman.item.AzazelGuideBookItem;
import com.benji.netherman.item.AzazelTrophyItem;
import com.benji.netherman.network.TotemAnimationPacket;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.level.block.*;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.fml.config.ModConfig;
import com.benji.netherman.client.ManipulationOverlay;
import com.benji.netherman.client.renderer.*;
import com.benji.netherman.client.renderer.entity.GhastlyRenderer;
import com.benji.netherman.client.renderer.entity.GildedGolemRenderer;
import com.benji.netherman.client.renderer.entity.GuardianRenderer;
import com.benji.netherman.effect.ManipulationEffect;
import com.benji.netherman.effect.ZoneEffect;
import com.benji.netherman.entity.*;
import com.benji.netherman.entity.BelieverEntity;
import com.benji.netherman.item.GeoBlockItem;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import com.benji.netherman.network.ModMessages;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.benji.netherman.client.renderer.entity.WelcomerRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;

@Mod(NetherExp.MODID)
public class NetherExp {
    public static final String MODID = "netherman";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.STRUCTURE_TYPE, MODID);

    public static final DeferredRegister<PaintingVariant> PAINTING_VARIANTS = DeferredRegister.create(ForgeRegistries.PAINTING_VARIANTS, MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final SoundType SAMSONIT_SOUNDS = new ForgeSoundType(
            1.0F, 1.0F,
            ModSounds.SAMSONIT_BREAK,
            ModSounds.SAMSONIT_STEP,
            ModSounds.SAMSONIT_PLACE,
            ModSounds.SAMSONIT_HIT,
            ModSounds.SAMSONIT_STEP
    );

    public static final SoundType SAMSONIT_BRICKS_SOUNDS = new ForgeSoundType(
            1.0F, 1.0F,
            ModSounds.SAMSONIT_BREAK,
            ModSounds.SAMSONIT_BRICKS_STEP,
            ModSounds.SAMSONIT_BRICKS_PLACE,
            ModSounds.SAMSONIT_HIT,
            ModSounds.SAMSONIT_BRICKS_STEP
    );

    public static final RegistryObject<PaintingVariant> ANGEL_PAINTING = PAINTING_VARIANTS.register("angel_painting", () -> new PaintingVariant(48, 64));
    public static final RegistryObject<PaintingVariant> FACE_PAINTING = PAINTING_VARIANTS.register("face_painting", () -> new PaintingVariant(48, 64));
    public static final RegistryObject<PaintingVariant> VILLAGE_PAINTING = PAINTING_VARIANTS.register("village_painting", () -> new PaintingVariant(48, 64));
    public static final RegistryObject<PaintingVariant> KING_PAINTING = PAINTING_VARIANTS.register("king_painting", () -> new PaintingVariant(48, 64));
    public static final RegistryObject<PaintingVariant> THRONE_PAINTING = PAINTING_VARIANTS.register("throne_painting", () -> new PaintingVariant(48, 64));

    public static final RegistryObject<Block> NETHER_SPAWNER = BLOCKS.register("nether_spawner",
            () -> new NetherSpawnerBlock(BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)
                    .lightLevel(state -> 15)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)
                    .noOcclusion()));
    public static final RegistryObject<Item> NETHER_SPAWNER_ITEM = ITEMS.register("nether_spawner",
            () -> new BlockItem(NETHER_SPAWNER.get(), new Item.Properties()));

    public static final RegistryObject<Block> TOTEMUS_HOLE = BLOCKS.register("totemus_hole",
            () -> new TotemusHoleBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)
                    .noOcclusion()));

    public static final RegistryObject<Item> TOTEMUS_HOLE_ITEM = ITEMS.register("totemus_hole",
            () -> new BlockItem(TOTEMUS_HOLE.get(), new Item.Properties()));

    public static final RegistryObject<Block> LABYRINTH_TELEPORT = BLOCKS.register("labyrinth_teleport",
            () -> new LabyrinthTeleportBlock(BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)
                    .lightLevel(state -> 15)
                    .requiresCorrectToolForDrops()
                    .strength(20.0F)
                    .noOcclusion()));

    public static final RegistryObject<Item> LABYRINTH_TELEPORT_ITEM = ITEMS.register("labyrinth_teleport",
            () -> new BlockItem(LABYRINTH_TELEPORT.get(), new Item.Properties()));

    public static final RegistryObject<Block> LABYRINTH_BELLSPAWN = BLOCKS.register("labyrinth_bellspawn",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)
                    .lightLevel(state -> 15)
                    .requiresCorrectToolForDrops()
                    .strength(20.0F)
                    .noOcclusion()));

    public static final RegistryObject<Item> LABYRINTH_BELLSPAWN_ITEM = ITEMS.register("labyrinth_bellspawn",
            () -> new BlockItem(LABYRINTH_BELLSPAWN.get(), new Item.Properties()));

    public static final RegistryObject<Block> BLACKSTONE_COLUMN = BLOCKS.register("blackstone_column",
            () -> new BlackstoneColumnBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(2.0F)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Item> BLACKSTONE_COLUMN_ITEM = ITEMS.register("blackstone_column",
            () -> new BlockItem(BLACKSTONE_COLUMN.get(), new Item.Properties()));

    public static final RegistryObject<Block> POTENT_MAGMA = BLOCKS.register("potent_magma",
            () -> new PotentMagmaBlock(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(2.0F)
                    .lightLevel(state -> 10)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Item> POTENT_MAGMA_ITEM = ITEMS.register("potent_magma",
            () -> new BlockItem(POTENT_MAGMA.get(), new Item.Properties()));
    
    public static final RegistryObject<Block> FACE_PUZZLE_RIGHT_DOWN = BLOCKS.register("face_puzzle_right_down",
            () -> new FacePuzzleBlock(BlockBehaviour.Properties.copy(Blocks.STONE).strength(2.0F).requiresCorrectToolForDrops().noOcclusion(), 2, NetherExp.FACE_PUZZLE_RIGHT_DOWN_BE));

    public static final RegistryObject<Block> FACE_PUZZLE_LEFT_UP = BLOCKS.register("face_puzzle_left_up",
            () -> new FacePuzzleBlock(BlockBehaviour.Properties.copy(Blocks.STONE).strength(2.0F).requiresCorrectToolForDrops().noOcclusion(), 2, NetherExp.FACE_PUZZLE_LEFT_UP_BE));

    public static final RegistryObject<Block> FACE_PUZZLE_RIGHT_UP = BLOCKS.register("face_puzzle_right_up",
            () -> new FacePuzzleBlock(BlockBehaviour.Properties.copy(Blocks.STONE).strength(2.0F).requiresCorrectToolForDrops().noOcclusion(), 3, NetherExp.FACE_PUZZLE_RIGHT_UP_BE));

    public static final RegistryObject<Block> FACE_PUZZLE_LEFT_DOWN = BLOCKS.register("face_puzzle_left_down",
            () -> new FacePuzzleBlock(BlockBehaviour.Properties.copy(Blocks.STONE).strength(2.0F).requiresCorrectToolForDrops().noOcclusion(), 3, NetherExp.FACE_PUZZLE_LEFT_DOWN_BE));

    public static final RegistryObject<Item> FACE_PUZZLE_RIGHT_DOWN_ITEM = ITEMS.register("face_puzzle_right_down", () -> new BlockItem(FACE_PUZZLE_RIGHT_DOWN.get(), new Item.Properties()));
    public static final RegistryObject<Item> FACE_PUZZLE_LEFT_UP_ITEM = ITEMS.register("face_puzzle_left_up", () -> new BlockItem(FACE_PUZZLE_LEFT_UP.get(), new Item.Properties()));
    public static final RegistryObject<Item> FACE_PUZZLE_RIGHT_UP_ITEM = ITEMS.register("face_puzzle_right_up", () -> new BlockItem(FACE_PUZZLE_RIGHT_UP.get(), new Item.Properties()));
    public static final RegistryObject<Item> FACE_PUZZLE_LEFT_DOWN_ITEM = ITEMS.register("face_puzzle_left_down", () -> new BlockItem(FACE_PUZZLE_LEFT_DOWN.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<FacePuzzleBlockEntity>> FACE_PUZZLE_RIGHT_DOWN_BE = BLOCK_ENTITIES.register("face_puzzle_right_down",
            () -> BlockEntityType.Builder.of((pos, state) -> new FacePuzzleBlockEntity(NetherExp.FACE_PUZZLE_RIGHT_DOWN_BE.get(), pos, state), FACE_PUZZLE_RIGHT_DOWN.get()).build(null));

    public static final RegistryObject<BlockEntityType<FacePuzzleBlockEntity>> FACE_PUZZLE_LEFT_UP_BE = BLOCK_ENTITIES.register("face_puzzle_left_up",
            () -> BlockEntityType.Builder.of((pos, state) -> new FacePuzzleBlockEntity(NetherExp.FACE_PUZZLE_LEFT_UP_BE.get(), pos, state), FACE_PUZZLE_LEFT_UP.get()).build(null));

    public static final RegistryObject<BlockEntityType<FacePuzzleBlockEntity>> FACE_PUZZLE_RIGHT_UP_BE = BLOCK_ENTITIES.register("face_puzzle_right_up",
            () -> BlockEntityType.Builder.of((pos, state) -> new FacePuzzleBlockEntity(NetherExp.FACE_PUZZLE_RIGHT_UP_BE.get(), pos, state), FACE_PUZZLE_RIGHT_UP.get()).build(null));

    public static final RegistryObject<BlockEntityType<FacePuzzleBlockEntity>> FACE_PUZZLE_LEFT_DOWN_BE = BLOCK_ENTITIES.register("face_puzzle_left_down",
            () -> BlockEntityType.Builder.of((pos, state) -> new FacePuzzleBlockEntity(NetherExp.FACE_PUZZLE_LEFT_DOWN_BE.get(), pos, state), FACE_PUZZLE_LEFT_DOWN.get()).build(null));


    public static final RegistryObject<Block> SAMSONITE_BELL = BLOCKS.register("samsonite_bell",
            () -> new SamsoniteBellBlock(BlockBehaviour.Properties.copy(Blocks.BELL)));

    public static final RegistryObject<BlockEntityType<SamsoniteBellBlockEntity>> SAMSONITE_BELL_BE = BLOCK_ENTITIES.register("samsonite_bell",
            () -> BlockEntityType.Builder.of(SamsoniteBellBlockEntity::new, SAMSONITE_BELL.get()).build(null));

    
    public static final RegistryObject<Block> SAMSONIT = BLOCKS.register("samsonit",
            () -> new SamsonitBlock(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_SOUNDS)));

    public static final RegistryObject<Item> SAMSONIT_ITEM = ITEMS.register("samsonit",
            () -> new BlockItem(SAMSONIT.get(), new Item.Properties()));

    public static final RegistryObject<Block> LOCKER_NETHER = BLOCKS.register("locker_nether",
            () -> new LockerNetherBlock(BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)
                    .strength(2.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<Item> LOCKER_NETHER_ITEM = ITEMS.register("locker_nether",
            () -> new BlockItem(LOCKER_NETHER.get(), new Item.Properties()));

    public static final RegistryObject<Block> SAMSONIT_EYE = BLOCKS.register("samsonit_eye",
            () -> new SamsonitEyeBlock(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_SOUNDS)));

    public static final RegistryObject<Item> SAMSONIT_EYE_ITEM = ITEMS.register("samsonit_eye",
            () -> new BlockItem(SAMSONIT_EYE.get(), new Item.Properties()));

    public static final RegistryObject<Block> SAMSONIT_BELL = BLOCKS.register("samsonit_bell",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_SOUNDS)));

    public static final RegistryObject<Item> SAMSONIT_BELL_ITEM = ITEMS.register("samsonit_bell",
            () -> new BlockItem(SAMSONIT_BELL.get(), new Item.Properties()));

    public static final RegistryObject<Block> SAMSONIT_KEY = BLOCKS.register("samsonit_key",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_SOUNDS)));

    public static final RegistryObject<Item> SAMSONIT_KEY_ITEM = ITEMS.register("samsonit_key",
            () -> new BlockItem(SAMSONIT_KEY.get(), new Item.Properties()));

    public static final RegistryObject<Block> A_PUZZLE = BLOCKS.register("a_puzzle",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_SOUNDS)));

    public static final RegistryObject<Block> Z_PUZZLE = BLOCKS.register("z_puzzle",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_SOUNDS)));

    public static final RegistryObject<Block> E_PUZZLE = BLOCKS.register("e_puzzle",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_SOUNDS)));

    public static final RegistryObject<Block> L_PUZZLE = BLOCKS.register("l_puzzle",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_SOUNDS)));

    public static final RegistryObject<Item> A_PUZZLE_ITEM = ITEMS.register("a_puzzle",
            () -> new BlockItem(A_PUZZLE.get(), new Item.Properties()));

    public static final RegistryObject<Item> Z_PUZZLE_ITEM = ITEMS.register("z_puzzle",
            () -> new BlockItem(Z_PUZZLE.get(), new Item.Properties()));

    public static final RegistryObject<Item> E_PUZZLE_ITEM = ITEMS.register("e_puzzle",
            () -> new BlockItem(E_PUZZLE.get(), new Item.Properties()));

    public static final RegistryObject<Item> L_PUZZLE_ITEM = ITEMS.register("l_puzzle",
            () -> new BlockItem(L_PUZZLE.get(), new Item.Properties()));


    public static final RegistryObject<Block> COBBLED_SAMSONIT = BLOCKS.register("cobbled_samsonit",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_SOUNDS)));

    public static final RegistryObject<Item> COBBLED_SAMSONIT_ITEM = ITEMS.register("cobbled_samsonit",
            () -> new BlockItem(COBBLED_SAMSONIT.get(), new Item.Properties()));


    public static final RegistryObject<Block> SAMSONIT_BRICKS = BLOCKS.register("samsonit_bricks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_BRICKS_SOUNDS)));

    public static final RegistryObject<Item> SAMSONIT_BRICKS_ITEM = ITEMS.register("samsonit_bricks",
            () -> new BlockItem(SAMSONIT_BRICKS.get(), new Item.Properties()));

    public static final RegistryObject<Block> POLISHED_SAMSONIT = BLOCKS.register("polished_samsonit",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_BRICKS_SOUNDS)));

    public static final RegistryObject<Item> POLISHED_SAMSONIT_ITEM = ITEMS.register("polished_samsonit",
            () -> new BlockItem(POLISHED_SAMSONIT.get(), new Item.Properties()));

    public static final RegistryObject<Block> SAMSONIT_TILES = BLOCKS.register("samsonit_tiles",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_BRICKS_SOUNDS)));

    public static final RegistryObject<Item> SAMSONIT_TILES_ITEM = ITEMS.register("samsonit_tiles",
            () -> new BlockItem(SAMSONIT_TILES.get(), new Item.Properties()));

    public static final RegistryObject<Block> CHISELED_SAMSONIT = BLOCKS.register("chiseled_samsonit",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_BRICKS_SOUNDS)));

    public static final RegistryObject<Item> CHISELED_SAMSONIT_ITEM = ITEMS.register("chiseled_samsonit",
            () -> new BlockItem(CHISELED_SAMSONIT.get(), new Item.Properties()));



    public static final RegistryObject<Block> COBBLED_SAMSONIT_SLAB = BLOCKS.register("cobbled_samsonit_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_SOUNDS)));

    public static final RegistryObject<Item> COBBLED_SAMSONIT_SLAB_ITEM = ITEMS.register("cobbled_samsonit_slab",
            () -> new BlockItem(COBBLED_SAMSONIT_SLAB.get(), new Item.Properties()));

    public static final RegistryObject<Block> POLISHED_SAMSONIT_SLAB = BLOCKS.register("polished_samsonit_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_BRICKS_SOUNDS)));

    public static final RegistryObject<Item> POLISHED_SAMSONIT_SLAB_ITEM = ITEMS.register("polished_samsonit_slab",
            () -> new BlockItem(POLISHED_SAMSONIT_SLAB.get(), new Item.Properties()));


    public static final RegistryObject<Block> SAMSONIT_BRICKS_SLAB = BLOCKS.register("samsonit_bricks_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_BRICKS_SOUNDS)));

    public static final RegistryObject<Item> SAMSONIT_BRICKS_SLAB_ITEM = ITEMS.register("samsonit_bricks_slab",
            () -> new BlockItem(SAMSONIT_BRICKS_SLAB.get(), new Item.Properties()));

    

    public static final RegistryObject<Block> COBBLED_SAMSONIT_STAIRS = BLOCKS.register("cobbled_samsonit_stairs",
            () -> new StairBlock(() -> COBBLED_SAMSONIT.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_SOUNDS)));

    public static final RegistryObject<Item> COBBLED_SAMSONIT_STAIRS_ITEM = ITEMS.register("cobbled_samsonit_stairs",
            () -> new BlockItem(COBBLED_SAMSONIT_STAIRS.get(), new Item.Properties()));

    public static final RegistryObject<Block> POLISHED_SAMSONIT_STAIRS = BLOCKS.register("polished_samsonit_stairs",
            () -> new StairBlock(() -> POLISHED_SAMSONIT.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_BRICKS_SOUNDS)));

    public static final RegistryObject<Item> POLISHED_SAMSONIT_STAIRS_ITEM = ITEMS.register("polished_samsonit_stairs",
            () -> new BlockItem(POLISHED_SAMSONIT_STAIRS.get(), new Item.Properties()));


    public static final RegistryObject<Block> SAMSONIT_BRICKS_STAIRS = BLOCKS.register("samsonit_bricks_stairs",
            () -> new StairBlock(() -> SAMSONIT_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_BRICKS_SOUNDS)));

    public static final RegistryObject<Item> SAMSONIT_BRICKS_STAIRS_ITEM = ITEMS.register("samsonit_bricks_stairs",
            () -> new BlockItem(SAMSONIT_BRICKS_STAIRS.get(), new Item.Properties()));



    public static final RegistryObject<Block> COBBLED_SAMSONIT_WALL = BLOCKS.register("cobbled_samsonit_wall",
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_SOUNDS)));

    public static final RegistryObject<Item> COBBLED_SAMSONIT_WALL_ITEM = ITEMS.register("cobbled_samsonit_wall",
            () -> new BlockItem(COBBLED_SAMSONIT_WALL.get(), new Item.Properties()));

    public static final RegistryObject<Block> POLISHED_SAMSONIT_WALL = BLOCKS.register("polished_samsonit_wall",
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_BRICKS_SOUNDS)));

    public static final RegistryObject<Item> POLISHED_SAMSONIT_WALL_ITEM = ITEMS.register("polished_samsonit_wall",
            () -> new BlockItem(POLISHED_SAMSONIT_WALL.get(), new Item.Properties()));


    public static final RegistryObject<Block> SAMSONIT_BRICKS_WALL = BLOCKS.register("samsonit_bricks_wall",
            () -> new WallBlock(BlockBehaviour.Properties.copy(Blocks.TUFF)
                    .strength(6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SAMSONIT_BRICKS_SOUNDS)));

    public static final RegistryObject<Item> SAMSONIT_BRICKS_WALL_ITEM = ITEMS.register("samsonit_bricks_wall",
            () -> new BlockItem(SAMSONIT_BRICKS_WALL.get(), new Item.Properties()));

    


    public static final RegistryObject<Block> POINTED_BLACKSTONE = BLOCKS.register("pointed_blackstone",
            () -> new PointedBlackstoneBlock(BlockBehaviour.Properties.copy(Blocks.POINTED_DRIPSTONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.0F)
                    .noOcclusion()));

    public static final RegistryObject<Item> POINTED_BLACKSTONE_ITEM = ITEMS.register("pointed_blackstone",
            () -> new BlockItem(POINTED_BLACKSTONE.get(), new Item.Properties()));

    public static final RegistryObject<Block> BLACKSTONE_PLANT = BLOCKS.register("blackstone_plant",
            () -> new BlackstonePlantBlock(BlockBehaviour.Properties.copy(Blocks.GRASS)
                    .lightLevel(state -> 10)
                    .instabreak()
                    .noOcclusion()));

    public static final RegistryObject<StructureType<com.benji.netherman.worldgen.structure.MegaJigsawStructure>> MEGA_JIGSAW_STRUCTURE =
            STRUCTURE_TYPES.register("mega_jigsaw", () -> () -> com.benji.netherman.worldgen.structure.MegaJigsawStructure.CODEC);

    public static final RegistryObject<Block> VOIDMID = BLOCKS.register("void_mid",
            () -> new VoidMidBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(20.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<Item> VOIDMID_ITEM = ITEMS.register("void_mid",
            () -> new BlockItem(VOIDMID.get(), new Item.Properties()));

    public static final RegistryObject<Block> VOIDCORNER = BLOCKS.register("void_corner",
            () -> new VoidCornerBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(20.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<Item> VOIDCORNER_ITEM = ITEMS.register("void_corner",
            () -> new BlockItem(VOIDCORNER.get(), new Item.Properties()));


    public static final RegistryObject<Block> VOIDMIDCORNER = BLOCKS.register("void_midcorner",
            () -> new VoidMidCornerBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(20.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<Item> VOIDMIDCORNER_ITEM = ITEMS.register("void_midcorner",
            () -> new BlockItem(VOIDMIDCORNER.get(), new Item.Properties()));

    public static final RegistryObject<Block> VOIDMIDNETHER = BLOCKS.register("voidnether_mid",
            () -> new VoidNetherMidBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(20.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));


    public static final RegistryObject<Block> VOIDMID_CAVE = BLOCKS.register("void_cave_mid",
            () -> new VoidCaveMidBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(20.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 10)
                    .noOcclusion()));

    public static final RegistryObject<Item> VOIDMID_CAVE_ITEM = ITEMS.register("void_cave_mid",
            () -> new BlockItem(VOIDMID_CAVE.get(), new Item.Properties()));

    public static final RegistryObject<Block> VOIDCORNER_CAVE = BLOCKS.register("void_cave_corner",
            () -> new VoidCaveCornerBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(20.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 10)
                    .noOcclusion()));

    public static final RegistryObject<Item> VOIDCORNER_CAVE_ITEM = ITEMS.register("void_cave_corner",
            () -> new BlockItem(VOIDCORNER_CAVE.get(), new Item.Properties()));


    public static final RegistryObject<Block> VOIDMIDCORNER_CAVE = BLOCKS.register("void_cave_midcorner",
            () -> new VoidCaveMidCornerBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(20.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 10)
                    .noOcclusion()));

    public static final RegistryObject<Item> VOIDMIDCORNER_CAVE_ITEM = ITEMS.register("void_cave_midcorner",
            () -> new BlockItem(VOIDMIDCORNER_CAVE.get(), new Item.Properties()));


    public static final RegistryObject<Item> VOIDMIDNETHER_ITEM = ITEMS.register("voidnether_mid",
            () -> new BlockItem(VOIDMIDNETHER.get(), new Item.Properties()));

    public static final RegistryObject<Block> VOIDCORNERNETHER = BLOCKS.register("voidnether_corner",
            () -> new VoidNetherCornerBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(20.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<Item> VOIDCORNERNETHER_ITEM = ITEMS.register("voidnether_corner",
            () -> new BlockItem(VOIDCORNERNETHER.get(), new Item.Properties()));


    public static final RegistryObject<Block> VOIDMIDCORNERNETHER = BLOCKS.register("voidnether_midcorner",
            () -> new VoidNetherMidCornerBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(20.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<Item> VOIDMIDCORNERNETHER_ITEM = ITEMS.register("voidnether_midcorner",
            () -> new BlockItem(VOIDMIDCORNERNETHER.get(), new Item.Properties()));

    public static final RegistryObject<Item> BLACKSTONE_PLANT_ITEM = ITEMS.register("blackstone_plant",
            () -> new BlockItem(BLACKSTONE_PLANT.get(), new Item.Properties()));

    public static final RegistryObject<Block> BLACKSTONE_AXON = BLOCKS.register("blackstone_axon",
            () -> new BlackstoneAxonBlock(BlockBehaviour.Properties.copy(Blocks.GRASS)
                    .lightLevel(state -> 10)
                    .instabreak()
                    .noOcclusion()));

    public static final RegistryObject<Item> BLACKSTONE_AXON_ITEM = ITEMS.register("blackstone_axon",
            () -> new BlockItem(BLACKSTONE_AXON.get(), new Item.Properties()));
    public static final RegistryObject<Block> ENTRANCE = BLOCKS.register("entrance",
            () -> new EntranceBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_WART_BLOCK)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()));

    public static final RegistryObject<Item> ENTRANCE_ITEM = ITEMS.register("entrance",
            () -> new BlockItem(ENTRANCE.get(), new Item.Properties()));


    public static final RegistryObject<Block> CRIMSON_WEB = BLOCKS.register("crimson_web",
            () -> new CrimsonWebBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_WART_BLOCK)
                    .instabreak()
                    .noOcclusion()));


    public static final RegistryObject<Item> CRIMSON_WEB_ITEM = ITEMS.register("crimson_web",
            () -> new GeoBlockItem(
                    CRIMSON_WEB.get(),
                    new Item.Properties(),
                    new ResourceLocation(MODID, "geo/crimson_web.geo.json"),
                    new ResourceLocation(MODID, "textures/block/crimson_web.png"),
                    new ResourceLocation(MODID, "animations/crimson_web.animation.json"),
                    new ResourceLocation(MODID, "textures/block/blackstone_column_emissive.png")
            ));

    public static final RegistryObject<Block> AZAZEL_TROPHY = BLOCKS.register("azazel_trophy",
            () -> new AzazelTrophyBlock(BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK)
                    .noOcclusion()));

    public static final RegistryObject<Item> AZAZEL_TROPHY_ITEM = ITEMS.register("azazel_trophy",
            () -> new AzazelTrophyItem(AZAZEL_TROPHY.get(), new Item.Properties()));

    public static final RegistryObject<CreativeModeTab> NETHERMAN_TAB = CREATIVE_MODE_TABS.register("netherman_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(AZAZEL_TROPHY_ITEM.get()))
                    .title(Component.translatable("creativetab.netherman_tab"))
                    .displayItems((parameters, output) -> {
                        for (RegistryObject<Item> item : ITEMS.getEntries()) {
                            output.accept(item.get());
                        }
                    })
                    .build()
    );

    public static final RegistryObject<Block> TRAPHIVE = BLOCKS.register("traphive",
            () -> new TraphiveBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_WART_BLOCK)
                    .instabreak()
                    .noOcclusion()));


    public static final RegistryObject<Item> TRAPHIVE_ITEM = ITEMS.register("traphive",
            () -> new GeoBlockItem(
                    TRAPHIVE.get(),
                    new Item.Properties(),
                    new ResourceLocation(MODID, "geo/traphive.geo.json"),
                    new ResourceLocation(MODID, "textures/block/traphive.png"),
                    new ResourceLocation(MODID, "animations/traphive.animation.json"),
                    new ResourceLocation(MODID, "textures/block/blackstone_column_emissive.png")
            ));

    public static final RegistryObject<Block> STATUE_STAND = BLOCKS.register("statue_stand",
            () -> new StatueStandBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_BRICKS)
                    .strength(5.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<Item> STATUE_STAND_ITEM = ITEMS.register("statue_stand",
            () -> new BlockItem(STATUE_STAND.get(), new Item.Properties()));

    public static final RegistryObject<Block> TOTEMUS = BLOCKS.register("totemus",
            () -> new TotemusBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_BRICKS)
                    .strength(5.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));


    public static final RegistryObject<Item> TOTEMUS_ITEM = ITEMS.register("totemus",
            () -> new BlockItem(TOTEMUS.get(), new Item.Properties()));


    public static final RegistryObject<Block> EYE = BLOCKS.register("eye_block",
            () -> new EyeBlock(BlockBehaviour.Properties.copy(Blocks.HONEY_BLOCK)
                    .strength(5.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));


    public static final RegistryObject<Item> EYE_ITEM = ITEMS.register("eye_block",
            () -> new GeoBlockItem(
                    EYE.get(),
                    new Item.Properties(),
                    new ResourceLocation(MODID, "geo/eye_block.geo.json"),
                    new ResourceLocation(MODID, "textures/block/eye_block.png"),
                    new ResourceLocation(MODID, "animations/eye_block.animation.json"),
                    new ResourceLocation(MODID, "textures/block/eye_block_emissive.png")
            ));

    public static final RegistryObject<Block> ALTAR = BLOCKS.register("altar",
            () -> new AltarBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE)
                    .lightLevel(state -> state.getValue(AltarBlock.LIT) ? 10 : 0)
                    .strength(10.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<Item> ALTAR_ITEM = ITEMS.register("altar",
            () -> new GeoBlockItem(
                    ALTAR.get(),
                    new Item.Properties(),
                    new ResourceLocation(MODID, "geo/altar.geo.json"),
                    new ResourceLocation(MODID, "textures/block/altar.png"),
                    new ResourceLocation(MODID, "animations/altar.animation.json"),
                    new ResourceLocation(MODID, "textures/block/altar_emissive.png")
            ));


    public static final RegistryObject<Block> MOSAIC_CHURCH = BLOCKS.register("mosaic_church",
            () -> new MosaicChurchBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)
                    .lightLevel(state -> 10)
                    .instabreak()
                    .noOcclusion()));


    public static final RegistryObject<Item> MOSAIC_CHURCH_ITEM = ITEMS.register("mosaic_church",
            () -> new BlockItem(MOSAIC_CHURCH.get(), new Item.Properties()));
    public static final RegistryObject<Item> MANIPULATOR_STICK = ITEMS.register("manipulator_stick",
            () -> new com.benji.netherman.item.ManipulatorStickItem());

    public static final RegistryObject<Item> CHANCE_TOTEM = ITEMS.register("chance_totem",
            () -> new com.benji.netherman.item.ChanceTotemItem(new Item.Properties()));

    public static final RegistryObject<Item> NOTE = ITEMS.register("note",
            () -> new com.benji.netherman.item.NoteItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Block> GRAND_DOOR = BLOCKS.register("grand_door",
            () -> new GrandDoorBlock(BlockBehaviour.Properties.copy(Blocks.POLISHED_BLACKSTONE_BRICKS)
                    .strength(20.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));
    public static final RegistryObject<Item> GRAND_DOOR_ITEM = ITEMS.register("grand_door",
            () -> new GeoBlockItem(
                    GRAND_DOOR.get(),
                    new Item.Properties(),
                    new ResourceLocation(MODID, "geo/grand_door.geo.json"),
                    new ResourceLocation(MODID, "textures/block/grand_door.png"),
                    new ResourceLocation(MODID, "animations/grand_door.animation.json"),
                    new ResourceLocation(MODID, "textures/block/grand_door_emissive.png")
            ));

    public static final RegistryObject<Item> MUSIC_DISC_BOSS = ITEMS.register("music_disc_boss",
            () -> new RecordItem(15, ModSounds.BOSS_FIGHT,
                    new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE), 2900)); 

    public static final RegistryObject<Item> MUSIC_DISC_QUAR = ITEMS.register("music_disc_quar",
            () -> new RecordItem(6, ModSounds.CAVE_AMBIENT,
                    new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE), 660));

    public static final RegistryObject<Item> MUSIC_DISC_MAZE = ITEMS.register("music_disc_maze",
            () -> new RecordItem(7, ModSounds.MAZE_AMBIENT,
                    new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE), 720));

    public static final RegistryObject<Item> MUSIC_DISC_SACRED = ITEMS.register("music_disc_sacred",
            () -> new RecordItem(8, ModSounds.CITY_AMBIENT,
                    new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE), 700));

    public static final RegistryObject<Item> MUSIC_DISC_AZAZEL = ITEMS.register("music_disc_azazel",
            () -> new RecordItem(15, ModSounds.CHURCH_AMBIENT,
                    new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE), 360));

    public static final RegistryObject<Block> MAZE_DOOR = BLOCKS.register("maze_door",
            () -> new MazeDoorBlock(BlockBehaviour.Properties.copy(Blocks.POLISHED_BLACKSTONE_BRICKS)
                    .strength(20.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<Item> MAZE_DOOR_ITEM = ITEMS.register("maze_door",
            () -> new GeoBlockItem(
                    MAZE_DOOR.get(),
                    new Item.Properties(),
                    new ResourceLocation(MODID, "geo/maze_door.geo.json"),
                    new ResourceLocation(MODID, "textures/block/maze_door.png"),
                    new ResourceLocation(MODID, "animations/maze_door.animation.json"),
                    new ResourceLocation(MODID, "textures/block/blackstone_column_emissive.png")
            ));

    public static final RegistryObject<Block> GRAND_DOOR_PART = BLOCKS.register("grand_door_part",
            () -> new GrandDoorPartBlock(BlockBehaviour.Properties.copy(Blocks.POLISHED_BLACKSTONE_BRICKS)
                    .strength(500.0F, 500.0F)
                    .noOcclusion()
                    .noLootTable()));
    public static final RegistryObject<Block> CRIMSON_HONEY_BLOCK = BLOCKS.register("crimson_honey_block",
            () -> new CrimsonHoneyBlock(BlockBehaviour.Properties.copy(Blocks.HONEY_BLOCK)
                    .instabreak()
                    .lightLevel(state -> 5)
                    .noOcclusion()));

    public static final RegistryObject<Item> CRIMSON_HONEY_BLOCK_ITEM = ITEMS.register("crimson_honey_block",
            () -> new BlockItem(CRIMSON_HONEY_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> CRIMSON_ARROW_ITEM = ITEMS.register("crimson_arrow",
            () -> new com.benji.netherman.item.CrimsonArrowItem(new Item.Properties()));
    public static final RegistryObject<EntityType<com.benji.netherman.entity.CrimsonArrowEntity>> CRIMSON_ARROW_ENTITY = ENTITIES.register("crimson_arrow",
            () -> EntityType.Builder.<com.benji.netherman.entity.CrimsonArrowEntity>of(com.benji.netherman.entity.CrimsonArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("crimson_arrow"));


    public static final RegistryObject<Block> GHASTLY_NEST = BLOCKS.register("ghastly_nest",
            () -> new GhastlyNestBlock(BlockBehaviour.Properties.copy(Blocks.BEEHIVE)
                    .strength(2.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));
    public static final RegistryObject<Item> GHASTLY_NEST_ITEM = ITEMS.register("ghastly_nest",
            () -> new BlockItem(GHASTLY_NEST.get(), new Item.Properties()));

    public static final RegistryObject<Item> CRIMSON_HONEY_BOTTLE = ITEMS.register("crimson_honey_bottle",
            () -> new com.benji.netherman.item.CrimsonHoneyBottleItem(new Item.Properties()
                    .stacksTo(16) 
                    .craftRemainder(net.minecraft.world.item.Items.GLASS_BOTTLE)
                    .food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(6).saturationMod(0.1F).alwaysEat().build())));

    public static final RegistryObject<Item> QUEST_ICON_1 = ITEMS.register("locker_quest1", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> QUEST_ICON_2 = ITEMS.register("locker_quest2", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> QUEST_ICON_3 = ITEMS.register("locker_quest3", () -> new Item(new Item.Properties()));

    public static final RegistryObject<BlockEntityType<GhastlyNestBlockEntity>> GHASTLY_NEST_BE = BLOCK_ENTITIES.register("ghastly_nest",
            () -> BlockEntityType.Builder.of(GhastlyNestBlockEntity::new, GHASTLY_NEST.get()).build(null));

    public static final RegistryObject<BlockEntityType<VoidNetherMidCornerBlockEntity>> VOIDMIDCORNERNETHER_BE = BLOCK_ENTITIES.register("voidnether_midcorner",
            () -> BlockEntityType.Builder.of(VoidNetherMidCornerBlockEntity::new, VOIDMIDCORNERNETHER.get()).build(null));

    public static final RegistryObject<BlockEntityType<VoidNetherCornerBlockEntity>> VOIDCORNERNETHER_BE = BLOCK_ENTITIES.register("voidnether_corner",
            () -> BlockEntityType.Builder.of(VoidNetherCornerBlockEntity::new, VOIDCORNERNETHER.get()).build(null));

    public static final RegistryObject<BlockEntityType<VoidNetherMidBlockEntity>> VOIDMIDNETHER_BE = BLOCK_ENTITIES.register("voidnether_mid",
            () -> BlockEntityType.Builder.of(VoidNetherMidBlockEntity::new, VOIDMIDNETHER.get()).build(null));

    public static final RegistryObject<BlockEntityType<EyeBlockEntity>> EYE_BE = BLOCK_ENTITIES.register("eye_block",
            () -> BlockEntityType.Builder.of(EyeBlockEntity::new, EYE.get()).build(null));

    public static final RegistryObject<BlockEntityType<LabyrinthTeleportBlockEntity>> LABYRINTH_TELEPORT_BE = BLOCK_ENTITIES.register("labyrinth_teleport",
            () -> BlockEntityType.Builder.of(LabyrinthTeleportBlockEntity::new, LABYRINTH_TELEPORT.get()).build(null));

    public static final RegistryObject<BlockEntityType<AltarBlockEntity>> ALTAR_BE = BLOCK_ENTITIES.register("altar",
            () -> BlockEntityType.Builder.of(AltarBlockEntity::new, ALTAR.get()).build(null));

    public static final RegistryObject<BlockEntityType<TotemusHoleBlockEntity>> TOTEMUS_HOLE_BE = BLOCK_ENTITIES.register("totemus_hole",
            () -> BlockEntityType.Builder.of(TotemusHoleBlockEntity::new, TOTEMUS_HOLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TotemusBlockEntity>> TOTEMUS_BE = BLOCK_ENTITIES.register("totemus",
            () -> BlockEntityType.Builder.of(TotemusBlockEntity::new, TOTEMUS.get()).build(null));

    public static final RegistryObject<BlockEntityType<TraphiveBlockEntity>> TRAPHIVE_BE = BLOCK_ENTITIES.register("traphive",
            () -> BlockEntityType.Builder.of(TraphiveBlockEntity::new, TRAPHIVE.get()).build(null));


    public static final RegistryObject<BlockEntityType<GrandDoorBlockEntity>> GRAND_DOOR_BE = BLOCK_ENTITIES.register("grand_door",
            () -> BlockEntityType.Builder.of(GrandDoorBlockEntity::new, GRAND_DOOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<MazeDoorBlockEntity>> MAZE_DOOR_BE = BLOCK_ENTITIES.register("maze_door",
            () -> BlockEntityType.Builder.of(MazeDoorBlockEntity::new, MAZE_DOOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<NetherSpawnerBlockEntity>> NETHER_SPAWNER_BE = BLOCK_ENTITIES.register("nether_spawner",
            () -> BlockEntityType.Builder.of(NetherSpawnerBlockEntity::new, NETHER_SPAWNER.get()).build(null));


    public static final RegistryObject<BlockEntityType<PointedBlackstoneBlockEntity>> POINTED_BLACKSTONE_BE = BLOCK_ENTITIES.register("pointed_blackstone",
            () -> BlockEntityType.Builder.of(PointedBlackstoneBlockEntity::new, POINTED_BLACKSTONE.get()).build(null));
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);
    public static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);

    public static final RegistryObject<MobEffect> MANIPULATION_EFFECT = EFFECTS.register("manipulation", ManipulationEffect::new);
    public static final RegistryObject<MobEffect> FEAR_EFFECT = EFFECTS.register("fear", () -> new ZoneEffect(0x000000));
    public static final RegistryObject<MobEffect> EXCITEMENT_EFFECT = EFFECTS.register("excitement", () -> new ZoneEffect(0xFF0000));
    public static final RegistryObject<MobEffect> FAITH_EFFECT = EFFECTS.register("faith", () -> new ZoneEffect(0x800080));
    public static final RegistryObject<MobEffect> ANXIETY_EFFECT = EFFECTS.register("anxiety", () -> new ZoneEffect(0x8B0000));
    public static final RegistryObject<MobEffect> ALERTNESS_EFFECT = EFFECTS.register("alertness", () -> new ZoneEffect(0x8B0000));
    public static final RegistryObject<net.minecraft.world.item.crafting.RecipeSerializer<?>> CRIMSON_ARROW_CRAFTING = RECIPE_SERIALIZERS.register("crimson_arrow_coating",
            () -> new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(com.benji.netherman.item.crafting.CrimsonArrowRecipe::new));


    public static final RegistryObject<EntityType<AzazelEntity>> AZAZEL = ENTITIES.register("azazel",
            () -> EntityType.Builder.of(AzazelEntity::new, MobCategory.MONSTER)
                    .sized(3.0F, 4.5F)
                    .fireImmune()
                    .build(new ResourceLocation(MODID, "azazel").toString()));

    public static final RegistryObject<EntityType<AzazelHumanEntity>> AZAZEL_HUMAN = ENTITIES.register("azazel_human",
            () -> EntityType.Builder.of(AzazelHumanEntity::new, MobCategory.MONSTER)
                    .sized(2.5F, 12.75F)
                    .fireImmune()
                    .build(new ResourceLocation(MODID, "azazel_human").toString()));


    public static final RegistryObject<EntityType<AzazelSplashEntity>> SPLASH_ENTITY = ENTITIES.register("azazel_splash",
            () -> EntityType.Builder.<AzazelSplashEntity>of(AzazelSplashEntity::new, MobCategory.MISC)
                    .sized(3.5F, 6.5F).clientTrackingRange(64).build("azazel_splash"));

    public static final RegistryObject<EntityType<AzazelSpikesProjectileEntity>> SPIKE_PROJECTILE_ENTITY = ENTITIES.register("azazel_spike_projectile",
            () -> EntityType.Builder.<AzazelSpikesProjectileEntity>of(AzazelSpikesProjectileEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.5F).clientTrackingRange(64).build("azazel_spike_projectile"));

    public static final RegistryObject<EntityType<AzazelSpikeEntity>> SPIKE_ENTITY = ENTITIES.register("azazel_spike",
            () -> EntityType.Builder.<AzazelSpikeEntity>of(AzazelSpikeEntity::new, MobCategory.MISC)
                    .sized(0.75F, 3.125F).clientTrackingRange(64).build("azazel_spike"));

    public static final RegistryObject<EntityType<GildedGolemEntity>> GILDED_GOLEM = ENTITIES.register("gilded_golem",
            () -> EntityType.Builder.of(GildedGolemEntity::new, MobCategory.MISC)
                    .sized(1.4F, 2.7F)
                    .fireImmune()
                    .build(new net.minecraft.resources.ResourceLocation(MODID, "gilded_golem").toString()));

    public static final RegistryObject<EntityType<StatueBossunitEntity>> STATUE_BOSSUNIT = ENTITIES.register("statue_bossunit",
            () -> EntityType.Builder.of(StatueBossunitEntity::new, MobCategory.MONSTER)
                    .sized(0.625F, 2.125F)
                    .fireImmune()
                    .build(new ResourceLocation(MODID, "statue_bossunit").toString()));

    public static final RegistryObject<EntityType<TotemusPuzzleEntity>> TOTEMUS_PUZZLE = ENTITIES.register("totemus_puzzle",
            () -> EntityType.Builder.of(TotemusPuzzleEntity::new, MobCategory.MONSTER)
                    .sized(0.5F, 2.25F)
                    .fireImmune()
                    .build(new ResourceLocation(MODID, "totemus_puzzle").toString()));

    public static final RegistryObject<EntityType<LaserEntity>> LASER = ENTITIES.register("laser",
            () -> EntityType.Builder.of(LaserEntity::new, MobCategory.MISC)
                    .sized(3.0F, 18.75F)
                    .fireImmune()
                    .build(new ResourceLocation(MODID, "laser").toString()));

    public static final RegistryObject<EntityType<StatueEntity>> STATUE = ENTITIES.register("statue_entity",
            () -> EntityType.Builder.of(StatueEntity::new, MobCategory.MONSTER)
                    .sized(0.625F, 2.125F)
                    .fireImmune()
                    .build(new ResourceLocation(MODID, "statue_entity").toString()));

    public static final RegistryObject<EntityType<BellGuardianEntity>> BELL_GUARDIAN = ENTITIES.register("bell_guardian",
            () -> EntityType.Builder.of(BellGuardianEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .build(new ResourceLocation(MODID, "bell_guardian").toString()));

    public static final RegistryObject<EntityType<TraderEntity>> TRADER = ENTITIES.register("trader",
            () -> EntityType.Builder.of(TraderEntity::new, MobCategory.CREATURE)
                    .sized(1.125F, 1.5F)
                    .build(new ResourceLocation(MODID, "trader").toString()));

    public static final RegistryObject<EntityType<DoctorEntity>> DOCTOR = ENTITIES.register("doctor",
            () -> EntityType.Builder.of(DoctorEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(new ResourceLocation(MODID, "doctor").toString()));

    public static final RegistryObject<EntityType<BlacksmithEntity>> BLACKSMITH = ENTITIES.register("blacksmith",
            () -> EntityType.Builder.of(BlacksmithEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(new ResourceLocation(MODID, "blacksmith").toString()));


    public static final RegistryObject<EntityType<BelieverEntity>> BELIEVER = ENTITIES.register("believer",
            () -> EntityType.Builder.of(BelieverEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(new ResourceLocation(MODID, "believer").toString()));

    public static final RegistryObject<EntityType<BelieverVillagerEntity>> BELIEVER_VILLAGER = ENTITIES.register("believer_villager",
            () -> EntityType.Builder.of(BelieverVillagerEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(new ResourceLocation(MODID, "believer_villager").toString()));

    public static final RegistryObject<EntityType<VillagerPrisonerEntity>> VILLAGER_PRISONER = ENTITIES.register("villager_prisoner",
            () -> EntityType.Builder.of(VillagerPrisonerEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build("villager_prisoner"));

    public static final RegistryObject<Item> ALTAR_COMPASS_KEY = ITEMS.register("altar_compass_key",
            () -> new com.benji.netherman.item.AltarCompassKeyItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> AZAZEL_GUIDE_BOOK_ITEM = ITEMS.register("azazel_guide_book",
            () -> new AzazelGuideBookItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> MAZE_KEY = ITEMS.register("maze_key",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<EntityType<AzazelGuideBookEntity>> AZAZEL_GUIDE_BOOK_ENTITY = ENTITIES.register("azazel_guide_book",
            () -> EntityType.Builder.of(AzazelGuideBookEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .fireImmune()
                    .build("azazel_guide_book"));


    public static final RegistryObject<EntityType<PiglinPrisonerEntity>> PIGLIN_PRISONER = ENTITIES.register("piglin_prisoner",
            () -> EntityType.Builder.of(PiglinPrisonerEntity::new, MobCategory.CREATURE) 
                    .sized(0.6F, 1.95F)
                    .build("piglin_prisoner"));
    public static final RegistryObject<EntityType<ManipulatorEntity>> MANIPULATOR = ENTITIES.register("manipulator",
            () -> EntityType.Builder.of(ManipulatorEntity::new, MobCategory.MONSTER)
                    .fireImmune()
                    .sized(0.9375F, 2.125F)
                    .build("manipulator"));

    public static final RegistryObject<EntityType<WelcomerEntity>> WELCOMER = ENTITIES.register("welcomer",
            () -> EntityType.Builder.of(WelcomerEntity::new, MobCategory.MONSTER)
                    .sized(0.625f, 2.25f)
                    .build("welcomer"));
    public static final RegistryObject<EntityType<GuardianEntity>> GUARDIAN = ENTITIES.register("guardian",
            () -> EntityType.Builder.of(GuardianEntity::new, MobCategory.MONSTER)
                    .sized(1.0f, 5.125f)
                    .fireImmune()
                    .build("guardian"));

    public static final RegistryObject<EntityType<GhastlyEntity>> GHASTLY = ENTITIES.register("ghastly",
            () -> EntityType.Builder.of(GhastlyEntity::new, MobCategory.CREATURE)
                    .sized(0.625f, 0.8125f)
                    .fireImmune()
                    .build("ghastly"));

    public static final RegistryObject<Item> GILDED_GOLEM_SPAWN_EGG = ITEMS.register("gilded_golem_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(
                    GILDED_GOLEM,
                    0xFFFFFF,
                    0xFFFFFF,
                    new Item.Properties()
            ));

    public static final RegistryObject<Item> GHASTLY_SPAWN_EGG = ITEMS.register("ghastly_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(
                    GHASTLY,
                    0xFFFFFF,
                    0xFFFFFF,
                    new Item.Properties()
            ));

    public static final RegistryObject<Item> GUARDIAN_SPAWN_EGG = ITEMS.register("guardian_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(
                    GUARDIAN,
                    0xFFFFFF,
                    0xFFFFFF,
                    new Item.Properties()
            ));

    public static final RegistryObject<Item> BELIEVER_SPAWN_EGG = ITEMS.register("believer_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(
                    BELIEVER,
                    0xFFFFFF,
                    0xFFFFFF,
                    new Item.Properties()
            ));

    public static final RegistryObject<Item> BELIEVER_VILLAGER_SPAWN_EGG = ITEMS.register("believer_villager_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(
                    BELIEVER_VILLAGER,
                    0xFFFFFF,
                    0xFFFFFF,
                    new Item.Properties()
            ));

    public static final RegistryObject<Item> BLACKSMITH_SPAWN_EGG = ITEMS.register("blacksmith_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(
                    BLACKSMITH,
                    0xFFFFFF,
                    0xFFFFFF,
                    new Item.Properties()
            ));

    public static final RegistryObject<Item> DOCTOR_SPAWN_EGG = ITEMS.register("doctor_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(
                    DOCTOR,
                    0xFFFFFF,
                    0xFFFFFF,
                    new Item.Properties()
            ));

    public static final RegistryObject<Item> MANIPULATOR_SPAWN_EGG = ITEMS.register("manipulator_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(
                    MANIPULATOR,
                    0xFFFFFF,
                    0xFFFFFF,
                    new Item.Properties()
            ));

    public static final RegistryObject<Item> STATUE_BOSSUNIT_SPAWN_EGG = ITEMS.register("statue_bossunit_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(
                    STATUE_BOSSUNIT,
                    0xFFFFFF,
                    0xFFFFFF,
                    new Item.Properties()
            ));

    public static final RegistryObject<Item> STATUE_SPAWN_EGG = ITEMS.register("statue_entity_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(
                    STATUE,
                    0xFFFFFF,
                    0xFFFFFF,
                    new Item.Properties()
            ));

    public static final RegistryObject<Item> TRADER_SPAWN_EGG = ITEMS.register("trader_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(
                    TRADER,
                    0xFFFFFF,
                    0xFFFFFF,
                    new Item.Properties()
            ));

    public static final RegistryObject<Item> AZAZEL_SPAWN_EGG = ITEMS.register("azazel_spawn_egg",
            () -> new net.minecraftforge.common.ForgeSpawnEggItem(
                    AZAZEL,
                    0xFFFFFF,
                    0xFFFFFF,
                    new Item.Properties()
            ));

    public NetherExp(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        context.registerConfig(ModConfig.Type.COMMON, AzazelConfig.SPEC);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ENTITIES.register(modEventBus);
        EFFECTS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        STRUCTURE_TYPES.register(modEventBus);
        PAINTING_VARIANTS.register(modEventBus);

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModMessages.register();
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS || event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(NETHER_SPAWNER_ITEM);
            event.accept(GRAND_DOOR_ITEM);
            event.accept(BLACKSTONE_COLUMN_ITEM);
            event.accept(ENTRANCE_ITEM);
            event.accept(AZAZEL_TROPHY_ITEM);
            event.accept(CRIMSON_WEB_ITEM);
            event.accept(TRAPHIVE_ITEM);
            event.accept(VOIDMID_ITEM);
            event.accept(VOIDMID_CAVE_ITEM);
            event.accept(VOIDCORNER_CAVE_ITEM);
            event.accept(VOIDMIDCORNER_CAVE_ITEM);
            event.accept(VOIDCORNER_ITEM);
            event.accept(VOIDMIDCORNER_ITEM);
            event.accept(VOIDMIDNETHER_ITEM);
            event.accept(VOIDCORNERNETHER_ITEM);
            event.accept(MOSAIC_CHURCH_ITEM);
            event.accept(STATUE_STAND_ITEM);
            event.accept(TOTEMUS_ITEM);
            event.accept(EYE_ITEM);
            event.accept(ALTAR_ITEM);
            event.accept(LABYRINTH_TELEPORT_ITEM);
            event.accept(VOIDMIDCORNERNETHER_ITEM);
            event.accept(SAMSONIT_ITEM);
            event.accept(SAMSONIT_BRICKS_ITEM);
            event.accept(SAMSONIT_TILES_ITEM);
            event.accept(POLISHED_SAMSONIT_ITEM);
            event.accept(LABYRINTH_BELLSPAWN_ITEM);
            event.accept(COBBLED_SAMSONIT_ITEM);
            event.accept(CHISELED_SAMSONIT_ITEM);
            event.accept(COBBLED_SAMSONIT_SLAB_ITEM);
            event.accept(COBBLED_SAMSONIT_STAIRS_ITEM);
            event.accept(COBBLED_SAMSONIT_WALL_ITEM);
            event.accept(POLISHED_SAMSONIT_SLAB_ITEM);
            event.accept(TOTEMUS_HOLE_ITEM);
            event.accept(MAZE_DOOR_ITEM);
            event.accept(POLISHED_SAMSONIT_STAIRS_ITEM);
            event.accept(POLISHED_SAMSONIT_WALL_ITEM);
            event.accept(SAMSONIT_BRICKS_SLAB_ITEM);
            event.accept(SAMSONIT_BRICKS_STAIRS_ITEM);
            event.accept(SAMSONIT_BRICKS_WALL_ITEM);
            event.accept(SAMSONIT_EYE_ITEM);
            event.accept(SAMSONIT_BELL_ITEM);
            event.accept(LOCKER_NETHER_ITEM);
            event.accept(SAMSONIT_KEY_ITEM);
            event.accept (A_PUZZLE_ITEM);
            event.accept (Z_PUZZLE_ITEM);
            event.accept (E_PUZZLE_ITEM);
            event.accept (L_PUZZLE_ITEM);

        }
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(CRIMSON_HONEY_BOTTLE);
        }
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(GILDED_GOLEM_SPAWN_EGG);
            event.accept(GHASTLY_SPAWN_EGG);
            event.accept(GUARDIAN_SPAWN_EGG);
            event.accept(BELIEVER_SPAWN_EGG);
            event.accept(BELIEVER_VILLAGER_SPAWN_EGG);
            event.accept(BLACKSMITH_SPAWN_EGG);
            event.accept(DOCTOR_SPAWN_EGG);
            event.accept(MANIPULATOR_SPAWN_EGG);
            event.accept(STATUE_BOSSUNIT_SPAWN_EGG);
            event.accept(STATUE_SPAWN_EGG);
            event.accept(TRADER_SPAWN_EGG);
            event.accept(AZAZEL_SPAWN_EGG);
        }
        if (event.getTabKey() == CreativeModeTabs.COMBAT || event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(MANIPULATOR_STICK);
            event.accept(CRIMSON_ARROW_ITEM);
            event.accept(CHANCE_TOTEM);
            event.accept(NOTE);
            event.accept(MUSIC_DISC_AZAZEL);
            event.accept(MUSIC_DISC_BOSS);
            event.accept(MUSIC_DISC_QUAR);
            event.accept(MUSIC_DISC_SACRED);
            event.accept(MUSIC_DISC_MAZE);
            event.accept(MAZE_KEY);
            event.accept(AZAZEL_GUIDE_BOOK_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(BLACKSTONE_PLANT_ITEM);
            event.accept(BLACKSTONE_AXON_ITEM);
            event.accept(CRIMSON_HONEY_BLOCK_ITEM);
            event.accept(GHASTLY_NEST_ITEM);
            event.accept(POINTED_BLACKSTONE_ITEM);
            event.accept(POTENT_MAGMA_ITEM);
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(AZAZEL_GUIDE_BOOK_ENTITY.get(), AzazelGuideBookEntity.createAttributes().build());
            event.put(GILDED_GOLEM.get(), GildedGolemEntity.createAttributes().build());
            event.put(BELL_GUARDIAN.get(), BellGuardianEntity.createAttributes().build());
            event.put(TOTEMUS_PUZZLE.get(), TotemusPuzzleEntity.createAttributes().build());
            event.put(AZAZEL.get(), AzazelEntity.createAttributes().build());
            event.put(AZAZEL_HUMAN.get(), AzazelHumanEntity.createAttributes().build());
            event.put(LASER.get(), LaserEntity.createAttributes().build());
            event.put(STATUE_BOSSUNIT.get(), StatueBossunitEntity.createAttributes().build());
            event.put(BLACKSMITH.get(), BlacksmithEntity.createAttributes().build());
            event.put(DOCTOR.get(), DoctorEntity.createAttributes().build());
            event.put(TRADER.get(), TraderEntity.createAttributes().build());
            event.put(STATUE.get(), StatueEntity.createAttributes().build());
            event.put(BELIEVER.get(), BelieverEntity.createAttributes().build());
            event.put(BELIEVER_VILLAGER.get(), BelieverVillagerEntity.createAttributes().build());
            event.put(PIGLIN_PRISONER.get(), PiglinPrisonerEntity.createAttributes().build());
            event.put(VILLAGER_PRISONER.get(), VillagerPrisonerEntity.createAttributes().build());
            event.put(MANIPULATOR.get(), ManipulatorEntity.createAttributes().build());
            event.put(WELCOMER.get(), WelcomerEntity.createAttributes().build());
            event.put(GHASTLY.get(), GhastlyEntity.createAttributes().build());
            event.put(GUARDIAN.get(), GuardianEntity.createAttributes().build());
        }
    }
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                net.minecraft.client.renderer.item.ItemProperties.register(
                        NetherExp.AZAZEL_TROPHY_ITEM.get(),
                        new net.minecraft.resources.ResourceLocation(NetherExp.MODID, "stage"),
                        com.benji.netherman.item.AzazelTrophyItem::getMaskStageProperty
                );

                ItemProperties.register(NetherExp.ALTAR_COMPASS_KEY.get(), new ResourceLocation("angle"), (stack, level, entity, seed) -> {
                    if (entity == null && !stack.isFramed()) return 0.0F;

                    if (!stack.getOrCreateTag().contains("TargetX")) {
                        return (float) ((System.currentTimeMillis() % 4000L) / 4000.0);
                    }

                    double targetX = stack.getTag().getInt("TargetX");
                    double targetZ = stack.getTag().getInt("TargetZ");

                    Entity player = entity != null ? entity : stack.getFrame();
                    if (player == null) return 0.0F;

                    double targetYaw = Math.toDegrees(Math.atan2(targetZ - player.getZ(), targetX - player.getX())) - 90.0;

                    double playerYaw = player.getYRot();

                    double relativeYaw = targetYaw - playerYaw;

                    double angle = 0.5 + (relativeYaw / 360.0);

                    return (float) net.minecraft.util.Mth.positiveModulo(angle, 1.0D);
                });
            });
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(NetherExp.LABYRINTH_TELEPORT_BE.get(), LabyrinthTeleportRenderer::new);
            event.registerBlockEntityRenderer(VOIDMIDCORNERNETHER_BE.get(), VoidNetherMidCornerRenderer::new);
            event.registerBlockEntityRenderer(VOIDCORNERNETHER_BE.get(), VoidNetherCornerRenderer::new);
            event.registerBlockEntityRenderer(VOIDMIDNETHER_BE.get(), VoidNetherMidRenderer::new);
            event.registerBlockEntityRenderer(TRAPHIVE_BE.get(), TraphiveRenderer::new);
            event.registerBlockEntityRenderer(EYE_BE.get(), EyeRenderer::new);
            event.registerBlockEntityRenderer(ALTAR_BE.get(), AltarRenderer::new);
            event.registerBlockEntityRenderer(GRAND_DOOR_BE.get(), GrandDoorRenderer::new);
            event.registerBlockEntityRenderer(SAMSONITE_BELL_BE.get(), SamsoniteBellRenderer::new);
            event.registerEntityRenderer(BELL_GUARDIAN.get(), BellGuardianRenderer::new);
            event.registerBlockEntityRenderer(MAZE_DOOR_BE.get(), MazeDoorRenderer::new);
            event.registerBlockEntityRenderer(POINTED_BLACKSTONE_BE.get(), PointedBlackstoneRenderer::new);
            event.registerEntityRenderer(GILDED_GOLEM.get(), GildedGolemRenderer::new);
            event.registerEntityRenderer(TOTEMUS_PUZZLE.get(), TotemusPuzzleRenderer::new);
            event.registerBlockEntityRenderer(FACE_PUZZLE_LEFT_DOWN_BE.get(), FacePuzzleLeftDownRenderer::new);
            event.registerBlockEntityRenderer(FACE_PUZZLE_RIGHT_DOWN_BE.get(), FacePuzzleRightDownRenderer::new);
            event.registerBlockEntityRenderer(FACE_PUZZLE_LEFT_UP_BE.get(), FacePuzzleLeftUpRenderer::new);
            event.registerBlockEntityRenderer(FACE_PUZZLE_RIGHT_UP_BE.get(), FacePuzzleRightUpRenderer::new);
            event.registerEntityRenderer(CRIMSON_ARROW_ENTITY.get(), com.benji.netherman.client.renderer.entity.CrimsonArrowRenderer::new);
            event.registerEntityRenderer(AZAZEL.get(), AzazelRenderer::new);
            event.registerEntityRenderer(AZAZEL_HUMAN.get(), AzazelHumanRenderer::new);
            event.registerEntityRenderer(SPLASH_ENTITY.get(), AzazelSplashRenderer::new);
            event.registerEntityRenderer(SPIKE_PROJECTILE_ENTITY.get(), AzazelSpikeProjectileRenderer::new);
            event.registerEntityRenderer(SPIKE_ENTITY.get(), AzazelSpikeRenderer::new);
            event.registerEntityRenderer(LASER.get(), LaserRenderer::new);
            event.registerEntityRenderer(STATUE_BOSSUNIT.get(), StatueBossunitRenderer::new);
            event.registerEntityRenderer(BLACKSMITH.get(), BlacksmithRenderer::new);
            event.registerEntityRenderer(DOCTOR.get(), DoctorRenderer::new);
            event.registerEntityRenderer(TRADER.get(), TraderRenderer::new);
            event.registerEntityRenderer(STATUE.get(), StatueRenderer::new);
            event.registerEntityRenderer(BELIEVER_VILLAGER.get(), BelieverVillagerRenderer::new);
            event.registerEntityRenderer(BELIEVER.get(), BelieverRenderer::new);
            event.registerEntityRenderer(AZAZEL_GUIDE_BOOK_ENTITY.get(), AzazelGuideBookRenderer::new);
            event.registerEntityRenderer(PIGLIN_PRISONER.get(), PiglinPrisonerRenderer::new);
            event.registerEntityRenderer(VILLAGER_PRISONER.get(), VillagerPrisonerRenderer::new);
            event.registerEntityRenderer(MANIPULATOR.get(), ManipulatorRenderer::new);
            event.registerEntityRenderer(WELCOMER.get(), WelcomerRenderer::new);
            event.registerEntityRenderer(GUARDIAN.get(), GuardianRenderer::new);
            event.registerEntityRenderer(GHASTLY.get(), GhastlyRenderer::new);
        }
        @SubscribeEvent
        public static void registerGuiOverlays(net.minecraftforge.client.event.RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("manipulation_overlay", ManipulationOverlay.HUD_OVERLAY);
        }
    }
}