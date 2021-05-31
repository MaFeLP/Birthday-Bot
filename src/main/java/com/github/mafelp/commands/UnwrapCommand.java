package com.github.mafelp.commands;

import com.github.mafelp.Manager.PresentManager;
import com.github.mafelp.utils.Command;
import com.github.mafelp.utils.CommandParser;
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
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"unwrap\"; Response: Not enough arguments.");
            return;
        }
        String receiverTag = command.getStringArgument(0).get();
        if (!receiverTag.matches("<@([!]?)([0-123456789]*)>")) {
            sendHelpMessage();
            return;
        }

        if (messageCreateEvent.getServer().isEmpty()) {
            messageCreateEvent.getChannel().sendMessage(
                    new EmbedBuilder()
            ).thenAccept(message -> logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"unwrap\"; Response: Server is Empty."));
            return;
        }

        if (messageCreateEvent.getMessageAuthor().asUser().isEmpty()) {
            messageCreateEvent.getChannel().sendMessage(
                    new EmbedBuilder()
            ).thenAccept(message -> logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"unwrap\"; Response: User is Empty."));
            return;
        }

        var presents = PresentManager.getPresents(messageCreateEvent.getServer().get(), messageCreateEvent.getMessageAuthor().asUser().get());

        if (presents == null) {
            messageCreateEvent.getChannel().sendMessage(
                    new EmbedBuilder()
            ).thenAccept(message -> logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"unwrap\"; Response: Presents are Empty."));
            return;
        }

        for (JsonObject jsonObject : presents) {
            logger.debug("Current present: " + jsonObject.toString());
            messageCreateEvent.getChannel().sendMessage(PresentManager.buildPresent(jsonObject)).thenAccept(message -> logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" unpacked present \"" + jsonObject.get("title").getAsString() + "\"."));
        }
    }

    private void sendHelpMessage() {
        messageCreateEvent.getChannel().sendMessage(
                new EmbedBuilder()
        ).thenAccept(message -> logger.debug("Help message sent!"));
    }
}
