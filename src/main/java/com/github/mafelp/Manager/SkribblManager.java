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

public class SkribblManager {
    private static final Logger logger = LogManager.getLogger(SkribblManager.class);

    private static final Map<Server, List<String>> skribblWordMap = new HashMap<>();

    private static final File globalSkribblFile = new File(Configuration.configurationFilesFolder, "defaultSkribblWords.txt");

    public static List<String> getSkribblWords(Server server) {
        if (skribblWordMap.get(server) != null) {
            return skribblWordMap.get(server);
        }

        File serverConfigurationFolder = Configuration.getServerConfigurationFolder(server);
        if (serverConfigurationFolder == null) {
            return new ArrayList<>();
        }
        File skribblFile = new File(serverConfigurationFolder, "skribblWords.txt");
        if (skribblFile.exists()) {
            try {
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

    public static void addSkribblWord(Server server, String word) {
        if (!(word.endsWith(",")))
            word += ",";

        List<String> words = getSkribblWords(server);

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

    public static List<String> removeSkribblWord(Server server, String word) {
        List<String> words = getSkribblWords(server);

        if (!(word.endsWith(",")))
            word += ",";

        boolean listChanged = false;

        int i = 0;
        for (String s : words) {
            if (s.equalsIgnoreCase(word)) {
                words.remove(i);

                listChanged = true;
                break;
            }

             ++i;
        }

        if (listChanged) {
            skribblWordMap.replace(server, words);
        }
        return skribblWordMap.get(server);
    }

    public static void resetSkribblWords(Server server) {
        var oldWords = getSkribblWords(server);

        logger.debug("Old Skribbl words were: " + oldWords);

        skribblWordMap.put(server, new ArrayList<>());
        logger.debug("Saving the new words to the file.");
        saveSkribblWords(server);
    }
}
