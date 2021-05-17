package com.github.mafelp.commands;

import com.github.mafelp.utils.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

public class UnwrapCommand extends Thread {
    private static long threadID = 0;

    private final MessageCreateEvent messageCreateEvent;
    private final String prefix;
    private final Command command;

    private static final Logger logger = LogManager.getLogger(UnwrapCommand.class);

    public UnwrapCommand(MessageCreateEvent messageCreateEvent, Command command, String prefix) {
        this.messageCreateEvent = messageCreateEvent;
        this.prefix = prefix;
        this.command = command;

        this.setName("UnwrapCommand-" + threadID);
        ++threadID;
    }

    @Override
    public void run() {
        messageCreateEvent.getChannel().sendMessage(
                new EmbedBuilder()
                .setAuthor(messageCreateEvent.getMessageAuthor())
                .setTitle("Error!")
                .setDescription("No Presents are configured for this birthday!")
                .setFooter("Upgrade to version 1.4 or higher for present support.")
        );

        logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"unwrap\"; Response: Command not available.");
    }
}
