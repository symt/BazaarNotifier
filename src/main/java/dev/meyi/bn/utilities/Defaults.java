package dev.meyi.bn.utilities;

import org.json.JSONArray;

public class Defaults {

  public static final int SUGGESTION_MODULE_X = 5;
  public static final int SUGGESTION_MODULE_Y = 10;
  public static final int BANK_MODULE_X = 5;
  public static final int BANK_MODULE_Y = 10;
  public static final int NOTIFICATION_MODULE_X = 5;
  public static final int NOTIFICATION_MODULE_Y = 10;
  public static final int CRAFTING_MODULE_X = 5;
  public static final int CRAFTING_MODULE_Y = 10;
  public static final int CRAFTING_LIST_LENGTH = 10;
  public static final int SUGGESTION_LIST_LENGTH = 10;
  public static final int CRAFTING_SORTING_OPTION = 0;
  public static final boolean INSTASELL_PROFIT = true;
  public static final boolean SELLOFFER_PROFIT = true;
  public static final boolean PROFIT_PER_MIL = true;
  public static final boolean COLLECTION_CHECKING = false;
  public static JSONArray DEFAULT_ORDERS_LAYOUT() {
    return new JSONArray();
  }
}
