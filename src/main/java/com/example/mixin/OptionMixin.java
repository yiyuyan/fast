package com.example.mixin;

import com.example.interfaces.IPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * &#064;Author: KSmc_brigade
 * &#064;Date: 2025/10/4 上午9:11
 */
@Mixin(SimpleOption.class)
public abstract class OptionMixin {
    @Shadow public abstract String toString();

    @Inject(method = "getValue",at = @At("RETURN"),cancellable = true)
    public void get(CallbackInfoReturnable<Double> cir){
        MinecraftClient MC = MinecraftClient.getInstance();
        if(MC.player!=null && ((IPlayer)MC.player).isEnabled() && this.toString().equals(MC.options.getGamma().toString())){
            cir.setReturnValue(15d);
        }
    }
}
