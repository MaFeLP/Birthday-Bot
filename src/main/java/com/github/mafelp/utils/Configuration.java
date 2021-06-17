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

/**
 * The class that handles all the configuration tasks for servers and on a global level.
 */
public class Configuration {
    /**
     * The logging instance to log statements to the console and the log file.
     */
    private static final Logger logger = LogManager.getLogger(Configuration.class);

    /**
     * The global configuration.
     */
    public static YamlConfiguration config = new YamlConfiguration();
    /**
     * A map of server configurations associated with their servers.
     */
    private static final Map<Server, YamlConfiguration> serverConfigurations = new HashMap<>();

    /**
     * The configuration file which houses the global {@link Configuration#config}.
     */
    // public static File globalConfigurationFile = new File("config.yml");                         // Normal use
    public static File globalConfigurationFile = new File("data/config.yml");              // Development use
    /**
     * The folder which houses all the sub folder with the server specific configurations, skribbl words, etc.
     */
    // public static File configurationFilesFolder = new File("server-configurations");             // Normal use
    public static File configurationFilesFolder = new File("data/server-configurations");  // Development use

    /**
     * Method to load the global configuration into memory.
     */
    public static void loadGlobalConfiguration() {
        logger.info("Loading configuration from config.yml...");
        config = YamlConfiguration.loadConfiguration(globalConfigurationFile);
        logger.debug("Setting defaults...");
        config.setDefaults(Defaults.createDefaultConfig());
    }

    /**
     * Method to load a server configuration and put it into the map of server configurations.
     * @param server The server to load the configuration from.
     * @return The configuration that has been loaded.
     */
    public static YamlConfiguration load(Server server) {
        long serverID = server.getId();

        File serverConfigurationFileFolder = getServerConfigurationFolder(server);

        if (serverConfigurationFileFolder == null)
            if (serverConfigurations.containsKey(server))
                logger.debug("Server configs already contain config for server and config file does not exist... Leaving it unchanged.");
            else
                serverConfigurations.put(server, Defaults.createDefaultServerConfiguration());

        File serverConfigurationFile = new File(serverConfigurationFileFolder, "config.yml");

        // If the configuration file exists, load from it.
        if (serverConfigurationFile.exists()) {
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(serverConfigurationFile);
            if (serverConfigurations.containsKey(server))
                serverConfigurations.replace(server, configuration);
            else
                serverConfigurations.put(server, configuration);
        // If the configuration file does not exist, create one and fill it with the default configuration.
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

                return serverConfigurations.get(server);
            } catch (IOException | InvalidConfigurationException e) {
                logger.error("Could not create config file for server with ID " + serverID, e);
                if (serverConfigurations.containsKey(server))
                    logger.debug("Server configs already contain config for server and config file does not exist... Leaving it unchanged.");
                else
                    serverConfigurations.put(server, Defaults.createDefaultServerConfiguration());
            }
        }
        return null;
    }

    /**
     * Method that loads all the server configurations into memory.
     */
    public static void loadAll() {
        Main.discordApi.getServers().forEach(Configuration::load);
    }

    /**
     * Method that saves the current global configuration to the disk.
     */
    public static void saveGlobalConfiguration() {
        logger.info("Saving the configuration to " + globalConfigurationFile.getAbsolutePath() + " ...");

        try {
            config.save(globalConfigurationFile);
            logger.info("Configuration file saved!");
        } catch (IOException ioException) {
            logger.error("Error saving the global configuration file!", ioException);
        }
    }

    /**
     * Method that gets the configuration folder of given server.
     * This folder houses the configuration files, presents, skribbl words, etc.
     * @param server The server to get the folder of.
     * @return The file which is the server.
     */
    public static File getServerConfigurationFolder(@NotNull Server server) {
        File serverConfigurationFileFolder = new File(configurationFilesFolder, server.getId() + "");

        // If the folder does not exist, create it.
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

    /**
     * The method that return the server configuration which is in the map.
     * @param server The server to get the configuration of.
     * @return The server configuration of the server.
     */
    public static YamlConfiguration getServerConfiguration(@NotNull Server server) {
        if (serverConfigurations.get(server) != null)
            return serverConfigurations.get(server);

        return load(server);
    }

    /**
     * The method to save all the server configurations to the disk.
     */
    public static void saveAll() {
        serverConfigurations.forEach(Configuration::save);
    }

    /**
     * The method that saves a server configuration.
     * @param server The server to save the configuration of.
     * @param configurationToSave The configuration to save.
     */
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