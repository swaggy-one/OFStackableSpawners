package me.pebbleprojects.ofstackablespawners.database.objects;

import me.pebbleprojects.ofstackablespawners.OFStackableSpawners;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SpawnerItem implements Cloneable {
    // For essential calculation
    public final ItemStack itemStack;
    public final Material material;
    final int displaySlot;
    final int maxSpawn;
    final int minSpawn;
    final int defaultMaxAmount;
    int maxAmount;
    public int currentAmount;

    public SpawnerItem(Material material, int displaySlot, int minSpawn, int maxSpawn, int maxAmount) {
        this.material = material;
        this.displaySlot = displaySlot;
        this.maxSpawn = maxSpawn;
        this.minSpawn = minSpawn;
        this.defaultMaxAmount = maxAmount;
        itemStack = new ItemStack(material);
    }

    void updateMaxAmount(int multiplier) {
        maxAmount = defaultMaxAmount * multiplier;
        if (currentAmount > maxAmount)
            currentAmount = maxAmount;
    }

    public void tryToGenerate(int multiplier) {
        int createdAmount;
        if (maxSpawn > 0)
            createdAmount = OFStackableSpawners.random.nextInt(maxSpawn);
        else
            createdAmount = minSpawn;
        currentAmount += Math.max(minSpawn, createdAmount) * multiplier;
        currentAmount = Math.min(currentAmount, maxAmount);

    }

    @Override
    public SpawnerItem clone() {
        try {
            SpawnerItem clone = (SpawnerItem) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
