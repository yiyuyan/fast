package com.example;

import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class method {
    public static void getEntitiesAroundPlayer(PlayerEntity player, World world, int a) {
        if (player != null && world != null) {
            DecimalFormat decimalFormat = new DecimalFormat("#.0");
            Vec3d playerPos = player.getPos();
            Box searchArea = new Box(playerPos.subtract(a, a, a), playerPos.add(a, a, a));
            List<Entity> entitiesInRange = world.getEntitiesByClass(Entity.class, searchArea, (entityx) -> {
                return entityx != player;
            });
            Entity closestEntity = getEntity(player, entitiesInRange);

            if (closestEntity != null) {
                float x = Float.parseFloat(decimalFormat.format(closestEntity.getX()));
                double y = Float.parseFloat(decimalFormat.format(closestEntity.getY()));
                float z = Float.parseFloat(decimalFormat.format(closestEntity.getZ()));
                Vec3d pos1 = new Vec3d(player.getX(), player.getY(), player.getZ());
                Vec3d pos2 = new Vec3d(x, y, z);
                int distance = (int)calculateDistance(pos1, pos2);
                Box collisionBox = closestEntity.getBoundingBox();
                double collisionBoxHeight = collisionBox.getYLength();
                double YR = Double.parseDouble(decimalFormat.format(collisionBoxHeight));
                player.sendMessage(Text.of("§c锁定距离:" + distance + "/" + a + "§9目标高度:" + YR), true);

                double ay;
                if (YR > 2.0D) {
                    ay = YR - 2.0D;
                    y = y + ay - 0.1D;
                } else if (YR < 2.0D && YR != 0.0D) {
                    ay = 2.0D - YR;
                    y = y - ay + 0.1D;
                }

                lookAt((double)x, y, (double)z);
            }

        }
    }

    private static @Nullable Entity getEntity(PlayerEntity player, List<Entity> entitiesInRange) {
        double minDistance = Double.MAX_VALUE;
        Entity closestEntity = null;
        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();

        for (Object o : entitiesInRange) {
            Entity entity = (Entity) o;
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                if (livingEntity.getHealth() > 0.0F) {
                    double entityX = entity.getX();
                    double entityY = entity.getY();
                    double entityZ = entity.getZ();
                    double distance = Math.sqrt(Math.pow(playerX - entityX, 2.0D) + Math.pow(playerY - entityY, 2.0D) + Math.pow(playerZ - entityZ, 2.0D));
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestEntity = entity;
                    }
                }
            }
        }
        return closestEntity;
    }

    public static void lookAt(double targetX, double targetY, double targetZ) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            Vec3d eyePos = new Vec3d(player.getX(), player.getY(), player.getZ());
            double dx = targetX - eyePos.x;
            double dy = targetY - eyePos.y;
            double dz = targetZ - eyePos.z;
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
            float pitch = (float)(-Math.toDegrees(Math.atan2(dy, horizontalDistance)));
            player.setYaw(MathHelper.wrapDegrees(yaw));
            player.setPitch(MathHelper.wrapDegrees(pitch));
        }
    }

    public static double calculateDistance(Vec3d pos1, Vec3d pos2) {
        double dx = pos2.x - pos1.x;
        double dy = pos2.y - pos1.y;
        double dz = pos2.z - pos1.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
