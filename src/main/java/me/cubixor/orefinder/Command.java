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
import java.util.Arrays;
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
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-invalid-player"));
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


        } else if (args[0].equalsIgnoreCase("findores")) {
            if (args.length != 5) {
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-findores-usage"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (!Bukkit.getOnlinePlayers().contains(target)) {
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-invalid-player"));
                return true;
            }

            int radius = 0;
            try {
                radius = Integer.parseInt(args[2]);
            } catch (Exception ex) {
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-findores-radius"));
                return true;
            }

            int time = 0;
            if (!args[3].equalsIgnoreCase("none")) {
                try {
                    time = Integer.parseInt(args[3]);
                } catch (Exception ex) {
                    sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-findores-time"));
                    return true;
                }
            }

            List<String> enabledOres = new ArrayList<>(Arrays.asList(args[4].split(",")));
            List<Material> materials = new ArrayList<>();
            boolean duplicated = false;
            for (String ore : enabledOres) {
                if (ore.equalsIgnoreCase("coal")) {
                    if (materials.contains(Material.COAL_ORE)) {
                        duplicated = true;
                        break;
                    }
                    materials.add(Material.COAL_ORE);
                } else if (ore.equalsIgnoreCase("iron")) {
                    if (materials.contains(Material.IRON_ORE)) {
                        duplicated = true;
                        break;
                    }
                    materials.add(Material.IRON_ORE);
                } else if (ore.equalsIgnoreCase("gold")) {
                    if (materials.contains(Material.GOLD_ORE)) {
                        duplicated = true;
                        break;
                    }
                    materials.add(Material.GOLD_ORE);
                } else if (ore.equalsIgnoreCase("redstone")) {
                    if (materials.contains(Material.REDSTONE_ORE)) {
                        duplicated = true;
                        break;
                    }
                    materials.add(Material.REDSTONE_ORE);
                } else if (ore.equalsIgnoreCase("lapis")) {
                    if (materials.contains(Material.LAPIS_ORE)) {
                        duplicated = true;
                        break;
                    }
                    materials.add(Material.LAPIS_ORE);
                } else if (ore.equalsIgnoreCase("diamond")) {
                    if (materials.contains(Material.DIAMOND_ORE)) {
                        duplicated = true;
                        break;
                    }
                    materials.add(Material.DIAMOND_ORE);
                } else if (ore.equalsIgnoreCase("emerald")) {
                    if (materials.contains(Material.EMERALD_ORE)) {
                        duplicated = true;
                        break;
                    }
                    materials.add(Material.EMERALD_ORE);
                } else if (ore.equalsIgnoreCase("quartz")) {
                    if (materials.contains(Material.NETHER_QUARTZ_ORE)) {
                        duplicated = true;
                        break;
                    }
                    materials.add(Material.NETHER_QUARTZ_ORE);
                } else {
                    sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-findores-invalid-ore"));
                    return true;
                }
            }
            if (duplicated) {
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-findores-duplicate"));
                return true;
            }

            if (plugin.cooldown.containsKey(target.getUniqueId().toString()) || plugin.sneaking.contains(target.getUniqueId().toString())) {
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-findores-already-enabled"));
                return true;
            }

            plugin.materialOres.put(target.getUniqueId().toString(), materials);

            plugin.chunk.put(target.getUniqueId().toString(), target.getLocation().getChunk());
            plugin.radius.put(target.getUniqueId().toString(), radius);


            if (args[3].equalsIgnoreCase("none")) {
                plugin.sneaking.add(target.getUniqueId().toString());
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("enable"));
            } else {
                plugin.cooldown.put(target.getUniqueId().toString(), time);
                (new Finding(plugin)).disappear(target);
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("enable-cooldown").replace("%time%", plugin.cooldown.get(target.getUniqueId().toString()).toString()));
            }


        } else if (args[0].equalsIgnoreCase("disable")) {
            if (args.length != 2) {
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-disable-usage"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (!Bukkit.getOnlinePlayers().contains(target)) {
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-invalid-player"));
                return true;
            }

            if (!plugin.cooldown.containsKey(target.getUniqueId().toString()) && !plugin.sneaking.contains(target.getUniqueId().toString())) {
                sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("command-disable-not-enabled"));
                return true;
            }
            plugin.cooldown.remove(target.getUniqueId().toString());

            if (plugin.sneaking.contains(target.getUniqueId().toString())) {
                plugin.sneaking.remove(target.getUniqueId().toString());
                plugin.radius.remove(target.getUniqueId().toString());
                plugin.chunk.remove(target.getUniqueId().toString());
                (new Finding(plugin)).removeShulker(target.getUniqueId().toString());

            }
            plugin.materialOres.remove(target.getUniqueId().toString());

            sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("disable"));

        } else {
            sender.sendMessage(plugin.getMessage("prefix") + plugin.getMessage("unknown-command"));
        }
        return true;
    }
}
