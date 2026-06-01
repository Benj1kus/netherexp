package com.benji.netherman;

import com.benji.netherman.block.*;
import com.benji.netherman.block.entity.*;
import com.benji.netherman.client.ManipulationOverlay;
import com.benji.netherman.client.renderer.*;
import com.benji.netherman.client.renderer.entity.GhastlyRenderer;
import com.benji.netherman.client.renderer.entity.GuardianRenderer;
import com.benji.netherman.effect.ManipulationEffect;
import com.benji.netherman.effect.ZoneEffect;
import com.benji.netherman.entity.*;
import com.benji.netherman.entity.BelieverEntity;
import com.benji.netherman.item.GeoBlockItem;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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

    // REGISTER
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

    // BLOCK & ITEMS
    public static final RegistryObject<Block> NETHER_SPAWNER = BLOCKS.register("nether_spawner",
            () -> new NetherSpawnerBlock(BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK)
                    .lightLevel(state -> 15) // Излучает свет
                    .requiresCorrectToolForDrops()
                    .strength(2.0F)
                    .noOcclusion())); // Рекомендуется для кастомных рендеров, чтобы соседние блоки не становились прозрачными

    // 2. Регистрируем предмет-блок через наш GeoBlockItem
    public static final RegistryObject<Item> NETHER_SPAWNER_ITEM = ITEMS.register("nether_spawner",
            () -> new GeoBlockItem(
                    NETHER_SPAWNER.get(),
                    new Item.Properties(),
                    new ResourceLocation(MODID, "geo/nether_spawner.geo.json"),
                    new ResourceLocation(MODID, "textures/block/nether_spawner.png"),
                    new ResourceLocation(MODID, "animations/empty.animation.json"), // можно передать null, если анимаций пока нет
                    new ResourceLocation(MODID, "textures/block/nether_spawner_emissive.png") // Текстура свечения!
            ));

    public static final RegistryObject<Block> BLACKSTONE_COLUMN = BLOCKS.register("blackstone_column",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
                    .strength(2.0F)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Item> BLACKSTONE_COLUMN_ITEM = ITEMS.register("blackstone_column",
            () -> new BlockItem(BLACKSTONE_COLUMN.get(), new Item.Properties()));


    // Внутри регистрации BLOCKS:
    public static final RegistryObject<Block> POINTED_BLACKSTONE = BLOCKS.register("pointed_blackstone",
            () -> new PointedBlackstoneBlock(BlockBehaviour.Properties.copy(Blocks.POINTED_DRIPSTONE)
                    .requiresCorrectToolForDrops()
                    .strength(1.0F)
                    .noOcclusion()));

    public static final RegistryObject<Item> POINTED_BLACKSTONE_ITEM = ITEMS.register("pointed_blackstone",
            () -> new BlockItem(POINTED_BLACKSTONE.get(), new Item.Properties()));

    //PLANTS

    public static final RegistryObject<Block> BLACKSTONE_PLANT = BLOCKS.register("blackstone_plant",
            () -> new BlackstonePlantBlock(BlockBehaviour.Properties.copy(Blocks.GRASS)
                    .lightLevel(state -> 10)
                    .instabreak()
                    .noOcclusion()));

// VOID
    public static final RegistryObject<Block> VOIDMID = BLOCKS.register("void_mid",
            () -> new VoidMidBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()));

    public static final RegistryObject<Item> VOIDMID_ITEM = ITEMS.register("void_mid",
            () -> new BlockItem(VOIDMID.get(), new Item.Properties()));

    public static final RegistryObject<Block> VOIDCORNER = BLOCKS.register("void_corner",
            () -> new VoidCornerBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()));

    public static final RegistryObject<Item> VOIDCORNER_ITEM = ITEMS.register("void_corner",
            () -> new BlockItem(VOIDCORNER.get(), new Item.Properties()));


    public static final RegistryObject<Block> VOIDMIDCORNER = BLOCKS.register("void_midcorner",
            () -> new VoidMidCornerBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()));

    public static final RegistryObject<Item> VOIDMIDCORNER_ITEM = ITEMS.register("void_midcorner",
            () -> new BlockItem(VOIDMIDCORNER.get(), new Item.Properties()));
