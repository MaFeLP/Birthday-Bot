package com.github.mafelp.Listeners;

import com.github.mafelp.Builders.PresentBuilder;
import com.github.mafelp.utils.Enums.PresentBuilderState;
import com.vdurmont.emoji.EmojiParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.jetbrains.annotations.Contract;

/**
 * The class that is being attached to messages when the PresentBuilder is running.
 */
public class PresentBuilderReactionListener implements ReactionAddListener {
    /**
     * The logging instance used to log statements to the console and the log file.
     */
    private static final Logger logger = LogManager.getLogger(PresentBuilderReactionListener.class);

    /**
     * The method that handles the actual execution of the event.
     * @param reactionAddEvent The event that is passed by the discord bot and contains
     *                         useful information about the message and reaction.
     */
    @Override
    public void onReactionAdd(ReactionAddEvent reactionAddEvent) {
        // Checks if the user who added the reaction is a user and sets it.
        User adder;
        if (reactionAddEvent.getUser().isPresent()) {
            adder = reactionAddEvent.getUser().get();
        } else {
            if (reactionAddEvent.getChannel().asPrivateChannel().isEmpty()) {
                logger.debug("Message not in a private channel. Ignoring...");
                return;
            }
            if (reactionAddEvent.getChannel().asPrivateChannel().get().getRecipient().isEmpty()) {
                logger.debug("ReactionOwner is Empty... ignoring...");
                return;
            }

            adder = reactionAddEvent.getChannel().asPrivateChannel().get().getRecipient().get();
        }
        // Checks that the user is not the bot.
        if (adder.isYourself()) {
            logger.debug("Bot added this reaction. Ignoring it...");
            return;
        }
        // Checks that the message is not empty.
        if (reactionAddEvent.getMessage().isEmpty()) {
            logger.debug("Message of reaction is empty. Ignoring it...");
            return;
        }

        logger.debug("Reaction to message(id=" + reactionAddEvent.getMessage().get().getId() + "): " + reactionAddEvent.getEmoji());

        // Checks if the user has a present builder running.
        // THIS SHOULD NEVER BE THE CASE, because the presentBuilder SHOULD detach this listener when it is finished.
        PresentBuilder presentBuilder = PresentBuilder.getPresentBuilder(adder);
        if (presentBuilder == null) {
            logger.warn("An error occurred while getting a present builder. I'm not sure why, though...");
            return;
        }

        // Goes through the reactions and executes corresponding actions.
        if (reactionAddEvent.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":white_check_mark:"))) {
            logger.debug("Build reaction...");
            presentBuilder.setState(PresentBuilderState.FINISHED).nextStep(null);
        } else if (reactionAddEvent.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":x:"))) {
            logger.debug("Cancel reaction...");
            presentBuilder.setState(PresentBuilderState.CANCELLED).nextStep(null);
        } else if (reactionAddEvent.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":eye:"))) {
            logger.debug("Preview reaction...");
            presentBuilder.sendPreview();
        } else {
            logger.debug("Unknown reaction...");
        }
    }
}
