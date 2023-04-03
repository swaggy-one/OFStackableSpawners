package me.pebbleprojects.ofstackablespawners.database;

import me.pebbleprojects.ofstackablespawners.OFStackableSpawners;
import me.pebbleprojects.ofstackablespawners.database.objects.StackedSpawner;
import me.pebbleprojects.ofstackablespawners.listeners.InventoryListener;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class Cache {

    public static Map<EntityType, StackedSpawner> defaultSpawners;
    private static Map<Block, StackedSpawner> registeredSpawners;

    public static void loadData() {
        defaultSpawners = new HashMap<>();
        registeredSpawners = new HashMap<>();
    }

    @Nullable
    public static StackedSpawner getSpawner(Block block) {
        return registeredSpawners.get(block);
    }

    public static void registerSpawner(Block block, StackedSpawner spawner) {
        registeredSpawners.put(block, spawner);
    }

    public static void unregisterSpawner(Block block) {
        registeredSpawners.remove(block);
    }

    public static Map<Block, StackedSpawner> getRegisteredSpawners() {
        return registeredSpawners;
    }

    private static int taskID = -1;

    public static void startGenerateTask() {
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(OFStackableSpawners.getInstance(), () -> {
            for (Map.Entry<Block, StackedSpawner> entry : registeredSpawners.entrySet()) {
                entry.getValue().generate();

                // Update the inventories
                for (Map.Entry<Player, StackedSpawner> inventoryEntry : InventoryListener.inventoryManager.entrySet()) {
                    String title = inventoryEntry.getKey().getOpenInventory().getTitle();
                    if (title.equals(OFStackableSpawners.getString("inventory.mainMenu.mainMenuInventoryTitle")))
                        inventoryEntry.getValue().openMainMenu(inventoryEntry.getKey());
                    else if (title.equals(OFStackableSpawners.getString("inventory.storageMenu.storageMenuInventoryTitle")))
                        inventoryEntry.getValue().openStorageMenu(inventoryEntry.getKey());
                }
            }
        }, OFStackableSpawners.generateDelay * 20L, OFStackableSpawners.generateDelay * 20L);
    }

    public static void stopGenerateTask() {
        if (taskID != -1)
            Bukkit.getScheduler().cancelTask(taskID);
    }

}
