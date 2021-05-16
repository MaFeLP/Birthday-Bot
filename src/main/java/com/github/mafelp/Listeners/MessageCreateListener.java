package com.github.mafelp.Listeners;

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

import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
        } else

        if (cmd.getCommand().equalsIgnoreCase(prefix + "game")) {
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
            logger.debug("Executing command random...");
            if (cmd.getArguments() == null) {
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

            int r = random.nextInt(cmd.getArguments().length);

            logger.debug("Choosing random argument...");
            if (cmd.getStringArgument(r).isPresent()) {
                logger.debug("Random argument chosen: " + cmd.getStringArgument(r).get());
                logger.debug("Sending reply!");
                new MessageBuilder().setEmbed(
                        new EmbedBuilder()
                                .setTitle("Random")
                                .addField("Random value got chosen!", "Result: " + cmd.getStringArgument(r).get())
                                .setColor(Color.YELLOW)
                                .setAuthor(messageCreateEvent.getMessageAuthor())
                ).send(messageCreateEvent.getChannel());

                logger.info(messageCreateEvent.getMessageAuthor().getName() + " executed command random.");
            } else {
                new MessageBuilder().setEmbed(
                        new EmbedBuilder()
                                .setTitle("Error!")
                                .setAuthor(messageCreateEvent.getMessageAuthor())
                                .setColor(Color.RED)
                                .addField("Unknown Error", "An unknown error occurred. Please try again!")
                );

                logger.warn("Something went wrong while " + messageCreateEvent.getMessageAuthor().getName() + " tried to execute command random.");
            }
        } else

        if (cmd.getCommand().equalsIgnoreCase(prefix + "config")) {
            logger.debug("Executing config command...");
            logger.debug("Checking authority of user...");
            // Check authorization status of sender
            boolean authorized = false;

            if (messageCreateEvent.getMessageAuthor().isBotOwner()) {
                logger.debug("User "+ messageCreateEvent.getMessageAuthor().getName() + " is authorized as bot owner.");
                authorized = true;
            }

            if (messageCreateEvent.getMessageAuthor().isServerAdmin()) {
                logger.debug("User "+ messageCreateEvent.getMessageAuthor().getName() + " is authorized as server admin.");
                authorized = true;
            }

            for (long id :
                    Configuration.config.getLongList("authorizedAccountIDs")) {
                if (id == messageCreateEvent.getMessageAuthor().getId()) {
                    logger.debug("User " + messageCreateEvent.getMessageAuthor().getName() + " is in authorized accounts list..");
                    authorized = true;
                }
            }

            if (!authorized) {
                logger.warn("User " + messageCreateEvent.getMessageAuthor().getName() + "(" + messageCreateEvent.getMessageAuthor().getId() + ")" + " tried to execute the config command!");
                logger.debug("Sending error reply...");
                new MessageBuilder()
                        .setEmbed(
                                new EmbedBuilder()
                                .setAuthor(messageCreateEvent.getMessageAuthor())
                                .setTitle("Error!")
                                .setColor(Color.RED)
                                .addField("Permission denied error", "You do not have the permission to execute this command!")
                        ).send(messageCreateEvent.getChannel());
                logger.debug("Error reply sent.");
                return;
            }

            Command subcmd = CommandParser.parseFromArray(cmd.getArguments());

            // subcommand get:
            // gets the value of a path in the configuration
            if (subcmd.getCommand().equalsIgnoreCase("get")) {
                logger.debug("Executing subcommand get...");
                // if there is a argument after get, execute.
                if (subcmd.getStringArgument(0).isPresent()) {
                    String path = subcmd.getStringArgument(0).get();
                    Object value = Configuration.config.get(path);

                    logger.debug("Getting the configuration entry to " + path);
                    if (value != null) {
                        new MessageBuilder().setEmbed(
                                new EmbedBuilder()
                                        .setAuthor(messageCreateEvent.getMessageAuthor())
                                        .setColor(Color.CYAN)
                                        .setTitle("Configuration Entry")
                                        .addField(path, value.toString())
                        ).send(messageCreateEvent.getChannel());
                        logger.debug("Configuration entry is: " + value);
                    } else {
                        logger.debug("Value to path " + path + " does not exist! Sending help embed.");
                        new MessageBuilder().setEmbed(
                                new EmbedBuilder()
                                        .setTitle("Error")
                                        .addField("Error getting value","There was an error whilst trying to get the value to " + path + "! Maybe it doesn't exists?")
                                        .setAuthor(messageCreateEvent.getMessageAuthor())
                                        .setColor(Color.RED)
                        ).send(messageCreateEvent.getChannel());
                    }
                // if no argument was parsed into the subcommand, give the person an error message.
                } else {
                    logger.debug("Person passed not enough arguments into the command. Sending help embed.");
                    new MessageBuilder().setEmbed(
                            new EmbedBuilder()
                                    .setColor(Color.RED)
                                    .setAuthor(messageCreateEvent.getMessageAuthor())
                                    .setTitle("Error")
                                    .addField("Argument error","Not enough arguments given! Please use config get <path>!")
                    ).send(messageCreateEvent.getChannel());
                    logger.debug("Help embed sent.");
                }
                logger.debug("Executed command 'config get'");
                return;
            }

            if (cmd.getStringArgument(2).isEmpty() || cmd.getStringArgument(3).isPresent()) {
                logger.debug("User " + messageCreateEvent.getMessageAuthor().getName() + " did not pass enough arguments. Sending help embed.");

                new MessageBuilder()
                        .setEmbed(
                                new EmbedBuilder()
                                .setAuthor(messageCreateEvent.getMessageAuthor())
                                .setColor(Color.RED)
                                .setTitle("Error!")
                                .addField("Not enough arguments!","Usage: " + prefix + "config <set|get|add|remove> <path> <value>")
                        ).send(messageCreateEvent.getChannel());
                logger.debug("Help embed sent!");
                return;
            }

            switch (subcmd.getCommand().toLowerCase(Locale.ROOT)) {
                // subcommand set:
                // sets a value in the configuration to the specified value
                case "set" -> {
                    // checks if an argument is present and if so,
                    // tries to get a boolean, long and at last a string from the argument.
                    subcmd.getStringArgument(0).ifPresent(path -> {
                        // Checks if the argument is a boolean
                        if (subcmd.getBooleanArgument(1).isPresent()) {
                            logger.info("Setting the config entry for " + path + " to " + subcmd.getBooleanArgument(1).get() + ".");
                            boolean boolValue = subcmd.getBooleanArgument(1).get();
                            Configuration.config.set(path, boolValue);
                            // Checks if the argument is a number (long)
                        } else if (subcmd.getLongArgument(1).isPresent()) {
                            logger.info("Setting the config entry for " + path + " to " + subcmd.getLongArgument(1).get() + ".");
                            long longValue = subcmd.getLongArgument(1).get();
                            Configuration.config.set(path, longValue);
                            // the last check is, if the argument is a String.
                        } else if (subcmd.getStringArgument(1).isPresent()){
                            logger.info("Setting the config entry for " + path + " to " + subcmd.getStringArgument(1).get() + ".");
                            String stringValue = subcmd.getStringArgument(1).get();
                            Configuration.config.set(path, stringValue);
                            // If the argument could not be parsed, we throw an error.
                        } else {
                            logger.error("An unknown error occurred during command config set!");
                            logger.debug("Sending help embed.");
                            new MessageBuilder().setEmbed(
                                    new EmbedBuilder()
                                    .addField("Unknown Error","An unknown error happened. Please try again!")
                                    .setColor(Color.RED)
                                    .setAuthor(messageCreateEvent.getMessageAuthor())
                                    .setTitle("Error!")
                            ).send(messageCreateEvent.getChannel());
                            logger.debug("Help embed sent.");
                        }
                    });

                    try {
                        logger.info("Saving the configuration...");
                        Configuration.config.save("./config.yml");
                        logger.info("Configuration saved!");
                    } catch (IOException ioException) {
                        logger.error("Could not save the configuration!", ioException);
                    }
                    
                    // Send a success message
                    if (subcmd.getStringArgument(0).isPresent() && subcmd.getStringArgument(1).isPresent()) {
                        logger.debug("Successfully executed command config set!");
                        logger.debug("Sending success embed...");
                        new MessageBuilder().setEmbed(
                                new EmbedBuilder()
                                .setTitle("Success!")
                                .addField("Saving config", "Successfully saved and reloaded the configuration file.")
                                .setColor(Color.GREEN)
                                .setAuthor(messageCreateEvent.getMessageAuthor())
                        ).send(messageCreateEvent.getChannel());
                        logger.debug("Success embed sent!");
                    }
                }
                // subcommand add
                // adds a value to a list.
                case "add" -> {
                    logger.debug("Executing subcommand add...");
                    // checks if an argument is present and if so,
                    // tries to get a boolean, long and at last a string from the argument.
                    subcmd.getStringArgument(0).ifPresent(path -> {
                        // Checks if the argument is a boolean
                        if (subcmd.getBooleanArgument(1).isPresent()) {
                            boolean boolValue = subcmd.getBooleanArgument(1).get();
                            logger.debug("Adding boolean value " + boolValue + " to " + path);
                            List<Boolean> booleanList = Configuration.config.getBooleanList(path);
                            booleanList.add(boolValue);
                            Configuration.config.set(path, booleanList);
                            logger.debug("Set " + path + " to " + booleanList);
                            // Checks if the argument is a number (long)
                        } else if (subcmd.getLongArgument(1).isPresent()) {
                            long longValue = subcmd.getLongArgument(1).get();
                            logger.debug("Adding boolean value " + longValue + " to " + path);
                            List<Long> longList = Configuration.config.getLongList(path);
                            longList.add(longValue);
                            Configuration.config.set(path, longList);
                            logger.debug("Set " + path + " to " + longList);

                        // the last check is, if the argument is a String.
                        } else if (subcmd.getStringArgument(1).isPresent()) {
                            String stringValue = subcmd.getStringArgument(1).get();
                            logger.debug("Adding boolean value " + stringValue + " to " + path);
                            List<String> stringList = Configuration.config.getStringList(path);
                            stringList.add(stringValue);
                            Configuration.config.set(path, stringList);
                            logger.debug("Set " + path + " to " + stringList);
                        }
                    });

                    // Send a success message
                    if (subcmd.getStringArgument(0).isPresent() && subcmd.getStringArgument(1).isPresent()) {
                        new MessageBuilder().setEmbed(
                                new EmbedBuilder()
                                        .setColor(Color.GREEN)
                                        .setAuthor(messageCreateEvent.getMessageAuthor())
                                        .setTitle("Success!")
                                        .addField("Setting config entry", "Successfully saved the config entry " + subcmd.getStringArgument(0).get() + " to " + Configuration.config.getStringList(subcmd.getStringArgument(0).get()))
                        ).send(messageCreateEvent.getChannel());
                    }

                    logger.debug("Executed subcommand add.");
                }
                // subcommand remove
                // removes a value from a list
                case "remove" -> {
                    logger.debug("Executing subcommand remove...");
                    // checks if an argument is present and if so,
                    // tries to get a boolean, long and at last a string from the argument.
                    subcmd.getStringArgument(0).ifPresent(path -> {
                        // Checks if the argument is a boolean
                        if (subcmd.getBooleanArgument(1).isPresent()) {
                            boolean boolValue = subcmd.getBooleanArgument(1).get();
                            logger.debug("Removing " + boolValue + " from " + path);
                            List<Boolean> booleanList = Configuration.config.getBooleanList(path);
                            booleanList.removeAll(Collections.singleton(boolValue));
                            Configuration.config.set(path, booleanList);
                            logger.debug(path + " is now set to: " + booleanList);

                        // Checks if the argument is a number (long)
                        } else if (subcmd.getLongArgument(1).isPresent()) {
                            long longValue = subcmd.getLongArgument(1).get();
                            logger.debug("Removing " + longValue + " from " + path);
                            List<Long> longList = Configuration.config.getLongList(path);
                            longList.removeAll(Collections.singleton(longValue));
                            Configuration.config.set(path, longList);
                            logger.debug(path + " is now set to: " + longList);


                        // the last check is, if the argument is a String.
                        } else if (subcmd.getStringArgument(1).isPresent()){
                            String stringValue = subcmd.getStringArgument(1).get();
                            logger.debug("Removing " + stringValue + " from " + path);
                            List<String> stringList = Configuration.config.getStringList(path);
                            stringList.removeAll(Collections.singleton(stringValue));
                            Configuration.config.set(path, stringList);
                            logger.debug(path + " is now set to: " + stringList);
                        }
                    });

                    // Send a success message
                    logger.debug("Sending success message...");
                    if (subcmd.getStringArgument(0).isPresent() && subcmd.getStringArgument(1).isPresent()) {
                        new MessageBuilder().setEmbed(
                                new EmbedBuilder()
                                .setColor(Color.GREEN)
                                .setAuthor(messageCreateEvent.getMessageAuthor())
                                .setTitle("Success!")
                                .addField("Success", "Successfully removed the item from the configuration!")
                        ).send(messageCreateEvent.getChannel());
                    }
                    logger.debug("Success message sent.");
                }
                // No subcommand is given:
                default ->  {
                    logger.debug("no subcommand given. Sending help embed...");
                        new MessageBuilder().setEmbed(
                                new EmbedBuilder()
                        .setColor(Color.RED)
                        .setTitle("Error")
                        .setAuthor(messageCreateEvent.getMessageAuthor())
                        .addField("Usage", "config <set|add|get|remove> <path> [<value>]")
                    ).send(messageCreateEvent.getChannel());
                    logger.debug("Help embed sent!");
                    return;
                }
            }
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" (ID: " + messageCreateEvent.getMessageAuthor().getIdAsString() + ") changed the configuration!");

            Configuration.save();
        }

        if (cmd.getCommand().equalsIgnoreCase(prefix + "unwrap")) {
            if (cmd.getStringArgument(0).isPresent()) {
                switch (cmd.getStringArgument(0).get()) {
                    case "<@!459019618686730271>" -> {
                        EmbedBuilder reply = new EmbedBuilder()
                                .setImage("https://i.otto.de/i/otto/5a519a15-6d3f-5ffa-bc70-0a968a744b23")
                                .setColor(new Color(0xff2bce))
                                .addField("Your Present!", "Dein Geschenk von <@459019618686730271> ist ein Flaschenhalter für dein Fahrrad!")
                                ;

                        // Sets MaFeLP as the message author
                            try {
                                reply.setAuthor(messageCreateEvent.getApi().getUserById(459019618686730271L).get());
                            } catch (InterruptedException | ExecutionException e) {
                                reply.setAuthor("Max");

                                logger.error("Something went wrong getting the user by id: 459019618686730271!");
                                logger.debug("Stack Trace: ", e);
                            }

                        messageCreateEvent.getChannel().sendMessage(reply);
                    }
                    default -> {
                        messageCreateEvent.getChannel().sendMessage(
                                new EmbedBuilder()
                        );

                        logger.info("User \"" + messageCreateEvent.getMessageAuthor().getDisplayName() + "\" passed not enough arguments to the command \"" + prefix + "unwrap\"!");
                    }
                }
            } else {
                messageCreateEvent.getChannel().sendMessage(
                        new EmbedBuilder()
                        .setColor(Color.RED.darker())
                        .setAuthor(messageCreateEvent.getMessageAuthor())
                        .setTitle("Error!")
                        .addField("NotEnoughArgumentsError","Usage: " + prefix + "unwrap <@User to unwrap the present of>")
                        .setFooter("")
                );
            }
        }
    }
}