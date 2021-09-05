package me.cubixor.orefinder;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;


public class Finding implements Listener {

    private final OreFinder plugin;

    public Finding() {
        this.plugin = OreFinder.getInstance();
    }

    @EventHandler
    public void findOres(PlayerInteractEvent evt) {
        if (evt.getHand() == null || !evt.getHand().equals(EquipmentSlot.HAND)) {
            return;
        }

        ItemStack findingItem = plugin.hasFindingItem(evt.getPlayer(), true);

        if (findingItem == null) {
            return;
        }

        if (!evt.getPlayer().hasPermission("orefinder.use")) {
            evt.getPlayer().sendMessage(plugin.getMessage("no-permission"));
            return;
        }

        Player player = evt.getPlayer();

        if (plugin.getPlayerData().containsKey(player)) {
            removeShulker(player);
            plugin.getPlayerData().remove(player);
            evt.getPlayer().sendMessage(plugin.getMessage("disable"));
            return;
        }


        if (findingItem.getItemMeta().getLore() == null) {
            evt.getPlayer().sendMessage(plugin.getMessage("no-ores"));
            return;
        }

        List<String> lore = findingItem.getItemMeta().getLore() != null ? new ArrayList<>(findingItem.getItemMeta().getLore()) : new ArrayList<>();

        List<Block> materials = new ArrayList<>();
        for (Block block : plugin.getBlocksToFind().values()) {
            if (lore.contains(block.getName())) {
                materials.add(block);
            }
        }
        int radius = plugin.getConfig().getInt("radius");
        Chunk chunk = player.getLocation().getChunk();
        int time = plugin.getConfig().getInt("cooldown");

        PlayerData playerData = new PlayerData(materials, radius, chunk, time);
        plugin.getPlayerData().put(player, playerData);

        if (evt.getPlayer().isSneaking()) {
            evt.getPlayer().sendMessage(plugin.getMessage("enable"));
            playerData.setCooldown(-1);
        } else {
            evt.getPlayer().sendMessage(plugin.getMessage("enable-cooldown").replace("%time%", Integer.toString(playerData.getCooldown())));
        }

        disappear(evt.getPlayer());
        updateChunks(evt.getPlayer(), true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent evt) {
        updateChunks(evt.getPlayer(), false);
    }

    public void updateChunks(Player player, boolean firstUpdate) {
        if (!plugin.getPlayerData().containsKey(player)) {
            return;
        }

        PlayerData playerData = plugin.getPlayerData().get(player);

        if (playerData.getChunk().equals(player.getLocation().getChunk()) && !firstUpdate) {
            return;
        }

        playerData.setChunk(player.getLocation().getChunk());

        List<Material> materials = new ArrayList<>();
        for (Block block : playerData.getBlocksToFind()) {
            materials.add(block.getMaterial());
        }


        List<org.bukkit.block.Block> nearbyBlocks = new ArrayList<>();

        Collection<Chunk> chunks = getChunksAroundPlayer(player);


        for (Chunk chunk : chunks) {
            final int minX = chunk.getX() << 4;
            final int minZ = chunk.getZ() << 4;
            final int maxX = minX | 15;
            final int maxY = chunk.getWorld().getMaxHeight();
            final int maxZ = minZ | 15;

            for (int x = minX; x <= maxX; ++x) {
                for (int y = 0; y <= maxY; ++y) {
                    loop:
                    for (int z = minZ; z <= maxZ; ++z) {
                        org.bukkit.block.Block block = chunk.getWorld().getBlockAt(x, y, z);

                        if (!materials.contains(block.getType())) {
                            continue;
                        }

                        nearbyBlocks.add(block);
                        for (Shulker shulker : new ArrayList<>(playerData.getMarkedBlocks())) {
                            if (shulker.getLocation().getBlock().equals(block)) {
                                continue loop;
                            }
                        }

                        Location loc = block.getLocation();


                        Shulker shulker = (Shulker) player.getWorld().spawnEntity(loc, EntityType.SHULKER);
                        shulker.setAI(false);
                        shulker.setInvulnerable(true);
                        shulker.setGlowing(true);
                        PotionEffect invisibility = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 255, false, false);
                        shulker.addPotionEffect(invisibility);

                        playerData.getMarkedBlocks().add(shulker);


                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (!p.equals(player)) {
                                plugin.entityHider.hideEntity(p, shulker);
                            }
                        }

                        plugin.getBlockByMaterial(block.getType()).getTeam().addEntry(shulker.getUniqueId().toString());
                    }
                }
            }
        }


