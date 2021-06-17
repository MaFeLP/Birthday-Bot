package com.github.mafelp.commands;

import com.github.mafelp.Listeners.SkribblListener;
import com.github.mafelp.Listeners.SkribblReactionListener;
import com.github.mafelp.utils.Command;
import com.github.mafelp.utils.Configuration;
import com.github.mafelp.Manager.SkribblManager;
import com.github.mafelp.utils.PermissionValidate;
import com.vdurmont.emoji.EmojiParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

/**
 * The class that handles asynchronous execution of the Skribbl command.
 */
public class SkribblCommand extends Thread{
    /**
     * The logger which is used to log statements to the console.
     */
    private static final Logger logger = LogManager.getLogger(SkribblCommand.class);

    /**
     * The number of threads of this kind that were being created.
     */
    private static long threadID = 0;

    /**
     * The Event that is being passed to this class by the discord API.
     */
    private final MessageCreateEvent messageCreateEvent;

    /**
     * The command which was being parsed with the {@link com.github.mafelp.utils.CommandParser} command parser.
     */
    private final Command command;

    /**
     * Default Constructor sets thread names for this thread.
     * @param messageCreateEvent The Event that is being passed to this class by the discord API.
     * @param command The command which was being parsed with the {@link com.github.mafelp.utils.CommandParser} command parser.
     */
    public SkribblCommand(MessageCreateEvent messageCreateEvent, Command command) {
        this.messageCreateEvent = messageCreateEvent;
        this.command = command;

        this.setName("SkribblCommand-" + threadID);
        ++threadID;
    }

    /**
     * The method handles the actual execution of this command.
     */
    @Override
    public void run() {
        logger.debug("Executing command skribbl...");

	// Check if the command has an argument.
        if (command.getStringArgument(0).isEmpty()) {
            logger.debug("Not enough arguments.");
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"game\"; Response: Not enough Arguments");
            return;
        }

