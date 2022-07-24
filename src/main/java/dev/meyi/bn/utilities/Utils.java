package dev.meyi.bn.utilities;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.json.resp.BazaarItem;
import dev.meyi.bn.json.resp.BazaarResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.lwjgl.opengl.GL11;

public class Utils {


  private static String playerUUID = "";
  private static long recipeCooldown = 0;

  public static BazaarResponse getBazaarData() throws IOException {
    Gson gson = new Gson();
    HttpClient client = HttpClientBuilder.create().build();
    String apiBit = "";
    if (!BazaarNotifier.apiKeyDisabled) {
      apiBit = "?key=" + BazaarNotifier.config.api;
    }
    HttpGet request = new HttpGet(
        "https://api.hypixel.net/skyblock/bazaar" + apiBit);
    HttpResponse response = client.execute(request);

    String result = IOUtils.toString(new BufferedReader
        (new InputStreamReader(
            response.getEntity().getContent())));

    JsonObject j = gson.fromJson(result, JsonObject.class).getAsJsonObject();
    return new BazaarResponse(j.get("success").getAsBoolean(), j.get("lastUpdated").getAsLong(),
        jsonToMap(j.getAsJsonObject("products")));
  }


  public static List<String> unlockedRecipes() throws IOException {
    Gson gson = new Gson();
    if (recipeCooldown + 300000 > System.currentTimeMillis()) {
      return null;
    } else {
      recipeCooldown = System.currentTimeMillis();
    }
    if (!BazaarNotifier.validApiKey) {
      BazaarNotifier.validApiKey = validateApiKey();
    }
    if (!BazaarNotifier.config.api.equals("") && BazaarNotifier.validApiKey) {

      HttpClient client = HttpClientBuilder.create().build();
      if (playerUUID.equals("")) {
        HttpGet request = new HttpGet(
            "https://api.mojang.com/users/profiles/minecraft/" + Minecraft.getMinecraft()
                .getSession().getUsername()); //Chance this to your Username
        HttpResponse response = client.execute(request);

        String uuidResponse = IOUtils
            .toString(new BufferedReader(new InputStreamReader(response.getEntity().getContent())));

        try {
          playerUUID = gson.fromJson(uuidResponse, JsonObject.class).getAsJsonObject().get("id")
              .getAsString();
        } catch (Exception e) {
          return null;
        }
      }

      HttpGet request = new HttpGet(
          "https://api.hypixel.net/skyblock/profiles?key=" + BazaarNotifier.config.api + "&uuid="
              + playerUUID);
      HttpResponse response = client.execute(request);

      String _results = IOUtils
          .toString(new BufferedReader(new InputStreamReader(response.getEntity().getContent())));
      JsonObject results = gson.fromJson(_results, JsonObject.class);
      long lastSaved = 0;
      int profileIndex = 0;
      if (!results.get("success").getAsBoolean() || !results.has("profiles")) {
        return null;
      }

      for (int i = 0; i < results.get("profiles").getAsJsonArray().size(); i++) {
        if (results.get("profiles").getAsJsonArray().get(i).getAsJsonObject().get("members")
            .getAsJsonObject()
            .get(playerUUID).getAsJsonObject().has("last_save")) {
          if (results.get("profiles").getAsJsonArray().get(i).getAsJsonObject().get("members")
              .getAsJsonObject()
              .get(playerUUID).getAsJsonObject().get("last_save").getAsLong() > lastSaved) {
            lastSaved = results.get("profiles").getAsJsonArray().get(i).getAsJsonObject()
                .get("members").getAsJsonObject()
                .get(playerUUID).getAsJsonObject().get("last_save").getAsLong();
            profileIndex = i;
          }
        }
      }
      BazaarNotifier.playerDataFromAPI = results.get("profiles").getAsJsonArray()
          .get(profileIndex).getAsJsonObject().get("members").getAsJsonObject().get(playerUUID)
          .getAsJsonObject();
      if (!results.getAsJsonArray("profiles").get(profileIndex).getAsJsonObject()
          .get("members").getAsJsonObject().get(playerUUID).getAsJsonObject()
          .has("unlocked_coll_tiers")
          || !results.getAsJsonArray("profiles").get(profileIndex).getAsJsonObject()
          .get("members").getAsJsonObject().get(playerUUID).getAsJsonObject()
          .has("slayer_bosses")) {
        System.out.println("could not load unlocked collection tiers from API");
        return null;
      }
      List<String> unlockedCollections = new ArrayList<>();

      results.getAsJsonArray("profiles").get(profileIndex).getAsJsonObject()
          .get("members").getAsJsonObject().get(playerUUID).getAsJsonObject()
          .get("unlocked_coll_tiers")
          .getAsJsonArray().forEach(cmd -> unlockedCollections.add(cmd.getAsString()));

      JsonObject slayer = results.getAsJsonArray("profiles").get(profileIndex).getAsJsonObject()
          .get("members").getAsJsonObject().get(playerUUID).getAsJsonObject().get("slayer_bosses")
          .getAsJsonObject();
      Set<Map.Entry<String, JsonElement>> set = slayer.entrySet();
      for (Map.Entry<String, JsonElement> entry : set) {
        Set<Map.Entry<String, JsonElement>> set2 = slayer.getAsJsonObject(entry.getKey())
            .getAsJsonObject("claimed_levels").entrySet();
        for (Map.Entry<String, JsonElement> entry2 : set2) {
          unlockedCollections.add(entry.getKey() + entry2.getKey().replace("level", ""));
        }
      }
      return unlockedCollections;
    } else {
      BazaarNotifier.config.collectionCheckDisabled = true;

      return null;
    }
  }


