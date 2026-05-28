package com.benji.netherman.client;

import com.benji.netherman.NetherExp;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NetherExp.MODID, value = Dist.CLIENT)
public class ClientEffectEvents {
    private static int clickTimer = 0;
    private static double rotationAngle = 0;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.player != Minecraft.getInstance().player) return;
        LocalPlayer player = (LocalPlayer) event.player;

        if (player.hasEffect(NetherExp.MANIPULATION_EFFECT.get())) {
            // 1. Плавный поворот камеры по синусоиде
            rotationAngle += 0.05;
            player.setYRot(player.getYRot() + (float) Math.sin(rotationAngle) * 0.8F);
            player.setXRot(player.getXRot() + (float) Math.cos(rotationAngle * 0.5) * 0.3F);

            // 2. Случайные удары (ЛКМ) раз в пару секунд
            clickTimer--;
            if (clickTimer <= 0) {
                // ИСПРАВЛЕНИЕ 1: Имитируем физическое нажатие кнопки атаки (ЛКМ)
                KeyMapping.click(Minecraft.getInstance().options.keyAttack.getKey());
                clickTimer = player.getRandom().nextInt(40) + 20; // задержка от 1 до 3 секунд
            }
        }
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.hasEffect(NetherExp.MANIPULATION_EFFECT.get())) return;

        // Поиск лавы или огня в радиусе 10 блоков
        BlockPos playerPos = player.blockPosition();
        BlockPos dangerPos = null;
        outerLoop:
        for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-10, -5, -10), playerPos.offset(10, 5, 10))) {
            if (player.level().getBlockState(pos).is(Blocks.LAVA) || player.level().getBlockState(pos).is(Blocks.FIRE)) {
                dangerPos = pos.immutable();
                break outerLoop;
            }
        }

        if (dangerPos != null) {
            // Ведём игрока к лаве
            Vec3 lookVec = player.getLookAngle();
            Vec3 targetVec = new Vec3(dangerPos.getX() + 0.5 - player.getX(), 0, dangerPos.getZ() + 0.5 - player.getZ()).normalize();

            double dot = lookVec.x * targetVec.x + lookVec.z * targetVec.z;
            double det = lookVec.x * targetVec.z - lookVec.z * targetVec.x;
            double angle = Math.atan2(det, dot);

            // ИСПРАВЛЕНИЕ 2: Используем getInput() вместо getMovementInput()
            event.getInput().forwardImpulse = angle > -Math.PI/4 && angle < Math.PI/4 ? 1.0F : 0.0F;
            event.getInput().leftImpulse = angle >= Math.PI/4 && angle < 3*Math.PI/4 ? 1.0F : (angle <= -Math.PI/4 && angle > -3*Math.PI/4 ? -1.0F : 0.0F);
        } else {
            // Если лавы нет — просто хаотичный WASD
            if (player.tickCount % 15 == 0) {
                // ИСПРАВЛЕНИЕ 2: Используем getInput() вместо getMovementInput()
                event.getInput().forwardImpulse = player.getRandom().nextFloat() * 2.0F - 1.0F;
                event.getInput().leftImpulse = player.getRandom().nextFloat() * 2.0F - 1.0F;
            }
        }
    }
}