package me.pebbleprojects.ofstackablespawners.listeners;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import me.pebbleprojects.ofstackablespawners.OFStackableSpawners;
import me.pebbleprojects.ofstackablespawners.database.objects.SpawnerItem;
import me.pebbleprojects.ofstackablespawners.database.objects.StackedSpawner;
import me.pebbleprojects.ofstackablespawners.util.MessageManager;
import net.ess3.api.MaxMoneyException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class InventoryListener implements Listener {

    public static Map<Player, StackedSpawner> inventoryManager = new HashMap<>();
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER)
            return;

        String title = event.getView().getTitle();

        if (title.equals(OFStackableSpawners.getString("inventory.mainMenu.mainMenuInventoryTitle"))) {
            event.setCancelled(true);

            int clickedSlot = event.getSlot();

            int storageSlot = OFStackableSpawners.getInt("inventory.mainMenu.items.storageDisplayItem.slot");
            int xpSlot = OFStackableSpawners.getInt("inventory.mainMenu.items.xpDisplayItem.slot");

            Player player = (Player) event.getWhoClicked();
            if (clickedSlot == storageSlot) {
                if (event.getClick() == ClickType.RIGHT) {
                    StackedSpawner spawner = inventoryManager.get(player);
                    double totalWorth = 0.0;
                    for (SpawnerItem spawnerItem : spawner.getContent()) {
                        BigDecimal price = OFStackableSpawners.worth.getPrice(OFStackableSpawners.ess, spawnerItem.itemStack);
                        if (price != null) {
                            totalWorth += price.doubleValue() * spawnerItem.currentAmount;
                            spawnerItem.currentAmount = 0;
                        }
                    }

                    if (totalWorth <= 0.0) {
                        MessageManager.sendMessage(player, "messages.error.noItemToSell");
                        return;
                    }

                    try {
                        Economy.add(player.getUniqueId(), new BigDecimal(totalWorth));
                    } catch (NoLoanPermittedException | UserDoesNotExistException | MaxMoneyException e) {
                        throw new RuntimeException(e);
                    }

                    MessageManager.sendMessage(player, "messages.itemsSold", "%worth%", totalWorth + "");
                } else
                    inventoryManager.get(player).openStorageMenu(player);
            }

            if (clickedSlot == xpSlot) {
                StackedSpawner spawner = inventoryManager.get(player);
                int xp = spawner.getStoredXP();
                if (xp > 0) {
                    player.giveExp(xp);
                    spawner.resetXPStorage();
                    MessageManager.sendMessage(player, "messages.xpTaken", "%amount%", xp + "");
                    spawner.openMainMenu(player);
                } else {
                    MessageManager.sendMessage(player, "messages.error.notEnoughXP");
                }
            }
        } else if (title.equals(OFStackableSpawners.getString("inventory.storageMenu.storageMenuInventoryTitle"))) {
            event.setCancelled(true);
            ItemStack itemStack = event.getCurrentItem();
            if (itemStack == null || itemStack.getType() == Material.GRAY_STAINED_GLASS_PANE)
                return;

            if (!itemStack.hasItemMeta())
                return;

            Player player = (Player) event.getWhoClicked();

            int count = itemStack.getItemMeta().getPersistentDataContainer().get(OFStackableSpawners.availableItem, PersistentDataType.INTEGER);
            int request = Math.min(event.getClick() == ClickType.LEFT ? 1 : 16, count);


            if (request == 0) {
                MessageManager.sendMessage(player, "messages.error.notEnoughItem");
                return;
            }

            Material material = itemStack.getType();
            player.getInventory().addItem(new ItemStack(material, request));
            MessageManager.sendMessage(player, "messages.itemTaken");

            StackedSpawner spawner = inventoryManager.get(player);
            for (SpawnerItem spawnerItem : spawner.getContent()) {
                if (spawnerItem.material == material)
                    spawnerItem.currentAmount -= request;
            }

            spawner.openStorageMenu(player);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(OFStackableSpawners.getInstance(), () -> {
            String title = event.getView().getTitle();
            if (title.equals(OFStackableSpawners.mainMenuTitle) || title.equals(OFStackableSpawners.storageMenuTitle)) {
                String currentTitle = event.getPlayer().getOpenInventory().getTitle();
                if (!currentTitle.equals(OFStackableSpawners.mainMenuTitle) && !currentTitle.equals(OFStackableSpawners.storageMenuTitle)) {
                    inventoryManager.remove((Player) event.getPlayer());
                }
            }
        }, 2L);
    }
}



