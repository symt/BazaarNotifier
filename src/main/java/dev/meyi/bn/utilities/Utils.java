package dev.meyi.bn.utilities;

import dev.meyi.bn.BazaarNotifier;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

public class Utils {

  public static JSONObject getBazaarData() throws IOException {
    HttpClient client = HttpClientBuilder.create().build();
    HttpGet request = new HttpGet(
        "https://api.hypixel.net/skyblock/bazaar?key=" + BazaarNotifier.apiKey);
    HttpResponse response = client.execute(request);

    String result = IOUtils.toString(new BufferedReader
        (new InputStreamReader(
            response.getEntity().getContent())));

    return new JSONObject(result).getJSONObject("products");
  }

  public static String stripString(String s) {
    char[] nonValidatedString = StringUtils.stripControlCodes(s).toCharArray();
    StringBuilder validated = new StringBuilder();
    for (char a : nonValidatedString) {
      if ((int) a < 127 && (int) a > 20) {
        validated.append(a);
      }
    }
    return validated.toString();
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

  public static boolean updateChecker() {


    return false;
  }
}
