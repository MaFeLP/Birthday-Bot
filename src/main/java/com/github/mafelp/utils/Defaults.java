package com.github.mafelp.utils;

import org.bukkit.configuration.file.YamlConfiguration;

public class Defaults {
    private static final long[] listeningChannels = new long[] {
            1234L
    };

    private static final long[] members = new long[] {
            1234L
    };

    private static final String[] games = {
            "Among Us",
            "Gartic Phone",
            "Skirbble.io",
            "Minecraft: BedWars",
            "Minecraft: Lucky Block Bedwars",
            "Minecraft: Murder Mystery",
            "Codenames"
    };

    private static final String[] happyBirthdaySongs = {
            "https://www.youtube.com/watch?v=nl62hhiBMOM",
            "https://www.youtube.com/watch?v=RcVZfJO01NI",
            "https://www.youtube.com/watch?v=qCJSNMqub8g"
    };

    private static final long[] authorizedAccountIDs = new long[] {
            1234L
    };

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