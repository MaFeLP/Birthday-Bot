package com.github.mafelp.utils;

import com.github.mafelp.Main;
import com.github.mafelp.Manager.PresentManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The class that handles the shutdown of the bot and saves its current state.
 */
public class ShutdownProcess extends Thread{
    /**
     * The logging instance to log statements to the console and the log file.
     */
    private static final Logger logger = LogManager.getLogger(ShutdownProcess.class);

    /**
     * If the normal logging instance should be used to print statements to STDOUT.
     * This is, because the logger will be shout down before this method could finish,
     * so to indicate to the user that the program is doing something, normal print statements will be used.
     */
    private final boolean useLogger;

    /**
     * The default constructor which sets the use logger to no.
     */
    public ShutdownProcess() {
        this.useLogger = false;
    }

    /**
     * The constructor to specify manually, if a logger should be used (preferred).
     * @param useLogger If the logger or print statements should be used.
     */
    public ShutdownProcess(boolean useLogger) {
        this.useLogger = useLogger;
    }

    /**
     * The method that handles the actual execution of the shutdown routine.
     */
    @Override
    public void run() {
        // Sets the thread to something meaningful to be displayed in the logger.
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName("Shutdown-Hook #1");

        logger.warn("Received Shutdown Signal!");

        // Shutdown routine with(out) the logger.
        if (useLogger) {
            logger.info("Disconnecting discord API...");
            Main.discordApi.disconnect();
            logger.info("Disconnected discord API!");

            logger.info("Saving all presents from all Servers.");
            PresentManager.savePresents();
            logger.info("Presents saved!");

            logger.info("Saving global configuration...");
            Configuration.saveGlobalConfiguration();
            logger.info("Global configuration saved!");

            logger.info("Saving all server configurations...");
            Configuration.saveAll();
            logger.info("Saved all server configurations!");

            logger.info("Shutdown-Routine done!");
        } else {
            logger.warn("Shutting down logger, but disconnecting the API, Saving all presents and configurations.");
            logger.warn("The following messages will only be sent to the Console and not the Log File!");

            System.out.println("[Shutdown/INFO ]: Disconnecting discord API...");
            Main.discordApi.disconnect();
            System.out.println("[Shutdown/INFO ]: Disconnected discord API!");

            System.out.println("[Shutdown/INFO ]: Saving all presents from all Servers.");
            PresentManager.savePresents();
            System.out.println("[Shutdown/INFO ]: Presents saved!");

            System.out.println("[Shutdown/INFO ]: Saving global configuration...");
            Configuration.saveGlobalConfiguration();
            System.out.println("[Shutdown/INFO ]: Global configuration saved!");

            System.out.println("[Shutdown/INFO ]: Saving all server configurations...");
            Configuration.saveAll();
            System.out.println("[Shutdown/INFO ]: Saved all server configurations!");

            System.out.println("[Shutdown/INFO ]: Exited gracefully!");
        }

        // Reset the current thread name.
        Thread.currentThread().setName(oldName);
    }
}