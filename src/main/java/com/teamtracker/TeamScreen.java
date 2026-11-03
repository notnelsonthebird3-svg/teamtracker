package com.teamtracker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class TeamScreen extends Screen {
    private TextFieldWidget nameField;

    public TeamScreen() {
        super(Text.literal("Team Tracker"));
    }

    @Override
    protected void init() {
        int cx = width / 2;

        nameField = new TextFieldWidget(textRenderer, cx - 120, 40, 160, 20, Text.literal("Username"));
        nameField.setMaxLength(16);
        addDrawableChild(nameField);
        setInitialFocus(nameField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Add to Team"), b -> addPlayer())
                .dimensions(cx + 45, 40, 75, 20).build());

        addDrawableChild(ButtonWidget.builder(modeText(), b -> {
            TeamManager.data.mode = TeamManager.data.mode == TeamManager.Mode.COORDS
                    ? TeamManager.Mode.DIRECTION : TeamManager.Mode.COORDS;
            TeamManager.save();
            b.setMessage(modeText());
        }).dimensions(cx - 120, 66, 118, 20).build());

        addDrawableChild(ButtonWidget.builder(hudText(), b -> {
            TeamManager.data.hudEnabled = !TeamManager.data.hudEnabled;
            TeamManager.save();
            b.setMessage(hudText());
        }).dimensions(cx + 2, 66, 118, 20).build());

        int y = 110;
        for (String name : TeamManager.data.members) {
            final String n = name;
            addDrawableChild(ButtonWidget.builder(Text.literal("Remove"), b -> {
                TeamManager.remove(n);
                clearAndInit();
            }).dimensions(cx + 70, y - 5, 50, 20).build());
            y += 24;
        }
    }

    private Text modeText() {
        return Text.literal("Show: " + (TeamManager.data.mode == TeamManager.Mode.COORDS ? "Coordinates" : "Direction"));
    }

    private Text hudText() {
        return Text.literal("Arrows: " + (TeamManager.data.hudEnabled ? "ON" : "OFF"));
    }

    private void addPlayer() {
        if (TeamManager.add(nameField.getText())) {
            clearAndInit();
        } else {
            nameField.setText("");
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && nameField.isFocused()) {
            addPlayer();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        int cx = width / 2;
        ctx.drawCenteredTextWithShadow(textRenderer, "Team Tracker", cx, 14, 0xFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Player username:", cx - 120, 29, 0xAAAAAA);
        super.render(ctx, mouseX, mouseY, delta);

        MinecraftClient mc = MinecraftClient.getInstance();
        int y = 110;
        if (TeamManager.data.members.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer, "No team members yet. Add a username above.", cx, y, 0x888888);
        }
        for (String name : TeamManager.data.members) {
            ctx.drawTextWithShadow(textRenderer, name, cx - 120, y - 5, 0xFFFFFF);
            ctx.drawTextWithShadow(textRenderer, info(mc, name), cx - 120, y + 5, 0xAAAAAA);
            y += 24;
        }
    }

    private String info(MinecraftClient mc, String name) {
        PlayerEntity t = TeamTrackerClient.findPlayer(name);
        if (mc.player == null || t == null) return "Out of range / not loaded";
        if (t == mc.player) return "That's you!";
        int dist = (int) mc.player.distanceTo(t);
        if (TeamManager.data.mode == TeamManager.Mode.COORDS) {
            return "X: " + (int) t.getX() + "  Y: " + (int) t.getY() + "  Z: " + (int) t.getZ() + "  (" + dist + "m)";
        }
        double bearing = TeamTrackerClient.bearingTo(mc.player, t);
        return TeamTrackerClient.compass(bearing) + "  (" + (int) bearing + "°)  " + dist + "m away";
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
