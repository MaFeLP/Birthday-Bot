package com.github.mafelp.utils;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * The class the holds default values for all the values that are configured in the configuration.
 */
public class Defaults {
    /**
     * The default listening channels.
     */
    private static final long[] listeningChannels = new long[] {
            1234L
    };

    /**
     * The default members of the birthday.
     */
    private static final long[] members = new long[] {
            1234L
    };

    /**
     * The default games that could be played.
     */
    private static final String[] games = {
            "Among Us",
            "Gartic Phone",
            "Skribble.io",
            "Minecraft: BedWars",
            "Minecraft: Lucky Block Bedwars",
            "Minecraft: Murder Mystery",
            "Codenames"
    };

    /**
     * Links to happy birthday songs that could be played.
     */
    private static final String[] happyBirthdaySongs = {
            "https://www.youtube.com/watch?v=nl62hhiBMOM",
            "https://www.youtube.com/watch?v=RcVZfJO01NI",
            "https://www.youtube.com/watch?v=qCJSNMqub8g"
    };

    /**
     * Account IDs that are allowed to perform administrative tasks with the bot.
     */
    private static final long[] authorizedAccountIDs = new long[] {
            1234L
    };

    /**
     * The function that creates a default global configuration.
     * @return The default global configuration.
     */
    public static YamlConfiguration createDefaultConfig () {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.set("games", games);
        yamlConfiguration.set("happyBirthdaySongs", happyBirthdaySongs);
        yamlConfiguration.set("members", members);
        yamlConfiguration.set("listeningChannels", listeningChannels);
        yamlConfiguration.set("prefix", "!");
        yamlConfiguration.set("allowPrivateMessages",true);
        yamlConfiguration.set("authorizedAccountIDs", authorizedAccountIDs);
        yamlConfiguration.set("apiToken", "<Your Token goes here>");
        yamlConfiguration.set("skribbl.addReaction", true);

        return yamlConfiguration;
    }

    /**
     * The function that creates a default server configuration.
     * @return The default server configuration.
     */
    public static YamlConfiguration createDefaultServerConfiguration() {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.set("games", games);
        yamlConfiguration.set("happyBirthdaySongs", happyBirthdaySongs);
        yamlConfiguration.set("members", members);
        yamlConfiguration.set("listeningChannels", listeningChannels);
        yamlConfiguration.set("prefix", "!");
        yamlConfiguration.set("authorizedAccountIDs", authorizedAccountIDs);
        yamlConfiguration.set("skribbl.addReaction", true);
        yamlConfiguration.set("skribbl.sendWordsOnEnd", true);

        return yamlConfiguration;
    }
}