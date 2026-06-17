package com.benji.netherman.compat;

import com.benji.netherman.NetherExp;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

public class CuriosCompat {

    @SuppressWarnings("deprecation")
    public static ItemStack getTotemFromCurios(Player player) {
        Optional<SlotResult> result = CuriosApi.getCuriosHelper().findFirstCurio(player, NetherExp.CHANCE_TOTEM.get());

        if (result.isPresent()) {
            return result.get().stack();
        }

        return null;
    }
}