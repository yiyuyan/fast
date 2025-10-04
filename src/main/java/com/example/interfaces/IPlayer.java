package com.example.interfaces;

/**
 * &#064;Author: KSmc_brigade
 * &#064;Date: 2025/10/4 上午8:54
 */
public interface IPlayer {
    void setKillAura(boolean active);
    void setEnabled(boolean value);

    boolean isEnabled();
    boolean isKillAuraEnabled();
}
