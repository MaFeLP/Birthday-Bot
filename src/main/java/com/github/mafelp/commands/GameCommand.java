package com.github.mafelp.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.List;
import java.util.Random;

/**
 * The class that handles the asynchronous execution of the game command.
 */
public class GameCommand extends Thread {
    /**
     * The number of threads of this kind that were being created.
     */
    private static long threadID = 0;

    /**
     * The Event that is being passed to this class by the discord API.
     */
    private final MessageCreateEvent messageCreateEvent;

    /**
     * The list of games, specified in the configuration of the server, on which the message was sent on.
     */
    private final List<String> games;

    /**
     * The instance to select a random game.
     */
    private static final Random random = new Random();

    /**
     * The logger which is used to log statements to the console.
     */
    private static final Logger logger = LogManager.getLogger(GameCommand.class);

    /**
     * The "Default" Constructor
     * @param messageCreateEvent The Event that is being passed to this class by the discord API.
     * @param games The list of games specified in the configuration of the server, on which the message was sent.
     */
    public GameCommand(MessageCreateEvent messageCreateEvent, List<String> games) {
        this.messageCreateEvent = messageCreateEvent;
        this.games = games;

        this.setName("GameCommand-" + threadID);
        ++threadID;
    }

    /**
     * The method handles the actual execution of this command.
     */
    @Override
    public void run() {
        logger.debug("Executing command game...");
        int r = random.nextInt(games.size());

        String game = games.get(r);

        logger.debug("Sending reply...");
        messageCreateEvent.getChannel().sendMessage(
                new EmbedBuilder()
                        .setAuthor(messageCreateEvent.getMessageAuthor())
                        .addField("Game Chosen", game)
                        .setColor(Color.GREEN)
        );

        logger.debug("Reply sent.");
        logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"game\"; Response: Game Chosen: \"" + game + "\"");
    }
}
