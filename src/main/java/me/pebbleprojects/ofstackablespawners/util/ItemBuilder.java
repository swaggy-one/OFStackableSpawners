package me.pebbleprojects.ofstackablespawners.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder implements Cloneable {
    private ItemStack itemStack;
    private ItemMeta itemMeta;

    public ItemBuilder(Material material, String name) {
        itemStack = new ItemStack(material);
        itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
    }

    public ItemBuilder(Material material, String name, List<String> lore) {
        itemStack = new ItemStack(material);
        itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        setLore(lore);
    }

    public ItemBuilder(Material material) {
        itemStack = new ItemStack(material);
        itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder setMaterial(Material material) {
        itemStack.setType(material);
        return this;
    }

    public ItemBuilder setName(String newName) {
        if (!newName.isEmpty())
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', newName));
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        List<String> coloredLore = new ArrayList<>();
        for (String s : lore)
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', s));
        itemMeta.setLore(coloredLore);
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        itemMeta.setLore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder addLore(String line) {
        List<String> lore = itemMeta.getLore();
        if (lore == null)
            lore = new ArrayList<>();
        lore.add(line);
        setLore(lore);
        return this;
    }

    public ItemBuilder replaceName(String target, String replace) {
        setName(itemMeta.getDisplayName().replaceAll(target, replace));
        return this;
    }

    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public ItemBuilder clone() {
        try {
            ItemBuilder clone = (ItemBuilder) super.clone();
            clone.itemStack = itemStack.clone();
            clone.itemMeta = itemMeta.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public String getName() {
        return itemMeta.getDisplayName();
    }
}
