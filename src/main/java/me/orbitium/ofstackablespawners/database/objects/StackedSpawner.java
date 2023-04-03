package me.orbitium.ofstackablespawners.database.objects;

import me.orbitium.ofstackablespawners.OFStackableSpawners;
import me.orbitium.ofstackablespawners.util.ItemBuilder;
import me.orbitium.ofstackablespawners.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.security.auth.login.Configuration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StackedSpawner implements Cloneable {

    EntityType entityType;
    private int stackedAmount;
    private final int maxStack;
    private List<SpawnerItem> content;
    private int storedXP;
    public StackedSpawner(EntityType entityType, int stackedAmount, int maxStack, List<SpawnerItem> content) {
        this.entityType = entityType;
        this.stackedAmount = stackedAmount;
        this.maxStack = maxStack;
        this.content = content;
        storedXP = 0;
    }

    public boolean merge() {
        if (stackedAmount + 1 > maxStack)
            return false;

        stackedAmount++;
        update();
        return true;
    }

    public int getStackSize() {
        return stackedAmount;
    }

    public int getStoredXP() {
        return storedXP;
    }

    public void setStackSize(int stack) {
        stackedAmount = stack;
        update();
    }

    public void resetXPStorage() {
        storedXP = 0;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void generate() {
        for (SpawnerItem spawnerItem : content)
            spawnerItem.tryToGenerate(stackedAmount);
        storedXP += (OFStackableSpawners.XPGeneration * stackedAmount);
    }

    public List<SpawnerItem> getContent() {
        return content;
    }

    public int getStackedAmount() {
        return stackedAmount;
    }

    public boolean removeSpawnerOnBreak() {
        stackedAmount--;
        update();
        return stackedAmount <= 0;
    }

    public void loadStorageData(Map<Material, Integer> storageData, int storedXP) {
        for (Map.Entry<Material, Integer> entry : storageData.entrySet()) {
            for (SpawnerItem spawnerItem : content) {
                if (spawnerItem.material == entry.getKey())
                    spawnerItem.currentAmount = entry.getValue();
            }
        }
        this.storedXP = storedXP;
    }

    public void update() {
        for (SpawnerItem spawnerItem : content) {
            spawnerItem.updateMaxAmount(stackedAmount);
        }
    }
    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, OFStackableSpawners.getString("inventory.mainMenu.mainMenuInventoryTitle"));
        for (int i = 0; i < 27; i++)
            inv.setItem(i, ItemManager.emptyItem);

        // Build content
        FileConfiguration config = OFStackableSpawners.getInstance().getConfig();
        inv.setItem(config.getInt("inventory.mainMenu.items.storageDisplayItem.slot"), ItemManager.storageItem);

        inv.setItem(config.getInt("inventory.mainMenu.items.stackDisplayItem.slot"), ItemManager.stackDisplayItem.clone()
                .replaceName("%spawner_stack%", stackedAmount + "")
                .replaceName("%spawner_type%", entityType.name())
                .build());

        inv.setItem(config.getInt("inventory.mainMenu.items.xpDisplayItem.slot"), ItemManager.xpDisplayItem.clone()
                .replaceName("%spawner_xp%", storedXP + "")
                .build());

        player.openInventory(inv);
    }

    public void openStorageMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, OFStackableSpawners.getString("inventory.storageMenu.storageMenuInventoryTitle"));
        for (int i = 0; i < 27; i++)
            inv.setItem(i, ItemManager.emptyItem);

        for (SpawnerItem spawnerItem : content) {
            ItemBuilder itemBuilder = ItemManager.defaultStorageItem.clone();
            itemBuilder.setMaterial(spawnerItem.material);
            itemBuilder.replaceName("%item_amount%", spawnerItem.currentAmount + "");
            itemBuilder.replaceName("%max_item_amount%", spawnerItem.maxAmount + "");
            itemBuilder.replaceName("%item_type%", spawnerItem.material.name());

            // Write count data for taking items in GUI
            ItemStack itemStack = itemBuilder.build();
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.getPersistentDataContainer().set(OFStackableSpawners.availableItem, PersistentDataType.INTEGER, spawnerItem.currentAmount);
            itemStack.setItemMeta(itemMeta);

            inv.setItem(spawnerItem.displaySlot, itemStack);
        }

        player.openInventory(inv);
    }

    @Override
    public StackedSpawner clone() {
        try {
            StackedSpawner clone = (StackedSpawner) super.clone();
            List<SpawnerItem> arrayList = new ArrayList<>();
            for (SpawnerItem spawnerItem : content) {
                arrayList.add(spawnerItem.clone());
            }

            clone.content = arrayList;

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
