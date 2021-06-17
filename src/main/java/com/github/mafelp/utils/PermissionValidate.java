package com.github.mafelp.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.List;

/**
 * The class that checks if a user is authorised in the configuration.
 */
public class PermissionValidate {
    /**
     * Checks if a message author is bot owner or server admin or is authorised
     * in the configuration file ({@link Configuration#getServerConfiguration(Server)}).
     * @param server The server to check the permission on.
     * @param user The {@link MessageAuthor} to check th permissions of.
     * @return If the user has the requested permissions.
     */
    public static boolean authorised(Server server, MessageAuthor user) {
        if (user.isBotOwner())
            return true;
        if (user.isServerAdmin())
            return true;
        if (user.asUser().isEmpty())
            return false;
        return serverPermission(server, user.asUser().get());
    }

    /**
     * Checks if the given user's id is authorised in the server's configuration file.
     * @param server The server on which to check the configuration.
     * @param user The user to check the permission of.
     * @return If the user is authorised.
     */
    public static boolean serverPermission(Server server, User user) {
        YamlConfiguration yamlConfiguration = Configuration.getServerConfiguration(server);
        List<Long> allowedUserIDs = yamlConfiguration.getLongList("authorizedUserIDs");

        for (long id : allowedUserIDs) {
            if (id == user.getId())
                return true;
        }

        return false;
    }
}
