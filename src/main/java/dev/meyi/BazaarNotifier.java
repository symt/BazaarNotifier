package dev.meyi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.json.JSONObject;
import org.json.JSONTokener;

@Mod(modid = BazaarNotifier.MODID, version = BazaarNotifier.VERSION)
public class BazaarNotifier {

  public static final String MODID = "BazaarNotifier";
  public static final String VERSION = "1.0.0";
  public static String apiKey = "";

  public static boolean activeBazaar = false;
  public static boolean inRequest = false;

  public static String prefix =
      EnumChatFormatting.GOLD + "[BazaarNotifier] " + EnumChatFormatting.RESET;
  public static JSONObject orders = new JSONObject();
  public static JSONObject bazaarData = new JSONObject();
  public static JSONObject bazaarConversionsReversed = new JSONObject(
      new JSONTokener(BazaarNotifier.class.getResourceAsStream("/bazaarConversionsReversed.json")));

  public static File configFile;

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event) {
    configFile = event.getSuggestedConfigurationFile();

    if (configFile.isFile()) {
      try {
        apiKey = new String(Files.readAllBytes(Paths.get(configFile.getPath())));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  @Mod.EventHandler
  public void init(FMLInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(new EventHandler());

    ClientCommandHandler.instance.registerCommand(new BazaarNotifierCommand());

    Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
      if (!inRequest) {
        inRequest = true;
        try {
          if (activeBazaar) {
            try {
              bazaarData = Utils.getBazaarData();
            } catch (IOException e) {
              e.printStackTrace();
            }

            if (orders.length() > 0) {
              Iterator<String> ordersIT = orders.keys();
              while (ordersIT.hasNext()) {
                String key = ordersIT.next();
                for (int i = 0; i < orders.getJSONArray(key).length(); i++) {
                  double price = orders.getJSONArray(key).getJSONObject(i).getDouble("price");
                  if (orders.getJSONArray(key).getJSONObject(i).getString("type").equals("buy")) {
                    // Buy
                    if (bazaarData.getJSONObject(key).getJSONArray("sell_summary").getJSONObject(0)
                        .getDouble("pricePerUnit") - price > 0) {
                      Minecraft.getMinecraft().thePlayer
                          .addChatMessage(new ChatComponentText(BazaarNotifier.prefix +
                              EnumChatFormatting.DARK_RED + orders.getJSONArray(key)
                              .getJSONObject(i)
                              .getString("product") + EnumChatFormatting.RED + " -> "
                              + EnumChatFormatting.DARK_RED + price
                              + EnumChatFormatting.RED + " (you) vs " + EnumChatFormatting.DARK_RED
                              + bazaarData
                              .getJSONObject(key).getJSONArray("sell_summary").getJSONObject(0)
                              .getDouble("pricePerUnit") + EnumChatFormatting.RED + " (top) | " + EnumChatFormatting.GREEN + "BUY ORDER"));
                      orders.getJSONArray(key).remove(i);
                    }
                  } else {
                    // Sell
                    if (price - bazaarData.getJSONObject(key).getJSONArray("buy_summary")
                        .getJSONObject(0)
                        .getDouble("pricePerUnit") > 0) {
                      Minecraft.getMinecraft().thePlayer
                          .addChatMessage(new ChatComponentText(BazaarNotifier.prefix +
                              EnumChatFormatting.DARK_RED + orders.getJSONArray(key)
                              .getJSONObject(i)
                              .getString("product") + EnumChatFormatting.RED + " -> "
                              + EnumChatFormatting.DARK_RED + price
                              + EnumChatFormatting.RED + " (you) vs " + EnumChatFormatting.DARK_RED
                              + bazaarData
                              .getJSONObject(key).getJSONArray("buy_summary").getJSONObject(0)
                              .getDouble("pricePerUnit") + EnumChatFormatting.RED + " (top) | " + EnumChatFormatting.GREEN + "SELL OFFER"));
                      orders.getJSONArray(key).remove(i);
                    }
                  }
                }
                if (BazaarNotifier.orders.getJSONArray(key).length() == 0) {
                  ordersIT.remove();
                }
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        inRequest = false;
      }
    }, 0, 3, TimeUnit.SECONDS);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      Utils.saveConfigFile(configFile, apiKey);
    }));
  }
}
