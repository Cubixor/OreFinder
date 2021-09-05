package me.cubixor.orefinder;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public final class OreFinder extends JavaPlugin {

    private static OreFinder instance;
    private final File messagesFile = new File(getDataFolder(), "messages.yml");
    private final HashMap<String, Block> blocksToFind = new HashMap<>();
    private final HashMap<Player, PlayerData> playerData = new HashMap<>();
    private FileConfiguration messagesConfig;

    public static OreFinder getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        VersionUtils.initialize();

        getCommand("orefinder").setExecutor(new Command());
        getCommand("orefinder").setTabCompleter(new TabCompleter());
        getServer().getPluginManager().registerEvents(new Finding(), this);

        loadConfigs();

        new Updater(this, 73688).runUpdaterTask();
    }

    public void loadConfigs() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        reloadConfig();


        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        for (String material : getConfig().getConfigurationSection("blocks").getKeys(false)) {
            Block block = new Block(material,
                    ChatColor.translateAlternateColorCodes('&', getConfig().getString("blocks." + material + ".name")),
                    ChatColor.getByChar(getConfig().getString("blocks." + material + ".chat-color")));
            blocksToFind.put(material, block);
        }
    }

    @Override
    public void onDisable() {
        for (Player player : getPlayerData().keySet()) {
            new Finding().removeShulker(player);
            getPlayerData().remove(player);
        }
        for (Block block : getBlocksToFind().values()) {
            block.getTeam().unregister();
        }
    }

    public String getMessage(String path) {
        String message = messagesConfig.getString(path);
        String prefix = messagesConfig.getString("prefix");
        String replaced = message.replace("%prefix%", prefix);
        return ChatColor.translateAlternateColorCodes('&', replaced);
    }

    public List<String> getMessageList(String path) {
        List<String> message = new ArrayList<>(messagesConfig.getStringList(path));
        List<String> finalMessage = new ArrayList<>();
        for (String s : message) {
            finalMessage.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        return finalMessage;
    }

    public ItemStack hasFindingItem(Player target, boolean requireMainHand) {
        if (isFindingItem(target.getInventory().getItemInMainHand())) {
            return target.getInventory().getItemInMainHand();
        } else if (requireMainHand) {
            return null;
        }

        for (ItemStack itemStack : target.getInventory().getContents()) {
            if (isFindingItem(itemStack)) {
                return itemStack;
            }
        }
        return null;
    }

    private boolean isFindingItem(ItemStack itemStack) {
        return itemStack.getType().equals(Material.getMaterial(getConfig().getString("item-material"))) &&
                itemStack.getItemMeta().getDisplayName().equals(getMessage("item-name"));
    }

    public Block getBlockByMaterial(Material material) {
        for (Block block : getBlocksToFind().values()) {
            if (block.getMaterial().equals(material)) {
                return block;
            }
        }
        return null;
    }

    public HashMap<String, Block> getBlocksToFind() {
        return blocksToFind;
    }

    public HashMap<Player, PlayerData> getPlayerData() {
        return playerData;
    }
}
