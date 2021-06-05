package com.github.mafelp.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.List;

public class PermissionValidate {
    public static boolean authorised(Server server, MessageAuthor user) {
        if (user.isBotOwner())
            return true;
        if (user.isServerAdmin())
            return true;
        if (user.asUser().isEmpty())
            return false;
        return serverPermission(server, user.asUser().get());
    }

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
