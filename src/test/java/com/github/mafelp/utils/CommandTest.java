package com.github.mafelp.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest {

    static Stream<Command> argumentMaker() {
        return Stream.of(
                new Command("Hallo", new String[]{"1234", "true", "geht?"}),
                new Command("test", new String[]{"18237349", "false", "!@#$^&?"})
        );
    }

    static int runthrough = 0;

    @ParameterizedTest
    @MethodSource("argumentMaker")
    void getStringArgument(Command command) {
        assertEquals(command.getStringArgument(-1), Optional.empty());
        assertTrue(command.getStringArgument(0).isPresent());
        assertTrue(command.getStringArgument(1).isPresent());
        assertTrue(command.getStringArgument(2).isPresent());
        assertFalse(command.getStringArgument(3).isPresent());
        assertEquals(command.getStringArgument(4), Optional.empty());

        switch (runthrough) {
            case 0 -> {
                assertEquals("1234", command.getStringArgument(0).get());
                assertEquals("true", command.getStringArgument(1).get());
                assertEquals("geht?", command.getStringArgument(2).get());
            }
            case 1 -> {
                assertEquals("18237349", command.getStringArgument(0).get());
                assertEquals("false", command.getStringArgument(1).get());
                assertEquals("!@#$^&?", command.getStringArgument(2).get());

                runthrough = -1;
            }
        }

        ++runthrough;
    }

    @ParameterizedTest
    @MethodSource("argumentMaker")
    void getBooleanArgument(Command command) {
        assertFalse(command.getBooleanArgument(0).isPresent());
        assertTrue(command.getBooleanArgument(1).isPresent());
        assertInstanceOf(Boolean.class, command.getBooleanArgument(1).get());
        assertFalse(command.getBooleanArgument(2).isPresent());

        switch (runthrough) {
            case 0 -> assertTrue(command.getBooleanArgument(1).get());
            case 1 -> {
                assertFalse(command.getBooleanArgument(1).get());

                runthrough = -1;
            }
        }

        ++runthrough;
    }

    @Test
    void getLongArgument() {
    }

    @Test
    void testEquals() {
    }

    @Test
    void testToString() {
    }

    @Test
    void testToStringWithQuotationMarks() {
    }
}