package me.cubixor.orefinder;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


class Finding implements Listener {

    private Orefinder plugin;
    private HashMap<String, Integer> cooldown = new HashMap<>();
    private HashMap<Shulker, String> shulkers = new HashMap<>();
    private HashMap<Shulker, Block> ores = new HashMap<>();

    Finding(Orefinder of) {
        plugin = of;
    }

    @EventHandler
    void findOres(PlayerInteractEvent evt) {
        if (!(evt.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.TRIPWIRE_HOOK) && evt.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(plugin.getMessage("item-name")))) {
            return;
        }
        if (!(evt.getAction().equals(Action.RIGHT_CLICK_BLOCK) || evt.getAction().equals(Action.RIGHT_CLICK_AIR))) {
            return;
        }
        if (!evt.getPlayer().hasPermission("orefinder.use")) {
            evt.getPlayer().sendMessage(plugin.getMessage("no-permission"));
            return;
        }
        if (cooldown.containsKey(evt.getPlayer().getUniqueId().toString())) {
            evt.getPlayer().sendMessage(plugin.getMessage("prefix") + plugin.getMessage("wait-message"));
            return;
        }

        int raduis = plugin.getConfig().getInt("radius");
        Block middle = evt.getPlayer().getLocation().getBlock();
        ItemStack hook = evt.getPlayer().getInventory().getItemInMainHand();
        ArrayList<String> hookLore;
        if (hook.getItemMeta().getLore() != null) {
            hookLore = new ArrayList<>(hook.getItemMeta().getLore());
        } else {
            hookLore = new ArrayList<>();
        }

        List<Material> materialOres = new ArrayList<>();
        if (hookLore.contains(plugin.getMessage("item-lore-coal"))) {
            materialOres.add(Material.COAL_ORE);
        }
        if (hookLore.contains(plugin.getMessage("item-lore-iron"))) {
            materialOres.add(Material.IRON_ORE);
        }
        if (hookLore.contains(plugin.getMessage("item-lore-gold"))) {
            materialOres.add(Material.GOLD_ORE);
        }
        if (hookLore.contains(plugin.getMessage("item-lore-redstone"))) {
            materialOres.add(Material.REDSTONE_ORE);
        }
        if (hookLore.contains(plugin.getMessage("item-lore-lapis"))) {
            materialOres.add(Material.LAPIS_ORE);
        }
        if (hookLore.contains(plugin.getMessage("item-lore-diamond"))) {
            materialOres.add(Material.DIAMOND_ORE);
        }
        if (hookLore.contains(plugin.getMessage("item-lore-emerald"))) {
            materialOres.add(Material.EMERALD_ORE);
        }
        if (hookLore.contains(plugin.getMessage("item-lore-quartz"))) {
            materialOres.add(Material.NETHER_QUARTZ_ORE);
        }


