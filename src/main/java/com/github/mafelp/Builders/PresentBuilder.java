package com.github.mafelp.Builders;

import com.github.mafelp.Listeners.PresentBuilderReactionListener;
import com.github.mafelp.Listeners.PrivateChannelListener;
import com.github.mafelp.Manager.PresentManager;
import com.github.mafelp.utils.Enums.PresentBuilderState;
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
    /**
     * The logger, which is used to log information to the console.
     */
    private static final Logger logger = LogManager.getLogger(PresentBuilder.class);

    /**
     * The map which saves the current state and all the presentBuilds for Users.
     */
    private static final Map<User, PresentBuilder> presentBuilders = new HashMap<>();

    /**
     * The state of the current builder.
     */
    private PresentBuilderState state;
    /**
     * The present which will be configured.
     */
    private final JsonObject present;

    /**
     * The channel, to which the messages were sent.
     */
    private PrivateChannel channel;

    /**
     * The server on which the presents should be created.
     */
    private final Server server;

    /**
     * The user who created the present.
     */
    private final User sender;

    /**
     * The user who shall receive and can unpack the present.
     */
    private final User receiver;

    /**
     * The first message sent by this bot in order to initiate the wizard.
     */
    private Message message;

    /**
     * The Listener, which listens to to {@link PresentBuilder#message}.
     */
    private final PresentBuilderReactionListener presentBuilderReactionListener = new PresentBuilderReactionListener();

    /**
     * @param sender The user who has triggered this event and will create a present.
     * @param receiver The first argument, which defines the user, who shall receive and be able to unpack the present.
     * @param server The server on which the present shall be unpacked.
     * @param originalMessage The original message, sent on the server.
     */
    public PresentBuilder(User sender, User receiver, Server server, Message originalMessage) {
        // Initiates some builder resources.
        this.state = PresentBuilderState.START;
        this.sender = sender;
        this.receiver = receiver;
        this.server = server;
        this.present = new JsonObject();
        present.addProperty("sender", sender.getId());
        present.addProperty("receiver", receiver.getId());

        // Gets/opens a private channel with the user.
        if (sender.getPrivateChannel().isPresent())
            this.channel = sender.getPrivateChannel().get();
        else {
            try {
                this.channel = sender.openPrivateChannel().join();
            } catch (CompletionException e) {
                logger.info("Could not open a private chat with user: " + sender.getName());
                logger.debug("Stack trace: ", e);
                this.channel = null;
            }
        }

        if (originalMessage.canYouDelete())
            originalMessage.delete().thenAccept(none -> logger.debug("Deleted original Message which contained the wrap command."));
        else
            originalMessage.addReaction(EmojiParser.parseToUnicode(":white_check_mark:")).thenAccept(none -> logger.debug("Could not delete original Message. Added a reaction instead."));
        presentBuilders.put(sender, this);
    }

    /**
     * @param user The user whose present builder will be returned.
     * @return The present builder from the User.
     */
    public static PresentBuilder getPresentBuilder(@NotNull User user) {
        return presentBuilders.get(user);
    }

    /**
     * @return Getter for the JSONObject, which is the present.
     */
    public JsonObject getPresent() {
        return this.present;
    }

    /**
     * @param builderState The current state in the enum that represents the current state of the present builder.
     * @return The new state of the present builder.
     */
    public PresentBuilder setState(PresentBuilderState builderState) {
        this.state = builderState;
        return this;
    }

    /**
     * This method sends a preview to the configured private channel.
     */
    public void sendPreview() {
        if (channel == null)
            return;

        JsonObject previewPresent = present.getAsJsonObject();
        // Logic to prevent the PresentManager#buildPresent from failing with an error, that no title/content was found.
        if (previewPresent.get("title") == null || previewPresent.get("title").getAsString().isEmpty() || previewPresent.get("title").getAsString().isBlank()) {
            previewPresent.addProperty("title","Placeholder-Title");
            logger.debug("Adding temporary title to present: " + present.get("title").getAsString());
        }
        if (previewPresent.get("content") == null || previewPresent.get("content").getAsString().isEmpty() || previewPresent.get("content").getAsString().isBlank()) {
            previewPresent.addProperty("content","Placeholder-Content (needs to be configured, before the present can be built!)");
            logger.debug("Adding content to present: " + present.get("content").getAsString());
        }

        this.channel.sendMessage(
                "Here is a preview, of your present: ", PresentManager.buildPresent(previewPresent)
        ).thenAccept(message -> logger.debug("Sent Preview Message"));
    }

    /**
     * The method that handles the actual execution of the present builder.
     * @param content The content of the message.
     * @return The current instance of the present builder to allow the chaining of methods.
     */
    public PresentBuilder nextStep(String content) {
        if (channel == null) {
            return null;
        }

        switch (this.state) {
            case START -> {
                // Initiation sequence, will be executed on startup and sends the Wizard-Start-Message.
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

                // Also add some reactions, to make the life of the user easier.
                message.addReaction(EmojiParser.parseToUnicode(":eye:"));
                message.addReaction(EmojiParser.parseToUnicode(":x:"));
                message.addReactionAddListener(this.presentBuilderReactionListener);
                logger.debug("Started Present Building with a user in a private channel: Sent first Message.");
                this.state = PresentBuilderState.TITLE;
            }
            case TITLE -> {
                // The first step of the present builder, which sets the title of the present.
                present.addProperty("title", content);
                channel.sendMessage(
                        new EmbedBuilder()
                        .setAuthor(this.sender)
                        .setColor(new Color(0xa201ff))
                        .setTitle("Step 2")
                        .addField("Set the title to", content)
                        .addField("Step 2", "Please give a description for your present/a small text for the receiver.")
                ).thenAccept(message -> logger.debug("Added Title to present: " + content));
                this.state = PresentBuilderState.CONTENT;
            }
            case CONTENT -> {
                // The second step of the present builder, which sets the content of the present.
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
                this.state = PresentBuilderState.IMAGE;
            }
            case IMAGE -> {
                // The third step of the present builder, which sets the image of the present.
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

                this.state = PresentBuilderState.FINISHED;
                return this.nextStep(content);
            }
            case FINISHED -> {
                // Cleanup routine, will be executed after the last step.
                channel.sendMessage("Your finished Present:",
                        PresentManager.buildPresent(this.present)
                ).thenAccept(message -> logger.debug("Successfully built the present!"));
                PresentManager.addPresent(server, this.present);
                PrivateChannelListener.setListeningState(this.sender, PrivateListenerState.NONE);
                this.message.removeMessageAttachableListener(this.presentBuilderReactionListener);
                this.message.removeAllReactions().thenAccept(none -> logger.debug("Removed all Reactions from original Present Message."));
                presentBuilders.remove(this.sender);
                PresentManager.savePresents(this.server);
            }
            case CANCELLED -> {
                // Cancel routine, will be executed, if the user has reacted with :x:
                channel.sendMessage(
                        new EmbedBuilder()
                        .setAuthor(this.sender)
                        .setColor(new Color(0xe7ff7c))
                        .setTitle("Cancelled!")
                        .setDescription("You successfully cancelled the PresentBuilding Wizard! You will now be able to create another present or use another wizard!")
                ).thenAccept(message -> logger.debug("Cancelled Present Building for user" + this.sender.getName()));

                PrivateChannelListener.setListeningState(this.sender, PrivateListenerState.NONE);
                this.message.removeMessageAttachableListener(this.presentBuilderReactionListener);
                this.message.removeAllReactions().thenAccept(none -> logger.debug("Removed all Reactions from original Present Message."));
                presentBuilders.remove(this.sender);
            }
        }

        return this;
    }
}