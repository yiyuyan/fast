package com.example;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.interfaces.IPlayer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class Fast implements ModInitializer {
    private int tickCounter = 0;
    private boolean isInGame = false;
    public boolean wasRightClickPressed;
    int[] a;
    int[] b;
    int[] c;
    int a2;
    int b2;
    int c2;
    int fps;
    int range;

    public static KeyBinding killAuraKey = KeyBindingRegistryImpl.registerKeyBinding(new KeyBinding("KillAura",GLFW.GLFW_KEY_R,KeyBinding.GAMEPLAY_CATEGORY));

    public Fast() {
        this.wasRightClickPressed = false;
        this.a = new int[]{0, 0, 0, 0};
        this.b = new int[]{0, 0, 0, 0};
        this.c = new int[]{0, 0, 0, 0};
        this.a2 = 0;
        this.b2 = 0;
        this.c2 = 0;
        this.fps = 0;
        this.range = 50;
    }

    public void onInitialize() {
        ClientTickEvents.START_CLIENT_TICK.register((Client) -> {

            if(Client.player==null) return;
            //将部分变量转换到 Mixin 当中
            IPlayer iPlayer = ((IPlayer) Client.player);

            boolean isPressed = InputUtil.isKeyPressed(Client.getWindow().getHandle(), GLFW.GLFW_KEY_C);
            //无限夜视已更改至 OptionMixin 实现
            if (isPressed && !iPlayer.isEnabled() && Client.player != null) {
                iPlayer.setEnabled(true);
                Client.player.sendMessage(Text.of("§e战术护目镜启动！"));
            }

            boolean isPressed2 = InputUtil.isKeyPressed(Client.getWindow().getHandle(), GLFW.GLFW_KEY_X);
            if (isPressed2 && iPlayer.isEnabled() && Client.player != null) {
                iPlayer.setEnabled(false);
                Client.player.sendMessage(Text.of("§e关闭！"));
            }

            if(killAuraKey.isPressed()){
                iPlayer.setKillAura(!iPlayer.isKillAuraEnabled());
                Client.player.sendMessage(Text.translatable("§eKillAuraMode: %s",iPlayer.isKillAuraEnabled()));
            }
            //将杀戮光环和战术目镜拆分为单独的两个功能
            if (iPlayer.isKillAuraEnabled()) {

                int killAuraRange = (int) Math.max(10,(Client.interactionManager==null?0:Client.interactionManager.getReachDistance()));
                boolean isRightClickPressed = GLFW.glfwGetMouseButton(Client.getWindow().getHandle(), 1) == 1;
                if (isRightClickPressed && Client.player != null) {
                    method.getEntitiesAroundPlayer(Client.player, Client.world, this.range);
                }

                this.wasRightClickPressed = isRightClickPressed;
                method.getEntitiesAroundPlayer(Client.player, Client.world, killAuraRange);
                if (Client.player != null && Client.targetedEntity != null && Client.interactionManager != null) {
                    if(isFair(Client.player,Client.targetedEntity))Client.interactionManager.attackEntity(Client.player, Client.targetedEntity);
                    //Client.player.sendMessage(Text.of("§c杀戮光环启动！"));
                }
            }

            if (this.isInGame) {
                ++this.tickCounter;
                if (this.tickCounter == 100 && Client.player != null) {
                    Client.player.sendMessage(Text.of("功能及使用:"), false);
                    Client.player.sendMessage(Text.of("      "), false);
                    Client.player.sendMessage(Text.of("1.本战术护目镜主要针对于打怪，采用了防守式自瞄设计（索敌范围50格，优先锁定距离自身最近的生物）"), false);
                    Client.player.sendMessage(Text.of("      "), false);
                    Client.player.sendMessage(Text.of("2.功能:1.生物高度差锁头（任何生物，也包括玩家趴下后的状态）2.近战杀戮光环退敌控距（距离自身10格以内触发）3.流畅的线条标记指向(玩家白线，其他黄线) 4.玩家/生物信息显示(详细/简易)"), false);
                    Client.player.sendMessage(Text.of("      "), false);
                    Client.player.sendMessage(Text.of("3.使用:按下c启动，x关闭，右键，按 R 开关杀戮光环，杀戮光环将在十格以内锁定距离自身最近生物并进行攻击"), false);
                    Client.player.sendMessage(Text.of("§c警告事项:请乎将帧数设置为无限制，过高的帧数很可能在一些体量过大的整合包中导致渲染崩溃"), false);
                }

                if (this.tickCounter >= 170) {
                    if (Client.player != null) {
                        Client.player.sendMessage(Text.of("战术护目镜1.0§b【作者:millok_ , KSmc_brigade】"), false);
                    }

                    this.isInGame = false;
                    this.tickCounter = 0;
                }
            }

        });
        WorldRenderEvents.END.register((context) -> {
            if (MinecraftClient.getInstance().player!=null && ((IPlayer)MinecraftClient.getInstance().player).isEnabled()) {
                (new RenderHandler()).onRenderWorld(context);
            }

        });
        RenderHandler.sethaha((double)this.range);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            this.isInGame = true;
            this.tickCounter = 0;
        });
        WorldRenderEvents.END.register((context) -> {
            if (MinecraftClient.getInstance().player!=null && ((IPlayer)MinecraftClient.getInstance().player).isEnabled()) {
                PlayerMarkerRenderer.renderPlayerMarkers(context.matrixStack(), context.tickDelta());
            }

        });
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            drawContext.drawText(client.textRenderer, PlayerMarkerRenderer.findNearestPlayer(client), 10, 10, 16777215, false);
            if (PlayerMarkerRenderer.buildArmorInfoArray()[0].equals("--")) {
                drawContext.drawText(client.textRenderer, "", 10, 25, 16777215, false);
            } else {
                drawContext.drawText(client.textRenderer, "§e护甲信息:", 10, 25, 16777215, false);
            }

            drawContext.drawText(client.textRenderer, PlayerMarkerRenderer.buildArmorInfoArray()[0], 10, 40, 16777215, false);
            drawContext.drawText(client.textRenderer, PlayerMarkerRenderer.buildArmorInfoArray()[1], 10, 55, 16777215, false);
            drawContext.drawText(client.textRenderer, PlayerMarkerRenderer.buildArmorInfoArray()[2], 10, 70, 16777215, false);
            drawContext.drawText(client.textRenderer, PlayerMarkerRenderer.buildArmorInfoArray()[3], 10, 85, 16777215, false);
            Pattern pattern = Pattern.compile("\\d+\\.?\\d*");
            if (PlayerMarkerRenderer.buildArmorInfoArray()[0] != null) {
                Matcher matcherxxx = pattern.matcher(PlayerMarkerRenderer.buildArmorInfoArray()[0]);
                ArrayList numbers = new ArrayList();

                while(matcherxxx.find()) {
                    numbers.add(Double.parseDouble(matcherxxx.group()));
                }

                if (!numbers.isEmpty()) {
                    this.a[0] = ((Double)numbers.get(0)).byteValue();
                }

                if (1 < numbers.size()) {
                    this.b[0] = ((Double)numbers.get(1)).byteValue();
                }

                if (2 < numbers.size()) {
                    this.c[0] = ((Double)numbers.get(2)).byteValue();
                }
            }

            Pattern pattern2 = Pattern.compile("\\d+\\.?\\d*");
            if (PlayerMarkerRenderer.buildArmorInfoArray()[0] != null) {
                Matcher matcher = pattern2.matcher(PlayerMarkerRenderer.buildArmorInfoArray()[1]);
                ArrayList numbersx = new ArrayList();

                while(matcher.find()) {
                    numbersx.add(Double.parseDouble(matcher.group()));
                }

                if (!numbersx.isEmpty()) {
                    this.a[1] = ((Double)numbersx.get(0)).byteValue();
                }

                if (1 < numbersx.size()) {
                    this.b[1] = ((Double)numbersx.get(1)).byteValue();
                }

                if (2 < numbersx.size()) {
                    this.c[1] = ((Double)numbersx.get(2)).byteValue();
                }
            }

            Pattern pattern3 = Pattern.compile("\\d+\\.?\\d*");
            if (PlayerMarkerRenderer.buildArmorInfoArray()[0] != null) {
                Matcher matcherx = pattern3.matcher(PlayerMarkerRenderer.buildArmorInfoArray()[2]);
                ArrayList numbersxx = new ArrayList();

                while(matcherx.find()) {
                    numbersxx.add(Double.parseDouble(matcherx.group()));
                }

                if (!numbersxx.isEmpty()) {
                    this.a[2] = ((Double)numbersxx.get(0)).byteValue();
                }

                if (1 < numbersxx.size()) {
                    this.b[2] = ((Double)numbersxx.get(1)).byteValue();
                }

                if (2 < numbersxx.size()) {
                    this.c[2] = ((Double)numbersxx.get(2)).byteValue();
                }
            }

            Pattern pattern4 = Pattern.compile("\\d+\\.?\\d*");
            if (PlayerMarkerRenderer.buildArmorInfoArray()[0] != null) {
                Matcher matcherxx = pattern4.matcher(PlayerMarkerRenderer.buildArmorInfoArray()[3]);
                ArrayList numbersxxx = new ArrayList();

                while(matcherxx.find()) {
                    numbersxxx.add(Double.parseDouble(matcherxx.group()));
                }

                if (!numbersxxx.isEmpty()) {
                    this.a[3] = ((Double)numbersxxx.get(0)).byteValue();
                }

                if (1 < numbersxxx.size()) {
                    this.b[3] = ((Double)numbersxxx.get(1)).byteValue();
                }

                if (2 < numbersxxx.size()) {
                    this.c[3] = ((Double)numbersxxx.get(2)).byteValue();
                }
            }

            this.a2 = this.a[0] + this.a[1] + this.a[2] + this.a[3];
            this.b2 = this.b[0] + this.b[1] + this.b[2] + this.b[3];
            int max = this.c[0];
            if (this.c[1] > max) {
                max = this.c[1];
            }

            if (this.c[2] > max) {
                max = this.c[2];
            }

            if (this.c[3] > max) {
                max = this.c[3];
            }

            this.c2 = max;
            if (PlayerMarkerRenderer.buildArmorInfoArray()[0].equals("--")) {
                drawContext.drawText(client.textRenderer, "", 10, 25, 16777215, false);
            } else {
                float var10004 = (float)this.b2;
                drawContext.drawText(client.textRenderer, "当前单次最大极限承伤:" + PlayerMarkerRenderer.calculateMaxDamage(PlayerMarkerRenderer.hp, this.a2, var10004, this.c2) + " （无穿透碎甲等影响）", 10, 100, 16777215, false);
                drawContext.drawText(client.textRenderer, "当前总盔甲值:" + this.a2 + " 韧性:" + this.b2 + " 最高保护等级:" + this.c2, 10, 115, 16777215, false);
            }

            ++this.fps;
            if (this.fps == 40) {
                this.fps = 0;
                this.a[0] = 0;
                this.a[1] = 0;
                this.a[2] = 0;
                this.a[3] = 0;
                this.b[0] = 0;
                this.b[1] = 0;
                this.b[2] = 0;
                this.b[3] = 0;
                this.c[0] = 0;
                this.c[1] = 0;
                this.c[2] = 0;
                this.c[3] = 0;
                this.a2 = 0;
                this.b2 = 0;
                this.c2 = 0;
            }

        });
        PlayerMarkerRenderer.RANGE = (double)this.range;
    }

    //判断攻击是否合法，减少被反作弊发现的可能性
    public boolean isFair(ClientPlayerEntity player, Entity target){
        if(!target.isAttackable() || !target.isAlive()) return false;
        if(target.isInvulnerable()) return false;

        float progress = player.getAttackCooldownProgress(1f);
        return !(progress > 0f && progress < 1f);
    }
}
