package com.github.mafelp.commands;

import com.github.mafelp.Manager.PresentManager;
import com.github.mafelp.utils.Command;
import com.github.mafelp.utils.CommandParser;
import com.github.mafelp.utils.Configuration;
import com.github.mafelp.utils.exceptions.CommandNotFinishedException;
import com.github.mafelp.utils.exceptions.NoCommandGivenException;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.Arrays;

/**
 * The class that handles asynchronous execution of the Unwrap command.
 */
public class UnwrapCommand extends Thread {
    /**
     * The logger which is used to log statements to the console.
     */
    private static final Logger logger = LogManager.getLogger(SkribblCommand.class);

    /**
     * The number of threads of this kind that were being created.
     */
    private static long threadID = 0;


    /**
     * The Event that is being passed to this class by the discord API.
     */
    private final MessageCreateEvent messageCreateEvent;

    /**
     * The command which was being parsed with the {@link com.github.mafelp.utils.CommandParser} command parser.
     */
    private final Command command;

    /**
     * Default Constructor sets thread names for this thread.
     * @param messageCreateEvent The Event that is being passed to this class by the discord API.
     */
    public UnwrapCommand(MessageCreateEvent messageCreateEvent) {
        this.messageCreateEvent = messageCreateEvent;

	// Initialises the command with the message content, not the <b>readable</b> message contents.
        Command cmd = null;
        try {
            cmd = CommandParser.parseFromString(messageCreateEvent.getMessageContent());
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
        this.command = cmd;

        this.setName("UnwrapCommand-" + threadID);
        ++threadID;
    }

    /**
     * The method handles the actual execution of this command.
     */
    @Override
    public void run() {
        if (this.command == null)
            return;

	// Check if arguments were given. If not, send the help hembed
        if (command.getStringArgument(0).isEmpty()) {
            sendHelpMessage(true);
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"unwrap\"; Response: Not enough arguments.");
            return;
        }

	// Check if the first argument is a valid ping to a user.
        String receiverTag = command.getStringArgument(0).get();
        if (!receiverTag.matches("<@([!]?)[1-9]{1}[0-9]{16,18}>")) {
            sendHelpMessage(true);
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"unwrap\"; Response: Wrong argument.");
            return;
        }

	// Check if the first argument is "help", if so, send the help message.
        if (receiverTag.equalsIgnoreCase("help")) {
            sendHelpMessage(false);
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"unwrap help\"; Help page.");
            return;
        }

	// Check if the message was sent on a server
        if (messageCreateEvent.getServer().isEmpty()) {
            messageCreateEvent.getChannel().sendMessage(
                    new EmbedBuilder()
                    .setAuthor(messageCreateEvent.getMessageAuthor())
                    .setColor(Color.RED)
                    .setTitle("Error!")
                    .setDescription("Sorry, but this command can only be executed on a server!")
            ).thenAccept(message -> logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"unwrap\"; Response: Server is Empty."));
            return;
        }

	// check if the message creator is a user.
        if (messageCreateEvent.getMessageAuthor().asUser().isEmpty()) {
            messageCreateEvent.getChannel().sendMessage(
                    new EmbedBuilder()
                    .setAuthor(messageCreateEvent.getMessageAuthor())
                    .setColor(Color.RED)
                    .setTitle("Error!")
                    .setDescription("Sorry, but only users can unwrap presents!")
                    //.setFooter("Use \"" + command.getCommand() + " help\" to get help on this command!")
            ).thenAccept(message -> logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"unwrap\"; Response: User is Empty."));
            return;
        }

	// Get all presents for the user on this server.
        var presents = PresentManager.getPresents(messageCreateEvent.getServer().get(), messageCreateEvent.getMessageAuthor().asUser().get());

	// If this user does not have any presents, send an error.
        if (presents == null) {
            messageCreateEvent.getChannel().sendMessage(
                    new EmbedBuilder()
                    .setColor(new Color(0xff5959))
                    .setAuthor(messageCreateEvent.getMessageAuthor())
                    .setTitle("No Presents!")
                    .setDescription("Sorry, but " + command.getStringArgument(0).get() + " does not have any presents for you!")
                    .setFooter("Use \"" + Configuration.getServerConfiguration(messageCreateEvent.getServer().get()).getString("prefix") + "wrap help\" to get help on how to wrap a present for a specific user!")
            ).thenAccept(message -> logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"unwrap\"; Response: Presents are Empty."));
            return;
        }

	// Build all presents and ssend them to the channel.
        for (JsonObject jsonObject : presents) {
            logger.debug("Current present: " + jsonObject.toString());
            messageCreateEvent.getChannel().sendMessage(PresentManager.buildPresent(jsonObject)).thenAccept(message -> logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" unpacked present \"" + jsonObject.get("title").getAsString() + "\"."));
        }
    }

    /**
     * Sends the help message to the channel, in which the command was executed.
     * @param beError if the message should have the error as a title and the color red (only help embed does not send an error).
     */
    private void sendHelpMessage(boolean beError) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(new Color(0xFFC270))
                .setAuthor(messageCreateEvent.getMessageAuthor())
                .setTitle("Help for command: " + command.getCommand())
                .setDescription("The unwrap command unwraps a present from another server member, which has been packed before, using the wrap command.")
                .addField("Arguments","There is only one argument needed to execute this command. This argument is a ping to the member, whose present you wan to unpack.")
                ;

        if (beError) {
            embed.setColor(Color.RED)
                    .setTitle("Wrong usage of command: " + command.getCommand());
        }

        messageCreateEvent.getChannel().sendMessage(
                embed
        ).thenAccept(message -> logger.debug("Help message sent!"));
    }
}
