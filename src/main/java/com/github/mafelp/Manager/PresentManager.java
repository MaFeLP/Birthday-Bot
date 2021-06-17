package com.github.mafelp.Manager;

import com.github.mafelp.Main;
import com.github.mafelp.utils.Configuration;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletionException;

/**
 * The class that handles tasks that have something to do with Presents.
 */
public class PresentManager {
    /**
     * The logging instance to log statements to the console and the log file.
     */
    private static final Logger logger = LogManager.getLogger(PresentManager.class);

    /**
     * The Map of presents for a specific server.
     */
    private static final Map<Server, JsonArray> presentsMap = new HashMap<>();

    /**
     * Gets a presents file for a specific server.
     * @param server The server to get the presents file of.
     * @return The presents file.
     */
    public static File getPresentsFile(Server server) {
        File presentFile = new File(Configuration.getServerConfigurationFolder(server), "presents.json");

        // If the file exists, return it.
        if (presentFile.exists())
            return presentFile;
        // If the file foes not exist, create one and write "[]" as default to it.
        else {
            try {
                presentFile.createNewFile();

                PrintStream printStream = new PrintStream(presentFile);
                printStream.println("[]");

                printStream.close();

                return presentFile;
            } catch (IOException e) {
                logger.error("Error creating presents file for server \"" + server.getName() + "\" with id \"" + server.getIdAsString() + "\"!");
                logger.debug("Stack Trace: ", e);
                return null;
            }
        }
    }

    /**
     * The method to a add a present to a server.
     * @param server The server to add the present to.
     * @param present The present you want to add.
     */
    public static void addPresent(Server server, JsonObject present) {
        JsonArray presents = presentsMap.get(server);
        if (presents == null) {
            presents = new JsonArray();
            presents.add(present);
            presentsMap.put(server, presents);
        } else {
            presents.add(present);
            presentsMap.replace(server, presents);
        }
        logger.debug("Updated the presents for server " + server.getName() + ": Added present: " + present.toString());
    }

    /**
     * The getter for all presents, that can be unpacked by a user.
     * @param server The server you want to get all the presents of.
     * @param user The user of whom you want to get the presents.
     * @return The list of presents the user has.
     */
    public static List<JsonObject> getPresents(Server server, User user) {
        JsonArray defaultArray = new JsonArray();

        JsonArray presents = presentsMap.getOrDefault(server, defaultArray);

        // Checks if the user has any presents.
        if (presents.equals(defaultArray)) {
            return null;
        }

        // Adds all the presents to a list.
        List<JsonObject> out = new ArrayList<>();
        for (JsonElement element : presents) {
            JsonObject jsonObject = element.getAsJsonObject();

            if (jsonObject.get("receiver").getAsLong() == user.getId()) {
                out.add(jsonObject);
            }
        }

        // If no presents were found, return null.
        if (out.size() == 0)
            return null;

        // return the presents.
        return out;
    }

    /**
     * A function which creates an embed out of a JSON Present.
     * @param present The JSON Object to build the present from.
     * @return The embed which is ready to send.
     */
    public static EmbedBuilder buildPresent(JsonObject present) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(new Color(0xe684b2))
                // Add required parameters to the present.
                .setTitle(present.get("title").getAsString())
                .setDescription(present.get("content").getAsString())
                ;

        try {
            // Add required parameters to the present.
            User author = Main.discordApi.getUserById(present.get("sender").getAsLong()).join();
            embedBuilder.setAuthor(author);
            logger.debug("Adding author to present: " + author.getName());
        } catch (CompletionException e) {
            // If no user with this id could be found, add the bot.
            embedBuilder.setAuthor(Main.discordApi.getYourself());
            logger.warn("Could not get User with ID " + present.get("sender").getAsLong() + " whilst trying to build a present. Using Bot Instead.");
            logger.debug("Adding author to present: Bot");
        }

        // Add optional parameters to the present.
        if (present.get("imageURL") != null && !present.get("imageURL").getAsString().isEmpty() && !present.get("imageURL").getAsString().isBlank()) {
            logger.debug("Adding imageURL to present: " + present.get("imageURL").getAsString());
            embedBuilder.setImage(present.get("imageURL").getAsString());
        } else {
            logger.debug("No imageURL found in present. Not adding one.");
        }

        return embedBuilder;
    }

    /**
     * Saves the presents from a server to its corresponding file.
     * @param server The server to save the presents of.
     */
    public static void savePresents(Server server ) {
        File presentsFile = getPresentsFile(server);
        // If no presents file could be created.
        if (presentsFile == null) {
            logger.debug("Skipping Server " + server.getName() + "...");
            logger.error("Could not save presents for server " + server.getName() + ": Could not get the file to save to!");
            return;
        }

        try {
            PrintStream printStream = new PrintStream(presentsFile);
            printStream.println(presentsMap.get(server).toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * A method that saves <b>all Presents</b> from <b>all servers</b>.
     */
    public static void savePresents() {
        for (Server server : presentsMap.keySet()) {
            savePresents(server);
        }
    }

    /**
     * Loads all the presents from all the servers in.
     */
    public static void loadPresents() {
        for (Server server : Main.discordApi.getServers()) {
            File presentsFile = getPresentsFile(server);
            if (presentsFile == null) {
                logger.debug("Skipping Server " + server.getName() + "...");
                logger.error("Could not load presents for server " + server.getName() + ": File does not exist and is not writable!");
                continue;
            }

            // Read all the lines of the file in.
            StringBuilder presentsFileContentsBuilder = new StringBuilder();
            try {
                Scanner scanner = new Scanner(presentsFile);

                while (scanner.hasNextLine()) {
                    presentsFileContentsBuilder.append(scanner.nextLine());
                }

                scanner.close();
            } catch (FileNotFoundException e) {
                logger.error("Could not load presents for server " + server.getName() + ": File does not exist and is not readable!");
                logger.debug("Stack Trace: ", e);
                continue;
            }
            String presentsFileContents = presentsFileContentsBuilder.toString();

            // If no presents exist for this server, add an empty array.
            if (presentsFileContents.equalsIgnoreCase("[]") || presentsFileContents.equalsIgnoreCase("[]\n")) {
                presentsMap.put(server, new JsonArray());
            } else {
                JsonParser jsonParser = new JsonParser();
                JsonArray serverPresents = jsonParser.parse(presentsFileContents).getAsJsonArray();
                presentsMap.put(server, serverPresents);
            }
        }
    }
}
