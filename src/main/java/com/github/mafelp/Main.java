package com.github.mafelp;

import com.github.mafelp.Listeners.MessageCreateListener;
import com.github.mafelp.Listeners.PrivateChannelListener;
import com.github.mafelp.Listeners.SkribblListener;
import com.github.mafelp.Manager.PresentManager;
import com.github.mafelp.utils.Configuration;
import com.github.mafelp.utils.ShutdownProcess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.Objects;
import java.util.concurrent.CompletionException;

/**
 * The main class that is being called on startup.
 */
public class Main {
    /**
     * The logger used to log statements to the console and the log file.
     */
    private static final Logger logger = LogManager.getLogger(Main.class);
    /**
     * The main discord API that connects to the discord servers and initialises callbacks.
     */
    public static DiscordApi discordApi;

    /**
     * The main Method called on startup.
     * @param args The additional arguments given to this program.
     */
    public static void main(String[] args) {
        logger.info("Starting Birthday-Bot version 1.4-beta");

        preStartup();
        logIn();
        postStartup();

        logger.debug("Adding shutdown hook.");
        Runtime.getRuntime().addShutdownHook(new ShutdownProcess(false));
    }

    /**
     * The method that handles all the tasks that should be executed <b>before</b> the bot is being logged in.
     */
    public static void preStartup() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName("PreStartup-Worker");

        logger.info("Loading global configuration...");
        Configuration.loadGlobalConfiguration();
        logger.debug("Current configuration is: \n" + Configuration.config.saveToString());
        logger.info("Global configuration loaded!");

        if (Configuration.config.get("apiToken") == null || Objects.equals(Configuration.config.get("apiToken"), "<Your Token goes here>")) {
            logger.fatal("No API Token configured!");
            logger.fatal("Please head to the config.yml file and set the value for \"apiToken:\" to your api token!");
            logger.info("The api token can be found here: https://discord.com/developers/applications/ !");
            logger.fatal("Exiting...");
            System.exit(1);
        }

        Thread.currentThread().setName(oldName);
    }

    /**
     * The method that handles logging in of the discord bot.
     */
    public static void logIn() {
        logger.info("Starting bot instance...");
        logger.info("Using api token: " + Configuration.config.getString("apiToken"));
        try {
            discordApi =  new DiscordApiBuilder()
                    .setToken(Configuration.config.getString("apiToken"))
                    .addListener(MessageCreateListener::new)
                    .addListener(PrivateChannelListener::new)
                    .addListener(SkribblListener::new)
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

    /**
     * The method that handles all the tasks that should be done <b>after</b> the bot has been logged in.
     */
    public static void postStartup() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName("PostStartup-Worker");

        logger.info("Loading Server configurations...");
        Configuration.loadAll();
        logger.info("Loaded all Server configurations!");

        logger.info("Loading all presents...");
        PresentManager.loadPresents();

        Thread.currentThread().setName(oldName);
    }
}