  public static boolean isInteger(String s) {
    return isInteger(s, 10);
  }

  public static boolean isInteger(String s, int radix) {
    if (s.isEmpty()) {
      return false;
    }
    for (int i = 0; i < s.length(); i++) {
      if (i == 0 && s.charAt(i) == '-') {
        if (s.length() == 1) {
          return false;
        } else {
          continue;
        }
      }
      if (Character.digit(s.charAt(i), radix) < 0) {
        return false;
      }
    }
    return true;
  }


  public static JsonArray sortJSONArray(JsonArray jsonArr, String key) {
    List<JsonObject> jsonValues = new ArrayList<>();
    for (int i = 0; i < jsonArr.size(); i++) {
      jsonValues.add(jsonArr.get(i).getAsJsonObject());
    }
    jsonValues.sort(new JSONComparator(key));
    JsonArray sortedJsonArray = new JsonArray();
    for (int i = 0; i < jsonArr.size(); i++) {
      sortedJsonArray.add(jsonValues.get(i));
    }
    return sortedJsonArray;
  }

  public static boolean isJSONValid(String jsonInString) {
    Gson gson = new Gson();
    try {
      gson.fromJson(jsonInString, Object.class);
      return true;
    } catch (com.google.gson.JsonSyntaxException ex) {
      return false;
    }
  }

  public static boolean validateApiKey(String key) throws IOException {
    Gson gson = new Gson();
    return gson.fromJson(IOUtils.toString(new BufferedReader
        (new InputStreamReader(
            HttpClientBuilder.create().build().execute(new HttpGet(
                "https://api.hypixel.net/key?key=" + key)).getEntity()
                .getContent()))), JsonObject.class).getAsJsonObject().get("success").getAsBoolean();
  }

  public static boolean validateApiKey() throws IOException {
    return validateApiKey(BazaarNotifier.config.api);
  }


  /**
   * @param price price per unit of order
   * @param i index of order
   * @param type Buy Order or Sell Offer (sets message color to dark purple vs blue)
   * @param notification yellow notification message
   * @return ChatComponentText completed message
   */
  public static ChatComponentText chatNotification(double price, int i, String type,
      String notification) {
    EnumChatFormatting messageColor =
        (notification.equalsIgnoreCase("REVIVED") ? EnumChatFormatting.GREEN
            : type.equalsIgnoreCase("Buy Order") ? EnumChatFormatting.DARK_PURPLE
                : EnumChatFormatting.BLUE);
    return new ChatComponentText(
        messageColor + type
            + EnumChatFormatting.GRAY + " for "
            + messageColor + BazaarNotifier.dfNoDecimal
            .format(BazaarNotifier.orders.get(i).startAmount)
            + EnumChatFormatting.GRAY + "x " + messageColor
            + BazaarNotifier.orders.get(i).product
            + EnumChatFormatting.YELLOW
            + " " + notification + " " + EnumChatFormatting.GRAY + "("
            + messageColor + BazaarNotifier.df.format(price)
            + EnumChatFormatting.GRAY + ")"
    );
  }


