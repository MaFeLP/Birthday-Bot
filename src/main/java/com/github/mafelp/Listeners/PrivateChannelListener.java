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

public class PrivateChannelListener extends MessageCreateListener {
    private static final Logger logger = LogManager.getLogger(PrivateChannelListener.class);
    private static long threadID = 0;

    private static final Map<User, PrivateListenerState> listenerStates = new HashMap<>();

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        if (messageCreateEvent.isGroupMessage() || messageCreateEvent.isServerMessage()) {
            logger.debug("Message is not a private message... Ignoring it.");
            return;
        }

        // Changes this threads name to make it more visible to the user, what the bot is currently doing.
        String currentThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName("PrivateMessageListener-" + threadID);
        ++threadID;

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

        PrivateListenerState state = listenerStates.get(messageCreateEvent.getMessageAuthor().asUser().get());

        if (state == null) {
            logger.info("Message sent via private message from \"" + messageCreateEvent.getMessageAuthor().getName() + "\": " + messageCreateEvent.getReadableMessageContent() + "\"; State: NONE -> MainListener handles this message.");
            return;
        }

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

    public static boolean setListeningState(User user, PrivateListenerState listenerState)  {
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

    public static PrivateListenerState getListeningState(@NotNull User user) {
        if (listenerStates.get(user) == null) {
            return PrivateListenerState.NONE;
        }
        return listenerStates.get(user);
    }
}