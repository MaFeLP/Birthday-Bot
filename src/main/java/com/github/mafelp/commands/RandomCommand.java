package com.github.mafelp.commands;

import com.github.mafelp.utils.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.Random;

public class RandomCommand extends Thread {
    private static long threadID = 0;
    private final MessageCreateEvent messageCreateEvent;
    private final String prefix;
    private final Command command;

    private static final Random random = new Random();
    private static final Logger logger = LogManager.getLogger(RandomCommand.class);

    public RandomCommand(MessageCreateEvent messageCreateEvent, Command command, String prefix) {
        this.messageCreateEvent = messageCreateEvent;
        this.prefix = prefix;
        this.command = command;

        this.setName("RandomCommand-" + threadID);
        ++threadID;
    }

    @Override
    public void run() {
        logger.debug("Executing command random...");
        if (command.getArguments() == null) {
            logger.warn("Command random has not enough arguments. Ignoring");
            logger.debug("Sending help embed...");
            new MessageBuilder().setEmbed(
                    new EmbedBuilder()
                            .setTitle("Error!")
                            .addField("Not enough arguments!", "Usage: " + prefix + "random <value1> <value2> [<value 3> ...]")
                            .setColor(Color.RED)
            ).send(messageCreateEvent.getChannel());
            logger.debug("Help embed sent.");
        }

        int r = random.nextInt(command.getArguments().length);

        logger.debug("Choosing random argument...");
        if (command.getStringArgument(r).isPresent()) {
            logger.debug("Random argument chosen: " + command.getStringArgument(r).get());
            logger.debug("Sending reply!");
            new MessageBuilder().setEmbed(
                    new EmbedBuilder()
                            .setTitle("Random")
                            .addField("Random value got chosen!", "Result: " + command.getStringArgument(r).get())
                            .setColor(Color.YELLOW)
                            .setAuthor(messageCreateEvent.getMessageAuthor())
            ).send(messageCreateEvent.getChannel());

            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"random\"; Response: Random output chosen: \"" + command.getStringArgument(r).get() + "\"");
        } else {
            new MessageBuilder().setEmbed(
                    new EmbedBuilder()
                            .setTitle("Error!")
                            .setAuthor(messageCreateEvent.getMessageAuthor())
                            .setColor(Color.RED)
                            .addField("Unknown Error", "An unknown error occurred. Please try again!")
            );

            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"random\"; Response: Error getting the string argument.");
        }
    }
}
