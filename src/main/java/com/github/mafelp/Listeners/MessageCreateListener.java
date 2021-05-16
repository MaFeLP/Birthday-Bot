package com.github.mafelp.Listeners;

import com.github.mafelp.commands.*;
import com.github.mafelp.utils.Command;
import com.github.mafelp.utils.CommandParser;
import com.github.mafelp.utils.Configuration;
import com.github.mafelp.utils.exceptions.CommandNotFinishedException;
import com.github.mafelp.utils.exceptions.NoCommandGivenException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;

import java.util.*;
import java.util.List;

public class MessageCreateListener implements org.javacord.api.listener.message.MessageCreateListener {
    private static final Random random = new Random();
    private static final Logger logger = LogManager.getLogger(MessageCreateListener.class);

    @Override
    public void onMessageCreate(final MessageCreateEvent messageCreateEvent) {
        if (messageCreateEvent.getMessageAuthor().isYourself()) {
            logger.debug("Message sent by this bot. Ignoring...");
            return;
        }

        String content = messageCreateEvent.getReadableMessageContent();
        logger.info("Message sent to channel " + messageCreateEvent.getChannel().getId() + "; " + content);

        if (content == null) {
            logger.debug("No content in the message!");
            return;
        }

        boolean cont = false;
        for (long channelID :
                Configuration.config.getLongList("listeningChannels")) {
            if (channelID == messageCreateEvent.getChannel().getId()) {
                cont = true;
                logger.debug("Channel found in configuration: listeningChannels");
                break;
            }
        }

        if (!cont) {
            logger.debug("Message was not sent to an allowed channel. Ignoring it.");
            return;
        }

        List<Long> members = Configuration.config.getLongList("members");
        List<String> games = Configuration.config.getStringList("games");
        List<String> happyBirthdaySongs = Configuration.config.getStringList("happyBirthdaySongs");
        String prefix = Configuration.config.getString("prefix");

        logger.debug(messageCreateEvent.getMessageAuthor().getName() + " sent message " + content);
        if (prefix != null && !content.startsWith(prefix)) {
            logger.debug("Message not a command. Ignoring it.");
            return;
        }

        Command cmd = null;

        try {
            cmd = CommandParser.parseFromString(content);
            logger.debug("Command is: " + cmd.getCommand());

            if (cmd.getArguments() != null)
                logger.debug("Arguments are: " + Arrays.toString(cmd.getArguments()));
            else
                logger.debug("Arguments are: null");
        } catch (NoCommandGivenException e) {
            logger.error("An error occurred while parsing the message contents." + e.getMessage());
            return;
        } catch (CommandNotFinishedException e) {
            logger.debug("Exception caught!" ,e);
            logger.debug("Sending help embed.");

            messageCreateEvent.getChannel().sendMessage(
                    new EmbedBuilder()
                    .setColor(Color.RED)
                    .setAuthor(messageCreateEvent.getMessageAuthor())
                    .setTitle("Error!")
                    .addField("Command not finished Exception", "Please finish your command with a quotation mark!")
            );
        }

        if (cmd == null) {
            logger.error("command is null! Ignoring...");
            return;
        }

        if (cmd.getCommand().equalsIgnoreCase(prefix + "person")) {
            PersonCommand personCommand = new PersonCommand(messageCreateEvent, members);
            personCommand.start();
        } else

        if (cmd.getCommand().equalsIgnoreCase(prefix + "game")) {
            GameCommand gameCommand = new GameCommand(messageCreateEvent, games);
            gameCommand.start();
        } else

        if (cmd.getCommand().equalsIgnoreCase(prefix + "randomPlay")) {
            logger.debug("Executing command randomPlay...");
            int r = random.nextInt(happyBirthdaySongs.size());

            logger.debug("Sending play message...");
            new MessageBuilder()
                    .append("!play ")
                    .append(happyBirthdaySongs.get(r))
                    .send(messageCreateEvent.getChannel())
            ;

            logger.debug("Play message sent.");
            logger.debug("Executed command randomPlay.");
        } else

        if (cmd.getCommand().equalsIgnoreCase(prefix + "random")) {
            RandomCommand randomCommand = new RandomCommand(messageCreateEvent, cmd, prefix);
            randomCommand.start();
        } else

        if (cmd.getCommand().equalsIgnoreCase(prefix + "config")) {
            ConfigCommand configCommand = new ConfigCommand(messageCreateEvent, cmd, prefix);
            configCommand.start();
        } else

        if (cmd.getCommand().equalsIgnoreCase(prefix + "unwrap")) {
            UnwrapCommand unwrapCommand = new UnwrapCommand(messageCreateEvent, cmd, prefix);
            unwrapCommand.start();
        }
    }
}