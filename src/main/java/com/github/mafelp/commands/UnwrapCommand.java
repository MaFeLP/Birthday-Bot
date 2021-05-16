package com.github.mafelp.commands;

import com.github.mafelp.utils.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.concurrent.ExecutionException;

public class UnwrapCommand extends Thread {
    private final MessageCreateEvent messageCreateEvent;
    private final String prefix;
    private final Command command;

    private static final Logger logger = LogManager.getLogger(UnwrapCommand.class);

    public UnwrapCommand(MessageCreateEvent messageCreateEvent, Command command, String prefix) {
        this.messageCreateEvent = messageCreateEvent;
        this.prefix = prefix;
        this.command = command;
    }

    @Override
    public void run() {
        if (command.getStringArgument(0).isPresent()) {
            switch (command.getStringArgument(0).get()) {
                case "<@!459019618686730271>" -> {
                    EmbedBuilder reply = new EmbedBuilder()
                            .setImage("https://i.otto.de/i/otto/5a519a15-6d3f-5ffa-bc70-0a968a744b23")
                            .setColor(new Color(0xff2bce))
                            .addField("Your Present!", "Dein Geschenk von <@459019618686730271> ist ein Flaschenhalter für dein Fahrrad!")
                            ;

                    // Sets MaFeLP as the message author
                    try {
                        reply.setAuthor(messageCreateEvent.getApi().getUserById(459019618686730271L).get());
                    } catch (InterruptedException | ExecutionException e) {
                        reply.setAuthor("Max");

                        logger.error("Something went wrong getting the user by id: 459019618686730271!");
                        logger.debug("Stack Trace: ", e);
                    }

                    messageCreateEvent.getChannel().sendMessage(reply);
                }
                default -> {
                    messageCreateEvent.getChannel().sendMessage(
                            new EmbedBuilder()
                    );

                    logger.info("User \"" + messageCreateEvent.getMessageAuthor().getDisplayName() + "\" passed not enough arguments to the command \"" + prefix + "unwrap\"!");
                }
            }
        } else {
            messageCreateEvent.getChannel().sendMessage(
                    new EmbedBuilder()
                            .setColor(Color.RED.darker())
                            .setAuthor(messageCreateEvent.getMessageAuthor())
                            .setTitle("Error!")
                            .addField("NotEnoughArgumentsError","Usage: " + prefix + "unwrap <@User to unwrap the present of>")
                            .setFooter("")
            );
        }
    }
}
