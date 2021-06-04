package com.github.mafelp.commands;

import com.github.mafelp.utils.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;

public class BirthdayCommand extends Thread {
    private static final Logger logger = LogManager.getLogger(BirthdayCommand.class);

    private final MessageCreateEvent messageCreateEvent;
    private final Command command;

    public BirthdayCommand(Command command, MessageCreateEvent messageCreateEvent) {
        this.messageCreateEvent = messageCreateEvent;
        this.command = command;
    }

    @Override
    public void run() {
        messageCreateEvent.getChannel().sendMessage(
                new EmbedBuilder()
                .setAuthor(messageCreateEvent.getApi().getYourself())
                .setColor(Color.RED)
                .setTitle("Error: Not implemented!")
                .setDescription("Sorry, this command is currently not available!\n" +
                        "Please ask the bot Owner, to update to the latest version!")
        ).thenAccept(message -> logger.info("User " + messageCreateEvent.getMessageAuthor().getName() + " used command " + command.getCommand() + "; Response: Command not available."));
    }
}