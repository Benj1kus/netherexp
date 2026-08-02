package com.benji.netherman;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class QuotaManager {

    private static final UUID PENALTY_UUID = UUID.fromString("B2432B2B-1845-4200-8DF4-3A5D17DBDF4B");

    public static void generateNewQuota(Player player) {
        CompoundTag data = player.getPersistentData();
        data.putInt("QuotaTimeLeft", 72000);

        List<Integer> tasks = new ArrayList<>();
        for (int i = 0; i <= 5; i++) tasks.add(i);
        Collections.shuffle(tasks);

        for (int i = 1; i <= 3; i++) {
            int taskType = tasks.get(i - 1);
            data.putInt("QuotaTask" + i, taskType);
            data.putInt("QuotaProg" + i, 0);

            int max = 1;
            switch (taskType) {
                case 0: max = 1; break; // Алтарь
                case 1: max = 5 + player.getRandom().nextInt(6); break;
                case 2: max = 30; break; // Жертвоприношение
                case 3: max = 32 + player.getRandom().nextInt(33); break;
                case 4: max = 5 + player.getRandom().nextInt(11); break;
                case 5: max = 5 + player.getRandom().nextInt(11); break;
            }
            data.putInt("QuotaMax" + i, max);
        }
    }

    public static void addProgress(Player player, int taskType, int amount) {
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean("AzazelCultist")) return;

        boolean updated = false;
        boolean taskJustCompleted = false;

        for (int i = 1; i <= 3; i++) {
            if (data.contains("QuotaTask" + i) && data.getInt("QuotaTask" + i) == taskType) {
                int prog = data.getInt("QuotaProg" + i);
                int max = data.getInt("QuotaMax" + i);
                if (prog < max) {
                    int newProg = Math.min(prog + amount, max);
                    data.putInt("QuotaProg" + i, newProg);
                    updated = true;

                    if (newProg == max) {
                        taskJustCompleted = true;
                    }
                }
            }
        }

        if (taskJustCompleted) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        if (updated) checkCompletion(player);
    }

    private static void checkCompletion(Player player) {
        CompoundTag data = player.getPersistentData();
        boolean allDone = true;

        for (int i = 1; i <= 3; i++) {
            if (data.getInt("QuotaProg" + i) < data.getInt("QuotaMax" + i)) {
                allDone = false;
                break;
            }
        }

        if (allDone) {
            int stage = data.getInt("QuotaStage");

            if (stage < 5) {
                // Если это квоты с 1 по 4
                data.putInt("QuotaStage", stage + 1);
                player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                generateNewQuota(player);
            }
            else if (stage == 5) {
                data.putInt("QuotaStage", 6);
                player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.5F, 0.5F);

                if (player.level() instanceof ServerLevel sl) {
                    var boss = com.benji.netherman.NetherExp.AZAZEL_HUMAN.get().create(sl);

                    if (boss != null) {
                        double dx = (sl.random.nextDouble() - 0.5) * 6;
                        double dz = (sl.random.nextDouble() - 0.5) * 6;
                        boss.moveTo(player.getX() + dx, player.getY(), player.getZ() + dz, 0, 0);

                        boss.getEntityData().set(com.benji.netherman.entity.AzazelHumanEntity.BOSS_STATE, 110);
                        boss.getEntityData().set(com.benji.netherman.entity.AzazelHumanEntity.DIALOGUE_TICK, 0);

                        sl.addFreshEntity(boss);
                    }
                }

                data.remove("QuotaTask1");
                data.remove("QuotaTask2");
                data.remove("QuotaTask3");
                data.remove("QuotaTimeLeft");
            }
        }
    }

    public static void failQuota(Player player) {
        CompoundTag data = player.getPersistentData();

        boolean isCultist = data.getBoolean("AzazelCultist");

        boolean hasQuota = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(NetherExp.QUOTA.get())) {
                hasQuota = true;
                break;
            }
        }

        if (!isCultist || !hasQuota) {
            return;
        }

        player.level().playSound(null, player.blockPosition(), ModSounds.BELL_BEAST_LAUGH.get(), SoundSource.PLAYERS, 1.5F, 1.0F);

        var attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) {
            attr.removeModifier(PENALTY_UUID);
            attr.addPermanentModifier(new AttributeModifier(PENALTY_UUID, "AzazelPenalty", -6.0D, AttributeModifier.Operation.ADDITION));
        }

        data.putLong("AzazelPenaltyTime", player.level().getGameTime() + 24000);

        if (player.level() instanceof ServerLevel sl) {
            for (int i = 0; i < 3; i++) {
                var statue = NetherExp.STATUE.get().create(sl); // Для 1.21.1 возможно ModEntities.STATUE.get()
                if (statue != null) {
                    double dx = (sl.random.nextDouble() - 0.5) * 6;
                    double dz = (sl.random.nextDouble() - 0.5) * 6;
                    statue.moveTo(player.getX() + dx, player.getY(), player.getZ() + dz, sl.random.nextFloat() * 360F, 0);
                    sl.addFreshEntity(statue);
                    sl.sendParticles(ParticleTypes.LARGE_SMOKE, statue.getX(), statue.getY() + 1, statue.getZ(), 20, 0.5, 0.5, 0.5, 0.05);
                }
            }
        }

        generateNewQuota(player);
    }

    public static void restoreHealth(Player player) {
        var attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null && attr.getModifier(PENALTY_UUID) != null) {
            attr.removeModifier(PENALTY_UUID);
            player.level().playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.5F, 1.5F);
        }
    }

    public static boolean checkAltarStructure(Level level, BlockPos placedPos) {

        for (BlockPos pos : BlockPos.betweenClosed(placedPos.offset(-6, -6, -6), placedPos.offset(6, 6, 6))) {
            if (level.getBlockState(pos).is(Blocks.BELL)) {
                if (isValidAltarAt(level, pos)) {

                    if (level instanceof ServerLevel sl) {
                        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-3, -5, -3), pos.offset(3, 0, 3))) {
                            if (sl.getBlockState(p).is(Blocks.GOLD_BLOCK) || sl.getBlockState(p).is(Blocks.BELL)) {
                                sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX()+0.5, p.getY()+0.5, p.getZ()+0.5, 10, 0.3, 0.3, 0.3, 0.1);
                            }
                        }
                        sl.playSound(null, pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 1.5F, 1.0F);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isValidAltarAt(Level level, BlockPos bellPos) {
        for (int i = 1; i <= 5; i++) {
            if (!level.getBlockState(bellPos.below(i)).is(Blocks.GOLD_BLOCK)) return false;
        }

        BlockPos center = bellPos.below(3);


        boolean armsX = level.getBlockState(center.east(1)).is(Blocks.GOLD_BLOCK) &&
                level.getBlockState(center.east(2)).is(Blocks.GOLD_BLOCK) &&
                level.getBlockState(center.east(2).above(1)).is(Blocks.GOLD_BLOCK) &&
                level.getBlockState(center.west(1)).is(Blocks.GOLD_BLOCK) &&
                level.getBlockState(center.west(2)).is(Blocks.GOLD_BLOCK) &&
                level.getBlockState(center.west(2).above(1)).is(Blocks.GOLD_BLOCK);


        boolean armsZ = level.getBlockState(center.south(1)).is(Blocks.GOLD_BLOCK) &&
                level.getBlockState(center.south(2)).is(Blocks.GOLD_BLOCK) &&
                level.getBlockState(center.south(2).above(1)).is(Blocks.GOLD_BLOCK) &&
                level.getBlockState(center.north(1)).is(Blocks.GOLD_BLOCK) &&
                level.getBlockState(center.north(2)).is(Blocks.GOLD_BLOCK) &&
                level.getBlockState(center.north(2).above(1)).is(Blocks.GOLD_BLOCK);

        return armsX || armsZ;
    }
}