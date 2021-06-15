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
import java.util.concurrent.CompletionException;

/**
 * The class that handles asynchronous execution of the Unwrap command.
 */
public class WrapCommand extends Thread {
    /**
     * The logger which is used to log statements to the console.
     */
    private static final Logger logger = LogManager.getLogger(ConfigCommand.class);

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
    private Command command;

    /**
     * The "Default" Constructor
     * @param messageCreateEvent The Event that is being passed to this class by the discord API.
     */
    public WrapCommand(MessageCreateEvent messageCreateEvent) {
        this.messageCreateEvent = messageCreateEvent;

	// Initialises the command with the message content, not the <b>readable</b> message contents.
        Command cmd;
        try {
            cmd = CommandParser.parseFromString(messageCreateEvent.getMessageContent());
            logger.debug("Command is: " + cmd.getCommand());

            if (cmd.getArguments() != null)
                logger.debug("Arguments are: " + Arrays.toString(cmd.getArguments()));
            else
                logger.debug("Arguments are: null");
        } catch (NoCommandGivenException e) {
            logger.error("An error occurred while parsing the message contents." + e.getMessage());
            logger.debug("Stack-Trace of " + e.getMessage() + ":", e);
            return;
        } catch (CommandNotFinishedException e) {
            logger.debug("Exception caught!", e);
            logger.debug("Sending help embed.");

            messageCreateEvent.getChannel().sendMessage(
                    new EmbedBuilder()
                            .setColor(Color.RED)
                            .setAuthor(messageCreateEvent.getMessageAuthor())
                            .setTitle("Error!")
                            .addField("Command not finished Exception", "Please finish your command with a quotation mark!")
            );
            return;
        }

        this.command = cmd;

        this.setName("WrapCommand-" + threadID);
        ++threadID;
    }

    /**
     * The method handles the actual execution of this command.
     */
    @Override
    public void run() {
        if (this.command == null)
            return;

        logger.debug("Executing command wrap...");

        // Send the help embed on wrong usage.
        if (command.getStringArgument(0).isEmpty()) {
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"wrap\"; Response: Not enough Arguments");
            sendHelpEmbed(true);
            return;
        }

        // Sends an error Embed, if the message was not sent on a server.
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

        // If the argument is help, send a help embed.
        if ("help".equals(command.getStringArgument(0).get().toLowerCase(Locale.ROOT))) {
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"wrap help\"; Response: Help embed");
            sendHelpEmbed(false);
            return;
        }

        // If the executor of the command is not a user, send an error.
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

        // Check if the user has no current wizard running.
        if (PrivateChannelListener.getListeningState(messageCreateEvent.getMessageAuthor().asUser().get()) == PrivateListenerState.NONE) {
            if (command.getStringArgument(0).get().matches("<@([!]?)[1-9]{1}[0-9]{16,18}>")) {
                // Pass the 1st argument into a discord id.
                StringBuilder builder = new StringBuilder();
                for (char c : command.getStringArgument(0).get().toCharArray()) {
                    if (c != '<' && c != '@' && c != '!' && c != '>')
                        builder.append(c);
                }
                String receiverID = builder.toString();

                // Try to create a PresentBuilder, catch errors and send embeds.
                try {
                    User receiver = Main.discordApi.getUserById(receiverID).join();

                    PresentBuilder presentBuilder = new PresentBuilder(messageCreateEvent.getMessageAuthor().asUser().get(), receiver, messageCreateEvent.getServerTextChannel().get().getServer(), messageCreateEvent.getMessage());
                    presentBuilder.nextStep(command.getStringArgument(0).get());
                } catch (NumberFormatException e) {
                    sendReceiverErrorEmbed();
                    logger.info("User \"" + messageCreateEvent.getMessageAuthor().asUser().get().getName() + "\" executed command \"wrap\"; Response: Number in the argument too long.");
                } catch (CompletionException e) {
                    sendReceiverErrorEmbed();
                    logger.info("User \"" + messageCreateEvent.getMessageAuthor().asUser().get().getName() + "\" executed command \"wrap\"; Response: User with ID not known.");
                }
            } else {
                sendReceiverErrorEmbed();
                logger.info("User \"" + messageCreateEvent.getMessageAuthor().asUser().get().getName() + "\" executed command \"wrap\"; Response: No ID given.");
            }
        // If the user HAS a wizard running, exit and send an embed.
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

    /**
     * Sends the help embed to the channel in which the original message was sent to.
     * @param addReaction If a negative Reaction should be added to the original message.
     */
    private void sendHelpEmbed(boolean addReaction) {
        messageCreateEvent.getChannel().sendMessage(
                new EmbedBuilder()
                .setColor(new Color(0xFFC270))
                .setAuthor(messageCreateEvent.getMessageAuthor())
                .setTitle("Help page for command: " + command.getCommand())
                .setDescription("The wrap command wraps a present for another server member. This command starts initiating a wizard in a private channel with this bot, where you will be taken through all necessary steps, to create your personal present!")
                .addField("Arguments","There is only one argument needed to execute this command. This argument is a ping to the member, who shall receiver this present.")
                .setFooter("Your command message will be deleted, if possible.")
        ).thenAccept(message -> logger.debug("Help message sent!"));

        if (addReaction)
            messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":x:"));
    }

    /**
     * Sends a receiver error embed, which says, that no user was found with this id, or that the argument passed
     * in is not a valid id.
     */
    private void sendReceiverErrorEmbed() {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.RED)
                .setAuthor(messageCreateEvent.getMessageAuthor())
                .setTitle("Error!")
                .setDescription("You pinged/passed in a non existent user! Please try again!")
                ;
        if (command.getStringArgument(0).isPresent())
            embed.addField("Your Input:", command.getStringArgument(0).get());
        messageCreateEvent.getChannel().sendMessage(
                embed
        ).thenAccept(message -> logger.debug("ReceiverErrorEmbed sent!"));

        messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":x:"));
    }
}
