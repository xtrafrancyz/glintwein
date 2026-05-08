package net.glintwein.util;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ResourceLoaderUtil {
    public static final Gson gson = new Gson();

    public static InputStream getStream(String path) {
        if (!path.startsWith("/"))
            path = "/" + path;
        InputStream is = ResourceLoaderUtil.class.getResourceAsStream(path);
        if (is == null)
            throw new RuntimeException("Resource not found: " + path);
        return is;
    }

    public static <V> V getJson(InputStream is, Class<V> clazz) {
        return gson.fromJson(new InputStreamReader(new BufferedInputStream(is)), clazz);
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
