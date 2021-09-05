package me.cubixor.orefinder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Command implements CommandExecutor {

    private final OreFinder plugin;

    public Command() {
        this.plugin = OreFinder.getInstance();
    }


    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("must-be-player"));
            return true;
        }


        Player player = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            if (!sender.hasPermission("orefinder.command.help")) {
                sender.sendMessage(plugin.getMessage("no-permission"));
                return true;
            }

            player.sendMessage(plugin.getMessageList("help").toArray(new String[0]));
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("orefinder.command.reload")) {
                sender.sendMessage(plugin.getMessage("no-permission"));
                return true;
            }

            for (Player target : plugin.getPlayerData().keySet()) {
                new Finding().removeShulker(target);
                target.sendMessage(plugin.getMessage("disable"));
            }

            plugin.getPlayerData().clear();
            plugin.getBlocksToFind().clear();
            plugin.loadConfigs();

            player.sendMessage(plugin.getMessage("command-reload"));
        } else if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("orefinder.command.give")) {
                sender.sendMessage(plugin.getMessage("no-permission"));
                return true;
            }

            if (args.length != 2) {
                player.sendMessage(plugin.getMessage("command-give-usage"));
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);

            if (target == null) {
                player.sendMessage(plugin.getMessage("command-invalid-player"));
                return true;
            }

            ItemStack itemStack = new ItemStack(Material.getMaterial(plugin.getConfig().getString("item-material")));
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(plugin.getMessage("item-name"));
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemStack.setItemMeta(itemMeta);


            target.getInventory().addItem(itemStack);
            player.sendMessage(plugin.getMessage("command-give-success"));

        } else if (args[0].equalsIgnoreCase("addore")) {
            if (!sender.hasPermission("orefinder.command.addore")) {
                sender.sendMessage(plugin.getMessage("no-permission"));
                return true;
            }

            if (args.length != 3) {
                player.sendMessage(plugin.getMessage("command-addore-usage"));
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);

            if (target == null) {
                player.sendMessage(plugin.getMessage("command-invalid-player"));
                return true;
            }

            ItemStack itemStack = plugin.hasFindingItem(target, false);

            if (itemStack == null) {
                player.sendMessage(plugin.getMessage("command-item-in-hand"));
                return true;
            }

            ItemMeta hookMeta = itemStack.getItemMeta();
            List<String> hookLore = hookMeta.getLore() != null ? new ArrayList<>(hookMeta.getLore()) : new ArrayList<>();

            List<Block> materials = oresList(args[2], player);
            if (materials == null) {
                return true;
            }


            for (Block block : materials) {
                if (hookLore.contains(block.getName())) {
                    sender.sendMessage(plugin.getMessage("command-addore-has-ore"));
                    return true;
                }
                hookLore.add(block.getName());
            }

            hookMeta.setLore(hookLore);
            target.getInventory().remove(itemStack);
            itemStack.setItemMeta(hookMeta);
            target.getInventory().addItem(itemStack);

            player.sendMessage(plugin.getMessage("command-addore-success"));

        } else if (args[0].equalsIgnoreCase("removeore")) {
            if (args.length != 3) {
                player.sendMessage(plugin.getMessage("command-removeore-usage"));
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);

            if (target == null) {
                player.sendMessage(plugin.getMessage("command-invalid-player"));
                return true;
            }

            ItemStack itemStack = plugin.hasFindingItem(target, false);

            if (itemStack == null) {
                player.sendMessage(plugin.getMessage("command-item-in-hand"));
                return true;
            }

            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = itemMeta.getLore() != null ? new ArrayList<>(itemMeta.getLore()) : new ArrayList<>();

            List<String> toRemoveArgs = new ArrayList<>(Arrays.asList(args[2].split(",")));
            List<String> toRemoveUpper = new ArrayList<>();
            for (String block : toRemoveArgs) {
                toRemoveUpper.add(block.toUpperCase());
            }

            List<String> materials = new ArrayList<>();
            for (Block block : plugin.getBlocksToFind().values()) {
                if (lore.contains(block.getName())) {
                    materials.add(block.getMaterial().name());
                }
            }
            List<Block> toRemove = new ArrayList<>();
            for (String materialToRemove : toRemoveUpper) {
                if (!materials.contains(materialToRemove)) {
                    player.sendMessage(plugin.getMessage("command-removeore-not-added"));
                    return true;
                }
                Block block = plugin.getBlocksToFind().get(materialToRemove);

                if (toRemove.contains(block)) {
                    player.sendMessage(plugin.getMessage("command-duplicate"));
                    return true;
                }
                toRemove.add(block);
            }

            for (Block block : toRemove) {
                lore.remove(block.getName());
            }

            itemMeta.setLore(lore);
            target.getInventory().remove(itemStack);
            itemStack.setItemMeta(itemMeta);
            target.getInventory().addItem(itemStack);

            player.sendMessage(plugin.getMessage("command-removeore-success"));

        } else if (args[0].equalsIgnoreCase("findores")) {
            if (!sender.hasPermission("orefinder.command.findores")) {
                sender.sendMessage(plugin.getMessage("no-permission"));
                return true;
            }

            if (args.length != 5) {
                player.sendMessage(plugin.getMessage("command-findores-usage"));
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);

            if (target == null) {
                player.sendMessage(plugin.getMessage("command-invalid-player"));
                return true;
            }

            int radius;
            try {
                radius = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getMessage("command-findores-radius"));
                return true;
            }

            int time = 0;
            if (!args[3].equalsIgnoreCase("none")) {
                try {
                    time = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(plugin.getMessage("command-findores-time"));
                    return true;
                }
            }

            if (plugin.getPlayerData().containsKey(player)) {
                player.sendMessage(plugin.getMessage("command-findores-already-enabled"));
                return true;
            }

            List<Block> materials = oresList(args[4], player);
            if (materials == null) {
                return true;
            }


            PlayerData playerData = new PlayerData(materials, radius, player.getLocation().getChunk(), time);
            plugin.getPlayerData().put(player, playerData);
            new Finding().updateChunks(player, true);

            if (args[3].equalsIgnoreCase("none")) {
                player.sendMessage(plugin.getMessage("enable"));
                playerData.setCooldown(-1);
            } else {
                player.sendMessage(plugin.getMessage("enable-cooldown").replace("%time%", Integer.toString(time)));
            }

            new Finding().disappear(target);

        } else if (args[0].equalsIgnoreCase("disable")) {
            if (!sender.hasPermission("orefinder.command.disable")) {
                sender.sendMessage(plugin.getMessage("no-permission"));
                return true;
            }

            if (args.length != 2) {
                player.sendMessage(plugin.getMessage("command-disable-usage"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage(plugin.getMessage("command-invalid-player"));
                return true;
            }

            if (!plugin.getPlayerData().containsKey(target)) {
                player.sendMessage(plugin.getMessage("command-disable-not-enabled"));
                return true;
            }

            new Finding().removeShulker(target);
            plugin.getPlayerData().remove(target);

            player.sendMessage(plugin.getMessage("command-disable-success"));

        } else if (args[0].equalsIgnoreCase("listores")) {
            if (!sender.hasPermission("orefinder.command.listores")) {
                sender.sendMessage(plugin.getMessage("no-permission"));
                return true;
            }

            StringBuilder stringBuilder = new StringBuilder();
            for (String block : plugin.getBlocksToFind().keySet()) {
                stringBuilder.append(block + ChatColor.GRAY + ", ");
            }
            stringBuilder.setLength(stringBuilder.length() - 2);
            player.sendMessage(plugin.getMessage("command-listores").replace("%blocks%", stringBuilder.toString()));


        } else {
            player.sendMessage(plugin.getMessage("unknown-command"));
        }
        return true;
    }

    private List<Block> oresList(String blocksString, Player player) {
        List<String> enabledBlocks = new ArrayList<>(Arrays.asList(blocksString.split(",")));
        List<Block> blocks = new ArrayList<>();

        List<String> enabledBlocksUpper = new ArrayList<>();
        for (String block : enabledBlocks) {
            enabledBlocksUpper.add(block.toUpperCase());
        }

        for (String block : enabledBlocksUpper) {
            if (!plugin.getBlocksToFind().containsKey(block)) {
                player.sendMessage(plugin.getMessage("command-invalid-ore"));
                return null;
            }

            Block b = plugin.getBlocksToFind().get(block);

            if (blocks.contains(b)) {
                player.sendMessage(plugin.getMessage("command-duplicate"));
                return null;
            }

            blocks.add(b);
        }

        return blocks;
    }
}
