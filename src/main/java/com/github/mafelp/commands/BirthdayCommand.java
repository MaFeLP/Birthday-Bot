package com.github.mafelp.commands;

import com.github.mafelp.Listeners.SkribblListener;
import com.github.mafelp.utils.Command;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.GloballyAttachableListener;

import java.util.Locale;

public class BirthdayCommand extends Thread {
    private static final Logger logger = LogManager.getLogger(BirthdayCommand.class);

    private final MessageCreateEvent messageCreateEvent;
    private final Command command;

    public BirthdayCommand(Command command, MessageCreateEvent messageCreateEvent) {
        this.messageCreateEvent = messageCreateEvent;
        this.command = command;
    }

    @Override
    public void run() {

    }
}