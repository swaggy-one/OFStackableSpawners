package me.orbitium.ofstackablespawners.util;

import me.orbitium.ofstackablespawners.OFStackableSpawners;
import me.orbitium.ofstackablespawners.database.objects.SpawnerItem;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.inventory.ItemStack;

public class ItemManager {

    public static ItemStack emptyItem;
    public static ItemStack storageItem;
    public static ItemBuilder stackDisplayItem;
    public static ItemBuilder xpDisplayItem;
    public static ItemBuilder defaultStorageItem;

    public static void load(Configuration config) {
        emptyItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE, " ").build();
        storageItem = new ItemBuilder(
                Material.getMaterial(config.getString("inventory.mainMenu.items.storageDisplayItem.type")),
                config.getString("inventory.mainMenu.items.storageDisplayItem.name"),
                config.getStringList("inventory.mainMenu.items.storageDisplayItem.lore")
        ).build();

        stackDisplayItem = new ItemBuilder(
                Material.getMaterial(config.getString("inventory.mainMenu.items.stackDisplayItem.type")),
                config.getString("inventory.mainMenu.items.stackDisplayItem.name"));

        xpDisplayItem = new ItemBuilder(
                Material.getMaterial(config.getString("inventory.mainMenu.items.xpDisplayItem.type")),
                config.getString("inventory.mainMenu.items.xpDisplayItem.name"));

        defaultStorageItem = new ItemBuilder(
                Material.STONE,
                config.getString("inventory.storageMenu.storageItemFormat.itemDisplayName"),
                config.getStringList("inventory.storageMenu.storageItemFormat.itemLore"));
    }

}
