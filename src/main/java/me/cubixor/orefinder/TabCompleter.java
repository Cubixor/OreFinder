package me.cubixor.orefinder;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {

    private final OreFinder plugin;

    public TabCompleter() {
        this.plugin = OreFinder.getInstance();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equals("orefinder")) {
            return null;
        }

        List<String> result = new ArrayList<>();
        switch (args.length) {
            case 1: {
                if ("help".startsWith(args[0]) && sender.hasPermission("orefinder.command.help")) {
                    result.add("help");
                }

                if ("reload".startsWith(args[0]) && sender.hasPermission("orefinder.command.reload")) {
                    result.add("reload");
                }

                if ("give".startsWith(args[0]) && sender.hasPermission("orefinder.command.give")) {
                    result.add("give");
                }

                if ("addore".startsWith(args[0]) && sender.hasPermission("orefinder.command.addore")) {
                    result.add("addore");
                }
                if ("removeore".startsWith(args[0]) && sender.hasPermission("orefinder.command.removeore")) {
                    result.add("removeore");
                }

                if ("findores".startsWith(args[0]) && sender.hasPermission("orefinder.command.findores")) {
                    result.add("findores");
                }

                if ("disable".startsWith(args[0]) && sender.hasPermission("orefinder.command.disable")) {
                    result.add("disable");
                }

                if ("listores".startsWith(args[0]) && sender.hasPermission("orefinder.command.listores")) {
                    result.add("listores");
                }
                break;
            }
            case 2: {
                if ((args[0].equalsIgnoreCase("give") && sender.hasPermission("orefinder.command.give")) ||
                        (args[0].equalsIgnoreCase("addore") && sender.hasPermission("orefinder.command.addore")) ||
                        (args[0].equalsIgnoreCase("removeore") && sender.hasPermission("orefinder.command.removeore")) ||
                        (args[0].equalsIgnoreCase("findores") && sender.hasPermission("orefinder.command.findores")) ||
                        (args[0].equalsIgnoreCase("disable") && sender.hasPermission("orefinder.command.disable"))) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                            result.add(p.getName());
                    }
                }
                break;
            }
            case 3: {
                if ((args[0].equalsIgnoreCase("addore") && sender.hasPermission("orefinder.command.addore")) ||
                        (args[0].equalsIgnoreCase("removeore") && sender.hasPermission("orefinder.command.removeore"))) {
                    for (String material : plugin.getBlocksToFind().keySet()) {
                        if (material.toLowerCase().startsWith(args[2].toLowerCase())) {
                            result.add(material);
                        }
                    }
                }
                break;
            }
            case 4: {
                if ((args[0].equalsIgnoreCase("findores") && sender.hasPermission("orefinder.command.findores"))) {
                    if ("none".startsWith(args[3])) {
                        result.add("none");
                    }
                }
                break;
            }
            case 5: {
                if ((args[0].equalsIgnoreCase("findores") && sender.hasPermission("orefinder.command.findores"))) {
                    for (String material : plugin.getBlocksToFind().keySet()) {
                        if (material.toLowerCase().startsWith(args[4].toLowerCase())) {
                            result.add(material);
                        }
                    }
                }
                break;
            }
        }
        return result;
    }
}
