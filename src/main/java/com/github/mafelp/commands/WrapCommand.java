package com.github.mafelp.commands;

import com.github.mafelp.Builders.PresentBuilder;
import com.github.mafelp.Listeners.PrivateChannelListener;
import com.github.mafelp.Main;
import com.github.mafelp.utils.Command;
import com.github.mafelp.utils.CommandParser;
import com.github.mafelp.utils.Enums.PrivateListenerState;
import com.github.mafelp.utils.exceptions.CommandNotFinishedException;
import com.github.mafelp.utils.exceptions.NoCommandGivenException;
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
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"wrap\"; Response: Not enough Arguments");
            sendHelpEmbed(messageCreateEvent);
            return;
        }

        if (messageCreateEvent.getServerTextChannel().isEmpty()) {
            messageCreateEvent.getChannel().sendMessage(
                    new EmbedBuilder()
                    .setAuthor(messageCreateEvent.getMessageAuthor())
                    .setColor(Color.RED)
                    .setTitle("Error!")
                    .setDescription("This command can only be used in ServerTextChannels! Please head over to one, where the bot can read the content as well!")
            ).join();
            messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":x:"));
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"wrap\"; Response: not a ServerTextChannel.");
            return;
        }

        if ("help".equals(command.getStringArgument(0).get().toLowerCase(Locale.ROOT))) {
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"wrap help\"; Response: Help embed");
            sendHelpEmbed(messageCreateEvent);
            return;
        }

        if (messageCreateEvent.getMessageAuthor().asUser().isEmpty()) {
            messageCreateEvent.getChannel().sendMessage(
                    new EmbedBuilder()
                    .setAuthor(messageCreateEvent.getMessageAuthor())
                    .setColor(Color.RED)
                    .setTitle("Error!")
                    .setDescription("Sorry, but only Users can pack presents! Please be a user to continue.")
            ).join();
            messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":x:"));
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"wrap\"; Response: Not a user.");
            return;
        }

        if (PrivateChannelListener.getListeningState(messageCreateEvent.getMessageAuthor().asUser().get()) == PrivateListenerState.NONE) {
            if (command.getStringArgument(0).get().matches("<@([!]?)([0-123456789]*)>")) {
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

                    PresentBuilder presentBuilder = new PresentBuilder(messageCreateEvent.getMessageAuthor().asUser().get(), receiver, messageCreateEvent.getServerTextChannel().get().getServer(), messageCreateEvent.getMessage());
                    presentBuilder.nextStep(command.getStringArgument(0).get());
                } catch (NumberFormatException e) {
                    logger.debug("ID not a long.");
                }
            } else {
                logger.debug("Wrong receiver");
            }
        } else {
            messageCreateEvent.getChannel().sendMessage(
                    new EmbedBuilder()
                    .setAuthor(messageCreateEvent.getMessageAuthor())
                    .setColor(Color.RED)
                    .setTitle("Error!")
                    .setDescription("Cannot initiate the wizard: You are currently in another wizard! Please cancel or finish this wizard, before executing this command again!")
            ).join();
            messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":x:")).join();
            logger.info("User " + messageCreateEvent.getMessageAuthor().asUser().get().getName() + " executed command \"wrap\"; Response: Already in another wizard.");
        }
    }

    private void sendHelpEmbed(MessageCreateEvent messageCreateEvent) {
        messageCreateEvent.getChannel().sendMessage(
                new EmbedBuilder()
                .setColor(new Color(0xFFC270))
                .setAuthor(messageCreateEvent.getMessageAuthor())
                .setTitle("Help page for command: " + command.getCommand())
                .setDescription("The wrap command wraps a present for another server member. This command starts initiating a wizard in a private channel with this bot, where you will be taken through all necessary steps, to create your personal present!")
                .addField("Arguments","There is only one argument needed to execute this command. This argument is a ping to the member, who shall receiver this present.")
                .setFooter("Your command message will be deleted, if possible.")
        ).thenAccept(message -> logger.debug("Help message sent!"));

        messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":x:"));
    }
}