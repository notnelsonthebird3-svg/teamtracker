package com.teamtracker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

public class TeamTrackerClient implements ClientModInitializer {
    public static KeyBinding openMenu;

    @Override
    public void onInitializeClient() {
        TeamManager.load();

        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.teamtracker.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.categories.teamtracker"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new TeamScreen());
            }
        });

        HudRenderCallback.EVENT.register(ArrowHud::render);
    }

    /** Finds a loaded player by username (case-insensitive), or null if not in render range. */
    public static PlayerEntity findPlayer(String name) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return null;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p.getName().getString().equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    /** Absolute compass bearing (0 = N, 90 = E) from the local player to the target. */
    public static double bearingTo(PlayerEntity from, PlayerEntity to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double mcYaw = Math.toDegrees(Math.atan2(-dx, dz));
        double b = (mcYaw + 180.0) % 360.0;
        return b < 0 ? b + 360.0 : b;
    }

    public static String compass(double bearing) {
        String[] dirs = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        return dirs[(int) Math.round(bearing / 45.0) % 8];
    }
}
