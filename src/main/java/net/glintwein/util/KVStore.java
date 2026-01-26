package net.glintwein.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class KVStore {
    private static final String FILE_PATH = "glintwein_kvstore.json";
    private static JsonObject data = new JsonObject();
    private static boolean dirty = false;

    public static void load() {
        try (InputStream is = Files.newInputStream(Paths.get(FILE_PATH))) {
            data = new JsonParser().parse(new InputStreamReader(new BufferedInputStream(is))).getAsJsonObject();
        } catch (Exception e) {
            // File might not exist yet, that's fine
        }
    }

    public static void save() {
        if (!dirty)
            return;
        dirty = false;
        try {
            Files.write(Paths.get(FILE_PATH), data.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save KVStore", e);
        }
    }

    public static void put(String key, int value) {
        data.addProperty(key, value);
        dirty = true;
    }

    public static void put(String key, double value) {
        data.addProperty(key, value);
        dirty = true;
    }

    public static void put(String key, boolean value) {
        data.addProperty(key, value);
        dirty = true;
    }

    public static void put(String key, String value) {
        data.addProperty(key, value);
        dirty = true;
    }

    public static int getInt(String key, int defaultValue) {
        JsonElement el = data.get(key);
        return el != null ? el.getAsInt() : defaultValue;
    }

    public static float getFloat(String key, float defaultValue) {
        JsonElement el = data.get(key);
        return el != null ? el.getAsFloat() : defaultValue;
    }

    public static double getDouble(String key, double defaultValue) {
        JsonElement el = data.get(key);
        return el != null ? el.getAsDouble() : defaultValue;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        JsonElement el = data.get(key);
        return el != null ? el.getAsBoolean() : defaultValue;
    }

    public static String getString(String key, String defaultValue) {
        JsonElement el = data.get(key);
        return el != null ? el.getAsString() : defaultValue;
    }
}
