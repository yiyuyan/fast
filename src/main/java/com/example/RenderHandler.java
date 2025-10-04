//
// Jar Analyzer by 4ra1n
// (powered by FernFlower decompiler)
//
package com.example;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RenderHandler {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final List targetPositions = new ArrayList();
    public static double range = 0.0D;
    public float red = 0.0F;
    public float green = 0.0F;
    public float blue = 0.0F;
    public float alpha = 0.0F;
    boolean playerok;

    public RenderHandler() {
        List entityPositions = this.getNearbyEntityPositions();
        Iterator var2 = entityPositions.iterator();

        while(var2.hasNext()) {
            Vec3d pos = (Vec3d)var2.next();
            LivingEntity entity = this.getEntityAtPosition(pos);
            if (entity != mc.player) {
                this.targetPositions.add(new Vec3d(pos.x, pos.y, pos.z));
            }
        }

    }

    public void onRenderWorld(WorldRenderContext context) {
        if (mc.player != null && mc.world != null) {
            Vec3d playerPos = mc.player.getCameraPosVec(context.tickDelta());
            Vec3d viewVector = mc.player.getRotationVec(context.tickDelta());
            double crosshairDistance = 5.0D;
            Vec3d crosshairPos = playerPos.add(viewVector.multiply(crosshairDistance));
            Iterator var7 = this.targetPositions.iterator();

            while(var7.hasNext()) {
                Vec3d targetPos = (Vec3d)var7.next();
                LivingEntity targetEntity = this.getEntityAtPosition(targetPos);
                boolean isPlayer = targetEntity instanceof PlayerEntity;
                float red = 1.0F;
                float green = 1.0F;
                float blue = isPlayer ? 1.0F : 0.0F;
                this.playerok = isPlayer;
                this.renderLine(crosshairPos, targetPos, context.matrixStack(), context.camera(), red, green, blue);
                this.renderSquare(targetPos, context.matrixStack(), context.camera(), this.playerok);
            }

        }
    }

    private void renderLine(Vec3d start, Vec3d end, MatrixStack matrixStack, Camera camera, float red, float green, float blue) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(2.0F);
        matrixStack.push();
        Vec3d cameraPos = camera.getPos();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        buffer.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        float alpha = 1.0F;
        buffer.vertex(matrixStack.peek().getPositionMatrix(), (float)start.x, (float)start.y, (float)start.z).color(red, green, blue, alpha).next();
        buffer.vertex(matrixStack.peek().getPositionMatrix(), (float)end.x, (float)end.y, (float)end.z).color(red, green, blue, alpha).next();
        tessellator.draw();
        matrixStack.pop();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private LivingEntity getEntityAtPosition(Vec3d pos) {
        if (mc.world == null) {
            return null;
        } else {
            Box searchBox = new Box(pos.add(-0.3D, -0.3D, -0.3D), pos.add(0.3D, 0.3D, 0.3D));
            List entities = mc.world.getEntitiesByClass(LivingEntity.class, searchBox, (entity) -> {
                return true;
            });
            return entities.isEmpty() ? null : (LivingEntity)entities.get(0);
        }
    }

    private void renderSquare(Vec3d center, MatrixStack matrixStack, Camera camera, boolean playerok) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(2.0F);
        matrixStack.push();
        Vec3d cameraPos = camera.getPos();
        matrixStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Vec3d corner1 = center.add(-1.0D, 0.0D, -1.0D);
        Vec3d corner2 = center.add(1.0D, 0.0D, -1.0D);
        Vec3d corner3 = center.add(1.0D, 0.0D, 1.0D);
        Vec3d corner4 = center.add(-1.0D, 0.0D, 1.0D);
        buffer.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        if (playerok) {
            this.red = 1.0F;
            this.green = 1.0F;
            this.blue = 1.0F;
            this.alpha = 1.0F;
        } else {
            this.red = 1.0F;
            this.green = 1.0F;
            this.blue = 0.0F;
            this.alpha = 1.0F;
        }

        this.drawLine(buffer, matrixStack, corner1, corner2, this.red, this.green, this.blue, this.alpha);
        this.drawLine(buffer, matrixStack, corner2, corner3, this.red, this.green, this.blue, this.alpha);
        this.drawLine(buffer, matrixStack, corner3, corner4, this.red, this.green, this.blue, this.alpha);
        this.drawLine(buffer, matrixStack, corner4, corner1, this.red, this.green, this.blue, this.alpha);
        tessellator.draw();
        matrixStack.pop();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void drawLine(BufferBuilder buffer, MatrixStack matrixStack, Vec3d start, Vec3d end, float red, float green, float blue, float alpha) {
        buffer.vertex(matrixStack.peek().getPositionMatrix(), (float)start.x, (float)start.y, (float)start.z).color(red, green, blue, alpha).next();
        buffer.vertex(matrixStack.peek().getPositionMatrix(), (float)end.x, (float)end.y, (float)end.z).color(red, green, blue, alpha).next();
    }

    public List getNearbyEntityPositions() {
        List entityPositions = new ArrayList();
        if (mc.player != null && mc.world != null) {
            Vec3d playerPos = mc.player.getPos();
            Box searchBox = new Box(playerPos.add(-range, -range, -range), playerPos.add(range, range, range));
            World world = mc.world;
            List entities = world.getEntitiesByClass(LivingEntity.class, searchBox, (entityx) -> {
                return true;
            });
            Iterator var6 = entities.iterator();

            while(var6.hasNext()) {
                LivingEntity entity = (LivingEntity)var6.next();
                entityPositions.add(entity.getPos());
            }

            return entityPositions;
        } else {
            return entityPositions;
        }
    }

    public static void sethaha(double a) {
        range = a;
    }
}
