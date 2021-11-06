package com.github.jenya705.iplog;

import lombok.Cleanup;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * @author Jenya705
 */
public class IpLogConfig {

    private final Map<String, Object> config;

    public IpLogConfig(File directory) throws IOException {
        directory.mkdirs();
        File configFile = new File(directory, "config.yml");
        if (!configFile.exists()) {
            @Cleanup InputStream inputStream =
                    getClass().getClassLoader().getResourceAsStream("config.yml");
            Files.write(configFile.toPath(), inputStream.readAllBytes(), StandardOpenOption.CREATE);
        }
        @Cleanup Reader reader = new FileReader(configFile);
        config = new Yaml().load(reader);

    }

    @SuppressWarnings("unchecked")
    public Object get(String key) {
        String[] keyPath = key.split("/.");
        Map<String, Object> node = config;
        for (int i = 0; i < keyPath.length; ++i) {
            String path = keyPath[i];
            if (!node.containsKey(path)) return null;
            Object obj = node.get(path);
            if (!(obj instanceof Map)) {
                if (i + 1 == keyPath.length) return obj;
                return null;
            }
            node = (Map<String, Object>) obj;
        }
        return node;
    }

    @SuppressWarnings("unchecked")
    public <T> T getT(String key) {
        Object obj = get(key);
        if (obj == null) return null;
        return (T) obj;
    }

}
