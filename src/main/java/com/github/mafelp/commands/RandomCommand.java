package com.github.mafelp.commands;

import com.github.mafelp.utils.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.Random;

/**
 * The class the handles execution of the random command.
 */
public class RandomCommand extends Thread {
    /**
     * The number of threads of this kind that were being created.
     */
    private static long threadID = 0;

    /**
     * The Event that is being passed to this class by the discord API.
     */
    private final MessageCreateEvent messageCreateEvent;

    /**
     * The command which was being parsed with the {@link com.github.mafelp.utils.CommandParser} command parser.
     */
    private final Command command;

    /**
     * The instance to select a random game.
     */
    private static final Random random = new Random();

    /**
     * The logger which is used to log statements to the console.
     */
    private static final Logger logger = LogManager.getLogger(GameCommand.class);

    /**
     * The constructor that initialises the command execution.
     * @param messageCreateEvent The event passed in by the bot, which contains useful information about the message.
     * @param command The command that was parsed.
     */
    public RandomCommand(MessageCreateEvent messageCreateEvent, Command command) {
        this.messageCreateEvent = messageCreateEvent;
        this.command = command;

        this.setName("RandomCommand-" + threadID);
        ++threadID;
    }

    /**
     * The method handles the actual execution of this command.
     */
    @Override
    public void run() {
        logger.debug("Executing command random...");
        if (command.getArguments() == null) {
            logger.warn("Command random has not enough arguments. Ignoring");
            logger.debug("Sending help embed...");
            new MessageBuilder().setEmbed(
                    new EmbedBuilder()
                            .setTitle("Error!")
                            .addField("Not enough arguments!", "Usage: " + command.getCommand() + " <value1> <value2> [<value 3> ...]")
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
