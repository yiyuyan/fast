package com.example;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class PlayerMarkerRenderer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static double RANGE;
    public static PlayerEntity nearestPlayer2 = null;
    static int hp = 0;

    public static void renderPlayerMarkers(MatrixStack matrices, float tickDelta) {
        if (mc.player != null && mc.world != null) {
            mc.world.getPlayers().stream().filter((player) -> {
                return player != mc.player;
            }).filter((player) -> {
                return mc.player.squaredDistanceTo(player) <= RANGE * RANGE;
            }).forEach((player) -> {
                Vec3d pos = player.getPos().add(0.0D, (double)player.getHeight() + 0.5D, 0.0D);
                String var10002 = player.getEntityName();
                renderFloatingText(matrices, pos, Text.of("§f玩家:" + var10002 + "血量:" + player.getHealth() + "护甲" + player.getArmor()), tickDelta);
            });
            mc.world.getEntities().forEach((entity) -> {
                if (!(entity instanceof PlayerEntity) && entity instanceof LivingEntity) {
                    if (mc.player.squaredDistanceTo(entity) <= RANGE * RANGE) {
                        String translationKey = entity.getType().getTranslationKey();
                        Text displayName = Text.translatable(translationKey);
                        float health = ((LivingEntity)entity).getHealth();
                        int armor = ((LivingEntity)entity).getArmor();
                        boolean isHostile = entity instanceof HostileEntity;
                        MutableText var10000;
                        Object[] var10002;
                        MutableText text;
                        if (isHostile) {
                            var10000 = Text.literal("").append(displayName);
                            var10002 = new Object[]{health};
                            text = var10000.append(" §c血量: " + String.format(" §c %.1f", var10002)).append(" |  §c 护甲: " + armor);
                        } else {
                            var10000 = Text.literal("").append(displayName);
                            var10002 = new Object[]{health};
                            text = var10000.append(" 血量: " + String.format("%.1f", var10002)).append(" | 护甲: " + armor);
                        }

                        Vec3d pos = entity.getPos().add(0.0D, (double)entity.getHeight() + 0.5D, 0.0D);
                        renderFloatingText(matrices, pos, text, tickDelta);
                    }
                }
            });
        }
    }

    private static void renderFloatingText(MatrixStack matrices, Vec3d pos, Text text, float ignoredTickDelta) {
        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        matrices.push();
        matrices.translate(pos.x - cameraPos.x, pos.y - cameraPos.y, pos.z - cameraPos.z);
        matrices.multiply(mc.gameRenderer.getCamera().getRotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        TextRenderer textRenderer = mc.textRenderer;
        int width = textRenderer.getWidth(text);
        matrices.translate(0.0F, 0.0F, 0.0F);
        matrices.translate((float)(-width) / 2.0F, 0.0F, 0.0F);
        Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
        textRenderer.draw(text, 0.0F, 0.0F, 16776960, false, matrices.peek().getPositionMatrix(), immediate, TextLayerType.SEE_THROUGH, 0, 15728880);
        immediate.draw();
        matrices.pop();
        RenderSystem.enableDepthTest();
    }

    public static String findNearestPlayer(MinecraftClient client) {
        String message = "";
        PlayerEntity currentPlayer = client.player;
        PlayerEntity nearestPlayer = null;
        if (client.world != null) {
            assert currentPlayer != null;

            Stream var10000 = client.world.getPlayers().stream().filter((player) -> {
                return player != currentPlayer;
            }).filter((player) -> {
                return ((PlayerEntity)Objects.requireNonNull(currentPlayer)).squaredDistanceTo(player) <= RANGE * RANGE;
            });
            Objects.requireNonNull(currentPlayer);
            nearestPlayer = (PlayerEntity)var10000.min(Comparator.comparingDouble((t)->{
                return currentPlayer.squaredDistanceTo((PlayerEntity)t);
            })).orElse((Object)null);
            var10000 = client.world.getPlayers().stream().filter((player) -> {
                return player != currentPlayer;
            }).filter((player) -> {
                return ((PlayerEntity)Objects.requireNonNull(currentPlayer)).squaredDistanceTo(player) <= RANGE * RANGE;
            });
            Objects.requireNonNull(currentPlayer);
            nearestPlayer2 = (PlayerEntity)var10000.min(Comparator.comparingDouble((t)->{
                return currentPlayer.squaredDistanceTo((PlayerEntity)t);
            })).orElse((Object)null);
        }

        if (nearestPlayer != null) {
            double distance = Math.sqrt(currentPlayer.squaredDistanceTo(nearestPlayer));
            double var6 = RANGE;
            message = String.format("最近玩家: %s 距离: (%.1f格) 侦测范围:" + var6 + " 生命值:" + nearestPlayer.getHealth() + " 手持物品:" + nearestPlayer.getMainHandStack().getName().getString() + " 护甲值:" + nearestPlayer.getArmor(), nearestPlayer.getName().getString(), distance);
            hp = (int)nearestPlayer.getHealth();
        }

        return message;
    }

    public double squaredDistanceTo(PlayerEntity current,Entity entity) {
        return current.squaredDistanceTo(entity);
    }

    public static String[] buildArmorInfoArray() {
        PlayerEntity player = nearestPlayer2;
        String[] defaultInfo = new String[]{"--", "§b--", "§b--", "§b--"};
        if (player != null && player.isAlive()) {
            String[] armorInfo = new String[4];
            EquipmentSlot[] slots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

            for(int i = 0; i < slots.length; ++i) {
                try {
                    EquipmentSlot slot = slots[i];
                    ItemStack armorStack = player.getEquippedStack(slot);
                    StringBuilder infoBuilder = new StringBuilder();
                    infoBuilder.append("§b").append(getChineseSlotName(slot)).append("§b: ");
                    if (armorStack.isEmpty()) {
                        infoBuilder.append("§b无装备");
                    } else {
                        String armorName = armorStack.getName().getString();
                        int armorValue = 0;
                        float toughness = 0.0F;
                        int protectionLevel = EnchantmentHelper.getLevel(Enchantments.PROTECTION, armorStack);
                        String durabilityInfo = "";
                        if (armorStack.isDamageable()) {
                            int maxDurability = armorStack.getMaxDamage();
                            int currentDurability = maxDurability - armorStack.getDamage();
                            float percent = (float)currentDurability / (float)maxDurability;
                            String durabilityColor = (double)percent > 0.75D ? "§a" : ((double)percent > 0.25D ? "§e" : "§c");
                            durabilityInfo = String.format("%s耐久: %d/%d", durabilityColor, currentDurability, maxDurability);
                        }

                        Item var19 = armorStack.getItem();
                        if (var19 instanceof ArmorItem) {
                            ArmorItem armorItem = (ArmorItem)var19;
                            armorValue = armorItem.getProtection();
                            toughness = armorItem.getMaterial().getToughness();
                        }

                        infoBuilder.append("§b名称:").append(armorName).append(",").append("§b护甲:").append(armorValue).append(",").append("§b韧性:").append(String.format("%.1f", toughness)).append(",").append("§b保护:").append(protectionLevel).append(",").append(durabilityInfo);
                    }

                    armorInfo[i] = infoBuilder.toString();
                } catch (Exception var17) {
                    armorInfo[i] = " " + getChineseSlotName(slots[i]) + " ";
                }
            }

            return armorInfo;
        } else {
            return defaultInfo;
        }
    }

    private static String getChineseSlotName(EquipmentSlot slot) {
        String var10000;
        switch(slot) {
            case HEAD:
                var10000 = "§b头盔";
                break;
            case CHEST:
                var10000 = "§b胸甲";
                break;
            case LEGS:
                var10000 = "§b护腿";
                break;
            case FEET:
                var10000 = "§b靴子";
                break;
            default:
                var10000 = "未知";
        }

        return var10000;
    }

    public static float calculateMaxDamage(int health, int armor, float toughness, int protectionLevel) {
        if (health <= 0) {
            return 0.0F;
        } else {
            armor = Math.min(Math.max(armor, 0), 20);
            toughness = Math.max(toughness, 0.0F);
            protectionLevel = Math.min(Math.max(protectionLevel, 0), 4);
            float armorReduction = Math.min((float)armor * 0.04F, 0.8F);
            float toughnessFactor = Math.min(toughness / (toughness + 8.0F), 0.8F);
            float armorEffectiveness = armorReduction * (1.0F - toughnessFactor);
            int epf = Math.min(protectionLevel * 2, 20);
            float enchantmentReduction = Math.min((float)epf * 0.03F, 0.6F);
            float totalReduction = armorEffectiveness + (1.0F - armorEffectiveness) * enchantmentReduction;
            if (totalReduction >= 0.9999F) {
                return Float.POSITIVE_INFINITY;
            } else {
                float maxDamage = (float)health / (1.0F - totalReduction);
                return (float)Math.round(maxDamage * 10.0F) / 10.0F;
            }
        }
    }
}
