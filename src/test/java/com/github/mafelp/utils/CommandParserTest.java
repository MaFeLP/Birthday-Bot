package com.github.mafelp.utils;

import com.github.mafelp.utils.exceptions.CommandNotFinishedException;
import com.github.mafelp.utils.exceptions.NoCommandGivenException;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {
    private final Command standard = new Command("!hello", new String[] {"world"});

    @ParameterizedTest
    @ValueSource(strings = {"!hello world", "\"!hello\" \"world\""})
    void repeatedParseFromString(String testCommand) {
        Command output = CommandParser.parseFromString(testCommand);
        assertEquals(output, standard);
    }

    @Test
    void parseFromStringFails() {
        Command output = CommandParser.parseFromString("!hello world");
        assertEquals(output, standard);

        assertThrows(NoCommandGivenException.class, () -> CommandParser.parseFromString(null));
        assertThrows(CommandNotFinishedException.class, () -> CommandParser.parseFromString("\"!hello world"));
    }

    @Test
    void parseFromArray() {
        Command output = CommandParser.parseFromArray(new String[] {"!hello", "world"});
        assertEquals(output, standard);
        output = CommandParser.parseFromArray(new String[] {"!hello", " world"});
        assertNotEquals(output, standard);

        assertThrows(NoCommandGivenException.class, () -> CommandParser.parseFromArray(null));
    }
}