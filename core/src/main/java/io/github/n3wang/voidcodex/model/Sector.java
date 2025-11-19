package io.github.n3wang.voidcodex.model;

/**
 * Represents a sector the player travels through.
 */
public class Sector {
    private int index;
    private Biome biome;
    private boolean explored;
    private boolean codexRead;

    public Sector(int index, Biome biome) {
        this.index = index;
        this.biome = biome;
        this.explored = false;
        this.codexRead = false;
    }

    public int getIndex() { return index; }
    public Biome getBiome() { return biome; }
    public boolean isExplored() { return explored; }
    public void setExplored(boolean explored) { this.explored = explored; }
    public boolean isCodexRead() { return codexRead; }
    public void setCodexRead(boolean read) { this.codexRead = read; }
}

