package me.cubixor.orefinder;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


class Finding implements Listener {

    public Orefinder plugin;

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
        if (!evt.getHand().equals(EquipmentSlot.HAND)) {
            return;
        }
        if (!evt.getPlayer().hasPermission("orefinder.use")) {
            evt.getPlayer().sendMessage(plugin.getMessage("prefix") + plugin.getMessage("no-permission"));
            return;
        }

        if (plugin.sneaking.contains(evt.getPlayer().getUniqueId().toString())) {
            removeShulker(evt.getPlayer().getUniqueId().toString());
            plugin.sneaking.remove(evt.getPlayer().getUniqueId().toString());
            evt.getPlayer().sendMessage(plugin.getMessage("prefix") + plugin.getMessage("disable"));
            return;
        }

        if (plugin.cooldown.containsKey(evt.getPlayer().getUniqueId().toString())) {
            plugin.cooldown.remove(evt.getPlayer().getUniqueId().toString());
            plugin.radius.remove(evt.getPlayer().getUniqueId().toString());
            return;
        }

        ItemStack hook = evt.getPlayer().getInventory().getItemInMainHand();
        ArrayList<String> hookLore;
        if (hook.getItemMeta().getLore() != null) {
            hookLore = new ArrayList<>(hook.getItemMeta().getLore());
        } else {
            hookLore = new ArrayList<>();
        }

        List<Material> materials = new ArrayList<>();
        if (hookLore.contains(plugin.getMessage("item-lore-coal"))) {
            materials.add(Material.COAL_ORE);
        }
        if (hookLore.contains(plugin.getMessage("item-lore-iron"))) {
            materials.add(Material.IRON_ORE);
        }
        if (hookLore.contains(plugin.getMessage("item-lore-gold"))) {
            materials.add(Material.GOLD_ORE);
        }
        if (hookLore.contains(plugin.getMessage("item-lore-redstone"))) {
            materials.add(Material.REDSTONE_ORE);
        }
        if (hookLore.contains(plugin.getMessage("item-lore-lapis"))) {
            materials.add(Material.LAPIS_ORE);
        }
        if (hookLore.contains(plugin.getMessage("item-lore-diamond"))) {
            materials.add(Material.DIAMOND_ORE);
        }
        if (hookLore.contains(plugin.getMessage("item-lore-emerald"))) {
            materials.add(Material.EMERALD_ORE);
        }
        if (hookLore.contains(plugin.getMessage("item-lore-quartz"))) {
            materials.add(Material.NETHER_QUARTZ_ORE);
        }
        plugin.materialOres.put(evt.getPlayer().getUniqueId().toString(), materials);
        plugin.radius.put(evt.getPlayer().getUniqueId().toString(), plugin.getConfig().getInt("radius"));
        plugin.chunk.put(evt.getPlayer().getUniqueId().toString(), evt.getPlayer().getLocation().getChunk());



