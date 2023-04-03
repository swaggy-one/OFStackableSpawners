package me.orbitium.ofstackablespawners.listeners;

import me.orbitium.ofstackablespawners.OFStackableSpawners;
import me.orbitium.ofstackablespawners.database.Cache;
import me.orbitium.ofstackablespawners.database.objects.StackedSpawner;
import me.orbitium.ofstackablespawners.util.MessageManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.Map;

public class BlockListener implements Listener {

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlock();
        if (!(placedBlock.getState() instanceof CreatureSpawner spawner))
            return;

        EntityType entityType = spawner.getSpawnedType();
        StackedSpawner newSpawner = Cache.defaultSpawners.get(entityType);
        Player player = event.getPlayer();
        if (newSpawner == null) {
            event.setCancelled(true);
            MessageManager.sendMessage(player, "messages.error.unknownSpawnerType");
            return;
        }

        Block blockAgainst = event.getBlockAgainst();
        if (blockAgainst.getState() instanceof CreatureSpawner againstSpawner) {
            StackedSpawner stackableSpawner = Cache.getSpawner(blockAgainst);

            if (stackableSpawner == null) {
                MessageManager.sendMessage(player, "messages.error.abandonedSpawnersCantBeStacked");
                event.setCancelled(true);
                return;
            }

            if (!stackableSpawner.getEntityType().equals(entityType)) {
                StackedSpawner registeredSpawner = newSpawner.clone();
                registeredSpawner.update();
                Cache.registerSpawner(placedBlock, registeredSpawner);
                MessageManager.sendMessage(player, "messages.spawnerPlaced");
                return;
            }

            if (stackableSpawner.merge()) {
                MessageManager.sendMessage(player, "messages.spawnerStacked");
                placedBlock.setType(Material.AIR);
            } else {
                MessageManager.sendMessage(player, "messages.error.spawnerCantBeMoreStacked");
                event.setCancelled(true);
            }

            return;
        }
        StackedSpawner registeredSpawner = newSpawner.clone();
        registeredSpawner.update();
        Cache.registerSpawner(placedBlock, registeredSpawner);
        MessageManager.sendMessage(player, "messages.spawnerPlaced");
    }

    @EventHandler
    public void onSpawnerBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();

        if (!(brokenBlock.getState() instanceof CreatureSpawner spawner))
            return;

        Player player = event.getPlayer();
        if (!player.isOp())
            if (player.getPlayerTime() / 60 < OFStackableSpawners.minPlayTime) {
                MessageManager.sendMessage(player, "messages.error.notEnoughPlayTime", "%time&",
                        OFStackableSpawners.minPlayTime + "");
                event.setCancelled(true);
                return;
            }

        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
        boolean dropSpawner = itemStack.containsEnchantment(Enchantment.SILK_TOUCH);

        StackedSpawner stackableSpawner = Cache.getSpawner(brokenBlock);

        // If the spawner is not placed by player
        if (stackableSpawner == null) {
            MessageManager.sendMessage(player, "messages.abandonedSpawnerRemoved");

            if (!dropSpawner)
                return;
            ItemStack drop = new ItemStack(Material.SPAWNER);
            BlockStateMeta bsm = (BlockStateMeta) drop.getItemMeta();
            CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();
            cs.setSpawnedType(cs.getSpawnedType());
            bsm.setBlockState(cs);
            drop.setItemMeta(bsm);
            player.getWorld().dropItemNaturally(brokenBlock.getLocation(), drop);
            return;
        }

        // If the spawner is placed by player
        if (player.isSneaking()) {
            boolean deleteSpawner = stackableSpawner.removeSpawnerOnBreak();
            if (!deleteSpawner) {
                event.setCancelled(true);
                MessageManager.sendMessage(player, "messages.oneSpawnerStackRemoved");
            } else {
                Cache.unregisterSpawner(brokenBlock);
                MessageManager.sendMessage(player, "messages.spawnerRemoved");
            }

            if (!dropSpawner)
                return;
            ItemStack drop = new ItemStack(Material.SPAWNER);
            BlockStateMeta bsm = (BlockStateMeta) drop.getItemMeta();
            CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();
            cs.setSpawnedType(cs.getSpawnedType());
            bsm.setBlockState(cs);
            drop.setItemMeta(bsm);
            player.getWorld().dropItemNaturally(brokenBlock.getLocation(), drop);
            return;
        }

        if (dropSpawner) {
            int stackSize = stackableSpawner.getStackSize();
            ItemStack drop = new ItemStack(Material.SPAWNER, stackSize);
            BlockStateMeta bsm = (BlockStateMeta) drop.getItemMeta();
            CreatureSpawner cs = (CreatureSpawner) bsm.getBlockState();
            cs.setSpawnedType(cs.getSpawnedType());
            bsm.setBlockState(cs);
            drop.setItemMeta(bsm);
            player.getWorld().dropItemNaturally(brokenBlock.getLocation(), drop);
        }
        Cache.unregisterSpawner(brokenBlock);
        MessageManager.sendMessage(player, "messages.spawnerRemoved");
    }

    @EventHandler
    public static void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND)
            return;

        if (event.getClickedBlock() == null)
            return;

        Block block = event.getClickedBlock();
        if (!(block.getState() instanceof CreatureSpawner spawner))
            return;

        ItemStack itemStack = event.getItem();
        StackedSpawner stackableSpawner = Cache.getSpawner(block);
        if (itemStack != null && itemStack.getType().equals(Material.SPAWNER))
            return;
        /*
        Disabled: can be triggered twice (BlockPlace + this event)
        if (itemStack != null && itemStack.getType().equals(Material.SPAWNER)) {

            EntityType entityType = spawner.getSpawnedType();
            Player player = event.getPlayer();

            if (stackableSpawner == null) {
                MessageManager.sendMessage(player, "messages.error.abandonedSpawnersCantBeStacked");
                event.setCancelled(true);
                return;
            }

            if (!stackableSpawner.getEntityType().equals(entityType))
                return;

            if (stackableSpawner.merge()) {
                MessageManager.sendMessage(player, "messages.spawnerStacked");
            } else {
                MessageManager.sendMessage(player, "messages.error.spawnerCantBeMoreStacked");
                event.setCancelled(true);
            }

            return;
        }*/
        Player player = event.getPlayer();

        if (stackableSpawner != null) {
            for (Map.Entry<Player, StackedSpawner> entry : InventoryListener.inventoryManager.entrySet()) {
                if (entry.getValue() == stackableSpawner) {
                    MessageManager.sendMessage(player, "messages.error.cantInteract");
                    return;
                }
            }


            stackableSpawner.openMainMenu(player);
            InventoryListener.inventoryManager.put(player, stackableSpawner);
        } else {
            MessageManager.sendMessage(player, "messages.error.abandonedSpawnersCantOpen");
        }
    }

}
