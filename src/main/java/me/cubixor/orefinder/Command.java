package me.cubixor.orefinder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Command implements CommandExecutor {

    private Orefinder plugin;

    Command(Orefinder of) {
        plugin = of;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("orefinder")) {
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("must-be-player"));
            return true;
        }

        if (!sender.hasPermission("orefinder.command")) {
            sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0 || (args[0] != null && args[0].equalsIgnoreCase("help"))) {
            for (String message : plugin.messagesConfig.getStringList("help")) {
                String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
                sender.sendMessage(coloredMessage);
            }
        } else if (args[0].equalsIgnoreCase("give")) {
            if (args.length != 2) {
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-give-usage"));
                return true;
            }

            ItemStack hook = new ItemStack(Material.TRIPWIRE_HOOK);
            ItemMeta hookMeta = hook.getItemMeta();
            hookMeta.setDisplayName(plugin.getMessage("item-name"));
            hookMeta.addEnchant(Enchantment.PIERCING, 1, true);
            hook.setItemMeta(hookMeta);

            try {
                Bukkit.getPlayer(args[1]).getInventory().addItem(hook);
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-give-success"));
            } catch (Exception ex) {
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-give-invalid-player"));
            }

        } else if (args[0].equalsIgnoreCase("addore")) {
            if (args.length != 3) {
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-addore-usage"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[2]);
            if (!Bukkit.getOnlinePlayers().contains(target)) {
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-addore-invalid-player"));
                return true;
            }

            try {
                target.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equals(plugin.getMessage("item-name"));
            } catch (Exception ex) {
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-addore-item-in-hand"));
                return true;
            }


            ItemStack hook = target.getInventory().getItemInMainHand();
            ItemMeta hookMeta = hook.getItemMeta();
            List<String> hookLore;
            if (hookMeta.getLore() != null) {
                hookLore = new ArrayList<>(hookMeta.getLore());
            } else {
                hookLore = new ArrayList<>();
            }

            if (args[1].equalsIgnoreCase("coal")) {
                if (hookLore.contains(plugin.getMessage("item-lore-coal"))) {
                    sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-addore-has-ore"));
                    return true;
                }
                hookLore.add(plugin.getMessage("item-lore-coal"));
            } else if (args[1].equalsIgnoreCase("iron")) {
                if (hookLore.contains(plugin.getMessage("item-lore-iron"))) {
                    sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-addore-has-ore"));
                    return true;
                }
                hookLore.add(plugin.getMessage("item-lore-iron"));
            } else if (args[1].equalsIgnoreCase("gold")) {
                if (hookLore.contains(plugin.getMessage("item-lore-gold"))) {
                    sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-addore-has-ore"));
                    return true;
                }
                hookLore.add(plugin.getMessage("item-lore-gold"));
            } else if (args[1].equalsIgnoreCase("redstone")) {
                if (hookLore.contains(plugin.getMessage("item-lore-redstone"))) {
                    sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-addore-has-ore"));
                    return true;
                }
                hookLore.add(plugin.getMessage("item-lore-redstone"));
            } else if (args[1].equalsIgnoreCase("lapis")) {
                if (hookLore.contains(plugin.getMessage("item-lore-lapis"))) {
                    sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-addore-has-ore"));
                    return true;
                }
                hookLore.add(plugin.getMessage("item-lore-lapis"));
            } else if (args[1].equalsIgnoreCase("diamond")) {
                if (hookLore.contains(plugin.getMessage("item-lore-diamond"))) {
                    sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-addore-has-ore"));
                    return true;
                }
                hookLore.add(plugin.getMessage("item-lore-diamond"));
            } else if (args[1].equalsIgnoreCase("emerald")) {
                if (hookLore.contains(plugin.getMessage("item-lore-emerald"))) {
                    sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-addore-has-ore"));
                    return true;
                }
                hookLore.add(plugin.getMessage("item-lore-emerald"));
            } else if (args[1].equalsIgnoreCase("quartz")) {
                if (hookLore.contains(plugin.getMessage("item-lore-quartz"))) {
                    sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-addore-has-ore"));
                    return true;
                }
                hookLore.add(plugin.getMessage("item-lore-quartz"));
            } else {
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-addore-invalid-ore"));
                return true;
            }

            hookMeta.setLore(hookLore);
            hook.setItemMeta(hookMeta);
            target.getInventory().setItemInMainHand(hook);

            sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-addore-success"));


        }
        return true;
    }
}
