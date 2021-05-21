package com.github.mafelp.commands;

import com.github.mafelp.Listeners.SkribblListener;
import com.github.mafelp.utils.Command;
import com.github.mafelp.utils.Configuration;
import com.github.mafelp.utils.SkribblManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.io.File;
import java.util.Locale;

public class SkribblCommand extends Thread{
    private static long threadID = 0;

    private final MessageCreateEvent messageCreateEvent;
    private final Command command;

    private static final Logger logger = LogManager.getLogger(com.github.mafelp.commands.GameCommand.class);

    public SkribblCommand(MessageCreateEvent messageCreateEvent, Command command) {
        this.messageCreateEvent = messageCreateEvent;
        this.command = command;

        this.setName("SkribblCommand-" + threadID);
        ++threadID;
    }

    @Override
    public void run() {
        logger.debug("Executing command skribbl...");

        if (command.getStringArgument(0).isEmpty()) {
            logger.debug("Not enough arguments.");
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"game\"; Response: Not enough Arguments");
            return;
        }

        if (messageCreateEvent.getServerTextChannel().isEmpty()) {
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"skribbl\"; Response: not a ServerTextChannel.");
            return;
        }

        switch (command.getStringArgument(0).get().toLowerCase(Locale.ROOT)) {
            case "start" -> {
                SkribblListener.addListeningChannel(messageCreateEvent.getServerTextChannel().get());
                logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"skribbl start\"; Response: Added Skribbl Listener in channel #" + messageCreateEvent.getServerTextChannel().get().getName());

                File serverConfigurationFolder = Configuration.getServerConfigurationFolder(messageCreateEvent.getServerTextChannel().get().getServer());
                File skribblFile = new File(serverConfigurationFolder, "skribblWords.txt");

                EmbedBuilder embed = new EmbedBuilder()
                                .setTitle("More Information")
                                .setAuthor(messageCreateEvent.getApi().getYourself())
                                .setDescription("The " + messageCreateEvent.getApi().getYourself().getMentionTag() + " is now listening to this channel (" + messageCreateEvent.getServerTextChannel().get().getMentionTag() + ") and add all words, that not start with \"" + Configuration.getServerConfiguration(messageCreateEvent.getServerTextChannel().get().getServer()).getString("prefix", "!") + "\" to the list of skribbl words.")
                                .addField("How to stop it","Use the command \"" + Configuration.getServerConfiguration(messageCreateEvent.getServerTextChannel().get().getServer()).getString("prefix", "!") + "skribbl stop\" to stop the listener of words in this channel.")
                                .setColor(new Color(0x05fcc2));
                new MessageBuilder()
                        .setContent("Here is the list of currently added words:")
                        .addAttachment(skribblFile)
                        .setEmbed(embed)
                        .send(messageCreateEvent.getServerTextChannel().get())
                        .thenAccept(message -> logger.debug("Starting Skribbl Listener message sent to channel "));
            }
            case "stop" -> {
                SkribblListener.removeListeningChannel(messageCreateEvent.getServerTextChannel().get());
                logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"skribbl stop\"; Response: Stopped Skribbl Listener in channel #" + messageCreateEvent.getServerTextChannel().get().getName());
                SkribblManager.saveSkribblWords(messageCreateEvent.getServerTextChannel().get().getServer());

                // File serverConfigurationFolder = Configuration.getServerConfigurationFolder(messageCreateEvent.getServerTextChannel().get().getServer());
                // File skribblFile = new File(serverConfigurationFolder, "skribblWords.txt");

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("More Information")
                        .setAuthor(messageCreateEvent.getApi().getYourself())
                        .setDescription("The " + messageCreateEvent.getApi().getYourself().getMentionTag() + " is now listening to this channel (" + messageCreateEvent.getServerTextChannel().get().getMentionTag() + ") and added many more awesome words!")
                        .addField("How to get the names","Use the command \"" + Configuration.getServerConfiguration(messageCreateEvent.getServerTextChannel().get().getServer()).getString("prefix", "!") + "skribbl get\" to get the words in this channel.")
                        .setColor(new Color(0xFF008C));
                new MessageBuilder()
                        //.setContent("Here is the list of currently added words:")
                        //.addAttachment(skribblFile)
                        .setEmbed(embed)
                        .send(messageCreateEvent.getServerTextChannel().get())
                        .thenAccept(message -> logger.debug("Stopped Skribbl Listener message sent to channel "));

                //TODO add sending of words with config toggle.
            }
            case "get" -> {
                File serverConfigurationFolder = Configuration.getServerConfigurationFolder(messageCreateEvent.getServerTextChannel().get().getServer());
                File skribblFile = new File(serverConfigurationFolder, "skribblWords.txt");

                messageCreateEvent.getChannel().sendMessage("Skribbl word list.", skribblFile);

                String words = SkribblManager.getSkribblWords(messageCreateEvent.getServerTextChannel().get().getServer()).toString();
                logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"skribbl get\"; Response: " + words);
            }
            case "reset", "clear" -> {
                messageCreateEvent.getChannel().sendMessage(
                        new EmbedBuilder()
                        .setAuthor(messageCreateEvent.getMessageAuthor())
                        .setTitle("Error!")
                        .setColor(Color.RED)
                        .setDescription("This command is currently not available! Check for updates here: https://github.com/MaFeLP/Birthday-Bot/releases/latest/")
                );
            }
            case "help" -> {
                logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"skribbl help\"; Response: Help message");

                messageCreateEvent.getChannel().sendMessage(
                        new EmbedBuilder()
                        .setTitle("Help message")
                        .setAuthor(messageCreateEvent.getApi().getYourself())
                        .setColor(new Color(0xFFB500))
                        .setDescription("The Skribbl command enables you to collect a list of words with your fellow server mates in a channel.")
                        .addInlineField("start","Start listening into a channel and appends them to a list of words.")
                        .addInlineField("stop","Stops recording and adding more words to the list.")
                        .addInlineField("get", "Outputs the list of words.")
                        .addInlineField("reset/clear", "Clears the list of words, currently in the skribbl List.")
                        .addInlineField("help","Prints this message")
                ).thenAccept(message -> logger.debug("Help message sent to channel with ID " + message.getChannel().getId()));
            }
            default -> {
                logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"skribbl\"; Response: Unknown Argument..");
            }
        }
    }
}
