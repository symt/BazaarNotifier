package dev.meyi.bn;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.meyi.bn.commands.BazaarNotifierCommand;
import dev.meyi.bn.config.Configuration;
import dev.meyi.bn.handlers.ChestTickHandler;
import dev.meyi.bn.handlers.EventHandler;
import dev.meyi.bn.handlers.MouseHandler;
import dev.meyi.bn.handlers.UpdateHandler;
import dev.meyi.bn.json.Order;
import dev.meyi.bn.json.resp.BazaarResponse;
import dev.meyi.bn.modules.ModuleList;
import dev.meyi.bn.utilities.ScheduledEvents;
import dev.meyi.bn.utilities.Utils;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;


@Mod(modid = BazaarNotifier.MODID, version = BazaarNotifier.VERSION)
public class BazaarNotifier {

  public static final String MODID = "BazaarNotifier";
  public static final String VERSION = "1.5.0-beta6";
  public static final String prefix =
      EnumChatFormatting.GOLD + "[" + EnumChatFormatting.YELLOW + "BN" + EnumChatFormatting.GOLD
          + "] " + EnumChatFormatting.RESET;
  public static final String RESOURCE_LOCATION = "https://raw.githubusercontent.com/symt/BazaarNotifier/resources/resources.json";
  public static DecimalFormat df = new DecimalFormat("#,##0.0");
  public static DecimalFormat dfNoDecimal = new DecimalFormat("#,###");

  public static boolean activeBazaar = true;
  public static boolean inBazaar = false;
  public static boolean inBank = false;
  public static boolean forceRender = false;
  public static boolean validApiKey = false;
  public static boolean apiKeyDisabled = true;// Change this if an api key is ever required to access the bazaar again.


  public static ArrayList<Order> orders = new ArrayList<>();
  public static BazaarResponse bazaarDataRaw;
  public static JsonObject playerDataFromAPI = new JsonObject();
  public static ModuleList modules;
  public static Configuration config;
  public static JsonObject resources;

  public static String guiToOpen = "";

  public static JsonObject enchantCraftingList;
  public static BiMap<String, String> bazaarConv = HashBiMap.create();

  public static File configFile;
  public static File resourcesFile;

  public static void resetMod() {
    modules.resetAll();
    orders = new ArrayList<>();
    config = Configuration.createDefaultConfig();
  }

  public static void resetScale() {
    modules.resetScale();
  }

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    File bnDir = new File(event.getModConfigurationDirectory(), "BazaarNotifier");
    bnDir.mkdirs();
    configFile = new File(bnDir, "config.json");
    resourcesFile = new File(bnDir, "resources.json");
    String configString = null;
    String resourcesString = null;
    Gson gson = new Gson();
    try {
      if (configFile.isFile()) {
        configString = new String(Files.readAllBytes(Paths.get(configFile.getPath())));
        config = gson.fromJson(configString, Configuration.class);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      if (resourcesFile.isFile()) {
        resourcesString = new String(Files.readAllBytes(Paths.get(resourcesFile.getPath())));
        resources = gson.fromJson(resourcesString, JsonObject.class);
      } else {
        Reader reader = new InputStreamReader(
            BazaarNotifier.class.getResourceAsStream("/resources.json"), StandardCharsets.UTF_8);
        resources = gson.fromJson(reader, JsonObject.class);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (configString == null) {
      config = Configuration.createDefaultConfig();
      modules = new ModuleList();
      modules.resetAll();
    } else {
      modules = new ModuleList(config);
    }

    try {
      Utils.updateResources();
    } catch (IOException e) {
      System.err.println("Error while getting resources from GitHub");
      e.printStackTrace();
      JsonObject bazaarConversions = resources.getAsJsonObject("bazaarConversions");
      enchantCraftingList = resources.getAsJsonObject("enchantCraftingList");
      bazaarConv = Utils.jsonToBimap(bazaarConversions);
    }
  }


  @Mod.EventHandler
  public void init(FMLInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(new EventHandler());
    MinecraftForge.EVENT_BUS.register(new ChestTickHandler());
    MinecraftForge.EVENT_BUS.register(new MouseHandler());
    MinecraftForge.EVENT_BUS.register(new UpdateHandler());

    ClientCommandHandler.instance.registerCommand(new BazaarNotifierCommand());
    ScheduledEvents.create();

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  Configuration.saveConfig(configFile, config);
                  Utils.saveResources(resourcesFile, resources);
                }));

  }
}
