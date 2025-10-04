package com.example.mixin;

import com.example.interfaces.IPlayer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * &#064;Author: KSmc_brigade
 * &#064;Date: 2025/10/4 上午8:54
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerMixin extends LivingEntity implements IPlayer{

    @Unique
    private boolean killAura = false;
    @Unique
    private boolean active = false;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    @Unique
    public void setKillAura(boolean active) {
        this.killAura = active;
    }

    @Override
    @Unique
    public void setEnabled(boolean value) {
        this.active = value;
    }

    @Override
    @Unique
    public boolean isEnabled() {
        return this.active;
    }

    @Override
    @Unique
    public boolean isKillAuraEnabled() {
        return this.killAura;
    }
}