	// Check if the command was executed on a server.
        if (messageCreateEvent.getServerTextChannel().isEmpty()) {
            logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"skribbl\"; Response: not a ServerTextChannel.");
            return;
        }

	// Execute different things based on the first argument.
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

                MessageBuilder messageBuilder = new MessageBuilder();

                if (Configuration.getServerConfiguration(messageCreateEvent.getServerTextChannel().get().getServer()).getBoolean("skribbl.sendWordsOnEnd", true)) {
                    File serverConfigurationFolder = Configuration.getServerConfigurationFolder(messageCreateEvent.getServerTextChannel().get().getServer());
                    File skribblFile = new File(serverConfigurationFolder, "skribblWords.txt");

                    messageBuilder.setContent("The " + messageCreateEvent.getApi().getYourself().getMentionTag() + " is now not listening to this channel anymore (" + messageCreateEvent.getServerTextChannel().get().getMentionTag() + ") and added many more awesome words!\n" +
                            "Attached you can also find a list of the words I have collected for you!")
                                .addAttachment(skribblFile);
                } else {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("More Information")
                            .setAuthor(messageCreateEvent.getApi().getYourself())
                            .setDescription("The " + messageCreateEvent.getApi().getYourself().getMentionTag() + " is now not listening to this channel anymore (" + messageCreateEvent.getServerTextChannel().get().getMentionTag() + ") and added many more awesome words!")
                            .addField("How to get the names","Use the command \"" + Configuration.getServerConfiguration(messageCreateEvent.getServerTextChannel().get().getServer()).getString("prefix", "!") + "skribbl get\" to get the words in this channel.")
                            .setColor(new Color(0xFF008C));
                    messageBuilder.setEmbed(embed);
                }
                messageBuilder.send(messageCreateEvent.getServerTextChannel().get())
                        .thenAccept(message -> logger.debug("Stopped Skribbl Listener and sent a message to channel " + message.getChannel().getId()));
            }
            case "get" -> {
                String words = SkribblManager.getSkribblWords(messageCreateEvent.getServerTextChannel().get().getServer()).toString();

                File serverConfigurationFolder = Configuration.getServerConfigurationFolder(messageCreateEvent.getServerTextChannel().get().getServer());
                File skribblFile = new File(serverConfigurationFolder, "skribblWords.txt");

                messageCreateEvent.getServerTextChannel().get().sendMessage("Skribbl word list.", skribblFile);
                logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"skribbl get\"; Response: " + words);
            }
            case "reset", "clear" -> new MessageBuilder()
                    .setEmbed(
                            new EmbedBuilder()
                            .setAuthor(messageCreateEvent.getApi().getYourself())
                            .setTitle("Confirmation required")
                            .setColor(new Color(0x1))
                            .setDescription("Do you really want to reset the list of skribbl words?\n\n" +
                                    "If so, please react to this message with " + EmojiParser.parseToUnicode(":white_check_mark:") + "\n\n" +
                                    "If you want to cancel, please react with " + EmojiParser.parseToUnicode((":negative_squared_cross_mark:")) + "\n")
                    ).send(messageCreateEvent.getChannel()).thenAccept(message -> {
                        message.addReaction(EmojiParser.parseToUnicode(":white_check_mark:"));
                        message.addReaction(EmojiParser.parseToUnicode(":negative_squared_cross_mark:"));
                        message.addReactionAddListener(new SkribblReactionListener());
                    }).thenAccept(none -> logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"skribbl help\"; Response: Sent confirmation embed."));
            case "remove" -> {
                if (!PermissionValidate.authorised(messageCreateEvent.getServerTextChannel().get().getServer(), messageCreateEvent.getMessageAuthor())) {
                    messageCreateEvent.getChannel().sendMessage(
                            new EmbedBuilder()
                            .setAuthor(messageCreateEvent.getMessageAuthor())
                            .setColor(Color.RED)
                            .setTitle("Permission denied!")
                            .setDescription("Sorry, you do not have the required permission to execute this command!")
                    ).thenAccept(message -> logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"skribbl remove\"; Response: not authorised."));
                    messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":no_entry:"));
                    return;
                }

                boolean isFirstArgument = true;
                if (command.getStringArgument(1).isEmpty()) {
                    messageCreateEvent.getChannel().sendMessage(
                            new EmbedBuilder()
                            .setAuthor(messageCreateEvent.getMessageAuthor())
                            .setColor(Color.RED)
                            .setTitle("Error!")
                            .setDescription("You need to provide at least one more argument to this command, so we can remove this word from the list!")
                    ).thenAccept(message -> logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"skribbl help\"; Response: Sent confirmation embed."));
                    return;
                }
                int removedWords = 0;

                List<String> notRemovedWords = new ArrayList<>();
                List<String> removedWordlist = new ArrayList<>();

                for (String word : command.getArguments()) {
                    if (isFirstArgument) {
                        isFirstArgument = false;
                        continue;
                    }

                    if (SkribblManager.removeSkribblWord(messageCreateEvent.getServerTextChannel().get().getServer(), word)) {
                        ++removedWords;
                        removedWordlist.add(word);
                    } else {
                        notRemovedWords.add(word);
                    }
                    logger.debug("Removed Skribbl word " + word + " from server " + messageCreateEvent.getServerTextChannel().get().getServer().getName() + "!");
                }

                logger.debug("Not removed words: " + notRemovedWords);
                logger.debug("Removed words: " + removedWordlist);

                if (removedWords == 0) {
                    messageCreateEvent.getChannel().sendMessage(
                            new EmbedBuilder()
                    ).thenAccept(message -> logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"" + command.getCommand() + " remove\"; Response: No words removed."));
                    messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":x:"));
                    return;
                }

                messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":white_check_mark:"));

                switch (removedWords) {
                    case 1 -> messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":one:"));
                    case 2 -> messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":two:"));
                    case 3 -> messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":three:"));
                    case 4 -> messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":four:"));
                    case 5 -> messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":five:"));
                    case 6 -> messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":six:"));
                    case 7 -> messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":seven:"));
                    case 8 -> messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":eight:"));
                    case 9 -> messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":nine:"));
                    case 10 -> messageCreateEvent.getMessage().addReaction(EmojiParser.parseToUnicode(":ten:"));
                }

                SkribblManager.saveSkribblWords(messageCreateEvent.getServerTextChannel().get().getServer());

                EmbedBuilder embed = new EmbedBuilder()
                        .setAuthor(messageCreateEvent.getApi().getYourself())
                        .setColor(Color.GREEN)
                        .setTitle("Success!")
                        .setDescription("Successfully removed " + removedWords + " words from the skribbl word list!")
                        .setFooter("Use \"" + command.getCommand() + " get\" to get the current list of skribbl words.")
                        ;

                if (!removedWordlist.isEmpty()) {
                    StringBuilder words = new StringBuilder();
                    for (int i = 0; i < removedWordlist.size(); ++i) {
                        words.append(removedWordlist.get(i));
                        if (i != removedWordlist.size() -1) {
                            words.append(',');
                            words.append(' ');
                        }
                    }
                    embed.addField("Removed words:", words.toString());
                }
                if (!notRemovedWords.isEmpty()) {
                    StringBuilder words = new StringBuilder();
                    for (int i = 0; i < notRemovedWords.size(); ++i) {
                        words.append(notRemovedWords.get(i));
                        if (i != notRemovedWords.size() -1) {
                            words.append(',');
                            words.append(' ');
                        }
                    }
                    embed.addField("Not removed words:", words.toString());
                }

                messageCreateEvent.getChannel().sendMessage(embed).thenAccept(message -> logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"" + command.getCommand() + " remove\"; Response: No words removed."));
            }
            case "help" -> {
                logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"skribbl help\"; Response: Help message");
                sendHelpMessage();
            }
            default -> {
                logger.info("User \"" + messageCreateEvent.getMessageAuthor().getName() + "\" executed command \"skribbl\"; Response: Unknown Argument..");
                sendHelpMessage();
            }
        }
    }

    /**
     * The method that sends the help message to the channel in which the command was executed.
     */
    private void sendHelpMessage() {
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
}
