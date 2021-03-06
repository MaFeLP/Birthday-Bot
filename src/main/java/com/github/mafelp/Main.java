package com.github.mafelp;

import com.github.mafelp.Listeners.MessageCreateListener;
import com.github.mafelp.utils.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.Objects;
import java.util.concurrent.CompletionException;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    protected static DiscordApi discordApi;

    public static void main(String[] args) {
        logger.info("Starting Birthday-Bot version 0.2-beta");

        logger.info("loading configuration...");

        Configuration.load();

        if (!Configuration.configurationFile.exists())
            Configuration.save();

        logger.debug("Current configuration is: \n" + Configuration.config.saveToString());

        if (Configuration.config.get("apiToken") == null || Objects.equals(Configuration.config.get("apiToken"), "<Your Token goes here>")) {
            logger.fatal("No API Token configured!");
            logger.fatal("Please head to the config.yml file and set the value for \"apiToken:\" to your api token!");
            logger.info("The api token can be found here: https://discord.com/developers/applications/ !");
            logger.fatal("Exiting...");
            System.exit(1);

        }

        logger.info("Starting bot instance...");
        logger.info("Using api token: " + Configuration.config.getString("apiToken"));
        try {
            discordApi =  new DiscordApiBuilder()
                    .setToken(Configuration.config.getString("apiToken"))
                    .addListener(MessageCreateListener::new)
                    .login().join();
            logger.info("Bot instance started! You can now execute commands.");
            logger.info("Discord bot invite token is: " + discordApi.createBotInvite());
        } catch (CompletionException | IllegalStateException exception) {
            logger.fatal("Invalid bot Token: " + Configuration.config.getString("apiToken"));
            logger.fatal("Please head to the config.yml file and set the value for \"apiToken:\" to your api token!");
            logger.info("The api token can be found here: https://discord.com/developers/applications/ !");
            logger.debug("Stack trace:", exception);
            logger.fatal("Exiting...");
            System.exit(1);
        }
    }
}
