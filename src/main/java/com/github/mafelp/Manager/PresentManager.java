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

public class PresentManager {
    private static final Logger logger = LogManager.getLogger(PresentManager.class);

    private static final Map<Server, JsonArray> presentsMap = new HashMap<>();

    public static File getPresentsFile(Server server) {
        File presentFile = new File(Configuration.getServerConfigurationFolder(server), "presents.json");

        if (presentFile.exists())
            return presentFile;
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

    public static List<JsonObject> getPresents(Server server, User user) {
        String receiverTag = user.getMentionTag();
        StringBuilder receiverBuilder = new StringBuilder();

        for (char c : receiverTag.toCharArray()) {
            if (c != '!')
                receiverBuilder.append(c);
        }
        receiverTag = receiverBuilder.toString();

        JsonArray defaultArray = new JsonArray();

        JsonArray presents = presentsMap.getOrDefault(server, defaultArray);

        if (presents.equals(defaultArray)) {
            return null;
        }

        List<JsonObject> out = new ArrayList<>();
        for (JsonElement element : presents) {
            JsonObject jsonObject = element.getAsJsonObject();

            if (jsonObject.get("receiver").getAsString().equals(receiverTag)) {
                out.add(jsonObject);
            }
        }

        if (out.size() == 0)
            return null;

        return out;
    }

    public static EmbedBuilder buildPresent(JsonObject present) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(new Color(0xe684b2))
                .setTitle(present.get("title").getAsString())
                .setDescription(present.get("content").getAsString())
                ;

        User author = Main.discordApi.getUserById(present.get("authorID").getAsLong()).join();

        if (author != null) {
            embedBuilder.setAuthor(author);
            logger.debug("Adding author to present: " + author.getName());
        } else {
            embedBuilder.setAuthor(Main.discordApi.getYourself());
            logger.warn("Could not get User with ID " + present.get("authorID").getAsLong() + " whilst trying to build a present. Using Bot Instead.");
            logger.debug("Adding author to present: Bot");
        }

        if (!present.get("imageURL").getAsString().isEmpty() && !present.get("imageURL").getAsString().isBlank()) {
            logger.debug("Adding imageURL to present: " + present.get("imageURL").getAsString());
            embedBuilder.setImage(present.get("imageURL").getAsString());
        } else {
            logger.debug("No imageURL found in present. Not adding one.");
        }

        return embedBuilder;
    }

    public static void savePresents() {
        for (Server server : presentsMap.keySet()) {
            File presentsFile = getPresentsFile(server);
            if (presentsFile == null) {
                logger.debug("Skipping Server " + server.getName() + "...");
                continue;
            }

            try {
                PrintStream printStream = new PrintStream(presentsFile);
                printStream.println(presentsMap.get(server).toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadPresents() {
        for (Server server : Main.discordApi.getServers()) {
            File presentsFile = getPresentsFile(server);
            if (presentsFile == null) {
                logger.debug("Skipping Server " + server.getName() + "...");
                continue;
            }

            StringBuilder presentsFileContentsBuilder = new StringBuilder();
            try {
                Scanner scanner = new Scanner(presentsFile);

                while (scanner.hasNextLine()) {
                    presentsFileContentsBuilder.append(scanner.nextLine());
                }

                scanner.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                continue;
            }
            String presentsFileContents = presentsFileContentsBuilder.toString();

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
