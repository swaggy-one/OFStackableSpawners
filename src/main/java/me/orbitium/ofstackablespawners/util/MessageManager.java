package me.orbitium.ofstackablespawners.util;

import me.orbitium.ofstackablespawners.OFStackableSpawners;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

public class MessageManager {
    private static Configuration config;

    public static void setConfiguration(Configuration configuration) {
        config = configuration;
    }

    public static void sendMessage(Player player, String messagePath) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString(messagePath)));
    }

    public static void sendMessage(Player player, String messagePath, String target, String replace) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString(messagePath)
                .replace(target, replace)));
    }

    public static void sendMessage(CommandSender commandSender, String messagePath) {
        commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString(messagePath)));
    }
}
