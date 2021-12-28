package dev.meyi.bn.utilities;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.config.Configuration;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.opengl.GL11;

public class Utils {


  private static String playerUUID = "";

  public static JSONObject getBazaarData() throws IOException {
    HttpClient client = HttpClientBuilder.create().build();
    String apiBit = "";
    if (!BazaarNotifier.apiKeyDisabled) {
      apiBit = "?key=" + BazaarNotifier.apiKey;
    }
    HttpGet request = new HttpGet(
        "https://api.hypixel.net/skyblock/bazaar" + apiBit);
    HttpResponse response = client.execute(request);

    String result = IOUtils.toString(new BufferedReader
        (new InputStreamReader(
            response.getEntity().getContent())));

    return new JSONObject(result).getJSONObject("products");
  }


  public static JSONArray unlockedRecipes() throws IOException {
    if (!BazaarNotifier.apiKey.equals("")) {

      HttpClient client = HttpClientBuilder.create().build();
      if (playerUUID.equals("")) {
        HttpGet request = new HttpGet(
            "https://api.mojang.com/users/profiles/minecraft/" + Minecraft.getMinecraft()
                .getSession().getUsername()); //Chance this to your Username
        HttpResponse response = client.execute(request);

        String uuidResponse = IOUtils
            .toString(new BufferedReader(new InputStreamReader(response.getEntity().getContent())));

        playerUUID = new JSONObject(uuidResponse).getString("id");
      }

      HttpGet request = new HttpGet(
          "https://api.hypixel.net/skyblock/profiles?key=" + BazaarNotifier.apiKey + "&uuid="
              + playerUUID);
      HttpResponse response = client.execute(request);

      String _results = IOUtils
          .toString(new BufferedReader(new InputStreamReader(response.getEntity().getContent())));
      JSONObject results = new JSONObject(_results);
      long lastSaved = 0;
      int profileIndex = 0;

      for (int i = 0; i < results.getJSONArray("profiles").length(); i++) {
        if (results.getJSONArray("profiles").getJSONObject(i).getJSONObject("members")
            .getJSONObject(playerUUID).getLong("last_save") > lastSaved) {
          lastSaved = results.getJSONArray("profiles").getJSONObject(i).getJSONObject("members")
              .getJSONObject(playerUUID).getLong("last_save");
          profileIndex = i;
        }
      }
      BazaarNotifier.playerDataFromAPI = results.getJSONArray("profiles")
          .getJSONObject(profileIndex).getJSONObject("members").getJSONObject(playerUUID);
      JSONArray unlockedCollections = results.getJSONArray("profiles").getJSONObject(profileIndex)
          .getJSONObject("members").getJSONObject(playerUUID).getJSONArray("unlocked_coll_tiers");
      JSONObject slayer = results.getJSONArray("profiles").getJSONObject(profileIndex)
          .getJSONObject("members").getJSONObject(playerUUID).getJSONObject("slayer_bosses");
      if (slayer.getJSONObject("zombie").getJSONObject("claimed_levels").has("level_4")) {
        unlockedCollections.put("zombie_4");
      }
      if (slayer.getJSONObject("spider").getJSONObject("claimed_levels").has("level_4")) {
        unlockedCollections.put("spider_4");
      }
      if (slayer.getJSONObject("wolf").getJSONObject("claimed_levels").has("level_4")) {
        unlockedCollections.put("wolf_4");
      }
      if (slayer.getJSONObject("enderman").getJSONObject("claimed_levels").has("level_2")) {
        unlockedCollections.put("enderman_2");
      }
      return unlockedCollections;
    } else {
      Configuration.collectionCheckDisabled = true;
      return new JSONArray();
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

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static void saveConfigFile(File configFile, String toSave) {
    try {
      if (!configFile.isFile()) {
        configFile.createNewFile();
      }
      Files.write(Paths.get(configFile.getAbsolutePath()),
          toSave.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static JSONArray sortJSONArray(JSONArray jsonArr, String key) {
    List<JSONObject> jsonValues = new ArrayList<>();
    for (int i = 0; i < jsonArr.length(); i++) {
      jsonValues.add(jsonArr.getJSONObject(i));
    }
    jsonValues.sort(new JSONComparator(key));
    JSONArray sortedJsonArray = new JSONArray();
    for (int i = 0; i < jsonArr.length(); i++) {
      sortedJsonArray.put(jsonValues.get(i));
    }
    return sortedJsonArray;
  }

  public static boolean isValidJSONObject(String json) {
    try {
      new JSONObject(json);
    } catch (JSONException e) {
      return false;
    }
    return true;
  }

  public static boolean validateApiKey() throws IOException {
    return new JSONObject(IOUtils.toString(new BufferedReader
        (new InputStreamReader(
            HttpClientBuilder.create().build().execute(new HttpGet(
                "https://api.hypixel.net/key?key=" + BazaarNotifier.apiKey)).getEntity()
                .getContent())))).getBoolean("success");
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
            .format(BazaarNotifier.orders.getJSONObject(i).getInt("startAmount"))
            + EnumChatFormatting.GRAY + "x " + messageColor
            + BazaarNotifier.orders.getJSONObject(i).getString("product")
            + EnumChatFormatting.YELLOW
            + " " + notification + " " + EnumChatFormatting.GRAY + "("
            + messageColor + BazaarNotifier.df.format(price)
            + EnumChatFormatting.GRAY + ")"
    );
  }


  public static void drawCenteredString(String text, int x, int y, int color, float moduleScale) {
    GL11.glScalef(moduleScale, moduleScale, 1);
    Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, color);
    GL11.glScalef((float) Math.pow(moduleScale, -1), (float) Math.pow(moduleScale, -1), 1);
  }

  public static void initializeConfigValues() {
    if (BazaarNotifier.config.has("craftingListLength")) {

      Configuration.craftingListLength = BazaarNotifier.config.getInt("craftingListLength");
      Configuration.suggestionListLength = BazaarNotifier.config.getInt("suggestionListLength");
      Configuration.craftingSortingOption = BazaarNotifier.config.getInt("craftingSortingOption");
      Configuration.showInstantSellProfit = BazaarNotifier.config
          .getBoolean("showInstantSellProfit");
      Configuration.showSellOfferProfit = BazaarNotifier.config.getBoolean("showSellOfferProfit");
      Configuration.showProfitPerMil = BazaarNotifier.config.getBoolean("showProfitPerMil");
      Configuration.collectionCheckDisabled = BazaarNotifier.config
          .getBoolean("collectionChecking");
    } else {
      Configuration.craftingListLength = Defaults.CRAFTING_LIST_LENGTH;
      Configuration.suggestionListLength = Defaults.SUGGESTION_LIST_LENGTH;
      Configuration.craftingSortingOption = Defaults.CRAFTING_SORTING_OPTION;
      Configuration.showInstantSellProfit = Defaults.INSTANT_SELL_PROFIT;
      Configuration.showSellOfferProfit = Defaults.SELL_OFFER_PROFIT;
      Configuration.showProfitPerMil = Defaults.PROFIT_PER_MIL;
      Configuration.collectionCheckDisabled = Defaults.COLLECTION_CHECKING;
    }
  }
}