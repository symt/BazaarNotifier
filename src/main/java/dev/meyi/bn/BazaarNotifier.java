package dev.meyi.bn;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.meyi.bn.commands.BazaarNotifierCommand;
import dev.meyi.bn.config.Configuration;
import dev.meyi.bn.handlers.ChestTickHandler;
import dev.meyi.bn.handlers.EventHandler;
import dev.meyi.bn.handlers.MouseHandler;
import dev.meyi.bn.handlers.UpdateHandler;
import dev.meyi.bn.modules.ModuleList;
import dev.meyi.bn.utilities.Order;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;


@Mod(modid = BazaarNotifier.MODID, version = BazaarNotifier.VERSION)
public class BazaarNotifier {

  public static final String MODID = "BazaarNotifier";
  public static final String VERSION = "1.5.0";
  public static final String prefix =
      EnumChatFormatting.GOLD + "[" + EnumChatFormatting.YELLOW + "BN" + EnumChatFormatting.GOLD
          + "] " + EnumChatFormatting.RESET;
  public static final String RESOURCE_LOCATION = "https://raw.githubusercontent.com/Detlev1/BazaarNotifier/master/src/main/resources/resources.json";
  public static DecimalFormat df = new DecimalFormat("#,##0.0");
  public static DecimalFormat dfNoDecimal = new DecimalFormat("#,###");

  public static boolean activeBazaar = true;
  public static boolean inBazaar = false;
  public static boolean inBank = false;
  public static boolean forceRender = false;
  public static boolean validApiKey = false;
  public static boolean apiKeyDisabled = true;// Change this if an api key is ever required to access the bazaar again.


  public static ArrayList<Order> orders = new ArrayList<>();
  public static JsonObject bazaarDataRaw = new JsonObject();
  public static JsonObject bazaarCache = new JsonObject();
  public static JsonArray bazaarDataFormatted = new JsonArray();
  public static JsonObject playerDataFromAPI = new JsonObject();
  public static ModuleList modules;
  public static Configuration config;


  public static JsonObject enchantCraftingList;
  public static BiMap<String, String> bazaarConv = HashBiMap.create();

  public static JsonObject bazaarConversions = new JsonParser().parse(
          new InputStreamReader(Objects.requireNonNull(BazaarNotifier.class.getResourceAsStream
                  ("/bazaarConversions.json")), StandardCharsets.UTF_8)).getAsJsonObject();
  public static JsonObject bazaarConversionsReversed = new JsonParser().parse(
          new InputStreamReader(Objects.requireNonNull(BazaarNotifier.class.getResourceAsStream
                  ("/bazaarConversionsReversed.json")), StandardCharsets.UTF_8)).getAsJsonObject();
  public static JsonObject enchantCraftingList = new JsonParser().parse(
          new InputStreamReader(Objects.requireNonNull(BazaarNotifier.class.getResourceAsStream
                  ("/enchantCraftingList.json")), StandardCharsets.UTF_8)).getAsJsonObject();


  public static File configFile;


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
    configFile = event.getSuggestedConfigurationFile();
    String configString = null;
    Gson gson = new Gson();
    try {
      if (configFile.isFile()) {
        configString = new String(Files.readAllBytes(Paths.get(configFile.getPath())));
        config = gson.fromJson(configString, Configuration.class);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (configString == null) {
      config = Configuration.createDefaultConfig();
      modules = new ModuleList();
      modules.resetAll();
    }else {
      modules = new ModuleList(config);
    }

    try{
      Utils.updateResources();
    }catch (IOException e){
      System.out.println("Error while getting resources from GitHub");
      if(configString != null){
        JsonObject resources = config.resources;
        JsonObject bazaarConversions = resources.getAsJsonObject("bazaarConversions");
        enchantCraftingList = resources.getAsJsonObject("enchantCraftingList");
        bazaarConv = Utils.jsonToBimap(bazaarConversions);
      }
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
                    () -> config.saveConfig(configFile , config)));

  }
}