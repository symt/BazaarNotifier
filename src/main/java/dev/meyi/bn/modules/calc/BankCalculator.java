package dev.meyi.bn.modules.calc;


import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import dev.meyi.bn.BazaarNotifier;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;


public class BankCalculator {

  public static double bank = 0;
  public static double moneyNotFromBazaar = 0;
  public static double moneyOnBazaarLeave = 0;
  private static double moneyOnStartup =
      moneyStoredInSellOffers() + moneyStoredInBuyOrders() + getPurse();

  public static double calculateProfit() {
    return getPurse() + moneyStoredInBuyOrders() + moneyStoredInSellOffers() + bank
        - moneyOnStartup;
  }


  public static double moneyStoredInSellOffers() {
    if (!BazaarNotifier.orders.isEmpty()) {
      double orderWorth = 0;
      for (int i = 0; i < BazaarNotifier.orders.length(); i++) {
        if (BazaarNotifier.orders.getJSONObject(i).getString("type") == "sell") {
          orderWorth += BazaarNotifier.orders.getJSONObject(i).getInt("orderValue");
        }
      }
      return orderWorth;
    }
    return 0;
  }

  public static double moneyStoredInBuyOrders() {
    if (!BazaarNotifier.orders.isEmpty()) {
      double orderWorth = 0;
      for (int i = 0; i < BazaarNotifier.orders.length(); i++) {
        if (BazaarNotifier.orders.getJSONObject(i).getString("type") == "buy") {
          orderWorth += BazaarNotifier.orders.getJSONObject(i).getInt("orderValue");
        }
      }
      return orderWorth;
    }
    return 0;
  }

  public static double getPurse() {
    if (getPurseFromSidebar() == -1) {
      return getPurseFromAPI();
    } else {
      return getPurseFromSidebar();
    }
  }

  public static double getPurseFromAPI() {
    if (!BazaarNotifier.playerDataFromAPI.isEmpty()) {
      return BazaarNotifier.playerDataFromAPI.getDouble("coin_purse");
    }
    return -1;
  }

  private static double getPurseFromSidebar() {
    Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
    if (scoreboard == null) {
      return -1;
    }

    ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);

    if (objective == null) {
      return -1;
    }

    Collection<Score> scores = scoreboard.getSortedScores(objective);
    List<Score> list = scores.stream()
        .filter(input -> input != null && input.getPlayerName() != null && !input.getPlayerName()
            .startsWith("#")).collect(Collectors.toList());

    if (list.size() > 15) {
      scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
    } else {
      scores = list;
    }

    for (Score score : scores) {
      ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
      if (ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()).contains("Purse")) {
        if (ScorePlayerTeam.formatPlayerName(team, score.getPlayerName())
            .contains(")")) {// coins get added to your purse
          String purse = StringUtils
              .stripControlCodes(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
          int i = purse.indexOf("(");
          String s = purse.substring(i + 1, purse.length() - 1);
          purse = purse.replace(s, "").replaceAll("[^0-9 .]", "");
          return Float.parseFloat(purse);
        } else {
          String purse = StringUtils
              .stripControlCodes(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()))
              .replaceAll("[^0-9 +.]", "");
          return Float.parseFloat(purse);
        }
      }
    }

    return -1;
  }

  public static void extractBankFromItemDescription(IInventory chest) {
    if (chest != null) {
      if (chest.getStackInSlot(11) != null) {
        if (chest.getStackInSlot(11).getDisplayName().toLowerCase().contains("deposit coins")) {
          if (bank == 0) {
            bank = Double.parseDouble(StringUtils.stripControlCodes(
                chest.getStackInSlot(11).getTagCompound().getCompoundTag("display")
                    .getTagList("Lore", 8)
                    .getStringTagAt(0)).split("balance: ")[1].replaceAll(",", ""));
            moneyOnStartup += bank;
          }
          bank = Double.parseDouble(StringUtils.stripControlCodes(
              chest.getStackInSlot(11).getTagCompound().getCompoundTag("display")
                  .getTagList("Lore", 8)
                  .getStringTagAt(0)).split("balance: ")[1].replaceAll(",", ""));
        }
      }
    }
  }

  public static void reset() {
    moneyOnStartup = getPurse() + moneyStoredInBuyOrders() + moneyStoredInSellOffers() + bank;
    moneyNotFromBazaar = 0;
    moneyOnBazaarLeave = 0;
  }
}
