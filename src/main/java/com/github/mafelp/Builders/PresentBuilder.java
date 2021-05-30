package com.github.mafelp.Builders;

import com.github.mafelp.Listeners.PrivateChannelListener;
import com.github.mafelp.Manager.PresentManager;
import com.github.mafelp.utils.Enums.PrivateListenerState;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

public class PresentBuilder {
    private static final Logger logger = LogManager.getLogger(PresentBuilder.class);

    private static final Map<User, PresentBuilder> presentBuilders = new HashMap<>();

    private BuilderState state;
    private final JsonObject present;
    private PrivateChannel channel;
    private final Server server;
    private final User sender;

    public PresentBuilder(User sender, User receiver, Server server) {
        this.state = BuilderState.START;
        this.sender = sender;
        this.server = server;

        if (sender.getPrivateChannel().isPresent())
            this.channel = sender.getPrivateChannel().get();
        else {
            try {
                this.channel = sender.openPrivateChannel().join();
            } catch (CompletionException e) {
                logger.info("could not open a private chat with user: " + sender.getName());
                logger.debug("Stack trace: ", e);
                this.channel = null;
            }
        }

        this.present = new JsonObject();
        present.addProperty("sender", sender.getId());
        present.addProperty("receiver", receiver.getId());

        presentBuilders.put(sender, this);
    }

    public static PresentBuilder getPresentBuilder(@NotNull User user) {
        return presentBuilders.get(user);
    }

    public JsonObject getPresent() {
        return this.present;
    }

    public PresentBuilder nextStep(String content) {
        if (channel == null) {
            return null;
        }

        switch (this.state) {
            case START -> {
                if (!(PrivateChannelListener.setListeningState(this.sender, PrivateListenerState.PRESENT))) {
                    logger.debug("There is currently another listening in this channel is progress!");
                    return null;
                }

                channel.sendMessage(
                        new EmbedBuilder()
                        .setTitle("asdf")
                ).thenAccept(message -> logger.debug("Started Present Building with a user in a private channel: Sent first Message."));
                this.state = BuilderState.TITLE;
            }
            case TITLE -> {
                present.addProperty("title", content);
                channel.sendMessage(
                        new EmbedBuilder()
                        .setTitle("asdf2")
                ).thenAccept(message -> logger.debug("Added Title to present: " + content));
                this.state = BuilderState.CONTENT;
            }
            case CONTENT -> {
                present.addProperty("content", content);
                this.state = BuilderState.IMAGE;
            }
            case IMAGE -> {
                present.addProperty("imageURL", content);

                this.state = BuilderState.FINISHED;
                return this.nextStep(content);
            }
            case FINISHED -> {
                PresentManager.addPresent(server, this.present);
                PrivateChannelListener.setListeningState(this.sender, PrivateListenerState.NONE);
                this.channel.sendMessage(PresentManager.buildPresent(this.present, this.sender));
                presentBuilders.remove(this.sender);
            }
        }

        return this;
    }
}

enum BuilderState{
    START,
    TITLE,
    CONTENT,
    IMAGE,
    FINISHED,
}