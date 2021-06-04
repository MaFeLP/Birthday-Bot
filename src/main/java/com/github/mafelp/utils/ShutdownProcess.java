package com.github.mafelp.utils;

import com.github.mafelp.Main;
import com.github.mafelp.Manager.PresentManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShutdownProcess extends Thread{
    private static final Logger logger = LogManager.getLogger(ShutdownProcess.class);

    private final boolean useLogger;

    public ShutdownProcess(boolean useLogger) {
        this.useLogger = useLogger;
    }

    @Override
    public void run() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName("Shutdown-Hook #1");

        logger.warn("Received Shutdown Signal!");

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

        Thread.currentThread().setName(oldName);
    }
}