package com.interviewai.util;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static Properties props = new Properties();
    static {
        try (InputStream in = ConfigLoader.class.getResourceAsStream("/config/config.properties")) {
            if (in != null) props.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String get(String key) { return props.getProperty(key); }
}
