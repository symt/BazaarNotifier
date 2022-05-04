package dev.meyi.bn.utilities;

import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.modules.Module;
import dev.meyi.bn.modules.ModuleName;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {

  public static JSONObject getBazaarData() throws IOException {
    String apiBit = "";
    if (!BazaarNotifier.apiKeyDisabled) {
      apiBit = "?key=" + BazaarNotifier.apiKey;
    }

    HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.hypixel.net/skyblock/bazaar" + apiBit).openConnection();
    connection.setSSLSocketFactory(BazaarNotifier.sslSocketFactory);
    connection.connect();

    String result = IOUtils.toString(new BufferedReader
        (new InputStreamReader(
            connection.getInputStream())));


    return new JSONObject(result).getJSONObject("products");
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

  public static double round(double value, int places) {
    if (places < 0) {
      throw new IllegalArgumentException();
    }

    BigDecimal bd = new BigDecimal(Double.toString(value));
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
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
    List<JSONObject> jsonValues = new ArrayList<JSONObject>();
    for (int i = 0; i < jsonArr.length(); i++) {
      jsonValues.add(jsonArr.getJSONObject(i));
    }
    Collections.sort(jsonValues, new JSONComparator(key));
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

  public static JSONObject initializeConfig() {
    JSONObject newConfig = new JSONObject().put("api", BazaarNotifier.apiKey)
        .put("version", BazaarNotifier.VERSION);

    JSONArray modules = new JSONArray();

    for (ModuleName value : ModuleName.values()) {
      Module m = value.returnDefaultModule();
      if (m != null) {
        modules.put(m.generateModuleConfig());
      }
    }

    BazaarNotifier.validApiKey = false;

    return newConfig.put("modules", modules);
  }

  public static boolean validateApiKey() throws IOException {
    return new JSONObject(IOUtils.toString(new BufferedReader
        (new InputStreamReader(
            HttpClientBuilder.create().build().execute(new HttpGet(
                "https://api.hypixel.net/key?key=" + BazaarNotifier.apiKey)).getEntity()
                .getContent())))).getBoolean("success");
  }


  /**
   * @param key order key
   * @param price price per unit of order
   * @param i index of order
   * @param type Buy Order or Sell Offer (sets message color to dark purple vs blue)
   * @param notification yellow notification message
   * @return ChatComponentText completed message
   */
  public static ChatComponentText chatNotification(String key, double price, int i, String type,
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

  public static void drawCenteredString(String text, int x, int y, int color, double scale) {
    Minecraft.getMinecraft().fontRendererObj.drawString(text,
        (int) (x / scale) - Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) / 2,
        (int) (y / scale), color);
  }
}
