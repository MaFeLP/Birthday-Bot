package com.github.mafelp.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Configuration {
    private static final Logger logger = LogManager.getLogger(Configuration.class);

    public static YamlConfiguration config = new YamlConfiguration();

    public static File configurationFile = new File("./config.yml");

    public static YamlConfiguration load() {
        logger.info("Loading configuration from config.yml...");
        config = YamlConfiguration.loadConfiguration(configurationFile);
        logger.debug("Setting defaults...");
        config.setDefaults(Defaults.createDefaultConfig());
        save();
        return config;
    }

    public static YamlConfiguration save() {
        logger.info("Saving the configuration to " + configurationFile.getAbsolutePath() + " ...");

        try {
            config.save(configurationFile);
            logger.info("Configuration file saved!");
        } catch (IOException ioException) {
            logger.error("Error saving the configuration to the file!", ioException);
        }

        return config;
    }
}
