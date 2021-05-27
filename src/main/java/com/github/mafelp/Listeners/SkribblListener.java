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

public class SkribblListener extends MessageCreateListener {
    private static final Logger logger = LogManager.getLogger(SkribblListener.class);

    private static final List<ServerTextChannel> listeningChannels = new ArrayList<>();

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        if (messageCreateEvent.getMessageAuthor().isYourself()) {
            logger.debug("Message was sent by this bot. Ignoring...");
            return;
        }

        if (messageCreateEvent.getServerTextChannel().isEmpty()) {
            logger.debug("Message was not sent to a skribbl listening channel...");
            return;
        }

        ServerTextChannel serverTextChannel = messageCreateEvent.getServerTextChannel().get();
        Server server = serverTextChannel.getServer();

        if (messageCreateEvent.getReadableMessageContent().startsWith(Objects.requireNonNull(Configuration.getServerConfiguration(server).getString("prefix", "!")))) {
            logger.debug("Message is a command. Not treating it as a skribblWord.");
            return;
        }

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

        try {
            Command command = CommandParser.parseFromString(messageCreateEvent.getReadableMessageContent());
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

    public static List<ServerTextChannel> getListeningChannels() {
        return listeningChannels;
    }

    public static List<ServerTextChannel> addListeningChannel(ServerTextChannel channelToAdd) {
        listeningChannels.add(channelToAdd);
        return listeningChannels;
    }

    public static List<ServerTextChannel> removeListeningChannel(ServerTextChannel channelToRemove) {
        listeningChannels.removeAll(Collections.singleton(channelToRemove));
        return listeningChannels;
    }
}
