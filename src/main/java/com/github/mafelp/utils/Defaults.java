package com.github.mafelp.utils;

import org.bukkit.configuration.file.YamlConfiguration;

public class Defaults {
    private static final long[] listeningChannels = new long[] {
            824634884802609182L, // bd Chat
            828332839991771176L  // Test Server Chat
    };

    private static final long[] members = new long[] {
            429194033819156481L, // Christian
            459019618686730271L, // Max
            689887441070719059L, // Bennet
            699597263059812433L, // Collin
            460071330025439233L, // Jannis
            630475083001102336L, // Lily
            381356301231587329L, // Marvin
            507957898866065408L, // Mats
            473870434690138112L  // Michel
    };

    private static final String[] games = {
            "Among Us",
            "Gartic Phone",
            "Skirbble.io",
            "Minecraft: BedWars",
            "Minecraft: Lucky Block Bedwars",
            "Minecraft: Murder Mystery"
    };

    private static final String[] happyBirthdaySongs = {
            "https://www.youtube.com/watch?v=nl62hhiBMOM",
            "https://www.youtube.com/watch?v=RcVZfJO01NI",
            "https://www.youtube.com/watch?v=qCJSNMqub8g"
    };

    private static final long[] authorizedAccountIDs = new long[] {
            459019618686730271L, // MaFeLP
            507957898866065408L, // Mats
            473870434690138112L, // Michel
    };


    public static YamlConfiguration createDefaultConfig () {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.set("games", games);
        yamlConfiguration.set("happyBirthdaySongs", happyBirthdaySongs);
        yamlConfiguration.set("members", members);
        yamlConfiguration.set("listeningChannels", listeningChannels);
        yamlConfiguration.set("prefix", "!");
        yamlConfiguration.set("authorizedAccountIDs", authorizedAccountIDs);
        yamlConfiguration.set("apiToken", "<Your Token goes here>");

        return yamlConfiguration;
    }
}