package com.github.mafelp.commands;

import com.github.mafelp.utils.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

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
        logger.debug("Executing command " + command.getCommand() + Arrays.toString(command.getArguments()));
        if (command.getStringArgument(0).isEmpty()) {
            sendHelpMessage(true);
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command " + command.getCommand() + "\"; Response: Not enough arguments");
            return;
        }
        switch (command.getStringArgument(0).get().toLowerCase(Locale.ROOT)) {
            case "help" -> {
                sendHelpMessage(false);
                logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command " + command.getCommand() + " help\"; Response: Help Embed.");
            }
            case "", " " -> {
                sendHelpMessage(true);
                logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command " + command.getCommand() + "\"; Response: NOt enough arguments");
            }
            case "start", "stop", "date" -> {
                messageCreateEvent.getChannel().sendMessage(
                        new EmbedBuilder()
                        .setAuthor(messageCreateEvent.getMessageAuthor())
                        .setColor(Color.RED)
                        .setTitle("Feature not implemented!")
                        .setDescription("Sorry, this feature is currently not available! In the meantime, please enjoy a picture of some tasty cake!")
                        .setThumbnail("https://upload.wikimedia.org/wikipedia/commons/a/a2/Gabe-birthday-part.jpg")
                        .setFooter("Image Source: wikimedia")
                        .addField("What can I do?", "Ask the bot owner to upgrade to the latest version. If they already did it, wait a few days and check again, because I'm then still working on the feature.")
                ).thenAccept(message -> logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command " + command.getCommand() + "\"; Response: Feature not implemented."));
            }
            default -> {
                sendHelpMessage(true);
                logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command " + command.getCommand() + "\"; Response: NOt enough arguments");
            }
        }
    }

    private void sendHelpMessage(boolean wrongUsage) {
        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(messageCreateEvent.getMessageAuthor())
                .setColor(new Color(0xFFB500))
                .setTitle("Help for command " + command.getCommand())
                .setDescription("The birthday command allows you to manage (running) birthdays.")
                .addInlineField("start <UserPing>", "Promotes a user temporarily to an authorised user.")
                .addInlineField("stop", "Stops the currently running birthday(s).")
                .addInlineField("date", "Adds your birthday date to the list of birthdays, so I can initiate a birthday at your birthday!")
                //.addInlineField("","")
                .addInlineField("help","Displays this message")
                ;

        if (wrongUsage)
            embed.setTitle("Wrong Usage!")
                    .setColor(new Color(0xFF7F4C))
                    .setFooter("Help for command " + command.getCommand(), messageCreateEvent.getApi().getYourself().getAvatar())
                    ;
    }
}