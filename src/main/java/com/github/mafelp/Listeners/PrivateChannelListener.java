package com.github.mafelp.Listeners;

import com.github.mafelp.Builders.PresentBuilder;
import com.github.mafelp.utils.Enums.PrivateListenerState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The listener which listens to private messages.
 */
public class PrivateChannelListener extends MessageCreateListener {
    /**
     * The logging instance to log statements to the console and the log file.
     */
    private static final Logger logger = LogManager.getLogger(PrivateChannelListener.class);
    /**
     * The number of threads of this message listener.
     */
    private static long threadID = 0;

    /**
     * The map used to save the states of all the users that have a wizard in their private chat running.
     */
    private static final Map<User, PrivateListenerState> listenerStates = new HashMap<>();

    /**
     * The method that handles actual execution of private messages.
     * @param messageCreateEvent The event class of the discord bot which contains useful information
     */
    @Override
    public void onMessageCreate(final MessageCreateEvent messageCreateEvent) {
        // Ignore all messages that are not a private message.
        if (messageCreateEvent.isGroupMessage() || messageCreateEvent.isServerMessage()) {
            logger.debug("Message is not a private message... Ignoring it.");
            return;
        }

        // Changes this threads name to make it more visible to the user, what the bot is currently doing.
        String currentThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName("PrivateMessageListener-" + threadID);
        ++threadID;

        // Checks if a user sent this message.
        if (messageCreateEvent.getMessageAuthor().asUser().isEmpty()) {
            messageCreateEvent.getChannel().sendMessage(
                    new EmbedBuilder()
                    .setColor(Color.RED)
                    .setAuthor(messageCreateEvent.getMessageAuthor())
                    .setTitle("Error!")
                    .setDescription("Sorry, only Users can use private messages with this bot!")
            ).thenAccept(message -> logger.debug("Message Author is not a User. Sending Error reply..."));
            return;
        }

        // Get which wizard is active in this channel.
        PrivateListenerState state = listenerStates.get(messageCreateEvent.getMessageAuthor().asUser().get());

        // null means, the user didn't have a wizard running, yet.
        if (state == null) {
            logger.info("Message sent via private message from \"" + messageCreateEvent.getMessageAuthor().getName() + "\": " + messageCreateEvent.getReadableMessageContent() + "\"; State: NONE -> MainListener handles this message.");
            return;
        }

        // Goes through the states and hands off the execution to their corresponding classes.
        switch (state) {
            case PRESENT -> {
                PresentBuilder.getPresentBuilder(messageCreateEvent.getMessageAuthor().asUser().get()).nextStep(messageCreateEvent.getReadableMessageContent());
                logger.info("Message sent via private message from \"" + messageCreateEvent.getMessageAuthor().getName() + "\": " + messageCreateEvent.getReadableMessageContent() + "\"; State: PRESENT -> Handing task over to the present builder.");
            }
            case NONE -> logger.info("Message sent via private message from \"" + messageCreateEvent.getMessageAuthor().getName() + "\": " + messageCreateEvent.getReadableMessageContent() + "\"; State: NONE -> MainListener handles this message.");
        }

        // Removes name changes from this thread.
        Thread.currentThread().setName(currentThreadName);
    }

    /**
     * The setter for a user to set the running wizard to.
     * @param user The user to set the wizard to.
     * @param listenerState The wizard you want to set.
     * @return If the state was successful.
     */
    public static boolean setListeningState(final User user, final PrivateListenerState listenerState)  {
        PrivateListenerState currentState = listenerStates.get(user);

        if (currentState == null) {
            listenerStates.put(user, listenerState);
            return true;
        }

        if (listenerState == PrivateListenerState.NONE) {
            listenerStates.replace(user, listenerState);
            return true;
        }

        if (currentState == PrivateListenerState.NONE) {
            listenerStates.replace(user, listenerState);
            return true;
        }
        return false;
    }

    /**
     * The getter for the listening state in the map.
     * @param user The user to get the state from.
     * @return The currently running wizard.
     */
    public static PrivateListenerState getListeningState(@NotNull final User user) {
        if (listenerStates.get(user) == null) {
            return PrivateListenerState.NONE;
        }
        return listenerStates.get(user);
    }
}