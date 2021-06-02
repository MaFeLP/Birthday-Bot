package com.github.mafelp.utils;

import com.github.mafelp.Manager.PresentManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShutdownProcess extends Thread{
    private static final Logger logger = LogManager.getLogger(ShutdownProcess.class);

    @Override
    public void run() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName("Shutdown-Hook #1");


        logger.info("Received Shutdown Signal!");
        logger.info("Saving all presents from all Servers.");
        PresentManager.savePresents();

        Configuration.save();

        logger.info("Exited gracefully");

        Thread.currentThread().setName(oldName);
        //System.exit(0);
    }
}