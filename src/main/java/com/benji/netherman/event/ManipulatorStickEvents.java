package com.benji.netherman.event;

import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.AzazelEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NetherExp.MODID)
public class ManipulatorStickEvents {

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || entity.tickCount % 4 != 0) return;

        // --- ЛОГИКА 3: Жители и пиглины убегают от игрока с палкой ---
        if (entity instanceof Villager || entity instanceof AbstractPiglin) {
            Player scaringPlayer = entity.level().getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), 12.0D,
                    p -> p instanceof Player && (((Player) p).getMainHandItem().is(NetherExp.MANIPULATOR_STICK.get()) ||
                            ((Player) p).getOffhandItem().is(NetherExp.MANIPULATOR_STICK.get())));

            if (scaringPlayer != null) {
                Vec3 awayPos = DefaultRandomPos.getPosAway((net.minecraft.world.entity.PathfinderMob) entity, 16, 7, scaringPlayer.position());
                if (awayPos != null) {
                    ((net.minecraft.world.entity.PathfinderMob) entity).getNavigation().moveTo(awayPos.x, awayPos.y, awayPos.z, 1.3D);
                }
            }
        }

        // --- НОВОЕ: ПРИНУДИТЕЛЬНЫЙ РАДАР ДЛЯ СКЕЛЕТОВ ---
        // Ванильные скелеты не ищут монстров сами, поэтому мы находим их за них
        if (entity instanceof WitherSkeleton skeleton && skeleton.getPersistentData().contains("SummonerUUID")) {
            if (skeleton.getTarget() == null) { // Если скелет стоит без дела
                java.util.UUID summonerUUID = skeleton.getPersistentData().getUUID("SummonerUUID");

                // Ищем всех живых существ в радиусе 16 блоков
                java.util.List<LivingEntity> potentialTargets = skeleton.level().getEntitiesOfClass(LivingEntity.class, skeleton.getBoundingBox().inflate(16.0D),
                        e -> (e instanceof net.minecraft.world.entity.monster.Monster || e instanceof Player) // Ищем монстров или игроков
                                && !e.getUUID().equals(summonerUUID) // Игнорируем призывателя
                                && !(e instanceof AzazelEntity)      // Игнорируем Азазеля
                                && !(e instanceof WitherSkeleton)    // Игнорируем своих же скелетов
                );

                if (!potentialTargets.isEmpty()) {
                    // Берем в таргет первого попавшегося подходящего врага!
                    skeleton.setTarget(potentialTargets.get(0));
                }
            }
        }
    }

    // ЛОГИКА 4: Защита создателя (Осталась без изменений)
    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof WitherSkeleton skeleton) {
            if (skeleton.getPersistentData().contains("SummonerUUID")) {
                java.util.UUID summonerUUID = skeleton.getPersistentData().getUUID("SummonerUUID");
                LivingEntity newTarget = event.getNewTarget();

                if (newTarget != null) {
                    if (newTarget.getUUID().equals(summonerUUID)) {
                        event.setCanceled(true);
                        return;
                    }

                    boolean shouldAttack = newTarget instanceof Player || newTarget instanceof net.minecraft.world.entity.monster.Monster;
                    if (newTarget instanceof AzazelEntity || newTarget instanceof WitherSkeleton) {
                        shouldAttack = false;
                    }

                    if (!shouldAttack) {
                        event.setNewTarget(null);
                    }
                }
            }
        }
    }
}