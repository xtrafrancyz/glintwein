package net.glintwein.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ResourceLoader {
    public static final Gson gson = new Gson();

    public static InputStream getStream(String path) {
        if (!path.startsWith("/"))
            path = "/" + path;
        InputStream is = ResourceLoader.class.getResourceAsStream(path);
        if (is == null)
            throw new RuntimeException("Resource not found: " + path);
        return is;
    }

    public static String getString(String path) {
        try (InputStream is = getStream(path)) {
            return toString(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource as string: " + path, e);
        }
    }

    public static byte[] getBytes(String path) {
        try (InputStream is = getStream(path)) {
            return toBytes(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource as bytes: " + path, e);
        }
    }

    public static <V> V getJson(String path, Class<V> clazz) {
        try (InputStream is = getStream(path)) {
            return gson.fromJson(new InputStreamReader(new BufferedInputStream(is)), clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON resource: " + path, e);
        }
    }

    public static JsonObject getJsonObject(String path) {
        try (InputStream is = getStream(path)) {
            return new JsonParser().parse(new InputStreamReader(new BufferedInputStream(is))).getAsJsonObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource as bytes: " + path, e);
        }
    }

    public static String toString(InputStream is) {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int length;

            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            return result.toString(StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read input stream as string", e);
        }
    }

    public static byte[] toBytes(InputStream is) {
        try {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int length;

            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            return result.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read input stream as bytes", e);
        }
    }
}
