package com.github.mafelp.Listeners;

import com.github.mafelp.Builders.PresentBuilder;
import com.github.mafelp.utils.Enums.PresentBuilderState;
import com.vdurmont.emoji.EmojiParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;

public class PresentBuilderReactionListener implements ReactionAddListener {
    private static final Logger logger = LogManager.getLogger(PresentBuilderReactionListener.class);

    public PresentBuilderReactionListener() {

    }

    @Override
    public void onReactionAdd(ReactionAddEvent reactionAddEvent) {
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
        if (adder.isYourself()) {
            logger.debug("Bot added this reaction. Ignoring it...");
            return;
        }
        if (reactionAddEvent.getMessage().isEmpty()) {
            logger.debug("Message of reaction is empty. Ignoring it...");
            return;
        }

        logger.debug("Reaction to message(id=" + reactionAddEvent.getMessage().get().getId() + "): " + reactionAddEvent.getEmoji());

//        boolean isListeningMessage = false;
//        for (Message message : listeningMessages) {
//            if (message.equals(reactionAddEvent.getMessage().get())) {
//                isListeningMessage = true;
//                logger.debug("Found message in listening Messages!");
//                break;
//            }
//        }
//
//        if (!isListeningMessage) {
//            logger.debug("Message is not in the list of listening messages. Ignoring...");
//            return;
//        }

        if (reactionAddEvent.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":white_check_mark:"))) {
            logger.debug("Build reaction...");
            PresentBuilder presentBuilder = PresentBuilder.getPresentBuilder(adder);
            if (presentBuilder == null) {
                logger.warn("An error occurred while getting a present builder. I'm not sure why, though...");
                return;
            }
            presentBuilder.setState(PresentBuilderState.FINISHED).nextStep(null);
        } else if (reactionAddEvent.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":x:"))) {
            logger.debug("Cancel reaction...");
        } else if (reactionAddEvent.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(":eye:"))) {
            logger.debug("Preview reaction...");
        } else {
            logger.debug("Unknown reaction...");
        }
    }
}
