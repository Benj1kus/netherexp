package com.benji.netherman.entity;

import com.benji.netherman.NetherExp;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class AzazelEarthquakeEntity extends Projectile implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int lifeTicks = 0;

    private final List<CrackBranch> branches = new ArrayList<>();

    public AzazelEarthquakeEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        if (this.level().isClientSide()) return;

        if (lifeTicks == 1) {
            for (int i = 0; i < 4; i++) {
                float angle = (90.0F * i) + (this.random.nextFloat() * 20.0F - 10.0F); // 0, 90, 180, 270
                branches.add(new CrackBranch(angle, 0));
            }
        }

        if (lifeTicks <= 20) {
            List<CrackBranch> newBranches = new ArrayList<>();

            for (CrackBranch branch : branches) {
                branch.currentRadius += 1.0;
                branch.angle += (this.random.nextFloat() - 0.5F) * 30.0F;

                if (this.random.nextFloat() < 0.15F && branches.size() + newBranches.size() < 24) {
                    newBranches.add(new CrackBranch(branch.angle + (this.random.nextBoolean() ? 45 : -45), branch.currentRadius));
                }

                breakEarth((ServerLevel) this.level(), branch);
            }
            branches.addAll(newBranches);
        }

        if (lifeTicks > 30) {
            this.discard();
        }
    }

    private void breakEarth(ServerLevel level, CrackBranch branch) {
        double rad = Math.toRadians(branch.angle);
        int targetX = this.getBlockX() + (int) (Math.cos(rad) * branch.currentRadius);
        int targetZ = this.getBlockZ() + (int) (Math.sin(rad) * branch.currentRadius);
        int startY = this.getBlockY() - 1;

        for (int depth = 0; depth < 3; depth++) {
            BlockPos pos = new BlockPos(targetX, startY - depth, targetZ);
            BlockState state = level.getBlockState(pos);

            if (!state.isAir() && state.getDestroySpeed(level, pos) >= 0 && !state.hasBlockEntity()) {
                if (depth == 2) {
                    BlockState magmaOrLava = this.random.nextBoolean() ? Blocks.LAVA.defaultBlockState() : NetherExp.POTENT_MAGMA.get().defaultBlockState();
                    level.setBlock(pos, magmaOrLava, 3);
                } else {
                    level.destroyBlock(pos, false);
                    level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2, 0.2, 0.2, 0.2, 0.05);
                }
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    private static class CrackBranch {
        float angle;
        double currentRadius;

        CrackBranch(float angle, double startRadius) {
            this.angle = angle;
            this.currentRadius = startRadius;
        }
    }
}