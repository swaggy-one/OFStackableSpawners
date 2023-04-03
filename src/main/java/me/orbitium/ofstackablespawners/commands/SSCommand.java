package me.orbitium.ofstackablespawners.commands;

import me.orbitium.ofstackablespawners.OFStackableSpawners;
import me.orbitium.ofstackablespawners.util.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SSCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp())
            return true;

        if (args[0].equals("reload") && args.length == 1) {
            OFStackableSpawners.load();
            sender.sendMessage(ChatColor.GREEN + "OFStackableSpawner reloaded!");
        }

        if (args.length > 2) {
            Player player = Bukkit.getPlayer(args[1]);
            EntityType entityType;
            try {
                entityType = EntityType.valueOf(args[2].toUpperCase());
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Wrong entity type!");
                return true;
            }

            if (player == null) {
                MessageManager.sendMessage(sender, "messages.error.playerIsNotOnline");
                return true;
            }

            int amount = 1;
            if (args.length == 4)
                amount = Integer.parseInt(args[3]);

            ItemStack drop = new ItemStack(Material.SPAWNER, amount);
            BlockStateMeta bsm = (BlockStateMeta) drop.getItemMeta();
            CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();
            cs.setSpawnedType(entityType);
            bsm.setBlockState(cs);
            drop.setItemMeta(bsm);
            player.getInventory().addItem(drop);
            MessageManager.sendMessage(sender, "messages.spawnerGiven");
            MessageManager.sendMessage(player, "messages.youReceivedSpawner");
        }

        return true;
    }

    List<String> emptyList = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp())
            return emptyList;

        if (args.length == 1)
            return Arrays.asList("give", "reload");
        else if (args.length == 2)
            return null;
        else if (args.length == 3) {
            List<String> entityTypes = new ArrayList<>();
            for (EntityType value : EntityType.values()) {
                entityTypes.add(value.name());
            }
            return entityTypes;
        } else if (args.length == 4) {
            return Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
        }
        return emptyList;
    }
}
