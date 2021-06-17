package com.github.mafelp.Listeners;

import com.github.mafelp.utils.Configuration;
import com.github.mafelp.Manager.SkribblManager;
import com.github.mafelp.utils.PermissionValidate;
import com.vdurmont.emoji.EmojiParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;

import java.awt.*;
import java.util.concurrent.CompletionException;

/**
 * The class that handles confirming reactions when trying to reset the skribbl file.
 */
public class SkribblReactionListener implements ReactionAddListener {

    /**
     * The logging instance to log statements to the console and the log file.
     */
    private static final Logger logger = LogManager.getLogger(SkribblReactionListener.class);

    /**
     * The method that handles the actual execution of the event.
     * @param reactionAddEvent The event passed in by the ot instance which contains useful information
     *                         about the message and the reaction.
     */
    @Override
    public void onReactionAdd(ReactionAddEvent reactionAddEvent) {
        // Check if reaction is on a server
        // THIS SHOULD NOT BE EXECUTED IN NORMAL PROCEDURES,
        // because the listener is only attached and removed to/from messages on a server.
        if (reactionAddEvent.getServer().isEmpty()) {
            reactionAddEvent.getChannel().sendMessage(
                    new EmbedBuilder()
                            .setAuthor(reactionAddEvent.getApi().getYourself())
                            .setTitle("Not A Server Error!")
                            .setColor(Color.RED)
                            .setDescription("Sorry, this command can only be used on servers!")
            ).thenAccept(message -> logger.debug("Not a server message sent."));
            return;
        }
        // Check if the reactor is a user.
        User reactionAuthor;
        if (reactionAddEvent.getUser().isPresent()) {
            logger.debug("Got user from event.");
            reactionAuthor = reactionAddEvent.getUser().get();
        } else {
            try {
            reactionAuthor = reactionAddEvent.getApi().getUserById(reactionAddEvent.getUserId()).join();
            } catch (CompletionException completionException) {
                logger.debug("Not a user.");
                return;
            }
            logger.debug("Got user from API and ID.");
        }

        // If the user is the bot, ignore this reaction.
        if (reactionAuthor.isYourself()) {
            logger.debug("Reaction Author is yourself.");
            return;
        }

        // Checks if the user is authorised to reset the skribbl file.
        if (reactionAuthor.isBotOwner()) {
            logger.debug("User authorised as bot owner.");
        } else if (reactionAddEvent.getServer().get().isOwner(reactionAuthor)) {
            logger.debug("User authorizes as Server Owner.");
        } else if (reactionAddEvent.getServer().get().isAdmin(reactionAuthor)) {
            logger.debug("User authorised as Server Admin.");
        } else {
            if (PermissionValidate.serverPermission(reactionAddEvent.getServer().get(), reactionAuthor)) {
                logger.debug("User authorised as authorised by configuration.");
            } else {
                logger.debug("User does not have the permission to reset the skribbl words.");

                reactionAddEvent.getChannel().sendMessage(
                        new EmbedBuilder()
                                .setAuthor(reactionAuthor)
                                .setTitle("Permission denied!")
                                .setColor(Color.RED)
                                .setDescription("Sorry, you do not have the required permissions to execute this command!")
                );

                return;
            }
        }

        logger.debug("User with id: \"" + reactionAddEvent.getUserIdAsString() + "\" reacted with " + reactionAddEvent.getEmoji());

        // Goes through the reactions and handles them accordingly.
        // If the reaction is confirm:
        if (reactionAddEvent.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":white_check_mark:"))) {
            logger.debug("Continue Reaction...");
            if (reactionAddEvent.getMessage().isPresent()) {
                if (reactionAddEvent.getMessage().get().canYouDelete()) {
                    reactionAddEvent.getMessage().get().delete().thenAccept(none -> logger.debug("Original Message deleted."));
                }
            } else {
                logger.debug("Message not present anymore?");
            }

            logger.debug("Resetting skribbl words for server: " + reactionAddEvent.getServer().get().getName());
            SkribblManager.resetSkribblWords(reactionAddEvent.getServer().get());
            logger.debug("Skribbl words reset! Sending success message...");

            reactionAddEvent.getChannel().sendMessage(
                    new EmbedBuilder()
                    .setAuthor(reactionAddEvent.getApi().getYourself())
                    .setTitle("Success!")
                    .setColor(Color.GREEN)
                    .setDescription("Successfully reset your skribbl words!\n\n To add new words, use \"" + Configuration.getServerConfiguration(reactionAddEvent.getServer().get()).getString("prefix", "!") + "skribbl start\"!")
            ).thenAccept(message -> logger.debug("Success message sent!"));
            logger.info("Reset the skribbl words for server \"" + reactionAddEvent.getServer().get().getName() + "\".");
        // If the reaction is cancel.
        } else if (reactionAddEvent.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":negative_squared_cross_mark:"))) {
            logger.debug("Cancel reaction...");

            if (reactionAddEvent.getMessage().isPresent()) {
                if (reactionAddEvent.getMessage().get().canYouDelete()) {
                    reactionAddEvent.getMessage().get().delete().thenAccept(none -> logger.debug("Original Message deleted."));
                }
            } else {
                logger.debug("Message not present anymore?");
            }

            reactionAddEvent.getChannel().sendMessage(
                    new EmbedBuilder()
                    .setAuthor(reactionAddEvent.getApi().getYourself())
                    .setColor(Color.YELLOW)
                    .setTitle("Cancelled!")
                    .setDescription("Successfully cancelled the resetting of the skribbl words.")
            ).thenAccept(message -> logger.debug("Cancel message sent!"));
            logger.info("Cancelled resetting the skribbl words for server \"" + reactionAddEvent.getServer().get().getName() + "\".");
        // If the reaction is unknown.
        } else {
            logger.debug("Unknown reaction... Ignoring it...");
            return;
        }

        reactionAddEvent.getApi().removeListener(this);
        reactionAddEvent.removeAllReactionsFromMessage().thenAccept(none -> logger.debug("Removed all reactions from message."));
    }
}
