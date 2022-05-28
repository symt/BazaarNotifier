package dev.meyi.bn.config;

public class ModuleConfig {

  public String name;
  public int x;
  public int y;
  public float scale;
  public boolean active;

  public ModuleConfig(String name, int x, int y, float scale, boolean active) {
    this.name = name;
    this.x = x;
    this.y = y;
    this.scale = scale;
    this.active = active;
  }
}
