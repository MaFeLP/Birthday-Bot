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

public class UnwrapCommand extends Thread {
    private static long threadID = 0;

    private final MessageCreateEvent messageCreateEvent;
    private Command command;

    private static final Logger logger = LogManager.getLogger(UnwrapCommand.class);

    public UnwrapCommand(MessageCreateEvent messageCreateEvent) {
        this.messageCreateEvent = messageCreateEvent;

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

    @Override
    public void run() {
        if (this.command == null)
            return;

        if (command.getStringArgument(0).isEmpty()) {
            sendHelpMessage(true);
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"unwrap\"; Response: Not enough arguments.");
            return;
        }

        String receiverTag = command.getStringArgument(0).get();
        if (!receiverTag.matches("<@([!]?)([0-123456789]*)>")) {
            sendHelpMessage(true);
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"unwrap\"; Response: Wrong argument.");
            return;
        }

        if (receiverTag.equalsIgnoreCase("help")) {
            sendHelpMessage(false);
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"unwrap help\"; Help page.");
            return;
        }

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

        var presents = PresentManager.getPresents(messageCreateEvent.getServer().get(), messageCreateEvent.getMessageAuthor().asUser().get());

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

        for (JsonObject jsonObject : presents) {
            logger.debug("Current present: " + jsonObject.toString());
            messageCreateEvent.getChannel().sendMessage(PresentManager.buildPresent(jsonObject)).thenAccept(message -> logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" unpacked present \"" + jsonObject.get("title").getAsString() + "\"."));
        }
    }

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