        if (evt.getPlayer().isSneaking()) {
            plugin.sneaking.add(evt.getPlayer().getUniqueId().toString());
            evt.getPlayer().sendMessage(plugin.getMessage("prefix") + plugin.getMessage("enable"));
        } else {
            plugin.cooldown.put(evt.getPlayer().getUniqueId().toString(), plugin.getConfig().getInt("cooldown"));
            disappear(evt.getPlayer());
            evt.getPlayer().sendMessage(plugin.getMessage("prefix") + plugin.getMessage("enable-cooldown").replace("%time%", plugin.cooldown.get(evt.getPlayer().getUniqueId().toString()).toString()));
        }

    }

    @EventHandler
    public void onMove(PlayerMoveEvent evt) {
        if (!plugin.cooldown.containsKey(evt.getPlayer().getUniqueId().toString()) && !plugin.sneaking.contains(evt.getPlayer().getUniqueId().toString())) {
            return;
        }

        if (plugin.chunk.get(evt.getPlayer().getUniqueId().toString()).equals(evt.getPlayer().getLocation().getChunk())) {
            return;
        }

        plugin.chunk.replace(evt.getPlayer().getUniqueId().toString(), evt.getPlayer().getLocation().getChunk());

        int raduis = (16 * plugin.radius.get(evt.getPlayer().getUniqueId().toString()));
        Block middle = evt.getPlayer().getLocation().getBlock();
        List<Block> nearbyBlocks = new ArrayList<>();
        for (int x = raduis; x >= -raduis; x--) {
            for (int y = raduis; y >= -raduis; y--) {
                for (int z = raduis; z >= -raduis; z--) {
                    if (plugin.materialOres.get(evt.getPlayer().getUniqueId().toString()).contains(middle.getRelative(x, y, z).getType())) {
                        nearbyBlocks.add(middle.getRelative(x, y, z));
                        if (!plugin.blocks.containsValue(middle.getRelative(x, y, z))) {
                            Location loc = middle.getRelative(x, y, z).getLocation();
                            Shulker shulker = (Shulker) evt.getPlayer().getWorld().spawnEntity(loc, EntityType.SHULKER);
                            shulker.setAI(false);
                            shulker.setInvulnerable(true);
                            shulker.setGlowing(true);
                            PotionEffect invisibility = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 255, false, false);
                            shulker.addPotionEffect(invisibility);
                            plugin.shulkers.put(shulker, evt.getPlayer().getUniqueId().toString());
                            plugin.blocks.put(shulker, evt.getPlayer().getWorld().getBlockAt(loc));

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
        }
        List<Shulker> toRemove = new ArrayList<>();
        for (Shulker shulker : plugin.blocks.keySet()) {
            if (!nearbyBlocks.contains(plugin.blocks.get(shulker))) {
                World world = evt.getPlayer().getWorld();

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
                plugin.shulkers.remove(shulker);
                toRemove.add(shulker);
                shulker.remove();
            }
        }
        for (Shulker shulker : toRemove) {
            plugin.blocks.remove(shulker);
        }
    }


    public void disappear(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.cooldown.containsKey(player.getUniqueId().toString())) {
                    removeShulker(player.getUniqueId().toString());
                    plugin.materialOres.remove(player.getUniqueId().toString());
                    plugin.radius.remove(player.getUniqueId().toString());
                    plugin.chunk.remove(player.getUniqueId().toString());
                    this.cancel();
                } else {
                    if (plugin.cooldown.get(player.getUniqueId().toString()) != 0) {
                        int time = plugin.cooldown.replace(player.getUniqueId().toString(), plugin.cooldown.get(player.getUniqueId().toString()) - 1);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(plugin.getMessage("cooldown-format").replace("%time%", Integer.toString(time))));
                    } else {
                        removeShulker(player.getUniqueId().toString());
                        plugin.cooldown.remove(player.getUniqueId().toString());
                        plugin.materialOres.remove(player.getUniqueId().toString());
                        plugin.radius.remove(player.getUniqueId().toString());
                        plugin.chunk.remove(player.getUniqueId().toString());
                        player.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("disable"));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    @EventHandler
    public void oreBreak(BlockBreakEvent evt) {
        if (!plugin.cooldown.containsKey(evt.getPlayer().getUniqueId().toString()) && !plugin.sneaking.contains(evt.getPlayer().getUniqueId().toString())) {
            return;
        }

        HashMap<Shulker, Block> oresToRemove = new HashMap<>();
        for (Shulker shulker : plugin.blocks.keySet()) {

            if (evt.getBlock().getLocation().equals(plugin.blocks.get(shulker).getLocation())) {

                plugin.shulkers.remove(shulker);
                oresToRemove.put(shulker, plugin.blocks.get(shulker));

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
                for (Shulker s : plugin.shulkers.keySet()) {
                    if (plugin.shulkers.get(s).equals(evt.getPlayer().getUniqueId().toString())) {
                        otherOres = true;
                        break;
                    }
                }
                if (!otherOres) {
                    plugin.cooldown.remove(evt.getPlayer().getUniqueId().toString());
                }
                break;
            }
        }
        for (Shulker shulker : oresToRemove.keySet()) {
            plugin.blocks.remove(shulker);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent evt) {
        if (!plugin.cooldown.containsKey(evt.getPlayer().getUniqueId().toString()) && !plugin.sneaking.contains(evt.getPlayer().getUniqueId().toString())) {
            return;
        }
        plugin.cooldown.remove(evt.getPlayer().getUniqueId().toString());
        if (plugin.sneaking.contains(evt.getPlayer().getUniqueId().toString())) {
            plugin.sneaking.remove(evt.getPlayer().getUniqueId().toString());
            (new Finding(plugin)).removeShulker(evt.getPlayer().getUniqueId().toString());
        }
        plugin.materialOres.remove(evt.getPlayer().getUniqueId().toString());
        plugin.radius.remove(evt.getPlayer().getUniqueId().toString());
        plugin.chunk.remove(evt.getPlayer().getUniqueId().toString());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        for (Shulker s : plugin.shulkers.keySet()) {
            plugin.entityHider.toggleEntity(evt.getPlayer(), s);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent evt) {
        if (evt.getItemInHand().getItemMeta().getDisplayName().equals(plugin.getMessage("item-name"))) {
            evt.setCancelled(true);
        }
    }

    public void removeShulker(String player) {
        World world = null;
        for (Shulker shulker : plugin.shulkers.keySet()) {
            if (plugin.shulkers.get(shulker).equals(player)) {
                world = shulker.getWorld();
            }
        }

        List<Shulker> toRemove = new ArrayList<>();
        for (Shulker shulker : plugin.shulkers.keySet()) {
            if (plugin.shulkers.get(shulker).equals(player)) {
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
                toRemove.add(shulker);
                plugin.blocks.remove(shulker);
                shulker.remove();
            }
        }
        for (Shulker shulker : toRemove) {
            plugin.shulkers.remove(shulker);
        }
    }
}
