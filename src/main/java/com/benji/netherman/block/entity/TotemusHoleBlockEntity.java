package com.benji.netherman.block.entity;

import com.benji.netherman.ModSounds;
import com.benji.netherman.NetherExp;
import com.benji.netherman.entity.TotemusPuzzleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TotemusHoleBlockEntity extends BlockEntity {

    public static final int STATE_INACTIVE = 0;
    public static final int STATE_ACTIVE = 1;
    public static final int STATE_SOLVED = 2;

    private int puzzleState = STATE_INACTIVE;
    private int myColor = -1;

    private boolean isMaster = false;
    private BlockPos masterPos = null;
    private List<BlockPos> linkedHoles = new ArrayList<>();

    private int expectedColor = 0;
    private int cooldown = 0;
    private TotemusPuzzleEntity myEntity = null;

    public TotemusHoleBlockEntity(BlockPos pos, BlockState state) {
        super(NetherExp.TOTEMUS_HOLE_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TotemusHoleBlockEntity entity) {
        if (level.isClientSide() || entity.puzzleState == STATE_SOLVED) return;

        if (entity.cooldown > 0) {
            entity.cooldown--;
            return;
        }

        if (entity.puzzleState == STATE_INACTIVE) {
            // Проверяем игроков в радиусе 10 блоков
            AABB detectionBox = new AABB(pos).inflate(10.0D);
            if (!level.getEntitiesOfClass(Player.class, detectionBox).isEmpty()) {
                entity.initiatePuzzleNetwork();
            }
        }
    }

    private void initiatePuzzleNetwork() {
        List<BlockPos> foundHoles = new ArrayList<>();
        int searchRadius = 15;
        for (BlockPos checkPos : BlockPos.betweenClosed(worldPosition.offset(-searchRadius, -searchRadius, -searchRadius), worldPosition.offset(searchRadius, searchRadius, searchRadius))) {
            if (level.getBlockEntity(checkPos) instanceof TotemusHoleBlockEntity hole) {
                if (hole.puzzleState == STATE_INACTIVE) {
                    foundHoles.add(checkPos.immutable());
                }
            }
        }

        if (foundHoles.size() >= 6) {
            this.isMaster = true;
            this.linkedHoles = foundHoles.subList(0, 6);
            this.expectedColor = 0;

            List<Integer> colors = Arrays.asList(0, 1, 2, 3, 4, 5);
            Collections.shuffle(colors);

            for (int i = 0; i < 6; i++) {
                BlockPos targetPos = this.linkedHoles.get(i);
                if (level.getBlockEntity(targetPos) instanceof TotemusHoleBlockEntity hole) {
                    hole.activate(this.worldPosition, colors.get(i));
                }
            }
        }
    }

    public void activate(BlockPos master, int color) {
        this.masterPos = master;
        this.myColor = color;
        this.puzzleState = STATE_ACTIVE;

        TotemusPuzzleEntity entity = NetherExp.TOTEMUS_PUZZLE.get().create(level);
        if (entity != null) {
            entity.setPos(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0, this.worldPosition.getZ() + 0.5);
            entity.setColor(color);
            level.addFreshEntity(entity);
            this.myEntity = entity;

            level.playSound(null, this.worldPosition, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 1.0F, 0.5F);
        }
    }

    public void reportDeath(int colorKilled, Player player) {
        this.myEntity = null;

        if (this.isMaster) {
            this.handleKillSequence(colorKilled, player);
        } else if (this.masterPos != null) {
            if (level.getBlockEntity(masterPos) instanceof TotemusHoleBlockEntity master) {
                master.handleKillSequence(colorKilled, player);
            }
        }
    }

    private void handleKillSequence(int colorKilled, Player player) {
        if (colorKilled == this.expectedColor) {
            this.expectedColor++;

            if (this.expectedColor >= 6) {
                this.solvePuzzle(player);
            }
        } else {
            this.failPuzzle();
        }
    }

    private void failPuzzle() {
        level.playSound(null, this.worldPosition, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 0.5F);

        for (BlockPos pos : this.linkedHoles) {
            if (level.getBlockEntity(pos) instanceof TotemusHoleBlockEntity hole) {
                if (hole.myEntity != null && hole.myEntity.isAlive()) {
                    hole.myEntity.discard();
                    hole.myEntity = null;
                }
                hole.puzzleState = STATE_INACTIVE;
                hole.cooldown = 60;
                hole.isMaster = false;
            }
        }
    }

    private void solvePuzzle(Player player) {
        level.playSound(null, this.worldPosition, ModSounds.GIANT_BELL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.5, this.worldPosition.getZ() + 0.5,
                    100, 0.5, 0.5, 0.5, 0.2);
        }

        ItemEntity key = new ItemEntity(level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.5, this.worldPosition.getZ() + 0.5, new ItemStack(NetherExp.MAZE_KEY.get()));
        level.addFreshEntity(key);

        for (BlockPos pos : this.linkedHoles) {
            if (level.getBlockEntity(pos) instanceof TotemusHoleBlockEntity hole) {
                hole.puzzleState = STATE_SOLVED;
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("PuzzleState", this.puzzleState);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.puzzleState = tag.getInt("PuzzleState");
    }
}