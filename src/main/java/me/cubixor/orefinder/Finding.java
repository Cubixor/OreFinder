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

    private Orefinder plugin;
    private HashMap<String, Integer> cooldown = new HashMap<>();
    private HashMap<Shulker, String> shulkers = new HashMap<>();
    private HashMap<Shulker, Block> blocks = new HashMap<>();
    private List<String> sneaking = new ArrayList<>();
    private HashMap<String, List<Material>> materialOres = new HashMap<>();

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

        if (sneaking.contains(evt.getPlayer().getUniqueId().toString())) {
            removeShulker(evt.getPlayer().getUniqueId().toString());
            sneaking.remove(evt.getPlayer().getUniqueId().toString());
            evt.getPlayer().sendMessage(plugin.getMessage("prefix") + plugin.getMessage("disable"));
            return;
        }

        if (cooldown.containsKey(evt.getPlayer().getUniqueId().toString())) {
            cooldown.remove(evt.getPlayer().getUniqueId().toString());
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
        materialOres.put(evt.getPlayer().getUniqueId().toString(), materials);


        if (evt.getPlayer().isSneaking()) {
            sneaking.add(evt.getPlayer().getUniqueId().toString());
            evt.getPlayer().sendMessage(plugin.getMessage("prefix") + plugin.getMessage("enable"));
        } else {
            cooldown.put(evt.getPlayer().getUniqueId().toString(), plugin.getConfig().getInt("cooldown"));
            disappear(evt.getPlayer());
            evt.getPlayer().sendMessage(plugin.getMessage("prefix") + plugin.getMessage("enable-cooldown").replace("%time%", cooldown.get(evt.getPlayer().getUniqueId().toString()).toString()));
        }

    }

    @EventHandler
    public void onMove(PlayerMoveEvent evt) {
        if (!cooldown.containsKey(evt.getPlayer().getUniqueId().toString()) && !sneaking.contains(evt.getPlayer().getUniqueId().toString())) {
            return;
        }

        int raduis = plugin.getConfig().getInt("radius");
        Block middle = evt.getPlayer().getLocation().getBlock();
        List<Block> nearbyBlocks = new ArrayList<>();
        for (int x = raduis; x >= -raduis; x--) {
            for (int y = raduis; y >= -raduis; y--) {
                for (int z = raduis; z >= -raduis; z--) {
                    if (materialOres.get(evt.getPlayer().getUniqueId().toString()).contains(middle.getRelative(x, y, z).getType())) {
                        nearbyBlocks.add(middle.getRelative(x, y, z));
                        if (!blocks.containsValue(middle.getRelative(x, y, z))) {
                            Location loc = middle.getRelative(x, y, z).getLocation();
                            Shulker shulker = (Shulker) evt.getPlayer().getWorld().spawnEntity(loc, EntityType.SHULKER);
                            shulker.setAI(false);
                            shulker.setInvulnerable(true);
                            shulker.setGlowing(true);
                            PotionEffect invisibility = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 255, false, false);
                            shulker.addPotionEffect(invisibility);
                            shulkers.put(shulker, evt.getPlayer().getUniqueId().toString());
                            blocks.put(shulker, evt.getPlayer().getWorld().getBlockAt(loc));

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
        for (Shulker shulker : blocks.keySet()) {
            if (!nearbyBlocks.contains(blocks.get(shulker))) {
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
                shulkers.remove(shulker);
                toRemove.add(shulker);
                shulker.remove();
            }
        }
        for (Shulker shulker : toRemove) {
            blocks.remove(shulker);
        }
    }


    private void disappear(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!cooldown.containsKey(player.getUniqueId().toString())) {
                    removeShulker(player.getUniqueId().toString());
                    materialOres.remove(player.getUniqueId().toString());
                    this.cancel();
                } else {
                    if (cooldown.get(player.getUniqueId().toString()) != 0) {
                        int time = cooldown.replace(player.getUniqueId().toString(), cooldown.get(player.getUniqueId().toString()) - 1);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(plugin.getMessage("cooldown-format").replace("%time%", Integer.toString(time))));
                    } else {
                        removeShulker(player.getUniqueId().toString());
                        cooldown.remove(player.getUniqueId().toString());
                        materialOres.remove(player.getUniqueId().toString());
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
        if (!cooldown.containsKey(evt.getPlayer().getUniqueId().toString()) && !sneaking.contains(evt.getPlayer().getUniqueId().toString())) {
            return;
        }

        HashMap<Shulker, Block> oresToRemove = new HashMap<>();
        for (Shulker shulker : blocks.keySet()) {

            if (evt.getBlock().getLocation().equals(blocks.get(shulker).getLocation())) {

                shulkers.remove(shulker);
                oresToRemove.put(shulker, blocks.get(shulker));

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
        for (Shulker shulker : oresToRemove.keySet()) {
            blocks.remove(shulker);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent evt) {
        if (!cooldown.containsKey(evt.getPlayer().getUniqueId().toString())) {
            return;
        }
        cooldown.remove(evt.getPlayer().getUniqueId().toString());
        materialOres.remove(evt.getPlayer().getUniqueId().toString());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        for (Shulker s : shulkers.keySet()) {
            plugin.entityHider.toggleEntity(evt.getPlayer(), s);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent evt) {
        if (evt.getItemInHand().getItemMeta().getDisplayName().equals(plugin.getMessage("item-name"))) {
            evt.setCancelled(true);
        }
    }

    private void removeShulker(String player) {
        World world = null;
        for (Shulker shulker : shulkers.keySet()) {
            if (shulkers.get(shulker).equals(player)) {
                world = shulker.getWorld();
            }
        }

        List<Shulker> toRemove = new ArrayList<>();
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
                toRemove.add(shulker);
                blocks.remove(shulker);
                shulker.remove();
            }
        }
        for (Shulker shulker : toRemove) {
            shulkers.remove(shulker);
        }
    }
}
