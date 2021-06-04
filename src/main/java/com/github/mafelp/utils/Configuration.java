package com.github.mafelp.utils;

import com.github.mafelp.Main;
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
    // public static File globalConfigurationFile = new File("config.yml");
    // public static File configurationFilesFolder = new File("server-configurations");

    // Development usage
    public static File globalConfigurationFile = new File("data/config.yml");
    public static File configurationFilesFolder = new File("data/server-configurations");

    public static void loadGlobalConfiguration() {
        logger.info("Loading configuration from config.yml...");
        config = YamlConfiguration.loadConfiguration(globalConfigurationFile);
        logger.debug("Setting defaults...");
        config.setDefaults(Defaults.createDefaultConfig());
    }

    public static void load(Server server) {
        long serverID = server.getId();

        File serverConfigurationFileFolder = getServerConfigurationFolder(server);

        if (serverConfigurationFileFolder == null)
            if (serverConfigurations.containsKey(server))
                logger.debug("Server configs already contain config for server and config file does not exist... Leaving it unchanged.");
            else
                serverConfigurations.put(server, Defaults.createDefaultServerConfiguration());

        File serverConfigurationFile = new File(serverConfigurationFileFolder, "config.yml");

        if (serverConfigurationFile.exists()) {
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(serverConfigurationFile);
            if (serverConfigurations.containsKey(server))
                serverConfigurations.replace(server, configuration);
            else
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

                if (serverConfigurations.containsKey(server))
                    logger.debug("Server configs already contain config for server and config file does not exist... Leaving it unchanged.");
                else
                    serverConfigurations.put(server, configuration);
            } catch (IOException | InvalidConfigurationException e) {
                logger.error("Could not create config file for server with ID " + serverID, e);
                if (serverConfigurations.containsKey(server))
                    logger.debug("Server configs already contain config for server and config file does not exist... Leaving it unchanged.");
                else
                    serverConfigurations.put(server, Defaults.createDefaultServerConfiguration());
            }
        }
    }

    public static void loadAll() {
        Main.discordApi.getServers().forEach(Configuration::load);
    }

    public static void saveGlobalConfiguration() {
        logger.info("Saving the configuration to " + globalConfigurationFile.getAbsolutePath() + " ...");

        try {
            config.save(globalConfigurationFile);
            logger.info("Configuration file saved!");
        } catch (IOException ioException) {
            logger.error("Error saving the global configuration file!", ioException);
        }
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
        return serverConfigurations.get(server);
    }

    public static void saveAll() {
        serverConfigurations.forEach(Configuration::save);
    }

    public static void save(@NotNull Server server, @NotNull YamlConfiguration configurationToSave) {
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
    }
}