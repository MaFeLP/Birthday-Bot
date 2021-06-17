package com.github.mafelp.Listeners;

import com.github.mafelp.utils.Command;
import com.github.mafelp.utils.CommandParser;
import com.github.mafelp.utils.Configuration;
import com.github.mafelp.Manager.SkribblManager;
import com.github.mafelp.utils.exceptions.CommandNotFinishedException;
import com.github.mafelp.utils.exceptions.NoCommandGivenException;
import com.vdurmont.emoji.EmojiParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The listener which listens to all messages and if they are in a skribbl listening channel,
 * adds them to the list of skribbl words.
 */
public class SkribblListener extends MessageCreateListener {
    /**
     * The logging instance to log statements to the console and the log file.
     */
    private static final Logger logger = LogManager.getLogger(SkribblListener.class);

    /**
     * The list of channels which are defined as listening channel:
     * Only add skribbl words to their corresponding file if the channel appears in this list.
     */
    private static final List<ServerTextChannel> listeningChannels = new ArrayList<>();

    /**
     * The method that handles the actual execution of the handling of the skribbl words.
     * @param messageCreateEvent The event class of the discord bot which contains useful information
     */
    @Override
    public void onMessageCreate(final MessageCreateEvent messageCreateEvent) {
        // Check if the message was NOT sent by this bot.
        if (messageCreateEvent.getMessageAuthor().isYourself()) {
            logger.debug("Message was sent by this bot. Ignoring...");
            return;
        }

        // Check if the message was even sent on a server.
        if (messageCreateEvent.getServerTextChannel().isEmpty()) {
            logger.debug("Message was not sent to a skribbl listening channel...");
            return;
        }

        // Set some useful variables for later on.
        final ServerTextChannel serverTextChannel = messageCreateEvent.getServerTextChannel().get();
        final Server server = serverTextChannel.getServer();

        // Checks if the message is a command: If so, do not add the word to the skribbl list.
        if (messageCreateEvent.getReadableMessageContent().startsWith(Objects.requireNonNull(Configuration.getServerConfiguration(server).getString("prefix", "!")))) {
            logger.debug("Message is a command. Not treating it as a skribblWord.");
            return;
        }

        // Checks if the message was sent to a listening channel configured channel.
        boolean isListeningChannel = false;
        for (ServerTextChannel i : listeningChannels) {
            if (serverTextChannel.equals(i)) {
                logger.debug("Found message to be in a skribbl listening channel. Continuing...");
                isListeningChannel = true;
                break;
            }
        }
        if (!isListeningChannel) {
            logger.debug("Channel is not a skibbl Listening channel!");
            return;
        }

        // Parses the words with the command parser.
        try {
            final Command command = CommandParser.parseFromString(messageCreateEvent.getReadableMessageContent());
            SkribblManager.addSkribblWord(server, command.getCommand());

            if (command.getArguments() != null) {
                for (String s : command.getArguments()) {
                    SkribblManager.addSkribblWord(server, s);
                }
            }

            if (Configuration.getServerConfiguration(server).getBoolean("skribbl.addReaction", true) && Configuration.config.getBoolean("skribbl.addReaction", true))
                messageCreateEvent.addReactionToMessage(EmojiParser.parseToUnicode(":white_check_mark:"));
        } catch (CommandNotFinishedException | NoCommandGivenException e) {
            logger.debug("NoCommandGiven exception", e);
            if (Configuration.getServerConfiguration(server).getBoolean("skribbl.addReaction", true) && Configuration.config.getBoolean("skribbl.addReaction", true))
                messageCreateEvent.addReactionToMessage(EmojiParser.parseToUnicode(":negative_squared_cross_mark:"));
        }
    }

    /**
     * The getter for the listening channels.
     * @return the listening channels.
     */
    public static List<ServerTextChannel> getListeningChannels() {
        return listeningChannels;
    }

    /**
     * The method that attaches this listener to a channel.
     * @param channelToAdd The channel to attach to.
     */
    public static void addListeningChannel(ServerTextChannel channelToAdd) {
        listeningChannels.add(channelToAdd);
    }

    /**
     * The method that detaches this listener from a channel.
     * @param channelToRemove The channel to detach from.
     */
    public static void removeListeningChannel(ServerTextChannel channelToRemove) {
        listeningChannels.removeAll(Collections.singleton(channelToRemove));
    }
}
