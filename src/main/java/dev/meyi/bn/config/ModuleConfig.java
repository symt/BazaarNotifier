package dev.meyi.bn.config;

public class ModuleConfig {
    public String name;
    public int x;
    public int y;
    public float scale;

    public ModuleConfig(String name, int x, int y, float scale){
        this.name = name;
        this.x = x;
        this.y = y;
        this.scale = scale;
    }
}
