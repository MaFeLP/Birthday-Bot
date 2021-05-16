package com.github.mafelp.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class PersonCommand extends Thread {
    private static long threadID = 0;
    private final MessageCreateEvent messageCreateEvent;
    private final List<Long> members;

    private static final Random random = new Random();
    private static final Logger logger = LogManager.getLogger(PersonCommand.class);

    public PersonCommand(MessageCreateEvent messageCreateEvent, List<Long> members) {
        this.messageCreateEvent = messageCreateEvent;
        this.members = members;

        this.setName("PersonCommand-"+threadID);
        ++threadID;
    }

    @Override
    public void run() {
        logger.debug("Executing command person...");
        int r = random.nextInt(members.size());

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

        logger.debug("Executed command person.");
    }
}
