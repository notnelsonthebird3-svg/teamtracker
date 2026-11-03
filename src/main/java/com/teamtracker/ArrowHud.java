package com.teamtracker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class ArrowHud {
    private static final int[] COLORS = {
            0xFF5555, 0x55FF55, 0x55FFFF, 0xFFFF55, 0xFF55FF, 0xFFAA00, 0x5555FF, 0xFFFFFF
    };
    private static final int RADIUS = 55;

    public static void render(DrawContext ctx, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) return;
        if (!TeamManager.data.hudEnabled) return;

        int cx = ctx.getScaledWindowWidth() / 2;
        int cy = ctx.getScaledWindowHeight() / 2;

        int i = 0;
        for (String name : TeamManager.data.members) {
            PlayerEntity target = TeamTrackerClient.findPlayer(name);
            int color = 0xFF000000 | COLORS[i % COLORS.length];
            i++;
            if (target == null || target == mc.player) continue;

            double bearing = TeamTrackerClient.bearingTo(mc.player, target);
            // Convert to MC yaw space so we can compare with where the player is looking
            float targetYaw = (float) (bearing - 180.0);
            float rel = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw(tickDelta));

            double rad = Math.toRadians(rel);
            int px = cx + (int) (Math.sin(rad) * RADIUS);
            int py = cy - (int) (Math.cos(rad) * RADIUS);

            ctx.getMatrices().push();
            ctx.getMatrices().translate(px, py, 0);
            ctx.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rel));
            drawArrow(ctx, color);
            ctx.getMatrices().pop();

            int dist = (int) mc.player.distanceTo(target);
            String label = name + " " + dist + "m";
            ctx.drawCenteredTextWithShadow(mc.textRenderer, label, px, py + 12, color);
        }
    }

    private static void drawArrow(DrawContext ctx, int color) {
        // Arrow head (triangle built from rows), pointing up
        for (int r = 0; r < 7; r++) {
            ctx.fill(-r, -10 + r, r + 1, -9 + r, color);
        }
        // Shaft
        ctx.fill(-1, -3, 2, 6, color);
    }
}
