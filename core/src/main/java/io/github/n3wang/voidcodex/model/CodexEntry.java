package io.github.n3wang.voidcodex.model;

/**
 * Represents a page in the Void Codex.
 */
public class CodexEntry {
    private String id;
    private String title;
    private String content;
    private BiomeType relatedBiome;
    private boolean revealsBiomeInfo;
    private String biomeHint;

    public CodexEntry(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.revealsBiomeInfo = false;
    }

    public CodexEntry(String id, String title, String content, BiomeType relatedBiome, String biomeHint) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.relatedBiome = relatedBiome;
        this.revealsBiomeInfo = true;
        this.biomeHint = biomeHint;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public BiomeType getRelatedBiome() { return relatedBiome; }
    public boolean revealsBiomeInfo() { return revealsBiomeInfo; }
    public String getBiomeHint() { return biomeHint; }
}

