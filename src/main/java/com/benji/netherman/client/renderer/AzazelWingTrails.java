package com.benji.netherman.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.*;

@Mod.EventBusSubscriber(modid = com.benji.netherman.NetherExp.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class AzazelWingTrails {

    public static class TrailData {
        public int ticksRemaining = 0;
        public LinkedList<Vec3> mainLeft = new LinkedList<>();
        public LinkedList<Vec3> mainRight = new LinkedList<>();
        public LinkedList<Vec3> addLeft = new LinkedList<>();
        public LinkedList<Vec3> addRight = new LinkedList<>();
    }

    public static class ShockwaveData {
        public final Vec3 center;
        public final Vec3 right;
        public final Vec3 backUp;
        public int age = 0;
        public final int maxAge = 8;

        public ShockwaveData(Vec3 center, Vec3 right, Vec3 backUp) {
            this.center = center;
            this.right = right;
            this.backUp = backUp;
        }
    }

    public static class SparkData {
        public Vec3 pos;
        public final Vec3 velocity;
        public int age = 0;
        public final int maxAge;
        public final int color;

        public SparkData(Vec3 pos, Vec3 velocity, int maxAge, int color) {
            this.pos = pos;
            this.velocity = velocity;
            this.maxAge = maxAge;
            this.color = color;
        }
    }

    private static final Map<UUID, TrailData> TRAILS = new HashMap<>();
    private static final List<ShockwaveData> SHOCKWAVES = new ArrayList<>();
    private static final List<SparkData> SPARKS = new ArrayList<>();
    private static final int MAX_TRAIL_LENGTH = 15;

    public static void spawnShockwave(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = look.cross(up).normalize();
        Vec3 backUp = right.cross(look).normalize();
        Vec3 center = player.position().add(0, 0.4D, 0).subtract(look.scale(0.01D));

        SHOCKWAVES.add(new ShockwaveData(center, right, backUp));
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        TRAILS.clear();
        SHOCKWAVES.clear();
        SPARKS.clear();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player.level().isClientSide()) {
            Player player = event.player;
            UUID uuid = player.getUUID();

            int ticks = player.getPersistentData().getInt("AzazelBoostTrail");

            if (!player.isAlive()) {
                ticks = 0;
                player.getPersistentData().remove("AzazelBoostTrail");
            }

            if (ticks > 0) {
                player.getPersistentData().putInt("AzazelBoostTrail", ticks - 1);
                TrailData data = TRAILS.computeIfAbsent(uuid, k -> new TrailData());
                data.ticksRemaining = ticks;

                Vec3 look = player.getLookAngle();
                Vec3 up = new Vec3(0, 1, 0);
                Vec3 right = look.cross(up).normalize();
                Vec3 backUp = right.cross(look).normalize();
                Vec3 center = player.position().add(0, 0.4D, 0);

                Vec3 mainRightPos = center.add(right.scale(2.8D)).subtract(look.scale(0.3D)).add(backUp.scale(0.2D));
                Vec3 mainLeftPos  = center.subtract(right.scale(2.8D)).subtract(look.scale(0.3D)).add(backUp.scale(0.2D));
                Vec3 addRightPos = center.add(right.scale(1.5D)).subtract(look.scale(0.1D)).subtract(backUp.scale(0.4D));
                Vec3 addLeftPos  = center.subtract(right.scale(1.5D)).subtract(look.scale(0.1D)).subtract(backUp.scale(0.4D));

                addPoint(data.mainLeft, mainLeftPos);
                addPoint(data.mainRight, mainRightPos);
                addPoint(data.addLeft, addLeftPos);
                addPoint(data.addRight, addRightPos);
            } else {
                TrailData data = TRAILS.get(uuid);
                if (data != null) {
                    data.ticksRemaining = 0;

                    shrinkTrail(data.mainLeft);
                    shrinkTrail(data.mainRight);
                    shrinkTrail(data.addLeft);
                    shrinkTrail(data.addRight);
                    if (data.mainLeft.isEmpty()) TRAILS.remove(uuid);
                }
            }

            int drillTicks = player.getPersistentData().getInt("AzazelDrillTicks");
            if (!player.isAlive()) {
                player.getPersistentData().remove("AzazelDrillTicks");
            } else if (drillTicks > 0) {
                Vec3 look = player.getLookAngle();
                Vec3 origin = player.position().add(0, 0.8D, 0).add(look.scale(0.5D));
                RandomSource rand = player.getRandom();

                for (int i = 0; i < 8; i++) {
                    Vec3 sparkVel = look.scale(0.15D).add(
                            (rand.nextDouble() - 0.5D) * 0.5D,
                            (rand.nextDouble() - 0.5D) * 0.5D,
                            (rand.nextDouble() - 0.5D) * 0.5D
                    );

                    int maxAge = 6 + rand.nextInt(10);
                    int colorIndex = rand.nextInt(3);
                    int color = 0xFF0000;
                    if (colorIndex == 1) color = 0xFF6600;
                    if (colorIndex == 2) color = 0xFFCC00;

                    SPARKS.add(new SparkData(origin, sparkVel, maxAge, color));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Iterator<ShockwaveData> iterator = SHOCKWAVES.iterator();
            while (iterator.hasNext()) {
                ShockwaveData wave = iterator.next();
                wave.age++;
                if (wave.age >= wave.maxAge) {
                    iterator.remove();
                }
            }

            Iterator<SparkData> sparkIterator = SPARKS.iterator();
            while (sparkIterator.hasNext()) {
                SparkData spark = sparkIterator.next();
                spark.pos = spark.pos.add(spark.velocity);
                spark.age++;
                if (spark.age >= spark.maxAge) {
                    sparkIterator.remove();
                }
            }
        }
    }

    private static void addPoint(LinkedList<Vec3> list, Vec3 point) {
        list.addFirst(point);
        if (list.size() > MAX_TRAIL_LENGTH) list.removeLast();
    }

    private static void shrinkTrail(LinkedList<Vec3> list) {
        if (!list.isEmpty()) list.removeLast();
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (TRAILS.isEmpty() && SHOCKWAVES.isEmpty() && SPARKS.isEmpty()) return;

        Vec3 camPos = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick();

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.depthMask(false);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f matrix = poseStack.last().pose();

        if (!TRAILS.isEmpty()) {
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (Map.Entry<UUID, TrailData> entry : TRAILS.entrySet()) {
                TrailData data = entry.getValue();
                drawRibbon(buffer, matrix, data.mainLeft, camPos, 0.02F);
                drawRibbon(buffer, matrix, data.mainRight, camPos, 0.02F);
                drawRibbon(buffer, matrix, data.addLeft, camPos, 0.02F);
                drawRibbon(buffer, matrix, data.addRight, camPos, 0.02F);

                if (data.ticksRemaining > 0 && Minecraft.getInstance().level != null) {
                    Player player = Minecraft.getInstance().level.getPlayerByUUID(entry.getKey());
                    if (player != null && player.isAlive()) { // Не рендерим купол над трупом
                        Vec3 playerPos = player.getPosition(partialTick).add(0, 0.4D, 0);
                        Vec3 look = player.getViewVector(partialTick);
                        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
                        Vec3 backUp = right.cross(look).normalize();

                        float boostProgress = Math.min(1.0F, data.ticksRemaining / 15.0F);
                    }
                }
            }
            tesselator.end();
        }

        if (!SHOCKWAVES.isEmpty()) {
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (ShockwaveData wave : SHOCKWAVES) {
                drawShockwaveRing(buffer, matrix, wave, partialTick);
            }
            tesselator.end();
        }

        if (!SPARKS.isEmpty()) {
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (SparkData spark : SPARKS) {
                drawSparkLine(buffer, matrix, spark, camPos);
            }
            tesselator.end();
        }

        poseStack.popPose();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }

    private static void drawSparkLine(BufferBuilder buffer, Matrix4f matrix, SparkData spark, Vec3 camPos) {
        float progress = (float) spark.age / spark.maxAge;
        int alpha = (int) (255 * (1.0F - progress));

        int r = (spark.color >> 16) & 0xFF;
        int g = (spark.color >> 8) & 0xFF;
        int b = spark.color & 0xFF;

        Vec3 p1 = spark.pos;
        Vec3 p2 = spark.pos.subtract(spark.velocity.scale(0.75D));

        float width = 0.025F * (1.0F - progress);

        Vec3 toCam = camPos.subtract(p1).normalize();
        Vec3 dir = p1.subtract(p2).normalize();
        Vec3 right = dir.cross(toCam).normalize().scale(width);

        addVertex(buffer, matrix, p1.add(right), r, g, b, alpha);
        addVertex(buffer, matrix, p1.subtract(right), r, g, b, alpha);
        addVertex(buffer, matrix, p2.subtract(right), r, g, b, alpha);
        addVertex(buffer, matrix, p2.add(right), r, g, b, alpha);
    }

    private static void drawRibbon(BufferBuilder buffer, Matrix4f matrix, LinkedList<Vec3> points, Vec3 camPos, float startWidth) {
        if (points.size() < 2) return;
        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 p1 = points.get(i);
            Vec3 p2 = points.get(i + 1);

            float progress1 = (float) i / points.size();
            float progress2 = (float) (i + 1) / points.size();

            float width1 = startWidth * (1.0F - progress1);
            float width2 = startWidth * (1.0F - progress2);

            int alpha1 = (int) (200 * (1.0F - progress1));
            int alpha2 = (int) (200 * (1.0F - progress2));

            int r1 = (int) lerp(255, 100, progress1);
            int g1 = (int) lerp(255, 100, progress1);
            int b1 = (int) lerp(255, 120, progress1);

            int r2 = (int) lerp(255, 100, progress2);
            int g2 = (int) lerp(255, 100, progress2);
            int b2 = (int) lerp(255, 120, progress2);

            Vec3 toCam1 = camPos.subtract(p1).normalize();
            Vec3 dir1 = p2.subtract(p1).normalize();
            Vec3 right1 = dir1.cross(toCam1).normalize().scale(width1);

            Vec3 toCam2 = camPos.subtract(p2).normalize();
            Vec3 right2 = dir1.cross(toCam2).normalize().scale(width2);

            addVertex(buffer, matrix, p1.add(right1), r1, g1, b1, alpha1);
            addVertex(buffer, matrix, p1.subtract(right1), r1, g1, b1, alpha1);
            addVertex(buffer, matrix, p2.subtract(right2), r2, g2, b2, alpha2);
            addVertex(buffer, matrix, p2.add(right2), r2, g2, b2, alpha2);
        }
    }

    private static void drawShockwaveRing(BufferBuilder buffer, Matrix4f matrix, ShockwaveData wave, float partialTick) {
        float progress = (wave.age + partialTick) / (float) wave.maxAge;
        if (progress > 1.0F) return;

        float maxRadius = 4.5F;
        float currentOuterRadius = 0.2F + (maxRadius - 0.2F) * progress;
        float thickness = 0.4F * (1.0F - progress);
        float currentInnerRadius = Math.max(0.0F, currentOuterRadius - thickness);

        int alpha = (int) (180 * (1.0F - progress));

        int innerR = 255, innerG = 255, innerB = 255;
        int outerR = 120, outerG = 120, outerB = 140;

        int segments = 32;

        Vec3 prevInner = null;
        Vec3 prevOuter = null;
        Vec3 firstInner = null;
        Vec3 firstOuter = null;

        for (int i = 0; i <= segments; i++) {
            double angle = (i * 2.0 * Math.PI) / segments;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            Vec3 dirVec = wave.right.scale(cos).add(wave.backUp.scale(sin));

            Vec3 innerPos = wave.center.add(dirVec.scale(currentInnerRadius));
            Vec3 outerPos = wave.center.add(dirVec.scale(currentOuterRadius));

            if (i == 0) {
                firstInner = innerPos;
                firstOuter = outerPos;
            } else {
                addVertex(buffer, matrix, prevInner, innerR, innerG, innerB, alpha);
                addVertex(buffer, matrix, prevOuter, outerR, outerG, outerB, alpha);
                addVertex(buffer, matrix, outerPos, outerR, outerG, outerB, alpha);
                addVertex(buffer, matrix, innerPos, innerR, innerG, innerB, alpha);
            }

            prevInner = innerPos;
            prevOuter = outerPos;
        }

        if (prevInner != null && firstInner != null) {
            addVertex(buffer, matrix, prevInner, innerR, innerG, innerB, alpha);
            addVertex(buffer, matrix, prevOuter, outerR, outerG, outerB, alpha);
            addVertex(buffer, matrix, firstOuter, outerR, outerG, outerB, alpha);
            addVertex(buffer, matrix, firstInner, innerR, innerG, innerB, alpha);
        }
    }

    private static void addVertex(BufferBuilder buffer, Matrix4f matrix, Vec3 pos, int r, int g, int b, int a) {
        buffer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z).color(r, g, b, a).endVertex();
    }

    private static float lerp(float start, float end, float delta) {
        return start + delta * (end - start);
    }
}