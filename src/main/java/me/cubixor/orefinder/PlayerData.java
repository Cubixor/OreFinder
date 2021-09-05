package me.cubixor.orefinder;

import org.bukkit.Chunk;
import org.bukkit.entity.Shulker;

import java.util.ArrayList;
import java.util.List;

public class PlayerData {

    private final List<Shulker> markedBlocks = new ArrayList<>();
    private final List<Block> blocksToFind = new ArrayList<>();
    private final int radius;
    private Chunk chunk;
    private int cooldown;

    public PlayerData(List<Block> blocksToFind, int radius, Chunk chunk, int cooldown) {
        this.radius = radius;
        this.chunk = chunk;
        this.cooldown = cooldown;
        this.blocksToFind.addAll(blocksToFind);
    }

    public List<Shulker> getMarkedBlocks() {
        return markedBlocks;
    }

    public List<Block> getBlocksToFind() {
        return blocksToFind;
    }

    public int getRadius() {
        return radius;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }
}