        boolean found = false;
        for (int x = raduis; x >= -raduis; x--) {
            for (int y = raduis; y >= -raduis; y--) {
                for (int z = raduis; z >= -raduis; z--) {
                    if (materialOres.contains(middle.getRelative(x, y, z).getType())) {
                        Location loc = middle.getRelative(x, y, z).getLocation();
                        Shulker shulker = (Shulker) evt.getPlayer().getWorld().spawnEntity(loc, EntityType.SHULKER);
                        shulker.setAI(false);
                        shulker.setInvulnerable(true);
                        shulker.setGlowing(true);
                        shulkers.put(shulker, evt.getPlayer().getUniqueId().toString());
                        ores.put(shulker, evt.getPlayer().getWorld().getBlockAt(loc));
                        found = true;

                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (!p.equals(evt.getPlayer())) {
                                plugin.entityHider.toggleEntity(p, shulker);
                            }
                        }


                        if (middle.getRelative(x, y, z).getType().equals(Material.COAL_ORE)) {
                            plugin.coalOre.addEntry(shulker.getUniqueId().toString());
                        } else if (middle.getRelative(x, y, z).getType().equals(Material.IRON_ORE)) {
                            plugin.ironOre.addEntry(shulker.getUniqueId().toString());
                        } else if (middle.getRelative(x, y, z).getType().equals(Material.GOLD_ORE)) {
                            plugin.goldOre.addEntry(shulker.getUniqueId().toString());
                        } else if (middle.getRelative(x, y, z).getType().equals(Material.REDSTONE_ORE)) {
                            plugin.redstoneOre.addEntry(shulker.getUniqueId().toString());
                        } else if (middle.getRelative(x, y, z).getType().equals(Material.LAPIS_ORE)) {
                            plugin.lapisOre.addEntry(shulker.getUniqueId().toString());
                        } else if (middle.getRelative(x, y, z).getType().equals(Material.DIAMOND_ORE)) {
                            plugin.diamondOre.addEntry(shulker.getUniqueId().toString());
                        } else if (middle.getRelative(x, y, z).getType().equals(Material.EMERALD_ORE)) {
                            plugin.emeraldOre.addEntry(shulker.getUniqueId().toString());
                        } else if (middle.getRelative(x, y, z).getType().equals(Material.NETHER_QUARTZ_ORE)) {
                            plugin.quartzOre.addEntry(shulker.getUniqueId().toString());
                        }
                    }
                }
            }
        }
        if (found) {
            cooldown.put(evt.getPlayer().getUniqueId().toString(), plugin.getConfig().getInt("cooldown"));
            disappear(evt.getPlayer().getUniqueId().toString());
        }

    }

    private void disappear(String player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!cooldown.containsKey(player)) {
                    this.cancel();
                    return;
                }

                if (cooldown.get(player) != 0) {
                    cooldown.replace(player, cooldown.get(player) - 1);
                    return;
                }

                removeShulker(player);
                this.cancel();
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    @EventHandler
    public void oreBreak(BlockBreakEvent evt) {
        if (!cooldown.containsKey(evt.getPlayer().getUniqueId().toString())) {
            return;
        }

        for (Shulker shulker : ores.keySet()) {
            if (evt.getBlock().getLocation().equals(ores.get(shulker).getLocation())) {
                shulkers.remove(shulker);
                ores.remove(shulker);

                if (evt.getBlock().getType().equals(Material.COAL_ORE)) {
                    plugin.coalOre.removeEntry(shulker.getUniqueId().toString());
                } else if (evt.getBlock().getType().equals(Material.IRON_ORE)) {
                    plugin.ironOre.removeEntry(shulker.getUniqueId().toString());
                } else if (evt.getBlock().getType().equals(Material.GOLD_ORE)) {
                    plugin.goldOre.removeEntry(shulker.getUniqueId().toString());
                } else if (evt.getBlock().getType().equals(Material.REDSTONE_ORE)) {
                    plugin.redstoneOre.removeEntry(shulker.getUniqueId().toString());
                } else if (evt.getBlock().getType().equals(Material.LAPIS_ORE)) {
                    plugin.lapisOre.removeEntry(shulker.getUniqueId().toString());
                } else if (evt.getBlock().getType().equals(Material.DIAMOND_ORE)) {
                    plugin.diamondOre.removeEntry(shulker.getUniqueId().toString());
                } else if (evt.getBlock().getType().equals(Material.EMERALD_ORE)) {
                    plugin.emeraldOre.removeEntry(shulker.getUniqueId().toString());
                } else if (evt.getBlock().getType().equals(Material.NETHER_QUARTZ_ORE)) {
                    plugin.quartzOre.removeEntry(shulker.getUniqueId().toString());
                }

                shulker.remove();

                boolean otherOres = false;
                for (Shulker s : shulkers.keySet()) {
                    if (shulkers.get(s).equals(evt.getPlayer().getUniqueId().toString())) {
                        otherOres = true;
                        break;
                    }
                }
                if (!otherOres) {
                    cooldown.remove(evt.getPlayer().getUniqueId().toString());
                }
                break;
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent evt) {
        if (!cooldown.containsKey(evt.getPlayer().getUniqueId().toString())) {
            return;
        }

        removeShulker(evt.getPlayer().getUniqueId().toString());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        for (Shulker s : shulkers.keySet()) {
            plugin.entityHider.toggleEntity(evt.getPlayer(), s);
        }
    }

    private void removeShulker(String player) {
        UUID playerUUID = UUID.fromString(player);
        World world = Bukkit.getPlayer(playerUUID).getWorld();
        for (Shulker shulker : shulkers.keySet()) {
            if (shulkers.get(shulker).equals(player)) {
                if (world.getBlockAt(shulker.getLocation()).getType().equals(Material.COAL_ORE)) {
                    plugin.diamondOre.removeEntry(shulker.getUniqueId().toString());
                } else if (world.getBlockAt(shulker.getLocation()).getType().equals(Material.IRON_ORE)) {
                    plugin.ironOre.removeEntry(shulker.getUniqueId().toString());
                } else if (world.getBlockAt(shulker.getLocation()).getType().equals(Material.GOLD_ORE)) {
                    plugin.goldOre.removeEntry(shulker.getUniqueId().toString());
                } else if (world.getBlockAt(shulker.getLocation()).getType().equals(Material.REDSTONE_ORE)) {
                    plugin.redstoneOre.removeEntry(shulker.getUniqueId().toString());
                } else if (world.getBlockAt(shulker.getLocation()).getType().equals(Material.LAPIS_ORE)) {
                    plugin.lapisOre.removeEntry(shulker.getUniqueId().toString());
                } else if (world.getBlockAt(shulker.getLocation()).getType().equals(Material.DIAMOND_ORE)) {
                    plugin.diamondOre.removeEntry(shulker.getUniqueId().toString());
                } else if (world.getBlockAt(shulker.getLocation()).getType().equals(Material.EMERALD_ORE)) {
                    plugin.emeraldOre.removeEntry(shulker.getUniqueId().toString());
                } else if (world.getBlockAt(shulker.getLocation()).getType().equals(Material.NETHER_QUARTZ_ORE)) {
                    plugin.quartzOre.removeEntry(shulker.getUniqueId().toString());
                }

                shulker.remove();
            }
        }
        cooldown.remove(player);
    }
}
