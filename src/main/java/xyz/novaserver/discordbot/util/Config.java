package xyz.novaserver.discordbot.util;

import com.google.common.io.ByteStreams;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.*;

public class Config {
    private final Class<?> mainClass;
    private final File configFile;
    private final String defaultFile;
    private ConfigurationNode rootNode;

    public Config(Class<?> mainClass, File configFile, String defaultFile) {
        this.mainClass = mainClass;
        this.configFile = configFile;
        this.defaultFile = defaultFile;
    }

    public static Config getConfig(Class<?> mainClass, File configFile, String defaultFile) {
        Config config = new Config(mainClass, configFile, defaultFile);
        if (config.loadConfig()) {
            return config;
        }
        return null;
    }

    public boolean loadConfig() {
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = mainClass.getResourceAsStream("/" + defaultFile);
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(configFile.toPath()).build();
        try {
            rootNode = loader.load();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public ConfigurationNode getRoot() {
        return rootNode;
    }
}

