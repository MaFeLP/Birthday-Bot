package com.github.mafelp.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class GameCommand extends Thread {
    private final MessageCreateEvent messageCreateEvent;
    private final List<String> games;

    private static final Random random = new Random();
    private static final Logger logger = LogManager.getLogger(GameCommand.class);

    public GameCommand(MessageCreateEvent messageCreateEvent, List<String> games) {
        this.messageCreateEvent = messageCreateEvent;
        this.games = games;
    }

    @Override
    public void run() {
        logger.debug("Executing command game...");
        int r = random.nextInt(games.size());

        logger.debug("Sending reply...");
        messageCreateEvent.getChannel().sendMessage(
                new EmbedBuilder()
                        .setAuthor(messageCreateEvent.getMessageAuthor())
                        .addField("Game Chosen", games.get(r))
                        .setColor(Color.GREEN)
        );

        logger.debug("Reply sent.");
        logger.debug("Executed command game.");
    }
}
