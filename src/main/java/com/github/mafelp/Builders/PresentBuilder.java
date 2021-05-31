package com.github.mafelp.Builders;

import com.github.mafelp.Listeners.PrivateChannelListener;
import com.github.mafelp.Manager.PresentManager;
import com.github.mafelp.utils.Enums.PrivateListenerState;
import com.google.gson.JsonObject;
import com.vdurmont.emoji.EmojiParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.PrivateChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
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
    private final User receiver;
    private Message message;

    public PresentBuilder(User sender, User receiver, Server server, Message originalMessage) {
        this.state = BuilderState.START;
        this.sender = sender;
        this.receiver = receiver;
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

        if (originalMessage.canYouDelete())
            originalMessage.delete().thenAccept(none -> logger.debug("Deleted original Message which contained the wrap command."));
        else
            originalMessage.addReaction(EmojiParser.parseToUnicode(":white_check_mark:")).thenAccept(none -> logger.debug("Could not delete original Message. Added a reaction instead."));

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

                this.message = channel.sendMessage(
                        new EmbedBuilder()
                        .setAuthor(this.sender)
                        .setColor(new Color(0xa201ff))
                        .setTitle("Present Wizard")
                        .setDescription("Welcome to the present creation Wizard!\nYou will be guided through the process of creating a present on the server " + this.server.getName() + " for user " + receiver.getMentionTag() + ".")
                        .addField("How to use this wizard", "Just send a message to this bot, with the content for the current field. The bot will then let you know, that it is ready for the next input.")
                        .addField("Reactions","To get a preview of the current present, just react to this message with " + EmojiParser.parseToUnicode(":eye:") + "\n\nTo build the present, just react to this message with " + EmojiParser.parseToUnicode(":white_check_mark:") + "\nTo cancel the wizard process, just react to this message with " + EmojiParser.parseToUnicode(":x:"))
                        .addField("Step 1","As a first Step, Please give a short title for this present.")
                ).join();
                message.addReaction(EmojiParser.parseToUnicode(":eye:"));
                message.addReaction(EmojiParser.parseToUnicode(":x:"));
                //TODO add Reaction listener.
                logger.debug("Started Present Building with a user in a private channel: Sent first Message.");
                this.state = BuilderState.TITLE;
            }
            case TITLE -> {
                present.addProperty("title", content);
                channel.sendMessage(
                        new EmbedBuilder()
                        .setAuthor(this.sender)
                        .setColor(new Color(0xa201ff))
                        .setTitle("Step 2")
                        .addField("Set the title to", content)
                        .addField("Step 2", "Please give a description for your present/a small text for the receiver.")
                ).thenAccept(message -> logger.debug("Added Title to present: " + content));
                this.state = BuilderState.CONTENT;
            }
            case CONTENT -> {
                present.addProperty("content", content);
                message.addReaction(EmojiParser.parseToUnicode(":white_check_mark:"));
                channel.sendMessage(
                        new EmbedBuilder()
                        .setAuthor(this.sender)
                        .setColor(new Color(0xa201ff))
                        .setTitle("Step 3")
                        .addField("Set the content of the message to", content)
                        .addField("Step 3", "Please give a direct URL to an Image, which will be displayed on your present.")
                        .setFooter("From now on, you can finish the creation process early, by reaction to the starting message with a green tick.")
                ).thenAccept(message -> logger.debug("Added content to present: " + content));
                this.state = BuilderState.IMAGE;
            }
            case IMAGE -> {
                present.addProperty("imageURL", content);
                channel.sendMessage(
                        new EmbedBuilder()
                        .setAuthor(this.sender)
                        .setColor(new Color(0x00aa00))
                        .setTitle("Finished!")
                        .setDescription("Your present has been built successfully and the wizard has finished!")
                        .addField("Added image to your present", "Link: " + content)
                        .setImage(content)
                ).thenAccept(message -> logger.debug("Added imageURL to present: " + content));

                this.state = BuilderState.FINISHED;
                return this.nextStep(content);
            }
            case FINISHED -> {
                channel.sendMessage("Your finished Present:",
                        PresentManager.buildPresent(this.present)
                ).thenAccept(message -> logger.debug(""));
                PresentManager.addPresent(server, this.present);
                PrivateChannelListener.setListeningState(this.sender, PrivateListenerState.NONE);
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