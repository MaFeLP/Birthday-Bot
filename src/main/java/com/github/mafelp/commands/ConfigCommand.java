package com.github.mafelp.commands;

import com.github.mafelp.utils.Command;
import com.github.mafelp.utils.CommandParser;
import com.github.mafelp.utils.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ConfigCommand extends Thread {
    private static long threadID = 0;
    private final MessageCreateEvent messageCreateEvent;
    private final String prefix;
    private final Command command;

    private static final Logger logger = LogManager.getLogger(ConfigCommand.class);

    public ConfigCommand(MessageCreateEvent messageCreateEvent, Command command, String prefix) {
        this.messageCreateEvent = messageCreateEvent;
        this.prefix = prefix;
        this.command = command;

        this.setName("ConfigCommand-" + threadID);
        ++threadID;
    }

    @Override
    public void run() {
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

        Command subcmd = CommandParser.parseFromArray(command.getArguments());

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

                    logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"config get " + path + "; Result: " + value);
                } else {
                    logger.debug("Value to path " + path + " does not exist! Sending help embed.");
                    new MessageBuilder().setEmbed(
                            new EmbedBuilder()
                                    .setTitle("Error")
                                    .addField("Error getting value","There was an error whilst trying to get the value to " + path + "! Maybe it doesn't exists?")
                                    .setAuthor(messageCreateEvent.getMessageAuthor())
                                    .setColor(Color.RED)
                    ).send(messageCreateEvent.getChannel());
                    logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"config get " + path + "; Result: Value not present");
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

                logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"config get \"\"; Result: not enough arguments.");
            }
            logger.debug("Executed command 'config get'");
            return;
        }

        if (command.getStringArgument(2).isEmpty() || command.getStringArgument(3).isPresent()) {
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
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"config get \"\"; Result: not enough arguments.");
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
                        java.util.List<Boolean> booleanList = Configuration.config.getBooleanList(path);
                        booleanList.add(boolValue);
                        Configuration.config.set(path, booleanList);
                        logger.debug("Set " + path + " to " + booleanList);
                        // Checks if the argument is a number (long)
                    } else if (subcmd.getLongArgument(1).isPresent()) {
                        long longValue = subcmd.getLongArgument(1).get();
                        logger.debug("Adding boolean value " + longValue + " to " + path);
                        java.util.List<Long> longList = Configuration.config.getLongList(path);
                        longList.add(longValue);
                        Configuration.config.set(path, longList);
                        logger.debug("Set " + path + " to " + longList);

                        // the last check is, if the argument is a String.
                    } else if (subcmd.getStringArgument(1).isPresent()) {
                        String stringValue = subcmd.getStringArgument(1).get();
                        logger.debug("Adding boolean value " + stringValue + " to " + path);
                        java.util.List<String> stringList = Configuration.config.getStringList(path);
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
                        java.util.List<Boolean> booleanList = Configuration.config.getBooleanList(path);
                        booleanList.removeAll(Collections.singleton(boolValue));
                        Configuration.config.set(path, booleanList);
                        logger.debug(path + " is now set to: " + booleanList);

                        // Checks if the argument is a number (long)
                    } else if (subcmd.getLongArgument(1).isPresent()) {
                        long longValue = subcmd.getLongArgument(1).get();
                        logger.debug("Removing " + longValue + " from " + path);
                        java.util.List<Long> longList = Configuration.config.getLongList(path);
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
}