        for (Shulker shulker : new ArrayList<>(playerData.getMarkedBlocks())) {
            org.bukkit.block.Block block = shulker.getWorld().getBlockAt(shulker.getLocation());
            if (!nearbyBlocks.contains(block)) {
                shulker.remove();
                plugin.getBlockByMaterial(block.getType()).getTeam().removeEntry(shulker.getUniqueId().toString());
                playerData.getMarkedBlocks().remove(shulker);
            }
        }


    }

    public Collection<Chunk> getChunksAroundPlayer(Player player) {
        int radius = plugin.getPlayerData().get(player).getRadius();
        Collection<Integer> offset = new HashSet<>();
        for (int i = -radius; i <= radius; i++) {
            offset.add(i);
        }

        World world = player.getWorld();
        int baseX = player.getLocation().getChunk().getX();
        int baseZ = player.getLocation().getChunk().getZ();

        Collection<Chunk> chunksAroundPlayer = new HashSet<>();
        for (int x : offset) {
            for (int z : offset) {
                Chunk chunk = world.getChunkAt(baseX + x, baseZ + z);
                chunksAroundPlayer.add(chunk);
            }
        }
        return chunksAroundPlayer;
    }

    void disappear(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getPlayerData().containsKey(player)) {
                    this.cancel();
                } else {
                    PlayerData playerData = plugin.getPlayerData().get(player);
                    int time = playerData.getCooldown();
                    if (time > 0) {
                        playerData.setCooldown(time - 1);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(plugin.getMessage("cooldown-format").replace("%time%", Integer.toString(time))));
                    } else if (time == -1) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(plugin.getMessage("enabled-format")));
                    } else {
                        removeShulker(player);
                        plugin.getPlayerData().remove(player);
                        player.sendMessage(plugin.getMessage("disable"));
                        this.cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    @EventHandler
    public void oreBreak(BlockBreakEvent evt) {
        for (PlayerData playerData : plugin.getPlayerData().values()) {
            for (Shulker shulker : playerData.getMarkedBlocks()) {
                if (evt.getBlock().equals(shulker.getLocation().getBlock())) {

                    playerData.getMarkedBlocks().remove(shulker);
                    plugin.getBlockByMaterial(evt.getBlock().getType()).getTeam().removeEntry(shulker.getUniqueId().toString());
                    shulker.remove();

                    return;
                }
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent evt) {
        if (!plugin.getPlayerData().containsKey(evt.getPlayer())) {
            return;
        }

        removeShulker(evt.getPlayer());
        plugin.getPlayerData().remove(evt.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        for (PlayerData playerData : plugin.getPlayerData().values()) {
            for (Shulker shulker : playerData.getMarkedBlocks()) {
                plugin.entityHider.hideEntity(evt.getPlayer(), shulker);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent evt) {
        if (plugin.hasFindingItem(evt.getPlayer(), true) != null) {
            evt.setCancelled(true);
        }
    }


    void removeShulker(Player player) {
        PlayerData playerData = plugin.getPlayerData().get(player);
        for (Shulker shulker : new ArrayList<>(playerData.getMarkedBlocks())) {
            Block block = plugin.getBlockByMaterial(player.getWorld().getBlockAt(shulker.getLocation()).getType());
            shulker.remove();
            block.getTeam().removeEntry(shulker.getUniqueId().toString());
            playerData.getMarkedBlocks().remove(shulker);
        }
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(""));
    }
}
