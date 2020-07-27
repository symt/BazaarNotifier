package dev.meyi.bn.utilities;

import java.util.Comparator;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONComparator implements Comparator<JSONObject> {

  String key = "";

  public JSONComparator(String key) {
    this.key = key;
  }

  @Override
  public int compare(JSONObject a, JSONObject b) {
    Double valA = 0.0d;
    Double valB = 0.0d;

    try {
      valA = a.getDouble(key);
      valB = b.getDouble(key);
    } catch (JSONException e) {
      System.err.println("The provided value in " + key + " is not a double.");
    }

    return -valA.compareTo(valB);
  }
}
