package net.glintwein.util;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceLoader {
    private static final Gson gson = new Gson();

    public static InputStream getResourceAsStream(String path) {
        if (!path.startsWith("/"))
            path = "/" + path;
        InputStream is = ResourceLoader.class.getResourceAsStream(path);
        if (is == null)
            throw new RuntimeException("Resource not found: " + path);
        return is;
    }

    public static String getResourceAsString(String path) {
        try (InputStream is = getResourceAsStream(path)) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource as string: " + path, e);
        }
    }

    public static byte[] getResourceAsBytes(String path) {
        try (InputStream is = getResourceAsStream(path)) {
            return IOUtils.toByteArray(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource as bytes: " + path, e);
        }
    }

    public static <V> V getResourceAsJson(String path, Class<V> clazz) {
        String jsonString = getResourceAsString(path);
        try {
            return gson.fromJson(jsonString, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON resource: " + path, e);
        }
    }
}
