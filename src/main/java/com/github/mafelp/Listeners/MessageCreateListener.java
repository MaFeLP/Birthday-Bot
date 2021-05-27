package com.github.mafelp.Listeners;

import com.github.mafelp.commands.*;
import com.github.mafelp.utils.Command;
import com.github.mafelp.utils.CommandParser;
import com.github.mafelp.utils.Configuration;
import com.github.mafelp.utils.exceptions.CommandNotFinishedException;
import com.github.mafelp.utils.exceptions.NoCommandGivenException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;

import java.util.*;
import java.util.List;

public class MessageCreateListener implements org.javacord.api.listener.message.MessageCreateListener {
    private static final Random random = new Random();
    private static final Logger logger = LogManager.getLogger(MessageCreateListener.class);
    private static long threadID = 0;

    @Override
    public void onMessageCreate(final MessageCreateEvent messageCreateEvent) {
        // Changes this threads name to make it more visible to the user, what the bot is currently doing.
        String currentThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName("MainListener-" + threadID);
        ++threadID;

        if (messageCreateEvent.getMessageAuthor().isYourself()) {
            logger.debug("Message sent by this bot. Ignoring...");
            return;
        }

        boolean messageSentToAllowedChannel = false;
        String content = messageCreateEvent.getReadableMessageContent();
        if (messageCreateEvent.getServer().isPresent()) {
            if (messageCreateEvent.getChannel().asServerChannel().isPresent()) {
                logger.info("Message sent to channel \"#" + messageCreateEvent.getChannel().asServerChannel().get().getName() + "\" on server \"" + messageCreateEvent.getServer().get().getName() + "\" by \"" + messageCreateEvent.getMessageAuthor().getName() + "\": " + content);
            }

            List<Long> listeningChannels = Configuration.getServerConfiguration(messageCreateEvent.getServer().get()).getLongList("listeningChannels");
            String listeningChannelsFromArray = listeningChannels.toString();
            logger.debug("listeningChannelsList: " + listeningChannelsFromArray);

            // If the list of listening channels is the default, check if the message is !init,
            // Then add this channel to the listening channels.
            if (listeningChannelsFromArray.equalsIgnoreCase("[1234]")) {
                logger.debug("Checking authority of user...");
                // Check authorization status of sender
                boolean authorized = false;
                if (messageCreateEvent.getMessageAuthor().isBotOwner()) {
                    logger.debug("User "+ messageCreateEvent.getMessageAuthor().getName() + " is authorized as bot owner.");
                    authorized = true;
                } else if (messageCreateEvent.getMessageAuthor().isServerAdmin()) {
                    logger.debug("User " + messageCreateEvent.getMessageAuthor().getName() + " is authorized as server admin.");
                    authorized = true;
                }

                if (content.toLowerCase().startsWith("!init")) {
                //if (content.toLowerCase().startsWith(Configuration.getServerConfiguration(messageCreateEvent.getServer().get()).getString("prefix") + "init")) {
                    if (authorized) {
                        listeningChannels = new ArrayList<>();
                        listeningChannels.add(messageCreateEvent.getChannel().getId());
                        logger.debug("listeningChannelsList now: " + listeningChannels);
                        YamlConfiguration currentConfig = Configuration.getServerConfiguration(messageCreateEvent.getServer().get());
                        currentConfig.set("listeningChannels", listeningChannels);
                        Configuration.save(messageCreateEvent.getServer().get(), currentConfig);

                        messageCreateEvent.getChannel().sendMessage(
                                new EmbedBuilder()
                                .setColor(Color.GREEN)
                                .setAuthor(messageCreateEvent.getMessageAuthor())
                                .setTitle("Success!")
                                .setDescription("Successfully added channel <#" + messageCreateEvent.getChannel().getId() + "> to the list of listening channels and disabled command \"!init\". You can now use this channel to send commands to the discord bot.")
                                .addField("Add more channels", "To add more channels to the list of listening channels, get the id of a channel, head over to https://mafelp.github.io/MCDC/get-channel-ID and follow the steps over there. Instead of executing the command as a player or in the console, execute the following command: \"!config add listeningChannels <THE CHANNEL ID>\"")
                        );

                        logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"!init\"; Response: Added channel with id \"" + messageCreateEvent.getChannel().getId() + "\" to the list of listening channels.");
                    } else {
                        logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"!init\"; Response: Permission denied.");
                        logger.debug("Sending permission denied Embed.");
                        messageCreateEvent.getChannel().sendMessage(
                                new EmbedBuilder()
                                .setColor(Color.RED)
                                .setAuthor(messageCreateEvent.getMessageAuthor())
                                .setTitle("Permission denied!")
                                .setDescription("Sorry, only the server admin, can use this command, if no channels were configured.")
                        );
                        logger.debug("Permission denied Embed sent!");
                        return;
                    }
                } // End of !init command.
            // If the list of channel IDs does not have the default, get through it and look, if this channel is in it.
            } else {
                for (long channelID : listeningChannels) {
                    if (channelID == messageCreateEvent.getChannel().getId()) {
                        logger.debug("Channel found in configuration: listeningChannels");
                        messageSentToAllowedChannel = true;
                        break;
                    }
                }
            }
        } else if (messageCreateEvent.getChannel().asGroupChannel().isPresent()) {
            logger.info("Message sent to group channel \"" + messageCreateEvent.getChannel().asGroupChannel().get().getName() + "\" by \"" + messageCreateEvent.getMessageAuthor().getName() + "\": " + content);
        } else if (messageCreateEvent.getChannel().asPrivateChannel().isPresent()) {
            logger.info("Message sent via private message from \"" + messageCreateEvent.getMessageAuthor().getName() + "\": " + content);

            if (Configuration.config.getBoolean("allowPrivateMessages")){
                logger.debug("Channel found as private message. Letting it pass.");
                messageSentToAllowedChannel = true;
            }
        } else {
            logger.warn("Message sent to no known channel type by \"" + messageCreateEvent.getMessageAuthor().getName() + "\": " + content);
        }

        if (content == null) {
            logger.debug("No content in the message!");
            return;
        }

        if (!messageSentToAllowedChannel) {
            logger.debug("Message was not sent to an allowed channel. Ignoring it.");
            return;
        }

        List<Long> members = Configuration.config.getLongList("members");
        List<String> games = Configuration.config.getStringList("games");
        List<String> happyBirthdaySongs = Configuration.config.getStringList("happyBirthdaySongs");
        String prefix = Configuration.config.getString("prefix");

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
            logger.debug("Stack-Trace of " + e.getMessage() + ":");
            for (var s : e.getStackTrace())
                logger.debug("\t" + s.toString());
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

        if (cmd.getCommand().equalsIgnoreCase(prefix + "wrap")) {
            WrapCommand wrapCommand = new WrapCommand(messageCreateEvent);
            wrapCommand.start();
        } else

        if (cmd.getCommand().equalsIgnoreCase(prefix + "unwrap")) {
            UnwrapCommand unwrapCommand = new UnwrapCommand(messageCreateEvent, cmd, prefix);
            unwrapCommand.start();
        } else

        if (cmd.getCommand().equalsIgnoreCase(prefix + "skribbl")) {
            SkribblCommand skribblCommand = new SkribblCommand(messageCreateEvent, cmd);
            skribblCommand.start();
        }

        // Removes name changes from this thread.
        Thread.currentThread().setName(currentThreadName);
    }
}