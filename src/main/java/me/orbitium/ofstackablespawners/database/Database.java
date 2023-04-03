package me.orbitium.ofstackablespawners.database;

import me.orbitium.ofstackablespawners.OFStackableSpawners;
import me.orbitium.ofstackablespawners.database.objects.SpawnerItem;
import me.orbitium.ofstackablespawners.database.objects.StackedSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class Database {

    public static void saveRegisteredSpawners() {
        File file = new File(OFStackableSpawners.getInstance().getDataFolder().getPath() + "/spawnerdata.data");
        try {
            if (file.exists())
                // Clear content
                new FileWriter(file).close();
            else
                file.createNewFile();

            Map<Block, StackedSpawner> spawners = Cache.getRegisteredSpawners();
            if (spawners == null)
                return;

            FileWriter fileWriter = new FileWriter(file);
            StringBuilder sb = new StringBuilder();

            for (Map.Entry<Block, StackedSpawner> entry : spawners.entrySet()) {

                // Block data
                Block block = entry.getKey();
                String worldName = block.getWorld().getName();
                int x = block.getX();
                int y = block.getY();
                int z = block.getZ();

                sb.append(worldName).append(" ");
                sb.append(x).append(" ");
                sb.append(y).append(" ");
                sb.append(z).append(" ");
                //Spawner data

                StackedSpawner spawner = entry.getValue();
                sb.append(spawner.getEntityType().getKey().getKey()).append(" ");
                sb.append(spawner.getStackedAmount()).append(" ");
                sb.append(spawner.getStoredXP()).append(" ");

                List<SpawnerItem> content = spawner.getContent();
                for (SpawnerItem spawnerItem : content) {
                    sb.append(spawnerItem.material.getKey().getKey()).append(" ");
                    sb.append(spawnerItem.currentAmount).append(" ");
                }
                sb.append("\n");
                fileWriter.write(sb.toString());
                sb.setLength(0);
            }

            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadRegisteredSpawners() {
        try {
            File file = new File(OFStackableSpawners.getInstance().getDataFolder().getPath() + "/spawnerdata.data");
            if (!file.exists())
                return;

            BufferedReader buffer = new BufferedReader(new FileReader(file));
            String line;

            while (true) {
                line = buffer.readLine();
                if (line == null || line.isEmpty())
                    break;

                String[] data = line.split(" ");

                // Block data
                String worldName = data[0];
                int x = Integer.parseInt(data[1]);
                int y = Integer.parseInt(data[2]);
                int z = Integer.parseInt(data[3]);

                Block block = Bukkit.getWorld(worldName).getBlockAt(x, y, z);
                if (block.getType() == Material.SPAWNER) {
                    // Spawner data
                    EntityType entityType = EntityType.valueOf(data[4].toUpperCase(Locale.US));
                    int stackedAmount = Integer.parseInt(data[5]);

                    // Product data
                    int storedXP = Integer.parseInt(data[6]);
                    Map<Material, Integer> storageData = new HashMap<>();
                    int productSize = (data.length - 7) / 2;
                    for (int i = 5; i < productSize; i++) {
                        Material material = Material.getMaterial(data[i]);
                        int amount = Integer.parseInt(data[i + 1]);
                        storageData.put(material, amount);
                    }

                    StackedSpawner spawner = Cache.defaultSpawners.get(entityType).clone();
                    spawner.setStackSize(stackedAmount);
                    spawner.loadStorageData(storageData, storedXP);
                    Cache.registerSpawner(block, spawner);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadDefaultSpawners() {
        String path = OFStackableSpawners.getInstance().getDataFolder().getPath() + "/spawners/";
        File spawnersPath = new File(path);

        if (!spawnersPath.exists()) {
            spawnersPath.mkdirs();

            try {
                // Generate example file if not existing
                File exampleFile = new File(spawnersPath.getPath() + "/EXAMPLE.yml");
                exampleFile.createNewFile();
                FileWriter fileWriter = new FileWriter(exampleFile);
                fileWriter.write("# This is an example spawner\n\n");
                fileWriter.write("# What's the target entity of spawner\n");
                fileWriter.write("target_entity_type: PIG\n\n");
                fileWriter.write("# Maximum stack size of spawner (like 10x spawner)\n");
                fileWriter.write("maxStack: 10\n\n");
                fileWriter.write("# What's the list of spawner production\n");
                fileWriter.write("production:\n");
                fileWriter.write("    # The item's name\n");
                fileWriter.write("    PORKCHOP:\n");
                fileWriter.write("        # The minimum spawn amount for every 1 minute\n");
                fileWriter.write("        minSpawnAmount: 1\n");
                fileWriter.write("        # The maximum spawn amount for every 1 minute\n");
                fileWriter.write("        maxSpawnAmount: 3\n");
                fileWriter.write("        # The maximum amount of item can be stored\n");
                fileWriter.write("        maxAmount: 50\n");
                fileWriter.write("        # The slot of item in product display menu\n");
                fileWriter.write("        displaySlot: 12\n\n");
                fileWriter.write("    LEATHER:\n");
                fileWriter.write("        minSpawnAmount: 2\n");
                fileWriter.write("        maxSpawnAmount: 4\n");
                fileWriter.write("        maxAmount: 10\n");
                fileWriter.write("        displaySlot: 14\n");
                fileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Register the spawner type
        for (File spawnerData : spawnersPath.listFiles()) {
            if (!spawnerData.getName().endsWith(".yml"))
                continue;
            YamlConfiguration data = YamlConfiguration.loadConfiguration(spawnerData);

            EntityType targetEntity;
            try {
                targetEntity = EntityType.valueOf(data.getString("target_entity_type"));
            } catch (Exception e) {
                OFStackableSpawners.getInstance().getLogger().log(Level.SEVERE, "Unknown entity type: " + data.get("target_entity_type"));
                continue;
            }
            List<SpawnerItem> products = new ArrayList<>();
            int maxStack = data.getInt("maxStack");

            ConfigurationSection productionSection = data.getConfigurationSection("production");
            for (String key : productionSection.getKeys(false)) {
                Material material = Material.getMaterial(key);
                if (material == null) {
                    OFStackableSpawners.getInstance().getLogger().log(Level.SEVERE, "Material called \"" + key + "\" is not found!");
                    continue;
                }
                int minSpawnAmount = productionSection.getInt(key + ".minSpawnAmount");
                int maxSpawnAmount = productionSection.getInt(key + ".maxSpawnAmount");
                int maxAmount = productionSection.getInt(key + ".maxAmount");
                int displaySlot = productionSection.getInt(key + ".displaySlot");

                SpawnerItem spawnerItem = new SpawnerItem(material, displaySlot, minSpawnAmount, maxSpawnAmount, maxAmount);
                products.add(spawnerItem);
            }

            Cache.defaultSpawners.put(targetEntity, new StackedSpawner(targetEntity, 1, maxStack, products));
        }
    }


}
