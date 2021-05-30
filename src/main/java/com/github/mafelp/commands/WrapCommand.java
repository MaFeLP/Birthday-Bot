package com.github.mafelp.commands;

import com.github.mafelp.Builders.PresentBuilder;
import com.github.mafelp.Listeners.PrivateChannelListener;
import com.github.mafelp.Main;
import com.github.mafelp.Manager.PresentManager;
import com.github.mafelp.utils.Command;
import com.github.mafelp.utils.CommandParser;
import com.github.mafelp.utils.Enums.PrivateListenerState;
import com.github.mafelp.utils.exceptions.CommandNotFinishedException;
import com.github.mafelp.utils.exceptions.NoCommandGivenException;
import com.google.gson.JsonObject;
import com.vdurmont.emoji.EmojiParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.Locale;

public class WrapCommand extends Thread {
    private static long threadID = 0;

    private final MessageCreateEvent messageCreateEvent;
    private Command command;

    private static final Logger logger = LogManager.getLogger(WrapCommand.class);

    public WrapCommand(MessageCreateEvent messageCreateEvent) {
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

        this.setName("WrapCommand-" + threadID);
        ++threadID;
    }

    @Override
    public void run() {
        if (this.command == null)
            return;

        logger.debug("Executing command wrap...");

        if (command.getStringArgument(0).isEmpty()) {
            logger.debug("Not enough arguments.");
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"wrap\"; Response: Not enough Arguments");
            sendHelpEmbed();
            return;
        }

        if (messageCreateEvent.getServerTextChannel().isEmpty()) {
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"wrap\"; Response: not a ServerTextChannel.");
            return;
        }

        if ("help".equals(command.getStringArgument(0).get().toLowerCase(Locale.ROOT))) {
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"wrap help\"; Response: Help embed");
            sendHelpEmbed();
            return;
        }

        if (messageCreateEvent.getMessageAuthor().asUser().isEmpty()) {
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"wrap\"; Response: Not a user.");
            return;
        }

        if (PrivateChannelListener.getListeningState(messageCreateEvent.getMessageAuthor().asUser().get()) == PrivateListenerState.NONE) {
            if (command.getStringArgument(0).get().matches("<@([!]?)([0-123456789]*)>")) {
                // Removes any '!' from the user, because some user's mention tag don't contain them!
                StringBuilder builder = new StringBuilder();
                for (char c : command.getStringArgument(0).get().toCharArray()) {
                    if (c != '<' && c != '@' && c != '!' && c != '>')
                        builder.append(c);
                }
                String receiverID = builder.toString();

                try {
                    User receiver = Main.discordApi.getUserById(receiverID).join();

                    if (receiver == null) {
                        logger.debug("Wrong receiver");
                        messageCreateEvent.addReactionsToMessage(EmojiParser.parseToUnicode(":x:"));
                        return;
                    }

                    PresentBuilder presentBuilder = new PresentBuilder(messageCreateEvent.getMessageAuthor().asUser().get(), receiver, messageCreateEvent.getServer().get());
                    presentBuilder.nextStep(command.getStringArgument(0).get());
                } catch (NumberFormatException e) {
                    logger.debug("ID not a long.");
                }
            } else {
                logger.debug("Wrong receiver");
                messageCreateEvent.addReactionsToMessage(EmojiParser.parseToUnicode(":x:"));
            }
        } else {
            JsonObject present = new JsonObject();

            for (String completeArgument : command.getArguments()) {
                StringBuilder argumentBuilder = new StringBuilder();
                StringBuilder valueBuilder = new StringBuilder();
                boolean isValue = false;

                for (char c : completeArgument.toCharArray()) {
                    if (c == '=')
                        isValue = true;
                    else {
                        if (isValue)
                            valueBuilder.append(c);
                        else
                            argumentBuilder.append(c);
                    }
                }
                String firstPartArgument = argumentBuilder.toString();
                String value = valueBuilder.toString();

                if (firstPartArgument.equals(completeArgument) || value.equals("")) {
                    //TODO add Argument Error Embed
                    sendHelpEmbed();
                    logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"wrap\"; Response: Argument Error. + Help");
                    return;
                }

                switch (firstPartArgument.toLowerCase(Locale.ROOT)) {
                    case "content" -> {
                        logger.debug("Add to Present: \"content\": \"" + value + "\"");
                        present.addProperty("content", value);
                    }
                    case "imageurl", "imagelink" -> {
                        logger.debug("Add to Present: \"imageLink\": \"" + value + "\"");
                        present.addProperty("imageURL", value);
                    }
                    case "title" -> {
                        logger.debug("Add to Present: \"Title\": \"" + value + "\"");
                        present.addProperty("title", value);
                    }
                    case "receiver" -> {
                        logger.debug("Add to Present: \"receiver\": \"" + value + "\"");
                        if (value.matches("<@([!]?)([0-123456789]*)>")) {
                            // Removes any '!' from the user, because some user's mention tag don't contain them!
                            valueBuilder = new StringBuilder();
                            for (char c : value.toCharArray()) {
                                if (c != '!')
                                    valueBuilder.append(c);
                            }
                            value = valueBuilder.toString();

                            present.addProperty("receiver", value);
                        } else {
                            logger.debug("Wrong receiver");
                            messageCreateEvent.addReactionsToMessage(EmojiParser.parseToUnicode(":x:"));
                            return;
                        }
                    }
                }
            }

            present.addProperty("author", messageCreateEvent.getMessageAuthor().getId());

            logger.debug("Present is in JSON: " + present);
            if (messageCreateEvent.getServer().isPresent()) {
                PresentManager.addPresent(messageCreateEvent.getServer().get(), present);
                messageCreateEvent.addReactionsToMessage(EmojiParser.parseToUnicode(":white_check_mark:"));
                PresentManager.savePresents();

                logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"wrap\"; Response: Created Present Embed.");
            } else {
                logger.debug("Not on a Server.");
                messageCreateEvent.addReactionsToMessage(EmojiParser.parseToUnicode(":x:"));

                logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"wrap\"; Response: not on a server.");
            }
        }
    }

    private void sendHelpEmbed() {
        messageCreateEvent.getChannel().sendMessage(
                new EmbedBuilder()
        ).thenAccept(message -> logger.debug("Help message sent!"));
    }
}