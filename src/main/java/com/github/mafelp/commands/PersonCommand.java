package com.github.mafelp.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.List;
import java.util.Random;

/**
 * The class that handles the asynchronous execution of the person command.
 */
public class PersonCommand extends Thread {
    /**
     * The number of threads of this kind that were being created.
     */
    private static long threadID = 0;

    /**
     * The Event that is being passed to this class by the discord API.
     */
    private final MessageCreateEvent messageCreateEvent;

    /**
     * The list of member ids, specified in the configuration of the server, on which the message was sent on.
     */
    private final List<Long> members;

    /**
     * The instance to select a random game.
     */
    private static final Random random = new Random();

    /**
     * The logger which is used to log statements to the console.
     */
    private static final Logger logger = LogManager.getLogger(PersonCommand.class);

    /**
     * The "Default" Constructor
     * @param messageCreateEvent The Event that is being passed to this class by the discord API.
     * @param members The list of members specified in the configuration of the server, on which the message was sent.
     */
    public PersonCommand(MessageCreateEvent messageCreateEvent, List<Long> members) {
        this.messageCreateEvent = messageCreateEvent;
        this.members = members;

        this.setName("PersonCommand-"+threadID);
        ++threadID;
    }

    /**
     * The method handles the actual execution of this command.
     */
    @Override
    public void run() {
        logger.debug("Executing command person...");
        int r = random.nextInt(members.size());

        long person = members.get(r);

        logger.debug("Sending reply...");
        messageCreateEvent.getChannel().sendMessage(
                new EmbedBuilder()
                        .setAuthor(messageCreateEvent.getMessageAuthor())
                        .addField("Random Person","You got chosen <@" +
                                members.get(r) + ">!"
                        )
                        .setColor(Color.GREEN)
        );
        logger.debug("Reply sent.");

        logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"person\"; Response: Person chosen with ID: \"" + person + "\"");
    }
}
