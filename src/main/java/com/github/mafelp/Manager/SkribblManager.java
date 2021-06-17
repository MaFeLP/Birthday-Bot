package com.github.mafelp.Manager;

import com.github.mafelp.utils.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.server.Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * The class that tasks that have to do something with the skribbl words.
 */
public class SkribblManager {
    /**
     * The logging instance to log statements to the console and the log file.
     */
    private static final Logger logger = LogManager.getLogger(SkribblManager.class);

    /**
     * The map with all the skribbl words sorted by a server.
     */
    private static final Map<Server, List<String>> skribblWordMap = new HashMap<>();

    /**
     * The function that gets all the skribbl words from server.
     * @param server The server to get the skribbl words of.
     * @return The skribbl words.
     */
    public static List<String> getSkribblWords(Server server) {
        // If skribbl words are configured, return them
        if (skribblWordMap.get(server) != null) {
            return skribblWordMap.get(server);
        }

        // If no skribbl words are configured for this server, load them in from their file.
        File serverConfigurationFolder = Configuration.getServerConfigurationFolder(server);
        if (serverConfigurationFolder == null) {
            return new ArrayList<>();
        }
        File skribblFile = new File(serverConfigurationFolder, "skribblWords.txt");
        if (skribblFile.exists()) {
            try {
                // Read the words in line by line.
                Scanner fileReader = new Scanner(skribblFile);

                List<String> currentSkribblWords = new ArrayList<>();
                while (fileReader.hasNextLine()) {
                    currentSkribblWords.add(fileReader.nextLine());
                }

                fileReader.close();

                skribblWordMap.put(server, currentSkribblWords);
                return skribblWordMap.get(server);
            } catch (FileNotFoundException e) {
                logger.error("File " + skribblFile.getAbsolutePath() + " not found!", e);
                return new ArrayList<>();
            }
        } else {
            try {
                skribblFile.createNewFile();
                skribblWordMap.put(server, new ArrayList<>());
                return skribblWordMap.get(server);
            } catch (IOException e) {
                logger.error("Could not create File " + skribblFile.getAbsolutePath() + "!", e);
                return new ArrayList<>();
            }
        }
    }

    /**
     * Saves the skribbl words for a specific server.
     * @param server The server to save the skribbl words of.
     */
    public static void saveSkribblWords(Server server) {
        List<String> wordsToSave = getSkribblWords(server);

        File serverConfigurationFolder = Configuration.getServerConfigurationFolder(server);
        File skribblFile = new File(serverConfigurationFolder, "skribblWords.txt");
        try {
            PrintStream writer = new PrintStream(skribblFile);
            logger.info("Writing skribbl words from Server " + server.getName() + " to file: " + skribblFile.getAbsolutePath());

            for (String word : wordsToSave) {
                writer.println(word);
                logger.debug("Added word " + word + " to the list of skribbl words for Server " + server.getName());
            }
        } catch (FileNotFoundException e) {
            logger.error("Could not find the skribbl file " + skribblFile.getAbsolutePath() + "...");
            logger.debug("Stack trace: ", e);
        }
    }

    /**
     * Added a new skribbl word to a server.
     * @param server The server to add the word to.
     * @param word the word you want to add.
     */
    public static void addSkribblWord(Server server, String word) {
        if (!(word.endsWith(",")))
            word += ",";

        List<String> words = getSkribblWords(server);

        // Check if the word already exists. If not, add it.
        boolean alreadyAdded = false;
        for (String s : words) {
            if (s.equalsIgnoreCase(word)) {
                alreadyAdded = true;
                break;
            }
        }
        if (!alreadyAdded) {
            words.add(word);
            skribblWordMap.replace(server, words);
        }
    }

    /**
     * Removes a word from the list of skribbl words.
     * @param server The server to remove the word from.
     * @param word The word you want to be removed.
     * @return If the word was removed. If false, that means that the word didn't exist in the first place.
     */
    public static boolean removeSkribblWord(Server server, String word) {
        List<String> words = getSkribblWords(server);

        if (!(word.endsWith(",")))
            word += ",";

        int i = 0;
        for (String s : words) {
            if (s.equalsIgnoreCase(word)) {
                words.remove(i);

                skribblWordMap.replace(server, words);
                return true;
            }

             ++i;
        }
        return false;
    }

    /**
     * Resets the skribbl words for a server.
     * @param server The server you want to reset the words off.
     */
    public static void resetSkribblWords(Server server) {
        List<String> oldWords = getSkribblWords(server);

        logger.debug("Old Skribbl words were: " + oldWords);

        skribblWordMap.put(server, new ArrayList<>());
        logger.debug("Saving the new words to the file.");
        saveSkribblWords(server);
    }
}
