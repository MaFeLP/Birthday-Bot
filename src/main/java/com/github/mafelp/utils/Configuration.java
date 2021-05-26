package com.github.mafelp.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.javacord.api.entity.server.Server;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    private static final Logger logger = LogManager.getLogger(Configuration.class);

    public static YamlConfiguration config = new YamlConfiguration();
    private static final Map<Server, YamlConfiguration> serverConfigurations = new HashMap<>();

    // Normal use
    public static File globalConfigurationFile = new File("config.yml");
    public static File configurationFilesFolder = new File("server-configurations");

    // Development usage
    // public static File globalConfigurationFile = new File("data/config.yml");
    // public static File configurationFilesFolder = new File("data/server-configurations");

    public static YamlConfiguration load() {
        logger.info("Loading configuration from config.yml...");
        config = YamlConfiguration.loadConfiguration(globalConfigurationFile);
        logger.debug("Setting defaults...");
        config.setDefaults(Defaults.createDefaultConfig());
        save();
        return config;
    }

    public static YamlConfiguration save() {
        logger.info("Saving the configuration to " + globalConfigurationFile.getAbsolutePath() + " ...");

        try {
            config.save(globalConfigurationFile);
            logger.info("Configuration file saved!");
        } catch (IOException ioException) {
            logger.error("Error saving the configuration to the file!", ioException);
        }

        return config;
    }

    public static File getServerConfigurationFolder(@NotNull Server server) {
        File serverConfigurationFileFolder = new File(configurationFilesFolder, server.getId() + "");

        if (!serverConfigurationFileFolder.exists()) {
            if (serverConfigurationFileFolder.mkdirs()){
                logger.debug("Created new Folder: " + serverConfigurationFileFolder.getAbsolutePath());
            } else {
                logger.error("Could not create folder: " + serverConfigurationFileFolder.getAbsolutePath());
                return null;
            }
        }

        return serverConfigurationFileFolder;
    }

    public static YamlConfiguration getServerConfiguration(@NotNull Server server) {
        long serverID = server.getId();

        File serverConfigurationFileFolder = getServerConfigurationFolder(server);

        if (serverConfigurationFileFolder == null)
            return Defaults.createDefaultServerConfiguration();

        File serverConfigurationFile = new File(serverConfigurationFileFolder, "config.yml");

        if (serverConfigurationFile.exists()) {
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(serverConfigurationFile);
            serverConfigurations.put(server, configuration);

        } else {
            try {
                serverConfigurationFile.createNewFile();
                logger.debug("Created config file " + serverConfigurationFile.getAbsolutePath());
                Defaults.createDefaultServerConfiguration().save(serverConfigurationFile);

                // Creates a new Instance of the configuration.
                String configurationString = Defaults.createDefaultServerConfiguration().saveToString();
                YamlConfiguration configuration = new YamlConfiguration();
                configuration.loadFromString(configurationString);

                serverConfigurations.put(server, configuration);
            } catch (IOException | InvalidConfigurationException e) {
                logger.error("Could not create config file for server with ID " + serverID, e);
                return Defaults.createDefaultServerConfiguration();
            }
        }

        return serverConfigurations.get(server);
    }

    public static YamlConfiguration save(@NotNull Server server, @NotNull YamlConfiguration configurationToSave) {
        long serverID = server.getId();

        File serverConfigurationFileFolder = new File(configurationFilesFolder, serverID + "");
        File serverConfigurationFile = new File(serverConfigurationFileFolder, "config.yml");

        try {
            logger.debug("Saving configuration file " + serverConfigurationFile.getAbsolutePath());
            configurationToSave.save(serverConfigurationFile);
            logger.debug("Saved configuration file " + serverConfigurationFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Could not save configuration for server " + server.getName(), e);
        }

        return serverConfigurations.get(server);
    }
}
