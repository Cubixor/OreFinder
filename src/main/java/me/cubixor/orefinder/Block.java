package me.cubixor.orefinder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class Block {

    private final Team team;
    private final Material material;
    private final String name;


    public Block(String material, String name, ChatColor chatColor) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team;
        if (scoreboard.getTeam(material) != null) {
            scoreboard.getTeam(material).unregister();
        }

        team = scoreboard.registerNewTeam(material);
        if (!VersionUtils.is12()) {
            team.setColor(chatColor);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "scoreboard teams option " + material + " color " + chatColor.name());
                }
            }.runTaskLater(OreFinder.getInstance(), 1);
        }

        this.material = Material.getMaterial(material);
        this.name = name;
        this.team = team;
    }


    public Team getTeam() {
        return team;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }
}