//==========

    // VOID
    public static final RegistryObject<Block> VOIDMIDNETHER = BLOCKS.register("voidnether_mid",
            () -> new VoidNetherMidBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()));

    public static final RegistryObject<Item> VOIDMIDNETHER_ITEM = ITEMS.register("voidnether_mid",
            () -> new BlockItem(VOIDMIDNETHER.get(), new Item.Properties()));

    public static final RegistryObject<Block> VOIDCORNERNETHER = BLOCKS.register("voidnether_corner",
            () -> new VoidNetherCornerBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()));

    public static final RegistryObject<Item> VOIDCORNERNETHER_ITEM = ITEMS.register("voidnether_corner",
            () -> new BlockItem(VOIDCORNERNETHER.get(), new Item.Properties()));


    public static final RegistryObject<Block> VOIDMIDCORNERNETHER = BLOCKS.register("voidnether_midcorner",
            () -> new VoidNetherMidCornerBlock(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()));

    public static final RegistryObject<Item> VOIDMIDCORNERNETHER_ITEM = ITEMS.register("voidnether_midcorner",
            () -> new BlockItem(VOIDMIDCORNERNETHER.get(), new Item.Properties()));
    //===========

    public static final RegistryObject<Item> BLACKSTONE_PLANT_ITEM = ITEMS.register("blackstone_plant",
            () -> new BlockItem(BLACKSTONE_PLANT.get(), new Item.Properties()));

    public static final RegistryObject<Block> BLACKSTONE_AXON = BLOCKS.register("blackstone_axon",
            () -> new BlackstoneAxonBlock(BlockBehaviour.Properties.copy(Blocks.GRASS)
                    .lightLevel(state -> 10)
                    .instabreak()
                    .noOcclusion()));

    public static final RegistryObject<Item> BLACKSTONE_AXON_ITEM = ITEMS.register("blackstone_axon",
            () -> new BlockItem(BLACKSTONE_AXON.get(), new Item.Properties()));

    // Блок входа
    public static final RegistryObject<Block> ENTRANCE = BLOCKS.register("entrance",
            () -> new EntranceBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_WART_BLOCK)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion())); // Чтобы сквозь него было видно мир, когда он прозрачный

    // Предмет
    public static final RegistryObject<Item> ENTRANCE_ITEM = ITEMS.register("entrance",
            () -> new GeoBlockItem(
                    ENTRANCE.get(),
                    new Item.Properties(),
                    new ResourceLocation(MODID, "geo/entrance.geo.json"),
                    new ResourceLocation(MODID, "textures/block/entrance.png"),
                    new ResourceLocation(MODID, "animations/entrance.animation.json"),
                    new ResourceLocation(MODID, "textures/block/entrance_emissive.png")
            ));


    public static final RegistryObject<Block> CRIMSON_WEB = BLOCKS.register("crimson_web",
            () -> new CrimsonWebBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_WART_BLOCK)
                    .instabreak()
                    .noOcclusion())); // Чтобы сквозь него было видно мир, когда он прозрачный


    public static final RegistryObject<Item> CRIMSON_WEB_ITEM = ITEMS.register("crimson_web",
            () -> new GeoBlockItem(
                    CRIMSON_WEB.get(),
                    new Item.Properties(),
                    new ResourceLocation(MODID, "geo/crimson_web.geo.json"),
                    new ResourceLocation(MODID, "textures/block/crimson_web.png"),
                    new ResourceLocation(MODID, "animations/crimson_web.animation.json"),
                    new ResourceLocation(MODID, "textures/block/blackstone_column_emissive.png")
            ));


    public static final RegistryObject<Block> TRAPHIVE = BLOCKS.register("traphive",
            () -> new TraphiveBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_WART_BLOCK)
                    .instabreak()
                    .noOcclusion())); // Чтобы сквозь него было видно мир, когда он прозрачный


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
            () -> new GeoBlockItem(
                    STATUE_STAND.get(),
                    new Item.Properties(),
                    new ResourceLocation(MODID, "geo/statue_stand.geo.json"),
                    new ResourceLocation(MODID, "textures/block/statue_stand.png"),
                    new ResourceLocation(MODID, "animations/statue_stand.animation.json"),
                    new ResourceLocation(MODID, "textures/block/blackstone_column_emissive.png")
            ));

    public static final RegistryObject<Block> TOTEMUS = BLOCKS.register("totemus",
            () -> new TotemusBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_BRICKS)
                    .strength(5.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));


    public static final RegistryObject<Item> TOTEMUS_ITEM = ITEMS.register("totemus",
            () -> new GeoBlockItem(
                    TOTEMUS.get(),
                    new Item.Properties(),
                    new ResourceLocation(MODID, "geo/totemus.geo.json"),
                    new ResourceLocation(MODID, "textures/block/totem_cave.png"),
                    new ResourceLocation(MODID, "animations/totemus.animation.json"),
                    new ResourceLocation(MODID, "textures/block/blackstone_column_emissive.png")
            ));


    public static final RegistryObject<Block> EYE = BLOCKS.register("eye_block",
            () -> new EyeBlock(BlockBehaviour.Properties.copy(Blocks.HONEY_BLOCK)
                    .strength(-1.0F, 3600000.0F)
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


    public static final RegistryObject<Block> MOSAIC_CHURCH = BLOCKS.register("mosaic_church",
            () -> new MosaicChurchBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)
                    .lightLevel(state -> 10)
                    .instabreak()
                    .noOcclusion())); // Чтобы сквозь него было видно мир, когда он прозрачный


    public static final RegistryObject<Item> MOSAIC_CHURCH_ITEM = ITEMS.register("mosaic_church",
            () -> new GeoBlockItem(
                    MOSAIC_CHURCH.get(),
                    new Item.Properties(),
                    new ResourceLocation(MODID, "geo/mosaic_church.geo.json"),
                    new ResourceLocation(MODID, "textures/block/mosaic_church.png"),
                    new ResourceLocation(MODID, "animations/mosaic_church.animation.json"),
                    new ResourceLocation(MODID, "textures/block/blackstone_column_emissive.png")
            ));


    public static final RegistryObject<Block> GRAND_DOOR = BLOCKS.register("grand_door",
            () -> new GrandDoorBlock(BlockBehaviour.Properties.copy(Blocks.POLISHED_BLACKSTONE_BRICKS)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()));

    // Предмет
    public static final RegistryObject<Item> GRAND_DOOR_ITEM = ITEMS.register("grand_door",
            () -> new GeoBlockItem(
                    GRAND_DOOR.get(),
                    new Item.Properties(),
                    new ResourceLocation(MODID, "geo/grand_door.geo.json"),
                    new ResourceLocation(MODID, "textures/block/grand_door.png"),
                    new ResourceLocation(MODID, "animations/grand_door.animation.json"),
                    new ResourceLocation(MODID, "textures/block/grand_door_emissive.png")
            ));

    // Фантомный блок для двери
    public static final RegistryObject<Block> GRAND_DOOR_PART = BLOCKS.register("grand_door_part",
            () -> new GrandDoorPartBlock(BlockBehaviour.Properties.copy(Blocks.POLISHED_BLACKSTONE_BRICKS)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()
                    .noLootTable()));

    // BLOCK ENTITY
    public static final RegistryObject<BlockEntityType<VoidNetherMidCornerBlockEntity>> VOIDMIDCORNERNETHER_BE = BLOCK_ENTITIES.register("voidnether_midcorner",
            () -> BlockEntityType.Builder.of(VoidNetherMidCornerBlockEntity::new, VOIDMIDCORNERNETHER.get()).build(null));

    public static final RegistryObject<BlockEntityType<VoidNetherCornerBlockEntity>> VOIDCORNERNETHER_BE = BLOCK_ENTITIES.register("voidnether_corner",
            () -> BlockEntityType.Builder.of(VoidNetherCornerBlockEntity::new, VOIDCORNERNETHER.get()).build(null));

    public static final RegistryObject<BlockEntityType<VoidNetherMidBlockEntity>> VOIDMIDNETHER_BE = BLOCK_ENTITIES.register("voidnether_mid",
            () -> BlockEntityType.Builder.of(VoidNetherMidBlockEntity::new, VOIDMIDNETHER.get()).build(null));

    public static final RegistryObject<BlockEntityType<EyeBlockEntity>> EYE_BE = BLOCK_ENTITIES.register("eye_block",
            () -> BlockEntityType.Builder.of(EyeBlockEntity::new, EYE.get()).build(null));

    public static final RegistryObject<BlockEntityType<StatueStandBlockEntity>> STATUE_STAND_BE = BLOCK_ENTITIES.register("statue_stand",
            () -> BlockEntityType.Builder.of(StatueStandBlockEntity::new, STATUE_STAND.get()).build(null));

    public static final RegistryObject<BlockEntityType<TotemusBlockEntity>> TOTEMUS_BE = BLOCK_ENTITIES.register("totemus",
            () -> BlockEntityType.Builder.of(TotemusBlockEntity::new, TOTEMUS.get()).build(null));

    public static final RegistryObject<BlockEntityType<TraphiveBlockEntity>> TRAPHIVE_BE = BLOCK_ENTITIES.register("traphive",
            () -> BlockEntityType.Builder.of(TraphiveBlockEntity::new, TRAPHIVE.get()).build(null));

    public static final RegistryObject<BlockEntityType<MosaicChurchBlockEntity>> MOSAIC_CHURCH_BE = BLOCK_ENTITIES.register("mosaic_church",
            () -> BlockEntityType.Builder.of(MosaicChurchBlockEntity::new, MOSAIC_CHURCH.get()).build(null));

    public static final RegistryObject<BlockEntityType<CrimsonWebBlockEntity>> CRIMSON_WEB_BE = BLOCK_ENTITIES.register("crimson_web",
            () -> BlockEntityType.Builder.of(CrimsonWebBlockEntity::new, CRIMSON_WEB.get()).build(null));

    public static final RegistryObject<BlockEntityType<EntranceBlockEntity>> ENTRANCE_BE = BLOCK_ENTITIES.register("entrance",
            () -> BlockEntityType.Builder.of(EntranceBlockEntity::new, ENTRANCE.get()).build(null));

    public static final RegistryObject<BlockEntityType<GrandDoorBlockEntity>> GRAND_DOOR_BE = BLOCK_ENTITIES.register("grand_door",
            () -> BlockEntityType.Builder.of(GrandDoorBlockEntity::new, GRAND_DOOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<NetherSpawnerBlockEntity>> NETHER_SPAWNER_BE = BLOCK_ENTITIES.register("nether_spawner",
            () -> BlockEntityType.Builder.of(NetherSpawnerBlockEntity::new, NETHER_SPAWNER.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlackstonePlantBlockEntity>> BLACKSTONE_PLANT_BE = BLOCK_ENTITIES.register("blackstone_plant",
            () -> BlockEntityType.Builder.of(BlackstonePlantBlockEntity::new, BLACKSTONE_PLANT.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlackstoneAxonBlockEntity>> BLACKSTONE_AXON_BE = BLOCK_ENTITIES.register("blackstone_axon",
            () -> BlockEntityType.Builder.of(BlackstoneAxonBlockEntity::new, BLACKSTONE_AXON.get()).build(null));

    public static final RegistryObject<BlockEntityType<PointedBlackstoneBlockEntity>> POINTED_BLACKSTONE_BE = BLOCK_ENTITIES.register("pointed_blackstone",
            () -> BlockEntityType.Builder.of(PointedBlackstoneBlockEntity::new, POINTED_BLACKSTONE.get()).build(null));

    // ENTITY
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);

    // Регистрация эффекта:
    public static final RegistryObject<MobEffect> MANIPULATION_EFFECT = EFFECTS.register("manipulation", ManipulationEffect::new);
    public static final RegistryObject<MobEffect> FEAR_EFFECT = EFFECTS.register("fear", () -> new ZoneEffect(0x000000));
    public static final RegistryObject<MobEffect> EXCITEMENT_EFFECT = EFFECTS.register("excitement", () -> new ZoneEffect(0xFF0000));
    public static final RegistryObject<MobEffect> FAITH_EFFECT = EFFECTS.register("faith", () -> new ZoneEffect(0x800080));


    public static final RegistryObject<EntityType<StatueBossunitEntity>> STATUE_BOSSUNIT = ENTITIES.register("statue_bossunit",
            () -> EntityType.Builder.of(StatueBossunitEntity::new, MobCategory.MONSTER)
                    .sized(0.625F, 2.125F) // Хитбокс статуи
                    .fireImmune()
                    .build(new ResourceLocation(MODID, "statue_bossunit").toString()));

    public static final RegistryObject<EntityType<LaserEntity>> LASER = ENTITIES.register("laser",
            () -> EntityType.Builder.of(LaserEntity::new, MobCategory.MISC)
                    .sized(0.5F, 18.75F) // Высоченный узкий хитбокс
                    .fireImmune()
                    .build(new ResourceLocation(MODID, "laser").toString()));

    public static final RegistryObject<EntityType<StatueEntity>> STATUE = ENTITIES.register("statue_entity",
            () -> EntityType.Builder.of(StatueEntity::new, MobCategory.MONSTER)
                    .sized(0.625F, 2.125F) // Хитбокс статуи
                    .fireImmune()
                    .build(new ResourceLocation(MODID, "statue_entity").toString()));


    public static final RegistryObject<EntityType<TraderEntity>> TRADER = ENTITIES.register("trader",
            () -> EntityType.Builder.of(TraderEntity::new, MobCategory.CREATURE)
                    .sized(1.125F, 1.5F)
                    .build(new ResourceLocation(MODID, "trader").toString()));

    public static final RegistryObject<EntityType<DoctorEntity>> DOCTOR = ENTITIES.register("doctor",
            () -> EntityType.Builder.of(DoctorEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F) // Хитбокс жителя
                    .build(new ResourceLocation(MODID, "doctor").toString()));

    public static final RegistryObject<EntityType<BlacksmithEntity>> BLACKSMITH = ENTITIES.register("blacksmith",
            () -> EntityType.Builder.of(BlacksmithEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F) // Хитбокс жителя
                    .build(new ResourceLocation(MODID, "blacksmith").toString()));

    public static final RegistryObject<EntityType<BelieverEntity>> BELIEVER = ENTITIES.register("believer",
            () -> EntityType.Builder.of(BelieverEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F) // Хитбокс жителя
                    .build(new ResourceLocation(MODID, "believer").toString()));

    public static final RegistryObject<EntityType<VillagerPrisonerEntity>> VILLAGER_PRISONER = ENTITIES.register("villager_prisoner",
            () -> EntityType.Builder.of(VillagerPrisonerEntity::new, MobCategory.CREATURE) // Мирный
                    .sized(0.6F, 1.95F) // Стандартные размеры жителя
                    .build("villager_prisoner"));

    public static final RegistryObject<EntityType<PiglinPrisonerEntity>> PIGLIN_PRISONER = ENTITIES.register("piglin_prisoner",
            () -> EntityType.Builder.of(PiglinPrisonerEntity::new, MobCategory.CREATURE) // Мирный
                    .sized(0.6F, 1.95F) // Стандартные размеры жителя
                    .build("piglin_prisoner"));

    // Регистрация Мнипулятора:
    public static final RegistryObject<EntityType<ManipulatorEntity>> MANIPULATOR = ENTITIES.register("manipulator",
            () -> EntityType.Builder.of(ManipulatorEntity::new, MobCategory.MONSTER)
                    .fireImmune()
                    .sized(0.9375F, 2.125F) // Хитбокс 15x34x15 пикселей
                    .build("manipulator"));

    public static final RegistryObject<EntityType<WelcomerEntity>> WELCOMER = ENTITIES.register("welcomer",
            () -> EntityType.Builder.of(WelcomerEntity::new, MobCategory.MONSTER)
                    // Указываем размеры из Blockbench: 10/16 = 0.625, 36/16 = 2.25
                    .sized(0.625f, 2.25f)
                    .build("welcomer"));

    // Регистрация моба с пересчетом хитбокса (16/16 = 1.0f ширина, 82/16 = 5.125f высота)
    public static final RegistryObject<EntityType<GuardianEntity>> GUARDIAN = ENTITIES.register("guardian",
            () -> EntityType.Builder.of(GuardianEntity::new, MobCategory.MONSTER)
                    .sized(1.0f, 5.125f)
                    .fireImmune()
                    .build("guardian"));

    public static final RegistryObject<EntityType<GhastlyEntity>> GHASTLY = ENTITIES.register("ghastly",
            () -> EntityType.Builder.of(GhastlyEntity::new, MobCategory.CREATURE) // Он мирный, поэтому CREATURE
                    .sized(0.625f, 0.8125f)
                    .fireImmune()
                    .build("ghastly"));

    public NetherExp(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Подключаем реестры
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ENTITIES.register(modEventBus);
        EFFECTS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);
    }
// CREATIVE MENU
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS || event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(NETHER_SPAWNER_ITEM);
            event.accept(GRAND_DOOR_ITEM);
            event.accept(BLACKSTONE_COLUMN_ITEM);
            event.accept(ENTRANCE_ITEM);
            event.accept(CRIMSON_WEB_ITEM);
            event.accept(TRAPHIVE_ITEM);
            event.accept(VOIDMID_ITEM);
            event.accept(VOIDCORNER_ITEM);
            event.accept(VOIDMIDCORNER_ITEM);
            event.accept(VOIDMIDNETHER_ITEM);
            event.accept(VOIDCORNERNETHER_ITEM);
            event.accept(MOSAIC_CHURCH_ITEM);
            event.accept(STATUE_STAND_ITEM);
            event.accept(TOTEMUS_ITEM);
            event.accept(EYE_ITEM);
            event.accept(VOIDMIDCORNERNETHER_ITEM);
        }
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(BLACKSTONE_PLANT_ITEM);
            event.accept(BLACKSTONE_AXON_ITEM);
            event.accept(POINTED_BLACKSTONE_ITEM);
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            // Регистрируем здоровье и характеристики для нашего моба
            event.put(LASER.get(), LaserEntity.createAttributes().build());
            event.put(STATUE_BOSSUNIT.get(), StatueBossunitEntity.createAttributes().build());
            event.put(BLACKSMITH.get(), BlacksmithEntity.createAttributes().build());
            event.put(DOCTOR.get(), DoctorEntity.createAttributes().build());
            event.put(TRADER.get(), TraderEntity.createAttributes().build());
            event.put(STATUE.get(), StatueEntity.createAttributes().build());
            event.put(BELIEVER.get(), BelieverEntity.createAttributes().build());
            event.put(PIGLIN_PRISONER.get(), PiglinPrisonerEntity.createAttributes().build());
            event.put(VILLAGER_PRISONER.get(), VillagerPrisonerEntity.createAttributes().build());
            event.put(MANIPULATOR.get(), ManipulatorEntity.createAttributes().build());
            event.put(WELCOMER.get(), WelcomerEntity.createAttributes().build());
            event.put(GHASTLY.get(), GhastlyEntity.createAttributes().build());
            event.put(GUARDIAN.get(), GuardianEntity.createAttributes().build());
        }
    }


    // Твой старый класс ClientModEvents
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(VOIDMIDCORNERNETHER_BE.get(), VoidNetherMidCornerRenderer::new);
            event.registerBlockEntityRenderer(VOIDCORNERNETHER_BE.get(), VoidNetherCornerRenderer::new);
            event.registerBlockEntityRenderer(VOIDMIDNETHER_BE.get(), VoidNetherMidRenderer::new);
            event.registerBlockEntityRenderer(MOSAIC_CHURCH_BE.get(), MosaicChurchRenderer::new);
            event.registerBlockEntityRenderer(TRAPHIVE_BE.get(), TraphiveRenderer::new);
            event.registerBlockEntityRenderer(STATUE_STAND_BE.get(), StatueStandRenderer::new);
            event.registerBlockEntityRenderer(TOTEMUS_BE.get(), TotemusRenderer::new);
            event.registerBlockEntityRenderer(EYE_BE.get(), EyeRenderer::new);
            event.registerBlockEntityRenderer(CRIMSON_WEB_BE.get(), CrimsonWebRenderer::new);
            event.registerBlockEntityRenderer(ENTRANCE_BE.get(), EntranceRenderer::new);
            event.registerBlockEntityRenderer(GRAND_DOOR_BE.get(), GrandDoorRenderer::new);
            event.registerBlockEntityRenderer(POINTED_BLACKSTONE_BE.get(), PointedBlackstoneRenderer::new);
            event.registerBlockEntityRenderer(NETHER_SPAWNER_BE.get(), NetherSpawnerRenderer::new);
            event.registerBlockEntityRenderer(BLACKSTONE_PLANT_BE.get(), BlackstonePlantRenderer::new);
            event.registerBlockEntityRenderer(BLACKSTONE_AXON_BE.get(), BlackstoneAxonRenderer::new);

            //entity
            event.registerEntityRenderer(LASER.get(), LaserRenderer::new);
            event.registerEntityRenderer(STATUE_BOSSUNIT.get(), StatueBossunitRenderer::new);
            event.registerEntityRenderer(BLACKSMITH.get(), BlacksmithRenderer::new);
            event.registerEntityRenderer(DOCTOR.get(), DoctorRenderer::new);
            event.registerEntityRenderer(TRADER.get(), TraderRenderer::new);
            event.registerEntityRenderer(STATUE.get(), StatueRenderer::new);
            event.registerEntityRenderer(BELIEVER.get(), BelieverRenderer::new);
            event.registerEntityRenderer(PIGLIN_PRISONER.get(), PiglinPrisonerRenderer::new);
            event.registerEntityRenderer(VILLAGER_PRISONER.get(), VillagerPrisonerRenderer::new);
            event.registerEntityRenderer(MANIPULATOR.get(), ManipulatorRenderer::new);
            event.registerEntityRenderer(WELCOMER.get(), WelcomerRenderer::new);
            event.registerEntityRenderer(GUARDIAN.get(), GuardianRenderer::new);
            event.registerEntityRenderer(GHASTLY.get(), GhastlyRenderer::new);
        }

        // --- ДОБАВЛЯЕМ НОВЫЙ МЕТОД ДЛЯ ОВЕРЛЕЕВ СЮДА ---
        @SubscribeEvent
        public static void registerGuiOverlays(net.minecraftforge.client.event.RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("manipulation_overlay", ManipulationOverlay.HUD_OVERLAY);
        }
    }
}