  public static void drawCenteredString(String text, int x, int y, int color, float moduleScale) {
    x = (int) (x / moduleScale + 200 / 4);
    y = (int) (y / moduleScale + (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * 6));
    GL11.glScalef(moduleScale, moduleScale, 1);
    Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, color);
    GL11.glScalef((float) Math.pow(moduleScale, -1), (float) Math.pow(moduleScale, -1), 1);
  }

  public static void updateResources() throws IOException {
    Gson gson = new Gson();
    String result;
    HttpGet request;
    HttpResponse response;
    HttpClient client = HttpClientBuilder.create().build();
    request = new HttpGet(BazaarNotifier.RESOURCE_LOCATION);
    response = client.execute(request);
    result = IOUtils
        .toString(new BufferedReader(new InputStreamReader(response.getEntity().getContent())));
    BazaarNotifier.resources = gson.fromJson(result, JsonObject.class).getAsJsonObject();
    BazaarNotifier.bazaarConv = jsonToBimap(
        BazaarNotifier.resources.getAsJsonObject("bazaarConversions"));
    BazaarNotifier.enchantCraftingList = BazaarNotifier.resources
        .getAsJsonObject("enchantCraftingList");
  }

  public static BiMap<String, String> jsonToBimap(JsonObject jsonObject) {
    BiMap<String, String> b = HashBiMap.create();
    Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
    for (Map.Entry<String, JsonElement> entry : entries) {
      try {
        b.put(entry.getKey(), jsonObject.get(entry.getKey()).getAsString());
      } catch (IllegalArgumentException ignored) {
      }
    }
    return b;
  }

  public static Map<String, BazaarItem> jsonToMap(JsonObject bazaarProducts) {
    Map<String, BazaarItem> products = new HashMap<>();
    for (Map.Entry<String, JsonElement> keys : bazaarProducts.entrySet()) {
      String itemName = keys.getKey();

      List<BazaarItem.BazaarSubItem> sellSummary = new ArrayList<>();
      for (int i = 0;
          i < bazaarProducts.getAsJsonObject(itemName).getAsJsonArray("sell_summary").size(); i++) {
        JsonObject j = bazaarProducts.getAsJsonObject(itemName).getAsJsonArray("sell_summary")
            .get(i).getAsJsonObject();
        sellSummary.add(new BazaarItem.BazaarSubItem(j.get("amount").getAsInt(),
            j.get("pricePerUnit").getAsDouble(), j.get("orders").getAsInt()));
      }
      List<BazaarItem.BazaarSubItem> buySummary = new ArrayList<>();
      for (int i = 0;
          i < bazaarProducts.getAsJsonObject(itemName).getAsJsonArray("buy_summary").size(); i++) {
        JsonObject j = bazaarProducts.getAsJsonObject(itemName).getAsJsonArray("buy_summary").get(i)
            .getAsJsonObject();
        buySummary.add(new BazaarItem.BazaarSubItem(j.get("amount").getAsInt(),
            j.get("pricePerUnit").getAsDouble(), j.get("orders").getAsInt()));
      }

      JsonObject JsonQuickStatus = bazaarProducts.getAsJsonObject(itemName)
          .getAsJsonObject("quick_status");
      BazaarItem.BazaarItemSummary quickStatus = new BazaarItem.BazaarItemSummary(itemName,
          JsonQuickStatus.get("sellPrice").getAsDouble(),
          JsonQuickStatus.get("sellVolume").getAsLong(),
          JsonQuickStatus.get("sellMovingWeek").getAsLong(),
          JsonQuickStatus.get("sellOrders").getAsLong(),
          JsonQuickStatus.get("buyPrice").getAsDouble(),
          JsonQuickStatus.get("buyVolume").getAsLong(),
          JsonQuickStatus.get("buyMovingWeek").getAsLong(),
          JsonQuickStatus.get("buyOrders").getAsLong());

      products.put(itemName, new BazaarItem(itemName, sellSummary, buySummary, quickStatus));
    }
    return products;
  }


  public static void saveResources(File file, JsonObject resources) {
    Gson gson = new Gson();
    try {
      if (!file.isFile()) {
        file.createNewFile();
      }
      Files.write(Paths.get(file.getAbsolutePath()),
          gson.toJson(resources).getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}