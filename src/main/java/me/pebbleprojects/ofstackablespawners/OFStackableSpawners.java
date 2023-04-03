package me.pebbleprojects.ofstackablespawners;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Worth;
import me.pebbleprojects.ofstackablespawners.commands.SSCommand;
import me.pebbleprojects.ofstackablespawners.database.Cache;
import me.pebbleprojects.ofstackablespawners.database.Database;
import me.pebbleprojects.ofstackablespawners.listeners.BlockListener;
import me.pebbleprojects.ofstackablespawners.listeners.InventoryListener;
import me.pebbleprojects.ofstackablespawners.listeners.PlayerListener;
import me.pebbleprojects.ofstackablespawners.util.ItemManager;
import me.pebbleprojects.ofstackablespawners.util.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Random;

public final class OFStackableSpawners extends JavaPlugin {

    public static int generateDelay;
    public static NamespacedKey availableItem;
    public static int XPGeneration;
    public static String mainMenuTitle;
    public static String storageMenuTitle;
    public static Random random = new Random();

    private static OFStackableSpawners instance;
    public static Essentials ess;

    public static int minPlayTime;

    public static Worth worth;

    private static boolean running = false;
    @Override
    public void onEnable() {
        // Plugin startup logic
        availableItem = new NamespacedKey(this, "counter");
        saveDefaultConfig();
        instance = this;
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        load();
        running = true;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this,
                this::save,
                5 * 60 * 20, 5 * 60 * 20
        );
        Cache.startGenerateTask();

        SSCommand command = new SSCommand();
        Objects.requireNonNull(getCommand("stackableSpawners")).setExecutor(command);
        Objects.requireNonNull(getCommand("stackableSpawners")).setTabCompleter(command);
    }

    public static void load() {
        if (running)
            Database.saveRegisteredSpawners();
        instance.reloadConfig();
        generateDelay = instance.getConfig().getInt("generateDelay");
        XPGeneration = instance.getConfig().getInt("xpGeneration");
        mainMenuTitle = getString("inventory.mainMenu.mainMenuInventoryTitle");
        storageMenuTitle = getString("inventory.storageMenu.storageMenuInventoryTitle");
        minPlayTime = getInt("minPlayTime") * 60;
        Cache.loadData();
        ItemManager.load(instance.getConfig());
        MessageManager.setConfiguration(instance.getConfig());
        Database.loadDefaultSpawners();
        Database.loadRegisteredSpawners();
        ess = (Essentials) instance.getServer().getPluginManager().getPlugin("Essentials");
        assert ess != null;
        worth = ess.getWorth();
    }

    private void save() {
        Bukkit.getScheduler().runTaskAsynchronously(instance, Database::saveRegisteredSpawners);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        Database.saveRegisteredSpawners();
    }

    public static OFStackableSpawners getInstance() {
        return instance;
    }

    public static String getString(String path) {
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(instance.getConfig().getString(path)));
    }

    public static int getInt(String path) {
        return instance.getConfig().getInt(path);
    }
}
