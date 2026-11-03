package com.teamtracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TeamManager {
    public enum Mode { COORDS, DIRECTION }

    public static class Data {
        public List<String> members = new ArrayList<>();
        public Mode mode = Mode.COORDS;
        public boolean hudEnabled = true;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("teamtracker.json");
    public static Data data = new Data();

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                try (Reader r = Files.newBufferedReader(FILE)) {
                    Data d = GSON.fromJson(r, Data.class);
                    if (d != null) data = d;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (data.members == null) data.members = new ArrayList<>();
        if (data.mode == null) data.mode = Mode.COORDS;
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(data, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean add(String name) {
        name = name.trim();
        if (name.isEmpty()) return false;
        for (String m : data.members) if (m.equalsIgnoreCase(name)) return false;
        data.members.add(name);
        save();
        return true;
    }

    public static void remove(String name) {
        data.members.removeIf(m -> m.equalsIgnoreCase(name));
        save();
    }
}
