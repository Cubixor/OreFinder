package me.cubixor.orefinder;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;


public final class Orefinder extends JavaPlugin {

    FileConfiguration messagesConfig;
    EntityHider entityHider;
    Team coalOre;
    Team ironOre;
    Team goldOre;
    Team redstoneOre;
    Team lapisOre;
    Team diamondOre;
    Team emeraldOre;
    Team quartzOre;
    private Scoreboard board;
    private File messages = new File(getDataFolder(), "messages.yml");
    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        entityHider = new EntityHider(this, EntityHider.Policy.BLACKLIST);

        getCommand("orefinder").setExecutor(new Command(this));
        getServer().getPluginManager().registerEvents(new Finding(this), this);
        getServer().getPluginManager().registerEvents(new Crafting(this), this);
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        saveResource("messages.yml", false);
        messagesConfig = YamlConfiguration.loadConfiguration(messages);

        board = Bukkit.getScoreboardManager().getMainScoreboard();


        try {
            coalOre = board.registerNewTeam("coalOre");
            coalOre.setColor(ChatColor.BLACK);
        } catch (IllegalArgumentException ex) {
            board.getTeam("coalOre").unregister();
            coalOre = board.registerNewTeam("coalOre");
            coalOre.setColor(ChatColor.BLACK);
        }
        try {
            ironOre = board.registerNewTeam("ironOre");
            ironOre.setColor(ChatColor.GRAY);
        } catch (IllegalArgumentException ex) {
            board.getTeam("ironOre").unregister();
            ironOre = board.registerNewTeam("ironOre");
            ironOre.setColor(ChatColor.GRAY);
        }
        try {
            goldOre = board.registerNewTeam("goldOre");
            goldOre.setColor(ChatColor.GOLD);
        } catch (IllegalArgumentException ex) {
            board.getTeam("goldOre").unregister();
            goldOre = board.registerNewTeam("goldOre");
            goldOre.setColor(ChatColor.GOLD);
        }
        try {
            redstoneOre = board.registerNewTeam("redstoneOre");
            redstoneOre.setColor(ChatColor.RED);
        } catch (IllegalArgumentException ex) {
            board.getTeam("redstoneOre").unregister();
            redstoneOre = board.registerNewTeam("redstoneOre");
            redstoneOre.setColor(ChatColor.RED);
        }
        try {
            lapisOre = board.registerNewTeam("lapisOre");
            lapisOre.setColor(ChatColor.DARK_BLUE);
        } catch (IllegalArgumentException ex) {
            board.getTeam("lapisOre").unregister();
            lapisOre = board.registerNewTeam("lapisOre");
            lapisOre.setColor(ChatColor.DARK_BLUE);
        }
        try {
            diamondOre = board.registerNewTeam("diamondOre");
            diamondOre.setColor(ChatColor.BLUE);
        } catch (IllegalArgumentException ex) {
            board.getTeam("diamondOre").unregister();
            diamondOre = board.registerNewTeam("diamondOre");
            diamondOre.setColor(ChatColor.BLUE);
        }
        try {
            emeraldOre = board.registerNewTeam("emeraldOre");
            emeraldOre.setColor(ChatColor.GREEN);
        } catch (IllegalArgumentException ex) {
            board.getTeam("emeraldOre").unregister();
            emeraldOre = board.registerNewTeam("emeraldOre");
            emeraldOre.setColor(ChatColor.GREEN);
        }
        try {
            quartzOre = board.registerNewTeam("quartzOre");
            quartzOre.setColor(ChatColor.WHITE);
        } catch (IllegalArgumentException ex) {
            board.getTeam("quartzOre").unregister();
            quartzOre = board.registerNewTeam("quartzOre");
            quartzOre.setColor(ChatColor.WHITE);
        }

        if (getConfig().getBoolean("enable-crafting")) {
            ItemStack hook = new ItemStack(Material.TRIPWIRE_HOOK);
            ItemMeta hookMeta = hook.getItemMeta();
            hookMeta.setDisplayName(getMessage("item-name"));
            hookMeta.addEnchant(Enchantment.PIERCING, 1, true);
            hook.setItemMeta(hookMeta);

            NamespacedKey hookKey = new NamespacedKey(this, "hookKey");
            ShapedRecipe hookRecipe = new ShapedRecipe(hookKey, hook);
            hookRecipe.shape("CEC", "CNC", "CCC");
            hookRecipe.setIngredient('C', Material.GOLDEN_CARROT);
            hookRecipe.setIngredient('E', Material.ENDER_EYE);
            hookRecipe.setIngredient('N', Material.NETHER_STAR);
            Bukkit.addRecipe(hookRecipe);
        }
    }

    @Override
    public void onDisable() {
        coalOre.unregister();
        ironOre.unregister();
        goldOre.unregister();
        redstoneOre.unregister();
        lapisOre.unregister();
        diamondOre.unregister();
        emeraldOre.unregister();
        quartzOre.unregister();
    }

    String getMessage(String path) {
        String message = messagesConfig.getString(path);
        String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);
        return coloredMessage;
    }
}
