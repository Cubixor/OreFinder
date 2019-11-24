package me.cubixor.orefinder;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Crafting implements Listener {

    private Orefinder plugin;

    Crafting(Orefinder of) {
        plugin = of;
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent evt) {
        if (plugin.getConfig().getBoolean("enable-crafting")) {
            for (int i = 1; i <= 9; i++) {
                if (evt.getInventory().getItem(i) == null) {
                    return;
                }
            }

            List<Material> materialType = new ArrayList<>();
            for (int i = 1; i <= 9; i++) {
                materialType.add(evt.getInventory().getItem(i).getType());
            }

            if (!materialType.get(4).equals(Material.TRIPWIRE_HOOK) && !evt.getInventory().getItem(5).getItemMeta().getDisplayName().equals(plugin.getMessage("item-name"))) {
                return;
            }


            HashMap<Material, String> ores = new HashMap<>();
            ores.put(Material.COAL_ORE, "item-lore-coal");
            ores.put(Material.IRON_ORE, "item-lore-iron");
            ores.put(Material.GOLD_ORE, "item-lore-gold");
            ores.put(Material.REDSTONE_ORE, "item-lore-redstone");
            ores.put(Material.LAPIS_ORE, "item-lore-lapis");
            ores.put(Material.DIAMOND_ORE, "item-lore-diamond");
            ores.put(Material.EMERALD_ORE, "item-lore-emerald");
            ores.put(Material.NETHER_QUARTZ_ORE, "item-lore-quartz");


            ItemStack newHook = new ItemStack(Material.TRIPWIRE_HOOK);
            ItemMeta newHookMeta = newHook.getItemMeta();
            newHookMeta.setDisplayName(plugin.getMessage("item-name"));
            newHookMeta.addEnchant(Enchantment.PIERCING, 1, true);
            newHook.setItemMeta(newHookMeta);
            List<String> hookLore = new ArrayList<>();

            if (evt.getInventory().getItem(5).getItemMeta().getLore() != null) {
                for (String s : evt.getInventory().getItem(5).getItemMeta().getLore()) {
                    hookLore.add(s);
                }
            }


            Material usedOre = null;
            for (int i = 1; i <= 9; i++) {
                if (i == 5) {
                    continue;
                }
                if (ores.containsKey(materialType.get(i - 1))) {
                    if (usedOre == null) {
                        usedOre = materialType.get(i - 1);
                    } else {
                        if (!usedOre.equals(materialType.get(i - 1))) {
                            return;
                        }
                    }
                } else {
                    return;
                }
            }

            ItemStack oldHook = evt.getInventory().getItem(5);

            for (Material ore : ores.keySet()) {
                if (usedOre.equals(ore)) {
                    String name = ores.get(ore);

                    if (oldHook.getItemMeta().getLore() == null || !oldHook.getItemMeta().getLore().contains(plugin.getMessage(name))) {

                        hookLore.add(plugin.getMessage(name));

                    }
                }
            }

            newHookMeta.setLore(hookLore);
            newHook.setItemMeta(newHookMeta);

            evt.getInventory().setResult(newHook);

        }
    }
}
